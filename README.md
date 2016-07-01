# ReaderBench

ReaderBench is a fully functional automated software framework, designed to provide support to learners and tutors for comprehension assessment and prediction in various educational scenarios. The system makes use of text-mining techniques based on advanced natural language processing and machine learning algorithms to deliver summative and formative assessments using multiple data sets (e.g., textual materials, behavior tracks, self-explanations).

## Aim
ReaderBench targets both tutors and students by providing an integrated learning model approach including individual and collaborative learning methods, cohesion-based discourse analysis, dialogical discourse model, textual complexity evaluation, reading strategies identification, and participation and collaboration assessment. By using natural language processing techniques, the main purpose of this framework is to bind traditional learning methods with new trends and technologies to support computer supported collaborative learning (CSCL). ReaderBench, by design, is not meant to replace the tutor, but to scaffold both tutors and learners towards continuous assessment, self-assessment, collaborative evaluation of individuals' contributions, as well as the analysis of reading materials to match readers to an appropriate level of text difficulty.

## Build

* Version: 2.2
* Demo available online at: http://www.readerbench.com/
* Repo owner: mihai.dascalu@cs.pub.ro

### Prerequisites
* Maven and JAVA_HOME environment variable

The following resources for English and Freench need to be downloaded: [https://owncloud.readerbench.com/index.php/s/w33mnCcpH1Bp1zs](https://owncloud.readerbench.com/index.php/s/w33mnCcpH1Bp1zs)

The zip archive is automatically downloaded in the build.sh installment script:

### Steps

The following commands should be run to install the project:

#### A. Download zip archive, unarchive and run:

```sh
mvn clean install
```

#### B. Run build and install scripts for the web server:

```sh
./build.sh
./run.sh
```
