package com.github.obsidianarch.swingext;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A useful component for viewing images.
 * 
 * @author Austin
 */
public class ImageView extends JComponent implements ActionListener, ChangeListener {

    //
    // Fields
    //
    
    /** Causes the image to be changed when it comes time. */
    private final Timer     animationTimer = new Timer( 1000, this );

    /** The source frames. */
    private BufferedImage[] frames;
    
    /** The zoomed-in frames. */
    private BufferedImage[] zoomedFrames;

    /** The delay between frame updates. */
    private int             frameDelay;
    
    /** The current index in the frame array. */
    private int             frameIndex;
    
    /** The zoom level, 1 = 100% */
    private double          zoom           = 1f;
    
    /** The slider that controls the ImageView's zoom */
    private JSlider         zoomSlider;

    //
    // Constructors
    //
    
    /**
     * Constructs a new ImageView.
     */
    public ImageView() {
        animationTimer.setInitialDelay( 0 );
    }
    
    //
    // Setters
    //
    
    /**
     * Changes the zoom level of the image, 1.0 is normal zoom..
     * 
     * @param zoom
     *            The new zoom level.
     */
    public void setZoom( double zoom ) {
        this.zoom = zoom;
        
        if ( zoomSlider != null ) {
            zoomSlider.setValue( ( int ) ( zoom * 100 ) );
        }

        // start the image resizing thread
        Thread thread = new Thread( new ImageZoomer( this ) );
        thread.setName( "Image Zoomer" );
        thread.start();
    }

    /**
     * @param image
     *            The new image.
     */
    public void setImage( Image image ) {
        // convert the image to a bufferedimage
        BufferedImage bi = new BufferedImage( image.getWidth( null ), image.getHeight( null ), BufferedImage.TYPE_INT_RGB );
        {
            Graphics2D grafix = bi.createGraphics();
            grafix.drawImage( image, 0, 0, null );
            grafix.dispose();
        }
        setImage( bi );
    }
    
    /**
     * @param bi
     *            The new image.
     */
    public void setImage( BufferedImage bi ) {
        setFrames( new BufferedImage[ ] { bi }, 1000 );
    }

    /**
     * @param bis
     *            The new set of frames for the image view.
     * @param delay
     *            The delay (in milliseconds) between each frame.
     */
    public void setFrames( BufferedImage[] bis, int delay ) {
        frames = bis;
        frameDelay = delay;
        frameIndex = 0; // reset to the first frame
        
        if ( ( bis != null ) && ( bis[ 0 ] != null ) ) {
            setPreferredSize( new Dimension( ( int ) ( bis[ 0 ].getWidth() * zoom ), ( int ) ( bis[ 0 ].getHeight() * zoom ) ) );
        }
        else if ( bis != null ) {
            frames = null;
        }

        // restart the timer with the new delay
        animationTimer.stop();
        animationTimer.setDelay( frameDelay );
        animationTimer.start();
    }
    
    /**
     * Reads and loads all available images from the file using the ImageIO API.
     * 
     * @param f
     *            The file to read.
     * @throws IOException
     *             If the image could not be read.
     * @throws NoSuchElementException
     *             If there were no available ImageReaders for the file.
     */
    public void setFrames( File f ) throws IOException, NoSuchElementException {

        try ( ImageInputStream iis = ImageIO.createImageInputStream( f ) ) {
            Iterator< ImageReader > readers = ImageIO.getImageReaders( iis );
            
            ImageReader reader = readers.next(); // get the image reader
            reader.setInput( iis );
            
            int frameCount = reader.getNumImages( true ); // find the frame count
            
            BufferedImage[] frames = new BufferedImage[ frameCount ]; // all the frames in this image
            
            // read all the images in the file
            for ( int i = 0; i < frameCount; i++ ) {
                BufferedImage read = reader.read( i ); // read the image from the file
                
                if ( i == 0 ) {
                    frames[ i ] = read; // set the frame
                }
                else {
                    // gifs compress by removing unchanged pixels, so we have to add those pixels back in by layering this frame over the previous one

                    BufferedImage previous = frames[ i - 1 ];
                    BufferedImage layered = new BufferedImage( previous.getWidth(), previous.getHeight(), BufferedImage.TYPE_INT_RGB );
                    {
                        Graphics2D grafix = layered.createGraphics();
                        grafix.drawImage( previous, 0, 0, null ); // draw the previous frame
                        
                        // the compressed images are offset to be aligned at the bottom-right corner
                        int x = ( layered.getWidth() - read.getWidth() );
                        int y = ( layered.getHeight() - read.getHeight() );
                        grafix.drawImage( read, x, y, null );
                        
                        grafix.dispose();
                    }
                    frames[ i ] = layered; // set the frame
                }
                
            } // end for

            // read the time between each frame
            int delay;
            try {
                IIOMetadata imageMetaData = reader.getImageMetadata( 0 );
                String metaFormatName = imageMetaData.getNativeMetadataFormatName();
                
                IIOMetadataNode root = ( IIOMetadataNode ) imageMetaData.getAsTree( metaFormatName );
                
                IIOMetadataNode gceNode;
                // find the gceNode
                {
                    int nNodes = root.getLength();
                    for ( int i = 0; i < nNodes; i++ ) {
                        if ( root.item( i ).getNodeName().compareToIgnoreCase( "GraphicsControlExtension" ) == 0 ) {
                            gceNode = ( ( IIOMetadataNode ) root.item( i ) );
                        }
                    }
                    IIOMetadataNode node = new IIOMetadataNode( "GraphicsControlExtension" );
                    root.appendChild( node );
                    
                    gceNode = node;
                }
                
                delay = Integer.parseInt( gceNode.getAttribute( "delayTime" ) ) * 10; // gif delay times are measured in centiseconds, not milliseconds
                // the GIF file format is just filled with completely illogical things, centiseconds, bottom-right alignment, who knows what else?
            }
            catch ( NumberFormatException e ) {
                delay = 100; // the delay couldn't be read
            }
            
            setFrames( frames, delay ); // set the frames and the delay
            setZoom( getZoom() );
        }

    }

