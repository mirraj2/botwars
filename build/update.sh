#!/bin/bash

git -C /root/botwars pull &
git -C /root/Ox pull &
git -C /root/Bowser pull &
git -C /root/EZDB pull &
git -C /root/JavaWebsockets pull &

wait

ant -f /root/botwars/build.xml build

/root/botwars/build/restart.sh
