# Build validation

The repository does not currently include a Gradle wrapper JAR, so CI uses the Gradle setup action rather than committing a fabricated wrapper.

The Android build runs on pushes to develop and pull requests targeting develop or main.

## Local Pixel test
1. Open the repository in Android Studio.
2. Let Gradle sync complete.
3. Connect the Pixel with USB debugging enabled.
4. Select the Pixel as the run target and run the app.
5. In the demo workflow, scan, start guided diagnosis, then use TAKE PHOTO WITH PIXEL or RECORD VIDEO WITH PIXEL.
6. Confirm captured evidence appears in the inspection evidence card.

Do not use real customer information in the public repository or screenshots committed to source control.
