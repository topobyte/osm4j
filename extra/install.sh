#!/bin/bash

set -e

DIR=$(dirname $0)

pushd "$DIR" > /dev/null
./gradlew clean installDist postInstallScript
popd

TARGET="$HOME/share/topobyte/osm4j-extra/osm4j-extra-snapshot"

mkdir -p "$TARGET"
rsync -av --delete "$DIR/cli/build/install/osm4j-extra/" "$TARGET"

"$DIR"/cli/build/setup/post-install.sh
