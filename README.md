SwingExt
========

A collection of swing extensions that I've had to create and will probably have to use again. Most projects that use swing will also be dependent upon this library.  


Components
========

These are chronologically order by the date they were created.  

_VTabbedPane_  
==
This allows for tab text to be oriented vertically. Useful whenever the tabs are on the left or right sides of the pane.  

_SimpleFrame_  
==
Modeled after the C# method of separating the design code from the action code, this is an extension of JFrame that adds the "partial class" from C# to Java. The action method for anything is determined by the components "getActionString" method, the implemented ActionListener will then use the native reflections API to find the matching method in the partial class and then execute it.  
The action methods can either have no parameters or a single paramter, which must be a subclass of SimpleFrame (this should probably be the class that is calling it), depending on whether or not the method needs access to the frame's controls.  
Creating menus is extremely simple when using SimpleFrame, as the methods used for this (addMenuItem(Keystroke, String... location) and addMenuItem(String... location)) utilize variable arguments. For example, _addMenuItem( "File", "Exit" )_ creates a menu item under the menu file, with the text "Exit". This applies for all arguments and the menus are created when needed. Here's an excerpt from my [JSnippet](https://github.com/Obsidianarch/jsnippet) project's menu:  
```
addMenuItem( "File", "Build" ).setActionCommand( "build" );;
addMenuItem( "File", "Execute" ).setActionCommand( "execute" );
addMenuItem( "File", "Build and Execute" ).setActionCommand( "buildAndExecute" );
addSeparator( "File" );
addMenuItem( "File", "Exit" ).setActionCommand( "exit" );
            
addMenuItem( "Edit", "Run Arguments" ).setActionCommand( "editRunArgs" );
addSeparator( "Edit" );
addMenuItem( "Edit", "Reset Default Text" ).setActionCommand( "resetDefaultText" );
addMenuItem( "Edit", "Reset Template File" ).setActionCommand( "resetTemplates" );
addMenuItem( "Edit", "Reset Keywords File" ).setActionCommand( "resetKeywords" );
```
Which produces [this](http://imgur.com/KKrwYFS) for the file menu, and [this](http://imgur.com/D60xMuS) for the edit menu.

_ImageView_  
==
An extension on a JComponent that adds the missing capability of zoom and panning from the traditional JLabel with only an icon.  

_SimpleTree_  
==
An extension of JTree that allows for adding nodes with ease.  The node creation system functions exactly the same was the menu system works in the SimpleFrame extension.
