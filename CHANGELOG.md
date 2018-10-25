**Version 2.0.1, 25 October 2018**

Released by @alexanderkoller

Changed the visibility of all abstract methods of Node, AbstractInputNode, and AbstractOutputNode to public. This will break Node implementations in which these methods have "protected" visibility. You can make your own Node implementations compatible with DialogOS 2.0.1 by changing their visibility to "public".

