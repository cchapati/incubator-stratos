#!/bin/bash
# ----------------------------------------------------------------------------
#
#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements.  See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership.  The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing,
#  software distributed under the License is distributed on an
#  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#  KIND, either express or implied.  See the License for the
#  specific language governing permissions and limitations
#  under the License.
#
# ----------------------------------------------------------------------------
#
# This script is for starting Apache Stratos servers.
# ----------------------------------------------------------------------------

# Die on any error:
set -e
product_list=$1
export LOG=$log_path/stratos.log
SLEEP=40

profile="default"

if [[ -f ./conf/stratos-setup.conf ]]; then
    source "./conf/stratos-setup.conf"
    echo "source it"
fi


function help {
    echo ""
    echo "Give one or more of the servers to start on this machine. The available servers are"
    echo "cc, as, sm, default. 'default' means you need to start all servers."
    echo "usage:"
    echo "stratos-start-servers.sh -p\"<profile>\""
    echo "eg."
    echo "stratos-start-servers.sh -p\"cc\""
    echo ""
}

while getopts p: opts
do
  case $opts in
    p)
        profile_list=${OPTARG}
        ;;
    \?)
        help
        exit 1
        ;;
  esac
done


arr=$(echo $profile_list | tr " " "\n")

for x in $arr
do
    if [[ $x = "cc" ]]; then
        profile="cc"
    elif [[ $x = "as" ]]; then
        profile="as"
    elif [[ $x = "sm" ]]; then
        profile="sm"
    else
        echo "Inavlid profile : 'default' profile will be selected."
        profile="default"
    fi
done

if [[ $profile = "default" ]]; then
    echo "Starting ActiveMQ server ..." >> $LOG
    $activemq_path/bin/activemq start
    echo "ActiveMQ server started" >> $LOG
    sleep $SLEEP
    sleep $SLEEP
fi

echo "Starting Stratos server ..." >> $LOG
echo "$stratos_extract_path/bin/stratos.sh -Dprofile=$profile start"
$stratos_extract_path/bin/stratos.sh -Dprofile=$profile start
echo "Stratos server started" >> $LOG
sleep $SLEEP
sleep $SLEEP


