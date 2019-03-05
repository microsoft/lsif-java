@echo off
set basePath=%cd%\repository

IF "%~1" == "" GOTO Help
set input=%~1

:Main
@echo Base dir is located in: %basePath% 
@echo Repo dir is located in: %input% 

java ^
-Dlog.level=ALL ^
-Dintellinav.repo.path=%input% ^
-noverify ^
-jar %basePath%\plugins\org.eclipse.equinox.launcher_1.5.200.v20180922-1751.jar ^
-configuration %basePath%\config_win
goto End

:Help
@echo Usage:
@echo index.bat [dir of repo]
:End
