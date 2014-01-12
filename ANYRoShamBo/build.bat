@echo off

set ARTIFACTS_DIR=%USERPROFILE%\DropBox\ANYRoShamBo\artifacts

call mvn clean package >"%ARTIFACTS_DIR%\build.log" 2>&1
copy "target\*.apk" "%ARTIFACTS_DIR%"

call mvn clean package -P ad >"%ARTIFACTS_DIR%\build-with-ads.log" 2>&1
copy "target\*.apk" "%ARTIFACTS_DIR%"