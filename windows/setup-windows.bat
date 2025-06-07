@echo off
REM ProLinkLi Java Core Framework - Windows Setup Script
echo =====================================
echo ProLinkLi Java Core Framework Setup
echo =====================================
echo.

REM Check if Java is installed
echo [1/5] Checking Java installation...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17+ from https://adoptium.net/
    echo.
    pause
    exit /b 1
) else (
    echo SUCCESS: Java is installed
)
echo.

REM Check if Docker is running
echo [2/5] Checking Docker installation...
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Docker is not installed or not running
    echo Please install Docker Desktop from https://www.docker.com/products/docker-desktop/
    echo.
    pause
    exit /b 1
) else (
    echo SUCCESS: Docker is available
)
echo.

REM Check if Maven is available (wrapper or installed)
echo [3/5] Checking Maven...
if exist "mvnw.cmd" (
    echo SUCCESS: Maven Wrapper found
    set MAVEN_CMD=mvnw.cmd
) else (
    mvn -version >nul 2>&1
    if %errorlevel% neq 0 (
        echo ERROR: Maven not found and no Maven Wrapper available
        echo Please ensure mvnw.cmd exists or install Maven
        pause
        exit /b 1
    ) else (
        echo SUCCESS: Maven is installed
        set MAVEN_CMD=mvn
    )
)
echo.

REM Start database
echo [4/5] Starting database...
docker compose -f docker-compose.yaml up -d
if %errorlevel% neq 0 (
    echo ERROR: Failed to start database
    echo Please check Docker Desktop is running
    pause
    exit /b 1
) else (
    echo SUCCESS: Database started
)
echo.

REM Build project
echo [5/5] Building project...
%MAVEN_CMD% clean install
if %errorlevel% neq 0 (
    echo ERROR: Failed to build project
    pause
    exit /b 1
) else (
    echo SUCCESS: Project built successfully
)
echo.

echo =====================================
echo Setup completed successfully!
echo =====================================
echo.
echo Next steps:
echo 1. Run: start-dev.bat (or %MAVEN_CMD% spring-boot:run -Dspring-boot.run.profiles=dev)
echo 2. Open: http://localhost:8080/v1/api
echo.
echo Available commands:
echo - start-dev.bat         : Start development server
echo - run-mybatis.bat       : Generate MyBatis code
echo - run-liquibase.bat     : Clear Liquibase checksums
echo.
pause 