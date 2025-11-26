# Woodlanders Launcher - Uninstaller
$ErrorActionPreference = "Continue"

Write-Host ""
Write-Host "Woodlanders Launcher Uninstaller" -ForegroundColor Cyan
Write-Host ""

$confirm = Read-Host "This will remove all Woodlanders Launcher files and shortcuts. Continue? (Y/n)"
if ($confirm -eq 'n' -or $confirm -eq 'N') {
    Write-Host "Uninstall cancelled" -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "Removing Woodlanders files..." -ForegroundColor Yellow

# Remove main installation directory
if (Test-Path "$env:LOCALAPPDATA\Woodlanders") {
    Remove-Item "$env:LOCALAPPDATA\Woodlanders" -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "✓ Installation directory removed" -ForegroundColor Green
}

# Remove desktop shortcut
if (Test-Path "$env:USERPROFILE\Desktop\Woodlanders Launcher.lnk") {
    Remove-Item "$env:USERPROFILE\Desktop\Woodlanders Launcher.lnk" -Force -ErrorAction SilentlyContinue
    Write-Host "✓ Desktop shortcut removed" -ForegroundColor Green
}

# Remove Start Menu folder
if (Test-Path "$env:APPDATA\Microsoft\Windows\Start Menu\Programs\Woodlanders") {
    Remove-Item "$env:APPDATA\Microsoft\Windows\Start Menu\Programs\Woodlanders" -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "✓ Start Menu entry removed" -ForegroundColor Green
}

# Remove JavaFX cache
if (Test-Path "$env:USERPROFILE\.cache\woodlanders-javafx") {
    Remove-Item "$env:USERPROFILE\.cache\woodlanders-javafx" -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "✓ JavaFX cache removed" -ForegroundColor Green
}

Write-Host ""
Write-Host "Uninstall complete!" -ForegroundColor Green
Write-Host ""
Read-Host "Press Enter to exit"
