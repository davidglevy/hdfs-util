#!/bin/bash

git pull
mvn clean install
cd target
unzip hdfs-util-bin.zip
cd hdfs-util
cd bin
./hdfs-tar.sh $1 $2
