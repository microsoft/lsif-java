@echo off
set basePath=%cd%\repository

IF "%~1" == "" GOTO Help
set input=%*

:Main

java ^
-Dlog.level=ALL ^
%input% ^
-noverify ^
-jar %basePath%\plugins\org.eclipse.equinox.launcher_1.5.200.v20180922-1751.jar ^
-configuration %basePath%\config_win
goto End

:Help
@echo Parameters:
@echo.   -Dintellinav.repo.path=[path of repo]
@echo.
@echo.   -Dintellinav.output.format=[format]
@echo.      Supported values: line, json. Default: line
@echo.
@echo Example:
@echo.   index.bat "-Dintellinav.repo.path=D:\Workspace\spring-petclinic"
@echo.
@echo.   index.bat "-Dintellinav.repo.path=D:\Workspace\spring-petclinic" "-Dintellinav.output.format=json"

:End
