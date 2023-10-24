#!/bin/bash

echo "---x---Adding Java Repository---x---"
sudo apt update && sudo apt upgrade -y

echo "---x---Installing Java 17---x---"
sudo apt install openjdk-17-jdk -y

echo "---x---Setting Java Environment Variables---x---"
echo "export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64/" >> ~/.bashrc
echo "export PATH=$PATH:$JAVA_HOME/bin" >> ~/.bashrc

# Refresh the current shell to recognize the changes made in .bashrc
source ~/.bashrc

echo "---x---Installing Maven---x---"
sudo apt install maven -y

#echo "---x---Installing PostgreSQL---x---"
#sudo apt install postgresql postgresql-contrib -y
#
## Start and enable PostgreSQL to start on boot
#sudo systemctl start postgresql
#sudo systemctl enable postgresql

ls

sudo mv /home/admin/user.csv /opt/

sudo mv /home/admin/db.properties /opt/

# Alter the postgres user password
#sudo -u postgres psql -c "ALTER USER postgres WITH PASSWORD 'postgres';"
#
## Create the userdata database
#sudo -u postgres createdb userdata
#
## Grant all privileges on userdata to postgres
#sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE userdata TO postgres;"


