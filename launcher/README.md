# Woodlanders Launcher

JavaFX-based launcher application for Woodlanders game.

## Features

- Automatic update checking from GitHub releases
- Smart caching of game client
- Version detection with SHA-256 verification
- One-click game launch
- Offline mode support

## Building

```bash
# Build launcher JAR
gradle :launcher:jar

# Run launcher
gradle :launcher:run
```

## Distribution

The launcher JAR includes all dependencies and can be distributed standalone.

## Requirements

- Java 21+
- JavaFX 21.0.4 (bundled in JAR)
- Internet connection for updates

## Configuration

Game files are cached in:
- **Windows**: `%USERPROFILE%\.config\woodlanders\`
- **Linux/macOS**: `~/.config/woodlanders/`
