package com.paulsnow.sesmail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import org.junit.jupiter.api.Test;

class MimeMessageFactoryTest {

    private final MimeMessageFactory factory = new MimeMessageFactory();

    @Test
    void buildRawMimeBytes_returnsNonEmptyBytes() throws Exception {
        MailRequest request = new MailRequest(
            "from@example.com",
            "to@example.com",
            "Test Subject",
            "Hello from test"
        );
        byte[] bytes = factory.buildRawMimeBytes(request);
        assertThat(bytes).isNotEmpty();
    }

    @Test
    void buildRawMimeBytes_containsExpectedHeaders() throws Exception {
        MailRequest request = new MailRequest(
            "sender@test.com",
            "recipient@test.com",
            "My Subject",
            "My body"
        );
        byte[] bytes = factory.buildRawMimeBytes(request);
        String raw = new String(bytes);

        assertThat(raw).contains("From:");
        assertThat(raw).contains("To:");
        assertThat(raw).contains("Subject:");
        assertThat(raw).contains("MIME-Version:");
        assertThat(raw).contains("Content-Type:");
    }

    @Test
    void buildRawMimeBytes_containsBodyText() throws Exception {
        MailRequest request = new MailRequest(
            "sender@test.com",
            "recipient@test.com",
            "Subject",
            "Unique body content 12345"
        );
        byte[] bytes = factory.buildRawMimeBytes(request);
        String raw = new String(bytes);
        assertThat(raw).contains("Unique body content 12345");
    }

    @Test
    void buildMimeHeaderPreview_returnsHeaders() throws Exception {
        MailRequest request = new MailRequest(
            "from@example.com",
            "to@example.com",
            "Preview Subject",
            "Preview body"
        );
        String headers = factory.buildMimeHeaderPreview(request);
        assertThat(headers).isNotBlank();
        assertThat(headers).contains("Subject:");
    }

    @Test
    void buildRawMimeBytes_handlesSpecialCharactersInSubject()
        throws Exception {
        MailRequest request = new MailRequest(
            "from@example.com",
            "to@example.com",
            "Test: with colon & ampersand",
            "Body text"
        );
        assertThatNoException().isThrownBy(() ->
            factory.buildRawMimeBytes(request)
        );
    }
}
