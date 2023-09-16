#!/bin/bash

RELATIVE_SCRIPT_DIR=$( dirname -- ${BASH_SOURCE[0]} )
SCRIPT_DIR=$( cd -- ${RELATIVE_SCRIPT_DIR} &> /dev/null && pwd )

cd ${SCRIPT_DIR}
source ./env.txt

GIT_ROOT_DIR=$( git rev-parse --show-toplevel )

LATEST_JAR=$( ls -v ${GIT_ROOT_DIR}/target/"${SOURCE_JAR_BASENAME}"*.war | tail -n 1 )
echo ${LATEST_JAR}

cp ${LATEST_JAR} "./${SOURCE_JAR_BASENAME}.war"

docker build -t ${TARGET_CONTAINER_NAME} --build-arg src="./${SOURCE_JAR_BASENAME}.war" .

rm "./${SOURCE_JAR_BASENAME}.war"
