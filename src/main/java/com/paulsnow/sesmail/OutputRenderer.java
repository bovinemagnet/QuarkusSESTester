package com.paulsnow.sesmail;

import java.time.Instant;

/**
 * Renders output to stdout in text or JSON format.
 * <p>
 * Secrets (AWS keys, session tokens) must never be printed. This renderer is responsible
 * for masking any sensitive values passed through.
 */
public class OutputRenderer {

    private final String format; // "text" or "json"

    public OutputRenderer(String format) {
        this.format = (format != null && format.equalsIgnoreCase("json"))
            ? "json"
            : "text";
    }

    /**
     * Renders the dry-run preview.
     */
    public void renderDryRun(AppConfig config, String mimeHeaderPreview) {
        if ("json".equals(format)) {
            System.out.println("{");
            System.out.println("  \"mode\": \"DRY_RUN\",");
            System.out.println(
                "  \"awsRegion\": " + jsonString(config.awsRegion()) + ","
            );
            System.out.println(
                "  \"from\": " + jsonString(config.from()) + ","
            );
            System.out.println("  \"to\": " + jsonString(config.to()) + ",");
            System.out.println(
                "  \"subject\": " + jsonString(config.subject()) + ","
            );
            System.out.println("  \"sesApiCall\": \"skipped\",");
            System.out.println("  \"mimeValid\": true");
            System.out.println("}");
        } else {
            System.out.println("Mode:        DRY_RUN");
            System.out.println("AWS Region:  " + nullSafe(config.awsRegion()));
            System.out.println("From:        " + nullSafe(config.from()));
            System.out.println("To:          " + nullSafe(config.to()));
            System.out.println("Subject:     " + nullSafe(config.subject()));
            System.out.println("SES API Call: skipped");
            System.out.println("MIME:        valid");
            if (mimeHeaderPreview != null && !mimeHeaderPreview.isBlank()) {
                System.out.println();
                System.out.println("--- MIME headers ---");
                System.out.print(mimeHeaderPreview);
                System.out.println("--- body preview ---");
                String bodyPreview = config.body();
                if (bodyPreview != null && bodyPreview.length() > 200) {
                    bodyPreview = bodyPreview.substring(0, 200) + "...";
                }
                System.out.println(bodyPreview);
            }
            if (
                config.sesConfigurationSet() != null &&
                !config.sesConfigurationSet().isBlank()
            ) {
                System.out.println(
                    "SES Config Set: " + config.sesConfigurationSet()
                );
            }
            System.out.println();
            System.out.println(
                "NOTE: SES prerequisites (verified identity, sandbox status, DNS) are not"
            );
            System.out.println(
                "      checked in dry-run mode. These must be configured externally."
            );
        }
    }

    /**
     * Renders a successful send result.
     */
    public void renderSuccess(String messageId, AppConfig config) {
        String timestamp = Instant.now().toString();
        if ("json".equals(format)) {
            System.out.println("{");
            System.out.println("  \"status\": \"SUCCESS\",");
            System.out.println(
                "  \"sesMessageId\": " + jsonString(messageId) + ","
            );
            System.out.println(
                "  \"from\": " + jsonString(config.from()) + ","
            );
            System.out.println("  \"to\": " + jsonString(config.to()) + ",");
            System.out.println(
                "  \"region\": " + jsonString(config.awsRegion()) + ","
            );
            System.out.println("  \"timestamp\": " + jsonString(timestamp));
            System.out.println("}");
        } else {
            System.out.println("Status:       SUCCESS");
            System.out.println("SES Message ID: " + messageId);
            System.out.println("From:         " + nullSafe(config.from()));
            System.out.println("To:           " + nullSafe(config.to()));
            System.out.println("Region:       " + nullSafe(config.awsRegion()));
            System.out.println("Timestamp:    " + timestamp);
        }
    }

    /**
     * Renders an error message to stderr.
     */
    public void renderError(String message) {
        System.err.println(message);
    }

    /**
     * Renders warnings to stderr.
     */
    public void renderWarning(String message) {
        System.err.println(message);
    }

    /**
     * Renders verbose config info, masking all secret values.
     */
    public void renderVerboseConfig(AppConfig config) {
        System.out.println("[verbose] Configuration sources resolved:");
        System.out.println(
            "[verbose]   from:       " + nullSafe(config.from())
        );
        System.out.println("[verbose]   to:         " + nullSafe(config.to()));
        System.out.println(
            "[verbose]   region:     " + nullSafe(config.awsRegion())
        );
        System.out.println("[verbose]   dryRun:     " + config.dryRun());
        System.out.println(
            "[verbose]   output:     " + nullSafe(config.outputFormat())
        );
        // Secrets are never printed - AWS credentials are handled by the SDK credential chain
        System.out.println(
            "[verbose]   AWS credentials: resolved by AWS SDK default credential chain"
        );
        System.out.println(
            "[verbose]   AWS_ACCESS_KEY_ID: " + maskEnv("AWS_ACCESS_KEY_ID")
        );
        System.out.println(
            "[verbose]   AWS_SESSION_TOKEN: " + maskEnv("AWS_SESSION_TOKEN")
        );
    }

    private String maskEnv(String key) {
        String val = System.getenv(key);
        if (val == null || val.isBlank()) return "(not set)";
        return val.substring(0, Math.min(4, val.length())) + "****";
    }

    private String nullSafe(String s) {
        return s != null ? s : "(not set)";
    }

    private String jsonString(String s) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"' -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
        return sb.toString();
    }
}
