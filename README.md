# DialogOS (dirty version)

Private repository for cleaning up DialogOS.

The purpose of this repository is to clean up the DialogOS source code so it can be made open-source. Please do not make this repository public, ever. It contains proprietary libraries, which will remain present in the repository history even when they are deleted from working copies.


## Compiling DialogOS

To run DialogOS, you need Java. Go to the main `dialogos` directory. On Linux and macOS, type:

```
./gradlew run
```

or, if you're using Windows:

```
gradlew.bat run
```

This command will first download and install [Gradle](http://gradle.org) if needed. Then it downloads dependencies from the Maven central repository, compiles all modules and plugins, and launches the application.

To build a single module, run

```
./gradlew moduleName:build
```

## Speech recognizer models

Some of the speech recognizer plugins that come with DialogOS require
model files to run correctly. These are too large to put them under
version control. Instead, download them here:

* [Basic English model for PocketSphinx](http://www.coli.uni-saarland.de/~koller/dialogos/models/pocketsphinx_en.zip)
  (from [PocketSphinx source code](https://github.com/cmusphinx/pocketsphinx))
* [German model for PocketSphinx](http://www.coli.uni-saarland.de/~koller/dialogos/models/pocketsphinx_de.zip)
  (from [Sphinx Sourceforge page](https://sourceforge.net/projects/cmusphinx/files/Acoustic%20and%20Language%20Models/))

Unpack these models inside the `models` underneath your current
working directory. This should create new directories such as
`./models/en_us`.

