@echo off
REM ProLinkLi Java Core Framework - MyBatis Generator (Windows)

echo =====================================
echo MyBatis Generator Process
echo =====================================
echo Using database: jdbc:postgresql://localhost:6543/postgres
echo.

REM JDBC Configuration
set JDBC_DRIVER=org.postgresql.Driver
set JDBC_URL=jdbc:postgresql://localhost:6543/postgres
set JDBC_USER=postgres
set JDBC_PASSWORD=docker
set JDBC_PARAMS=-Djdbc.driverClass=%JDBC_DRIVER% -Djdbc.url=%JDBC_URL% -Djdbc.userId=%JDBC_USER% -Djdbc.password=%JDBC_PASSWORD%

REM Check if Maven Wrapper exists, otherwise use Maven
if exist "mvnw.cmd" (
    set MAVEN_CMD=mvnw.cmd
) else (
    set MAVEN_CMD=mvn
)

REM Step 1: Clean and compile the project
echo [1/4] Cleaning and compiling project...
%MAVEN_CMD% clean compile %JDBC_PARAMS%
if %errorlevel% neq 0 (
    echo ERROR: Failed to compile project
    pause
    exit /b 1
) else (
    echo SUCCESS: Project compiled successfully
)
echo.

REM Step 2: Package the project (creates original JAR)
echo [2/4] Packaging project to create JAR files...
%MAVEN_CMD% package -DskipTests %JDBC_PARAMS%
if %errorlevel% neq 0 (
    echo ERROR: Failed to package project
    pause
    exit /b 1
) else (
    echo SUCCESS: Project packaged successfully
)
echo.

REM Step 3: Install original JAR to local repository
echo [3/4] Installing original JAR to local Maven repository...
%MAVEN_CMD% install:install-file ^
    -Dfile=target\framework-0.0.1-SNAPSHOT.jar.original ^
    -DgroupId=com.prolinkli ^
    -DartifactId=framework ^
    -Dversion=0.0.1-SNAPSHOT ^
    -Dclassifier=original ^
    -Dpackaging=jar ^
    %JDBC_PARAMS%
if %errorlevel% neq 0 (
    echo ERROR: Failed to install original JAR
    pause
    exit /b 1
) else (
    echo SUCCESS: Original JAR installed to local repository
)
echo.

REM Step 4: Run MyBatis Generator
echo [4/4] Running MyBatis Generator...
%MAVEN_CMD% mybatis-generator:generate %JDBC_PARAMS%
if %errorlevel% neq 0 (
    echo ERROR: MyBatis Generator failed
    pause
    exit /b 1
) else (
    echo SUCCESS: MyBatis Generator completed successfully!
    echo.
    echo Generated files:
    echo   - Model classes in: src\main\java\com\prolinkli\core\app\db\model\generated\
    echo   - Mapper classes in: src\main\java\com\prolinkli\core\app\db\mapper\generated\
    echo   - XML mappers in: src\main\resources\mapper\generated\
)
echo.

echo =====================================
echo MyBatis generation process completed!
echo =====================================
echo You can now use the generated classes in your application.
echo.
pause 