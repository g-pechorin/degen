degen
=====

This MOJO is supposed to scrape and "de-generate" a zip file containing jars into a project's generated-sources folder.
It is intended for "mavenize-with-changes" existing projects and allow the user (of the MOJO) to replaces certain classes which the do not have the ability/desire to recompile.

The itch I wanted to scratch was to replace libGDX's native methods on Matrix4, without setting up an environment to build the library.
In essence - it's an alternative to setting up a patch, which should be more robust when used with continuous integration / cron jobs.

History
=======
DONE
----
 * 1.0.3 : rename "resources" to "extracted" and "distributionZipURL" to "distribution" also rebuilt internals
 * 1.0.2 : get it to "work" with compiling the one Matrix4 class
 * 1.0.1 : get it to "work" with libGDX's gdx.jar when integrated into https://github.com/g-pechorin/libgdx-repack-dist
 * 1.0.0 : get it to "work" with libGDX's gdx.jar file from a localhost


TODO
----
 * 1.1.0 : allow the use of multiple (staggered) archives for a single project
 * 1.1.1 : get it to "work" with the whole of https://github.com/g-pechorin/libgdx-repack-dist and remove all ANT tags (YaY?)
 * 1.1.2 : get it to recompile .java files that are extracted from sources
 * 1.1.3 : use the same logic for all exclusions (instead of project source, then extracted source)
 * 1.2.0 : preserve the timestamps from the extracted resources
 * 1.2.1 : skip extracting resources that are already present and up to date
 * 2.0.0 : move distribution and archives definition out of the MOJO configuration or find some other way to share the distribution and archives definition
 * 2.0.1 : add "revert" MOJO which locates a .java file within the distribution and archives definition then copies it out into the src/main/java folder
 * 2.0.2 : add "remove" MOJO which deletes any .java files in src/main/java which match .java files within the distribution and archives definition
 * ?.0.0 : allow remote/revert/remove from google code/browseable repos (already works with github due to the zipball)
 * ?.0.0 : allow non-zip archive formats (.tar .tar.gz .tar.bz .tar.bz2 .7z)