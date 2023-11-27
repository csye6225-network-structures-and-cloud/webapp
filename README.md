# Webapp - RESTful API
- This project provides a RESTful API for managing user assignments built using Spring Boot.

## Prerequisites test
- Java 17: Ensure Java Development Kit (JDK) 17 is installed.
- Maven: This project uses the Maven build system.
- PostgreSQL: A running instance is required for bootstrapping and integration tests.


## Setup & Installation
### Database Configuration
- Database (PostgreSQL) is configured and instance up and running.
- Configured the application.properties file with the appropriate database credentials.
- The application will auto-bootstrap the database schema upon startup.
  
### Loading User Data
- User data will be loaded in a CSV format.
- CSV file is in the location /opt/user.csv.
- When the application starts, it will automatically pick up this file, load the users, and store their hashed passwords in the database.

### Running the Application
- mvn clean install

### Integration Testing
- Tests have been implemented for the /healtz endpoint. These tests are run against real MySQL and PostgreSQL instances in the CI environment.

### Continuous Integration
- Integrated GitHub Actions for CI/CD, which runs the integration tests for each PR.
- Branch protection rules are added. So, a PR can't be merged unless the GitHub Actions workflow gives is successful.

## Digital Ocean
-  Application will be showcased from a Debian 12 VM hosted in Digital Ocean
-  A setup.sh script is created to setup the environment (Java Maven Postgressql) for Debian VM
-  A seperate config property file is created to store the db credentials out of the web application
-  The project zip folder. users.csv, db.properties is scp to the /opt folder in debian
-  A start.sh Script is created to unzip and run the jar file in the project

## Necessary Commands used and required

- sudo -i -u postgres
- psql
- ALTER USER postgres WITH PASSWORD 'postgres';
- CREATE DATABASE userdata;
- GRANT ALL PRIVILEGES ON DATABASE userdata TO postgres;
- \q -  to quit psql
- sudo systemctl restart postgresql

- scp "C:\Users\valla\Documents\cloudapplication.zip" root@143.110.158.115:/opt/
- scp "C:\Users\valla\Downloads\user.csv" root@143.110.158.115:/opt/
- scp "C:\config\db.properties" root@143.110.158.115:/opt/

- sudo rm -rf /opt/Cloud/ -  to delete existing unzipped folder
- sudo rm -rf /Cloudapplication.zip

- psql -U superuser_name -d database_name
- psql -U postgres -d userdata -  to enter the db 
- \l - to list databases
- \dt - to list tables
- \c  userdata - to connect to db
- \dp userdata - to see the permissions
- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO userdata;
- DROP table userdata CASCADE;
- DROP table assignmentdata CASCADE

### AMI creation using Packer 
### Systemd creation and RDS working


###test






