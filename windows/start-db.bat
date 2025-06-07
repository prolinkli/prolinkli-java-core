@echo off
REM ProLinkLi Java Core Framework - Start Database (Windows)

echo Starting database server...
echo.

docker compose -f docker-compose.yaml up -d

if %errorlevel% equ 0 (
    echo.
    echo SUCCESS: Database started successfully!
    echo PostgreSQL is running on localhost:6543
    echo Username: postgres
    echo Password: docker
) else (
    echo.
    echo ERROR: Failed to start database
    echo Please ensure Docker Desktop is running
) 