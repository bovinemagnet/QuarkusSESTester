package com.example.sesmail;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MailValidatorTest {

    private final MailValidator validator = new MailValidator();

    @Test
    void validRequest_hasNoErrors() {
        MailRequest request = new MailRequest(
                "no-reply@customer.example.com",
                "user@customer.example.com",
                "Test subject",
                "Test body text");
        List<String> errors = validator.validate(request);
        assertThat(errors).isEmpty();
    }

    @Test
    void missingFrom_producesError() {
        MailRequest request = new MailRequest(null, "user@example.com", "Subject", "Body");
        List<String> errors = validator.validate(request);
        assertThat(errors).anyMatch(e -> e.contains("--send-from"));
    }

    @Test
    void blankFrom_producesError() {
        MailRequest request = new MailRequest("  ", "user@example.com", "Subject", "Body");
        List<String> errors = validator.validate(request);
        assertThat(errors).anyMatch(e -> e.contains("--send-from"));
    }

    @Test
    void invalidFromEmail_producesError() {
        MailRequest request = new MailRequest("not-an-email", "user@example.com", "Subject", "Body");
        List<String> errors = validator.validate(request);
        assertThat(errors).anyMatch(e -> e.contains("--send-from"));
    }

    @Test
    void missingTo_producesError() {
        MailRequest request = new MailRequest("from@example.com", null, "Subject", "Body");
        List<String> errors = validator.validate(request);
        assertThat(errors).anyMatch(e -> e.contains("--send-to"));
    }

    @Test
    void invalidToEmail_producesError() {
        MailRequest request = new MailRequest("from@example.com", "bad-email", "Subject", "Body");
        List<String> errors = validator.validate(request);
        assertThat(errors).anyMatch(e -> e.contains("--send-to"));
    }

    @Test
    void blankSubject_producesError() {
        MailRequest request = new MailRequest("from@example.com", "to@example.com", "", "Body");
        List<String> errors = validator.validate(request);
        assertThat(errors).anyMatch(e -> e.contains("--send-subject"));
    }

    @Test
    void blankBody_producesError() {
        MailRequest request = new MailRequest("from@example.com", "to@example.com", "Subject", "   ");
        List<String> errors = validator.validate(request);
        assertThat(errors).anyMatch(e -> e.contains("--send-body"));
    }

    @Test
    void missingRegion_producesError() {
        List<String> errors = validator.validateRegion(null);
        assertThat(errors).isNotEmpty();

        errors = validator.validateRegion("");
        assertThat(errors).isNotEmpty();
    }

    @Test
    void validRegion_hasNoErrors() {
        List<String> errors = validator.validateRegion("ap-southeast-2");
        assertThat(errors).isEmpty();
    }

    @Test
    void gmailSender_producesWarning() {
        MailRequest request = new MailRequest("user@gmail.com", "to@example.com", "Subj", "Body");
        List<String> warnings = validator.warnings(request);
        assertThat(warnings).isNotEmpty();
    }

    @Test
    void differentDomains_producesWarning() {
        MailRequest request = new MailRequest(
                "from@domain-a.com", "to@domain-b.com", "Subj", "Body");
        List<String> warnings = validator.warnings(request);
        assertThat(warnings).anyMatch(w -> w.contains("different domains"));
    }

    @Test
    void sameDomain_noWarningForDomainMismatch() {
        MailRequest request = new MailRequest(
                "from@example.com", "to@example.com", "Subj", "Body");
        List<String> warnings = validator.warnings(request);
        // No cross-domain warning expected
        assertThat(warnings).noneMatch(w -> w.contains("different domains"));
    }

    @Test
    void emailValidation_validFormats() {
        assertThat(validator.isValidEmail("user@example.com")).isTrue();
        assertThat(validator.isValidEmail("no-reply@subdomain.example.co.uk")).isTrue();
        assertThat(validator.isValidEmail("user+tag@example.org")).isTrue();
    }

    @Test
    void emailValidation_invalidFormats() {
        assertThat(validator.isValidEmail(null)).isFalse();
        assertThat(validator.isValidEmail("")).isFalse();
        assertThat(validator.isValidEmail("not-an-email")).isFalse();
        assertThat(validator.isValidEmail("@example.com")).isFalse();
        assertThat(validator.isValidEmail("user@")).isFalse();
    }
}
