# About

This is the osm4j module for reading and writing OSM data in the PBF format.

## Third party code

The code in the `crosby.binary` packages from Scott A. Crosby 
has been imported from this repository:

https://github.com/scrosby/OSM-binary

Which is released under the LGPL.

# Hacking

## Generating the protocol buffers source

First generate the protocol buffer source files using `protoc`:

    protoc --java_out lite:core/src/gen/java res/proto/*
    protoc --java_out full/src/gen/java res/proto/*

The generated source files don't compile in Eclipse due to a strange bug
involving a cast with generic types. Fortunately, the cast doesn't seem to
be necessary so we can remove it and make Eclipse happy:

    find core/src/gen/ -name "*.java" | xargs sed -i -e "s/return (Builder) DEFAULT_INSTANCE/return DEFAULT_INSTANCE/"
