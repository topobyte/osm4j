## License

This library is released under the terms of the GNU Lesser General Public
License.

See LGPL.md and GPL.md for details.

## About

See the [project homepage](http://www.jaryard.com/projects/osm4j/index.html) for
information about the library.

## Third party code

The code in the `crosby.binary` packages from Scott A. Crosby 
has been imported from this repository:

https://github.com/scrosby/OSM-binary

Which is released under the LGPL.

## Generating the protocol buffers source

    protoc --java_out lite:core/src/gen-lite/java res/proto/*
