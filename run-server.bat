@echo off
chcp 65001 >nul
color 0B
title Start Market Docker Environment

echo ===================================================
echo   ЗАПУСК DOCKER СЕРЕДОВИЩА (DB + BACKEND)
echo ===================================================

:: 1. Перевірка чи Docker запущений
docker info >nul 2>&1
IF %ERRORLEVEL% NEQ 0 (
    color 0C
    echo.
    echo [ПОМИЛКА] Docker Desktop не запущений!
    echo Будь ласка, запустіть Docker і спробуйте знову.
    echo.
    pause
    exit /b
)

:: 2. Запуск контейнерів
echo.
echo Запускаю контейнери...
echo.

:: up - підняти контейнери
:: -d - у фоновому режимі (detached)
:: --build - (опціонально) перезібрати, якщо немає образу, але краще це робити окремим скриптом оновлення
docker-compose up -d

IF %ERRORLEVEL% EQU 0 (
    color 0A
    echo.
    echo ===================================================
    echo   УСПІШНО!
    echo ===================================================
    echo.
    echo   Backend:  http://localhost:8080
    echo.
    echo   Щоб зупинити все, напиши в терміналі: docker-compose down
) ELSE (
    color 0C
    echo.
    echo [ПОМИЛКА] Щось пішло не так. Перевірте логи.
)

echo.
pause