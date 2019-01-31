
**Version 2.1.1, unreleased**

- Fixed bug in dialogos-distribution where gson failed to be bundled with distribution (see #157).
- Fixed bug in EV3 plugin where dialog would crash if the EV3 dummy implementation was selected (see #159).
- Fixed bug where test-variable node could not distinguish between different variables (see #162).
- Fixed bug with pronunciations (see #121).

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
