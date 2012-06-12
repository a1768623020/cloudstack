#!/usr/bin/env bash
# Copyright 2012 Citrix Systems, Inc. Licensed under the
# Apache License, Version 2.0 (the "License"); you may not use this
# file except in compliance with the License.  Citrix Systems, Inc.
# reserves all rights not expressly granted by the License.
# You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# 
# Automatically generated by addcopyright.py at 04/03/2012



 

# $Id: ipassoc.sh 9804 2010-06-22 18:36:49Z alex $ $HeadURL: svn://svn.lab.vmops.com/repos/vmdev/java/scripts/network/domr/ipassoc.sh $
# ipassoc.sh -- associate/disassociate a public ip with an instance
# @VERSION@

source /root/func.sh

lock="biglock"
locked=$(getLockFile $lock)
if [ "$locked" != "1" ]
then
    exit 1
fi

usage() {
  printf "Usage:\n %s -A -l <public-ip-address> -c <dev> [-f] \n" $(basename $0) >&2
  printf " %s -D -l <public-ip-address> -c <dev> [-f] \n" $(basename $0) >&2
}

remove_routing() {
  local pubIp=$1
  logger -t cloud "$(basename $0):Remove routing $pubIp on interface $ethDev"
  local ipNoMask=$(echo $pubIp | awk -F'/' '{print $1}')
  local mask=$(echo $pubIp | awk -F'/' '{print $2}')
  local tableNo=$(echo $ethDev | awk -F'eth' '{print $2}')

  local tableName="Table_$ethDev"
  local ethMask=$(ip route list scope link dev $ethDev | awk '{print $1}')
  if [ "$ethMask" == "" ]
  then
# rules and routes will be deleted for the last ip of the interface.
     sudo ip rule delete fwmark $tableNo table $tableName
     sudo ip rule delete table $tableName
     sudo ip route flush  table $tableName 
     sudo ip route flush cache
     logger -t cloud "$(basename $0):Remove routing $pubIp - routes and rules deleted"
  fi
}

# copy eth0,eth1 and the current public interface
copy_routes_from_main() {
  local tableName=$1

#get the network masks from the main table
  local eth0Mask=$(ip route list scope link dev eth0 | awk '{print $1}')
  local eth1Mask=$(ip route list scope link dev eth1 | awk '{print $1}')
  local ethMask=$(ip route list scope link dev $ethDev  | awk '{print $1}')

# eth0,eth1 and other know routes will be skipped, so as main routing table will decide the route. This will be useful if the interface is down and up.  
  sudo ip route add throw $eth0Mask table $tableName proto static 
  sudo ip route add throw $eth1Mask table $tableName proto static 
  sudo ip route add throw $ethMask  table $tableName proto static 
  return 0;
}

ip_addr_add() {
  local dev="$1"
  local ip="$2"
}

add_routing() {
  local pubIp=$1
  logger -t cloud "$(basename $0):Add routing $pubIp on interface $ethDev"
  local ipNoMask=$(echo $1 | awk -F'/' '{print $1}')
  local mask=$(echo $1 | awk -F'/' '{print $2}')

  local tableName="Table_$ethDev"
  local tablePresent=$(grep $tableName /etc/iproute2/rt_tables)
  local tableNo=$(echo $ethDev | awk -F'eth' '{print $2}')
  if [ "$tablePresent" == "" ]
  then
     if [ "$tableNo" == ""] 
     then
       return 0;
     fi
     sudo echo "$tableNo $tableName" >> /etc/iproute2/rt_tables
  fi

  copy_routes_from_main $tableName
# NOTE: this  entry will be deleted if the interface is down without knowing to Management server, in that case all the outside traffic will be send through main routing table or it will be the first public NIC.
  sudo ip route add default via $defaultGwIP table $tableName proto static
  sudo ip route flush cache

  local ethMask=$(ip route list scope link dev $ethDev  | awk '{print $1}')
  local rulePresent=$(ip rule show | grep $ethMask)
  if [ "$rulePresent" == "" ]
  then
# rules will be added while adding the first ip of the interface 
     sudo ip rule add from $ethMask table $tableName
     sudo ip rule add fwmark $tableNo table $tableName
     logger -t cloud "$(basename $0):Add routing $pubIp rules added"
  fi
  return 0;
}


add_an_ip () {
  local pubIp=$1
  logger -t cloud "$(basename $0):Adding ip $pubIp on interface $ethDev"
  local ipNoMask=$(echo $1 | awk -F'/' '{print $1}')
  sudo ip link show $ethDev | grep "state DOWN" > /dev/null
  local old_state=$?

  sudo ip addr add dev $dev $ip
  if [ $if_keep_state -ne 1 -o $old_state -ne 0 ]
  then
      sudo ip link set $ethDev up
      sudo arping -c 3 -I $ethDev -A -U -s $ipNoMask $ipNoMask;
  fi
  add_routing $1 
  return $?
   
}

remove_an_ip () {
  local pubIp=$1
  logger -t cloud "$(basename $0):Removing ip $pubIp on interface $ethDev"
  local ipNoMask=$(echo $1 | awk -F'/' '{print $1}')
  local mask=$(echo $1 | awk -F'/' '{print $2}')
  local existingIpMask=$(sudo ip addr show dev $ethDev | grep inet | awk '{print $2}'  | grep -w $ipNoMask)
  [ "$existingIpMask" == "" ] && return 0
  remove_snat $1
  local existingMask=$(echo $existingIpMask | awk -F'/' '{print $2}')
  if [ "$existingMask" == "32" ] 
  then
    sudo ip addr del dev $ethDev $existingIpMask
    result=$?
  fi

  if [ "$existingMask" != "32" ] 
  then
        replaceIpMask=`sudo ip addr show dev $ethDev | grep inet | grep -v $existingIpMask | awk '{print $2}' | sort -t/ -k2 -n|tail -1`
        sudo ip addr del dev $ethDev $existingIpMask;
        if [ -n "$replaceIpMask" ]; then
          sudo ip addr del dev $ethDev $replaceIpMask;
          replaceIp=`echo $replaceIpMask | awk -F/ '{print $1}'`;
          ip_addr_add $ethDev $replaceIp/$existingMask
        fi
    result=$?
  fi

  if [ $result -gt 0  -a $result -ne 2 ]
  then
     remove_routing $1
     return 1
  fi
  remove_routing $1
  return 0
}

#set -x
lflag=
cflag=
op=""


while getopts 'sfADa:l:c:g:' OPTION
do
  case $OPTION in
  A)	Aflag=1
		op="-A"
		;;
  D)	Dflag=1
		op="-D"
		;;
  l)	lflag=1
		publicIp="$OPTARG"
		;;
  c)	cflag=1
  		ethDev="$OPTARG"
  		;;
  g)	gflag=1
  		defaultGwIP="$OPTARG"
  		;;
  ?)	usage
                unlock_exit 2 $lock $locked
		;;
  esac
done


if [ "$Aflag$Dflag" != "1" ]
then
    usage
    unlock_exit 2 $lock $locked
fi

if [ "$lflag$cflag" != "11" ] 
then
    usage
    unlock_exit 2 $lock $locked
fi


if [ "$Aflag" == "1" ]
then
  add_an_ip  $publicIp  &&
  unlock_exit $? $lock $locked
fi


if [ "$Dflag" == "1" ]
then
  remove_an_ip  $publicIp &&
  unlock_exit $? $lock $locked
fi


