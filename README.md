# prolinkli-java-core
Java Core Framework

## Overview

This project is a Java core framework built with [Spring Boot](https://spring.io/projects/spring-boot), [Maven](https://maven.apache.org/), and uses [Docker](https://www.docker.com/) for local development and database management.

## Prerequisites

- Java 17+ (JDK)
- [Maven](https://maven.apache.org/) (or use the Maven Wrapper: `./mvnw`)
- [Docker](https://www.docker.com/) and Docker Compose

## Getting Started

### 1. Clone the Repository

```sh
git clone https://github.com/prolinkli/prolinkli-java-core
``` 
cd prolinkli-java-core
```

### 2. Start the Database

Start the database using Docker Compose:

```sh
./start-db.sh
```

This will spin up the required database containers in the background.

### 3. Build the Project

Build the project using Maven:

```sh
./mvnw clean install
```

Or, if Maven is installed globally:

```sh
mvn clean install
```

### 4. Start the Development Server

Start the Spring Boot application in development mode:

```sh
./start-dev.sh
```

This will run the application with the `dev` profile and appropriate JVM arguments.

### 5. Accessing the Application

- The application will be available at: [http://localhost:8080/v1/api](http://localhost:8080/v1/api)
- Build info endpoint: [http://localhost:8080/v1/api/buildinfo](http://localhost:8080/buildinfo/info)

## Useful Commands

- **Run tests:**  
  ```sh
  ./mvnw test
  ```
- **Stop database containers:**  
  ```sh
  docker compose -f docker-compose.yaml down
  ```




## Troubleshooting and Some Common Questions
---

### 1. How to add a new sql script? (table, insert, drop, etc.)

To add a new SQL script, place your SQL script within the `src/main/resources/liquibase/changelog` directory. The script should follow the naming convention `YYYYMMDD.XX_description.sql`, where the prefix is a timestamp and the suffix describes the change.

Such a script could look like this:
```sql
--liquibase formatted sql
--changeset kerdogan:example_table_creation

CREATE TABLE example_table (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

```

It's important to ensure that you leave your name/signature in the `changeset` line, as this is used by Liquibase to track changes. 

#### Common Problem: Mismatch Checksums

If you encounter a `mismatched checksum` error when running the application, it means that the Liquibase change log has been modified after it was initially applied. To resolve this, you can either:

1. **Clear the checksums**: Run the following command to clear the checksums in the database:
```bash
./run-liquibase.sh
```

2. Clear the `DATABASECHANGELOG` table in your database. This will reset the change log history, allowing Liquibase to reapply all changes.

3. Wipe your local database and start fresh. This is a more drastic measure but can be useful if you are consistently working on the same sql scripts and want to ensure the server runs it every time.

> Wiping the checksums **WILL** not rerun the SQL scripts that have already been applied. It will only clear the checksums, allowing you to modify existing scripts without Liquibase throwing a checksum error. 





#### Reference

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/)
- [Maven Documentation](https://maven.apache.org/guides/index.html)
- [Docker Documentation](https://docs.docker.com/)


