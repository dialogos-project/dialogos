**unreleased**
- merged the pull request to get rid of VoiceXML export (which never worked). Thanks Mikhail Raudin!
  This goes towards fixing issue #97 (however, the abstract interface still remains).
- fix a thread-safety issue with Swing that is triggered on (recent?) Java 8 when saving a document 

**Version 2.1.4, 2024-11-15**

- support for Java > 9 (which previously broke) and compile without deprecation warnings on Java 21.
- update MaryTTS version to 5.3-SNAPSHOT which makes DialogOS runnable again (although, only with one 
  single English voice available from maven artifact repositories; the full list remains available 
  through the installer)
- removed DialogOS's handling of character encodings. We'll just go with what Java uses, which typically
  is Utf-8.
- updated Gradle (mostly?) across the board to 8.10.2; also updated many dependencies to newer versions.
- better handling for user-defined functions and grammars that do not compile because of syntax errors. 
  Now a warning is shown after editing the function/grammar. Note that functions may still fail 
  when called at runtime (e.g. because you reference variables that are not accessible) and there is 
  no checking for grammars that are created from expressions at runtime. 
  However, this will already solve many frequent issues (like missing semicolons. 
  (fix for #102, #200, #201)
- left recursion, which leads to unexplainable behaviour in the parser, is now warned about.
- new script function `sleep` which does what you'd expect (in milliseconds).

**Version 2.1.3, 2019-09-01**

- Plugins can now register functions to be available via DialogOS script. See #197 for details.
  As an example, the tts plugin provides a function to query how long it would take to say a certain 
  text.
- a next synthesis will now always abort any previous syntheses that might still be ongoing (#196)
- a synthesis node can be instructed to only start speaking when the previous synthesis has ended
  (or, alternatively, to interrupt the previous synthesis)

**Version 2.1.2, 2019-08-16**

- Robustness against JavaFX issues
- Fixed bug 189 which could lead to issues with Strings missing multiple consecutive whitespaces in DialogOS-script
- if a file is opened via the commandline, the ProjectStartupWindow and the loading-progress windows are suppressed
- the SQL plugin now supports writing to a database
- Gradle version has been unified and updated to 4.10.3 (in most parts of the project)
- install more speech synthesis voices by default

- (many) plugins now save their settings to a DialogOS dialog model only if there is a node that is 
  relevant to the plugin and only those settings are saved that are different from the default settings.
  WARNING: This change can potentially lead to data loss: imagine you have stored many G2P rules. 
           If you then save the dialog model that does not contain any SphinxNode, the G2P rules will not
           be stored in the file (because the plugin assumes that it is not relevant for the given file).
  See notes in bug 178.
  DialogOS now warns about missing plugins when loading a file (that requests certain plugins).

**Version 2.1.1, 26 April 2019**

- Fixed bug in dialogos-distribution where gson failed to be bundled with distribution (see #157).
- Fixed bug in EV3 plugin where dialog would crash if the EV3 dummy implementation was selected (see #159).
- Fixed bug where test-variable node could not distinguish between different variables (see #162).
- Fixed bug with pronunciations (see #121 and #165).
- Improved interface for plugins, that enable them to not initialize if they are irrelevant for a document (see also #178 and #177)
- Enable German ASR for people running DialogOS via gradle rather than installation (see #104).

**Version 2.1.0, 24 January 2019**

Released by @alexanderkoller and @akoehn

- Added plugin that compiles DialogOS dialogs into skills for Amazon Alexa.
- Added plugin that allows DialogOS to control Lego Mindstorms EV3 robots.


**Version 2.0.5, 2 January 2019**

Released by @alexanderkoller

- Added "silent mode" to speech recognizer again (see #132).
- Fixed a bug where Slot#setValue would sometimes not change the value of a variable (see #137).
- "Keywords" in the speech recognizer window are now called "alternatives" (see #109).
- Removed obsolete clients from the DialogOS codebase (see #129).
- Fixed a bug where nodes would sometimes randomly move down (see #134).


**Version 2.0.4, 8 December 2018**

Released by @alexanderkoller

- Enabled use of Kleene plus (positive closure, +) operator in grammars (see #121).
- Reordered tabs in node properties windows: useful tabs are now on the left (see #122).
- GUI language can now be changed from the Help menu (see #95).
- Discarding TTS node property window without ever using it first no longer causes an exception (see #128).
- ASR no longer becomes unresponsive if aborted on the very first run (see #123).
- Library for building clients is now automatically released on Jitpack (see #127).
- Dialog files (`*.dos`) can now be opened by double-click on Windows (see #83).

**Version 2.0.3, 21 November 2018**

Released by @alexanderkoller

- Can now use umlauts in the speech recognizer under Windows (see #108).
- The recognizer properties window is now correctly localized (see #109).
- Better error reporting from various parts of DialogOS.


**Version 2.0.2, 15 November 2018**

Released by @alexanderkoller

- Deleting a speech recognizer node no longer crashes DialogOS.
- Exception dictionary in speech recognizer now works as intended (see #105).
- Better error reporting from various parts of DialogOS.


**Version 2.0.1, 25 October 2018**

Released by @alexanderkoller

Changed the visibility of all abstract methods of Node, AbstractInputNode, and AbstractOutputNode to public. This will break Node implementations in which these methods have "protected" visibility. You can make your own Node implementations compatible with DialogOS 2.0.1 by changing their visibility to "public".

**Version 2.0.0, 14 August 2018**

Released by @timobaumann

too many changes to list.
