@echo off
setlocal EnableExtensions EnableDelayedExpansion
cd /d "%~dp0"
echo ==========================================================
echo  Bird Watching - PRIVATE Fabric 26.2 build verification
echo  Local contributor test only. Do not redistribute assets.
echo ==========================================================

if not exist "gradlew.bat" (
  echo ERROR: Run this from the checked-out project root.
  exit /b 2
)
where java >nul 2>&1
if errorlevel 1 (
  echo ERROR: Java 25 JDK is required and must be on PATH.
  exit /b 2
)
where jar >nul 2>&1
if errorlevel 1 (
  echo ERROR: A JDK with the jar utility is required.
  exit /b 2
)

echo.
echo Checking Java version:
java -version
echo.
echo Verifying isolated Minecraft 26.2 APIs ...
call "%~dp0gradlew.bat" verifyRegistriesPort --no-daemon --stacktrace
if errorlevel 1 (
  echo ERROR: Fabric 26.2 API checks failed. No test JAR is ready.
  exit /b 1
)

echo.
echo Running FULL source compilation, strict migration audit and build ...
call "%~dp0gradlew.bat" clean build --no-daemon --stacktrace
if errorlevel 1 (
  echo ERROR: Full Fabric 26.2 build failed. Do not use leftover JARs.
  exit /b 1
)

set "FOUND="
for %%J in ("%~dp0build\libs\*.jar") do (
  if exist "%%~fJ" (
    echo %%~nxJ | findstr /i /c:"-sources.jar" /c:"-dev.jar" >nul
    if errorlevel 1 (
      jar tf "%%~fJ" | findstr /x /c:"fabric.mod.json" >nul
      if not errorlevel 1 (
        echo.
        echo PRIVATE TEST JAR: %%~fJ
        set "FOUND=1"
      )
    )
  )
)

if not defined FOUND (
  echo ERROR: Gradle returned success but no remapped Fabric JAR was found.
  exit /b 1
)

echo.
echo This is not an approved release. Keep the JAR on this computer
echo until the original author explicitly permits asset redistribution.
exit /b 0
