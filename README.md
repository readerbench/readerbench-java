# ReaderBench

ReaderBench is a fully functional automated software framework, designed to provide support to learners and tutors for comprehension assessment and prediction in various educational scenarios. The system makes use of text-mining techniques based on advanced natural language processing and machine learning algorithms to deliver summative and formative assessments using multiple data sets (e.g., textual materials, behavior tracks, self-explanations).

## Aim

ReaderBench targets both tutors and students by providing an integrated learning model approach including individual and collaborative learning methods, cohesion-based discourse analysis, dialogical discourse model, textual complexity evaluation, reading strategies identification, and participation and collaboration assessment. By using natural language processing techniques, the main purpose of this framework is to bind traditional learning methods with new trends and technologies to support computer supported collaborative learning (CSCL). ReaderBench, by design, is not meant to replace the tutor, but to scaffold both tutors and learners towards continuous assessment, self-assessment, collaborative evaluation of individuals' contributions, as well as the analysis of reading materials to match readers to an appropriate level of text difficulty.

## Build

* Version: 4.0.0
* Demo available online at: http://www.readerbench.com/
* Repo owner: mihai.dascalu@cs.pub.ro

```sh
mvn clean install
```

* Build without tests' running

```sh
mvn clean install -DskipTests
```

## Prerequisites

* Maven and JAVA_HOME environment variable

* The resources for each language need to be extracted for the corresponding archive available at the following link: http://readerbench.com/deployment

* Download resources from http://owncloud.readerbench.com/

## Project structure (technical aspects)

ReaderBench is a Maven project with 7 modules:

* Datasource Provider Module
* Core Services Module
* Textual complexity Module
* Comprehension Model Module
* Reading Strategies Module
* Processing Service Module
* Parallel Processing Service Module

### Dependencies between modules

* Each module uses Datasource Provide Module and Core Services Module.
* Processing Service Module uses Reading Strategies Module and Textual complexity Module
* Parallel Processing Service Module uses Processing Service Module

## Versioning

Change the version number and propagate it in all modules with the following command:

```sh
mvn versions:set -DnewVersion=x.y.z
```


### Versioning

Change the version number and propagate it in all modules with the following command:

```sh
mvn versions:set -DnewVersion=x.y.z
```


## Deploy

Deploy on ReaderBench Artifactory: http://artifactory.readerbench.com:8081/artifactory/webapp/

```sh
mvn clean install deploy
```

The command above will deploy all modules if it is launched from the folder parent. If you want to deploy only a specific module, launch the command from the folder module.

## Dependencies:

### Datasource Provider Module

* Maven
```sh
<dependency>
    <groupId>com.readerbench</groupId>
    <artifactId>datasource-provider</artifactId>
    <version>4.0.0</version>
</dependency>
```

* Ivy
```sh
<dependency org="com.readerbench" name="datasource-provider" rev="4.0.0">
    <artifact name="datasource-provider" ext="jar"/>
</dependency>
```

* Gradle
```sh
compile(group: 'com.readerbench', name: 'datasource-provider', version: '4.0.0')
```

* Sbt
```sh
libraryDependencies += "com.readerbench" % "datasource-provider" % "4.0.0"
```

### Core Services Module

* Maven
```sh
<dependency>
    <groupId>com.readerbench</groupId>
    <artifactId>core-services</artifactId>
    <version>4.0.0</version>
</dependency>
```

* Ivy
```sh
<dependency org="com.readerbench" name="core-services" rev="4.0.0">
    <artifact name="core-services" ext="jar"/>
</dependency>
```

* Gradle
```sh
compile(group: 'com.readerbench', name: 'core-services', version: '4.0.0')
```

* Sbt
```sh
libraryDependencies += "com.readerbench" % "core-services" % "4.0.0"
```

### Textual Complexity Module

* Maven
```sh
<dependency>
    <groupId>com.readerbench</groupId>
    <artifactId>textual-complexity</artifactId>
    <version>4.0.0</version>
</dependency>
```

* Ivy
```sh
<dependency org="com.readerbench" name="textual-complexity" rev="4.0.0">
    <artifact name="textual-complexity" ext="jar"/>
</dependency>
```

* Gradle
```sh
compile(group: 'com.readerbench', name: 'textual-complexity', version: '4.0.0')
```

* Sbt
```sh
libraryDependencies += "com.readerbench" % "textual-complexity" % "4.0.0"
```

### Comprehension Model Module

* Maven
```sh
<dependency>
    <groupId>com.readerbench</groupId>
    <artifactId>comprehension-model</artifactId>
    <version>4.0.0</version>
</dependency>
```

* Ivy
```sh
<dependency org="com.readerbench" name="comprehension-model" rev="4.0.0">
    <artifact name="comprehension-model" ext="jar"/>
</dependency>
```

* Gradle
```sh
compile(group: 'com.readerbench', name: 'comprehension-model', version: '4.0.0')
```

* Sbt
```sh
libraryDependencies += "com.readerbench" % "comprehension-model" % "4.0.0"
```

### Reading Strategies Module

* Maven
```sh
<dependency>
    <groupId>com.readerbench</groupId>
    <artifactId>reading-strategies</artifactId>
    <version>4.0.0</version>
</dependency>
```

* Ivy
```sh
<dependency org="com.readerbench" name="reading-strategies" rev="4.0.0">
    <artifact name="reading-strategies" ext="jar"/>
</dependency>
```

* Gradle
```sh
compile(group: 'com.readerbench', name: 'reading-strategies', version: '4.0.0')
```

* Sbt
```sh
libraryDependencies += "com.readerbench" % "reading-strategies" % "4.0.0"
```

### Processing Service Module

* Maven
```sh
<dependency>
    <groupId>com.readerbench</groupId>
    <artifactId>processing-service</artifactId>
    <version>4.0.0</version>
</dependency>
```

* Ivy
```sh
<dependency org="com.readerbench" name="processing-service" rev="4.0.0">
    <artifact name="processing-service" ext="jar"/>
</dependency>
```

* Gradle
```sh
compile(group: 'com.readerbench', name: 'processing-service', version: '4.0.0')
```

* Sbt
```sh
libraryDependencies += "com.readerbench" % "processing-service" % "4.0.0"
```

### Parallel Processing Service Module

* Maven
```sh
<dependency>
    <groupId>com.readerbench</groupId>
    <artifactId>parallel-processing-service</artifactId>
    <version>4.0.0</version>
</dependency>
```

* Ivy
```sh
<dependency org="com.readerbench" name="parallel-processing-service" rev="4.0.0">
    <artifact name="parallel-processing-service" ext="jar"/>
</dependency>
```

* Gradle
```sh
compile(group: 'com.readerbench', name: 'parallel-processing-service', version: '4.0.0')
```

* Sbt
```sh
libraryDependencies += "com.readerbench" % "parallel-processing-service" % "4.0.0"
```

