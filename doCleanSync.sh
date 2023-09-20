#!/bin/bash

# Clear the local gradle caches to prevent garbage artifacts.
rm -rf ~/.gradle/caches
./gradlew task syncToLocalMaven