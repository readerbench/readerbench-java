# ReaderBench

ReaderBench is a fully functional automated software framework, designed to provide support to learners and tutors for comprehension assessment and prediction in various educational scenarios. The system makes use of text-mining techniques based on advanced natural language processing and machine learning algorithms to deliver summative and formative assessments using multiple data sets (e.g., textual materials, behavior tracks, self-explanations).

## Aim

ReaderBench targets both tutors and students by providing an integrated learning model approach including individual and collaborative learning methods, cohesion-based discourse analysis, dialogical discourse model, textual complexity evaluation, reading strategies identification, and participation and collaboration assessment. By using natural language processing techniques, the main purpose of this framework is to bind traditional learning methods with new trends and technologies to support computer supported collaborative learning (CSCL). ReaderBench, by design, is not meant to replace the tutor, but to scaffold both tutors and learners towards continuous assessment, self-assessment, collaborative evaluation of individuals' contributions, as well as the analysis of reading materials to match readers to an appropriate level of text difficulty.

## Build

* Version: 3.0.0
* Demo available online at: http://www.readerbench.com/
* Repo owner: mihai.dascalu@cs.pub.ro

```sh
mvn clean install -P {global, english, french, spanish, german, italian, dutch, romanian}
```

## Prerequisites

* Maven and JAVA_HOME environment variable

* The resources for each language need to be extracted for the corresponding archive available at the following link: http://readerbench.com/deployment

## Deploy

Deploy on ReaderBench Artifactory: http://artifactory.readerbench.com:8081/artifactory/webapp/
```sh
mvn clean install deploy -P {global, english, french, spanish, german, italian, dutch, romanian}
```

## Dependencies:

### All languages

* Maven
```sh
<dependency>
    <groupId>com.readerbench.core</groupId>
    <artifactId>readerbench-core-all</artifactId>
    <version>1.0.0</version>
</dependency>
```

* Ivy
```sh
<dependency org="com.readerbench.core" name="readerbench-core-all" rev="1.0.0">
    <artifact name="readerbench-core-all" ext="jar"/>
</dependency>
```

* Gradle
```sh
compile(group: 'com.readerbench.core', name: 'readerbench-core-all', version: '1.0.0')
```

* Sbt
```sh
libraryDependencies += "com.readerbench.core" % "readerbench-core-all" % "1.0.0"
```

### English

* Maven
```sh
<dependency>
    <groupId>com.readerbench.core</groupId>
    <artifactId>readerbench-core-en</artifactId>
    <version>1.0.0</version>
</dependency>
```

* Ivy
```sh
<dependency org="com.readerbench.core" name="readerbench-core-en" rev="1.0.0">
    <artifact name="readerbench-core-en" ext="jar"/>
</dependency>
```

* Gradle
```sh
compile(group: 'com.readerbench.core', name: 'readerbench-core-en', version: '1.0.0')
```

* Sbt
```sh
libraryDependencies += "com.readerbench.core" % "readerbench-core-en" % "1.0.0"
```

### French

* Maven
```sh
<dependency>
    <groupId>com.readerbench.core</groupId>
    <artifactId>readerbench-core-fr</artifactId>
    <version>1.0.0</version>
</dependency>
```

* Ivy
```sh
<dependency org="com.readerbench.core" name="readerbench-core-fr" rev="1.0.0">
    <artifact name="readerbench-core-fr" ext="jar"/>
</dependency>
```

* Gradle
```sh
compile(group: 'com.readerbench.core', name: 'readerbench-core-fr', version: '1.0.0')
```

* Sbt
```sh
libraryDependencies += "com.readerbench.core" % "readerbench-core-fr" % "1.0.0"
```

### Spanish

* Maven
```sh
<dependency>
    <groupId>com.readerbench.core</groupId>
    <artifactId>readerbench-core-es</artifactId>
    <version>1.0.0</version>
</dependency>
```

* Ivy
```sh
<dependency org="com.readerbench.core" name="readerbench-core-es" rev="1.0.0">
    <artifact name="readerbench-core-es" ext="jar"/>
</dependency>
```

* Gradle
```sh
compile(group: 'com.readerbench.core', name: 'readerbench-core-es', version: '1.0.0')
```

* Sbt
```sh
libraryDependencies += "com.readerbench.core" % "readerbench-core-es" % "1.0.0"
```

### German

* Maven
```sh
<dependency>
    <groupId>com.readerbench.core</groupId>
    <artifactId>readerbench-core-de</artifactId>
    <version>1.0.0</version>
</dependency>
```

* Ivy
```sh
<dependency org="com.readerbench.core" name="readerbench-core-de" rev="1.0.0">
    <artifact name="readerbench-core-de" ext="jar"/>
</dependency>
```

* Gradle
```sh
compile(group: 'com.readerbench.core', name: 'readerbench-core-de', version: '1.0.0')
```

* Sbt
```sh
libraryDependencies += "com.readerbench.core" % "readerbench-core-de" % "1.0.0"
```

### Italian

* Maven
```sh
<dependency>
    <groupId>com.readerbench.core</groupId>
    <artifactId>readerbench-core-it</artifactId>
    <version>1.0.0</version>
</dependency>
```

* Ivy
```sh
<dependency org="com.readerbench.core" name="readerbench-core-it" rev="1.0.0">
    <artifact name="readerbench-core-it" ext="jar"/>
</dependency>
```

* Gradle
```sh
compile(group: 'com.readerbench.core', name: 'readerbench-core-it', version: '1.0.0')
```

* Sbt
```sh
libraryDependencies += "com.readerbench.core" % "readerbench-core-it" % "1.0.0"
```

### Dutch

* Maven
```sh
<dependency>
    <groupId>com.readerbench.core</groupId>
    <artifactId>readerbench-core-nl</artifactId>
    <version>1.0.0</version>
</dependency>
```

* Ivy
```sh
<dependency org="com.readerbench.core" name="readerbench-core-nl" rev="1.0.0">
    <artifact name="readerbench-core-nl" ext="jar"/>
</dependency>
```

* Gradle
```sh
compile(group: 'com.readerbench.core', name: 'readerbench-core-nl', version: '1.0.0')
```

* Sbt
```sh
libraryDependencies += "com.readerbench.core" % "readerbench-core-nl" % "1.0.0"
```

### Romanian

* Maven
```sh
<dependency>
    <groupId>com.readerbench.core</groupId>
    <artifactId>readerbench-core-ro</artifactId>
    <version>1.0.0</version>
</dependency>
```

* Ivy
```sh
<dependency org="com.readerbench.core" name="readerbench-core-ro" rev="1.0.0">
    <artifact name="readerbench-core-ro" ext="jar"/>
</dependency>
```

* Gradle
```sh
compile(group: 'com.readerbench.core', name: 'readerbench-core-ro', version: '1.0.0')
```

* Sbt
```sh
libraryDependencies += "com.readerbench.core" % "readerbench-core-ro" % "1.0.0"
```