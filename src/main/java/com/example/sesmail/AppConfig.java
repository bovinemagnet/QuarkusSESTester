package com.example.sesmail;

/**
 * Merged application configuration, assembled from CLI args, environment variables, .env file,
 * and built-in defaults.
 * <p>
 * Precedence order (highest to lowest):
 * <ol>
 *   <li>CLI arguments</li>
 *   <li>Environment variables (from the shell process)</li>
 *   <li>.env file values</li>
 *   <li>Built-in defaults</li>
 * </ol>
 */
public class AppConfig {

    private final String from;
    private final String to;
    private final String subject;
    private final String body;
    private final String awsRegion;
    private final boolean dryRun;
    private final boolean verbose;
    private final String outputFormat;
    private final String sesConfigurationSet;
    private final String sesEndpointOverride;

    private AppConfig(Builder builder) {
        this.from = builder.from;
        this.to = builder.to;
        this.subject = builder.subject;
        this.body = builder.body;
        this.awsRegion = builder.awsRegion;
        this.dryRun = builder.dryRun;
        this.verbose = builder.verbose;
        this.outputFormat = builder.outputFormat;
        this.sesConfigurationSet = builder.sesConfigurationSet;
        this.sesEndpointOverride = builder.sesEndpointOverride;
    }

    public String from() { return from; }
    public String to() { return to; }
    public String subject() { return subject; }
    public String body() { return body; }
    public String awsRegion() { return awsRegion; }
    public boolean dryRun() { return dryRun; }
    public boolean verbose() { return verbose; }
    public String outputFormat() { return outputFormat; }
    public String sesConfigurationSet() { return sesConfigurationSet; }
    public String sesEndpointOverride() { return sesEndpointOverride; }

    public MailRequest toMailRequest() {
        return new MailRequest(from, to, subject, body);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String from;
        private String to;
        private String subject;
        private String body;
        private String awsRegion = "us-east-1";
        private boolean dryRun = false;
        private boolean verbose = false;
        private String outputFormat = "text";
        private String sesConfigurationSet;
        private String sesEndpointOverride;

        /**
         * Merges values from the {@link DotenvLoader} into this builder,
         * only overwriting fields that are still null/default.
         * CLI-supplied values should be set before calling this method.
         */
        public Builder mergeFromDotenv(DotenvLoader dotenv) {
            if (from == null || from.isBlank()) {
                from = dotenv.getOrDefault("MAIL_SEND_FROM", null);
            }
            if (to == null || to.isBlank()) {
                to = dotenv.getOrDefault("MAIL_SEND_TO", null);
            }
            if (subject == null || subject.isBlank()) {
                subject = dotenv.getOrDefault("MAIL_SEND_SUBJECT", null);
            }
            if (body == null || body.isBlank()) {
                body = dotenv.getOrDefault("MAIL_SEND_BODY", null);
            }
            // AWS_REGION: use CLI value if already set, otherwise check env
            if ("us-east-1".equals(awsRegion)) {
                String region = dotenv.getOrDefault("AWS_REGION", null);
                if (region != null && !region.isBlank()) {
                    awsRegion = region;
                }
            }
            if (!dryRun) {
                String dryRunStr = dotenv.getOrDefault("MAIL_DRY_RUN", "false");
                dryRun = Boolean.parseBoolean(dryRunStr);
            }
            if (!verbose) {
                String verboseStr = dotenv.getOrDefault("MAIL_VERBOSE", "false");
                verbose = Boolean.parseBoolean(verboseStr);
            }
            if ("text".equals(outputFormat)) {
                String fmt = dotenv.getOrDefault("MAIL_OUTPUT", null);
                if (fmt != null && !fmt.isBlank()) {
                    outputFormat = fmt;
                }
            }
            if (sesConfigurationSet == null || sesConfigurationSet.isBlank()) {
                sesConfigurationSet = dotenv.getOrDefault("SES_CONFIGURATION_SET", null);
            }
            if (sesEndpointOverride == null || sesEndpointOverride.isBlank()) {
                sesEndpointOverride = dotenv.getOrDefault("SES_ENDPOINT_OVERRIDE", null);
            }
            return this;
        }

        public Builder from(String from) { this.from = from; return this; }
        public Builder to(String to) { this.to = to; return this; }
        public Builder subject(String subject) { this.subject = subject; return this; }
        public Builder body(String body) { this.body = body; return this; }
        public Builder awsRegion(String awsRegion) {
            if (awsRegion != null && !awsRegion.isBlank()) this.awsRegion = awsRegion;
            return this;
        }
        public Builder dryRun(boolean dryRun) { this.dryRun = dryRun; return this; }
        public Builder verbose(boolean verbose) { this.verbose = verbose; return this; }
        public Builder outputFormat(String outputFormat) {
            if (outputFormat != null && !outputFormat.isBlank()) this.outputFormat = outputFormat;
            return this;
        }
        public Builder sesConfigurationSet(String s) { this.sesConfigurationSet = s; return this; }
        public Builder sesEndpointOverride(String s) { this.sesEndpointOverride = s; return this; }

        public AppConfig build() {
            return new AppConfig(this);
        }
    }
}
