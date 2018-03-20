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
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
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

	/** The sound file for music and effects */
	private static String DOLL_BG_FILE = "trino/doll_bg.mp3";
	private static String HERBIVORE_BG_FILE = "trino/herbivore_bg.mp3";
	private static String CARNIVORE_BG_FILE = "trino/carnivore_bg.mp3";
	private static String POP_1_FILE = "trino/pop1.mp3";
	private static String POP_2_FILE = "trino/pop2.mp3";
	private static String POP_3_FILE = "trino/pop3.mp3";
	private static String POP_4_FILE = "trino/pop4.mp3";
	private static String POP_5_FILE = "trino/pop5.mp3";
	private static String POOF_FILE = "trino/poof.mp3";

	/** The texture file for general assets */
	private static String EARTH_FILE = "shared/earthtile.png";
	private static String GOAL_FILE = "shared/goaldoor.png";
	private static String FONT_FILE = "shared/Montserrat/Montserrat-Bold.ttf";
	private static int FONT_SIZE = 64;

	/** The texture files for the three dinosaurs (no animation) */
	private static final String DOLL_FILE_FRONT  = "trino/doll_front.png";
	private static final String DOLL_FILE_LEFT  = "trino/doll_left.png";
	private static final String DOLL_FILE_RIGHT  = "trino/doll_right.png";
	private static final String DOLL_FILE_BACK  = "trino/doll_back.png";
	private static final String HERBIVORE_FILE_FRONT  = "trino/herbivore_front.png";
	private static final String HERBIVORE_FILE_LEFT  = "trino/herbivore_left.png";
	private static final String HERBIVORE_FILE_RIGHT  = "trino/herbivore_right.png";
	private static final String HERBIVORE_FILE_BACK  = "trino/herbivore_back.png";
	private static final String CARNIVORE_FILE_FRONT  = "trino/carnivore_front.png";
	private static final String CARNIVORE_FILE_LEFT  = "trino/carnivore_left.png";
	private static final String CARNIVORE_FILE_RIGHT  = "trino/carnivore_right.png";
	private static final String CARNIVORE_FILE_BACK  = "trino/carnivore_back.png";
	private static final String ENEMY_FILE_FRONT = "trino/carnivore_front.png";
	private static final String ENEMY_FILE_LEFT = "trino/carnivore_left.png";
	private static final String ENEMY_FILE_RIGHT = "trino/carnivore_right.png";
	private static final String ENEMY_FILE_BACK = "trino/carnivore_back.png";
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

	private static int GRIDSIZE = 80;
	private static int GRID_MAX_X = 16;
	private static int GRID_MAX_Y = 8;

	private GameObject[][] grid = new GameObject[GRID_MAX_X][GRID_MAX_Y];


	/** Texture assets for the general game */
	private TextureRegion earthTile;
	private TextureRegion goalTile;
	private BitmapFont displayFont;

	/** Texture assets for Duggi's three forms */
	private TextureRegion dollTextureFront;
	private TextureRegion dollTextureLeft;
	private TextureRegion dollTextureRight;
	private TextureRegion dollTextureBack;
	private TextureRegion herbivoreTextureFront;
	private TextureRegion herbivoreTextureLeft;
	private TextureRegion herbivoreTextureRight;
	private TextureRegion herbivoreTextureBack;
	private TextureRegion carnivoreTextureFront;
	private TextureRegion carnivoreTextureLeft;
	private TextureRegion carnivoreTextureRight;
	private TextureRegion carnivoreTextureBack;

	/* Texture assets for enemies */
	private TextureRegion enemyTextureFront;
	private TextureRegion enemyTextureLeft;
	private TextureRegion enemyTextureRight;
	private TextureRegion enemyTextureBack;

	/* Texture assets for other world attributes */
	private TextureRegion wallTexture;
	private TextureRegion edibleWallTexture;
	private TextureRegion cottonTexture;
	private TextureRegion pathTexture;

	private HUDController hud;

	//index of the object Duggi collided with
	private PooledList<GameObject> collidedWith = new PooledList<GameObject>();
	private GameObject directlyInFront;
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
	private static final int EXIT_COUNT = 60;

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
	private Dinosaur clone;
	private Vector2 cloneLocation;
	private boolean removeClone = false;

	private Vector2 switchLocation = new Vector2(16, 6);

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

	// Variables for sound effects and music
	private Music bgMusic;
	private Music bgDoll;
	private Music bgHerb;
	private Music bgCarn;

	private Sound cottonPickUp;
	private Sound eatWall;
	private Sound collideWall;
	private Sound transformSound;

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
		hud.preLoadContent(manager);
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
		manager.load(DOLL_FILE_FRONT, Texture.class);
		assets.add(DOLL_FILE_FRONT);
		manager.load(DOLL_FILE_LEFT, Texture.class);
		assets.add(DOLL_FILE_LEFT);
		manager.load(DOLL_FILE_RIGHT, Texture.class);
		assets.add(DOLL_FILE_RIGHT);
		manager.load(DOLL_FILE_BACK, Texture.class);
		assets.add(DOLL_FILE_BACK);
		manager.load(HERBIVORE_FILE_FRONT, Texture.class);
		assets.add(HERBIVORE_FILE_FRONT);
		manager.load(HERBIVORE_FILE_LEFT, Texture.class);
		assets.add(HERBIVORE_FILE_LEFT);
		manager.load(HERBIVORE_FILE_RIGHT, Texture.class);
		assets.add(HERBIVORE_FILE_RIGHT);
		manager.load(HERBIVORE_FILE_BACK, Texture.class);
		assets.add(HERBIVORE_FILE_BACK);
		manager.load(CARNIVORE_FILE_FRONT, Texture.class);
		assets.add(CARNIVORE_FILE_FRONT);
		manager.load(CARNIVORE_FILE_LEFT, Texture.class);
		assets.add(CARNIVORE_FILE_LEFT);
		manager.load(CARNIVORE_FILE_RIGHT, Texture.class);
		assets.add(CARNIVORE_FILE_RIGHT);
		manager.load(CARNIVORE_FILE_BACK, Texture.class);
		assets.add(CARNIVORE_FILE_BACK);
		manager.load(WALL_FILE, Texture.class);
		assets.add(WALL_FILE);
		manager.load(EDIBLE_WALL_FILE, Texture.class);
		assets.add(EDIBLE_WALL_FILE);
		manager.load(COTTON_FLOWER_FILE, Texture.class);
		assets.add(COTTON_FLOWER_FILE);
		manager.load(ENEMY_FILE_FRONT, Texture.class);
		assets.add(ENEMY_FILE_FRONT);
		manager.load(ENEMY_FILE_LEFT, Texture.class);
		assets.add(ENEMY_FILE_LEFT);
		manager.load(ENEMY_FILE_RIGHT, Texture.class);
		assets.add(ENEMY_FILE_RIGHT);
		manager.load(ENEMY_FILE_BACK, Texture.class);
		assets.add(ENEMY_FILE_BACK);
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
		hud.loadContent(manager);
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

		dollTextureFront = createTexture(manager,DOLL_FILE_FRONT,false);
		dollTextureLeft = createTexture(manager,DOLL_FILE_LEFT,false);
		dollTextureRight = createTexture(manager,DOLL_FILE_RIGHT,false);
		dollTextureBack = createTexture(manager,DOLL_FILE_BACK,false);
		herbivoreTextureFront = createTexture(manager,HERBIVORE_FILE_FRONT,false);
		herbivoreTextureLeft = createTexture(manager,HERBIVORE_FILE_LEFT,false);
		herbivoreTextureRight = createTexture(manager,HERBIVORE_FILE_RIGHT,false);
		herbivoreTextureBack = createTexture(manager,HERBIVORE_FILE_BACK,false);
		carnivoreTextureFront = createTexture(manager,CARNIVORE_FILE_FRONT,false);
		carnivoreTextureLeft = createTexture(manager,CARNIVORE_FILE_LEFT,false);
		carnivoreTextureRight = createTexture(manager,CARNIVORE_FILE_RIGHT,false);
		carnivoreTextureBack = createTexture(manager,CARNIVORE_FILE_BACK,false);
		enemyTextureFront = createTexture(manager,ENEMY_FILE_FRONT, false);
		enemyTextureLeft = createTexture(manager,ENEMY_FILE_LEFT, false);
		enemyTextureRight = createTexture(manager,ENEMY_FILE_RIGHT, false);
		enemyTextureBack = createTexture(manager,ENEMY_FILE_BACK, false);
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
		hud.unloadContent(manager);
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
		hud.setCanvas(canvas);
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
		hud = new HUDController();
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

		// dispose music and sound assets
		bgMusic.dispose();
		bgDoll.dispose();
		bgHerb.dispose();
		bgCarn.dispose();

		cottonPickUp.dispose();
		eatWall.dispose();
		collideWall.dispose();
		transformSound.dispose();
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
		if (g.getType()!= COTTON)
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
		clone = null;
		cloneLocation = null;

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
		//////("in pre update");
		InputHandler input = InputHandler.getInstance();
		input.readInput(bounds, scale);
		if (listener == null) {
			return true;
		}
		
		// Handle resets*/
		if (input.didReset()) {
			reset();
		}
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
		//////("in draw");
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
			canvas.drawTextCentered("DUGGI ESCAPED!", displayFont, 0.0f);
			canvas.end();
		} else if (failed) {
			displayFont.setColor(Color.RED);
			canvas.begin(); // DO NOT SCALE
			canvas.drawTextCentered("EATEN ALIVE!", displayFont, 0.0f);
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
			hud.draw();
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

		float dwidth = dollTextureRight.getRegionWidth() / (scale.x * 2);
		float dheight = dollTextureRight.getRegionHeight() / scale.y;
		avatar = new Doll(screenToMaze(1), screenToMaze(7), dwidth);
		avatar.setType(DUGGI);

		/** Adding cotton flowers */
		dwidth = cottonTexture.getRegionWidth() / scale.x;
		dheight = cottonTexture.getRegionHeight() / scale.y;
		CottonFlower cf1 = new CottonFlower(1,4, screenToMaze(1), screenToMaze(4), dwidth, dheight);
		CottonFlower cf2 = new CottonFlower(2,1, screenToMaze(2), screenToMaze(1), dwidth, dheight);
		CottonFlower cf3 = new CottonFlower(2,8,screenToMaze(2), screenToMaze(8), dwidth, dheight);
		CottonFlower cf4 = new CottonFlower(3,5,screenToMaze(3), screenToMaze(5), dwidth, dheight);
		CottonFlower cf5 = new CottonFlower(3,8,screenToMaze(3), screenToMaze(8), dwidth, dheight);
		CottonFlower cf6 = new CottonFlower(4,5,screenToMaze(4), screenToMaze(5), dwidth, dheight);
		CottonFlower cf7 = new CottonFlower(4,6,screenToMaze(4), screenToMaze(6), dwidth, dheight);
		CottonFlower cf8 = new CottonFlower(4,8,screenToMaze(4), screenToMaze(8), dwidth, dheight);
		CottonFlower cf9 = new CottonFlower(7,7,screenToMaze(7), screenToMaze(7), dwidth, dheight);
		CottonFlower cf10 = new CottonFlower(11,2,screenToMaze(11), screenToMaze(2), dwidth, dheight);
		CottonFlower cf11 = new CottonFlower(13,6,screenToMaze(13), screenToMaze(6), dwidth, dheight);
		CottonFlower cf12 = new CottonFlower(15,8,screenToMaze(15), screenToMaze(8), dwidth, dheight);
		CottonFlower cf13 = new CottonFlower(16,1,screenToMaze(16), screenToMaze(1), dwidth, dheight);
		CottonFlower cf14 = new CottonFlower(16,4,screenToMaze(16), screenToMaze(4), dwidth, dheight);
		CottonFlower cf15 = new CottonFlower(16,8,screenToMaze(16), screenToMaze(8), dwidth, dheight);
		CottonFlower[] cf = new CottonFlower[] {cf1, cf2, cf3, cf4, cf5, cf6, cf7, cf8, cf9, cf10,
				cf11, cf12, cf13, cf14, cf15};
		for (int i = 0; i < 15; i++) {
			cf[i].setBodyType(BodyDef.BodyType.StaticBody);
			cf[i].setDrawScale(scale);
			cf[i].setTexture(cottonTexture);
			cf[i].setType(COTTON);
			addObject(cf[i]);
			addCottonFlower(cf[i]);
			grid[(int)cf[i].getGridLocation().x-1][(int)cf[i].getGridLocation().y-1] = cf[i];
		}

		// Add level goal
		dwidth = goalTile.getRegionWidth() / scale.x;
		dheight = goalTile.getRegionHeight() / scale.y;
		goalDoor = new Wall(16,2,screenToMaze(16), screenToMaze(2), dwidth, dheight, false);
		goalDoor.setBodyType(BodyDef.BodyType.StaticBody);
		goalDoor.setSensor(true);
		goalDoor.setDrawScale(scale);
		goalDoor.setTexture(goalTile);
		goalDoor.setName("exit");
		goalDoor.setType(GOAL);
		addObject(goalDoor);

		// Create enemy
		dwidth = carnivoreTextureFront.getRegionWidth() / (scale.x * 2);
		dheight = carnivoreTextureFront.getRegionHeight() / scale.y;


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
			en[i].setTexture(enemyTextureFront);
			addObject(en[i]);
			addEnemy(en[i]);
		}
		controls = new AIController[enemies.size()];
		for (int i = 0; i < en.length; i++) {
			controls[i] = new AIController(i,avatar,en,pathList[i]);
		}


		/** Adding edible walls */
		dwidth = edibleWallTexture.getRegionWidth() / scale.x;
		dheight = edibleWallTexture.getRegionHeight() / scale.y;

		Wall ew1 = new Wall(2,4,screenToMaze(2), screenToMaze(4), dwidth, dheight, true);
		Wall ew2 = new Wall(3,3,screenToMaze(3), screenToMaze(3), dwidth, dheight, true);
		Wall ew3 = new Wall(3,4,screenToMaze(3), screenToMaze(4), dwidth, dheight, true);
		Wall ew4 = new Wall(5,5,screenToMaze(5), screenToMaze(5), dwidth, dheight, true);
		Wall ew5 = new Wall(6,1,screenToMaze(6), screenToMaze(1), dwidth, dheight, true);
		Wall ew6 = new Wall(6,2,screenToMaze(6), screenToMaze(2), dwidth, dheight, true);
		Wall ew7 = new Wall(6,3,screenToMaze(6), screenToMaze(3), dwidth, dheight, true);
		Wall ew8 = new Wall(6,4,screenToMaze(6), screenToMaze(4), dwidth, dheight, true);
		Wall ew9 = new Wall(6,6,screenToMaze(6), screenToMaze(6), dwidth, dheight, true);
		Wall ew10 = new Wall(6,7,screenToMaze(6), screenToMaze(7), dwidth, dheight, true);
		Wall ew11 = new Wall(7,6,screenToMaze(7), screenToMaze(6), dwidth, dheight, true);
		Wall ew12 = new Wall(8,6,screenToMaze(8), screenToMaze(6), dwidth, dheight, true);
		Wall ew13 = new Wall(8,7,screenToMaze(8), screenToMaze(7), dwidth, dheight, true);
		Wall ew14 = new Wall(9,1,screenToMaze(9), screenToMaze(1), dwidth, dheight, true);
		Wall ew15 = new Wall(9,5,screenToMaze(9), screenToMaze(5), dwidth, dheight, true);
		Wall ew16 = new Wall(10,5,screenToMaze(10), screenToMaze(5), dwidth, dheight, true);
		Wall ew17 = new Wall(11,5,screenToMaze(11), screenToMaze(5), dwidth, dheight, true);
		Wall ew18 = new Wall(12,2,screenToMaze(12), screenToMaze(2), dwidth, dheight, true);
		Wall ew19 = new Wall(12,7,screenToMaze(12), screenToMaze(7), dwidth, dheight, true);
		Wall ew20 = new Wall(13,2,screenToMaze(13), screenToMaze(2), dwidth, dheight, true);
		Wall ew21 = new Wall(13,7,screenToMaze(13), screenToMaze(7), dwidth, dheight, true);
		Wall ew22 = new Wall(14,5,screenToMaze(14), screenToMaze(5), dwidth, dheight, true);
		Wall ew23 = new Wall(14,7,screenToMaze(14), screenToMaze(7), dwidth, dheight, true);
		Wall ew24 = new Wall(15,3,screenToMaze(15), screenToMaze(3), dwidth, dheight, true);
		Wall ew25 = new Wall(15,5,screenToMaze(15), screenToMaze(5), dwidth, dheight, true);
		Wall ew26 = new Wall(15,7,screenToMaze(15), screenToMaze(7), dwidth, dheight, true);
		Wall[] veg = new Wall[]{ew1, ew2, ew3, ew4, ew5, ew6, ew7, ew8, ew9, ew10, ew11, ew12, ew13,
				ew14, ew15, ew16, ew17, ew18, ew19, ew20, ew21, ew22, ew23, ew24, ew25, ew26};
		for (int i = 0; i < 26; i++) {
			veg[i].setBodyType(BodyDef.BodyType.StaticBody);
			veg[i].setDrawScale(scale);
			veg[i].setTexture(edibleWallTexture);
			veg[i].setType(EDIBLEWALL);
			addObject(veg[i]);
			addWall(veg[i]);
			grid[(int)veg[i].getGridLocation().x-1][(int)veg[i].getGridLocation().y-1] = veg[i];
		}

		/** Adding inedible walls */
		Wall iw1 = new Wall(2,2,screenToMaze(2), screenToMaze(2), dwidth, dheight, false);
		Wall iw2 = new Wall(2,3,screenToMaze(2), screenToMaze(3), dwidth, dheight, false);
		Wall iw3 = new Wall(2,5,screenToMaze(2), screenToMaze(5), dwidth, dheight, false);
		Wall iw4 = new Wall(2,6,screenToMaze(2), screenToMaze(6), dwidth, dheight, false);
		Wall iw5 = new Wall(3,6,screenToMaze(3), screenToMaze(6), dwidth, dheight, false);
		Wall iw6 = new Wall(3,7,screenToMaze(3), screenToMaze(7), dwidth, dheight, false);
		Wall iw7 = new Wall(4,7,screenToMaze(4), screenToMaze(7), dwidth, dheight, false);
		Wall iw8 = new Wall(5,3,screenToMaze(5), screenToMaze(3), dwidth, dheight, false);
		Wall iw9 = new Wall(5,4,screenToMaze(5), screenToMaze(4), dwidth, dheight, false);
		Wall iw10 = new Wall(5,8,screenToMaze(5), screenToMaze(8), dwidth, dheight, false);
		Wall iw11 = new Wall(6,8,screenToMaze(6), screenToMaze(8), dwidth, dheight, false);
		Wall iw12 = new Wall(7,3,screenToMaze(7), screenToMaze(3), dwidth, dheight, false);
		Wall iw13 = new Wall(7,4,screenToMaze(7), screenToMaze(4), dwidth, dheight, false);
		Wall iw14 = new Wall(8,2,screenToMaze(8), screenToMaze(2), dwidth, dheight, false);
		Wall iw15 = new Wall(9,4,screenToMaze(9), screenToMaze(4), dwidth, dheight, false);
		Wall iw16 = new Wall(10,6,screenToMaze(10), screenToMaze(6), dwidth, dheight, false);
		Wall iw17 = new Wall(10,7,screenToMaze(10), screenToMaze(7), dwidth, dheight, false);
		Wall iw18 = new Wall(10,8,screenToMaze(10), screenToMaze(8), dwidth, dheight, false);
		Wall iw19 = new Wall(12,5,screenToMaze(12), screenToMaze(5), dwidth, dheight, false);
		Wall iw20 = new Wall(13,4,screenToMaze(13), screenToMaze(4), dwidth, dheight, false);
		Wall iw21 = new Wall(13,5,screenToMaze(13), screenToMaze(5), dwidth, dheight, false);
		Wall iw22 = new Wall(16,5,screenToMaze(16), screenToMaze(5), dwidth, dheight, false);
		Wall iw23 = new Wall(4,4,screenToMaze(4), screenToMaze(4), dwidth, dheight, false);
		Wall[] iw = new Wall[] {iw1, iw2, iw3, iw4, iw5, iw6, iw7, iw8, iw9, iw10, iw11, iw12, iw13, iw14,
				iw15, iw16, iw17, iw18, iw19, iw20, iw21, iw22, iw23};
		for (int i =0; i < 23; i++) {
			iw[i].setBodyType(BodyDef.BodyType.StaticBody);
			iw[i].setDrawScale(scale);
			iw[i].setTexture(wallTexture);
			iw[i].setType(WALL);
			addObject(iw[i]);
			addWall(iw[i]);
			grid[(int)iw[i].getGridLocation().x-1][(int)iw[i].getGridLocation().y-1] = iw[i];
		}


		avatar.setTexture(dollTextureRight);
		avatar.setDrawScale(scale);
		addObject(avatar);
		avatarIdx = objects.size()-1;


		/** Music */

		if (bgMusic == null){
			bgDoll = Gdx.audio.newMusic(Gdx.files.internal(DOLL_BG_FILE));
			bgHerb = Gdx.audio.newMusic(Gdx.files.internal(HERBIVORE_BG_FILE));
			bgCarn = Gdx.audio.newMusic(Gdx.files.internal(CARNIVORE_BG_FILE));

			// set sound effects
			cottonPickUp = Gdx.audio.newSound(Gdx.files.internal(POP_1_FILE));
			eatWall = Gdx.audio.newSound(Gdx.files.internal(POP_2_FILE));
			collideWall = Gdx.audio.newSound(Gdx.files.internal(POP_5_FILE));
			transformSound = Gdx.audio.newSound(Gdx.files.internal(POOF_FILE));

		} else {
			// Pause all music
			bgMusic.pause();
			bgDoll.pause();
			bgHerb.pause();
			bgCarn.pause();
		}

		bgMusic = bgDoll;
		bgMusic.setLooping(true);
		bgMusic.setVolume(0.10f);
		bgMusic.setPosition(0);
		bgMusic.play();

	}

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
		int direction = avatar.getDirection();

		if (avatar.getX() >= screenToMaze(1) && avatar.getX() <= screenToMaze(16)
				&& avatar.getY() >= screenToMaze(1) && avatar.getY() <= screenToMaze(8)) {
			avatar.setLeftRight(InputHandler.getInstance().getHorizontal());
			avatar.setUpDown(InputHandler.getInstance().getVertical());

		}
		else {
			if (avatar.getX() < screenToMaze(1)) {
				avatar.setLeftRight(InputHandler.getInstance().getHorizontal()+ 5.0f);
				if (avatar.getY() < screenToMaze(1)) {
					avatar.setUpDown(InputHandler.getInstance().getVertical() + 5.0f);
					avatar.setDirection(Dinosaur.DOWN);
				}
				if (avatar.getY() > screenToMaze(8)) {
					avatar.setUpDown(InputHandler.getInstance().getVertical() - 5.0f);
					avatar.setDirection(Dinosaur.UP);
				}
				else {
					////("else");
					avatar.setUpDown(InputHandler.getInstance().getVertical());
					avatar.setDirection(Dinosaur.LEFT);
				}
			}
			if (avatar.getX() > screenToMaze(16)) {
				avatar.setLeftRight(InputHandler.getInstance().getHorizontal()- 5.0f);
				if (avatar.getY() < screenToMaze(1)) {
					avatar.setUpDown(InputHandler.getInstance().getVertical() + 5.0f);
					avatar.setDirection(Dinosaur.DOWN);
				}
				if (avatar.getY() > screenToMaze(8)) {
					avatar.setUpDown(InputHandler.getInstance().getVertical() - 5.0f);
					avatar.setDirection(Dinosaur.UP);
				}
				else {
					avatar.setUpDown(InputHandler.getInstance().getVertical());
					avatar.setDirection(Dinosaur.RIGHT);
				}
			}
			if (avatar.getY() < screenToMaze(1)) {
				avatar.setUpDown(InputHandler.getInstance().getHorizontal());
				avatar.setUpDown(InputHandler.getInstance().getVertical() + 5.0f);
				avatar.setDirection(Dinosaur.DOWN);

			}
			if (avatar.getY() > screenToMaze(8)) {
				avatar.setUpDown(InputHandler.getInstance().getHorizontal());
				avatar.setUpDown(InputHandler.getInstance().getVertical() - 5.0f);
				avatar.setDirection(Dinosaur.UP);
			}
		}
		int idx = objects.size()-1;
		if (InputHandler.getInstance().didTransform()) {
			if (avatar.canTransform()) {
				if (InputHandler.getInstance().didTransformDoll() && avatar.getForm() != Dinosaur.DOLL_FORM) {
					avatar = avatar.transformToDoll();

					// Change the music
					changeMusic(bgDoll);
					// play sound effect
					transformSound.pause();
					transformSound.play(1.0f);

					if (direction == Dinosaur.UP) {
						avatar.setTexture(dollTextureBack);
					} else if (direction == Dinosaur.LEFT) {
						avatar.setTexture(dollTextureLeft);
					} else if (direction == Dinosaur.RIGHT) {
						avatar.setTexture(dollTextureRight);
					} else {
						avatar.setTexture(dollTextureFront);
					}

					objects.set(idx, avatar);
				} else if (InputHandler.getInstance().didTransformHerbi() && avatar.getForm() != Dinosaur.HERBIVORE_FORM) {
					avatar = avatar.transformToHerbivore();

					// Change the music
					changeMusic(bgHerb);
					// play sound effect
					transformSound.pause();
					transformSound.play(1.0f);

					if (direction == Dinosaur.UP) {
						avatar.setTexture(herbivoreTextureBack);
					} else if (direction == Dinosaur.LEFT) {
						avatar.setTexture(herbivoreTextureLeft);
					} else if (direction == Dinosaur.RIGHT) {
						avatar.setTexture(herbivoreTextureRight);
					} else {
						avatar.setTexture(herbivoreTextureFront);
					}
					objects.set(idx, avatar);
				} else if (InputHandler.getInstance().didTransformCarni() && avatar.getForm() != Dinosaur.CARNIVORE_FORM) {
					avatar = avatar.transformToCarnivore();

					// Change the music
					changeMusic(bgCarn);
					// play sound effect
					transformSound.pause();
					transformSound.play(1.0f);

					if (direction == Dinosaur.UP) {
						avatar.setTexture(carnivoreTextureBack);
					} else if (direction == Dinosaur.LEFT) {
						avatar.setTexture(carnivoreTextureLeft);
					} else if (direction == Dinosaur.RIGHT) {
						avatar.setTexture(carnivoreTextureRight);
					} else {
						avatar.setTexture(carnivoreTextureFront);
					}
					objects.set(idx, avatar);
				}
			}
		}
		if (avatar.getForm() == Dinosaur.DOLL_FORM) {
			if (avatar.getDirection() == Dinosaur.UP) {
				avatar.setTexture(dollTextureBack);
			}
			else if (avatar.getDirection() == Dinosaur.LEFT) {
				avatar.setTexture(dollTextureLeft);
			}
			else if (avatar.getDirection() == Dinosaur.RIGHT) {
				avatar.setTexture(dollTextureRight);
			}
			else if (avatar.getDirection() == Dinosaur.DOWN) {
				avatar.setTexture(dollTextureFront);
			}
		}
		else if (avatar.getForm() == Dinosaur.HERBIVORE_FORM) {
			if (avatar.getDirection() == Dinosaur.UP) {
				avatar.setTexture(herbivoreTextureBack);
			}
			else if (avatar.getDirection() == Dinosaur.LEFT) {
				avatar.setTexture(herbivoreTextureLeft);
			}
			else if (avatar.getDirection() == Dinosaur.RIGHT) {
				avatar.setTexture(herbivoreTextureRight);
			}
			else if (avatar.getDirection() == Dinosaur.DOWN) {
				avatar.setTexture(herbivoreTextureFront);
			}
		}
		else if (avatar.getForm() == Dinosaur.CARNIVORE_FORM) {
			if (avatar.getDirection() == Dinosaur.UP) {
				avatar.setTexture(carnivoreTextureBack);
			}
			else if (avatar.getDirection() == Dinosaur.LEFT) {
				avatar.setTexture(carnivoreTextureLeft);
			}
			else if (avatar.getDirection() == Dinosaur.RIGHT) {
				avatar.setTexture(carnivoreTextureRight);
			}
			else if (avatar.getDirection() == Dinosaur.DOWN) {
				avatar.setTexture(carnivoreTextureFront);
			}
		}

		if (removeClone == true){
				clone.deactivatePhysics(world);
				objects.remove(clone);
				clone = null;
				cloneLocation = null;
				removeClone = false;
		}
		if (InputHandler.getInstance().didAction()) {
			if (avatar.getForm() == Dinosaur.DOLL_FORM) {
				GameObject cotton= grid[(int)avatarGrid().x-1][(int)avatarGrid().y-1];
				if (cotton != null) {
					// Play sound
					cottonPickUp.play(1.0f);
					cotton.deactivatePhysics(world);
					objects.remove(cotton);
					avatarIdx--;
					cottonFlower.remove(cotton);
					grid[(int)((CottonFlower)cotton).getGridLocation().x-1][(int)((CottonFlower)cotton).getGridLocation().y-1] = null;
					avatar.incrementResources();
				}
				else if  (clone == null && avatar.getResources() >= 1) {
					Vector2 location = avatarGrid();
					float dwidth = dollTextureFront.getRegionWidth() / scale.x;
					float dheight = dollTextureFront.getRegionHeight() / scale.y;
					removeClone = false;
					GameObject goal = grid[(int)switchLocation.x-1][(int)switchLocation.y-1];
					if (direction == Dinosaur.UP) {
						if (location.y != GRID_MAX_Y && (objectInFrontOfAvatar()== null ||objectInFrontOfAvatar() == goal) ){
							clone = new Doll(screenToMaze(location.x), screenToMaze(location.y+1), dwidth);
							cloneLocation = new Vector2(location.x, location.y+1);
						}
					}
					else if (direction == Dinosaur.DOWN) {
						if (location.y != 1 && (objectInFrontOfAvatar()== null ||objectInFrontOfAvatar() == goal)){
							clone = new Doll(screenToMaze(location.x), screenToMaze(location.y - 1), dwidth);
							cloneLocation = new Vector2(location.x, location.y-1);
						}
					}
					else if (direction == Dinosaur.LEFT) {
						if (location.x != 1 && (objectInFrontOfAvatar()== null ||objectInFrontOfAvatar() == goal)){
							clone = new Doll(screenToMaze(location.x - 1), screenToMaze(location.y), dwidth);
							cloneLocation = new Vector2(location.x-1, location.y);
						}
					}
					else if (direction == Dinosaur.RIGHT) {
						if (location.x != GRID_MAX_X && (objectInFrontOfAvatar()== null ||objectInFrontOfAvatar() == goal)){
							clone = new Doll(screenToMaze(location.x+1), screenToMaze(location.y), dwidth);
							cloneLocation = new Vector2(location.x+1, location.y);
						}
					}
					if (clone != null) {
						clone.setTexture(dollTextureFront);
						clone.setDrawScale(scale);
						clone.setTexture(dollTextureFront);
						clone.setType(CLONE);
						clone.setBodyType(BodyDef.BodyType.StaticBody);
						addObject(clone);

						avatar.decrementResources();
					}

				}
			}
			else if (avatar.getForm() == Dinosaur.HERBIVORE_FORM) {
				GameObject tmp = objectInFrontOfAvatar();
				////("tmp: " + tmp);
				if (tmp != null && tmp.getType() == EDIBLEWALL && isOnGrid(0.5,0.5)){
					eatWall.play(1.0f);
					tmp.deactivatePhysics(world);
					objects.remove(tmp);
					avatarIdx--;
					walls.remove(tmp);
					grid[(int)((Wall)tmp).getGridLocation().x-1][(int)((Wall)tmp).getGridLocation().y-1] = null;
					avatar.incrementResources();
				}
			}
			else if (avatar.getForm() == Dinosaur.CARNIVORE_FORM) {
				if (!((Carnivore) avatar).inChargeCycle())
					((Carnivore) avatar).loadCharge();
			}
		}

		if (InputHandler.getInstance().didActionRelease()) {
			if (avatar.getForm() == Dinosaur.CARNIVORE_FORM) {
				if (((Carnivore) avatar).chargeReady())
					((Carnivore) avatar).charge();
				else
					((Carnivore) avatar).stopCharge();
			}
		}

		avatar.applyForce();

		// AI movement
		for (int i = 0; i < enemies.size();i++){
			controls[i].getMoveAlongPath();
		}


		// If we use sound, we must remember this.
		SoundController.getInstance().update();
		hud.update(avatar.getResources(), avatar.getForm());
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
		Fixture fix1 = contact.getFixtureA();
		Fixture fix2 = contact.getFixtureB();

		Body body1 = fix1.getBody();
		Body body2 = fix2.getBody();


		Object fd1 = fix1.getUserData();
		Object fd2 = fix2.getUserData();

		try {
			GameObject bd1 = (GameObject)body1.getUserData();
			GameObject bd2 = (GameObject)body2.getUserData();

			// Check for win condition
			handleCollision(bd1, bd2);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void handleCollision(GameObject bd1, GameObject bd2){
		boolean charging = false;
		if (bd1.getType() == CLONE){
			if (bd2.getType() == ENEMY){
				removeClone = true;
			}
		}
		else if (bd2.getType() == CLONE){
			if (bd1.getType() == ENEMY){
				removeClone = true;
			}

		}
		else
			removeClone = false;
		if (bd1.getType() == DUGGI){
			if (bd2.getType() == GOAL)
				setComplete(true);
			else if (bd2.getType() == ENEMY){
				if (((Dinosaur)bd1).getType() == Dinosaur.CARNIVORE_FORM)
					charging = ((Carnivore) bd1).getCharging();
				if (charging) {
					System.out.println("Collided mid charge!");
				} else {
					setFailure(true);
				}
			}
			else if (bd2.getType() == WALL){
				if (isInFrontOfAvatar(bd2)){
					// play sound effect
					collideWall.pause();
					collideWall.play(1.0f);
				}
			}
		}
		else if (bd2.getType() == DUGGI){
			////("kill me");
			if (bd1.getType() == GOAL)
				setComplete(true);
			else if (bd1.getType() == ENEMY) {
				if (((Dinosaur)bd2).getType() == Dinosaur.CARNIVORE_FORM)
					charging = ((Carnivore) bd2).getCharging();
				if (charging) {
					////("Collided mid charge!");
				} else {
					setFailure(true);
				}
			}
			else if (bd1.getType() == WALL){
				if (isInFrontOfAvatar(bd1)) {
					// play sound effect
					collideWall.pause();
					collideWall.play(1.0f);
				}
			}
		}
	}

	public boolean didExist(GameObject bd, PooledList<GameObject>list){
		for(GameObject l : list){
			if (bd == l) return true;
		}
		return false;
	}

	/**
	 *
	 * @param bd
	 * @return true if object is directly in front of avatar
	 */
	public boolean isInFrontOfAvatar(GameObject bd){
		int direction = avatar.getDirection();
		Vector2 location = avatarGrid();
		if (bd.getType() != WALL && bd.getType() != COTTON && bd.getType() != EDIBLEWALL){
			if (isAlignedHorizontally(avatar, bd, 0.7)){
				if (direction == Dinosaur.LEFT)
					return bd.getX() <= avatar.getX();
				else if (direction == Dinosaur.RIGHT)
					return bd.getX() >= avatar.getX();
				else return false;
			}
			else if (isAlignedVertically(avatar, bd, 0.7)){
				if (direction == Dinosaur.UP) {
					return bd.getY() >= avatar.getY();
				}
				else if (direction == Dinosaur.DOWN) {
					return bd.getY() <= avatar.getY();
				}
				else return false;
			}
		}
		else {
			if (bd.getType() == WALL){
				if (direction == Dinosaur.LEFT) {
					if (((Wall) bd).getGridLocation().x + 1 == location.x &&
							((Wall) bd).getGridLocation().y == location.y)
						return true;
				}
				else if (direction == Dinosaur.RIGHT){
					if (((Wall) bd).getGridLocation().x - 1 == location.x &&
							((Wall) bd).getGridLocation().y == location.y)
						return true;
				}
				else if (direction == Dinosaur.UP){
					if (((Wall) bd).getGridLocation().x == location.x &&
							((Wall) bd).getGridLocation().y + 1 == location.y)
						return true;
				}
				else if (direction == Dinosaur.DOWN){
					if (((Wall) bd).getGridLocation().x == location.x &&
							((Wall) bd).getGridLocation().y - 1 == location.y)
						return true;
				}
				return false;
			}
		}
		return false;
	}

	public GameObject objectInFrontOfAvatar(){
		int direction = avatar.getDirection();
		if (direction == Dinosaur.UP){
			if ((int)avatarGrid().y == GRID_MAX_Y) return null;
			else{
				return grid[(int)avatarGrid().x-1][(int)avatarGrid().y];
			}
		}
		else if (direction == Dinosaur.DOWN){
			if ((int)avatarGrid().y == 1) return null;
			else{
				return grid[(int)avatarGrid().x-1][(int)avatarGrid().y-2];
			}
		}
		else if (direction == Dinosaur.LEFT){
			if ((int)avatarGrid().x == 1) return null;
			else{
				return grid[(int)avatarGrid().x-2][(int)avatarGrid().y-1];
			}
		}
		else if (direction == Dinosaur.RIGHT){
			if ((int)avatarGrid().x == GRID_MAX_X) return null;
			else{
				return grid[(int)avatarGrid().x][(int)avatarGrid().y-1];
			}
		}
		return null;
	}

	/**
	 *
	 * @param bd1
	 * @param bd2
	 * @return true if they are aligned horizontally
	 */
	public boolean isAlignedHorizontally(GameObject bd1, GameObject bd2, double offset){
		return (Math.abs(bd1.getY() - bd2.getY()) <= offset);
	}


	/**
	 *
	 * @param bd1
	 * @param bd2
	 * @return true if they are aligned horizontally
	 */
	public boolean isAlignedVertically(GameObject bd1, GameObject bd2, double offset){
		return (Math.abs(bd1.getX() - bd2.getX()) <= offset);
	}

	public boolean isOnTop(GameObject bd1, GameObject bd2){
		return isAlignedVertically(bd1, bd2, 0.9) && isAlignedHorizontally(bd1, bd2, 0.9);
	}

	public GameObject getCotton(){
		for (int i = 0; i < cottonFlower.size(); i++){
			if (isOnTop(cottonFlower.get(i), avatar)) return cottonFlower.get(i);
		}
		return null;
	}

	public Vector2 avatarGrid(){
		return new Vector2(Math.round((avatar.getX()-1)/2+1), Math.round((avatar.getY()-1)/2+1));
	}

	public boolean isOnGrid(double x, double y){
		float gridx = screenToMaze(avatarGrid().x);
		float gridy = screenToMaze(avatarGrid().y);
		return (Math.abs(avatar.getX() - gridx) <= x) && (Math.abs(avatar.getY() - gridy) <= y);
	}

	/**
	 * Callback method for the start of a collision
	 *
	 * This method is called when two objects cease to touch.  The main use of this method
	 * is to determine when the characer is NOT on the ground.  This is how we prevent
	 * double jumping.
	 */
	public void endContact(Contact contact) {
		Fixture fix1 = contact.getFixtureA();
		Fixture fix2 = contact.getFixtureB();

		Body body1 = fix1.getBody();
		Body body2 = fix2.getBody();

		Object fd1 = fix1.getUserData();
		Object fd2 = fix2.getUserData();

		Object bd1 = body1.getUserData();
		Object bd2 = body2.getUserData();

		if (bd1 == avatar) {
			//("dsfhjskgfd");
			collidedWith.remove(bd2);
			directlyInFront = null;
		}
		else if (bd2 == avatar) {
			//("dsfhjskgfd");
			collidedWith.remove(bd1);
			directlyInFront = null;

		}
	}

	/** drawing on screen */
	public int screenToMaze (float f) {
		return (int)(1+2*(f-1));
	}

	public Vector2 screenToMazeVector(float x, float y){
		return new Vector2(screenToMaze(x), screenToMaze(y));
	}



	/** Change the music based on timestamp */
	public void changeMusic(Music name){
		// Change the music
		bgMusic.pause();
		float seconds = bgMusic.getPosition();
		bgMusic = name;
		bgMusic.setLooping(true);
		bgMusic.setVolume(0.10f);
		bgMusic.play();
		bgMusic.pause();
		bgMusic.setPosition(seconds);
		bgMusic.play();
	}

	/** Unused ContactListener method */
	public void postSolve(Contact contact, ContactImpulse impulse) {}
	/** Unused ContactListener method */
	public void preSolve(Contact contact, Manifold oldManifold) {}
}