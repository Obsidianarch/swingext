package com.github.obsidianarch.swingext;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

/**
 * A simple extension of JTree that makes it easier to add data to the tree.
 * 
 * @author Austin
 */
public class SimpleTree extends JTree implements MouseListener {
    
    //
    // Fields
    //
    
    /** The root node of the tree. */
    private DefaultMutableTreeNode rootNode;

    /** The methods to execute when a node is selected */
    private List< Method >         clickListeners = new ArrayList<>();

    //
    // Constructors
    //
    
    /**
     * Constructs a new tree with no text for the root node.
     */
    public SimpleTree() {
        this( "" );
    }
    
    /**
     * Constructs a new tree with the default root node text.
     * 
     * @param nodeName
     *            The text of the root node.
     */
    public SimpleTree( String nodeName ) {
        rootNode = new DefaultMutableTreeNode( nodeName );
        getSelectionModel().setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
        addMouseListener( this );
    }
    
    //
    // Setters
    //
    
    /**
     * @param text
     *            The new text of the root node.
     */
    public void setRootText( String text ) {
        rootNode.setUserObject( text );
    }
    
    //
    // Getters
    //
    
    /**
     * @return The text of the root node.
     */
    public String getRootText() {
        return rootNode.getUserObject().toString();
    }

    /**
     * Gets the node specified by the path, if the path is incorrect or the node does not
     * exist, {@code null} will be returned.
     * 
     * @param path
     *            The path to the node.
     * @return The node specified by the path.
     */
    public DefaultMutableTreeNode getNode( String... path ) {
        if ( path.length == 0 ) return rootNode;
        return getNode( false, rootNode, path );
    }
    
    /**
     * Gets the node.
     * 
     * @param makeNodes
     *            If the nodes should be created if they don't exist.
     * @param parent
     *            The current parent (recusion).
     * @param path
     *            The path to the node.
     * @return The node.
     */
    private DefaultMutableTreeNode getNode( boolean makeNodes, DefaultMutableTreeNode parent, String... path ) {
        if ( path.length == 0 ) return parent;

        String[] nextPath = new String[ path.length - 1 ];
        for ( int i = 1; i < path.length; i++ ) {
            nextPath[ i - 1 ] = path[ i ];
        }
        
        for ( int i = 0; i < rootNode.getChildCount(); i++ ) {
            DefaultMutableTreeNode node = ( DefaultMutableTreeNode ) rootNode.getChildAt( i );
            if ( node.getUserObject().toString().equals( path[ 0 ] ) ) {
                return getNode( makeNodes, node, nextPath );
            }
        }
        
        if ( makeNodes ) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode( path[ 0 ] );
            parent.add( node );
            return getNode( true, node, nextPath );
        }

        return null;
    }

    //
    // Actions
    //
    
    /**
     * Adds a node to the path.
     * 
     * @param path
     *            The path to get to the node.
     */
    public void addNode( String... path ) {
        getNode( true, rootNode, path );
    }
    
    /**
     * Adds a method to be execute when a list item is clicked/
     * 
     * @param clazz
     *            The class the method appears in.
     * @param methodName
     *            The name of the method.
     * @param parameter
     *            If the method has a {@code DefaultMutableTreeNode} parameter.
     */
    public void addClickListener( Class< ? > clazz, String methodName, boolean parameter ) {
        try {
            
            Method method = null;
            if ( parameter ) {
                method = clazz.getMethod( methodName, DefaultMutableTreeNode.class );
            }
            else {
                method = clazz.getMethod( methodName );
            }
            
            addClickListener( method );
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }
    
    /**
     * Adds a method to be executed when a list item is clicked.
     * 
     * @param method
     *            The method to execute.
     */
    public void addClickListener( Method method ) {
        clickListeners.add( method );
    }

    //
    // Overrides
    //
    
    @Override
    public void mouseClicked( MouseEvent e ) {
        if ( e.getClickCount() != 2 ) return; // only respond to double clicks

        DefaultMutableTreeNode node = ( DefaultMutableTreeNode ) getLastSelectedPathComponent(); // get the last node
        if ( node == null ) return; // no node selected
            
        for ( Method method : clickListeners ) {
            
            try {
                
                if ( method.getParameterCount() == 0 ) {
                    method.invoke( this );
                }
                else if ( ( method.getParameterCount() == 1 ) && method.getParameterTypes()[ 0 ].equals( DefaultMutableTreeNode.class ) ) {
                    method.invoke( this, node );
                }
                else {
                    System.err.printf( "Invalid parameters should read %s() or %s( DefaultMutableTreeNode )%n", method.getName(), method.getName() );
                }
                
            }
            catch ( Exception ex ) {
                System.err.println( "Failed invoking method:  " + method.getName() );
                ex.printStackTrace();
            }
        }
    }
    
    @Override
    public void mouseEntered( MouseEvent e ) {
    }
    
    @Override
    public void mouseExited( MouseEvent e ) {
    }
    
    @Override
    public void mousePressed( MouseEvent e ) {
    }
    
    @Override
    public void mouseReleased( MouseEvent e ) {
    }

}
