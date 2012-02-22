Bigtop Legacy
=============

**These legacy libraries for Lift are no longer under development.  
If you would like to take ownership of any part of Bigtop Legacy, please [get in touch].**

The original vision for Bigtop was a collection of libraries extending the [Lift] web framework. Untyped have since refocused on developing web sites using [Blueeyes] and [Play], causing us to wipe the slate clean and start again with a [new Bigtop].

[get in touch]: http://untyped.com/contact.html
[Lift]: http://liftweb.net
[Blueeyes]: https://github.com/jdegoes/blueeyes
[Play]: https://github.com/playframework/Play20
[new Bigtop]: https://github.com/bigtop/bigtop

Development Status
------------------

Bigtop Legacy is no longer under development. However, we still use these libraries in a number of projects and are happy to accept bug fixes and pull requests.

If you would would like to take ownership of any part of Bigtop Legacy, including Bigtop Routes, please contact us via the *Issues* page or the [Untyped] web site. Alternatively, just fork away and get started - it's all open source, after all.

[Untyped]: http://untyped.com

Getting Bigtop Legacy
---------------------

You currently have two options:

 - Grab the source code from our [Github] repo and build it using SBT:
 
        git clone git://github.com/bigtop/bigtop-legacy.git bigtop-legacy
        cd bigtop
        sbt update
        sbt publish-local

 - Grab the artefacts directly from our [Maven] repository:
 
   - Server: `repo.untyped.com`
   - Group: `bigtop-legacy`
   - Artefact: `bigtop-«LIBRARY-NAME»_«SCALA-VERSION»`
   - Version: `«BIGTOP-VERSION»`

where:

 - `«LIBRARY-NAME»` is the name of the Bigtop library you need (e.g. `util` or `debug`);
 - `«SCALA_VERSION»` is the Scala language version (e.g. `2.8.1` or `2.9.0-1`);
 - `«BIGTOP-VERSION»` is the version of Bigtop (e.g. `0.1` or `0.2`).

Browse the [Maven] repository to find the latest versions and the applicable versions of Scala.

[Github]: https://github.com/bigtop/bigtop
[Maven]: http://repo.untyped.com/bigtop

License
-------

Bigtop is copyright Untyped Ltd and released under the Apache 2.0 License.

See the `LICENSE` file for the full license text.
