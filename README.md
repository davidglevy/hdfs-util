# hdfs-util
Small library of helper project utilities for HDFS

## Pre-requisites
This utility is only built to run on secured clusters with Kerberos. You should have your Kerberos credentials renewed, issuing either a "kinit -R" or "kinit" if that fails.

## Build
To build this utility run "mvn clean install"

## Build Artifact
This project builds both the JAR, "hdfs-util.jar", as well as the assembled binary with dependencies, "hdfs-util-bin.zip". If
you wanted to move the utility to a different system without rebuilding, copy the hdfs-util-bin.zip.

## Running the utilities
Unzip the assembled package, "hdfs-util-bin.zip".

### Unpack a tar.gz
Run the command with "./bin/hdfs-util.sh [tar-gz file in HDFS] [hdfs destination]

## Do It All
To do a build/package/run cycle, A small convience wrapper, "run.sh" has been made.
Run the command "run.sh" with the parameters [tar-gz file in HDFS] [hdfs destination] to do a pull, build and run.
