# Copilot Instructions for Collektive Example Distributed

## Project Overview

This repository contains a distributed example implementation of Collektive, a Kotlin Multiplatform library for aggregate programming. The project demonstrates distributed device coordination using MQTT for communication.

## Technology Stack

- **Language**: Kotlin (Multiplatform)
- **Build Tool**: Gradle 9.0.0
- **Platforms**: JVM and JavaScript (Node.js)
- **Communication**: MQTT (via HiveMQ client)
- **CI/CD**: GitHub Actions with semantic-release
- **Code Quality**: Detekt, ktlint

## Project Structure

- `distributed/` - Main Kotlin Multiplatform module
  - `src/commonMain/` - Shared Kotlin code
  - `src/jvmMain/` - JVM-specific implementation
  - `src/jsMain/` - JavaScript/Node.js-specific implementation
- `build.gradle.kts` - Root build configuration
- `distributed/build.gradle.kts` - Module-specific build configuration
- `.github/workflows/` - CI/CD workflow definitions

## Development Guidelines

### Building the Project

```bash
# Build the entire project
./gradlew build

# Run on JVM
./gradlew :distributed:jvmRun

# Build for JavaScript
./gradlew :distributed:jsBrowserProductionWebpack
```

### Code Quality

The project enforces strict code quality standards:

- **All warnings as errors**: The project is configured with `allWarningsAsErrors = true`
- **Linting**: Both Detekt and ktlint are enforced
- **Pre-commit hooks**: Automatically run `detektAll` and `ktlintCheck` before commits

Run quality checks:
```bash
# Run all linters
./gradlew detektAll ktlintCheck

# Auto-fix ktlint issues
./gradlew ktlintFormat
```

### Code Style

- Follow Kotlin coding conventions
- Use meaningful variable names
- Add KDoc comments for public APIs
- Use functional programming patterns where appropriate

### Multiplatform Considerations

- Common code goes in `commonMain`
- Platform-specific implementations in respective source sets
- Use `expect`/`actual` for platform-specific APIs
- The project uses `-Xexpect-actual-classes` compiler flag

### Dependencies

- Collektive core and standard library
- Kotlin coroutines for async operations
- HiveMQ MQTT client (JVM only)
- Kotlinx serialization for data serialization
- Kotlin logging for structured logging

### Testing

- Tests should be added in appropriate test source sets
- Run tests: `./gradlew test`
- Ensure platform-specific behavior is tested on relevant platforms

### Commit Messages

The project uses Conventional Commits:
- `feat:` for new features
- `fix:` for bug fixes
- `docs:` for documentation changes
- `chore:` for maintenance tasks
- `ci:` for CI/CD changes
- `refactor:` for code refactoring

### Release Process

- Automated via semantic-release

### Common Tasks

- **Update a version or add a dependency**: Modify version catalogs in `gradle/libs.versions.toml`, then if needed update `distributed/build.gradle.kts` in the appropriate source set
- **Run a device**: Use `./gradlew :distributed:jvmRun` with appropriate main class configuration

## Important Notes

- The MQTT broker used is `test.mosquitto.org` (public test broker)
- Default configuration: 50 devices, 1-second round time, 60-second execution
- Devices communicate via MQTT topics for neighbor discovery and message exchange
- The project uses Gradle develocity for build scans

## Debugging

- Enable verbose logging by configuring log4j2 (JVM) or console logging (JS)
- Use the `KotlinLogging` logger throughout the codebase
- Check MQTT connectivity if devices are not discovering neighbors

## Known Issues/TODOs

- Some documentation is marked with "TODO add documentation"
- Consider reviewing and completing missing KDoc comments
