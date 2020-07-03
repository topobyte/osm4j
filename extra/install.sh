#!/bin/bash

set -e

DIR=$(dirname $0)

pushd "$DIR" > /dev/null
./gradlew clean installDist setupScripts
popd

"$DIR"/cli/build/setup/install.sh
"$DIR"/cli/build/setup/post-install.sh
