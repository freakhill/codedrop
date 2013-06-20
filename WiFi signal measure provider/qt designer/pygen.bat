@ECHO OFF
ECHO Generating Python for QtDesigner ui files
ECHO Processing wsmp.ui
c:\Python26\Lib\site-packages\PyQt4\pyuic4.bat wsmp.ui > wsmp.py
ECHO Done
