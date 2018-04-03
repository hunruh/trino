package tiktaalik.trino;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.*;
import com.badlogic.gdx.physics.box2d.*;

/**
 * Primary view class for the game, abstracting the basic graphics calls.
 */
public class Canvas {
	private enum DrawPass {
		INACTIVE, // We are not drawing
		STANDARD, // We are drawing sprites
		DEBUG // We are drawing outlines
	}

	public enum BlendState {
		ALPHA_BLEND, // Alpha blending on, assuming the colors have pre-multipled alpha (DEFAULT)
		NO_PREMULT, // Alpha blending on, assuming the colors have no pre-multipled alpha
		ADDITIVE, // Color values are added together, causing a white-out effect
		OPAQUE // Color values are draw on top of one another with no transparency support
	}	

	private PolygonSpriteBatch spriteBatch; // Drawing context to handle textures AND POLYGONS as sprites
	private ShapeRenderer debugRender; // Rendering context for the debug outlines
	private DrawPass active; // Track whether or not we are active (for error checking)
	private BlendState blend; // The current color blending mode
	private OrthographicCamera camera; // Camera for the underlying SpriteBatch

	// CACHE VARIABLES
	int width; // Value to cache window width (if we are currently full screen)
	int height; // Value to cache window height (if we are currently full screen)
	private Affine2 local; // Affine cache for current sprite to draw
	private Matrix4 global; // Affine cache for all sprites this drawing pass
	private Vector2 vertex;
	private TextureRegion holder; // Cache object to handle raw textures

	/**
	 * Creates a new Canvas determined by the application configuration.
	 */
	public Canvas() {
		active = DrawPass.INACTIVE;
		spriteBatch = new PolygonSpriteBatch();
		debugRender = new ShapeRenderer();
		
		// Set the projection matrix (for proper scaling)
		camera = new OrthographicCamera(getWidth(),getHeight());
		camera.setToOrtho(false);
		spriteBatch.setProjectionMatrix(camera.combined);
		debugRender.setProjectionMatrix(camera.combined);

		// Initialize the cache objects
		holder = new TextureRegion();
		local  = new Affine2();
		global = new Matrix4();
		vertex = new Vector2();
	}
		
    /**
     * Eliminate any resources that should be garbage collected manually.
     */
    public void dispose() {
		if (active != DrawPass.INACTIVE) {
			Gdx.app.error("Canvas", "Cannot dispose while drawing active", new IllegalStateException());
			return;
		}
		spriteBatch.dispose();
    	spriteBatch = null;
    	local  = null;
    	global = null;
    	vertex = null;
    	holder = null;
    }

	/**
	 * Returns the width of this canvas
	 *
	 * @return the width of this canvas
	 */
	public int getWidth() {
		return Gdx.graphics.getWidth();
	}
	
	/**
	 * Changes the width of this canvas
	 *
	 * @param width the canvas width
	 */
	public void setWidth(int width) {
		if (active != DrawPass.INACTIVE) {
			Gdx.app.error("Canvas", "Cannot alter property while drawing active", new IllegalStateException());
			return;
		}
		this.width = width;
		if (!isFullscreen()) {
			Gdx.graphics.setWindowedMode(width, getHeight());
		}
		resize();
	}
	
	/**
	 * Returns the height of this canvas
	 *
	 * @return the height of this canvas
	 */
	public int getHeight() {
		return Gdx.graphics.getHeight();
	}
	
	/**
	 * Changes the height of this canvas
	 *
	 * @param height the canvas height
	 */
	public void setHeight(int height) {
		if (active != DrawPass.INACTIVE) {
			Gdx.app.error("Canvas", "Cannot alter property while drawing active", new IllegalStateException());
			return;
		}
		this.height = height;
		if (!isFullscreen()) {
			Gdx.graphics.setWindowedMode(getWidth(), height);	
		}
		resize();
	}
	
