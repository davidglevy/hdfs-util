#!/bin/bash

# Add function to expand home directory:
#
# From here: http://stackoverflow.com/a/29310477/1508064
#
expandPath() {
  case $1 in
    ~[+-]*)
      local content content_q
      printf -v content_q '%q' "${1:2}"
      eval "content=${1:0:2}${content_q}"
      printf '%s\n' "$content"
      ;;
    ~*)
      local content content_q
      printf -v content_q '%q' "${1:1}"
      eval "content=~${content_q}"
      printf '%s\n' "$content"
      ;;
    *)
      printf '%s\n' "$1"
      ;;
  esac
}


SPARK_KAFKA_VERSION=0.10
export SPARK_KAFKA_VERSION

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
APP_JAR="${DIR}/../hdfs-util.jar"
echo $APP_JAR
SPARK_JARS=""
FIRST_ITER=true

for x in `ls "${DIR}/../lib/runtime"`
do
  if [ "$FIRST_ITER" = false ] ; then
    SPARK_JARS="${SPARK_JARS}:"
  fi
  if [ "$FIRST_ITER" = true ] ; then
    FIRST_ITER=false
  fi
  SPARK_JARS="${SPARK_JARS}${DIR}/../lib/runtime/${x}"
done
echo $SPARK_JARS

##
## We need to expand the path before passing to spark files argument.
## 
RUNTIME_PROPERTY_FILE=$(expandPath '~/runtime.json')

java -cp $APP_JAR:$SPARK_JARS com.cloudera.hdfsutil.HdfsTar $1 $2