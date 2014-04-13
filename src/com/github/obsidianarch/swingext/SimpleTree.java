package com.github.obsidianarch.swingext;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * A simple extension of JTree that makes it easier to add data to the tree.
 * 
 * @author Austin
 */
public class SimpleTree extends JTree {
    
    //
    // Fields
    //
    
    /** The root node of the tree. */
    private DefaultMutableTreeNode rootNode;
    
    //
    // Constructors
    //
    
    /**
     * Constructs a new tree.
     */
    public SimpleTree() {
        rootNode = new DefaultMutableTreeNode();
    }
    
    /**
     * Constructs a new tree with the default root node text.
     * 
     * @param nodeName
     *            The text of the root node.
     */
    public SimpleTree( String nodeName ) {
        rootNode = new DefaultMutableTreeNode( nodeName );
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

}
