# History files

History files contain a 'visible' attribute for entities that can be set to
'false' to mean that it has been deleted.
In practice these files seem to have some different properties than
normal files.

In XML representation, invisible nodes do not carry 'lat' on 'lon'
attributes.

In PBF representation, our current code reads NaN for lat/lon values of
such invisible nodes.

These situtations are currently not supported as lat and lon are not
optional.

Ways and relations are less special, as invisible instances of these
entities simply do not contain any members which is not a overly exceptional
situtation, the implementation will just contain empty lists.

We need to investigate
* if the data we get for real world data is different dependending on
  dense / non-dense PBF structure for nodes
* how we can write them in a similar way so that they don't get changed to
  0 when rewriting PBF files
* represent this on nodes (something like 'hasCoordinates()' and make
  lat/lon nullable? Or use special values such as NaN?

## Real world files

Geofabrik has internal history files for download after login.

To convert their files to different formats, we should probably use
'osmium' as a reference implementation.

Convert pbf to xml:

    osmium cat berlin-internal.osh.pbf -o berlin-internal.osh.xml

Open points:
* Check if pbf file has dense or non-dense node storage
* Convert pbf to pbf with non-dense
