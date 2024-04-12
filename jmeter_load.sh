#!/bin/bash
#shell script to generate increasing load in steps and automatically scale down  
after error.
# vary load by changing values for initialThreads, step, loopsPerThread
#

cd /home/petclinic
# 0 is step up, non zero is step down
#
stepDirection=0
initialThreads=5
maxThreads=300
currentThreads=0
stepSize=5
rampupTime=30
loopsPerThread=5
requestsPerLoop=6

# delete results file from last run if any.
rm -f log.jtl

while :
do
        sudo rm -rf jmeter.log
        if [ $stepDirection == 0 ]
        then
                # Step Up
                currentThreads=$((currentThreads+stepSize))
        else
                # Step Down
                currentThreads=$((currentThreads-stepSize))
        fi

        if [ $currentThreads -le $initialThreads ]
        then
                currentThreads=$initialThreads
                stepDirection=0
        fi
        if [ $currentThreads -ge $maxThreads ]
        then
                currentThreads=$maxThreads
                stepDirection=1
        fi
        sh /opt/apache-jmeter-5.5/bin/jmeter.sh -n -t petclinic-jmeter.jmx -l log.jtl -Jthreads=${currentThreads} -Jrampuptime=${rampupTime} -Jloopsperthread=${loopsPerThread}
        # Decide number of response lines to check for error
                #
        responseLines=$((currentThreads*loopsPerThread*requestsPerLoop))
        #1594296081119,8,addOwner,302,,Thread Group 1-13,,true,,232,301,1,1,http://172.31.64.251/owners/new,8,0,0
        # Any error in response, then start step down
        errors=$(tail -n $responseLines log.jtl |  awk -F "," '{print $4}' | grep -c '5')

        # If there are more than 10% error, then start step down
        if [ $errors -ge $((responseLines / 6)) ]
        then
                stepDirection=1
        fi
        sleep 300
        /usr/sbin/logrotate -f /etc/logrotate.d/jmeter_logrotate

done