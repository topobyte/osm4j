# Commands

Create a changeset:

    curl -X PUT -u user:pass https://master.apis.dev.openstreetmap.org/api/0.6/changeset/create -d "<osm><changeset><tag k=\"created_by\" v=\"test 1.0\"/></changeset></osm>"

Close a changeset:

    curl -X PUT -u user:pass https://master.apis.dev.openstreetmap.org/api/0.6/changeset/165396/close
