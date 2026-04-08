package com.paulsnow.sesmail;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Loads configuration from a {@code .env} file.
 * <p>
 * If the file does not exist, the loader returns an empty environment rather than failing,
 * because the operator may rely entirely on real environment variables or CLI flags.
 */
public class DotenvLoader {

    private final Dotenv dotenv;

    /**
     * Loads the {@code .env} file from the current working directory.
     */
    public DotenvLoader() {
        this(".");
    }

    /**
     * Loads a {@code .env} file from the specified directory or file path.
     *
     * @param pathOrDir path to a {@code .env} file or the directory containing one
     */
    public DotenvLoader(String pathOrDir) {
        this.dotenv = buildDotenv(pathOrDir);
    }

    /**
     * Returns the value of a variable from the loaded env file, or empty if absent.
     */
    public Optional<String> get(String key) {
        try {
            String value = dotenv.get(key);
            return (value != null && !value.isBlank())
                ? Optional.of(value)
                : Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Returns the value, falling back to a system environment variable, then to the default.
     */
    public String getOrDefault(String key, String defaultValue) {
        return get(key).orElseGet(() -> {
            String envValue = System.getenv(key);
            return (envValue != null && !envValue.isBlank())
                ? envValue
                : defaultValue;
        });
    }

    private static Dotenv buildDotenv(String pathOrDir) {
        try {
            DotenvBuilder builder = Dotenv.configure().ignoreIfMissing();
            if (pathOrDir != null && !pathOrDir.equals(".")) {
                Path p = Path.of(pathOrDir);
                if (Files.isDirectory(p)) {
                    builder = builder.directory(pathOrDir);
                } else if (Files.isRegularFile(p)) {
                    // dotenv-java expects a directory; derive it from the file path
                    builder = builder
                        .directory(
                            p.getParent() != null
                                ? p.getParent().toString()
                                : "."
                        )
                        .filename(p.getFileName().toString());
                }
            }
            return builder.load();
        } catch (RuntimeException e) {
            // Gracefully degrade when .env is absent or unreadable
            return Dotenv.configure().ignoreIfMissing().load();
        }
    }
}
