@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"
chcp 65001 >nul
set "JAVAC=javac"
set "JAVA=java"
if defined JAVA_HOME (
  set "JAVAC=%JAVA_HOME%\bin\javac.exe"
  set "JAVA=%JAVA_HOME%\bin\java.exe"
)
if not exist out mkdir out
(for /r src %%f in (*.java) do (
  set "SOURCE=%%f"
  echo "!SOURCE:\=/!"
)) > sources.txt
"%JAVAC%" -encoding UTF-8 -d out @sources.txt
if errorlevel 1 exit /b 1
"%JAVA%" -Dfile.encoding=UTF-8 -cp out ru.edu.pr05.SelfCheck 
exit /b %errorlevel%
