# Phone Installment Android App (builds on GitHub)

1. Edit `app/src/main/java/com/shop/installment/Cfg.java` -> set `API` to your HTTPS api.php URL.
2. Create a signing key ONCE (keep the file safe — losing it means you can't update phones):
   `keytool -genkeypair -v -keystore release.jks -alias shop -keyalg RSA -keysize 2048 -validity 10000`
   Then base64 it: Linux/Mac `base64 -w0 release.jks`; Windows PowerShell `[Convert]::ToBase64String([IO.File]::ReadAllBytes("release.jks"))`
3. GitHub repo -> Settings -> Secrets and variables -> Actions -> add: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS` (shop), `KEY_PASSWORD`.
4. Build: Actions tab -> "Build APK" -> Run workflow (APK appears as an artifact), or push a tag `v1.0` (APK published on Releases as `installment.apk`).
5. Open the finished run's Summary: copy the `APK_CERT_CHECKSUM` into the server's config.php. Set `APK_URL` to
   `https://github.com/USER/REPO/releases/latest/download/installment.apk`.
Releasing an update: raise `versionCode`/`versionName` in app/build.gradle, set the same `LATEST_VERSION_CODE` in config.php, push a new tag.
The repo must be PUBLIC for the phone to download the APK during QR setup. Never commit your keystore or server config.
