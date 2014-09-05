Degenerated
===========

This is a growing pile of other people's work that I've repackaged into Maven.
My cool-ness here is that this project downloads the artifacts ... so anyone can (potentially) tweak parameters, hit build, and get their own distribution.
At the moment, it's all focussed around libGDX.

I have;

 * [libGDX](http://libgdx.badlogicgames.com/)
  * with some tweaks I have not pushed upstream
  * I'm aware that they've got a pom of their own - I don't think that it's in central; Aut inveniam viam aut faciam



Building
-------
When the project builds, it
 * scrapes for the distributions
  * it also "caches" the files in the root project's target folder
 * cracks that open for the .java, .class, .png, .fnt and whatever that it needs
 * packs them up (smartly) as if they were compiled by the project
  * there's some beard twirling where it tries to pack .java files, but otherwise packs .class files

Other Mojos
-----------
There are two other Mojos in here
 * one for extracting files (.mp3 and .ogg) from dependencies
 * once for editing the artifact at the end to remove things that're on the classpath and in the assets/ folder
These allow me to package all-the-things in classpath then sneak them into assets for APK builds.

Differences from vanilla-libGDX
-------------------------------
 * `Gdx.files.internal` falls back to classpath on Android (which is what it does on Desktop)
  * this is __BAD__ and will be removed
 * `com.badlogic.gdx.math.Frustum` does not use any native methods (so it can be used as a POJO)
 * `com.badlogic.gdx.math.Matrix4` does not use any native methods (so it can be used as a POJO)
 * `com.badlogic.gdx.math.Matrix4` does not use any shared objects (so it can be used in multiple threads)
 * the lwjgl backend depends on the version of lwjgl in Maven (because!)

Goals (1.3.4)
------------------
 * [x] fix build on Ubuntu 13.04
  * it was a problem of natives; I've stopped taking antives from libGDX
 * [x] move com.peterlavalle::droid into this project
 * [x] move com.peterlavalle::degen into this project
 * [x] flatten the module tree
 * [x] wicket style, change Mesh's "set" functions to return Mesh
  * [x] upstream this
 * added ariel-15 fnt/png files
  * moved Color to gdx.math
 * [x] upstream the "internal files fallback to classpath on Android"
  * __BAD__ there are actual reasons to use assets not classpath
 * [x] removal of libGDX version numbers
 * [x] droid::cull - needs to not-fail when there are no files to repack
 * [ ] droid::cull - needs reliable method of locating aapt
  * [x] rewrite it to have one-and-only-one maven plugin (but still multiple mojos)
 * [x] degen - when the zip file is not found, print an error. don't throw a nullpointer exception
 * [x] move SableCC scraping into this project
 * [x] support degenerating .tar.gz
  * (also updated the source listings)

Android Projects
================
This thing will / should skip the demo/apk files if you don't have ADK installed.
You don't need the ADK installed to build this project, but some modules will be skipped.
This uses the [Android Maven Plugin](http://code.google.com/p/maven-android-plugin/) to build your Android projects - which does mean [installing the ADK](http://code.google.com/p/maven-android-plugin/wiki/GettingStarted).
An examination of `demos.invaders.android` will reveal that I'm using [my own special blend](???) to get audio assets off of the classpath.
Knowing what I know now - I'd like to do this for ALL assets.

History
=======
DONE
----
 * 1.3.0 : use more complex recipes for sources, allowing multiple sources to be pulled in, extracted, examined, and renamed
 * 1.2.0 : two new features to actually work with libGDX on android
  * allows directories to be extracted from the archive (not just whole zip files anymore)
  * allows contents to be renamed
 * 1.1.4 : cache downloaded resources in root-parent's target
 * 1.1.3 : bugfixes
 * 1.1.2 : 100% Sonar compliance, 100% API documentation, >= 30% overall commenting
 * 1.1.1 : get it to "work" for libgdx (using new project)
 * 1.1.0 : allow the use of multiple (staggered) archives for a single project and recompile .java files that are extracted from sources with the same logic for all exclusions (instead of project source, then extracted source)
 * 1.0.3 : rename "resources" to "extracted" and "distributionZipURL" to "distribution" also rebuilt internals
 * 1.0.2 : get it to "work" with compiling the one Matrix4 class
 * 1.0.1 : get it to "work" with libGDX's gdx.jar when integrated into https://github.com/g-pechorin/libgdx-repack-dist
 * 1.0.0 : get it to "work" with libGDX's gdx.jar file from a localhost
