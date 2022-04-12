#!/bin/bash

#iterate over all other lib
submissionvl_home=`pwd`
CLIENTLIBS=`ls -R1 ${submissionvl_home}/lib/*.jar`

for i in ${CLIENTLIBS}; do
   if [ "${CLIENTLIBS}" ] ; then
      LIBPATH=${LIBPATH}:"$i"
   fi
done

#also add conf folder to have log4j config files available
LIBPATH=${LIBPATH}:${submissionvl_home}/conf/

#start java application with correct class path and 'config.properties' location as argument
java -cp "${submissionvl_home}/bin$LIBPATH" -Xms128m -Xmx1024m com.exlibris.dps.submissionvl.AppStarter "config.test.properties" "log4j.properties" "7777"
#server environment
#/opt/exlibris/product/java/bin/java -cp "${submissionvl_home}/bin$LIBPATH" -Xms128m -Xmx1024m com.exlibris.dps.submissionvl.AppStarter "config.test.properties" "log4j.properties" "7777"

exit
