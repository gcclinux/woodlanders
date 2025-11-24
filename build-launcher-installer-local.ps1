# Local Launcher Installer Builder
# Run this script to test the installer creation locally

$ErrorActionPreference = "Stop"

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "  Woodlanders Launcher Installer Builder" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Build the launcher JAR
Write-Host "[1/7] Building launcher JAR..." -ForegroundColor Yellow
gradle :launcher:jar -x test --no-daemon
if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to build launcher JAR" -ForegroundColor Red
    exit 1
}
Write-Host "Launcher JAR built successfully" -ForegroundColor Green
Write-Host ""

# Step 2: Create directory structure
Write-Host "[2/7] Creating directory structure..." -ForegroundColor Yellow
New-Item -ItemType Directory -Force -Path installer-staging | Out-Null
New-Item -ItemType Directory -Force -Path build\distributions | Out-Null
Write-Host "Directories created" -ForegroundColor Green
Write-Host ""

# Step 3: Copy launcher JAR and icon
Write-Host "[3/7] Copying launcher JAR and icon to staging..." -ForegroundColor Yellow
if (Test-Path launcher\build\libs\woodlanders-launcher.jar) {
    Copy-Item launcher\build\libs\woodlanders-launcher.jar -Destination installer-staging\
    Write-Host "Launcher JAR copied" -ForegroundColor Green
} else {
    Write-Host "Launcher JAR not found!" -ForegroundColor Red
    exit 1
}

# Copy icon if it exists (rename to match template expectations)
if (Test-Path assets\icon\icon.png) {
    Copy-Item assets\icon\icon.png -Destination installer-staging\launcher.ico
    Write-Host "Icon copied" -ForegroundColor Green
} else {
    Write-Host "Icon not found (optional)" -ForegroundColor Yellow
}
Write-Host ""

# Step 4: Create installer script
Write-Host "[4/7] Creating installer script..." -ForegroundColor Yellow

# Read the template from the original location or create it
$installerScriptPath = "installer-staging\install.ps1"
$installerContent = Get-Content -Path "C:\Users\ricardo\Programming\woodlanders-launcher\app\src\main\scripts\windows-installer.ps1" -Raw -ErrorAction SilentlyContinue

