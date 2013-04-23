

libGDX degen
============

This [libGDX](http://libgdx.badlogicgames.com/) re-packaged with my [degen](https://github.com/g-pechorin/degen)erate Mojo.
I wanted to use [libGDX](http://libgdx.badlogicgames.com/) in Maven, with sources, and I was tired of rolling Ant scripts.
The project downloads [libGDX](http://libgdx.badlogicgames.com/) distributions from GitHub via HTTP, and stores it in `target/`



Next Goals
----------
As of 2013-04-01 I'm still using this, and updating it in connection with coursework.
I'm using Hg-Git as my client, which can be flaky (and developed by GitHub, so it's kind of weird they don't maintain it)
 * find out why the desktop demos don't play music
 * Groovy (+groovy android), jnlp and applet demos
 * flatten the module tree
 * removal of libGDX version numbers
 * update to the libGDX version 0.9.8 with the new modelling stuff
 * droid::cull needs to not-fail when there are no files to repack

Done Goals (1.3.4)
------------------
 * move com.peterlavalle::droid into this project
 * move com.peterlavalle::degen into this project
 
Butter Scotch Goals
-------------------
 * __unicorns!__ everyone likes unicorns
 * a Mojo to build / package final binaries from projects
  * __desktop__	collect all dependencies, apply ProGuard, apply Launch4j
  * __applet__	collect all dependencies, apply ProGuard, sign it, include (but don't insert) a HelloApplet.html that displays it
  * __jnlp__	collect all dependencies, apply ProGuard, sign it, include (but don't insert) a HelloApplet.html that launches it
  * __apk__		collect all dependencies, apply ProGuard if debugging
 * switch to Scala - because!



Modules
=======
 * libgdx-degen.__root__ a root project for the 0.9.6 version of [libGDX](http://libgdx.badlogicgames.com/)
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
I would like to "fix" this someday ...