	/**
	 * Returns the dimensions of this canvas
	 *
	 * @return the dimensions of this canvas
	 */
	public Vector2 getSize() {
		return new Vector2(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
	}
	
	/**
	 * Changes the width and height of this canvas
	 *
	 * @param width the canvas width
	 * @param height the canvas height
	 */
	public void setSize(int width, int height) {
		if (active != DrawPass.INACTIVE) {
			Gdx.app.error("Canvas", "Cannot alter property while drawing active", new IllegalStateException());
			return;
		}
		this.width = width;
		this.height = height;
		if (!isFullscreen()) {
			Gdx.graphics.setWindowedMode(width, height);
		}
		resize();

	}

	public OrthographicCamera getCamera(){
		return camera;
	}

	public PolygonSpriteBatch getBatch(){
		return spriteBatch;
	}

	public ShapeRenderer getDebugRender(){
		return debugRender;
	}
	
	/**
	 * Returns whether this canvas is currently fullscreen.
	 *
	 * @return whether this canvas is currently fullscreen.
	 */	 
	public boolean isFullscreen() {
		return Gdx.graphics.isFullscreen(); 
	}
	
	/**
	 * Sets whether or not this canvas should change to fullscreen.
	 *
	 * @param fullscreen Whether this canvas should change to fullscreen.
	 * @param desktop 	 Whether to use the current desktop resolution
	 */	 
	public void setFullscreen(boolean fullscreen, boolean desktop) {
		if (active != DrawPass.INACTIVE) {
			Gdx.app.error("Canvas", "Cannot alter property while drawing active", new IllegalStateException());
			return;
		}
		if (fullscreen) {
			Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
		} else {
			Gdx.graphics.setWindowedMode(width, height);
		}
	}

	/**
	 * Resets the SpriteBatch camera when this canvas is resized.
	 */
	 public void resize() {
		// Resizing screws up the spriteBatch projection matrix
		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, getWidth(), getHeight());
	}
	
	/**
	 * Returns the current color blending state for this canvas.
	 *
	 * @return the current color blending state for this canvas
	 */
	public BlendState getBlendState() {
		return blend;
	}
	
