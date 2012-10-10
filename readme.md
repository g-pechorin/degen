(2012-10-08)

A Maven Mojo to "degenerate" non-maven projects into sources and binaries

degen
=====

This MOJO is supposed to scrape and "de-generate" a zip file containing jars into a project's generated-sources folder, skipping items provided in the `src/` folder.
It is intended to "mavenize-with-changes" an existing project and allow the user (of the Mojo) to replace classes which they do not have the ability/desire to recompile without setting up a full build.
It can also be used to import non-maven projects by not changing anything.

The itch I wanted to scratch was to replace libGDX's native methods on Matrix4, without setting up an environment to build the library.
In essence - it's an alternative to patching someone else's project.

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