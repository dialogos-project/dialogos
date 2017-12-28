# The DialogOS Dialog System

This is the source-code repository for the DialogOS dialog system. DialogOS was originally developed by CLT Sprachtechnologie GmbH. The rights were purchased by [Saarland University](https://www.uni-saarland.de/) in 2017, and DialogOS was subsequently open-sourced.

DialogOS has been used in a variety of projects for university and high-school students, see e.g. [here](http://www.debacher.de/wiki/DialogOS) and [here](http://www.coli.uni-saarland.de/courses/lego-04/). It combines a very intuitive graphical interface with out-of-the-box speech recognition and synthesis (using [PocketSphinx](https://github.com/cmusphinx/pocketsphinx) and [MaryTTS](http://mary.dfki.de/). We are currently planning to also restore the seamless Lego integration of the commercial version.


## Running DialogOS

To run DialogOS, you need Java. Go to the main `dialogos` directory. On Linux and macOS, type:

```
./gradlew run
```

or, if you're using Windows:

```
gradlew.bat run
```

This command will first download and install [Gradle](http://gradle.org) if needed. Then it downloads dependencies from the Maven central repository, compiles all modules and plugins, and launches the application.


