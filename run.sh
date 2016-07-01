class=${1-webService.ReaderBenchServer}
set -x
mvn -Dexec.args="-Xss1g -Xmx4g -classpath %classpath $class" -Dexec.executable=java -Dexec.classpathScope=runtime -Dexec.longClasspath=true -Dfile.encoding=UTF-8 org.codehaus.mojo:exec-maven-plugin:1.5.0:exec
