/*
 * GameController.java
 *
 * This is the most important new class in this lab.  This class serves as a combination 
 * of the CollisionController and GameplayController from the previous lab.  There is not 
 * much to do for collisions; Box2d takes care of all of that for us.  This controller 
 * invokes Box2d and then performs any after the fact modifications to the data 
 * (e.g. gameplay).
 *
 * If you study this class, and the contents of the edu.cornell.cs3152.physics.obstacles
 * package, you should be able to understand how the Physics engine works.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package tiktaalik.trino;

import java.util.Iterator;

import com.badlogic.gdx.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import tiktaalik.trino.duggi.*;
import tiktaalik.trino.enemy.Enemy;
import tiktaalik.trino.resources.Wall;
import tiktaalik.trino.resources.CottonFlower;
import tiktaalik.util.*;

/**
 * Base class for a world-specific controller.
 *
 *
 * A world has its own objects, assets, and input controller.  Thus this is 
 * really a mini-GameEngine in its own right.  The only thing that it does
 * not do is create a Canvas; that is shared with the main application.
 *
 * You will notice that asset loading is not done with static methods this time.  
 * Instance asset loading makes it easier to process our game modes in a loop, which 
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public class GameController implements ContactListener, Screen {
	/** 
	 * Tracks the asset state.  Otherwise subclasses will try to load assets 
	 */
	protected enum AssetState {
		/** No assets loaded */
		EMPTY,
		/** Still loading assets */
		LOADING,
		/** Assets are complete */
		COMPLETE
	}

	/** Track asset loading from all instances and subclasses */
	private AssetState worldAssetState = AssetState.EMPTY;
	/** Track all loaded assets (for unloading purposes) */
	private Array<String> assets;

	// ASSET FILES AND VARIABLES
	/** The texture file for general assets */
	private static String EARTH_FILE = "shared/earthtile.png";
	private static String GOAL_FILE = "shared/goaldoor.png";
	private static String FONT_FILE = "shared/RetroGame.ttf";
	private static int FONT_SIZE = 64;

	/** The texture files for the three dinosaurs (no animation) */
	private static final String DOLL_FILE  = "trino/doll.png";
	private static final String HERBIVORE_FILE  = "trino/herbivore.png";
	private static final String CARNIVORE_FILE  = "trino/carnivore.png";
	private static final String ENEMY_FILE = "trino/enemy.png";
	private static final String WALL_FILE = "trino/wall.png";
	private static final String EDIBLE_WALL_FILE = "trino/ediblewall.png";
	private static final String COTTON_FLOWER_FILE = "trino/cotton.png";
	private static final String PATH_FILE = "trino/path.png";

	private static final int COTTON = 0;
	private static final int EDIBLEWALL = 1;
	private static final int STUNNEDENEMY = 2;
	private static final int WALL = 3;
	private static final int ENEMY = 4;
	private static final int GOAL = 5;
	private static final int DUGGI = 6;
	private static final int CLONE = 7;

	/** Texture assets for the general game */
	private TextureRegion earthTile;
	private TextureRegion goalTile;
	private BitmapFont displayFont;

	/** Texture assets for Duggi's three forms */
	private TextureRegion dollTexture;
	private TextureRegion herbivoreTexture;
	private TextureRegion carnivoreTexture;

	/* Texture assets for enemies */
	private TextureRegion enemyTexture;

	/* Texture assets for other world attributes */
	private TextureRegion wallTexture;
	private TextureRegion edibleWallTexture;
	private TextureRegion cottonTexture;
	private TextureRegion pathTexture;

	//index of the object Duggi collided with
	private GameObject collidedWith;
	private int collidedType;

	// GAME CONSTANTS AND VARIABLES
	/** Constants for initialization */
	private float enemyVertical = 10.0f;
	private static final float  BASIC_DENSITY = 0.0f;
	private static final float  BASIC_FRICTION = 0.4f;
	private static final float  BASIC_RESTITUTION = 0.1f;
