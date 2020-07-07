# About

This is the osm4j module that provides various additional utilities as well as
command line tools for performing additional tasks on OSM data.

# Installation

To install the CLI tools, run this:

    ./install.sh

This command will build the tools and install them into your `~/bin`
directory.

# Usage

To build extraction data structures for a region execute this:

    OsmExtraBuildExtractionFiles --input-format pbf --input country.osm.pbf \
        --output extraction --max-nodes 150000 \
        --max-members-simple 20000 --max-members-complex 20000
