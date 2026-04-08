package com.paulsnow.sesmail;

/**
 * Holds the configuration for a single email send request.
 */
public record MailRequest(
    String from,
    String to,
    String subject,
    String body
) {}
