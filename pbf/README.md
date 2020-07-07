# About

This is the osm4j module for reading and writing OSM data in the PBF format.

## Third party code

The code in the `crosby.binary` packages from Scott A. Crosby 
has been imported from this repository:

https://github.com/scrosby/OSM-binary

Which is released under the LGPL.

# Hacking

## Generating the protocol buffers source

    protoc --java_out lite:core/src/gen/java res/proto/*
    protoc --java_out full/src/gen/java res/proto/*
