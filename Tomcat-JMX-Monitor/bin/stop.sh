#!/bin/bash

#java -jar JMXMonitor.jar &

pid=`ps -ef | grep -i 'JMXMonitor.jar' | grep -v grep | awk '{print $2} ' `
if test "$pid" ;  then
 kill $pid
 echo Monitor is killed
else
 echo not found Monittor
fi

