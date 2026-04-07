package com.example.sesmail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.converter.EmailConverter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Builds a raw MIME email message using Simple Java Mail.
 * <p>
 * This class is responsible only for MIME construction. It does not perform any
 * SMTP transmission — SES submission is handled separately by {@link SesMailSender}.
 */
public class MimeMessageFactory {

    /**
     * Constructs a MIME message from the given request.
     *
     * @param request the validated mail request
     * @return the raw MIME bytes ready for submission to SES API
     */
    public byte[] buildRawMimeBytes(MailRequest request) throws MessagingException, IOException {
        Email email = EmailBuilder.startingBlank()
                .from(request.from())
                .to(request.to())
                .withSubject(request.subject())
                .withPlainText(request.body())
                .buildEmail();

        MimeMessage mimeMessage = EmailConverter.emailToMimeMessage(email);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        mimeMessage.writeTo(buffer);
        return buffer.toByteArray();
    }

    /**
     * Returns the MIME headers as a human-readable string for dry-run output.
     */
    public String buildMimeHeaderPreview(MailRequest request) throws MessagingException, IOException {
        Email email = EmailBuilder.startingBlank()
                .from(request.from())
                .to(request.to())
                .withSubject(request.subject())
                .withPlainText(request.body())
                .buildEmail();

        MimeMessage mimeMessage = EmailConverter.emailToMimeMessage(email);

        StringBuilder sb = new StringBuilder();
        java.util.Enumeration<String> headerLines = mimeMessage.getAllHeaderLines();
        while (headerLines.hasMoreElements()) {
            sb.append(headerLines.nextElement()).append("\n");
        }
        return sb.toString();
    }
}
