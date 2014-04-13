package com.github.obsidianarch.swingext;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

/**
 * @author Austin
 */
public class VTabbedPane extends JTabbedPane {
    
    //
    // Constants
    //
    
    /** The clockwise orientation of the vertical tabs. */
    public static final int CLOCKWISE         = 0;

    /** The counter-clockwise orientation of the vertical tabs. */
    public static final int COUNTER_CLOCKWISE = 1;
    
    //
    // Fields
    //
    
    /** The orientation of the text. */
    private int             orientation;

    //
    // Constructors
    //

    /**
     * Constructs a vertical tabbed pane where tabs are placed at the position described
     * in {@code tabPlacement} and the text oriented as described by {@code orientation}.
     * 
     * @param tabPlacement
     *            The position of the tabs.
     * @param orientation
     *            The orientation of the tabs.
     */
    public VTabbedPane( int tabPlacement, int orientation ) {
        super( tabPlacement );
        this.orientation = orientation;
    }
    
    //
    // Actions
    //
    
    /**
     * Rotates the text to match the current orientation.
     * 
     * @param text
     *            The text of the tab.
     * @return An icon of the text rotated in the correct direction.
     */
    private Icon createIcon( String text ) {
        FontMetrics metrics = getFontMetrics( getFont() ); // measurements for the current font
        
        int textHeight = metrics.getHeight();
        int textWidth = metrics.stringWidth( text ) + 4;
        BufferedImage source = new BufferedImage( textWidth, textHeight, BufferedImage.TYPE_INT_ARGB );
        {
            Graphics2D grafix = source.createGraphics();
            grafix.setFont( getFont() );
            grafix.setColor( getForeground() );
            grafix.drawString( text, 0, ( textHeight / 2 ) + 1 );
            grafix.dispose();
        }
        
        BufferedImage destination = new BufferedImage( textHeight - 6, textWidth, BufferedImage.TYPE_INT_ARGB );
        
        AffineTransform tx = new AffineTransform();
        tx.translate( textHeight / 2, textWidth / 2 );
        tx.rotate( Math.toRadians( orientation == CLOCKWISE ? 90.0 : -90.0 ) );
        tx.translate( -textWidth / 2, -textHeight / 2 );
        
        AffineTransformOp txo = new AffineTransformOp( tx, AffineTransformOp.TYPE_BICUBIC );
        destination = txo.filter( source, destination );
        
        ImageIcon icon = new ImageIcon( destination );
        return icon;
    }
    
    //
    // Overrides
    //
    
    @Override
    public void addTab( String title, Component component ) {
        addTab( title, null, component, "" );
    }
    
    @Override
    public void addTab( String title, Icon icon, Component component ) {
        addTab( title, icon, component, "" );
    }
    
    @Override
    public void addTab( String title, Icon icon, Component component, String tip ) {
        super.addTab( title, icon, component, tip );

        JLabel label = new JLabel( createIcon( title ) );
        setTabComponentAt( getTabCount() - 1, label );
    }

}
