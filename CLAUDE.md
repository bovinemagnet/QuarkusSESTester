# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A Quarkus CLI utility (`ses-mail-cli`) that constructs raw MIME emails using Simple Java Mail and submits them to Amazon SES via HTTPS API v2. Supports dry-run mode, `.env` configuration, and realistic sender-domain testing under SES/SPF/DMARC constraints.

## Build Commands

- **Build:** `gradle21w build`
- **Run tests:** `gradle21w test`
- **Run a single test class:** `gradle21w test --tests 'com.paulsnow.sesmail.MailValidatorTest'`
- **Run a single test method:** `gradle21w test --tests 'com.paulsnow.sesmail.MailValidatorTest.shouldRejectInvalidFromAddress'`
- **Dev mode:** `gradle21w quarkusDev`
- **Build native executable:** `gradle21w build -Dquarkus.native.enabled=true`

Use `gradle21w` (on PATH) instead of `./gradlew`.

## Architecture

**Control flow:** `CliCommand` → `DotenvLoader` → `AppConfig` → `MailValidator` → `MimeMessageFactory` → dry-run preview or `SesMailSender` → `OutputRenderer`

**Package:** `com.paulsnow.sesmail` — all classes in a single package under `src/main/java`.

**Key classes:**
- `CliCommand` — PicoCLI entry point, orchestrates the full send/dry-run pipeline
- `AppConfig` — Builder-pattern config with precedence: CLI args > env vars > .env file > defaults
- `SesMailSender` — AWS SES v2 raw email sender with endpoint override support (for LocalStack)
- `MimeMessageFactory` — Pure MIME construction via Simple Java Mail (no SMTP)
- `MailValidator` — RFC 5322-inspired validation with domain warnings (Gmail, cross-domain)
- `OutputRenderer` — Text/JSON output with credential masking
- `ExitCode` — Enum defining 7 exit codes (0–6) for distinct failure modes

## Configuration

Config is loaded with this precedence: CLI arguments > environment variables > `.env` file > defaults.

See `.env.example` for available configuration keys. Never commit `.env` files.

## Testing

- **Framework:** JUnit 5 + AssertJ
- **Test classes:** `MailValidatorTest`, `MimeMessageFactoryTest`, `AppConfigTest`, `DotenvLoaderTest`, `OutputRendererTest`
- Tests are unit-level; no integration tests requiring AWS credentials

## Tech Stack

- Java 25, Quarkus 3.34.3, Gradle
- `quarkus-picocli` for CLI
- `software.amazon.awssdk:sesv2` (v2.31.16) for SES
- `org.simplejavamail:simple-java-mail` (v8.12.6) for MIME construction
- `io.github.cdimascio:dotenv-java` for .env support
