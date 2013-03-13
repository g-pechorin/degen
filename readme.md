(Last updated 2012-10-03 ... so this is rather out of date)

libGDX degen
============

This [libGDX](http://libgdx.badlogicgames.com/) re-packaged with my [degen](https://github.com/g-pechorin/degen)erate Mojo.
I wanted to use [libGDX](http://libgdx.badlogicgames.com/) in Maven, with sources, and I was tired of rolling Ant scripts.


The project downloads [libGDX](http://libgdx.badlogicgames.com/) distributions from Google Code via HTTP, and stores these in `target/`
This will only be downloaded on the first build, or if you `clean` (which will delete the archives)


Modules
=======
	* libgdx-degen.__root__ a root project for the 0.9.6 version of [libGDX](http://libgdx.badlogicgames.com/)
		* libgdx-repack-dist.__gdx.math__ the basic math classes that [libGDX](http://libgdx.badlogicgames.com/) encompasses. It is made up entierly of POJO (I've replaced a few methods)
		* libgdx-repack-dist.__gdx__ the general [libGDX](http://libgdx.badlogicgames.com/) shared library. It depends on the math library to get its math classes. There's some native code
		* libgdx-repack-dist.__lwjgl__ the desktop library based on lwjgl
		* libgdx-repack-dist.__android__ the android library
	* demos.__invaders__ a root project for the invaders demo game
		* demos.__invaders.shared__ the shared code for the invaders demo game
		* demos.__invaders.lwjgl__ the code for the desktop invaders demo game
		* demos.__invaders.android__ the code for the android invaders demo game


Since I'm tweaking stuff, and I don't want my [DropBox](http://db.tt/4thLOYa) filling up with snapshots, I am not trying to build the new version at this time.
... someday, that'll change; for the moment there are other things I'd like to work on.

Android Projects
================
You don't need the ADK installed to build this project itself.
You'll probably want to use the [Android Maven Plugin](http://code.google.com/p/maven-android-plugin/) to build your Android projects - which does mean [installing the ADK](http://code.google.com/p/maven-android-plugin/wiki/GettingStarted).

An examination of `demos.invaders.android` will reveal that I'm using my own special blend](https://github.com/g-pechorin/droid-maven-plugin) to get audio assets off of the classpath.
I would like to "fix" this  omeday ... and the "[PNG](http://stackoverflow.com/questions/14171255/libgdx-cant-load-a-specific-image) issue" since smaller PNG is a good thing.


Native Code
===========
I wanted to reduce the reliance on native code ... because that sounded good and was faster in my tests.

(I'm aware that native code is faster on some old devices that I don't have access)

If you want to use standard [libGDX](http://libgdx.badlogicgames.com/)'s native code, delete the `src/` folder from `libgdx-repack-dist.gdx.math` and rebuild.

(deleting the `src/` folder will cause the [degen](https://github.com/g-pechorin/degen) mojo to use the original .java files which dialed-out to native code)