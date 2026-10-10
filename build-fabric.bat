@echo off
rem Builds the Fabric 1.21.1 port (needs JDK 21). The root gradlew is the old Forge 1.20.1 build, do not use it.
rem Usage: build-fabric.bat [tconstruct task, default: build]   e.g. build-fabric.bat runClient
setlocal
set TASK=%1
if "%TASK%"=="" set TASK=build
pushd "%~dp0mantle-fabric"
call gradlew.bat remapJar || (popd & exit /b 1)
popd
pushd "%~dp0tconstruct-fabric"
call gradlew.bat %TASK% || (popd & exit /b 1)
popd