//	private static final float[][] WALL1 = {{ 0.0f, 5.0f, 0.0f, 18.0f, 2.5f, 18.0f, 2.5f, 5.0f},
//			{ 0.0f, 0.0f, 30.0f, 0.0f, 30.0f, 2.5f, 0.0f, 2.5f},
//			{10.0f, 2.5f, 29.5f, 2.5f, 29.5f, 5.0f, 10.0f, 5.0f},
//			{29.5f, 0.0f,32.0f, 0.0f,32.0f, 11.5f,29.5f, 11.5f},
//			{29.5f, 18.0f,32.0f, 18.0f,32.0f, 14.0f,29.5f, 14.0f},
//			{10.0f, 5.0f,12.5f, 5.0f,12.5f, 14.0f,10.0f, 14.0f},
//			{7.5f,5.0f,10.0f,5.0f,10.0f, 14.0f,7.5f, 14.0f},
//			{12.5f,14.0f,15.5f,14.0f,15.5f,11.5f,12.5f,11.5f},
//			{18.0f,18.0f,25.0f,18.0f,25.0f,7.5f,18f,7.5f},
//			{ 2.5f,18.0f, 18.0f,18.0f, 18.0f,16.5f, 2.5f,16.5f},
//			{ 25.0f,18.0f, 29.5f,18.0f, 29.5f,16.5f, 25.0f,16.5f},
//	};

	// Other game objects
	/** The goal door position */
	private static Vector2 GOAL_POS = new Vector2(2.0f,15.0f);
	/** The position of the spinning barrier */
	private static Vector2 SPIN_POS = new Vector2(13.0f,12.5f);
	/** The initial position of the dude */
	private static Vector2 DUDE_POS = new Vector2(1.0f, 16.0f);
	/** The initial position of the enemy */
	private static Vector2 ENEMY_POS = new Vector2(28.25f, 5.0f);

	/** Exit code for quitting the game */
	public static final int EXIT_QUIT = 0;
	/** Exit code for advancing to next level */
	public static final int EXIT_NEXT = 1;
	/** Exit code for jumping back to previous level */
	public static final int EXIT_PREV = 2;
	/** How many frames after winning/losing do we continue? */
	private static final int EXIT_COUNT = 120;

	/** The amount of time for a physics engine step. */
	private static final float WORLD_STEP = 1/60.0f;
	/** Number of velocity iterations for the constrain solvers */
	private static final int WORLD_VELOC = 6;
	/** Number of position iterations for the constrain solvers */
	private static final int WORLD_POSIT = 2;

	/** Width of the game world in Box2d units */
	private static final float DEFAULT_WIDTH  = 32.0f;
	/** Height of the game world in Box2d units */
	private static final float DEFAULT_HEIGHT = 18.0f;
	/** The default value of gravity (going down) */
	private static final float DEFAULT_GRAVITY = -0.0f;

	/** Reference to the game canvas */
	protected Canvas canvas;
	/** All the objects in the world. */
	protected PooledList<GameObject> objects  = new PooledList<GameObject>();

	protected AIController[] controls;
	/** Queue for adding objects */
	private PooledList<GameObject> addQueue = new PooledList<GameObject>();
	private PooledList<Wall> walls = new PooledList<Wall>();
	private PooledList<CottonFlower> cottonFlower = new PooledList<CottonFlower>();
	private PooledList<Enemy> enemies = new PooledList<Enemy>();
	/** Listener that will update the player mode when we are done */
	private ScreenListener listener;

	/** The Box2D world */
	private World world;
	/** The boundary of the world */
	private Rectangle bounds;
	/** The world scale */
	private Vector2 scale;

	/** Whether or not this is an active controller */
	private boolean active;
	/** Whether we have completed this level */
	private boolean complete;
	/** Whether we have failed at this world (and need a reset) */
	private boolean failed;
	/** Countdown active for winning or losing */
	private int countdown;

	private Dinosaur avatar; // Reference to the character avatar
	private int avatarIdx; // Position of the character avatar in the objects list
	/** Reference to the enemy avatar */
	private Enemy enemy;
	/** Reference to the goalDoor (for collision detection) */
	private Wall goalDoor;


	// Variables for the enemy model
	private Vector2 cachePosition1 = new Vector2(0,0);
	private Vector2 cachePosition2 = new Vector2(0,0);
	private Vector2 cacheDirection = new Vector2(0,0);
	private float cacheDistance = 0;
	private boolean enemyMoving = false;
	private boolean goBackToStart = false;
	private boolean goToEnd = true;

	private float enemySpeed = 5;
	private float elapsed = 0.01f;

	/** Mark set to handle more sophisticated collision callbacks */
	private ObjectSet<Fixture> sensorFixtures;

	/**
	 * Preloads the assets for this controller.
	 *
	 * To make the game modes more for-loop friendly, we opted for nonstatic loaders
	 * this time.  However, we still want the assets themselves to be static.  So
	 * we have an AssetState that determines the current loading state.  If the
	 * assets are already loaded, this method will do nothing.
	 * 
	 * @param manager Reference to global asset manager.
	 */
	public void preLoadContent(AssetManager manager) {
		if (worldAssetState != AssetState.EMPTY) {
			return;
		}
		
		worldAssetState = AssetState.LOADING;
		// Load the shared tiles.
		manager.load(EARTH_FILE,Texture.class);
		assets.add(EARTH_FILE);
		manager.load(GOAL_FILE,Texture.class);
		assets.add(GOAL_FILE);
		
		// Load the font
		FreetypeFontLoader.FreeTypeFontLoaderParameter size2Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
		size2Params.fontFileName = FONT_FILE;
		size2Params.fontParameters.size = FONT_SIZE;
		manager.load(FONT_FILE, BitmapFont.class, size2Params);
		assets.add(FONT_FILE);

		// Load Trino files
		manager.load(DOLL_FILE, Texture.class);
		assets.add(DOLL_FILE);
		manager.load(HERBIVORE_FILE, Texture.class);
		assets.add(HERBIVORE_FILE);
		manager.load(CARNIVORE_FILE, Texture.class);
		assets.add(CARNIVORE_FILE);
		manager.load(WALL_FILE, Texture.class);
		assets.add(WALL_FILE);
		manager.load(EDIBLE_WALL_FILE, Texture.class);
		assets.add(EDIBLE_WALL_FILE);
		manager.load(COTTON_FLOWER_FILE, Texture.class);
		assets.add(COTTON_FLOWER_FILE);
		manager.load(ENEMY_FILE, Texture.class);
		assets.add(ENEMY_FILE);

		manager.load(PATH_FILE, Texture.class);
		assets.add(PATH_FILE);
	}

	/**
	 * Loads the assets for this controller.
	 *
	 * To make the game modes more for-loop friendly, we opted for nonstatic loaders
	 * this time.  However, we still want the assets themselves to be static.  So
	 * we have an AssetState that determines the current loading state.  If the
	 * assets are already loaded, this method will do nothing.
	 * 
	 * @param manager Reference to global asset manager.
	 */
	public void loadContent(AssetManager manager) {
		if (worldAssetState != AssetState.LOADING) {
			return;
		}
		
		// Allocate the tiles
		earthTile = createTexture(manager,EARTH_FILE,true);
		goalTile  = createTexture(manager,GOAL_FILE,true);
		
		// Allocate the font
		if (manager.isLoaded(FONT_FILE)) {
			displayFont = manager.get(FONT_FILE,BitmapFont.class);
		} else {
			displayFont = null;
		}

		dollTexture = createTexture(manager,DOLL_FILE,false);
		herbivoreTexture = createTexture(manager,HERBIVORE_FILE,false);
		carnivoreTexture = createTexture(manager,CARNIVORE_FILE,false);
		enemyTexture = createTexture(manager,ENEMY_FILE, false);
		wallTexture = createTexture(manager,WALL_FILE,false);
		edibleWallTexture = createTexture(manager, EDIBLE_WALL_FILE, false);
		cottonTexture = createTexture(manager, COTTON_FLOWER_FILE, false);
		pathTexture = createTexture(manager,PATH_FILE,false);

		worldAssetState = AssetState.COMPLETE;
	}

	/**
	 * Returns a newly loaded texture region for the given file.
	 *
	 * This helper methods is used to set texture settings (such as scaling, and
	 * whether or not the texture should repeat) after loading.
	 *
	 * @param manager 	Reference to global asset manager.
	 * @param file		The texture (region) file
	 * @param repeat	Whether the texture should be repeated
	 *
	 * @return a newly loaded texture region for the given file.
	 */
	private TextureRegion createTexture(AssetManager manager, String file, boolean repeat) {
		if (manager.isLoaded(file)) {
			TextureRegion region = new TextureRegion(manager.get(file, Texture.class));
			region.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
			if (repeat) {
				region.getTexture().setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
			}
			return region;
		}
		return null;
	}
	
	/**
	 * Returns a newly loaded filmstrip for the given file.
	 *
	 * This helper methods is used to set texture settings (such as scaling, and
	 * the number of animation frames) after loading.
	 *
	 * @param manager 	Reference to global asset manager.
	 * @param file		The texture (region) file
	 * @param rows 		The number of rows in the filmstrip
	 * @param cols 		The number of columns in the filmstrip
	 * @param size 		The number of frames in the filmstrip
	 *
	 * @return a newly loaded texture region for the given file.
	 */
	protected FilmStrip createFilmStrip(AssetManager manager, String file, int rows, int cols, int size) {
		if (manager.isLoaded(file)) {
			FilmStrip strip = new FilmStrip(manager.get(file, Texture.class),rows,cols,size);
			strip.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
			return strip;
		}
		return null;
	}
	
	/** 
	 * Unloads the assets for this game.
	 * 
	 * This method erases the static variables.  It also deletes the associated textures 
	 * from the asset manager. If no assets are loaded, this method does nothing.
	 * 
	 * @param manager Reference to global asset manager.
	 */
	public void unloadContent(AssetManager manager) {
    	for(String s : assets) {
    		if (manager.isLoaded(s)) {
    			manager.unload(s);
    		}
    	}
	}

	/**
	 * Returns true if the level is completed.
	 *
	 * If true, the level will advance after a countdown
	 *
	 * @return true if the level is completed.
	 */
	public boolean isComplete( ) {
		return complete;
	}

	/**
	 * Sets whether the level is completed.
	 *
	 * If true, the level will advance after a countdown
	 *
	 * @param value whether the level is completed.
	 */
	public void setComplete(boolean value) {
		if (value) {
			countdown = EXIT_COUNT;
		}
		complete = value;
	}

	/**
	 * Returns true if the level is failed.
	 *
	 * If true, the level will reset after a countdown
	 *
	 * @return true if the level is failed.
	 */
	public boolean isFailure( ) {
		return failed;
	}

	/**
	 * Sets whether the level is failed.
	 *
	 * If true, the level will reset after a countdown
	 *
	 * @param value whether the level is failed.
	 */
	public void setFailure(boolean value) {
		if (value) {
			countdown = EXIT_COUNT;
		}
		failed = value;
	}
	
	/**
	 * Returns true if this is the active screen
	 *
	 * @return true if this is the active screen
	 */
	public boolean isActive( ) {
		return active;
	}

	/**
	 * Returns the canvas associated with this controller
	 *
	 * The canvas is shared across all controllers
	 *
	 * @return the canvas associated with this controller
	 */
	public Canvas getCanvas() {
		return canvas;
	}
	
	/**
	 * Sets the canvas associated with this controller
	 *
	 * The canvas is shared across all controllers.  Setting this value will compute
	 * the drawing scale from the canvas size.
	 *
	 * @param canvas the canvas associated with this controller
	 */
	public void setCanvas(Canvas canvas) {
		this.canvas = canvas;
		this.scale.x = canvas.getWidth()/bounds.getWidth();
		this.scale.y = canvas.getHeight()/bounds.getHeight();
	}
	
	/**
	 * Creates a new game world with the default values.
	 *
	 * The game world is scaled so that the screen coordinates do not agree
	 * with the Box2d coordinates.  The bounds are in terms of the Box2d
	 * world, not the screen.
	 */
	protected GameController() {
		this(new Rectangle(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT),
				new Vector2(0, DEFAULT_GRAVITY));
		setComplete(false);
		setFailure(false);
		world.setContactListener(this);
		sensorFixtures = new ObjectSet<Fixture>();
	}

	/**
	 * Creates a new game world
	 *
	 * The game world is scaled so that the screen coordinates do not agree
	 * with the Box2d coordinates.  The bounds are in terms of the Box2d
	 * world, not the screen.
	 *
	 * @param width  	The width in Box2d coordinates
	 * @param height	The height in Box2d coordinates
	 * @param gravity	The downward gravity
	 */
	protected GameController(float width, float height, float gravity) {
		this(new Rectangle(0,0,width,height), new Vector2(0,gravity));
	}

	/**
	 * Creates a new game world
	 *
	 * The game world is scaled so that the screen coordinates do not agree
	 * with the Box2d coordinates.  The bounds are in terms of the Box2d
	 * world, not the screen.
	 *
	 * @param bounds	The game bounds in Box2d coordinates
	 * @param gravity	The gravitational force on this Box2d world
	 */
	protected GameController(Rectangle bounds, Vector2 gravity) {
		assets = new Array<String>();
		world = new World(gravity,false);
		this.bounds = new Rectangle(bounds);
		this.scale = new Vector2(1,1);
		complete = false;
		failed = false;
		active = false;
		countdown = -1;
	}
	
	/**
	 * Dispose of all (non-static) resources allocated to this mode.
	 */
	public void dispose() {
		for(GameObject g : objects) {
			g.deactivatePhysics(world);
		}
		objects.clear();
		addQueue.clear();
		world.dispose();
		objects = null;
		addQueue = null;
		bounds = null;
		scale  = null;
		world  = null;
		canvas = null;
	}

	/**
	 *
	 * Adds a physics object in to the insertion queue.
	 *
	 * Objects on the queue are added just before collision processing.  We do this to 
	 * control object creation.
	 *
	 * param obj The object to add
	 */
	public void addQueuedObject(GameObject g) {
		assert inBounds(g) : "Object is not in bounds";
		addQueue.add(g);
	}

	/**
	 * Immediately adds the object to the physics world
	 *
	 * @param g The object to add
	 */
	protected void addObject(GameObject g) {
		assert inBounds(g) : "Object is not in bounds";
		objects.add(g);
		g.activatePhysics(world);
	}

	public void addWall(Wall obj){
		assert inBounds(obj): "Object is not in bounds";
		walls.add(obj);
	}

	public void addCottonFlower (CottonFlower obj) {
		assert inBounds(obj): "Object is not in bounds";
		cottonFlower.add(obj);
	}

	public void addEnemy(Enemy obj){
		assert inBounds(obj) : "Objects is not in bounds";
		enemies.add(obj);
	}

	/**
	 * Returns true if the object is in bounds.
	 *
	 * This assertion is useful for debugging the physics.
	 *
	 * @param g The object to check.
	 *
	 * @return true if the object is in bounds.
	 */
	public boolean inBounds(GameObject g) {
		boolean horiz = (bounds.x <= g.getX() && g.getX() <= bounds.x+bounds.width);
		boolean vert  = (bounds.y <= g.getY() && g.getY() <= bounds.y+bounds.height);
		return horiz && vert;
	}

	/**
	 * Resets the status of the game so that we can play again.
	 *
	 * This method disposes of the world and creates a new one.
	 */
	public void reset() {
		Vector2 gravity = new Vector2(world.getGravity() );

		for(GameObject g : objects) {
			g.deactivatePhysics(world);
		}
		objects.clear();
		enemies.clear();
		addQueue.clear();
		world.dispose();

		world = new World(gravity,false);
		world.setContactListener(this);
		setComplete(false);
		setFailure(false);
		populateLevel();
	}
	
	/**
	 * Returns whether to process the update loop
	 *
	 * At the start of the update loop, we check if it is time
	 * to switch to a new game mode.  If not, the update proceeds
	 * normally.
	 *
	 * @param dt Number of seconds since last animation frame
	 * 
	 * @return whether to process the update loop
	 */
	public boolean preUpdate(float dt) {
		//System.out.println("in pre update");
		InputHandler input = InputHandler.getInstance();
		input.readInput(bounds, scale);
		if (listener == null) {
			return true;
		}
		
		// Handle resets*/
		if (input.didReset()) {
			reset();
		}
		/*
		// Now it is time to maybe switch screens.
		if (input.didExit()) {
			listener.exitScreen(this, EXIT_QUIT);
			return false;
		} else if (input.didAdvance()) {
			listener.exitScreen(this, EXIT_NEXT);
			return false;
		} else if (input.didRetreat()) {
			listener.exitScreen(this, EXIT_PREV);
			return false;
		*/

		// reset level when colliding with enemy
		if (countdown > 0) {
			countdown--;
		} else if (countdown == 0) {
			if (failed) {
				reset();
			}
		}
		return true;
	}
	
	/**
	 * Processes physics
	 *
	 * Once the update phase is over, but before we draw, we are ready to handle
	 * physics.  The primary method is the step() method in world.  This implementation
	 * works for all applications and should not need to be overwritten.
	 *
	 * @param dt Number of seconds since last animation frame
	 */
	public void postUpdate(float dt) {
		//System.out.println("in post update");
		// Add any objects created by actions
		while (!addQueue.isEmpty()) {
			addObject(addQueue.poll());
		}
		
		// Turn the physics engine crank.
		world.step(WORLD_STEP,WORLD_VELOC,WORLD_POSIT);

		// Garbage collect the deleted objects.
		// Note how we use the linked list nodes to delete O(1) in place.
		// This is O(n) without copying.
		Iterator<PooledList<GameObject>.Entry> iterator = objects.entryIterator();
		while (iterator.hasNext()) {
			PooledList<GameObject>.Entry entry = iterator.next();
			GameObject g = entry.getValue();
			if (g.isRemoved()) {
				g.deactivatePhysics(world);
				entry.remove();
			} else {
				// Note that update is called last!
				g.update(dt);
			}
		}
	}
	
	/**
	 * Draw the physics objects to the canvas
	 *
	 * For simple worlds, this method is enough by itself.  It will need
	 * to be overriden if the world needs fancy backgrounds or the like.
	 *
	 * The method draws all objects in the order that they were added.
	 *
	 * @param delta The difference from the last draw call
	 */
	public void draw(float delta) {
		//System.out.println("in draw");
		canvas.clear();
		
		canvas.begin();
		for(GameObject g : objects) {
			g.draw(canvas);
		}
		canvas.end();
		
		// Final message
		if (complete && !failed) {
			displayFont.setColor(Color.YELLOW);
			canvas.begin(); // DO NOT SCALE
			canvas.drawTextCentered("VICTORY!", displayFont, 0.0f);
			canvas.end();
		} else if (failed) {
			displayFont.setColor(Color.RED);
			canvas.begin(); // DO NOT SCALE
			canvas.drawTextCentered("FAILURE!", displayFont, 0.0f);
			canvas.end();
		}
	}
	
	/**
	 * Called when the Screen is resized. 
	 *
	 * This can happen at any point during a non-paused state but will never happen 
	 * before a call to show().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		// IGNORE FOR NOW
	}

	/**
	 * Called when the Screen should render itself.
	 *
	 * We defer to the other methods update() and draw().  However, it is VERY important
	 * that we only quit AFTER a draw.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	public void render(float delta) {
		if (active) {
			if (preUpdate(delta)) {
				update(delta); // This is the one that must be defined.
				postUpdate(delta);
			}
			draw(delta);
		}
	}

	/**
	 * Called when the Screen is paused.
	 * 
	 * This is usually when it's not active or visible on screen. An Application is 
	 * also paused before it is destroyed.
	 */
	public void pause() {
		// TODO Auto-generated method stub
	}

	/**
	 * Called when the Screen is resumed from a paused state.
	 *
	 * This is usually when it regains focus.
	 */
	public void resume() {
		// TODO Auto-generated method stub
	}
	
	/**
	 * Called when this screen becomes the current screen for a Game.
	 */
	public void show() {
		// Useless if called in outside animation loop
		active = true;
	}

	/**
	 * Called when this screen is no longer the current screen for a Game.
	 */
	public void hide() {
		// Useless if called in outside animation loop
		active = false;
	}

	/**
	 * Sets the ScreenListener for this mode
	 *
	 * The ScreenListener will respond to requests to quit.
	 */
	public void setScreenListener(ScreenListener listener) {
		this.listener = listener;
	}

	/**
	 * Lays out the game geography.
	 */
	private void populateLevel() {
		// Create player character
		// It is important that this is always created first, as transformations must swap the first element
		// in the objects list
		float dwidth = dollTexture.getRegionWidth() / (scale.x * 2);
		float dheight = dollTexture.getRegionHeight() / scale.y;
		avatar = new Doll(screenToMaze(1), screenToMaze(7), dwidth);
		avatar.setTexture(dollTexture);
		avatar.setDrawScale(scale);
		avatar.setType(DUGGI);
		addObject(avatar);


		// Add level goal
		dwidth = goalTile.getRegionWidth() / scale.x;
		dheight = goalTile.getRegionHeight() / scale.y;
		goalDoor = new Wall(screenToMaze(16), screenToMaze(2), dwidth, dheight, false);
		goalDoor.setBodyType(BodyDef.BodyType.StaticBody);
		goalDoor.setSensor(true);
		goalDoor.setDrawScale(scale);
		goalDoor.setTexture(goalTile);
		goalDoor.setName("exit");
		goalDoor.setType(GOAL);
		addObject(goalDoor);

		// Create enemy
		dwidth = dollTexture.getRegionWidth() / (scale.x * 2);
		dheight = dollTexture.getRegionHeight() / scale.y;


		//adding the rest of the enemies; they're static right now
		Enemy en1 = new Enemy(screenToMaze(4), screenToMaze(1), dwidth,0);
		Enemy en2 = new Enemy(screenToMaze(6), screenToMaze(5), dwidth,1);
		Enemy en3 = new Enemy(screenToMaze(9), screenToMaze(3), dwidth,2);
		Enemy en4 = new Enemy(screenToMaze(12), screenToMaze(4), dwidth,3);
		Enemy en5 = new Enemy(screenToMaze(14), screenToMaze(4), dwidth,4);
		Enemy en6 = new Enemy(screenToMaze(11), screenToMaze(6), dwidth,5);
		Enemy[] en = new Enemy[]{en1,en2,en3,en4,en5,en6};

		Vector2[] p1 = new Vector2[]{screenToMazeVector(4,1),
				screenToMazeVector(4,2),screenToMazeVector(4,3)};
		Vector2[] p2 = new Vector2[]{screenToMazeVector(6,5),
				screenToMazeVector(7,5),screenToMazeVector(8,5)};
		Vector2[] p3 = new Vector2[]{screenToMazeVector(9,3),
				screenToMazeVector(9,2)};
		Vector2[] p4 = new Vector2[]{screenToMazeVector(12,4),
				screenToMazeVector(12,3)};
		Vector2[] p5 = new Vector2[]{screenToMazeVector(14,4),
				screenToMazeVector(14,3),screenToMazeVector(14,2), screenToMazeVector(14,1)};
		Vector2[] p6 = new Vector2[]{screenToMazeVector(11,6),screenToMazeVector(12,6),
				screenToMazeVector(13,6),screenToMazeVector(14,6),screenToMazeVector(15,6),
				screenToMazeVector(16,6),screenToMazeVector(16,7),screenToMazeVector(16,8),
				screenToMazeVector(15,8),screenToMazeVector(14,8),screenToMazeVector(13,8),
				screenToMazeVector(12,8),screenToMazeVector(11,8),screenToMazeVector(11,7)};
		Vector2[][] pathList = new Vector2[][]{p1,p2,p3,p4,p5,p6};

		for (int i = 0; i < en.length; i++) {
			en[i].setType(ENEMY);
			en[i].setDrawScale(scale);
			en[i].setTexture(enemyTexture);
			addObject(en[i]);
			addEnemy(en[i]);
		}
		controls = new AIController[enemies.size()];
		for (int i = 0; i < en.length; i++) {
			controls[i] = new AIController(i,avatar,en,pathList[i]);
		}
		System.out.println("The size of the enemies pool list is " + enemies.size());


		/** Adding edible walls */
		dwidth = dollTexture.getRegionWidth() / scale.x;
		dheight = dollTexture.getRegionHeight() / scale.y;

		Wall ew1 = new Wall(screenToMaze(2), screenToMaze(4), dwidth, dheight, true);
		Wall ew2 = new Wall(screenToMaze(3), screenToMaze(3), dwidth, dheight, true);
		Wall ew3 = new Wall(screenToMaze(3), screenToMaze(4), dwidth, dheight, true);
		Wall ew4 = new Wall(screenToMaze(5), screenToMaze(5), dwidth, dheight, true);
		Wall ew5 = new Wall(screenToMaze(6), screenToMaze(1), dwidth, dheight, true);
		Wall ew6 = new Wall(screenToMaze(6), screenToMaze(2), dwidth, dheight, true);
		Wall ew7 = new Wall(screenToMaze(6), screenToMaze(3), dwidth, dheight, true);
		Wall ew8 = new Wall(screenToMaze(6), screenToMaze(4), dwidth, dheight, true);
		Wall ew9 = new Wall(screenToMaze(6), screenToMaze(6), dwidth, dheight, true);
		Wall ew10 = new Wall(screenToMaze(6), screenToMaze(7), dwidth, dheight, true);
		Wall ew11 = new Wall(screenToMaze(7), screenToMaze(6), dwidth, dheight, true);
		Wall ew12 = new Wall(screenToMaze(8), screenToMaze(6), dwidth, dheight, true);
		Wall ew13 = new Wall(screenToMaze(8), screenToMaze(7), dwidth, dheight, true);
		Wall ew14 = new Wall(screenToMaze(9), screenToMaze(1), dwidth, dheight, true);
		Wall ew15 = new Wall(screenToMaze(9), screenToMaze(5), dwidth, dheight, true);
		Wall ew16 = new Wall(screenToMaze(10), screenToMaze(5), dwidth, dheight, true);
		Wall ew17 = new Wall(screenToMaze(11), screenToMaze(5), dwidth, dheight, true);
		Wall ew18 = new Wall(screenToMaze(12), screenToMaze(2), dwidth, dheight, true);
		Wall ew19 = new Wall(screenToMaze(12), screenToMaze(7), dwidth, dheight, true);
		Wall ew20 = new Wall(screenToMaze(13), screenToMaze(2), dwidth, dheight, true);
		Wall ew21 = new Wall(screenToMaze(13), screenToMaze(7), dwidth, dheight, true);
		Wall ew22 = new Wall(screenToMaze(14), screenToMaze(5), dwidth, dheight, true);
		Wall ew23 = new Wall(screenToMaze(14), screenToMaze(7), dwidth, dheight, true);
		Wall ew24 = new Wall(screenToMaze(15), screenToMaze(3), dwidth, dheight, true);
		Wall ew25 = new Wall(screenToMaze(15), screenToMaze(5), dwidth, dheight, true);
		Wall ew26 = new Wall(screenToMaze(15), screenToMaze(7), dwidth, dheight, true);
		Wall[] veg = new Wall[]{ew1, ew2, ew3, ew4, ew5, ew6, ew7, ew8, ew9, ew10, ew11, ew12, ew13,
				ew14, ew15, ew16, ew17, ew18, ew19, ew20, ew21, ew22, ew23, ew24, ew25, ew26};
		for (int i = 0; i < 26; i++) {
			veg[i].setBodyType(BodyDef.BodyType.StaticBody);
			veg[i].setDrawScale(scale);
			veg[i].setTexture(edibleWallTexture);
			veg[i].setType(EDIBLEWALL);
			addObject(veg[i]);
			addWall(veg[i]);
		}
		//System.out.println(ew.getType());

		/** Adding inedible walls */
		Wall iw1 = new Wall(screenToMaze(2), screenToMaze(2), dwidth, dheight, false);
		Wall iw2 = new Wall(screenToMaze(2), screenToMaze(3), dwidth, dheight, false);
		Wall iw3 = new Wall(screenToMaze(2), screenToMaze(5), dwidth, dheight, false);
		Wall iw4 = new Wall(screenToMaze(2), screenToMaze(6), dwidth, dheight, false);
		Wall iw5 = new Wall(screenToMaze(3), screenToMaze(6), dwidth, dheight, false);
		Wall iw6 = new Wall(screenToMaze(3), screenToMaze(7), dwidth, dheight, false);
		Wall iw7 = new Wall(screenToMaze(4), screenToMaze(7), dwidth, dheight, false);
		Wall iw8 = new Wall(screenToMaze(5), screenToMaze(3), dwidth, dheight, false);
		Wall iw9 = new Wall(screenToMaze(5), screenToMaze(4), dwidth, dheight, false);
		Wall iw10 = new Wall(screenToMaze(5), screenToMaze(8), dwidth, dheight, false);
		Wall iw11 = new Wall(screenToMaze(6), screenToMaze(8), dwidth, dheight, false);
		Wall iw12 = new Wall(screenToMaze(7), screenToMaze(3), dwidth, dheight, false);
		Wall iw13 = new Wall(screenToMaze(7), screenToMaze(4), dwidth, dheight, false);
		Wall iw14 = new Wall(screenToMaze(8), screenToMaze(2), dwidth, dheight, false);
		Wall iw15 = new Wall(screenToMaze(9), screenToMaze(4), dwidth, dheight, false);
		Wall iw16 = new Wall(screenToMaze(10), screenToMaze(6), dwidth, dheight, false);
		Wall iw17 = new Wall(screenToMaze(10), screenToMaze(7), dwidth, dheight, false);
		Wall iw18 = new Wall(screenToMaze(10), screenToMaze(8), dwidth, dheight, false);
		Wall iw19 = new Wall(screenToMaze(12), screenToMaze(5), dwidth, dheight, false);
		Wall iw20 = new Wall(screenToMaze(13), screenToMaze(4), dwidth, dheight, false);
		Wall iw21 = new Wall(screenToMaze(13), screenToMaze(5), dwidth, dheight, false);
		Wall iw22 = new Wall(screenToMaze(16), screenToMaze(5), dwidth, dheight, false);
		Wall iw23 = new Wall(screenToMaze(4), screenToMaze(4), dwidth, dheight, false);
		Wall[] iw = new Wall[] {iw1, iw2, iw3, iw4, iw5, iw6, iw7, iw8, iw9, iw10, iw11, iw12, iw13, iw14,
				iw15, iw16, iw17, iw18, iw19, iw20, iw21, iw22, iw23};
		for (int i =0; i < 23; i++) {
			iw[i].setBodyType(BodyDef.BodyType.StaticBody);
			iw[i].setDrawScale(scale);
			iw[i].setTexture(wallTexture);
			iw[i].setType(WALL);
			addObject(iw[i]);
			addWall(iw[i]);
		}

		/** Adding cotton flowers */
		dwidth = dollTexture.getRegionWidth() / scale.x;
		dheight = dollTexture.getRegionHeight() / scale.y;
		//CottonFlower cf1 = new CottonFlower(screenToMaze(1), screenToMaze(4), dwidth, dheight);
		//CottonFlower cf2 = new CottonFlower(screenToMaze(2), screenToMaze(1), dwidth, dheight);
		CottonFlower cf3 = new CottonFlower(screenToMaze(2), screenToMaze(8), dwidth, dheight);
		CottonFlower cf4 = new CottonFlower(screenToMaze(3), screenToMaze(5), dwidth, dheight);
		CottonFlower cf5 = new CottonFlower(screenToMaze(3), screenToMaze(8), dwidth, dheight);
		CottonFlower cf6 = new CottonFlower(screenToMaze(4), screenToMaze(5), dwidth, dheight);
		CottonFlower cf7 = new CottonFlower(screenToMaze(4), screenToMaze(6), dwidth, dheight);
		CottonFlower cf8 = new CottonFlower(screenToMaze(4), screenToMaze(8), dwidth, dheight);
		CottonFlower cf9 = new CottonFlower(screenToMaze(7), screenToMaze(7), dwidth, dheight);
		CottonFlower cf10 = new CottonFlower(screenToMaze(11), screenToMaze(2), dwidth, dheight);
		CottonFlower cf11 = new CottonFlower(screenToMaze(13), screenToMaze(6), dwidth, dheight);
		CottonFlower cf12 = new CottonFlower(screenToMaze(15), screenToMaze(8), dwidth, dheight);
		CottonFlower cf13 = new CottonFlower(screenToMaze(16), screenToMaze(1), dwidth, dheight);
		CottonFlower cf14 = new CottonFlower(screenToMaze(16), screenToMaze(4), dwidth, dheight);
		CottonFlower cf15 = new CottonFlower(screenToMaze(16), screenToMaze(8), dwidth, dheight);
		CottonFlower[] cf = new CottonFlower[] {cf3, cf4, cf5, cf6, cf7, cf8, cf9, cf10,
				cf11, cf12, cf13, cf14, cf15};
		for (int i = 0; i < 13; i++) {
			cf[i].setBodyType(BodyDef.BodyType.StaticBody);
			cf[i].setDrawScale(scale);
			cf[i].setTexture(cottonTexture);
			cf[i].setType(COTTON);
			addObject(cf[i]);
			addCottonFlower(cf[i]);
		}
	}

