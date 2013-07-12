@echo off
setlocal

set UID=0x10205DB2
set NAME=jUninstaller
set CREATOR=Markus Birth
set CLASS=JUninstaller
set VER=6,4,5
:VER is following format: Major,Minor,Build (don't forget the commas)

:###########################################################################
:#### PROGRAM CODE FOLLOWING --- DO NOT CHANGE ANYTHING BELOW THIS LINE ####
:###########################################################################


IF NOT EXIST files\NUL mkdir files
IF NOT EXIST release\NUL mkdir release
IF NOT EXIST icon\20x16.bmp GOTO noicon
IF NOT EXIST icon\20x16m.bmp GOTO noicon
IF NOT EXIST icon\32x32.bmp GOTO noicon
IF NOT EXIST icon\32x32m.bmp GOTO noicon

echo Deleting old stuff.
del /Q files\*.*
del /Q release\*.*
IF EXIST config\genaif.cfg del /Q config\genaif.cfg
IF EXIST config\icon.mbm del /Q config\icon.mbm
IF EXIST config\manifest.mf del /Q config\manifest.mf
:IF EXIST config\makesis.pkg del /Q config\makesis.pkg

echo Building MBM.
config\bmconv /Q config\icon.mbm /c24icon\20x16.bmp /1icon\20x16m.bmp /c24icon\32x32.bmp /1icon\32x32m.bmp

echo Building genaif.cfg.
:USING UNIX-ECHO BECAUSE IT DOESN'T OUTPUT TRAILING SPACES.
:WINDOWS ECHO DOESN'T WORK WITH 0>> OR 1>> AND TRAILING SPACES ARE NOT NICE.
config\echo mbmfile=config\icon.mbm >config\genaif.cfg
config\echo ELangEnglish=%NAME% >>config\genaif.cfg
config\echo hidden=0 >>config\genaif.cfg
config\echo embeddability=0 >>config\genaif.cfg
config\echo newfile=1 >>config\genaif.cfg

echo Building AIF.
config\genaif %UID% <config\genaif.cfg >files\%NAME%.aif

echo Building APP.
config\genaif 0x00000000 0x10001734 %UID% >files\%NAME%.app

echo Building TXT.
config\echo -n -cp %NAME%.jar %CLASS%>files\%NAME%.txt

echo Building MANIFEST.
echo Manifest-Version: 1.0>config\manifest.mf
echo Created-By: 0.92-gcc (%CREATOR%)>>config\manifest.mf
echo Main-Class: %CLASS%>>config\manifest.mf

echo Building JAR.
config\jar cfm files\%NAME%.jar config\manifest.mf *.class

GOTO skipthis
echo Building SIS.
echo ^&EN>config\makesis.pkg
echo #{"%NAME%"},(%UID%),%VER%,TYPE=SISAPP>>config\makesis.pkg
echo (0x101F617B),2,0,0,{"UIQ20ProductID"}>>config\makesis.pkg
cd files
FOR %%f IN (*.*) DO echo "files\%%f"-"!:\System\Apps\%NAME%\%%f">>..\config\makesis.pkg
cd ..
dir /B /S /AA add>config\addfiles.lst
:skipthis
config\makesis config\makesis.pkg release\%NAME%.sis

echo Cleaning up.
:del /Q config\genaif.cfg
:del /Q config\icon.mbm
:del /Q config\manifest.mf
:del /Q config\makesis.pkg

goto end

:noicon
echo There are no icon BMPs in .\icon or at least one file is missing!!!
echo Please create following icons with the specified names:
echo.
echo 20x16.bmp	Small icon with 24bpp
echo 20x16m.bmp	Transparency mask for small icon with 1bpp
echo 32x32.bmp	Large icon with 24bpp
echo 32x32m.bmp	Transparency mask for large icon with 1bpp
echo.
echo Transparency: black=opaque, white=transparent
goto end

:end
endlocal
echo.
echo All done.