	/**
	 * Sets the color blending state for this canvas.
	 *
	 * @param state the color blending rule
	 */
	public void setBlendState(BlendState state) {
		if (state == blend)
			return;

		switch (state) {
		case NO_PREMULT:
			spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA);
			break;
		case ALPHA_BLEND:
			spriteBatch.setBlendFunction(GL20.GL_ONE,GL20.GL_ONE_MINUS_SRC_ALPHA);
			break;
		case ADDITIVE:
			spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA,GL20.GL_ONE);
			break;
		case OPAQUE:
			spriteBatch.setBlendFunction(GL20.GL_ONE,GL20.GL_ZERO);
			break;
		}
		blend = state;
	}
	
	/**
	 * Clear the screen so we can start a new animation frame
	 */
	public void clear() {
		Gdx.gl.glClearColor(0.39f, 0.58f, 0.93f, 1.0f);  // Homage to the XNA years
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);		
	}

	/**
	 * Start a standard drawing sequence.
	 *
	 * Nothing is flushed to the graphics card until the method end() is called.
	 *
	 * @param affine the global transform apply to the camera
	 */
    public void begin(Affine2 affine) {
		global.setAsAffine(affine);
    	global.mulLeft(camera.combined);
		spriteBatch.setProjectionMatrix(global);
		
		setBlendState(BlendState.NO_PREMULT);
		spriteBatch.begin();
    	active = DrawPass.STANDARD;
    }

	/**
	 * Start a standard drawing sequence.
	 *
	 * Nothing is flushed to the graphics card until the method end() is called.
	 *
	 * @param sx the amount to scale the x-axis
	 * @param sy the amount to scale the y-axis
	 */
    public void begin(float sx, float sy) {
		global.idt();
		global.scl(sx,sy,1.0f);
    	global.mulLeft(camera.combined);
		spriteBatch.setProjectionMatrix(global);
		
    	spriteBatch.begin();
    	active = DrawPass.STANDARD;
    }
    
	/**
	 * Start a standard drawing sequence.
	 *
	 * Nothing is flushed to the graphics card until the method end() is called.
	 */
    public void begin() {
		spriteBatch.setProjectionMatrix(camera.combined);
    	spriteBatch.begin();
    	active = DrawPass.STANDARD;
    }

	/**
	 * Ends a drawing sequence, flushing textures to the graphics card.
	 */
    public void end() {
    	spriteBatch.end();
    	active = DrawPass.INACTIVE;
    }

	/**
	 * Draws the tinted texture at the given position.
	 *
	 * @param image The texture to draw
	 * @param x 	The x-coordinate of the bottom left corner
	 * @param y 	The y-coordinate of the bottom left corner
	 */
	public void draw(Texture image, float x, float y) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("Canvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}

    	spriteBatch.setColor(Color.WHITE);
		spriteBatch.draw(image, x,  y);
	}
	
	/**
	 * Draws the tinted texture at the given position.
	 *
	 * @param image The texture to draw
	 * @param tint  The color tint
	 * @param x 	The x-coordinate of the bottom left corner
	 * @param y 	The y-coordinate of the bottom left corner
	 * @param width	The texture width
	 * @param height The texture height
	 */
	public void draw(Texture image, Color tint, float x, float y, float width, float height) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("Canvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}

    	spriteBatch.setColor(tint);
		spriteBatch.draw(image, x,  y, width, height);
	}
	
	/**
	 * Draws the tinted texture at the given position.
	 *
	 * @param image The texture to draw
	 * @param tint  The color tint
	 * @param ox 	The x-coordinate of texture origin (in pixels)
	 * @param oy 	The y-coordinate of texture origin (in pixels)
	 * @param x 	The x-coordinate of the texture origin (on screen)
	 * @param y 	The y-coordinate of the texture origin (on screen)
	 * @param width	The texture width
	 * @param height The texture height
	 */
	public void draw(Texture image, Color tint, float ox, float oy, float x, float y, float width, float height) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("Canvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		
		// Call the master drawing method (more efficient than base method)
		holder.setRegion(image);
		draw(holder, tint, x-ox, y-oy, width, height);
	}


	/**
	 * Draws the tinted texture with the given transformations
	 *
	 * @param image The texture to draw
	 * @param tint  The color tint
	 * @param ox 	The x-coordinate of texture origin (in pixels)
	 * @param oy 	The y-coordinate of texture origin (in pixels)
	 * @param x 	The x-coordinate of the texture origin (on screen)
	 * @param y 	The y-coordinate of the texture origin (on screen)
	 * @param angle The rotation angle (in degrees) about the origin.
	 * @param sx 	The x-axis scaling factor
	 * @param sy 	The y-axis scaling factor
	 */	
	public void draw(Texture image, Color tint, float ox, float oy, 
					float x, float y, float angle, float sx, float sy) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("Canvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		
		// Call the master drawing method (more efficient than base method)
		holder.setRegion(image);
		draw(holder,tint,ox,oy,x,y,angle,sx,sy);
	}
	
	/**
	 * Draws the tinted texture with the given transformations
	 *
	 * @param image The texture to draw
	 * @param tint  The color tint
	 * @param ox 	The x-coordinate of texture origin (in pixels)
	 * @param oy 	The y-coordinate of texture origin (in pixels)
	 * @param transform  The image transform
	 */	
	public void draw(Texture image, Color tint, float ox, float oy, Affine2 transform) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("Canvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		
		// Call the master drawing method (we have to for transforms)
		holder.setRegion(image);
		draw(holder,tint,ox,oy,transform);
	}
	
	/**
	 * Draws the tinted texture region (filmstrip) at the given position.
	 *
	 * @param region The texture to draw
	 * @param x 	The x-coordinate of the bottom left corner
	 * @param y 	The y-coordinate of the bottom left corner
	 */
	public void draw(TextureRegion region, float x, float y) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("Canvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}

    	spriteBatch.setColor(Color.WHITE);
		spriteBatch.draw(region, x,  y);
	}

	/**
	 * Draws the tinted texture at the given position.
	 *
	 * @param region The texture to draw
	 * @param tint  The color tint
	 * @param x 	The x-coordinate of the bottom left corner
	 * @param y 	The y-coordinate of the bottom left corner
	 * @param width	The texture width
	 * @param height The texture height
	 */
	public void draw(TextureRegion region, Color tint, float x, float y, float width, float height) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("Canvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}

    	spriteBatch.setColor(tint);
		spriteBatch.draw(region, x,  y, width, height);
	}
	
	/**
	 * Draws the tinted texture at the given position.
	 *
	 * @param region The texture to draw
	 * @param tint  The color tint
	 * @param ox 	The x-coordinate of texture origin (in pixels)
	 * @param oy 	The y-coordinate of texture origin (in pixels)
	 * @param x 	The x-coordinate of the texture origin (on screen)
	 * @param y 	The y-coordinate of the texture origin (on screen)
	 * @param width	The texture width
	 * @param height The texture height
	 */	
	public void draw(TextureRegion region, Color tint, float ox, float oy, float x, float y, float width, float height) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("Canvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}

    	spriteBatch.setColor(tint);
		spriteBatch.draw(region, x-ox, y-oy, width, height);
	}

	/**
	 * Draws the tinted texture region (filmstrip) with the given transformations
	 *
	 * @param region The texture to draw
	 * @param tint  The color tint
	 * @param ox 	The x-coordinate of texture origin (in pixels)
	 * @param oy 	The y-coordinate of texture origin (in pixels)
	 * @param x 	The x-coordinate of the texture origin (on screen)
	 * @param y 	The y-coordinate of the texture origin (on screen)
	 * @param angle The rotation angle (in degrees) about the origin.
	 * @param sx 	The x-axis scaling factor
	 * @param sy 	The y-axis scaling factor
	 */	
	public void draw(TextureRegion region, Color tint, float ox, float oy, 
					 float x, float y, float angle, float sx, float sy) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("Canvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}

		// BUG: The draw command for texture regions does not work properly.
		// There is a workaround, but it will break if the bug is fixed.
		// For now, it is better to set the affine transform directly.
		computeTransform(ox,oy,x,y,angle,sx,sy);
		spriteBatch.setColor(tint);
		spriteBatch.draw(region, region.getRegionWidth(), region.getRegionHeight(), local);
	}

	/**
	 * Draws the tinted texture with the given transformations
	 *
	 * @param region 	The region to draw
	 * @param tint  	The color tint
	 * @param ox 		The x-coordinate of texture origin (in pixels)
	 * @param oy 		The y-coordinate of texture origin (in pixels)
	 * @param affine  	The image transform
	 */	
	public void draw(TextureRegion region, Color tint, float ox, float oy, Affine2 affine) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("Canvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}

		local.set(affine);
		local.translate(-ox,-oy);				
		spriteBatch.setColor(tint);
		spriteBatch.draw(region, region.getRegionWidth(), region.getRegionHeight(), local);
	}

	/**
	 * Draws the polygonal region with the given transformations
	 *
	 * @param region The polygon to draw
	 * @param x 	The x-coordinate of the bottom left corner
	 * @param y 	The y-coordinate of the bottom left corner
	 */	
	public void draw(PolygonRegion region, float x, float y) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("Canvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}

    	spriteBatch.setColor(Color.WHITE);
		spriteBatch.draw(region, x,  y);
	}
	
	/**
	 * Draws the polygonal region with the given transformations
	 *
	 * @param region The polygon to draw
	 * @param tint  The color tint
	 * @param x 	The x-coordinate of the bottom left corner
	 * @param y 	The y-coordinate of the bottom left corner
	 * @param width	The texture width
	 * @param height The texture height
	 */	
	public void draw(PolygonRegion region, Color tint, float x, float y, float width, float height) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("Canvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}

    	spriteBatch.setColor(tint);
		spriteBatch.draw(region, x,  y, width, height);
	}
	
	/**
	 * Draws the polygonal region with the given transformations
	 *
	 * @param region The polygon to draw
	 * @param tint  The color tint
	 * @param ox 	The x-coordinate of texture origin (in pixels)
	 * @param oy 	The y-coordinate of texture origin (in pixels)
	 * @param x 	The x-coordinate of the texture origin (on screen)
	 * @param y 	The y-coordinate of the texture origin (on screen)
	 * @param width	The texture width
	 * @param height The texture height
	 */	
	public void draw(PolygonRegion region, Color tint, float ox, float oy, float x, float y, float width, float height) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("Canvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}

    	spriteBatch.setColor(tint);
		spriteBatch.draw(region, x-ox, y-oy, width, height);
	}
	
	/**
	 * Draws the polygonal region with the given transformations
	 *
	 * @param region The polygon to draw
	 * @param tint  The color tint
	 * @param ox 	The x-coordinate of texture origin (in pixels)
	 * @param oy 	The y-coordinate of texture origin (in pixels)
	 * @param x 	The x-coordinate of the texture origin (on screen)
	 * @param y 	The y-coordinate of the texture origin (on screen)
	 * @param angle The rotation angle (in degrees) about the origin.
	 * @param sx 	The x-axis scaling factor
	 * @param sy 	The y-axis scaling factor
	 */	
	public void draw(PolygonRegion region, Color tint, float ox, float oy, 
					 float x, float y, float angle, float sx, float sy) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("Canvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		
		TextureRegion bounds = region.getRegion();
		spriteBatch.setColor(tint);
		spriteBatch.draw(region, x, y, ox, oy, 
						 bounds.getRegionWidth(), bounds.getRegionHeight(), 
						 sx, sy, 180.0f*angle/(float)Math.PI);
	}

	/**
	 * Draws the polygonal region with the given transformations
	 *
	 * @param region 	The polygon to draw
	 * @param tint  	The color tint
	 * @param ox 		The x-coordinate of texture origin (in pixels)
	 * @param oy 		The y-coordinate of texture origin (in pixels)
	 * @param affine  	The image transform
	 */	
	public void draw(PolygonRegion region, Color tint, float ox, float oy, Affine2 affine) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("Canvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}

		local.set(affine);
		local.translate(-ox,-oy);
		computeVertices(local,region.getVertices());

		spriteBatch.setColor(tint);
		spriteBatch.draw(region, 0, 0);
		
		// Invert and restore
		local.inv();
		computeVertices(local,region.getVertices());
	}
	
	/**
	 * Transform the given vertices by the affine transform
	 */
	private void computeVertices(Affine2 affine, float[] vertices) {
		for(int ii = 0; ii < vertices.length; ii += 2) {
			vertex.set(vertices[2*ii], vertices[2*ii+1]);
			affine.applyTo(vertex);
			vertices[2*ii  ] = vertex.x;
			vertices[2*ii+1] = vertex.y;
		}
	}

    /**
     * Draws text on the screen.
     *
     * @param text The string to draw
     * @param font The font to use
     * @param x The x-coordinate of the lower-left corner
     * @param y The y-coordinate of the lower-left corner
     */
    public void drawText(String text, BitmapFont font, float x, float y) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("Canvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		GlyphLayout layout = new GlyphLayout(font,text);
		font.draw(spriteBatch, layout, x, y);
    }

    /**
     * Draws text centered on the screen.
     *
     * @param text The string to draw
     * @param font The font to use
     * @param offset The y-value offset from the center of the screen.
     */
    public void drawTextCentered(String text, BitmapFont font, float offset) {
		if (active != DrawPass.STANDARD) {
			Gdx.app.error("Canvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		
		GlyphLayout layout = new GlyphLayout(font,text);
		float x = (getWidth()  - layout.width) / 2.0f;
		float y = (getHeight() + layout.height) / 2.0f;
		font.draw(spriteBatch, layout, x, y+offset);
    }
    
	/**
	 * Start the debug drawing sequence.
	 *
	 * Nothing is flushed to the graphics card until the method end() is called.
	 *
	 * @param affine the global transform apply to the camera
	 */
    public void beginDebug(Affine2 affine) {
		global.setAsAffine(affine);
    	global.mulLeft(camera.combined);
    	debugRender.setProjectionMatrix(global);
		
    	debugRender.begin(ShapeRenderer.ShapeType.Line);
    	active = DrawPass.DEBUG;
    }
    
	/**
	 * Start the debug drawing sequence.
	 *
	 * Nothing is flushed to the graphics card until the method end() is called.
	 *
	 * @param sx the amount to scale the x-axis
	 * @param sy the amount to scale the y-axis
	 */    
    public void beginDebug(float sx, float sy) {
		global.idt();
		global.scl(sx,sy,1.0f);
    	global.mulLeft(camera.combined);
    	debugRender.setProjectionMatrix(global);
		
    	debugRender.begin(ShapeRenderer.ShapeType.Line);
    	active = DrawPass.DEBUG;
    }

	/**
	 * Start the debug drawing sequence.
	 *
	 * Nothing is flushed to the graphics card until the method end() is called.
	 */
    public void beginDebug() {
    	debugRender.setProjectionMatrix(camera.combined);
    	debugRender.begin(ShapeRenderer.ShapeType.Filled);
    	debugRender.setColor(Color.RED);
    	debugRender.circle(0, 0, 10);
    	debugRender.end();
    	
    	debugRender.begin(ShapeRenderer.ShapeType.Line);
    	active = DrawPass.DEBUG;
    }

	/**
	 * Ends the debug drawing sequence, flushing textures to the graphics card.
	 */
    public void endDebug() {
    	debugRender.end();
    	active = DrawPass.INACTIVE;
    }
    
    /**
     * Draws the outline of the given shape in the specified color
     *
     * @param shape The Box2d shape
     * @param color The outline color
     * @param x  The x-coordinate of the shape position
     * @param y  The y-coordinate of the shape position
     */
    public void drawPhysics(PolygonShape shape, Color color, float x, float y) {
		if (active != DrawPass.DEBUG) {
			Gdx.app.error("Canvas", "Cannot draw without active beginDebug()", new IllegalStateException());
			return;
		}
		
    	float x0, y0, x1, y1;
    	debugRender.setColor(color);
    	for(int ii = 0; ii < shape.getVertexCount()-1; ii++) {
    		shape.getVertex(ii  ,vertex);
    		x0 = x+vertex.x; y0 = y+vertex.y;
    		shape.getVertex(ii+1,vertex);
    		x1 = x+vertex.x; y1 = y+vertex.y;
    		debugRender.line(x0, y0, x1, y1);
    	}
    	// Close the loop
		shape.getVertex(shape.getVertexCount()-1,vertex);
		x0 = x+vertex.x; y0 = y+vertex.y;
		shape.getVertex(0,vertex);
		x1 = x+vertex.x; y1 = y+vertex.y;
		debugRender.line(x0, y0, x1, y1);
    }

    /**
     * Draws the outline of the given shape in the specified color
     *
     * @param shape The Box2d shape
     * @param color The outline color
     * @param x  The x-coordinate of the shape position
     * @param y  The y-coordinate of the shape position
     * @param angle  The shape angle of rotation
     */
    public void drawPhysics(PolygonShape shape, Color color, float x, float y, float angle) {
		if (active != DrawPass.DEBUG) {
			Gdx.app.error("Canvas", "Cannot draw without active beginDebug()", new IllegalStateException());
			return;
		}
		
		local.setToTranslation(x,y);
		local.rotateRad(angle);
		
    	float x0, y0, x1, y1;
    	debugRender.setColor(color);
    	for(int ii = 0; ii < shape.getVertexCount()-1; ii++) {
    		shape.getVertex(ii  ,vertex);
    		local.applyTo(vertex);
    		x0 = vertex.x; y0 = vertex.y;
    		shape.getVertex(ii+1,vertex);
    		local.applyTo(vertex);
    		x1 = vertex.x; y1 = vertex.y;
    		debugRender.line(x0, y0, x1, y1);
    	}
    	// Close the loop
		shape.getVertex(shape.getVertexCount()-1,vertex);
		local.applyTo(vertex);
		x0 = vertex.x; y0 = vertex.y;
		shape.getVertex(0,vertex);
		local.applyTo(vertex);
		x1 = vertex.x; y1 = vertex.y;
		debugRender.line(x0, y0, x1, y1);
    }

    /**
     * Draws the outline of the given shape in the specified color
     *
     * @param shape The Box2d shape
     * @param color The outline color
     * @param x  The x-coordinate of the shape position
     * @param y  The y-coordinate of the shape position
     * @param angle  The shape angle of rotation
     * @param sx The amount to scale the x-axis
     * @param sx The amount to scale the y-axis
     */
    public void drawPhysics(PolygonShape shape, Color color, float x, float y, float angle, float sx, float sy) {
		if (active != DrawPass.DEBUG) {
			Gdx.app.error("Canvas", "Cannot draw without active beginDebug()", new IllegalStateException());
			return;
		}
		
		local.setToScaling(sx,sy);
		local.translate(x,y);
		local.rotateRad(angle);
		
    	float x0, y0, x1, y1;
    	debugRender.setColor(color);
    	for(int ii = 0; ii < shape.getVertexCount()-1; ii++) {
    		shape.getVertex(ii  ,vertex);
    		local.applyTo(vertex);
    		x0 = vertex.x; y0 = vertex.y;
    		shape.getVertex(ii+1,vertex);
    		local.applyTo(vertex);
    		x1 = vertex.x; y1 = vertex.y;
    		debugRender.line(x0, y0, x1, y1);
    	}
    	// Close the loop
		shape.getVertex(shape.getVertexCount()-1,vertex);
		local.applyTo(vertex);
		x0 = vertex.x; y0 = vertex.y;
		shape.getVertex(0,vertex);
		local.applyTo(vertex);
		x1 = vertex.x; y1 = vertex.y;
		debugRender.line(x0, y0, x1, y1);
    }
    
    /** 
     * Draws the outline of the given shape in the specified color
     * 
     * @param shape The Box2d shape
     * @param color The outline color
     * @param x  The x-coordinate of the shape position
     * @param y  The y-coordinate of the shape position
     */
    public void drawPhysics(CircleShape shape, Color color, float x, float y) {
		if (active != DrawPass.DEBUG) {
			Gdx.app.error("Canvas", "Cannot draw without active beginDebug()", new IllegalStateException());
			return;
		}
		
    	debugRender.setColor(color);
    	debugRender.circle(x, y, shape.getRadius(),12);
    }
    
    /** 
     * Draws the outline of the given shape in the specified color
     * 
     * @param shape The Box2d shape
     * @param color The outline color
     * @param x  The x-coordinate of the shape position
     * @param y  The y-coordinate of the shape position
     * @param sx The amount to scale the x-axis
     * @param sy The amount to scale the y-axis
     */
    public void drawPhysics(CircleShape shape, Color color, float x, float y, float sx, float sy) {
		if (active != DrawPass.DEBUG) {
			Gdx.app.error("Canvas", "Cannot draw without active beginDebug()", new IllegalStateException());
			return;
		}
		
		float x0 = x*sx;
		float y0 = y*sy;
		float w = shape.getRadius()*sx;
		float h = shape.getRadius()*sy;
    	debugRender.setColor(color);
    	debugRender.ellipse(x0-w, y0-h, 2*w, 2*h, 12);
    }
    
	/**
	 * Compute the affine transform (and store it in local) for this image.
	 * 
	 * @param ox 	The x-coordinate of texture origin (in pixels)
	 * @param oy 	The y-coordinate of texture origin (in pixels)
	 * @param x 	The x-coordinate of the texture origin (on screen)
	 * @param y 	The y-coordinate of the texture origin (on screen)
	 * @param angle The rotation angle (in degrees) about the origin.
	 * @param sx 	The x-axis scaling factor
	 * @param sy 	The y-axis scaling factor
	 */
	private void computeTransform(float ox, float oy, float x, float y, float angle, float sx, float sy) {
		local.setToTranslation(x,y);
		local.rotate(180.0f*angle/(float)Math.PI);
		local.scale(sx,sy);
		local.translate(-ox,-oy);
	}
}