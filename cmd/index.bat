@echo off
set basePath=%cd%\repository

IF "%~1" == "" GOTO Help
set input=%*
set randomPath=%TEMP%\%RANDOM%%RANDOM%

:Main

java ^
-Dlog.level=ALL ^
%input% ^
-noverify ^
-jar %basePath%\plugins\org.eclipse.equinox.launcher_1.5.500.v20190715-1310.jar ^
-configuration %basePath%\config_win ^
-data %randomPath%

rmdir /s /q %randomPath%

goto End

:Help
@echo Parameters:
@echo.   -Drepo.path=[path of repo]
@echo.
@echo.   -Doutput.format=[format]
@echo.      Supported values: line, json. Default: line
@echo.
@echo Example:
@echo.   index.bat "-Drepo.path=D:\Workspace\spring-petclinic"
@echo.
@echo.   index.bat "-Drepo.path=D:\Workspace\spring-petclinic" "-Doutput.format=json"

:End
