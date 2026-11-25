@echo off
chcp 65001 >nul
setlocal
rem =====================================================================
rem  TOPOHIT · Android · Instalar Python para compilar la app y las métricas
rem  - Chaquopy necesita Python 3.11 en el ordenador para compilar (mete pandas y matplotlib en la app).
rem    Es 3.11 porque la tablet de clase (Galaxy Tab A) es de 32 bits y Chaquopy solo tiene hasta 3.11 para 32 bits.
rem  - La carpeta metricas\ (playtest, simulación y análisis) usa pandas, numpy, matplotlib y scikit-learn.
rem  Doble clic en este archivo y esperad a que diga «Listo».
rem =====================================================================

set "REQ=%~dp0metricas\requirements.txt"

echo.
echo === 1. Comprobando Python 3.11 (lo usa Chaquopy al compilar) ===
py -3.11 --version >nul 2>&1
if errorlevel 1 (
    echo No está Python 3.11. Instalándolo con winget...
    winget install --id Python.Python.3.11 -e --accept-package-agreements --accept-source-agreements
    echo.
    echo Python 3.11 instalado. CERRAD esta ventana y volved a abrir el .bat.
    pause
    exit /b 1
)
py -3.11 --version

echo.
echo === 2. Instalando las librerías de metricas\requirements.txt ===
py -3.11 -m pip install --upgrade pip
py -3.11 -m pip install -r "%REQ%"
if errorlevel 1 (
    echo.
    echo ERROR: no se han podido instalar las librerías. Mirad el mensaje de arriba.
    pause
    exit /b 1
)

echo.
echo === 3. Comprobando que se cargan ===
py -3.11 -c "import pandas, numpy, matplotlib, sklearn; print('pandas', pandas.__version__, '- matplotlib', matplotlib.__version__, '- scikit-learn', sklearn.__version__)"
if errorlevel 1 (
    echo.
    echo ERROR: alguna librería no se carga. Mirad el mensaje de arriba.
    pause
    exit /b 1
)

echo.
echo Listo. Para ejecutar las métricas: cd metricas y py -3.11 03_analisis.py
echo Recordad también en Android Studio: Settings, Build Tools, Gradle, Gradle JDK = JDK 17 o 21.
pause
