# About

This is the osm4j module that contains incubating features.

# TODO list

## Diskstorage: Node and Way databases

* Instead of using Java's Serializiation feature for writing the indexes,
  implement binary I/O for the index data structure
* Write index into separate file, but when finished append to data file so that
  the whole database is just a single file instead of dreadful two
* Add an additional header file that contains a magic code, the file format
  (encoding whether it's a node or way db, with and without tags) and also store
  the position of data and index partitions within the database file there.
