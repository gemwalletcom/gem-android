# Gem Wallet Android - Claude Code Reference

## Project Overview

Gem Wallet is an open-source, multi-chain cryptocurrency wallet for Android built with Kotlin/Compose. The app provides secure self-custody wallet functionality with support for multiple blockchains including Ethereum, Bitcoin, Solana, and many others.

**Key Technologies:**
- **Frontend**: Kotlin, Jetpack Compose, Android SDK
- **Backend**: Rust Core library for cryptographic operations
- **Build System**: Gradle with Kotlin DSL
- **Task Runner**: Just (justfile)
- **Docker**: Multi-stage builds for CI/CD

## Repository Structure

```
gem-android/
├── app/                     # Main Android application module
├── core/                    # Rust core library (submodule)
├── gemcore/                 # JNI bindings for Rust core
├── ui/                      # UI components and screens
├── data/                    # Data layer (repositories, services)
├── blockchain/              # Blockchain-specific implementations
├── features/                # Feature modules (NFT, recipient, etc.)
├── flavors/                 # Build flavors (Google, Samsung, etc.)
├── Dockerfile.base          # Base Docker image for Android development
├── Dockerfile.app           # App build Docker image
└── justfile                 # Task definitions
```

## Development Setup

### Prerequisites
1. **Android Studio** (latest version)
2. **JDK 17** (preferably via SDKMAN)
3. **Just** task runner (`brew install just`)
4. **GitHub Personal Access Token** with `read:packages` permission

### Initial Setup
```bash
# Install development dependencies
just bootstrap

# Generate GitHub token and add to local.properties:
echo "gpr.username=<your-github-username>" >> local.properties
echo "gpr.token=<your-github-personal-token>" >> local.properties

# Generate models (optional)
just generate
```

## Common Commands

### Development
```bash
# List all available commands
just list

# Bootstrap development environment
just bootstrap

# Generate TypeShare models from Rust
just generate

# Generate models only
just generate-models

# Update core submodule
just core-upgrade

# Localize strings
just localize
```

### Building
```bash
# Build debug APK
./gradlew assembleGoogleDebug

# Build release bundle (unsigned)
just unsigned-release

# Build all release variants
just release

# Extract universal APK from bundle
just extract-universal-apk
```

### Testing
```bash
# Build test APK
just build-test

# Run connected Android tests
just test

# Run unit tests
./gradlew test

# Run specific test suite
./gradlew :app:testGoogleDebugUnitTest
```

### Docker
```bash
# Build base Docker image
just build-base-image

# Build app Docker image
just build-app

# Build with specific tag
TAG=feature-branch just build-app
```

### Linting & Quality
```bash
# Run lint checks
./gradlew lint

# Run detekt static analysis
./gradlew detekt

# Format code
./gradlew ktlintFormat
```

## Project Architecture

### Multi-Module Structure
- **app/**: Main application module with Activities and Application class
- **ui/**: Shared UI components, themes, and Compose screens
- **data/**: Repository pattern implementation, database, network services
- **gemcore/**: JNI bindings to Rust core library
- **blockchain/**: Blockchain-specific implementations and models
- **features/**: Modular features (NFT management, recipient handling, etc.)

### Build Flavors
- **google**: Google Play Store version with Firebase/FCM
- **fdroid**: F-Droid version without proprietary dependencies  
- **samsung**: Samsung Galaxy Store version
- **solana**: Solana-focused variant
- **universal**: Universal build

### Key Dependencies
- **Jetpack Compose**: Modern UI toolkit
- **Hilt**: Dependency injection
- **Room**: Local database
- **Retrofit**: Network layer
- **WalletConnect**: dApp connectivity
- **Rust Core**: Cryptographic operations via JNI

## CI/CD

### GitHub Actions
- **ci.yml**: Main CI pipeline (tests, lint, build)
- **docker.yml**: Docker image builds for releases

### Docker Workflow
1. **Base Image**: Built from `Dockerfile.base` with Android SDK, NDK, JDK
2. **App Image**: Built from `Dockerfile.app` using base image
3. **Artifacts**: APK/AAB files uploaded to GitHub releases

## Development Guidelines

### Code Style
- Follow Kotlin coding conventions
- Use Jetpack Compose for UI development
- Implement repository pattern for data access
- Use Hilt for dependency injection
- Write unit tests for business logic

### Git Workflow
- **Main Branch**: `main` (stable releases)
- **Feature Branches**: `feature/description` or `fix/description`
- **Commit Messages**: Follow [Conventional Commits](https://www.conventionalcommits.org/)

### Security
- Never commit secrets or API keys
- Use `local.properties` for sensitive configuration
- Follow Android security best practices
- Implement proper key management

## Troubleshooting

### Common Issues

**Build Failures:**
```bash
# Clean and rebuild
./gradlew clean
just bootstrap
./gradlew build
```

**NDK Issues:**
```bash
# Reinstall NDK
just install-ndk
```

**Submodule Issues:**
```bash
# Update core submodule
git submodule update --init --recursive
just core-upgrade
```

**Docker Build Issues:**
- Ensure Docker has sufficient memory (8GB+ recommended)
- Check base image availability
- Verify GitHub token permissions

### Getting Help
- **Discord**: [Gem Wallet Discord](https://discord.gg/aWkq5sj7SY)
- **Telegram**: [Developer Chat](https://t.me/gemwallet_developers)
- **Issues**: [GitHub Issues](https://github.com/gemwalletcom/gem-android/issues)

## Useful File Locations

- **Main Activity**: `app/src/main/kotlin/com/gemwallet/android/MainActivity.kt`
- **Build Config**: `app/build.gradle.kts`
- **Core Dependencies**: `gradle/libs.versions.toml`
- **Localization**: `ui/src/main/res/values/`
- **Assets**: `app/src/main/assets/`

## Performance Notes

- **Rust Core**: Heavy cryptographic operations are offloaded to Rust
- **Memory**: Configure Android Studio with sufficient heap size
- **Build Cache**: Gradle build cache is enabled for faster builds
- **Docker**: Multi-stage builds optimize image size and build speed