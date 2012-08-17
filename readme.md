libGDX degen
============

This is a degenerate version of libGDX which I built.
I wanted to reduce the use of native code to increase speed and reusability.
With our without the Android SDK this sucker will build just fine as a pure maven project.
It will have to scrape a whole libGDX distribution from Google Code when it does this - but I'm working on that.

There are four modules
 * libgdx-repack-dist.gdx.math the basic math classes that libGDX encompasses. It will be pure java when I'm done
 * libgdx-repack-dist.gdx the general libGDX shared library. It includes the math library to get its math classes
 * libgdx-repack-dist.lwjgl the desktop library based on lwjgl (I think it should be .desktop)
 * libgdx-repack-dist.android the android library