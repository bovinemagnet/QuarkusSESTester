package com.example.sesmail;

/**
 * Process exit codes for the ses-mail-cli application.
 */
public enum ExitCode {

    SUCCESS(0),
    INVALID_USAGE(1),
    CONFIG_ERROR(2),
    VALIDATION_ERROR(3),
    AUTH_ERROR(4),
    SES_REJECTION(5),
    RUNTIME_ERROR(6);

    private final int code;

    ExitCode(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
