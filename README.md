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

##Version bump

Changing the version number in all modules:
```sh
mvn versions:set -DnewVersion=x.y.z
```

## Dependencies:
todo