Degenerated
===========

This is a growing pile of other people's work that I've repakcaged into Maven.
My cool-ness here is that this project downloads the artifacts ... so anyone can (potentially) tweak parameters, hit build, and get their own distribution.

I have;

    * [libGDX](http://libgdx.badlogicgames.com/)
        ** with some tweaks I have not pushed upstream
        ** I'm aware that they've got a pom of their own - I don't think that it's in central; Aut inveniam viam aut faciam
    * [JBullet](http://jbullet.advel.cz/)


Building
-------
When the project builds, it
    * scrapes for the distributions
        ** it also "caches" the files in the root project's target folder
    * cracks that open for the .java, .class, .png, .fnt and whatever that it needs
    * packs them up (smartly) as if they were compiled by the project
        ** there's some beard twirling where it tries to pack .java files, but otherwise packs .class files

Other Mojos
-----------
There are two other Mojos in here
    * one for extracting files (.mp3 and .ogg) from dependencies
    * once for editing the artifact at the end to remove things that'r on the classpath and in the assets/ folder
These allow me to package all-the-things in classpath then sneak them into assets for APK builds.

Differences from vanilla-libGDX
-------------------------------
    * `Gdx.files.internal` falls back to classpath on Android (which is what it does on Desktop)
    * `com.badlogic.gdx.math.Frustum` does not use any native methods (so it can be used as a POJO)
    * `com.badlogic.gdx.math.Matrix4` does not use any native methods (so it can be used as a POJO)
    * `com.badlogic.gdx.math.Matrix4` does not use any shared objects (so it can be used in multiple threads)
    * the lwjgl backend depends on the version of lwjgl in Maven (because!)

Goals (1.3.4)
------------------
	* [x] fix build on Ubuntu 13.04
		** it was a problem of natives; I've stopped taking antives from libGDX
	* [x] move com.peterlavalle::droid into this project
	* [x] move com.peterlavalle::degen into this project
	* [x] flatten the module tree
	* [x] (wicket style) change Mesh's "set" functions to return Mesh
		** [x] upstream this
	* added ariel-15 fnt/png files
	* More Maths For Shaders
		** [x] Vector4 add class
		** [ ] add ivec classes
		** [ ] add bvec classes
		** [ ] modify shader class to accept these parameters
		** [ ] upstream it
	* [x] (scala friendly) give "Mesh" setters for Int* and Float*
		** [ ] upstream this
	* [ ] find out why the desktop demos don't play music
	* [ ] upstream the "internal files fallback to classpath on Android"
	* [x] removal of libGDX version numbers
	* [ ] droid::cull - needs to not-fail when there are no files to repack
		** this has been "done" but it has not been tested
	* [ ] droid::cull - needs reliable method of locating aapt
	* [ ] degen - when the zip file is not found, print an error. don't throw a nullpointer exception
 
Goals (1.3.5)
------------------
	* [ ] per-architecture backend modules
		** [ ] desktop
			*** [ ] .osx.x86_64
			*** [ ] .linux.x86
			*** [ ] .linux.x86_64
			*** [ ] .win32.x86
			*** [ ] .win64.x86_64
		** [ ] android
			*** [ ]apk.arm
			*** [ ]apk.armv7a
	* [ ] update to the libGDX version 0.9.??? (with the new modelling stuff)
		** remove any "my classes" that have been upstreamed
    * "full" Scala demos / archetypes
        ** [ ] Desktop
        ** [ ] RoboVM
        ** [ ] Applet
        ** [ ] Android
        ** [ ] JNLP
	* [ ] rewrite it to have one-and-only-one maven plugin (but still multiple mojos)
	* [ ] get `Gdx.files.local` files to read from the user's home directory on Windoze
	* [ ] degen - allow ?{} so that if the left side of the pattern exists, the right side file is copied i.e. ?{(.*)\.fnt@$1.png} copies all .png files who match a .fnt file
	* [ ] degen - print number of files copied per pattern
	* [ ] degen - allow "extract from downloaded archive" as well as current "download archive and extract from archive contained within"
	* [ ] degen - allow ".class over .java" when choosing what to include (so I can keep JBullet's sexy JStackAlloc optimizations)

Butter Scotch Goals
-------------------
	* [ ] Mojo to generate the whole Android project and build it (right from the "shared" library!)
	    ** AndroidManifest.xml
	    ** a main class
	    ** icon and resources
	* [ ] Mojo to generate an applet
	* __unicorns!__ everyone likes unicorns
	* a Mojo to do GDX-JNIGEN (or like) stuff and compile it, from the scraped sources
	* a Mojo to rewrite PNG (et al) images to be more-better and friendly to whatever format I'm using
	* A C++ / GLES2.0 / "backend" for NaCL
	* A C++ / CgFX / "backend" for ... other thing
	* "more better" audio library
	    ** load / play audio from classpath / byte streams / whatever
	    ** "more better" positional audio support
	* lodepng PNG loading (stb_lib has a few shortcomings)

Android Projects
================
This thing will / should skip the demo/apk files if you don't have ADK installed.
You don't need the ADK installed to build this project, but some modules will be skipped.
This uses the [Android Maven Plugin](http://code.google.com/p/maven-android-plugin/) to build your Android projects - which does mean [installing the ADK](http://code.google.com/p/maven-android-plugin/wiki/GettingStarted).

An examination of `demos.invaders.android` will reveal that I'm using [my own special blend](https://github.com/g-pechorin/droid-maven-plugin) to get audio assets off of the classpath.
I would like to "fix" this someday ... with non-asset audio (so I can do audio mixing in GPGPU GLSL) but I have toher priorities.
