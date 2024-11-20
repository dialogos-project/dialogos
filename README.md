# The DialogOS Dialog System

[![Release](https://jitpack.io/v/dialogos-project/dialogos.svg)](https://jitpack.io/#dialogos-project/dialogos)

This is the source-code repository for the DialogOS dialog system. DialogOS was originally developed by CLT Sprachtechnologie GmbH. The rights were purchased by [Saarland University](https://www.uni-saarland.de/) in 2017, and DialogOS was subsequently open-sourced.

DialogOS has been used in a variety of projects for university and high-school students, see e.g. [here](http://www.debacher.de/wiki/DialogOS) and [here](http://www.coli.uni-saarland.de/courses/lego-04/). It combines a very intuitive graphical interface with out-of-the-box speech recognition and synthesis (using [PocketSphinx](https://github.com/cmusphinx/pocketsphinx) and [MaryTTS](http://mary.dfki.de/)), and is seamlessly integrated with Lego Mindstorms.

The open-source DialogOS runs under Windows, MacOS, and Linux. This is in contrast to the commercial version, which only worked under Windows due to limitations in the speech recognizer.

<img src="https://www.dialogos.app/images/DialogOS.jpg" />


## Documentation

An [English manual](https://github.com/dialogos-project/dialogos/wiki/Manual) is available in the Wiki.

Ein [deutsches Handbuch](https://github.com/dialogos-project/dialogos/wiki/Handbuch) finden Sie im Wiki.

Developers should check out the [Wiki](https://github.com/dialogos-project/dialogos/wiki) for detailed technical information.


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


## Speech recognizer models

DialogOS comes bundled with the PocketSphinx speech recognizer, which needs at least one speech recognizer model for each language that it should recognize. Because these models are large, they are not distributed with DialogOS. However, you can download them easily from within DialogOS.

To do this, go to Dialog -> CMU PocketSphinx. You will find that initially there are no models available in the "Model" dropdown box. Click on the button "Install more models" and select a model for the language you want. Once the model has been installed, it is _not_ automatically selected, but it is available in the "Model" dropdown box. Select it and close the configuration window. You are now ready to make PocketSphinx nodes and have DialogOS recognize your speech.
