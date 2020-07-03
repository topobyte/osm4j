#!/bin/bash

set -e

DIR=$(dirname $0)
ROOT="$DIR/../"

pushd "$ROOT" > /dev/null
./gradlew osm4j-utils-cli:clean osm4j-utils-cli:installDist osm4j-utils-cli:setupScripts
popd

"$DIR"/cli/build/setup/install.sh
"$DIR"/cli/build/setup/post-install.sh
