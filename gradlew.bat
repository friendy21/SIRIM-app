@ECHO OFF

SET DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

SET DIR=%~dp0
IF EXIST "%DIR%\gradle\wrapper\gradle-wrapper.jar" GOTO execute

ECHO ERROR: gradle-wrapper.jar is missing.
EXIT /B 1

:execute
SET CLASSPATH=%DIR%\gradle\wrapper\gradle-wrapper.jar

"%JAVA_HOME%\bin\java.exe" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% -classpath %CLASSPATH% org.gradle.wrapper.GradleWrapperMain %*
