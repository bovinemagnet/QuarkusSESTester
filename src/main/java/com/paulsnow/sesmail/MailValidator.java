package com.paulsnow.sesmail;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validates a {@link MailRequest} before sending or dry-running.
 */
public class MailValidator {

    // RFC 5322-inspired simple email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$"
    );

    /**
     * Validates the request and returns a list of error messages.
     * An empty list means the request is valid.
     */
    public List<String> validate(MailRequest request) {
        List<String> errors = new ArrayList<>();

        if (isBlank(request.from())) {
            errors.add("--send-from is required");
        } else if (!isValidEmail(request.from())) {
            errors.add(
                "--send-from is not a valid email address: " + request.from()
            );
        }

        if (isBlank(request.to())) {
            errors.add("--send-to is required");
        } else if (!isValidEmail(request.to())) {
            errors.add(
                "--send-to is not a valid email address: " + request.to()
            );
        }

        if (isBlank(request.subject())) {
            errors.add("--send-subject is required and must not be blank");
        }

        if (isBlank(request.body())) {
            errors.add("--send-body is required and must not be blank");
        }

        return errors;
    }

    /**
     * Validates the AWS region.
     */
    public List<String> validateRegion(String region) {
        List<String> errors = new ArrayList<>();
        if (isBlank(region)) {
            errors.add(
                "AWS region is required (use --aws-region or set AWS_REGION)"
            );
        }
        return errors;
    }

    /**
     * Returns warnings about the request (non-fatal).
     */
    public List<String> warnings(MailRequest request) {
        List<String> warnings = new ArrayList<>();

        if (!isBlank(request.from()) && isSuspiciousDomain(request.from())) {
            warnings.add(
                "WARNING: sender domain appears to be a well-known public provider. " +
                    "Ensure this address is verified in your SES account."
            );
        }

        if (!isBlank(request.from()) && !isBlank(request.to())) {
            String fromDomain = domainOf(request.from());
            String toDomain = domainOf(request.to());
            if (!fromDomain.equalsIgnoreCase(toDomain)) {
                warnings.add(
                    "WARNING: sender and recipient are on different domains (" +
                        fromDomain +
                        " vs " +
                        toDomain +
                        "). Ensure the SES sandbox allows this recipient."
                );
            }
        }

        return warnings;
    }

    public boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static final Set<String> PUBLIC_EMAIL_DOMAINS = Set.of(
        "gmail.com",
        "yahoo.com",
        "hotmail.com",
        "outlook.com",
        "live.com"
    );

    private boolean isSuspiciousDomain(String email) {
        return PUBLIC_EMAIL_DOMAINS.contains(domainOf(email).toLowerCase());
    }

    private String domainOf(String email) {
        int at = email.indexOf('@');
        return at >= 0 ? email.substring(at + 1) : email;
    }
}
