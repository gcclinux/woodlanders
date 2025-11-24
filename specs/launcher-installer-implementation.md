# Woodlanders Launcher Installer Implementation

## Overview
This document summarizes the implementation of a Windows installer system for the Woodlanders Launcher, including build separation, localization, and automated installer creation.

## Completed Features

### 1. Gradle Build Separation
**Problem:** Single `gradle run` command launched both the game and launcher together.

**Solution:** Created separate Gradle tasks:
- `gradle runApp` - Launches the main Woodlanders game
- `gradle runLauncher` - Launches the launcher UI
- `gradle build` - Builds all three JARs (client, server, launcher)

**Files Modified:**
- `build.gradle` - Added `runApp` and `runLauncher` tasks, `copyLauncherJar` task
- `launcher/build.gradle` - Removed version suffix from JAR output

**Output:**
- `build/libs/woodlanders-client.jar` (26 MB)
- `build/libs/woodlanders-server.jar` (2.7 MB)
- `build/libs/woodlanders-launcher.jar` (11.8 MB)

### 2. Launcher Localization
**Problem:** Launcher had no multi-language support like the game.

**Solution:** Implemented complete localization system:
- Created `LocalizationService` class
- Auto-detects system locale
- Supports 5 languages: English, Polish, Portuguese, Dutch, German
- Created `Main.java` wrapper to fix JavaFX module issues

**Files Created:**
- `launcher/src/main/java/wagemaker/uk/launcher/services/LocalizationService.java`
- `launcher/src/main/java/wagemaker/uk/launcher/Main.java`
- `launcher/src/main/resources/localization/en.json`
- `launcher/src/main/resources/localization/pl.json`
- `launcher/src/main/resources/localization/pt.json`
- `launcher/src/main/resources/localization/nl.json`
- `launcher/src/main/resources/localization/de.json`
- `launcher/LOCALIZATION.md`

**Files Modified:**
- `launcher/src/main/java/wagemaker/uk/launcher/ui/LauncherApplication.java`
- `launcher/build.gradle` - Changed Main-Class to use wrapper

**Key Fix:** JavaFX fat JAR issue resolved by creating `Main.java` wrapper that calls `LauncherApplication.launch()` instead of extending Application directly.

### 3. Windows Installer System
**Problem:** Users needed to manually install Java and JavaFX to run the launcher.

**Solution:** Created automated installer that:
- Checks for Java 21+
- Downloads and installs Liberica JDK Full (with JavaFX) if needed
- Installs launcher to `%LOCALAPPDATA%\Woodlanders\Launcher`
- Creates desktop and Start Menu shortcuts with icon
- Includes launcher icon from `assets/icon/icon.png`

**Files Created:**
- `build-launcher-installer-local.ps1` - Local build script for testing
- `.github/workflows/build-launcher-installer.yml` - GitHub Actions workflow

**Output Files:**
- `build/distributions/woodlanders-setup-launcher.exe` (33 KB)
- `build/distributions/woodlanders-launcher-installer.zip` (10.3 MB)

**ZIP Contents:**
- `woodlanders-setup-launcher.exe` - Installer executable
- `woodlanders-launcher.jar` - Launcher application
- `launcher.ico` - Icon for shortcuts
- `install.ps1` - PowerShell script alternative
- `README.txt` - Installation instructions

### 4. GitHub Workflow Integration
**Files Modified:**
- `.github/workflows/release.yml` - Updated to build all JARs and include launcher
- `.github/workflows/build-launcher-installer.yml` - New workflow for installer

**Release Assets:**
The release workflow now includes:
- Windows portable executable
- Client JAR
- Server JAR
- **Launcher JAR** (new)
- **Launcher installer EXE** (new)
- **Launcher installer ZIP** (new)

## Technical Challenges & Solutions

### Challenge 1: JavaFX Module System
**Issue:** JavaFX 11+ uses Java module system, can't run from fat JAR when main class extends Application.

**Solution:** Created `Main.java` wrapper class that calls `LauncherApplication.launch()` instead of extending Application.

