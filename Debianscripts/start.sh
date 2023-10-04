#!/bin/bash

# Navigate to the directory
cd /opt/

if [ -d "Cloud" ]; then
    echo "Removing old Cloud directory..."
    rm -rf Cloud
fi

# Unzip the application
echo "Unzipping cloudapplication.zip..."
unzip supriya_vallarapu_002726718_03.zip

ls

# Navigate to the target directory
cd Cloud/webapp/webapplication/target/

# Uncomment the next line if you need to compile and package using Maven on your Debian server
# mvn clean package

# Run the Spring Boot application with the production profile
java -jar webapplication-0.0.1-SNAPSHOT.jar --spring.profiles.active=production

