#!/bin/sh

echo "Downloading BotWars database"
ssh -C root@botwars.online mysqldump -u root botwars | mysql -u root -D botwars
echo "Done"



