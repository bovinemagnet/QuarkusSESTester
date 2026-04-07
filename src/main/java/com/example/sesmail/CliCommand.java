package com.example.sesmail;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;

/**
 * Main CLI command for ses-mail-cli.
 * <p>
 * Compose and send a plain-text email through Amazon SES API v2 (HTTPS),
 * or validate the configuration with {@code --dry-run} without actually calling SES.
 *
 * <h2>Prerequisites (must be configured externally)</h2>
 * <ul>
 *   <li>The sender email address or domain must be verified in the target SES region.</li>
 *   <li>For DMARC alignment, SPF/DKIM and optionally a custom MAIL FROM must be configured.</li>
 *   <li>If the AWS account is in the SES sandbox, only verified recipients are permitted.</li>
 *   <li>SES identity verification is regional; ensure the correct region is specified.</li>
 * </ul>
 *
 * <h2>Security</h2>
 * <ul>
 *   <li>AWS credentials are resolved by the AWS SDK default credential chain and are never printed.</li>
 *   <li>The {@code --send-from} address must correspond to a sender identity you own and have verified in SES.</li>
 * </ul>
 */
@Command(
        name = "ses-mail-cli",
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = {
                "Send a plain-text email through Amazon SES API v2 (HTTPS).",
                "The sender identity must be verified in SES; see AWS docs for prerequisites.",
                "",
                "NOTE: SES cannot be used to impersonate domains you do not own or control.",
                "      --send-from must correspond to an SES-verified sender identity."
        }
)
public class CliCommand implements Runnable {

    @Option(names = "--dry-run",
            description = "Validate and preview the message without calling SES.")
    boolean dryRun;

    @Option(names = "--send-from",
            description = "Sender email address (must be SES-verified).")
    String sendFrom;

    @Option(names = "--send-to",
            description = "Recipient email address.")
    String sendTo;

    @Option(names = "--send-subject",
            description = "Email subject line.")
    String sendSubject;

    @Option(names = "--send-body",
            description = "Email plain-text body.")
    String sendBody;

    @Option(names = "--aws-region",
            description = "AWS region for SES (default: from AWS_REGION env or .env).")
    String awsRegion;

    @Option(names = "--env-file",
            description = "Path to a .env file (default: ./.env).")
    String envFile;

    @Option(names = "--verbose",
            description = "Enable verbose output (masks secrets).")
    boolean verbose;

    @Option(names = "--output",
            description = "Output format: text (default) or json.")
    String outputFormat;

    @Override
    public void run() {
        // 1. Load .env
        DotenvLoader dotenv = new DotenvLoader(envFile != null ? envFile : ".");

        // 2. Build merged config (CLI > env > .env > defaults)
        AppConfig config = AppConfig.builder()
                .from(sendFrom)
                .to(sendTo)
                .subject(sendSubject)
                .body(sendBody)
                .awsRegion(awsRegion)
                .dryRun(dryRun)
                .verbose(verbose)
                .outputFormat(outputFormat)
                .mergeFromDotenv(dotenv)
                .build();

        OutputRenderer renderer = new OutputRenderer(config.outputFormat());

        if (config.verbose()) {
            renderer.renderVerboseConfig(config);
        }

        // 3. Validate
        MailValidator validator = new MailValidator();
        MailRequest request = config.toMailRequest();

        List<String> errors = validator.validate(request);
        List<String> regionErrors = validator.validateRegion(config.awsRegion());
        errors.addAll(regionErrors);

        if (!errors.isEmpty()) {
            errors.forEach(renderer::renderError);
            System.exit(ExitCode.VALIDATION_ERROR.code());
            return;
        }

        // 4. Emit warnings
        validator.warnings(request).forEach(renderer::renderWarning);

        // 5. Build MIME message
        MimeMessageFactory mimeFactory = new MimeMessageFactory();
        byte[] rawMimeBytes;
        String mimeHeaderPreview;
        try {
            rawMimeBytes = mimeFactory.buildRawMimeBytes(request);
            mimeHeaderPreview = mimeFactory.buildMimeHeaderPreview(request);
        } catch (Exception e) {
            renderer.renderError("ERROR: failed to build MIME message: " + e.getMessage());
            System.exit(ExitCode.RUNTIME_ERROR.code());
            return;
        }

        // 6. Dry-run: print preview and exit
        if (config.dryRun()) {
            renderer.renderDryRun(config, mimeHeaderPreview);
            System.exit(ExitCode.SUCCESS.code());
            return;
        }

        // 7. Real send
        SesMailSender sender = new SesMailSender(
                config.awsRegion(),
                config.sesEndpointOverride(),
                config.sesConfigurationSet());
        try {
            String messageId = sender.send(rawMimeBytes, config.sesConfigurationSet());
            renderer.renderSuccess(messageId, config);
            System.exit(ExitCode.SUCCESS.code());
        } catch (SesMailException e) {
            renderer.renderError(e.getMessage());
            System.exit(e.exitCode().code());
        } finally {
            sender.close();
        }
    }
}