    /**
     * Sets the zoom slider for the ImageView. This slider controls the zoom of the image.
     * 
     * @param slider
     *            The new zoom slider.
     */
    public void setZoomSlider( JSlider slider ) {
        // remove the change listener from the previous zoom slider
        if ( zoomSlider != null ) {
            zoomSlider.removeChangeListener( this );
        }
        
        // add the change listener to the new zoom slider
        zoomSlider = slider;
        if ( zoomSlider != null ) zoomSlider.addChangeListener( this );
    }

    //
    // Getters
    //
    
    /**
     * @return The current zoom level.
     */
    public double getZoom() {
        return zoom;
    }
    
    /**
     * @return The JSlider controlling zoom levels.
     */
    public JSlider getZoomSlider() {
        return zoomSlider;
    }

    //
    // Overrides
    //
    
    @Override
    public void paint( Graphics g ) {
        super.paint( g );
        
        BufferedImage frame;
        if ( zoomedFrames != null ) {
            frame = zoomedFrames[ frameIndex ];
        }
        else if ( frames != null ) {
            frame = frames[ frameIndex ];
        }
        else {
            return;
        }
        
        int x = ( getWidth() - frame.getWidth() ) / 2;
        int y = ( getHeight() - frame.getHeight() ) / 2;

        g.drawImage( frame, x, y, this );
    }
    
    @Override
    public void actionPerformed( ActionEvent e ) {
        // go to the next frame
        frameIndex++;
        if ( ( frames == null ) || ( frameIndex >= frames.length ) ) frameIndex = 0; // we've overstepped the number of frames
            
        repaint();
    }
    
    @Override
    public void stateChanged( ChangeEvent evt ) {
        if ( evt.getSource() instanceof JSlider ) {
            JSlider slider = ( JSlider ) evt.getSource();
            
            double newZoom = slider.getValue() / 100.0;
            
            // the user is currently moving the slider
            if ( slider.getValueIsAdjusting() ) {
                if ( Math.abs( newZoom - getZoom() ) > 0.10f ) {
                    setZoom( newZoom );
                }
            }
            else {
                setZoom( newZoom );
            }
        }
    }

    //
    // Nested Classes
    //
    
    /**
     * Zooms the images to the requested size.
     * 
     * @author Austin
     */
    private class ImageZoomer implements Runnable {
        
        //
        // Fields
        //
        
        /** The ImageView whose frames we are modifying. */
        private final ImageView component;
        
        //
        // Constructors
        //
        
        /**
         * Creates a new ImageZoomer for the given ImageView.
         * 
         * @param iv
         *            The ImageView.
         */
        public ImageZoomer( ImageView iv ) {
            component = iv;
        }
        
        //
        // Actions
        //
        
        /**
         * Scales an image to the correct size.
         * 
         * @param bi
         *            The image to scale.
         * @param zoom
         *            The new zoom level of the image.
         * @return The image scaled.
         */
        private BufferedImage scaleImage( BufferedImage bi, double zoom ) {
            int outWidth = ( int ) ( bi.getWidth() * zoom );
            int outHeight = ( int ) ( bi.getHeight() * zoom );
            
            // TODO separate it out into tiles
            
            BufferedImage output = new BufferedImage( outWidth, outHeight, BufferedImage.TYPE_INT_RGB );
            Graphics2D grafix = output.createGraphics();
            {
                grafix.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC );
                grafix.drawImage( bi, 0, 0, outWidth, outHeight, null );
            }
            grafix.dispose();
            
            System.gc();
            
            return output;
        }
        
        //
        // Overrides
        //
        
        @Override
        public void run() {
            
            BufferedImage[] sources;
            double zoom;
            synchronized ( component ) {
                sources = component.frames;
                zoom = component.getZoom();

                // take the shortcut to save time
                if ( zoom == 1.0 ) {
                    component.zoomedFrames = component.frames;
                    return;
                }
                else {
                    component.zoomedFrames = null;
                }
            }
            
            if ( sources == null ) return;

            BufferedImage[] output = new BufferedImage[ sources.length ];
            for ( int i = 0; i < output.length; i++ ) {
                long start = System.currentTimeMillis();
                output[ i ] = scaleImage( sources[ i ], zoom );
                System.out.printf( " Resized %d, took %d millis%n", i, ( System.currentTimeMillis() - start ) );
            }
            
            synchronized ( component ) {
                component.zoomedFrames = output;
            }

            System.out.println( "images sized" );
        }

    }

}