//	/** Music */
//	Music music = Gdx.audio.newMusic(Gdx.files.internal("data/doll_bg.mp3"));
//	music.setLooping(true);
//	music.play();


	/**
	 * The core gameplay loop of this world.
	 *
	 * This method contains the specific update code for this mini-game. It does
	 * not handle collisions, as those are managed by the parent class GameController.
	 * This method is called after input is read, but before collisions are resolved.
	 * The very last thing that it should do is apply forces to the appropriate objects.
	 *
	 * @param dt Number of seconds since last animation frame
	 */
	public void update(float dt) {
		// Process actions in object model
		if (InputHandler.getInstance().didTransform()) {
			if (InputHandler.getInstance().didTransformDoll() && avatar.getForm() != Dinosaur.DOLL_FORM) {
				avatar = avatar.transformToDoll();
				avatar.setTexture(dollTexture);
				objects.set(1, avatar);
			}
			else if (InputHandler.getInstance().didTransformHerbi() && avatar.getForm() != Dinosaur.HERBIVORE_FORM) {
				avatar = avatar.transformToHerbivore();
				avatar.setTexture(herbivoreTexture);
				objects.set(1, avatar);
			}
			else if (InputHandler.getInstance().didTransformCarni() && avatar.getForm() != Dinosaur.CARNIVORE_FORM) {
				avatar = avatar.transformToCarnivore();
				avatar.setTexture(carnivoreTexture);
				objects.set(1, avatar);
			}
		}
		avatar.setLeftRight(InputHandler.getInstance().getHorizontal());
		avatar.setUpDown(InputHandler.getInstance().getVertical());

		boolean hasClone = false;
		if (InputHandler.getInstance().didAction()) {
			if (avatar.getForm() == Dinosaur.DOLL_FORM) {
				if (!hasClone) {
					float dwidth = dollTexture.getRegionWidth() / scale.x;
					float dheight = dollTexture.getRegionHeight() / scale.y;
//					DuggiModel clone = new DuggiModel(screenToMaze(1), screenToMaze(1), dwidth, dheight);
//					clone.setTexture(dollTexture);
//					clone.setDrawScale(scale);
//					clone.setDollTexture(dollTexture);
//					clone.setType(CLONE);
//					addObject(clone);
					hasClone = true;
				}
			}
			else if (avatar.getForm() == Dinosaur.HERBIVORE_FORM) {
				System.out.println(collidedType);
				if (collidedType == EDIBLEWALL) {
					System.out.println("asdfA");
					collidedWith.deactivatePhysics(world);
					objects.remove(collidedWith);
					walls.remove(collidedWith);
					collidedType = -1;
					collidedWith = null;
				}
			}
		}

		avatar.applyForce();

		// AI movement
		for (int i = 0; i < enemies.size();i++){
			controls[i].getMoveAlongPath();
		}


		// If we use sound, we must remember this.
		SoundController.getInstance().update();
	}

	/**
	 * Callback method for the start of a collision
	 *
	 * This method is called when we first get a collision between two objects.  We use
	 * this method to test if it is the "right" kind of collision.  In particular, we
	 * use it to test if we made it to the win door.
	 *
	 * @param contact The two bodies that collided
	 */
	public void beginContact(Contact contact) {
		//System.out.println("in begin contact");
		Fixture fix1 = contact.getFixtureA();
		Fixture fix2 = contact.getFixtureB();

		Body body1 = fix1.getBody();
		Body body2 = fix2.getBody();


		Object fd1 = fix1.getUserData();
		Object fd2 = fix2.getUserData();

		try {
			GameObject bd1 = (GameObject)body1.getUserData();
			GameObject bd2 = (GameObject)body2.getUserData();

			if (bd1.getType() == DUGGI || bd2.getType() == DUGGI) {
				//System.out.println("dfghs'");
				//System.out.println(bd1.toString());
				//System.out.println(bd2.toString());
				//System.out.println(bd1.getType() == EDIBLEWALL);
			}

			// Check for win condition
			if ((bd1.getType() == DUGGI && bd2 == goalDoor) || (bd1 == goalDoor && bd2.getType() == DUGGI)) {
				setComplete(true);
			}

			// Check if collided with enemy
			if ((bd1.getType() == DUGGI && bd2.getType() == ENEMY) || (bd1.getType() == ENEMY && bd2.getType() == DUGGI)) {
				setFailure(true);
			}

			if ((bd1.getType() == DUGGI && bd2.getType() == EDIBLEWALL) ||
					(bd1.getType() == EDIBLEWALL && bd2.getType() == DUGGI)) {
				System.out.println("colllided");
				if (bd1.getType() == EDIBLEWALL)
					collidedWith = bd1;
				else
					collidedWith = bd2;
				collidedType = EDIBLEWALL;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void handleCollision(GameObject bd1, GameObject bd2){

	}

	/**
	 * Callback method for the start of a collision
	 *
	 * This method is called when two objects cease to touch.  The main use of this method
	 * is to determine when the characer is NOT on the ground.  This is how we prevent
	 * double jumping.
	 */
	public void endContact(Contact contact) {
		//System.out.println("in end contact");
		Fixture fix1 = contact.getFixtureA();
		Fixture fix2 = contact.getFixtureB();

		Body body1 = fix1.getBody();
		Body body2 = fix2.getBody();

		Object fd1 = fix1.getUserData();
		Object fd2 = fix2.getUserData();

		Object bd1 = body1.getUserData();
		Object bd2 = body2.getUserData();

		if (bd1 == avatar || bd2 == avatar) {
			System.out.println("Resetting");
			collidedType = -1;
			collidedWith = null;
		}
	}
/*
	public boolean objectTooFarX(Obstacle object){
		//System.out.println("in objectTooFarX, dx,width");
		//System.out.println(Math.abs(object.getX() - avatar.getX()));
		//System.out.println(avatar.getWidth());
		//System.out.println(avatar.getHeight());
		return (Math.abs(object.getX() - avatar.getX()) >= avatar.getWidth()/3);
	}
	public boolean objectTooFarY(Obstacle object){
		//System.out.println("in objectTooFar, dy, height");
		//System.out.println(Math.abs(object.getX() - avatar.getX()));
		//System.out.println(Math.abs(object.getY() - avatar.getY()));
		//System.out.println(avatar.getWidth());
		//System.out.println(avatar.getHeight());
		return (Math.abs(object.getX() - avatar.getX()) >= avatar.getWidth()/3);
	}
*/

	/** drawing on screen */
	public int screenToMaze (float f) {
		return (int)(1+2*(f-1));
	}

	public Vector2 screenToMazeVector(float x, float y){
		return new Vector2(screenToMaze(x), screenToMaze(y));
	}

	/** Unused ContactListener method */
	public void postSolve(Contact contact, ContactImpulse impulse) {}
	/** Unused ContactListener method */
	public void preSolve(Contact contact, Manifold oldManifold) {}
}