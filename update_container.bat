@echo off
chcp 65001 >nul
color 0B
title Update Market Backend

echo ===================================================
echo   ОНОВЛЕННЯ БЕКЕНДУ (DOCKER BUILD & RESTART)
echo ===================================================

:: 1. Перевірка чи запущений Docker
docker info >nul 2>&1
IF %ERRORLEVEL% NEQ 0 (
    color 0C
    echo.
    echo [ПОМИЛКА] Docker Desktop не запущений!
    echo Будь ласка, запустіть Docker.
    pause
    exit /b
)

echo.
echo [1/2] Перезбірка образу та оновлення контейнера...
echo Це може зайняти хвилину-дві, бо Maven качає залежності всередині контейнера...
echo.

docker-compose up -d --build backend

IF %ERRORLEVEL% NEQ 0 (
    color 0C
    echo.
    echo [ПОМИЛКА] Збірка не вдалася. Перевірте код на помилки.
    pause
    exit /b
)

:: 2. Очищення старих непотрібних образів (щоб диск не забивався)
echo.
echo [2/2] Очищення кешу старих образів...
docker image prune -f >nul 2>&1

color 0A
echo.
echo ===================================================
echo   ГОТОВО! БЕКЕНД ОНОВЛЕНО.
echo ===================================================
echo   Контейнер: market-backend
echo   Порт: 8080
echo.
echo   Щоб глянути логи, введи: docker logs -f market-backend
echo.
pause