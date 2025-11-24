# Launcher Localization

The Woodlanders launcher supports multiple languages based on your system locale.

## Supported Languages

- **English (en)** - Default
- **Polish (pl)** - Polski
- **Portuguese (pt)** - Português (Brazilian)
- **Dutch (nl)** - Nederlands
- **German (de)** - Deutsch

## How It Works

The launcher automatically detects your system language using `Locale.getDefault()` and loads the appropriate translation file. If your system language is not supported, it falls back to English.

## Translation Files

Translation files are located in `launcher/src/main/resources/localization/` as JSON files:

- `en.json` - English
- `pl.json` - Polish
- `pt.json` - Portuguese
- `nl.json` - Dutch
- `de.json` - German

## Adding a New Language

1. Create a new JSON file in `launcher/src/main/resources/localization/` (e.g., `fr.json` for French)
2. Copy the structure from `en.json` and translate all values
3. Update `LocalizationService.detectSystemLanguage()` to map the locale code to your new language file

## Translation Structure

```json
{
  "launcher": {
    "title": "Woodlanders Launcher",
    "project_site": "Project site: gcclinux.github.io/Woodlanders",
    "version_format": "Local: {0} | Remote: {1}",
    "button": {
      "launch": "Launch Woodlanders",
      ...
    },
    "status": {
      "checking": "Checking for updates…",
      ...
    }
  }
}
```

Keys use dot notation (e.g., `launcher.button.launch`) and support simple placeholder replacement with `{0}`, `{1}`, etc.
