package com.paulsnow.sesmail;

import java.net.URI;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.SesV2ClientBuilder;
import software.amazon.awssdk.services.sesv2.model.*;

/**
 * Sends raw MIME emails via the Amazon SES v2 HTTPS API using AWS SDK for Java v2.
 * <p>
 * This class does not use SMTP. The raw MIME bytes built by {@link MimeMessageFactory}
 * are submitted directly to the SES {@code SendEmail} API endpoint over HTTPS.
 */
public class SesMailSender {

    private final SesV2Client sesClient;
    private final String configurationSetName;

    /**
     * Creates a sender using the provided AWS region and optional endpoint override.
     *
     * @param region                AWS region, e.g. {@code ap-southeast-2}
     * @param endpointOverride      optional custom endpoint URI (e.g. for LocalStack); may be null
     * @param configurationSetName  optional SES configuration set name; may be null
     */
    public SesMailSender(
        String region,
        String endpointOverride,
        String configurationSetName
    ) {
        SesV2ClientBuilder builder = SesV2Client.builder()
            .region(Region.of(region))
            .httpClient(UrlConnectionHttpClient.create());

        if (endpointOverride != null && !endpointOverride.isBlank()) {
            builder = builder.endpointOverride(URI.create(endpointOverride));
        }

        this.sesClient = builder.build();
        this.configurationSetName = configurationSetName;
    }

    /**
     * Sends the raw MIME message to SES.
     *
     * @param rawMimeBytes the MIME message bytes
     * @return the SES message ID on success
     * @throws SesMailException if SES rejects or cannot process the request
     */
    public String send(byte[] rawMimeBytes) throws SesMailException {
        try {
            RawMessage rawMessage = RawMessage.builder()
                .data(SdkBytes.fromByteArray(rawMimeBytes))
                .build();

            EmailContent.Builder contentBuilder = EmailContent.builder().raw(
                rawMessage
            );

            SendEmailRequest.Builder requestBuilder =
                SendEmailRequest.builder().content(contentBuilder.build());

            if (
                configurationSetName != null && !configurationSetName.isBlank()
            ) {
                requestBuilder = requestBuilder.configurationSetName(
                    configurationSetName
                );
            }

            SendEmailResponse response = sesClient.sendEmail(
                requestBuilder.build()
            );
            return response.messageId();
        } catch (MailFromDomainNotVerifiedException e) {
            throw new SesMailException(
                "ERROR: sender identity not verified in SES region. " +
                    "Verify the sender domain or address in your SES console. " +
                    "Detail: " +
                    e.getMessage(),
                e,
                ExitCode.SES_REJECTION
            );
        } catch (AccountSuspendedException | SendingPausedException e) {
            throw new SesMailException(
                "ERROR: SES account sending is suspended or paused: " +
                    e.getMessage(),
                e,
                ExitCode.SES_REJECTION
            );
        } catch (software.amazon.awssdk.core.exception.SdkClientException e) {
            throw new SesMailException(
                "ERROR: AWS client configuration failure (check region, credentials, network): " +
                    e.getMessage(),
                e,
                ExitCode.AUTH_ERROR
            );
        } catch (SesV2Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("sandbox")) {
                throw new SesMailException(
                    "ERROR: account appears to be in SES sandbox; recipient must be verified. " +
                        e.getMessage(),
                    e,
                    ExitCode.SES_REJECTION
                );
            }
            if (
                e.awsErrorDetails() != null &&
                "InvalidClientTokenId".equals(e.awsErrorDetails().errorCode())
            ) {
                throw new SesMailException(
                    "ERROR: invalid AWS credentials: " + e.getMessage(),
                    e,
                    ExitCode.AUTH_ERROR
                );
            }
            throw new SesMailException(
                "ERROR: SES API error: " + e.getMessage(),
                e,
                ExitCode.SES_REJECTION
            );
        }
    }

    /**
     * Releases the underlying SES client resources.
     */
    public void close() {
        sesClient.close();
    }
}
