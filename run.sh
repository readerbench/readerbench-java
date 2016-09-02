if [ -z $JAVA_HOME ];
then
	echo "JAVA_HOME is not set!";
else
	if [ -z $M2_HOME ];
	then
		echo "M2_HOME is not set! Maven might not be installed on your computer!";
	else
		if [ -z $(which mvn) ]
		then
			echo "mvn is not installed";
		else
			class=${1-webService.ReaderBenchServer}
			set -x
			mvn -Dexec.args="-Xss1g -Xmx4g -classpath %classpath $class" -Dexec.executable=java -Dexec.classpathScope=runtime -Dexec.longClasspath=true -Dfile.encoding=UTF-8 org.codehaus.mojo:exec-maven-plugin:1.5.0:exec
		fi
	fi
fi