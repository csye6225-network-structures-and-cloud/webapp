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

echo "Installing htop and other dependencies"
sudo apt-get install -y htop

echo "Installing CloudWatch Agent"
sudo curl -o /home/admin/amazon-cloudwatch-agent.deb https://s3.amazonaws.com/amazoncloudwatch-agent/debian/amd64/latest/amazon-cloudwatch-agent.deb
sudo dpkg -i /home/admin/amazon-cloudwatch-agent.deb || sudo apt-get install -f

echo "Configuring CloudWatch Agent"
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
    -a fetch-config \
    -m ec2 \
    -c file:/home/admin/cloudwatch-config.json \
    -s

# Remove the downloaded package to clean up space
sudo rm /home/admin/amazon-cloudwatch-agent.deb
#echo "---x---Installing PostgreSQL---x---"
#sudo apt install postgresql postgresql-contrib -y
#
## Start and enable PostgreSQL to start on boot
#sudo systemctl start postgresql
#sudo systemctl enable postgresql


ls

sudo mv /home/admin/user.csv /opt/

sudo mv /home/admin/db.properties /opt/

sudo systemctl enable amazon-cloudwatch-agent


# Alter the postgres user password
#sudo -u postgres psql -c "ALTER USER postgres WITH PASSWORD 'postgres';"
#
## Create the userdata database
#sudo -u postgres createdb userdata
#
## Grant all privileges on userdata to postgres
#sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE userdata TO postgres;"


