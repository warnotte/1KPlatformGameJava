#!/bin/bash
# Wrapper script for gradle
if [ -x ./gradlew ]; then
  ./gradlew "$@"
else
  gradle "$@"
fi
