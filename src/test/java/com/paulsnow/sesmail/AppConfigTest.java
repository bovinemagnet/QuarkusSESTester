package com.paulsnow.sesmail;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppConfigTest {

    @Test
    void builder_setsValuesCorrectly() {
        AppConfig config = AppConfig.builder()
            .from("from@example.com")
            .to("to@example.com")
            .subject("My Subject")
            .body("My body")
            .awsRegion("us-west-2")
            .dryRun(true)
            .verbose(false)
            .outputFormat("json")
            .build();

        assertThat(config.from()).isEqualTo("from@example.com");
        assertThat(config.to()).isEqualTo("to@example.com");
        assertThat(config.subject()).isEqualTo("My Subject");
        assertThat(config.body()).isEqualTo("My body");
        assertThat(config.awsRegion()).isEqualTo("us-west-2");
        assertThat(config.dryRun()).isTrue();
        assertThat(config.verbose()).isFalse();
        assertThat(config.outputFormat()).isEqualTo("json");
    }

    @Test
    void builder_defaultRegion_isUsEast1() {
        AppConfig config = AppConfig.builder().build();
        assertThat(config.awsRegion()).isEqualTo("us-east-1");
    }

    @Test
    void builder_dryRunDefault_isFalse() {
        AppConfig config = AppConfig.builder().build();
        assertThat(config.dryRun()).isFalse();
    }

    @Test
    void toMailRequest_mapsFields() {
        AppConfig config = AppConfig.builder()
            .from("a@b.com")
            .to("c@d.com")
            .subject("Subj")
            .body("Body")
            .build();

        MailRequest req = config.toMailRequest();
        assertThat(req.from()).isEqualTo("a@b.com");
        assertThat(req.to()).isEqualTo("c@d.com");
        assertThat(req.subject()).isEqualTo("Subj");
        assertThat(req.body()).isEqualTo("Body");
    }

    @Test
    void builder_nullRegion_usesDefault() {
        AppConfig config = AppConfig.builder().awsRegion(null).build();
        assertThat(config.awsRegion()).isEqualTo("us-east-1");
    }

    @Test
    void builder_blankRegion_usesDefault() {
        AppConfig config = AppConfig.builder().awsRegion("   ").build();
        assertThat(config.awsRegion()).isEqualTo("us-east-1");
    }

    @Test
    void mergeFromDotenv_doesNotOverrideExplicitRegion(@TempDir Path tempDir)
        throws IOException {
        Files.writeString(tempDir.resolve(".env"), "AWS_REGION=eu-west-1\n");
        DotenvLoader dotenv = new DotenvLoader(tempDir.toString());

        AppConfig config = AppConfig.builder()
            .awsRegion("us-east-1")
            .mergeFromDotenv(dotenv)
            .build();

        assertThat(config.awsRegion()).isEqualTo("us-east-1");
    }

    @Test
    void mergeFromDotenv_fillsUnsetRegion(@TempDir Path tempDir)
        throws IOException {
        Files.writeString(tempDir.resolve(".env"), "AWS_REGION=eu-west-1\n");
        DotenvLoader dotenv = new DotenvLoader(tempDir.toString());

        AppConfig config = AppConfig.builder().mergeFromDotenv(dotenv).build();

        assertThat(config.awsRegion()).isEqualTo("eu-west-1");
    }

    @Test
    void mergeFromDotenv_doesNotOverrideExplicitDryRun(@TempDir Path tempDir)
        throws IOException {
        Files.writeString(tempDir.resolve(".env"), "MAIL_DRY_RUN=true\n");
        DotenvLoader dotenv = new DotenvLoader(tempDir.toString());

        AppConfig config = AppConfig.builder()
            .dryRun(false)
            .mergeFromDotenv(dotenv)
            .build();

        assertThat(config.dryRun()).isFalse();
    }

    @Test
    void mergeFromDotenv_fillsUnsetOutputFormat(@TempDir Path tempDir)
        throws IOException {
        Files.writeString(tempDir.resolve(".env"), "MAIL_OUTPUT=json\n");
        DotenvLoader dotenv = new DotenvLoader(tempDir.toString());

        AppConfig config = AppConfig.builder().mergeFromDotenv(dotenv).build();

        assertThat(config.outputFormat()).isEqualTo("json");
    }

    @Test
    void mergeFromDotenv_doesNotOverrideExplicitOutputFormat(
        @TempDir Path tempDir
    ) throws IOException {
        Files.writeString(tempDir.resolve(".env"), "MAIL_OUTPUT=json\n");
        DotenvLoader dotenv = new DotenvLoader(tempDir.toString());

        AppConfig config = AppConfig.builder()
            .outputFormat("text")
            .mergeFromDotenv(dotenv)
            .build();

        assertThat(config.outputFormat()).isEqualTo("text");
    }
}
