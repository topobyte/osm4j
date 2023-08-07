# About

This is the osm4j module that provides various additional utilities as well as
command line tools for performing additional tasks on OSM data.

# Installation

To install the CLI tools, run this:

    ./install.sh

This command will build the tools and install them into your `~/bin`
directory.

# Usage

## Extraction data structure

This module includes a mechanism for building a file based extraction data
structure. It processes an OSM data set and prepares it in multiple steps
so that querying for regions of data becomes very efficient.
The extraction process is optimized for the use case of producing extracts
with full referential integrity, i.e. it is possible to extract region data
sets that contain all nodes within that region, all the ways and relations
intersecting that region and also all the way nodes and relation members
of ways and relations included.

To build extraction data structures for a region execute this:

    OsmExtraBuildExtractionFiles --input-format pbf --input-file country.osm.pbf \
        --output-format tbo --output extraction --max-nodes 150000 \
        --max-members-simple 20000 --max-members-complex 20000

To then extract data for a region:

    OsmExtraQueryRegion --input-format tbo --output-format tbo \
        --input extraction/ --output output.tbo --region region.wkt

Building the extraction files from the planet file takes significant time.
Of course the exact time this is going to take depends on the hardware you
run this on. Here are a few samples.

Back in July 2017 when the planet file contained approximately
4 billion nodes, 420 million ways and 400 thousand relations, the whole process
took about 8 hours on a root server that costs around $10 per month and
has 4 dedicated CPU cores:

| Step                              | Duration       |
|-----------------------------------|---------------:|
| split                             | 1h 19m 29s     |
| compute bbox                      | 0s             |
| build nodetree                    | 56m 31s        |
| sort ways by first node id        | 25m 24s        |
| map ways to tree                  | 37m 51s        |
| find missing way nodes            | 25m 45s        |
| extract missing way nodes         | 17m 30s        |
| distribute ways                   | 1h 44m 11s     |
| merge tree node files             | 20m 48s        |
| merge tree way files              | 16m 14s        |
| separate simple/complex relations | 32s            |
| split relations, collect members  | 38m 18s        |
| distribute relations              | 24m 9s         |
| sort complex tree relations       | 1s             |
| sort non-tree relations           | 15m 18s        |
| clean up                          | 2s             |
| create geometries                 | 0s             |
| **total**                         | **7h 42m 10s** |

In August 2023, the planet file contained approximately
8.5 billion nodes, 960 million ways and 11 million relations.
On different but similar hardware as mentioned above the whole
process took about 30 hours:

| Step                              |        Duration |
|-----------------------------------|----------------:|
| split                             | 1h 22m 30s      |
| compute bbox                      | 0s              |
| build nodetree                    | 4h 50m 27s      |
| sort ways by first node id        | 59m 29s         |
| map ways to tree                  | 3h 13m 40s      |
| find missing way nodes            | 3h 22m 52s      |
| extract missing way nodes         | 1h 21m 12s      |
| distribute ways                   | 4h 24m 52s      |
| merge tree node files             | 2h 31m 41s      |
| merge tree way files              | 1h 32m 59s      |
| separate simple/complex relations | 3m 1s           |
| split relations, collect members  | 3h 8m 13s       |
| distribute relations              | 1h 22m 36s      |
| sort complex tree relations       | 6s              |
| sort non-tree relations           | 1h 57m 5s       |
| clean up                          | 5s              |
| create geometries                 | 2s              |
| **total**                         | **30h 21m 40s** |
