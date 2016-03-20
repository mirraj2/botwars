#!/bin/sh

screen -S botwars -X quit
screen -S botwars -d -m ant -f /root/botwars/build.xml BotWarsServer
echo Server started.
