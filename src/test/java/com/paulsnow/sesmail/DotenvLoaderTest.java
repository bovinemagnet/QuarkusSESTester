package com.paulsnow.sesmail;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DotenvLoaderTest {

    @Test
    void missingDotenvFile_loadsWithoutError() {
        // Should not throw when .env doesn't exist
        DotenvLoader loader = new DotenvLoader("/tmp/nonexistent-path-xyz");
        Optional<String> value = loader.get("MAIL_SEND_FROM");
        assertThat(value).isEmpty();
    }

    @Test
    void dotenvFile_loadsValues(@TempDir Path tempDir) throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(
            envFile,
            """
            MAIL_SEND_FROM=no-reply@example.com
            MAIL_SEND_TO=user@example.com
            MAIL_SEND_SUBJECT=Hello
            MAIL_SEND_BODY=Test body
            AWS_REGION=ap-southeast-2
            MAIL_DRY_RUN=true
            """
        );

        DotenvLoader loader = new DotenvLoader(tempDir.toString());

        assertThat(loader.get("MAIL_SEND_FROM")).contains(
            "no-reply@example.com"
        );
        assertThat(loader.get("MAIL_SEND_TO")).contains("user@example.com");
        assertThat(loader.get("MAIL_SEND_SUBJECT")).contains("Hello");
        assertThat(loader.get("AWS_REGION")).contains("ap-southeast-2");
        assertThat(loader.get("MAIL_DRY_RUN")).contains("true");
    }

    @Test
    void dotenvFile_missingKey_returnsEmpty(@TempDir Path tempDir)
        throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "EXISTING_KEY=value\n");

        DotenvLoader loader = new DotenvLoader(tempDir.toString());
        assertThat(loader.get("NOT_IN_FILE")).isEmpty();
    }

    @Test
    void getOrDefault_returnsDefaultWhenAbsent() {
        DotenvLoader loader = new DotenvLoader("/tmp/nonexistent-path-xyz");
        String result = loader.getOrDefault("TOTALLY_MISSING_KEY", "fallback");
        assertThat(result).isEqualTo("fallback");
    }
}
