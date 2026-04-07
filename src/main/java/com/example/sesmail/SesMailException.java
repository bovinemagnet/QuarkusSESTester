package com.example.sesmail;

/**
 * Typed exception for SES send failures, carrying an {@link ExitCode}.
 */
public class SesMailException extends Exception {

    private final ExitCode exitCode;

    public SesMailException(String message, Throwable cause, ExitCode exitCode) {
        super(message, cause);
        this.exitCode = exitCode;
    }

    public SesMailException(String message, ExitCode exitCode) {
        super(message);
        this.exitCode = exitCode;
    }

    public ExitCode exitCode() {
        return exitCode;
    }
}
