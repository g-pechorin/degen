

libGDX degen
============

This [libGDX](http://libgdx.badlogicgames.com/) re-packaged with my [degen](https://github.com/g-pechorin/degen)erate Mojo.
I wanted to use [libGDX](http://libgdx.badlogicgames.com/) in Maven, with sources, and I was tired of rolling Ant scripts.
The project downloads [libGDX](http://libgdx.badlogicgames.com/) distributions from GitHub via HTTP, and stores it in `target/`



Next Goals
----------
As of 2013-06-06 I'm still using this, and updating it in connection with coursework.
I'm using Hg-Git as my client, which can be flaky (and developed by GitHub, so it's kind of weird they don't maintain it)

Goals (1.3.4)
------------------
	* [x] move com.peterlavalle::droid into this project
	* [x] move com.peterlavalle::degen into this project
	* [x] flatten the module tree
	* [x] (wicket style) change Mesh's "set" functions to return Mesh
		** [x] upstream this
	* 
		** [x] Vector4 add class
		** [ ] Vector4 add remaining methods
		** [ ] add ivec classes
		** [ ] add bvec classes
		** [ ] modify shader class to accept these parameters
		** [ ] upstream it
	* [ ] (scala friendly) give "Mesh" setters for Array[Int]
		** [ ] upstream this
	* [ ] find out why the desktop demos don't play music
	* [ ] get local files to read from the user's home directory on Windoze
		** [ ] upstream this
	* [ ] upstream the "internal files fallback to classpath on Android"
	* Demos
		** [ ] (desktop/Android) demo
		** [ ] jnlp and applet demos
	* [ ] removal of libGDX version numbers
	* [ ] update to the libGDX version 0.9.??? (with the new modelling stuff)
		** remove any "my classes" that have been upstreamed
	* [ ] droid::cull needs to not-fail when there are no files to repack
	* [ ] Mojo to generate AndroidManifest.xml
 
Butter Scotch Goals
-------------------
	* __unicorns!__ everyone likes unicorns
	* a Mojo to do ProGuard on libGDX (build mega-jar, save listener and native, optimize everything else)
	* a Mojo to do GDX-JNIGEN (or like) stuff and compile it
	* A C++ / GLES / CG "backend" for that one system that uses those things
	* A C++ / GLES2.0 / "backend" for NaCL
	* non-asset audio system
	* lodepng PNG loading (stb_lib has a few shortcomings)



Modules
=======
 * libgdx-repack-dist.__gdx.math__ the basic math classes that [libGDX](http://libgdx.badlogicgames.com/) encompasses. It is made up entierly of POJO (which means I've replaced a few methods)
 * libgdx-repack-dist.__gdx__ the general [libGDX](http://libgdx.badlogicgames.com/) shared library. It depends on the math library to get its math classes. There's some native code
 * libgdx-repack-dist.__lwjgl__ the desktop library based on lwjgl
 * libgdx-repack-dist.__android__ the android library
 * demos.__invaders__ a root project for the invaders demo game
  * demos.__invaders.shared__ the shared code for the invaders demo game
  * demos.__invaders.lwjgl__ the code for the desktop invaders demo game
  * demos.__invaders.android__ the code for the android invaders demo game
 * __libgdx.scala.demo__ a root POM for the scala demos
  * __libgdx.scala.demo.game__ a very empty Scala libGDX game
  * __libgdx.scala.demo.desktop__ a (possible) Platform Runtime for the Scala game
   * it compiles fine in Maven
   * it runs fine in Maven
   * NetBeans doesn't like it
   * NetBeans doesn't like any Scala project with multiple modules?
   * I have not tried Eclipse

Android Projects
================
This thing will / should skip the demo/apk files if you don't have ADK
You don't need the ADK installed to build this project, but some modules will be skipped.
This uses the [Android Maven Plugin](http://code.google.com/p/maven-android-plugin/) to build your Android projects - which does mean [installing the ADK](http://code.google.com/p/maven-android-plugin/wiki/GettingStarted).

An examination of `demos.invaders.android` will reveal that I'm using my own special blend](https://github.com/g-pechorin/droid-maven-plugin) to get audio assets off of the classpath.
I would like to "fix" this someday ... with non-asset audio so I can do mixing in GPGPU GLSL