if ($installerContent) {
    # Use the existing script as template and patch it for ps2exe compatibility
    $installerContent = $installerContent -replace '\$\{VERSION\}', 'LOCAL-BUILD'
    
    # Fix $scriptDir to work with ps2exe compiled EXE
    # Replace the line that sets $scriptDir with a version that works in compiled EXE
    # When compiled with ps2exe, use the current directory where the EXE is located
    $installerContent = $installerContent -replace '\$scriptDir = if \(\$PSScriptRoot\) \{ \$PSScriptRoot \} else \{ Split-Path -Parent \$MyInvocation\.MyCommand\.Path \}', '$scriptDir = if ($PSScriptRoot) { $PSScriptRoot } elseif ($MyInvocation.MyCommand.Path) { Split-Path -Parent $MyInvocation.MyCommand.Path } else { Get-Location | Select-Object -ExpandProperty Path }'
    
    $installerContent | Out-File -FilePath $installerScriptPath -Encoding UTF8
    Write-Host "Installer script created from template (patched for EXE)" -ForegroundColor Green
} else {
    # Create a new installer script
    Write-Host "Creating new installer script..." -ForegroundColor Gray
    
    # Create the script in parts to avoid nested here-string issues
    $part1 = @'
# Woodlanders Launcher - Windows Installer
param([switch]$Silent = $false)
$ErrorActionPreference = "Stop"

$APP_NAME = "Woodlanders Launcher"
$APP_VERSION = "LOCAL-BUILD"
$INSTALL_DIR = "$env:LOCALAPPDATA\Woodlanders\Launcher"
$JRE_DIR = "$INSTALL_DIR\jre"
$DESKTOP_SHORTCUT = "$env:USERPROFILE\Desktop\Woodlanders Launcher.lnk"
$START_MENU_DIR = "$env:APPDATA\Microsoft\Windows\Start Menu\Programs\Woodlanders"
$JRE_DOWNLOAD_URL = "https://download.bell-sw.com/java/21.0.5+11/bellsoft-jdk21.0.5+11-windows-amd64-full.zip"

function Write-Status { param([string]$Message); if (-not $Silent) { Write-Host $Message -ForegroundColor Cyan } }
function Write-Success { param([string]$Message); if (-not $Silent) { Write-Host "OK $Message" -ForegroundColor Green } }
function Write-ErrorMsg { param([string]$Message); Write-Host "ERROR $Message" -ForegroundColor Red }

function Test-JavaInstalled {
    try {
        $javaVersion = java -version 2>&1 | Select-String "version" | ForEach-Object { $_.ToString() }
        if ($javaVersion -match "(\d+)\.(\d+)") {
            $majorVersion = [int]$matches[1]
            if ($majorVersion -ge 17) { return $true }
        }
    } catch { return $false }
    return $false
}

function Download-JRE {
    Write-Status "Downloading JDK with JavaFX..."
    if (Test-Path "$JRE_DIR\bin\java.exe") { Write-Success "JDK already installed"; return $true }
    if (Test-Path $JRE_DIR) { Remove-Item $JRE_DIR -Recurse -Force -ErrorAction SilentlyContinue }
    New-Item -ItemType Directory -Force -Path $JRE_DIR | Out-Null
    $jreZip = "$env:TEMP\woodlanders-jre.zip"
    try {
        Write-Status "Downloading JDK (this may take a few minutes)..."
        $ProgressPreference = 'SilentlyContinue'
        Invoke-WebRequest -Uri $JRE_DOWNLOAD_URL -OutFile $jreZip -UseBasicParsing
        $ProgressPreference = 'Continue'
        Write-Status "Extracting JDK..."
        $tempExtract = "$env:TEMP\woodlanders-jre-extract"
        if (Test-Path $tempExtract) { Remove-Item $tempExtract -Recurse -Force }
        Expand-Archive -Path $jreZip -DestinationPath $tempExtract -Force
        $extractedDir = Get-ChildItem -Path $tempExtract -Directory | Select-Object -First 1
        Get-ChildItem -Path $extractedDir.FullName | Move-Item -Destination $JRE_DIR -Force
        Remove-Item $tempExtract -Recurse -Force
        Remove-Item $jreZip -Force
        Write-Success "JDK installed"
        return $true
    } catch {
        Write-ErrorMsg "Failed to download JDK: $_"
        return $false
    }
}

function Install-Application {
    Write-Status "Installing $APP_NAME..."
    New-Item -ItemType Directory -Force -Path $INSTALL_DIR | Out-Null
    $scriptDir = if ($PSScriptRoot) { $PSScriptRoot } else { Split-Path -Parent $MyInvocation.MyCommand.Path }
    if (Test-Path "$scriptDir\woodlanders-launcher.jar") {
        Copy-Item "$scriptDir\woodlanders-launcher.jar" -Destination $INSTALL_DIR -Force
        Write-Success "Application files copied"
    } else {
        Write-ErrorMsg "Application JAR not found"
        return $false
    }
    if (Test-Path "$scriptDir\launcher-icon.png") {
        Copy-Item "$scriptDir\launcher-icon.png" -Destination $INSTALL_DIR -Force
    }
    return $true
}

function New-LauncherScript {
    $batContent = "@echo off`r`nsetlocal`r`nset APP_DIR=%~dp0`r`nset JAVAFX_CACHE=%USERPROFILE%\.cache\woodlanders-javafx`r`nif exist %APP_DIR%jre\bin\java.exe (set JAVA_CMD=%APP_DIR%jre\bin\java.exe) else (set JAVA_CMD=java)`r`n%JAVA_CMD% -Djavafx.cachedir=%JAVAFX_CACHE% -jar %APP_DIR%woodlanders-launcher.jar`r`nif %ERRORLEVEL% neq 0 (echo. & echo Application failed to start. & pause)`r`nendlocal"
    $batContent | Out-File -FilePath "$INSTALL_DIR\launcher.bat" -Encoding ASCII -Force
    Write-Success "Launcher script created"
}

function New-Shortcuts {
    $WshShell = New-Object -ComObject WScript.Shell
    $Shortcut = $WshShell.CreateShortcut($DESKTOP_SHORTCUT)
    $Shortcut.TargetPath = "$INSTALL_DIR\launcher.bat"
    $Shortcut.WorkingDirectory = $INSTALL_DIR
    $Shortcut.Description = "Woodlanders Game Launcher"
    if (Test-Path "$INSTALL_DIR\launcher-icon.png") {
        $Shortcut.IconLocation = "$INSTALL_DIR\launcher-icon.png"
    }
    $Shortcut.Save()
    Write-Success "Desktop shortcut created"
    New-Item -ItemType Directory -Force -Path $START_MENU_DIR | Out-Null
    $StartMenuShortcut = $WshShell.CreateShortcut("$START_MENU_DIR\Woodlanders Launcher.lnk")
    $StartMenuShortcut.TargetPath = "$INSTALL_DIR\launcher.bat"
    $StartMenuShortcut.WorkingDirectory = $INSTALL_DIR
    $StartMenuShortcut.Description = "Woodlanders Game Launcher"
    if (Test-Path "$INSTALL_DIR\launcher-icon.png") {
        $StartMenuShortcut.IconLocation = "$INSTALL_DIR\launcher-icon.png"
    }
    $StartMenuShortcut.Save()
    Write-Success "Start Menu shortcut created"
}

try {
    Write-Host ""
    Write-Host "Woodlanders Launcher Installer" -ForegroundColor Cyan
    Write-Host "Version: $APP_VERSION" -ForegroundColor Cyan
    Write-Host ""
    if (Test-JavaInstalled) {
        Write-Success "Java 17+ found"
    } else {
        if (-not (Download-JRE)) { throw "Failed to install JRE" }
    }
    if (-not (Install-Application)) { throw "Failed to install application" }
    New-LauncherScript
    New-Shortcuts
    Write-Host ""
    Write-Host "Installation Complete!" -ForegroundColor Green
    Write-Host "Installed to: $INSTALL_DIR"
    Write-Host ""
    if (-not $Silent) {
        $launch = Read-Host "Launch now? (Y/n)"
        if ($launch -ne 'n' -and $launch -ne 'N') {
            Start-Process -FilePath "$INSTALL_DIR\launcher.bat"
        }
    }
} catch {
    Write-Host ""
    Write-ErrorMsg "Installation failed: $_"
    if (-not $Silent) { Read-Host "Press Enter to exit" }
    exit 1
}
'@
    
    $part1 | Out-File -FilePath $installerScriptPath -Encoding UTF8
    Write-Host "Installer script created" -ForegroundColor Green
}
Write-Host ""

