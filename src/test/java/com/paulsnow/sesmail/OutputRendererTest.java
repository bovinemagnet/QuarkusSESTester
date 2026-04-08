package com.paulsnow.sesmail;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.Test;

class OutputRendererTest {

    @Test
    void jsonString_escapesNewlines() {
        OutputRenderer renderer = new OutputRenderer("json");
        AppConfig config = buildConfigWithSubject("Line1\nLine2");

        String output = captureStdout(() ->
            renderer.renderDryRun(config, null)
        );

        assertThat(output).contains("Line1\\nLine2");
    }

    @Test
    void jsonString_escapesTabsAndControlChars() {
        OutputRenderer renderer = new OutputRenderer("json");
        AppConfig config = buildConfigWithSubject("col1\tcol2\r\n");

        String output = captureStdout(() ->
            renderer.renderDryRun(config, null)
        );

        assertThat(output).contains("col1\\tcol2\\r\\n");
    }

    @Test
    void jsonString_escapesBackslashesAndQuotes() {
        OutputRenderer renderer = new OutputRenderer("json");
        AppConfig config = AppConfig.builder()
            .from("test@example.com")
            .to("to@example.com")
            .subject("He said \"hello\\there\"")
            .body("body")
            .awsRegion("us-east-1")
            .dryRun(true)
            .build();

        String output = captureStdout(() ->
            renderer.renderDryRun(config, null)
        );

        assertThat(output).contains("He said \\\"hello\\\\there\\\"");
    }

    @Test
    void textFormat_isDefault() {
        OutputRenderer renderer = new OutputRenderer(null);
        AppConfig config = buildConfig("body");

        String output = captureStdout(() ->
            renderer.renderDryRun(config, null)
        );

        assertThat(output).contains("Mode:        DRY_RUN");
        assertThat(output).doesNotContain("{");
    }

    @Test
    void renderError_writesToStderr() {
        OutputRenderer renderer = new OutputRenderer("text");

        String output = captureStderr(() ->
            renderer.renderError("something failed")
        );

        assertThat(output).contains("something failed");
    }

    @Test
    void renderSuccess_jsonFormat_containsRequiredFields() {
        OutputRenderer renderer = new OutputRenderer("json");
        AppConfig config = buildConfig("body");

        String output = captureStdout(() ->
            renderer.renderSuccess("msg-123", config)
        );

        assertThat(output)
            .contains("\"status\": \"SUCCESS\"")
            .contains("\"sesMessageId\": \"msg-123\"")
            .contains("\"from\": \"test@example.com\"");
    }

    private AppConfig buildConfig(String body) {
        return AppConfig.builder()
            .from("test@example.com")
            .to("to@example.com")
            .subject("Test Subject")
            .body(body)
            .awsRegion("us-east-1")
            .dryRun(true)
            .build();
    }

    private AppConfig buildConfigWithSubject(String subject) {
        return AppConfig.builder()
            .from("test@example.com")
            .to("to@example.com")
            .subject(subject)
            .body("body")
            .awsRegion("us-east-1")
            .dryRun(true)
            .build();
    }

    private String captureStdout(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));
        try {
            action.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString();
    }

    private String captureStderr(Runnable action) {
        PrintStream original = System.err;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setErr(new PrintStream(buffer));
        try {
            action.run();
        } finally {
            System.setErr(original);
        }
        return buffer.toString();
    }
}
