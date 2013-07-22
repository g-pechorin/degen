Degenerated
===========

This is a growing pile of other people's work that I've repackaged into Maven.
My cool-ness here is that this project downloads the artifacts ... so anyone can (potentially) tweak parameters, hit build, and get their own distribution.
At the moment, it's all focussed around libGDX.

I have;

 * [libGDX](http://libgdx.badlogicgames.com/)
  * with some tweaks I have not pushed upstream
  * I'm aware that they've got a pom of their own - I don't think that it's in central; Aut inveniam viam aut faciam
 * [JBullet](http://jbullet.advel.cz/)


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
 * [ ] find out why the desktop demos don't play music
 * [x] upstream the "internal files fallback to classpath on Android"
  * __BAD__ there are actual reasons to use assets not classpath
 * [x] removal of libGDX version numbers
 * [x] droid::cull - needs to not-fail when there are no files to repack
 * [ ] droid::cull - needs reliable method of locating aapt
  * [x] rewrite it to have one-and-only-one maven plugin (but still multiple mojos)
 * [ ] degen - when the zip file is not found, print an error. don't throw a nullpointer exception
 * [ ] move SableCC scraping into this project
 
Goals (1.3.5)
------------------
 * CMake
  * [ ] get it to emit cmake lists
  * [ ] compile stuff with cmake and attach it to the project
  * [ ] attach headers files to a project
  * [ ] unpack dependecy headers (if available)
  * [ ] unpack and attach dependency libs (if available)
  * [ ] cross compile cmake stuff to os/arch permutations
  * [ ] do GDX-JNIGEN stuff
 * [ ] per-architecture backend modules
  * [ ] .osx.x86_64
  * [ ] .linux.x86
  * [ ] .linux.x86_64
  * [ ] .win.x86
  * [ ] .win.x86_64
  * [ ] .apk.arm
  * [ ] .apk.armv7a
  * [ ] .apk.x86
  * [ ] .bsd.x86
  * [ ] .bsd.x86_64
  * [ ] .bsd.ppc
 * [ ] update to the libGDX version 0.9.??? (with the new modelling stuff)
  * remove any "my classes" that have been upstreamed
 * "full" Scala demos / archetypes
  * [ ] Desktop
  * [ ] RoboVM
  * [ ] Applet
  * [ ] Android
  * [ ] JNLP
  * [ ] Scala-GWT
 * [ ] get `Gdx.files.local` files to read from the user's home directory on Windoze
 * [ ] degen - allow ?{} so that if the left side of the pattern exists, the right side file is copied i.e. ?{(.*)\.fnt@$1.png} copies all .png files who match a .fnt file
 * [ ] degen - print number of files copied per pattern
 * [ ] degen - allow "extract from downloaded archive" as well as current "download archive and extract from archive contained within"
 * [ ] degen - allow ".class over .java" when choosing what to include (so I can keep JBullet's sexy JStackAlloc optimizations)

Butter Scotch Goals
-------------------
 * [ ] Mojo to generate the whole Android project and build it (right from the "shared" library!)
  * AndroidManifest.xml
  * a main class
  * icon and resources
 * [ ] Mojo to generate an applet
 * a https://code.google.com/p/getdown/ launcher mojo doohickey that unspools stuff (for $reasons)
 * __unicorns!__ everyone likes unicorns
 * a Mojo to rewrite PNG (et al) images to be more-better and friendly to whatever format I'm using
 * A C++ / GLES2.0 / "backend" for NaCL
 * A C++ / CgFX / "backend" for ... other thing
 * lodepng PNG loading (stb_lib has a few shortcomings)

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


TODO
----
 * 2.0.1 : add non-scriptable "override" MOJO which locates a .java file within the distribution and archives, then copies it out into the src/main/java folder
 * 2.0.2 : add "remove" MOJO which deletes any .java files in src/main/java which match .java files within the distribution and archives definition
 * ?.0.0 : skip extracting resources that are already present and up to date, remove extracted resources that don't exist
 * ?.0.0 : allow google code and svn repos for import (should already work with github through zipballs)
 * ?.0.0 : allow non-zip archive formats (.tar .tar.gz .tar.bz .tar.bz2 .7z)
 * ?.0.0 : allow the use of dependencies in the plugin
 * ?.0.0 : allow import of tests