# Step 5: Create README
Write-Host "[5/7] Creating README..." -ForegroundColor Yellow
$readmeLines = @(
    "# Woodlanders Launcher Installer",
    "",
    "## Installation Instructions",
    "",
    "### Option 1: Run the EXE (Recommended)",
    "Simply double-click woodlanders-setup-launcher.exe",
    "",
    "### Option 2: Run PowerShell Script",
    "1. Right-click install.ps1",
    "2. Select Run with PowerShell",
    "",
    "## What Gets Installed",
    "",
    "- Woodlanders Launcher application",
    "- Java 21 with JavaFX (if needed)",
    "- Desktop shortcut",
    "- Start Menu entry",
    "",
    "## Installation Location",
    "",
    "%LOCALAPPDATA%\Woodlanders\Launcher",
    "",
    "## Features",
    "",
    "- Auto-updates game from GitHub",
    "- Multi-language support",
    "- Offline mode",
    "",
    "For help: https://github.com/gcclinux/woodlanders"
)
$readmeLines -join "`r`n" | Out-File -FilePath installer-staging\README.txt -Encoding UTF8
Write-Host "README created" -ForegroundColor Green
Write-Host ""

# Step 6: Install ps2exe and convert to EXE
Write-Host "[6/7] Converting PowerShell script to EXE..." -ForegroundColor Yellow
Write-Host "  Checking for ps2exe module..." -ForegroundColor Gray

