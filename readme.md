

libGDX degen
============

This [libGDX](http://libgdx.badlogicgames.com/) re-packaged with my [degen](https://github.com/g-pechorin/degen)erate Mojo.
I wanted to use [libGDX](http://libgdx.badlogicgames.com/) in Maven, with sources, and I was tired of rolling Ant scripts.
The project downloads [libGDX](http://libgdx.badlogicgames.com/) distributions from GitHub via HTTP, and stores it in `target/`



Next Goals
----------
As of 2013-06-06 I'm still using this, and updating it in connection with coursework.
I'm using TortoiseHg + Hg-Git as my client, which can be flaky.
Considering GitHub wrote Hg-Git, I feel the flakiness is from GitHub.

Goals (1.3.4)
------------------
	* [x] move com.peterlavalle::droid into this project
	* [x] move com.peterlavalle::degen into this project
	* [x] flatten the module tree
	* [x] (wicket style) change Mesh's "set" functions to return Mesh
		** [x] upstream this
	* added ariel-15 fnt/png files
	* More Maths For Shaders
		** [x] Vector4 add class
		** [ ] Vector4 add remaining methods
		** [ ] add ivec classes
		** [ ] add bvec classes
		** [ ] modify shader class to accept these parameters
		** [ ] upstream it
	* [x] (scala friendly) give "Mesh" setters for Int* and Float*
		** [ ] upstream this
	* [ ] find out why the desktop demos don't play music
	* [ ] upstream the "internal files fallback to classpath on Android"
	* Demos
		** [ ] desktop Scala demo
		** [ ] applet Scala demo
		** [ ] Android Scala demo
		** [ ] jnlp Scala demo
	* [x] removal of libGDX version numbers
	* [ ] droid::cull needs to not-fail when there are no files to repack
	* [ ] degen - when the zip file is not found, print an error. don't throw a nullpointer exception
 
Goals (1.3.5)
------------------
	* ProGuard
		** [ ] add a mojo to execute it
		** [ ] add a dexguard module
	* [ ] move all natives to backends
	* [ ] per-architecture backend moduless
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
	* [ ] RoboVM Scala demo
	* [ ] applet Scala demo
	* [ ] rewrite it to have one-and-only-one maven plugin (but still multiple mojos from multiple modules)
	* [ ] build an "applet stuffs"
		** [ ] scrape lwjgl_applet_util
		** [ ] make mojo for emitting things?
		** [ ] tweak LWJGL stuff for applets to "just" appear in the applet backend
	* [ ] get local files to read from the user's home directory on Windoze
	* [ ] Mojo to generate AndroidManifest.xml
	* [ ] degen - allow ?{} so that if the left side of the pattern exists, the right side file is copied i.e. ?{(.*)\.fnt@$1.png} copies all .png files who match a .fnt file
	* [ ] degen - print number of files copied per pattern

Butter Scotch Goals
-------------------
	* Mojo to generate pop out an avianvm installer/uninstllaer
		** would need to compile windows32 / windows64 version
		** also need icon too
	* __unicorns!__ everyone likes unicorns
	* a Mojo to do GDX-JNIGEN (or like) stuff and compile it
	* A C++ / GLES2.0 / "backend" for NaCL
	* A C++ / CgFX / "backend" for ... other thing
	* non-asset audio system
	* lodepng PNG loading (stb_lib has a few shortcomings)

Android Projects
================
This thing will / should skip the demo/apk files if you don't have ADK installed.
You don't need the ADK installed to build this project, but some modules will be skipped.
This uses the [Android Maven Plugin](http://code.google.com/p/maven-android-plugin/) to build your Android projects - which does mean [installing the ADK](http://code.google.com/p/maven-android-plugin/wiki/GettingStarted).

An examination of `demos.invaders.android` will reveal that I'm using [my own special blend](https://github.com/g-pechorin/droid-maven-plugin) to get audio assets off of the classpath.
I would like to "fix" this someday ... with non-asset audio (so I can do audio mixing in GPGPU GLSL) but I have toher priorities.
