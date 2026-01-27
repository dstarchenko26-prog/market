@echo off
chcp 65001 >nul
color 0A

echo ===================================================
echo   ЗАПУСК БАЗИ ДАНИХ POSTGRESQL (DOCKER)
echo ===================================================

:: 1. Перевірка, чи запущений Docker Desktop
docker info >nul 2>&1
IF %ERRORLEVEL% NEQ 0 (
    color 0C
    echo.
    echo [ПОМИЛКА] Docker не запущений!
    echo Будь ласка, спочатку відкрий Docker Desktop.
    echo.
    pause
    exit /b
)

:: 2. Запуск тільки сервісу бази даних (db) у фоні (-d)
echo.
echo Запускаю контейнер...
docker-compose up -d db

:: 3. Перевірка результату
IF %ERRORLEVEL% EQU 0 (
    echo.
    echo [УСПІХ] База даних успішно запущена на порту 5432!
    echo.
) ELSE (
    color 0C
    echo.
    echo [ПОМИЛКА] Щось пішло не так при запуску контейнера.
)

echo.
pause