package com.paulsnow.sesmail;

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

    public String from() {
        return from;
    }

    public String to() {
        return to;
    }

    public String subject() {
        return subject;
    }

    public String body() {
        return body;
    }

    public String awsRegion() {
        return awsRegion;
    }

    public boolean dryRun() {
        return dryRun;
    }

    public boolean verbose() {
        return verbose;
    }

    public String outputFormat() {
        return outputFormat;
    }

    public String sesConfigurationSet() {
        return sesConfigurationSet;
    }

    public String sesEndpointOverride() {
        return sesEndpointOverride;
    }

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
        private String awsRegion;
        private Boolean dryRun;
        private Boolean verbose;
        private String outputFormat;
        private String sesConfigurationSet;
        private String sesEndpointOverride;

        /**
         * Merges values from the {@link DotenvLoader} into this builder,
         * only overwriting fields that have not been explicitly set via CLI.
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
            if (awsRegion == null || awsRegion.isBlank()) {
                awsRegion = dotenv.getOrDefault("AWS_REGION", "us-east-1");
            }
            if (dryRun == null) {
                String dryRunStr = dotenv.getOrDefault("MAIL_DRY_RUN", "false");
                dryRun = Boolean.parseBoolean(dryRunStr);
            }
            if (verbose == null) {
                String verboseStr = dotenv.getOrDefault(
                    "MAIL_VERBOSE",
                    "false"
                );
                verbose = Boolean.parseBoolean(verboseStr);
            }
            if (outputFormat == null || outputFormat.isBlank()) {
                outputFormat = dotenv.getOrDefault("MAIL_OUTPUT", "text");
            }
            if (sesConfigurationSet == null || sesConfigurationSet.isBlank()) {
                sesConfigurationSet = dotenv.getOrDefault(
                    "SES_CONFIGURATION_SET",
                    null
                );
            }
            if (sesEndpointOverride == null || sesEndpointOverride.isBlank()) {
                sesEndpointOverride = dotenv.getOrDefault(
                    "SES_ENDPOINT_OVERRIDE",
                    null
                );
            }
            return this;
        }

        public Builder from(String from) {
            this.from = from;
            return this;
        }

        public Builder to(String to) {
            this.to = to;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder awsRegion(String awsRegion) {
            if (awsRegion != null && !awsRegion.isBlank()) this.awsRegion =
                awsRegion;
            return this;
        }

        public Builder dryRun(boolean dryRun) {
            this.dryRun = dryRun;
            return this;
        }

        public Builder verbose(boolean verbose) {
            this.verbose = verbose;
            return this;
        }

        public Builder outputFormat(String outputFormat) {
            if (
                outputFormat != null && !outputFormat.isBlank()
            ) this.outputFormat = outputFormat;
            return this;
        }

        public Builder sesConfigurationSet(String s) {
            this.sesConfigurationSet = s;
            return this;
        }

        public Builder sesEndpointOverride(String s) {
            this.sesEndpointOverride = s;
            return this;
        }

        public AppConfig build() {
            if (awsRegion == null || awsRegion.isBlank()) awsRegion =
                "us-east-1";
            if (dryRun == null) dryRun = false;
            if (verbose == null) verbose = false;
            if (outputFormat == null || outputFormat.isBlank()) outputFormat =
                "text";
            return new AppConfig(this);
        }
    }
}
