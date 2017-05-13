@echo off
set class=webService.ReaderBenchServer
if not "%1"=="" set class=%1
@echo on
mvn -Dexec.args="-Xss1g -Xmx6g -classpath %%classpath %class%" -Dexec.executable=java -Dexec.classpathScope=runtime -Dexec.longClasspath=true -Dfile.encoding=UTF-8 org.codehaus.mojo:exec-maven-plugin:1.5.0:exec
