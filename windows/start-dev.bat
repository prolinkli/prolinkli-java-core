@echo off
REM ProLinkLi Java Core Framework - Start Development Server (Windows)

echo Starting development server...
echo.

REM Check if Maven Wrapper exists, otherwise use Maven
if exist "mvnw.cmd" (
    set MAVEN_CMD=mvnw.cmd
) else (
    set MAVEN_CMD=mvn
)

%MAVEN_CMD% spring-boot:run ^
  -Dspring-boot.run.profiles=dev ^
  -Dspring-boot.run.jvmArguments="-Xmx1024m -Xms512m -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" ^
  -Dlogging.level.liquibase=DEBUG 