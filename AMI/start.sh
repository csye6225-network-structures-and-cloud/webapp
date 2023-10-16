#!/bin/bash

ls
# mvn clean package
sudo mv /home/admin/user.csv /opt/

sudo mv /home/admin/db.properties /opt/

# Alter the postgres user password
sudo -u postgres psql -c "ALTER USER postgres WITH PASSWORD 'postgres';"

# Create the userdata database
sudo -u postgres createdb userdata

# Grant all privileges on userdata to postgres
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE userdata TO postgres;"

# Run the Spring Boot application with the production profile
java -jar webapplication-0.0.1-SNAPSHOT.jar --spring.profiles.active=production

