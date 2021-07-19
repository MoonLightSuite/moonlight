#!/bin/bash
./gradlew clean
./gradlew distribution --parallel
./gradlew distribution
./gradlew release