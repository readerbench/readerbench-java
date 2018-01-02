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
    <groupId>com.readerbench</groupId>
    <artifactId>readerbench-all</artifactId>
    <version>3.0.0</version>
</dependency>
```

* Ivy
```sh
<dependency org="com.readerbench" name="readerbench-all" rev="3.0.0">
    <artifact name="readerbench-all" ext="jar"/>
</dependency>
```

* Gradle
```sh
compile(group: 'com.readerbench', name: 'readerbench-all', version: '3.0.0')
```

* Sbt
```sh
libraryDependencies += "com.readerbench" % "readerbench-all" % "3.0.0"
```

### English

* Maven
```sh
<dependency>
    <groupId>com.readerbench</groupId>
    <artifactId>readerbench-en</artifactId>
    <version>3.0.0</version>
</dependency>
```

* Ivy
```sh
<dependency org="com.readerbench" name="readerbench-en" rev="3.0.0">
    <artifact name="readerbench-en" ext="jar"/>
</dependency>
```

* Gradle
```sh
compile(group: 'com.readerbench', name: 'readerbench-en', version: '3.0.0')
```

* Sbt
```sh
libraryDependencies += "com.readerbench" % "readerbench-en" % "3.0.0"
```

### French

* Maven
```sh
<dependency>
    <groupId>com.readerbench</groupId>
    <artifactId>readerbench-fr</artifactId>
    <version>3.0.0</version>
</dependency>
```

* Ivy
```sh
<dependency org="com.readerbench" name="readerbench-fr" rev="3.0.0">
    <artifact name="readerbench-fr" ext="jar"/>
</dependency>
```

* Gradle
```sh
compile(group: 'com.readerbench', name: 'readerbench-fr', version: '3.0.0')
```

* Sbt
```sh
libraryDependencies += "com.readerbench" % "readerbench-fr" % "3.0.0"
```

### Spanish

* Maven
```sh
<dependency>
    <groupId>com.readerbench</groupId>
    <artifactId>readerbench-es</artifactId>
    <version>3.0.0</version>
</dependency>
```

* Ivy
```sh
<dependency org="com.readerbench" name="readerbench-es" rev="3.0.0">
    <artifact name="readerbench-es" ext="jar"/>
</dependency>
```

* Gradle
```sh
compile(group: 'com.readerbench', name: 'readerbench-es', version: '3.0.0')
```

* Sbt
```sh
libraryDependencies += "com.readerbench" % "readerbench-es" % "3.0.0"
```

### German

* Maven
```sh
<dependency>
    <groupId>com.readerbench</groupId>
    <artifactId>readerbench-de</artifactId>
    <version>3.0.0</version>
</dependency>
```

* Ivy
```sh
<dependency org="com.readerbench" name="readerbench-de" rev="3.0.0">
    <artifact name="readerbench-de" ext="jar"/>
</dependency>
```

* Gradle
```sh
compile(group: 'com.readerbench', name: 'readerbench-de', version: '3.0.0')
```

* Sbt
```sh
libraryDependencies += "com.readerbench" % "readerbench-de" % "3.0.0"
```

### Italian

* Maven
```sh
<dependency>
    <groupId>com.readerbench</groupId>
    <artifactId>readerbench-it</artifactId>
    <version>3.0.0</version>
</dependency>
```

* Ivy
```sh
<dependency org="com.readerbench" name="readerbench-it" rev="3.0.0">
    <artifact name="readerbench-it" ext="jar"/>
</dependency>
```

* Gradle
```sh
compile(group: 'com.readerbench', name: 'readerbench-it', version: '3.0.0')
```

* Sbt
```sh
libraryDependencies += "com.readerbench" % "readerbench-it" % "3.0.0"
```

### Dutch

* Maven
```sh
<dependency>
    <groupId>com.readerbench</groupId>
    <artifactId>readerbench-nl</artifactId>
    <version>3.0.0</version>
</dependency>
```

* Ivy
```sh
<dependency org="com.readerbench" name="readerbench-nl" rev="3.0.0">
    <artifact name="readerbench-nl" ext="jar"/>
</dependency>
```

* Gradle
```sh
compile(group: 'com.readerbench', name: 'readerbench-nl', version: '3.0.0')
```

* Sbt
```sh
libraryDependencies += "com.readerbench" % "readerbench-nl" % "3.0.0"
```

### Romanian

* Maven
```sh
<dependency>
    <groupId>com.readerbench</groupId>
    <artifactId>readerbench-ro</artifactId>
    <version>3.0.0</version>
</dependency>
```

* Ivy
```sh
<dependency org="com.readerbench" name="readerbench-ro" rev="3.0.0">
    <artifact name="readerbench-ro" ext="jar"/>
</dependency>
```

* Gradle
```sh
compile(group: 'com.readerbench', name: 'readerbench-ro', version: '3.0.0')
```

* Sbt
```sh
libraryDependencies += "com.readerbench" % "readerbench-ro" % "3.0.0"
```