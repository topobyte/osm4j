#!/bin/bash

OsmExtraBuildExtractionFiles --input-format pbf --input country.osm.pbf \
    --output extraction --max-nodes 150000 \
    --max-members-simple 20000 --max-members-complex 20000
