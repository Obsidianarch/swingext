package com.github.obsidianarch.swingext;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * A simple extension on JFrame which makes is much easier and cleaner to add actions to
 * components.
 * 
 * @author Austin
 */
public class SimpleFrame extends JFrame implements ActionListener {
    
    //
    // Fields
    //
    
    /** The partial class which contains all the action methods. */
    private final Class< ? >     partialClass;
    
    /** The top level menus in the menu bar. */
    private Map< String, JMenu > topLevelMenus = new HashMap<>();
    
    /** The menubar on this program. */
    private JMenuBar             menuBar       = new JMenuBar();

    //
    // Constructors
    //
    
    /**
     * Constructs a new SimpleFrame with the given partial class for action methods.
     * 
     * @param partial
     *            The class which contains all action code.
     * @throws IllegalArgumentException
     *             If {@code partial} was null.
     */
    public SimpleFrame( Class< ? > partial ) throws IllegalArgumentException {
        if ( partial == null ) throw new IllegalArgumentException( "partial class cannot be null!" );
        partialClass = partial;
    }
    
    /**
     * Constructs a new SimpleFrame with the given object for action methods.
     * 
     * @param o
     *            The object which contains all action code.
     * @throws IllegalArgumentException
     *             If {@code o} was null.
     */
    public SimpleFrame( Object o ) throws IllegalArgumentException {
        if ( o == null ) throw new IllegalArgumentException( "partial class object cannot be null!" );
        partialClass = o.getClass();
    }

    //
    // Actions
    //
    
    /**
     * Builds an array of methods with matching names in the partial class.
     * 
     * @param methodName
     *            The name of the method.
     * @return An array of the methods with the same name.
     * @throws SecurityException
     *             If the security manager does not allow access to the reflections api.
     */
    private Method[] getMatchingMethods( String methodName ) throws SecurityException {
        Method[] methods = partialClass.getDeclaredMethods(); // all methods declared in the class, non-inherited methods
        
        List< Method > matchingMethods = new ArrayList<>(); // a list of the methods that are matching
        
        for ( Method method : methods ) {
            if ( !method.getName().equals( methodName ) ) continue; // is the method named correctly?

            matchingMethods.add( method ); // add the matching method to the list
        }

        return matchingMethods.toArray( new Method[ 0 ] );
    }

    /**
     * Gets the partial class's method if there are no parameters for the method.
     * 
     * @param methods
     *            The methods with the same name as the action command.
     * @return The method if it exists with no parameters, otherwise {@code null}.
     * @throws SecurityException
     *             If the security manager does not allow for access to the reflections
     *             api.
     */
    private Method getNoParameters( Method[] methods ) throws SecurityException {
        for ( Method method : methods ) {
            if ( method.getParameterTypes().length == 0 ) return method; // no parameters
        }
        
        return null;
    }
    
    /**
     * Gets the partial class's method if the only parameter is a subclass of SimpleFrame
     * (i.e. if the method requests the frame's class).
     * 
     * @param methods
     *            The methods with the same name as the action command.
     * @return The method if it exists with the frame parameter, otherwise {@code null}.
     * @throws SecurityException
     *             If the security manager does not allow for access to the reflections
     *             api.
     */
    private Method getFrameParameter( Method[] methods ) throws SecurityException {
        
        // iterate over each method
        for ( Method method : methods ) {
            if ( method.getParameterTypes().length != 1 ) continue; // there can only be one parameter

            Class< ? > param = method.getParameterTypes()[ 0 ]; // get the one parameter
            if ( SimpleFrame.class.isAssignableFrom( param ) ) return method; // the parameter must be a subclass of SimpleFrame
        }
        
        return null; // no matching methods were found
    }

    //
    // Menus
    //
    
    /**
     * Adds a menu item, and every string preceding it is a menu. Also assigns the
     * accelerator of the menu item.
     * 
     * @param ks
     *            The item's accelerator.
     * @param location
     *            The location to get to the menu item.
     * @return The Menu item that was created.
     */
    public JMenuItem addMenuItem( KeyStroke ks, String... location ) {
        JMenuItem mntm = addMenuItem( location );
        mntm.setAccelerator( ks );
        return mntm;
    }

    /**
     * Adds a menu item, and every string preceding it is a menu.
     * 
     * @param location
     *            The location to get to the menu item.
     * @return The Menu Item that was created
     */
    public JMenuItem addMenuItem( String... location ) {
        JMenu parent = getParent( true, location );
        JMenuItem item = new JMenuItem( location[ location.length - 1 ] );
        parent.add( item );
        
        item.addActionListener( this );
        
        return item;
    }
    
    /**
     * Adds a separator to the given hierarchy of menus.
     * 
     * @param location
     *            The menus to add the separator to.
     */
    public void addSeparator( String... location ) {
        JMenu parent = getParent( false, location );
        parent.addSeparator();
    }
    
    /**
     * Gets a JMenu from the hierarchy.
     * 
     * @param remove
     *            Is there an extra value at the end of the array?
     * @param location
     *            The location to the desired child element.
     * @return The desired child JMenu.
     */
    public JMenu getParent( boolean remove, String... location ) {
        JMenu parent = getTopLevelMenu( location[ 0 ] ); // the parent of each step
        
        // create the menus for the item
        for ( int i = 1; i < ( location.length - ( remove ? 1 : 0 ) ); i++ ) {
            
            // try to find a pre-existing menu item
            boolean exists = false;
            
            for ( int j = 0; j < parent.getItemCount(); j++ ) {
                if ( parent.getItem( j ).getText().equalsIgnoreCase( location[ i ] ) ) {
                    parent = ( JMenu ) parent.getItem( j );
                    exists = true;
                    break;
                }
            }
            
            // the menu hasn't been created, do so now
            if ( !exists ) {
                JMenu menu = new JMenu( location[ i ] ); // create a new menu
                parent.add( menu ); // add the menu to the parent
                parent = menu; // this is the new parent
            }
        }
        
        return parent;
    }
    
    /**
     * Gets the top level menu. If it does not exist it will be created.
     * 
     * @param name
     *            The text of the component.
     * @return The top level menu with the given text.
     */
    private JMenu getTopLevelMenu( String name ) {
        if ( topLevelMenus.isEmpty() ) setJMenuBar( menuBar );
        JMenu menu = topLevelMenus.get( name );
        
        // if the item doesn't exist, create it
        if ( menu == null ) {
            menu = new JMenu( name );
            menuBar.add( menu );
            topLevelMenus.put( name, menu );
        }
        
        return menu;
    }
    
    //
    // Overrides
    //
    
    @Override
    public void actionPerformed( ActionEvent e ) {
        String methodName = e.getActionCommand(); // the name of the method is defined by the action command
        
        try {
            
            Method[] matchingMethods = getMatchingMethods( methodName ); // get methods with the correct name

            Method method;
            method = getNoParameters( matchingMethods ); // try to find a method with no parameters with the correct name
            
            if ( method != null ) {
                method.invoke( this ); // invoke the method with no parameters
                return;
            }
            
            method = getFrameParameter( matchingMethods ); // try to find a method with a single parameter that is a subclass of SimpleFrame
            
            if ( method != null ) {
                method.invoke( this, this ); // invoke the method with "this" as a parameter
                return;
            }
            
            System.err.println( "No method found in partial class by the name \"" + methodName + "\"!" );
        }
        catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException ex ) {
            ex.printStackTrace();
        }

    }

}
