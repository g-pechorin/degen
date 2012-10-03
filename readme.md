(Last updated 2012-10-03)

libGDX degen
============

This [libGDX](http://libgdx.badlogicgames.com/) re-packaged with my [degen](https://github.com/g-pechorin/degen)erate Mojo.
I wanted to use [libGDX](http://libgdx.badlogicgames.com/) in Maven, with sources, and I was tired of rolling Ant scripts.


The project downloads [libGDX](http://libgdx.badlogicgames.com/) distributions from Google Code via HTTP, and stores these in `target/`
This will only be downloaded on the first build, or if you `clean` (which will delete the archives)


Modules
=======
 * libgdx-degen.__root-0.9.6__ a root project for the 0.9.2 version (I was reading some old tutorials)
  * libgdx-repack-dist.__gdx__ the general [libGDX](http://libgdx.badlogicgames.com/) shared library. It includes everything from gdx.jar
  * libgdx-repack-dist.__lwjgl__ the desktop library based on lwjgl
  * libgdx-repack-dist.__android__ the android library
 * libgdx-degen.__root-0.9.6__ a root project for the 0.9.6 (current) version
  * libgdx-repack-dist.__gdx.math__ the basic math classes that [libGDX](http://libgdx.badlogicgames.com/) encompasses. It will be pure java when I'm done
  * libgdx-repack-dist.__gdx__ the general [libGDX](http://libgdx.badlogicgames.com/) shared library. It depends on the math library to get its math classes
  * libgdx-repack-dist.__lwjgl__ the desktop library based on lwjgl
  * libgdx-repack-dist.__android__ the android library

Since I'm tweaking stuff, and I don't want my [DropBox](http://db.tt/4thLOYa) filling up with snapshots, I am not trying to build a 0.9.7-SNAPSHOT at this time.

Repository
==========

I have a repository hosted by [DropBox](http://db.tt/4thLOYa)
You can use it if you don't want to rebuild all of this yourself.
```xml
    <repositories>
        <repository>
            <id>dropbox.to.uk.orangedog</id>
            <url>http://dl.dropbox.com/u/15094498/mvn-repo</url>
        </repository>
    </repositories>
```

Android Projects
================
You don't need the ADK installed to build this project itself.
You'll probably want to use the [Android Maven Plugin](http://code.google.com/p/maven-android-plugin/) to build your Android projects - which does mean [installing the ADK](http://code.google.com/p/maven-android-plugin/wiki/GettingStarted).


Native Code
===========
As a secondary goal, I wanted to reduce the reliance on native code - even though I've been told that the native code is faster on pre-Froyo devices.
I ran several (crude) synthetic benchmarks and didn't find this to (consitently) be true - your results may vary.
If you want to use standard [libGDX](http://libgdx.badlogicgames.com/)'s native code, delete the `src/` folder from `libgdx-repack-dist.gdx.math` and rebuild.