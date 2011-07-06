Bigtop
======

Bigtop is a collection of libraries that extend the excellent [Lift] web framework.

[Lift]: http://liftweb.net

The goals of Bigtop are to provide comprehensive and flexible libraries to automate common tasks, and to make it even easier to develop highly interactive web sites using Lift. There is a strong focus on documentation and providing a consistent architecture.

Please see the [web site] for more information.

[web site]: http://bigtopweb.com

Development Status
------------------

Bigtop is currently in extremely early alpha. There's lots of code and documentation that isn't in place yet. Please bear with us as we get ourselves up and running.

Getting Bigtop
--------------

You currently have two options:

 - Grab the source code from our [Github] repo and build it using SBT:
 
       git clone git://github.com/bigtop/bigtop.git bigtop
       cd bigtop
       sbt update
       sbt publish-local

 - Grab the artefacts directly from our [Maven] repository:
 
   - Server: `repo.untyped.com`
   - Group: `bigtop`
   - Artefact: `bigtop-«LIBRARY-NAME»_«SCALA-VERSION»`
   - Version: `«BIGTOP-VERSION»`

where:

 - `«LIBRARY-NAME»` is the name of the Bigtop library you need (e.g. `util` or `debug`);
 - `«SCALA_VERSION»` is the Scala language version (e.g. `2.8.1` or `2.9.0-1`);
 - `«BIGTOP-VERSION»` is the version of Bigtop (e.g. `0.1` or `0.2`).

Browse the [Maven] repository to find the latest versions and the applicable versions of Scala.

[Github]: https://github.com/bigtop/bigtop
[Maven]: http://repo.untyped.com/bigtop

