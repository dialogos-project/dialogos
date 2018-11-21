**Version 2.0.4, unreleased**

Released by ...


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

