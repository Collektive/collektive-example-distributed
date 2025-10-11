# Collektive Distributed Example

[![CI/CD](https://github.com/Collektive/collektive-example-distributed/actions/workflows/dispatcher.yml/badge.svg)](https://github.com/Collektive/collektive-example-distributed/actions/workflows/dispatcher.yml)

A distributed example implementation of [Collektive](https://github.com/Collektive/collektive), demonstrating aggregate programming in a network of distributed devices using MQTT for communication.

## Overview

This project showcases how to build distributed aggregate computing applications using the Collektive framework. It implements a simple neighboring network where devices communicate via MQTT to exchange information about their neighbors, demonstrating the fundamental concepts of aggregate programming in a distributed environment.

## Features

- **Multiplatform Support**: Runs on both JVM and JavaScript (Node.js) platforms using Kotlin Multiplatform
- **MQTT Communication**: Devices communicate using the MQTT protocol via a public broker
- **Aggregate Programming**: Implements the `neighboring` operation to collect information from connected devices
- **Configurable Network**: Customize the number of devices, round time, and execution duration

## Requirements

- **JDK**: Version 8 or higher
- **Gradle**: Wrapper included (no installation required)
- **Node.js**: Version 22.19 (for JavaScript target)

## Installation

Clone the repository:

```bash
git clone https://github.com/Collektive/collektive-example-distributed.git
cd collektive-example-distributed
```

## Usage

### Running on JVM

Run the distributed example on the JVM platform:

```bash
./gradlew :distributed:jvmRun
```

This will start 3 devices (IDs 0-2) that communicate via MQTT, each running aggregate cycles every second for 60 seconds.

### Running on JavaScript (Node.js)

Run the distributed example on Node.js:

```bash
./gradlew :distributed:jsNodeDevelopmentRun
```

This will start 2 devices (IDs 2-3) using the JavaScript implementation.

### Customizing Parameters

The main entrypoint accepts several parameters:

- `deviceCount`: Number of devices in the network (default: 50)
- `startDeviceId`: Starting device ID (default: 0)
- `roundTime`: Time between aggregate cycles (default: 1 second)
- `executeFor`: Total execution duration (default: 60 seconds)
- `asyncNetwork`: Whether to use asynchronous network mode (default: false)

Modify the parameters in `distributed/src/jvmMain/kotlin/it/unibo/collektive/Main.kt` or `distributed/src/jsMain/kotlin/it/unibo/collektive/Main.kt` to customize behavior.

### Building

Build the entire project:

```bash
./gradlew build
```

This will compile both JVM and JavaScript targets and run code quality checks (ktlint, detekt).

### Running Tests

Execute all tests:

```bash
./gradlew test
```

### Code Quality

Run linting and static analysis:

```bash
./gradlew ktlintCheck detektAll
```

Auto-fix linting issues:

```bash
./gradlew ktlintFormat
```

## Project Structure

```
collektive-example-distributed/
├── distributed/                    # Main module
│   └── src/
│       ├── commonMain/            # Shared Kotlin code
│       │   └── kotlin/
│       │       └── it/unibo/collektive/
│       │           ├── CommonEntrypoint.kt      # Main aggregate program logic
│       │           └── network/                 # MQTT mailbox implementation
│       ├── jvmMain/               # JVM-specific code
│       │   └── kotlin/
│       │       └── it/unibo/collektive/
│       │           └── Main.kt                  # JVM entry point
│       └── jsMain/                # JavaScript-specific code
│           └── kotlin/
│               └── it/unibo/collektive/
│                   └── Main.kt                  # JavaScript entry point
├── buildSrc/                      # Build configuration
├── gradle/                        # Gradle wrapper and version catalog
├── release.config.js             # Semantic release configuration
└── README.md                     # This file
```

## Technologies

- **[Collektive](https://github.com/Collektive/collektive)**: Aggregate programming DSL (v26.1.2)
- **Kotlin**: Multiplatform programming language (v2.2.20)
- **MQTT**: Message protocol for distributed communication
  - **[MKTT](https://github.com/nfacha/MKTT)**: Kotlin Multiplatform MQTT client
  - **[HiveMQ](https://www.hivemq.com/)**: MQTT client library (JVM)
- **Kotlinx Serialization**: Data serialization (JSON and Protobuf)
- **Kotlinx Coroutines**: Asynchronous programming
- **Kotlin Logging**: Structured logging

## How It Works

1. **Device Initialization**: Each device creates an MQTT mailbox connected to a public broker (`test.mosquitto.org`)
2. **Aggregate Program**: Devices run an aggregate program that collects neighbor information using the `neighboring` operation
3. **Communication**: Devices exchange messages via MQTT, broadcasting their state and receiving updates from neighbors
4. **Cycles**: Each device periodically executes aggregate cycles, processing received messages and updating its local state
5. **Output**: Devices log their current neighbor list after each cycle

## Development

### Git Hooks

The project uses pre-commit hooks to ensure code quality:

- **Commit message validation**: Enforces conventional commits
- **Code checks**: Runs `detektAll` and `ktlintCheck` before commits

Hooks are automatically installed when building the project.

### Continuous Integration

The project uses GitHub Actions for CI/CD:

- Builds and tests on multiple platforms (Ubuntu, macOS, Windows)
- Runs code quality checks
- Automated dependency updates via Renovate
- Automated releases using semantic-release

## Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes using conventional commits
4. Ensure all tests pass and code quality checks succeed
5. Push to the branch (`git push origin feature/amazing-feature`)
6. Open a Pull Request

## License

This project is part of the [Collektive](https://github.com/Collektive) ecosystem. Please refer to the main Collektive repository for license information.

## Related Projects

- [Collektive](https://github.com/Collektive/collektive): The main Collektive DSL and runtime
- [Collektive Stdlib](https://github.com/Collektive/collektive): Standard library for aggregate programming

## Support

For questions, issues, or contributions, please visit:
- [Issue Tracker](https://github.com/Collektive/collektive-example-distributed/issues)
- [Discussions](https://github.com/Collektive/collektive-example-distributed/discussions)
