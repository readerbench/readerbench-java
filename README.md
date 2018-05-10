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

## Deploy

Deploy on ReaderBench Artifactory: http://artifactory.readerbench.com:8081/artifactory/webapp/
```sh
mvn clean install deploy
```

## Dependencies:

### Age of Eposure Module

* Maven
```sh
<dependency>
    <groupId>com.readerbench</groupId>
    <artifactId>age-of-exposure</artifactId>
    <version>4.0.0</version>
</dependency>
```

* Ivy
```sh
<dependency org="com.readerbench" name="age-of-exposure" rev="4.0.0">
    <artifact name="age-of-exposure" ext="jar"/>
</dependency>
```

* Gradle
```sh
compile(group: 'com.readerbench', name: 'age-of-exposure', version: '4.0.0')
```

* Sbt
```sh
libraryDependencies += "com.readerbench" % "age-of-exposure" % "4.0.0"
```
