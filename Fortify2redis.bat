@echo off
REM ###########################################################################
REM Script generated by Fortify SCA Scan Wizard (c) 2011-2020 Micro Focus or one of its affiliates
REM Created on 2020/11/06 15:25:30
REM ###########################################################################
REM Generated for the following languages:
REM 	Java
REM 	Java Bytecode
REM 	XML
REM ###########################################################################
REM DEBUG - if set to true, runs SCA in debug mode
REM SOURCEANALYZER - the name of the SCA executable
REM FPR - the name of analysis result file
REM BUILDID - the SCA build id
REM ARGFILE - the name of the argument file that's extracted and passed to SCA
REM BYTECODE_ARGFILE - the name of the argument file for Java Bytecode translation that's extracted and passed to SCA
REM MEMORY - the memory settings for SCA
REM SCANSWITCHES - parameters to be passed to the analysis phase of SCA
REM LAUNCHERSWITCHES - the launcher settings that are used to invoke SCA
REM OLDFILENUMBER - this defines the file which contains the number of files within the project, it is automatically generated
REM FILENOMAXDIFF - this is the percentage of difference between the number of files which will trigger a warning by the script
REM ###########################################################################

set DEBUG=false
set SOURCEANALYZER=sourceanalyzer
set FPR="Fortify2redis.fpr"
set BUILDID="2redis"
set ARGFILE="Fortify2redis.bat.args"
set BYTECODE_ARGFILE="Fortify2redis.bat.bytecode.args"
set MEMORY=-Xmx14661M -Xms400M -Xss24M 
set LAUNCHERSWITCHES=""
set SCANSWITCHES=""
set OLDFILENUMBER="Fortify2redis.bat.fileno"
set FILENOMAXDIFF=10
set ENABLE_BYTECODE=false

set PROJECTROOT0="D:\github\2redis"
IF NOT EXIST %PROJECTROOT0% (
   ECHO  ERROR: This script is being run on a different machine than it was
   ECHO         generated on or the targeted project has been moved. This script is 
   ECHO         configured to locate files at
   ECHO            %PROJECTROOT0%
   ECHO         Please modify the %%PROJECTROOT0%% variable found
   ECHO         at the top of this script to point to the corresponding directory
   ECHO         located on this machine.
   GOTO :FINISHED
)

IF %DEBUG%==true set LAUNCHERSWITCHES=-debug %LAUNCHERSWITCHES%
echo Extracting Arguments File


echo. >%ARGFILE%
echo. >%BYTECODE_ARGFILE%
SETLOCAL ENABLEDELAYEDEXPANSION
IF EXIST %0 (
   set SCAScriptFile=%0
) ELSE (
  set SCAScriptFile=%0.bat
)

set PROJECTROOT0=%PROJECTROOT0:)=^)%
FOR /f "delims=" %%a IN ('findstr /B /C:"REM ARGS" %SCAScriptFile%' ) DO (
   set argVal=%%a
   set argVal=!argVal:PROJECTROOT0_MARKER=%PROJECTROOT0:~1,-1%!
   echo !argVal:~9! >> %ARGFILE%
)
set PROJECTROOT0=%PROJECTROOT0:)=^)%
FOR /f "delims=" %%a IN ('findstr /B /C:"REM BYTECODE_ARGS" %SCAScriptFile%' ) DO (
   set ENABLE_BYTECODE=true
   set argVal=%%a
   set argVal=!argVal:PROJECTROOT0_MARKER=%PROJECTROOT0:~1,-1%!
   echo !argVal:~18! >> %BYTECODE_ARGFILE%
)
ENDLOCAL && set ENABLE_BYTECODE=%ENABLE_BYTECODE%

REM ###########################################################################
echo Cleaning previous scan artifacts
%SOURCEANALYZER% %MEMORY% %LAUNCHERSWITCHES% -b %BUILDID% -clean 
IF %ERRORLEVEL% NEQ 0  (
echo sourceanalyzer failed, exiting
GOTO :FINISHED
)
REM ###########################################################################
echo Translating files
%SOURCEANALYZER% %MEMORY% %LAUNCHERSWITCHES% -b %BUILDID% @%ARGFILE%
IF %ERRORLEVEL% NEQ 0  (
echo sourceanalyzer failed, exiting
GOTO :FINISHED
)
REM ###########################################################################
IF %ENABLE_BYTECODE%==true (
echo Translating Java bytecode files
%SOURCEANALYZER% %MEMORY% %LAUNCHERSWITCHES% -b %BUILDID% @%BYTECODE_ARGFILE%
IF %ERRORLEVEL% NEQ 0  (
echo sourceanalyzer failed, exiting
GOTO :FINISHED
)
)
REM ###########################################################################
echo Testing Difference between Translations
SETLOCAL
FOR /F "delims=" %%A in ('%SOURCEANALYZER% -b %BUILDID% -show-files ^| findstr /R /N "^" ^| find /C ":" ') DO SET FILENUMBER=%%A
IF NOT EXIST %OLDFILENUMBER% (
	ECHO It appears to be the first time running this script, setting %OLDFILENUMBER% to %FILENUMBER%
	ECHO %FILENUMBER% > %OLDFILENUMBER%
	GOTO TESTENDED
)

FOR /F "delims=" %%i IN (%OLDFILENUMBER%) DO SET OLDFILENO=%%i
set /a DIFF=%OLDFILENO% * %FILENOMAXDIFF%
set /a DIFF /=  100
set /a MAX=%OLDFILENO% + %DIFF%
set /a MIN=%OLDFILENO% - %DIFF%

IF %FILENUMBER% LSS %MIN% set SHOWWARNING=true
IF %FILENUMBER% GTR %MAX% set SHOWWARNING=true

IF DEFINED SHOWWARNING (
	ECHO WARNING: The number of files has changed by over %FILENOMAXDIFF%%%, it is recommended 
	ECHO          that this script is regenerated with the ScanWizard
)
:TESTENDED
ENDLOCAL

REM ###########################################################################
echo Starting scan
%SOURCEANALYZER% %MEMORY% %LAUNCHERSWITCHES% -b %BUILDID% %SCANSWITCHES% -scan -f %FPR%
IF %ERRORLEVEL% NEQ 0  (
echo sourceanalyzer failed, exiting
GOTO :FINISHED
)
REM ###########################################################################
echo Finished
:FINISHED
REM ARGS "-cp"
REM ARGS "PROJECTROOT0_MARKER\target\classes;PROJECTROOT0_MARKER\target\2redis-0.1-jar-with-dependencies.jar;PROJECTROOT0_MARKER\target\2redis-0.1.jar"
REM ARGS "-source"
REM ARGS "1.8"
REM ARGS "-exclude" "PROJECTROOT0_MARKER\**\*.jar"
REM ARGS "-exclude" "PROJECTROOT0_MARKER\**\*.class"
REM ARGS "PROJECTROOT0_MARKER"
