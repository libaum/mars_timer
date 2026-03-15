# fastlane documentation
fastlane ist hier für die Automatisierung von Android-Deployments konfiguriert.
## Installation
```bash
cd android
bundle install
```
## Verfügbare Lanes
### build
Baut nur die Flutter Release AAB ohne Upload.
```bash
bundle exec fastlane build
```
### alpha
Baut eine neue Version und lädt sie in den Alpha-Track (Closed Testing) hoch.
```bash
bundle exec fastlane alpha
```
### release
Promotet die getestete Alpha-Version direkt in den Store (Production).
```bash
bundle exec fastlane release
```
### metadata
Aktualisiert NUR die Store-Einträge (Titel, Beschreibung, Bilder).
```bash
bundle exec fastlane metadata
```
## Voraussetzungen
1. **Google Play Console API-Key**: Die Datei `pc-api.json` muss im `android/`-Verzeichnis liegen.
2. **Keystore**: Die Datei `upload-keystore.jks` muss in `android/app/` liegen.
3. **Key Properties**: Die Datei `key.properties` muss im `android/`-Verzeichnis liegen mit:
   ```
   storePassword=...
   keyPassword=...
   keyAlias=upload
   storeFile=upload-keystore.jks
   ```
## Store Metadata
Die Store-Texte und Screenshots befinden sich in:
```
android/fastlane/metadata/android/
├── de-DE/
│   ├── title.txt
│   ├── short_description.txt
│   ├── full_description.txt
│   └── video.txt
└── en-US/
    ├── title.txt
    ├── short_description.txt
    ├── full_description.txt
    ├── video.txt
    └── images/
        └── phoneScreenshots/
```
