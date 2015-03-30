#!/bin/sh

./gradlew -Drun.args="$2 $3" :`basename $1 /`:run
