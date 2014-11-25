package edu.umn.d.grenoble.mhcs.client;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import edu.umn.d.grenoble.mhcs.bus.AreaClickEvent;
import edu.umn.d.grenoble.mhcs.bus.AreaUpdateEvent;
import edu.umn.d.grenoble.mhcs.bus.AreaUpdateEventHandler;
import edu.umn.d.grenoble.mhcs.bus.Bus;
import edu.umn.d.grenoble.mhcs.modules.Area;
import edu.umn.d.grenoble.mhcs.modules.Module;
import edu.umn.d.grenoble.mhcs.modules.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Renders the landing area and the locations of the modules.
 * @author Scott Redig
 * @author Justin Pieper
 * @author Paul Rodysill
 */
public class AreaRenderer {
    
 /* Initialization */
    
    private static final int tileSize = 40;
    private Canvas canvas;
    private int imagesRemaining;
    private Map<String, ImageElement> images = new HashMap<String, ImageElement>();
    private String background = "images/MarsModuleLandingArea.jpg";
    
    public AreaRenderer(final MarsHabitatConfigurationSystem mhcs) {
        // Preload images
        List<String> toLoad = new ArrayList<String>();
        for ( Type t : Type.values() ) {
            String url = t.getImageUrl();
            if (url != null) {
                toLoad.add(url);
            }
        }
        toLoad.add(this.background);
        
        this.imagesRemaining = toLoad.size();
        
        for (final String filePath : toLoad) {
            final Image img = new Image(filePath);
            final ImageElement imgHandler = ImageElement.as(img.getElement());
            this.images.put(filePath, imgHandler);
            img.addErrorHandler(new ErrorHandler(){
                @Override
                public void onError(final ErrorEvent event) {
                    throw new Error("Image failed to load: " + filePath);
                }
            });
            final AreaRenderer areaRenderer = this;
            img.addLoadHandler(new LoadHandler(){
                public void onLoad(final LoadEvent event) {
                    areaRenderer.imagesRemaining -= 1;
                    if (areaRenderer.imagesRemaining <= 0) {
                        mhcs.Begin();
                    }
                }
            });
            img.setVisible(false);
            RootPanel.get().add(img);
        }
        
        this.canvas = Canvas.createIfSupported();
        if (this.canvas == null) {
            return;
        }
        
        final String px = "px";
        this.canvas.setSize(tileSize * Area.Width + px, tileSize * Area.Height + px);
        this.canvas.setCoordinateSpaceWidth(tileSize * Area.Width);
        this.canvas.setCoordinateSpaceHeight(tileSize * Area.Height);
        
        final AreaRenderer areaRenderer = this;
        
        Bus.bus.addHandler(AreaUpdateEvent.TYPE, new AreaUpdateEventHandler(){
            @Override
            public void onEvent(final AreaUpdateEvent event) {
                areaRenderer.RenderArea(event.getArea());
            }            
        });
        
        this.canvas.addClickHandler(new ClickHandler(){

            @Override
            public void onClick(final ClickEvent event) {
                int x = event.getRelativeX(areaRenderer.canvas.getElement()) / AreaRenderer.tileSize + 1;
                int y = Area.Height - event.getRelativeY(areaRenderer.canvas.getElement()) / AreaRenderer.tileSize;
                Bus.bus.fireEvent(new AreaClickEvent(x, y));
            }            
        });
        
    }
    
 /* Methods */
    
    /**
     * Getter (Accessor) for the canvas representation of the landing area.
     */
    public Canvas GetCanvas() {
        return this.canvas;
    }
    
    /**
     * Adds the modules to the canvas.
     * @param area - The canvas that the modules are to be added to
     */
    private void RenderArea(final Area area) {
        Context2d context = this.canvas.getContext2d();
        
        context.setFillStyle("#444444");
        context.drawImage(this.images.get(this.background), 0, 0, Area.Width * tileSize, Area.Height * tileSize);
        
        for (Module module : area.getModules()) {
            context.drawImage(
                    this.images.get(module.getType().getImageUrl()),
                    (module.getX() - 1) * tileSize, 
                    (Area.Height - module.getY()) * tileSize,
                    tileSize, tileSize);
        }
        
    }
    
}
