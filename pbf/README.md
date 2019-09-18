## License

This library is released under the terms of the GNU Lesser General Public
License.

See [LGPL.md](LGPL.md) and [GPL.md](GPL.md) for details.

## About

See the [project homepage](http://www.jaryard.com/projects/osm4j/index.html) for
information about the library.

## Third party code

The code in the `crosby.binary` packages from Scott A. Crosby 
has been imported from this repository:

https://github.com/scrosby/OSM-binary

Which is released under the LGPL.

## Generating the protocol buffers source

    protoc --java_out lite:core/src/gen/java res/proto/*
    protoc --java_out full/src/gen/java res/proto/*

# Download

We provide access to the artifacts via our own Maven repository:

<https://mvn.topobyte.de>

The packages are available at these coordinates:

    de.topobyte:osm4j-pbf:0.2.0
    de.topobyte:osm4j-pbf-full-runtime:0.2.0

You can also browse the repository online:

* <https://mvn.topobyte.de/de/topobyte/osm4j-pbf/>
* <https://mvn.topobyte.de/de/topobyte/osm4j-pbf-full-runtime/>
