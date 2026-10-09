@echo off
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo ========================================================
echo Starting CSIMS (Construction Site Intelligence System)
echo ========================================================
java -jar target\construction-site-intelligence-1.0.0-jar-with-dependencies.jar
pause
