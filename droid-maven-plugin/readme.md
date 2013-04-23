This is a Maven plugin to automate some parts of the android:maven build process.
This does not replace the http://code.google.com/p/maven-android-plugin/ existing plugin.

It supports these mojos;
 * `assetize` It copies certain files (.mp3|.ogg) to the android assets folder (typically target/assets) from both the target and the dependecies
 * `cull` It removes assets that are now duplicates from the final apk

TODO
----
 * 1.0.0.1 - cull will fail in projects with no files to repack
