degen
=====

This MOJO is supposed to scrape and "de-generate" a zip file containing jars into a project's generated-sources folder.
It is intended for "mavenize-with-changes" existing projects and allow the user (of the MOJO) to replaces certain classes which the do not have the ability/desire to recompile.

The itch I wanted to scratch was to replace libGDX's native methods on Matrix4

DONE
====
 * 1.0.0 : get it to "work" with libGDX's gdx.jar file from a localhost

TODO
====
 * 1.0.1 : get it to "work" with libGDX's gdx.jar when integrated into https://github.com/g-pechorin/libgdx-repack-dist
 * 1.0.2 : get it to "work" with compiling the one Matrix4 class
 * 1.1.0 : allow the use of multiple (staggered) archives for a single project
 * 1.1.1 : rename "resources" to "esources"
 * 1.1.2 : get it to "work" with the whole of https://github.com/g-pechorin/libgdx-repack-dist
 * 1.1.3 : use the same logic for all exclusions (instead of project source, then extracted source)
 * 2.0.0 : get source roots from the project object, but ignore any which are in target
 * 2.0.1 : delete previously extracted resources
 * 2.0.2 : skip extracting resources that are already present and up to date