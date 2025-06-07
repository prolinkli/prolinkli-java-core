@echo off
REM ProLinkLi Java Core Framework - Clear Liquibase Checksums (Windows)

echo Clearing Liquibase checksums...
echo.

REM Check if Maven Wrapper exists, otherwise use Maven
if exist "mvnw.cmd" (
    set MAVEN_CMD=mvnw.cmd
) else (
    set MAVEN_CMD=mvn
)

%MAVEN_CMD% liquibase:clearCheckSums ^
  -Dliquibase.url=jdbc:postgresql://localhost:6543/postgres ^
  -Dliquibase.username=postgres ^
  -Dliquibase.password=docker ^
  -Dliquibase.driver=org.postgresql.Driver

if %errorlevel% equ 0 (
    echo.
    echo SUCCESS: Liquibase checksums cleared successfully!
) else (
    echo.
    echo ERROR: Failed to clear Liquibase checksums
    echo Please ensure the database is running
) 