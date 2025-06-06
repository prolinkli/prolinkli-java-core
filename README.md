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

- The application will be available at: [http://localhost:8080](http://localhost:8080)
- Build info endpoint: [http://localhost:8080/buildinfo/info](http://localhost:8080/buildinfo/info)

## Useful Commands

- **Run tests:**  
  ```sh
  ./mvnw test
  ```
- **Stop database containers:**  
  ```sh
  docker compose -f docker-compose.yaml down
  ```

## Reference

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/)
- [Maven Documentation](https://maven.apache.org/guides/index.html)
- [Docker Documentation](https://docs.docker.com/)