if (-not (Get-Module -ListAvailable -Name ps2exe)) {
    Write-Host "  Installing ps2exe module..." -ForegroundColor Gray
    Install-Module -Name ps2exe -Force -Scope CurrentUser -AllowClobber
    Write-Host "  ps2exe installed" -ForegroundColor Gray
}

Import-Module ps2exe

Write-Host "  Converting to EXE..." -ForegroundColor Gray
$ps2exeParams = @{
    inputFile = "installer-staging\install.ps1"
    outputFile = "build\distributions\woodlanders-setup-launcher.exe"
    title = "Woodlanders Launcher Installer"
    description = "Woodlanders Game Launcher Installer"
    company = "Wagemaker UK"
    version = "1.0.0.0"
    requireAdmin = $true
    noConsole = $false
}

# Note: ps2exe requires ICO format for icons, not PNG
# The icon will be used in shortcuts after installation

Invoke-ps2exe @ps2exeParams

if (Test-Path build\distributions\woodlanders-setup-launcher.exe) {
    Write-Host "EXE created successfully" -ForegroundColor Green
} else {
    Write-Host "Failed to create EXE" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Step 7: Create distribution package
Write-Host "[7/7] Creating distribution package..." -ForegroundColor Yellow

# Copy all necessary files to distributions folder
Copy-Item installer-staging\woodlanders-launcher.jar -Destination build\distributions\ -Force
Copy-Item installer-staging\launcher.ico -Destination build\distributions\ -Force -ErrorAction SilentlyContinue
Copy-Item installer-staging\install.ps1 -Destination build\distributions\ -Force
Copy-Item installer-staging\README.txt -Destination build\distributions\ -Force

# Create ZIP with all installer files
$zipFiles = @(
    "build\distributions\woodlanders-setup-launcher.exe",
    "build\distributions\woodlanders-launcher.jar",
    "build\distributions\launcher.ico",
    "build\distributions\install.ps1",
    "build\distributions\README.txt"
)
$zipFiles = $zipFiles | Where-Object { Test-Path $_ }

Compress-Archive -Path $zipFiles -DestinationPath build\distributions\woodlanders-launcher-installer.zip -Force
Write-Host "Distribution package created" -ForegroundColor Green
Write-Host ""

# Summary
Write-Host "=========================================" -ForegroundColor Green
Write-Host "  Build Complete!" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green
Write-Host ""

$zipFile = Get-Item build\distributions\woodlanders-launcher-installer.zip

Write-Host "Created installer package:" -ForegroundColor Cyan
Write-Host "  Path: $($zipFile.FullName)" -ForegroundColor Yellow
Write-Host "  Size: $([math]::Round($zipFile.Length / 1MB, 2)) MB" -ForegroundColor Gray
Write-Host ""

Write-Host "ZIP Contents:" -ForegroundColor Cyan
Write-Host "  - woodlanders-setup-launcher.exe (installer)" -ForegroundColor Gray
Write-Host "  - woodlanders-launcher.jar (application)" -ForegroundColor Gray
Write-Host "  - launcher.ico (icon)" -ForegroundColor Gray
Write-Host "  - install.ps1 (PowerShell alternative)" -ForegroundColor Gray
Write-Host "  - README.txt (instructions)" -ForegroundColor Gray
Write-Host ""

Write-Host "To test the installer:" -ForegroundColor Cyan
Write-Host "  1. Extract the ZIP file" -ForegroundColor White
Write-Host "  2. Run woodlanders-setup-launcher.exe from the extracted folder" -ForegroundColor White
Write-Host ""

$test = Read-Host "Would you like to extract and test the installer now? (Y/n)"
if ($test -ne 'n' -and $test -ne 'N') {
    Write-Host ""
    Write-Host "Extracting ZIP..." -ForegroundColor Cyan
    $testDir = "build\distributions\test-installer"
    if (Test-Path $testDir) { Remove-Item $testDir -Recurse -Force }
    Expand-Archive -Path build\distributions\woodlanders-launcher-installer.zip -DestinationPath $testDir
    Write-Host "Launching installer..." -ForegroundColor Cyan
    Start-Process -FilePath "$testDir\woodlanders-setup-launcher.exe" -WorkingDirectory $testDir -Wait
}