### Challenge 2: ps2exe Script Directory
**Issue:** When PowerShell script is compiled to EXE with ps2exe, `$PSScriptRoot` and `$MyInvocation.MyCommand.Path` become null.

**Solution:** Added fallback to `Get-Location` for compiled EXE:
```powershell
$scriptDir = if ($PSScriptRoot) { $PSScriptRoot } 
             elseif ($MyInvocation.MyCommand.Path) { Split-Path -Parent $MyInvocation.MyCommand.Path } 
             else { Get-Location | Select-Object -ExpandProperty Path }
```

### Challenge 3: Icon Format
**Issue:** ps2exe requires ICO format, but we have PNG.

**Solution:** 
- Renamed PNG to `.ico` extension (Windows shortcuts accept PNG with .ico extension)
- Removed icon parameter from ps2exe (not needed for installer EXE)
- Icon is used for shortcuts after installation

### Challenge 4: EXE File Dependencies
**Issue:** ps2exe doesn't embed large files like JAR into the EXE.

**Solution:** 
- EXE and JAR must be distributed together
- Created ZIP package containing all required files
- Updated README with clear instructions

## Installation Flow

1. User downloads `woodlanders-launcher-installer.zip`
2. Extracts ZIP to a folder
3. Runs `woodlanders-setup-launcher.exe` from extracted folder
4. Installer checks for Java 21+
5. If not found, downloads Liberica JDK Full (includes JavaFX)
6. Copies launcher JAR and icon to `%LOCALAPPDATA%\Woodlanders\Launcher`
7. Creates `launcher.bat` script
8. Creates desktop shortcut with icon
9. Creates Start Menu entry with icon
10. Optionally launches the launcher

## Configuration Files Updated

### .gitignore
Added exclusions for:
- `*.class` files
- `launcher/build/`
- `launcher/.gradle/`
- `launcher/*.jar`
- `launcher/*.log`

### build.gradle (root)
- Added `runApp` task
- Added `runLauncher` task
- Added `copyLauncherJar` task
- Updated `build` task dependencies

### launcher/build.gradle
- Changed `archiveVersion` to empty string
- Updated `Main-Class` to `wagemaker.uk.launcher.Main`
- Excluded `**/module-info.class`

## Testing

### Local Testing
Run the build script:
```powershell
.\build-launcher-installer-local.ps1
```

This will:
1. Build launcher JAR
2. Create installer script
3. Convert to EXE
4. Create ZIP package
5. Optionally test the installer

### Manual Testing
```powershell
cd build\distributions
.\woodlanders-setup-launcher.exe
```

### Verification
Check installation:
- JAR: `%LOCALAPPDATA%\Woodlanders\Launcher\woodlanders-launcher.jar`
- Desktop shortcut: `%USERPROFILE%\Desktop\Woodlanders Launcher.lnk`
- Start Menu: `%APPDATA%\Microsoft\Windows\Start Menu\Programs\Woodlanders\`

## Future Enhancements

### Potential Improvements
1. Convert PNG to proper ICO format for installer EXE icon
2. Add uninstaller script
3. Add version checking to skip JDK download if already installed
4. Create MSI installer using WiX Toolset
5. Add digital signature to EXE
6. Add auto-update capability to installer itself

### Known Limitations
1. Installer EXE doesn't have custom icon (ps2exe limitation with PNG)
2. Files must be distributed together (not a single-file installer)
3. Windows-only (no Linux/Mac installer yet)
4. Requires PowerShell 5.1+ and ps2exe module

## Documentation

### User Documentation
- `launcher/LOCALIZATION.md` - Localization guide
- `build/distributions/README.txt` - Installation instructions (generated)

### Developer Documentation
- This file - Implementation summary
- `build-launcher-installer-local.ps1` - Commented build script

## Summary

Successfully implemented a complete launcher system with:
- ✅ Separate build commands for game and launcher
- ✅ Multi-language support (5 languages)
- ✅ Automated Windows installer
- ✅ Java/JavaFX auto-installation
- ✅ Desktop and Start Menu integration
- ✅ Icon support
- ✅ GitHub Actions automation
- ✅ Local testing capability

The launcher is now production-ready and will be included in future releases.
