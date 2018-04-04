package tiktaalik.trino;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import box2dLight.RayHandler;
import com.badlogic.gdx.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import tiktaalik.trino.duggi.*;
import tiktaalik.trino.enemy.AIController;
import tiktaalik.trino.enemy.Enemy;
import tiktaalik.trino.environment.FireFly;
import tiktaalik.trino.environment.Switch;
import tiktaalik.trino.environment.Wall;
import tiktaalik.trino.environment.CottonFlower;
import tiktaalik.trino.lights.ConeSource;
import tiktaalik.trino.lights.LightSource;
import tiktaalik.trino.lights.PointSource;
import tiktaalik.util.*;

/**
 * Base class for the game controller.
 */
public class GameController implements ContactListener, Screen {
	// Tracks the asset state.  Otherwise subclasses will try to load assets
	protected enum AssetState {
		EMPTY,
		LOADING,
		COMPLETE
	}

	// ASSET FILES AND VARIABLES
	private AssetState worldAssetState = AssetState.EMPTY; // Track asset loading from all instances and subclasses
	private Array<String> assets; // Track all loaded assets (for unloading purposes)

	// Sounds files
	private static String FONT_FILE = "shared/Montserrat/Montserrat-Bold.ttf";
	private static int FONT_SIZE = 64;

	// Texture files
	private static String BACKGROUND_FILE = "trino/background.png";
	private static String OVERLAY_FILE = "trino/overlay.png";
	private static String GOAL_FILE = "trino/openExitPlaceHolder.png";
	private static String GOAL_CLOSED_FILE = "trino/exitClosedPlaceholder.png";
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
	private static final String ENEMY_FILE_FRONT = "trino/enemy_trex_front.png";
	private static final String ENEMY_FILE_LEFT = "trino/enemy_trex_left.png";
	private static final String ENEMY_FILE_RIGHT = "trino/enemy_trex_right.png";
	private static final String ENEMY_FILE_BACK = "trino/enemy_trex_back.png";
	private static final String FIREFLY_FILE = "trino/enemy.png";
	private static final String WALL_FILE = "trino/wall_long.png";
	private static final String EDIBLE_WALL_FILE = "trino/ediblewall_long.png";
	private static final String COTTON_FLOWER_FILE = "trino/cotton.png";
	private static final String PATH_FILE = "trino/path.png";
	private static final String SWITCH_FILE = "trino/buttonRough.png";

	// Texture assets variables
	private BitmapFont displayFont;
	private TextureRegion background;
	private TextureRegion overlay;
	private TextureRegion goalTile;
	private TextureRegion goalClosedTile;

	// Texture assets for Duggi's three forms
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
	private TextureRegion enemyTextureFront;
	private TextureRegion enemyTextureLeft;
	private TextureRegion enemyTextureRight;
	private TextureRegion enemyTextureBack;
	private TextureRegion fireFlyTexture;
	private TextureRegion wallTexture;
	private TextureRegion edibleWallTexture;
	private TextureRegion cottonTexture;
	private TextureRegion switchTexture;

	// GAME CONSTANTS
	private static final int EXIT_COUNT = 60; // How many frames after winning/losing do we continue?
	public static final int EXIT_QUIT = 0; // Exit code for quitting the game
	public static final int EXIT_NEXT = 1; // Exit code for advancing to next level
	public static final int EXIT_PREV = 2; // Exit code for jumping back to previous level

	private static final float WORLD_STEP = 1/60.0f; // The amount of time for a physics engine step
	private static final int WORLD_VELOC = 6; // Number of velocity iterations for the constrain solvers
	private static final int WORLD_POSIT = 2; // Number of position iterations for the constrain solvers

	private static final float DEFAULT_WIDTH  = 32.0f; // Width of the game world in Box2d units
	private static final float DEFAULT_HEIGHT = 18.0f; // Height of the game world in Box2d units
	private static final float DEFAULT_GRAVITY = -0.0f; // The default value of gravity (going down)

	protected static final int COTTON = 0;
	protected static final int EDIBLEWALL = 1;
	protected static final int WALL = 2;
	protected static final int ENEMY = 3;
	protected static final int GOAL = 4;
	protected static final int DUGGI = 5;
	protected static final int CLONE = 6;
	protected static final int SWITCH = 7;
	protected static final int FIREFLY = 8;

	private static int GRID_MAX_X = 16;
	private static int GRID_MAX_Y = 8;

	// GAME VARIABLES
	private CollisionHandler collisionHandler;
	private HUDController hud;
	private ScreenListener listener; // Listener that will update the player mode when we are done

	protected Canvas canvas;
	protected PooledList<GameObject> objects  = new PooledList<GameObject>(); // All the objects in the world
	protected PooledList<GameObject> drawObjects  = new PooledList<GameObject>(); // Sortable list of objects for draw

	private PooledList<GameObject> addQueue = new PooledList<GameObject>(); // Queue for adding objects
	private PooledList<Wall> walls = new PooledList<Wall>();
	private PooledList<CottonFlower> cottonFlower = new PooledList<CottonFlower>();
	private PooledList<Enemy> enemies = new PooledList<Enemy>();
	private PooledList<AIController> controls = new PooledList<AIController>();
	private PooledList<FireFly> fireFlies = new PooledList<FireFly>();

	private GameObject[][] grid = new GameObject[GRID_MAX_X][GRID_MAX_Y];
	private World world;
	private Rectangle bounds; // The boundary of the world
	private Vector2 scale; // The world scale

	private boolean active; // Whether or not this is an active controller
	private boolean complete; // Whether we have completed this level
	private boolean failed; // Whether we have failed at this world (and need a reset)
	private int countdown; // Countdown active for winning or losing

	private Dinosaur avatar; // Reference to Duggi
	private Clone clone;
	private boolean removeClone = false;
	private Wall goalDoor;
	private Vector2 switchLocation = new Vector2(16, 6);

	/** The camera defining the RayHandler view; scale is in physics coordinates */
	protected OrthographicCamera raycamera;
	/** The rayhandler for storing lights, and drawing them (SIGH) */
	protected RayHandler rayhandler;
	/** All of the active lights that we loaded from the JSON file */
	private Array<LightSource> lights = new Array<LightSource>();
	/** The current light source being used.  If -1, there are no shadows */
	private int activeLight;

	private LightSource duggiLight;

	/** The reader to process JSON files */
	private JsonReader jsonReader;
	/** The JSON asset directory */
	private JsonValue  assetDirectory;
	/** The JSON defining the level model */
	private JsonValue  levelFormat;

	/**
	 * Preloads the assets for this controller.
	 *
	 * @param manager Reference to global asset manager.
	 */
	public void preLoadContent(AssetManager manager) {
		hud.preLoadContent(manager);
		if (worldAssetState != AssetState.EMPTY)
			return;
		
		worldAssetState = AssetState.LOADING;
		// Load the font
		FreetypeFontLoader.FreeTypeFontLoaderParameter size2Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
		size2Params.fontFileName = FONT_FILE;
		size2Params.fontParameters.size = FONT_SIZE;
		manager.load(FONT_FILE, BitmapFont.class, size2Params);
		assets.add(FONT_FILE);

		// Load textures
		manager.load(BACKGROUND_FILE,Texture.class);
		assets.add(BACKGROUND_FILE);
		manager.load(OVERLAY_FILE,Texture.class);
		assets.add(OVERLAY_FILE);
		manager.load(GOAL_FILE,Texture.class);
		assets.add(GOAL_FILE);
		manager.load(GOAL_CLOSED_FILE,Texture.class);
		assets.add(GOAL_CLOSED_FILE);
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
		manager.load(FIREFLY_FILE, Texture.class);
		assets.add(FIREFLY_FILE);
		manager.load(PATH_FILE, Texture.class);
		assets.add(PATH_FILE);
		manager.load(SWITCH_FILE, Texture.class);
		assets.add(SWITCH_FILE);

		jsonReader = new JsonReader();
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
		if (worldAssetState != AssetState.LOADING)
			return;
		
		// Allocate the font
		if (manager.isLoaded(FONT_FILE))
			displayFont = manager.get(FONT_FILE,BitmapFont.class);
		else
			displayFont = null;

		// Allocate the textures
		background = createTexture(manager,BACKGROUND_FILE,true);
		overlay = createTexture(manager,OVERLAY_FILE,true);
		goalTile  = createTexture(manager,GOAL_FILE,true);
		goalClosedTile =  createTexture(manager,GOAL_CLOSED_FILE, true);
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
		fireFlyTexture = createTexture(manager, FIREFLY_FILE, false);
		wallTexture = createTexture(manager,WALL_FILE,false);
		edibleWallTexture = createTexture(manager, EDIBLE_WALL_FILE, false);
		cottonTexture = createTexture(manager, COTTON_FLOWER_FILE, false);
		switchTexture = createTexture(manager, SWITCH_FILE, false);

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
	 * @param manager Reference to global asset manager.
	 */
	public void unloadContent(AssetManager manager) {
		hud.unloadContent(manager);
    	for(String s : assets) {
    		if (manager.isLoaded(s))
    			manager.unload(s);
    	}
	}

	/**
	 * Sets whether the level is completed.
	 *
	 * @param value whether the level is completed.
	 */
	public void setComplete(boolean value) {
		if (value)
			countdown = EXIT_COUNT;

		complete = value;
	}

	/**
	 * Sets whether the level is failed.
	 *
	 * @param value whether the level is failed.
	 */
	public void setFailure(boolean value) {
			if (value)
			countdown = EXIT_COUNT;

		failed = value;
	}

	/**
	 * Returns the canvas associated with this controller
	 *
	 * @return the canvas associated with this controller
	 */
	public Canvas getCanvas() {
		return canvas;
	}
	
	/**
	 * Sets the canvas associated with this controller
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
	 */
	protected GameController() {
		this(new Rectangle(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT),
				new Vector2(0, DEFAULT_GRAVITY));

		jsonReader = new JsonReader();
		setComplete(false);
		setFailure(false);
		world.setContactListener(this);
	}

	/**
	 * Creates a new game world
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
		collisionHandler = new CollisionHandler(this);
		hud = new HUDController();
	}
	
	/**
	 * Dispose of all (non-static) resources allocated to this mode.
	 */
	public void dispose() {
		for(LightSource light : lights) {
			light.remove();
		}
		lights.clear();

		if (rayhandler != null) {
			rayhandler.dispose();
			rayhandler = null;
		}

		for(GameObject g : objects)
			g.deactivatePhysics(world);
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
	 * Immediately adds the object to the physics world
	 *
	 * @param g The object to add
	 */
	protected void addObject(GameObject g) {
		assert inBounds(g) : "Object is not in bounds";
		objects.add(g);

		if (g.getType()!= COTTON && g.getType()!= SWITCH) {
			g.activatePhysics(world);
		}
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

	public void addFireFly(FireFly obj){
		assert inBounds(obj) : "Objects is no in bounds";
		fireFlies.add(obj);
	}

	/**
	 * Returns true if the object is in bounds.
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

		for(GameObject g : objects)
			g.deactivatePhysics(world);
		objects.clear();
		enemies.clear();
		fireFlies.clear();
		controls.clear();
		addQueue.clear();
		world.dispose();

		world = new World(gravity,false);
		world.setContactListener(this);

		SoundController.getInstance().dispose();
		SoundController.getInstance().init();
		SoundController.getInstance().playBackground(Dinosaur.DOLL_FORM);

		setComplete(false);
		setFailure(false);
		clone = null;

		// Reload the json each time
		levelFormat = jsonReader.parse(Gdx.files.internal("jsons/level.json"));
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
		InputHandler input = InputHandler.getInstance();
		input.readInput();
		if (listener == null)
			return true;
		
		// Handle resets
		if (input.didReset())
			reset();

		// Handle nightmode
		if (input.didNight())
			if (duggiLight.isActive()){
				duggiLight.setActive(false);
				rayhandler.setAmbientLight(1.0f,1.0f,1.0f,1.0f);
			} else {
				duggiLight.setActive(true);
				rayhandler.setAmbientLight(0.25f,0.25f,0.25f,0.25f);
			}


		// Reset level when colliding with enemy
		if (countdown > 0) {
			countdown--;
		} else if (countdown == 0) {
			if (failed || complete) {
				reset();
			}
		}

		return true;
	}
	
	/**
	 * Processes physics
	 *
	 * @param dt Number of seconds since last animation frame
	 */
	public void postUpdate(float dt) {
		// Add any objects created by actions
		while (!addQueue.isEmpty())
			addObject(addQueue.poll());
		
		// Turn the physics engine crank.
		world.step(WORLD_STEP,WORLD_VELOC,WORLD_POSIT);

		// Garbage collect the deleted objects.
		Iterator<PooledList<GameObject>.Entry> iterator = objects.entryIterator();
		while (iterator.hasNext()) {
			PooledList<GameObject>.Entry entry = iterator.next();
			GameObject g = entry.getValue();
			if (g.isRemoved()) {
				g.deactivatePhysics(world);
				entry.remove();
			} else {
				g.update(dt);
			}
		}
	}
	
	/**
	 * Draw everything to the canvas
	 *
	 * @param delta The difference from the last draw call
	 */
	public void draw(float delta) {
		canvas.clear();
		
		canvas.begin();
		while (drawObjects.size() < objects.size())
			drawObjects.add(null);
		while (drawObjects.size() > objects.size())
			drawObjects.pop();

		Collections.copy(drawObjects, objects);
		Collections.sort(drawObjects, new Comparator<GameObject>() {
			@Override
			public int compare(GameObject g1, GameObject g2) {
				if (g1.getType() == GOAL)
					return -1;
				if (g2.getType() == GOAL)
					return 1;

				if (g1.getType() == SWITCH)
					return -1;
				if (g2.getType() == SWITCH)
					return 1;

				if (g1.getType() == COTTON)
					return -1;
				if (g2.getType() == COTTON)
					return 1;
				return (int)(g2.getY() - g1.getY());
			}
		});
		canvas.draw(background,0,0);
		canvas.draw(background,1270,0);
		for(GameObject g : drawObjects)
			g.draw(canvas);
		canvas.draw(overlay,0,0);
		canvas.end();

		// Now draw the shadows
		if (rayhandler != null && activeLight != -1) {
			rayhandler.render();
		}
		
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
				update(delta);
				postUpdate(delta);
			}
			draw(delta);
			hud.draw();
		}
	}
	
	/**
	 * Called when this screen becomes the current screen for a Game.
	 */
	public void show() {
		active = true;
	}

	/**
	 * Called when this screen is no longer the current screen for a Game.
	 */
	public void hide() {
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
		// Create the lighting if appropriate
		if (levelFormat.has("lighting")) {
			initLighting(levelFormat.get("lighting"));
		}
		rayhandler.setAmbientLight(1.0f,1.0f,1.0f,1.0f);

		duggiLight = new PointSource(rayhandler, 512, Color.WHITE, 7, 0, 0);
		duggiLight.setColor(1.0f,1.0f,1.0f,1.0f);
		duggiLight.setSoft(true);
		duggiLight.setActive(false);

		// Create player character
		// It is important that this is always created first, as transformations must swap the first element
		// in the objects list

		float dwidth = dollTextureRight.getRegionWidth() / (scale.x * 2);
		float dheight = dollTextureRight.getRegionHeight() / scale.y;
		avatar = new Doll(screenToMaze(8), screenToMaze(6), dwidth);
		avatar.setType(DUGGI);
		avatar.setTextureSet(dollTextureLeft, dollTextureRight, dollTextureBack, dollTextureFront);
		avatar.setDrawScale(scale);
		addObject(avatar);
		duggiLight.attachToBody(avatar.getBody(), duggiLight.getX(), duggiLight.getY(), duggiLight.getDirection());


		/** Adding cotton flowers */
		dwidth = cottonTexture.getRegionWidth() / scale.x;
		dheight = cottonTexture.getRegionHeight() / scale.y;
		CottonFlower cf1 = new CottonFlower(1,5, screenToMaze(1), screenToMaze(5), dwidth, dheight);
		CottonFlower cf2 = new CottonFlower(10,4, screenToMaze(10), screenToMaze(4), dwidth, dheight);
		CottonFlower cf3 = new CottonFlower(12,7,screenToMaze(12), screenToMaze(7), dwidth, dheight);
		CottonFlower cf4 = new CottonFlower(15,1,screenToMaze(15), screenToMaze(1), dwidth, dheight);
		CottonFlower cf5 = new CottonFlower(17,1,screenToMaze(18), screenToMaze(8), dwidth, dheight);
		CottonFlower cf6 = new CottonFlower(25,8,screenToMaze(25), screenToMaze(8), dwidth, dheight);
		CottonFlower cf7 = new CottonFlower(29,4,screenToMaze(29), screenToMaze(4), dwidth, dheight);
		CottonFlower cf8 = new CottonFlower(32,3,screenToMaze(32), screenToMaze(3), dwidth, dheight);
		CottonFlower cf9 = new CottonFlower(32,7,screenToMaze(32), screenToMaze(7), dwidth, dheight);
		CottonFlower[] cf = new CottonFlower[] {cf1, cf2, cf3, cf4, cf5, cf6, cf7, cf8, cf9};
		for (int i = 0; i < 9; i++) {
			cf[i].setBodyType(BodyDef.BodyType.StaticBody);
			cf[i].setDrawScale(scale);
			cf[i].setTexture(cottonTexture);
			cf[i].setType(COTTON);
			addObject(cf[i]);
			addCottonFlower(cf[i]);
			//grid[(int)cf[i].getGridLocation().x-1][(int)cf[i].getGridLocation().y-1] = cf[i];
		}


		// Switch
		dwidth = switchTexture.getRegionWidth() / scale.x;
		dheight = switchTexture.getRegionHeight() / scale.y;
		// Switch texture
		Switch s = new Switch(28,1,screenToMaze(28),screenToMaze(1),dwidth,dheight);
		s.setBodyType(BodyDef.BodyType.StaticBody);
		s.setDrawScale(scale);
		s.setTexture(switchTexture);
		s.setType(SWITCH);
		addObject(s);
		//grid[(int)s.getGridLocation().x-1][(int)s.getGridLocation().y-1] = s;

		/** Adding inedible walls */
		Wall iw1 = new Wall(1,1,screenToMaze(1), screenToMaze(1), dwidth, dheight, false);
		Wall iw2 = new Wall(1,7,screenToMaze(1), screenToMaze(7), dwidth, dheight, false);
		Wall iw3 = new Wall(2,5,screenToMaze(2), screenToMaze(5), dwidth, dheight, false);
		Wall iw4 = new Wall(3,3,screenToMaze(3), screenToMaze(3), dwidth, dheight, false);
		Wall iw5 = new Wall(3,4,screenToMaze(3), screenToMaze(4), dwidth, dheight, false);
		Wall iw6 = new Wall(3,5,screenToMaze(3), screenToMaze(5), dwidth, dheight, false);
		Wall iw7 = new Wall(3,6,screenToMaze(3), screenToMaze(6), dwidth, dheight, false);
		Wall iw8 = new Wall(3,7,screenToMaze(3), screenToMaze(7), dwidth, dheight, false);
		Wall iw9 = new Wall(6,3,screenToMaze(6), screenToMaze(3), dwidth, dheight, false);
		Wall iw10 = new Wall(6,5,screenToMaze(6), screenToMaze(5), dwidth, dheight, false);
		Wall iw11 = new Wall(6,6,screenToMaze(6), screenToMaze(6), dwidth, dheight, false);
		Wall iw12 = new Wall(6,7,screenToMaze(6), screenToMaze(7), dwidth, dheight, false);
		Wall iw13 = new Wall(7,4,screenToMaze(7), screenToMaze(4), dwidth, dheight, false);
		Wall iw14 = new Wall(7,5,screenToMaze(7), screenToMaze(5), dwidth, dheight, false);
		Wall iw15 = new Wall(7,6,screenToMaze(7), screenToMaze(6), dwidth, dheight, false);
		Wall iw16 = new Wall(8,5,screenToMaze(8), screenToMaze(5), dwidth, dheight, false);
		Wall iw17 = new Wall(9,4,screenToMaze(9), screenToMaze(4), dwidth, dheight, false);
		Wall iw18 = new Wall(9,5,screenToMaze(9), screenToMaze(5), dwidth, dheight, false);
		Wall iw19 = new Wall(10,5,screenToMaze(10), screenToMaze(5), dwidth, dheight, false);
		Wall iw20 = new Wall(11,5,screenToMaze(11), screenToMaze(5), dwidth, dheight, false);
		Wall iw21 = new Wall(11,7,screenToMaze(11), screenToMaze(7), dwidth, dheight, false);
		Wall iw22 = new Wall(13,7,screenToMaze(13), screenToMaze(7), dwidth, dheight, false);
		Wall iw23 = new Wall(15,7,screenToMaze(15), screenToMaze(7), dwidth, dheight, false);
		Wall iw24 = new Wall(16,1,screenToMaze(16), screenToMaze(1), dwidth, dheight, false);
		Wall iw25 = new Wall(16,2,screenToMaze(16), screenToMaze(2), dwidth, dheight, false);
		Wall iw26 = new Wall(16,3,screenToMaze(16), screenToMaze(3), dwidth, dheight, false);
		Wall iw27 = new Wall(16,4,screenToMaze(16), screenToMaze(4), dwidth, dheight, false);
		Wall iw28 = new Wall(17,2,screenToMaze(17), screenToMaze(2), dwidth, dheight, false);
		Wall iw29 = new Wall(17,7,screenToMaze(17), screenToMaze(7), dwidth, dheight, false);
		Wall iw30 = new Wall(18,6,screenToMaze(18), screenToMaze(6), dwidth, dheight, false);
		Wall iw31 = new Wall(19,6, screenToMaze(19), screenToMaze(6), dwidth, dheight, false);
		Wall iw32 = new Wall(19,7,screenToMaze(19), screenToMaze(7), dwidth, dheight, false);
		Wall iw33 = new Wall(19,8,screenToMaze(19), screenToMaze(8), dwidth, dheight, false);
		Wall iw34 = new Wall(20,6,screenToMaze(20), screenToMaze(6), dwidth, dheight, false);
		Wall iw35 = new Wall(21,5,screenToMaze(21), screenToMaze(5), dwidth, dheight, false);
		Wall iw36 = new Wall(22,4,screenToMaze(22), screenToMaze(4), dwidth, dheight, false);
		Wall iw37 = new Wall(22,7,screenToMaze(22), screenToMaze(7), dwidth, dheight, false);
		Wall iw38 = new Wall(22,8,screenToMaze(22), screenToMaze(8), dwidth, dheight, false);
		Wall iw39 = new Wall(23,3,screenToMaze(23), screenToMaze(3), dwidth, dheight, false);
		Wall iw40 = new Wall(23,7,screenToMaze(23), screenToMaze(7), dwidth, dheight, false);
		Wall iw41 = new Wall(25,2,screenToMaze(25), screenToMaze(2), dwidth, dheight, false);
		Wall iw42 = new Wall(25,5,screenToMaze(25), screenToMaze(5), dwidth, dheight, false);
		Wall iw43 = new Wall(26,5,screenToMaze(26), screenToMaze(5), dwidth, dheight, false);
		Wall iw44 = new Wall(27,5,screenToMaze(27), screenToMaze(5), dwidth, dheight, false);
		Wall iw45 = new Wall(29,5,screenToMaze(29), screenToMaze(5), dwidth, dheight, false);
		Wall iw46 = new Wall(30,3,screenToMaze(30), screenToMaze(3), dwidth, dheight, false);
		Wall iw47 = new Wall(30,4,screenToMaze(30), screenToMaze(4), dwidth, dheight, false);
		Wall iw48 = new Wall(30,5,screenToMaze(30), screenToMaze(5), dwidth, dheight, false);
		Wall iw49 = new Wall(32,2,screenToMaze(32), screenToMaze(2), dwidth, dheight, false);


		Wall ew1 = new Wall(6,4,screenToMaze(6), screenToMaze(4), dwidth, dheight, true);
		Wall ew2 = new Wall(6,8,screenToMaze(6), screenToMaze(8), dwidth, dheight, true);
		Wall ew3 = new Wall(7,7,screenToMaze(7), screenToMaze(7), dwidth, dheight, true);
		Wall ew4 = new Wall(8,2,screenToMaze(8), screenToMaze(2), dwidth, dheight, true);
		Wall ew5 = new Wall(9,6,screenToMaze(9), screenToMaze(6), dwidth, dheight, true);
		Wall ew6 = new Wall(9,7,screenToMaze(9), screenToMaze(7), dwidth, dheight, true);
		Wall ew7 = new Wall(10,2,screenToMaze(10), screenToMaze(2), dwidth, dheight, true);
		Wall ew8 = new Wall(12,4,screenToMaze(12), screenToMaze(4), dwidth, dheight, true);
		Wall ew9 = new Wall(22,5,screenToMaze(22), screenToMaze(5), dwidth, dheight, true);
		Wall ew10 = new Wall(23,4,screenToMaze(23), screenToMaze(4), dwidth, dheight, true);
		Wall ew11 = new Wall(23,5,screenToMaze(23), screenToMaze(5), dwidth, dheight, true);
		Wall ew12 = new Wall(24,4,screenToMaze(24), screenToMaze(4), dwidth, dheight, true);
		Wall ew13 = new Wall(25,4,screenToMaze(25), screenToMaze(4), dwidth, dheight, true);
		Wall ew14 = new Wall(28,2,screenToMaze(28), screenToMaze(2), dwidth, dheight, true);
		Wall ew15 = new Wall(29,2,screenToMaze(29), screenToMaze(2), dwidth, dheight, true);
		Wall ew16 = new Wall(29,6,screenToMaze(29), screenToMaze(6), dwidth, dheight, true);
		Wall ew17 = new Wall(31,8,screenToMaze(31), screenToMaze(8), dwidth, dheight, true);
		Wall ew18 = new Wall(32,8,screenToMaze(32), screenToMaze(8), dwidth, dheight, true);

		Wall[] iw = new Wall[] {iw1, iw2, iw3, iw4, iw5, iw6, iw7, iw8, iw9, iw10, iw11, iw12, iw13, iw14,
				iw15, iw16, iw17, iw18, iw19, iw20, iw21, iw22, iw23, iw24, iw25, iw26, iw27, iw28,
				iw29, iw30, iw31, iw32, iw33, iw34, iw35, iw36, iw37, iw38, iw39, iw40, iw41, iw42, iw43,
				iw44, iw45, iw46, iw47, iw48, iw49, ew1, ew2, ew3, ew4, ew5, ew6, ew7, ew8,
				ew9, ew10, ew11, ew12, ew13, ew14, ew15, ew16, ew17, ew18};

		for (int i = iw.length - 1; i >= 0; i--) {
			iw[i].setBodyType(BodyDef.BodyType.StaticBody);
			iw[i].setDrawScale(scale);
			if (iw[i].getEdible()) {
				iw[i].setTexture(edibleWallTexture);
				iw[i].setType(EDIBLEWALL);
			}
			else {

				iw[i].setTexture(wallTexture);
				iw[i].setType(WALL);
			}
			addObject(iw[i]);
			addWall(iw[i]);
			//grid[(int)iw[i].getGridLocation().x-1][(int)iw[i].getGridLocation().y-1] = iw[i];
		}

		// Add level goal
		dwidth = goalTile.getRegionWidth() / scale.x;
		dheight = goalTile.getRegionHeight() / scale.y;
		goalDoor = new Wall(8,4,screenToMaze(8), screenToMaze(4), dwidth, dheight, false);
		goalDoor.setBodyType(BodyDef.BodyType.StaticBody);
		goalDoor.setSensor(true);
		goalDoor.setDrawScale(scale);
		goalDoor.setTexture(goalClosedTile);
		goalDoor.setName("exit");
		goalDoor.setType(GOAL);
		addObject(goalDoor);

		// Create enemy
		dwidth = carnivoreTextureFront.getRegionWidth() / (scale.x * 2);
		dheight = carnivoreTextureFront.getRegionHeight() / scale.y;

		// Adding the rest of the enemies; they're static right now
		Enemy en1 = new Enemy(screenToMaze(1), screenToMaze(2), dwidth,0);
		Enemy en2 = new Enemy(screenToMaze(3), screenToMaze(1), dwidth,1);
		Enemy en3 = new Enemy(screenToMaze(10), screenToMaze(6), dwidth,2);
		Enemy en4 = new Enemy(screenToMaze(13), screenToMaze(4), dwidth,3);
		Enemy en5 = new Enemy(screenToMaze(14), screenToMaze(2), dwidth,4);
		Enemy en6 = new Enemy(screenToMaze(15), screenToMaze(3), dwidth,5);
		Enemy en7 = new Enemy(screenToMaze(17), screenToMaze(8), dwidth,6);
		Enemy en8 = new Enemy(screenToMaze(20), screenToMaze(5), dwidth,7);
		Enemy en9 = new Enemy(screenToMaze(21), screenToMaze(4), dwidth,8);
		Enemy en10 = new Enemy(screenToMaze(21), screenToMaze(6), dwidth,9);
		Enemy en11 = new Enemy(screenToMaze(22), screenToMaze(3), dwidth,10);
		Enemy en12 = new Enemy(screenToMaze(23), screenToMaze(8), dwidth,11);
		Enemy en13 = new Enemy(screenToMaze(27), screenToMaze(4), dwidth,12);
		Enemy en14 = new Enemy(screenToMaze(28), screenToMaze(5), dwidth,13);
		Enemy en15 = new Enemy(screenToMaze(30), screenToMaze(8), dwidth,14);
		Enemy en16 = new Enemy(screenToMaze(31), screenToMaze(1), dwidth,15);
		Enemy en17 = new Enemy(screenToMaze(32), screenToMaze(6), dwidth,16);
		Enemy[] en = new Enemy[]{en1,en2,en3,en4,en5,en6,en7,en8,en9,en10,en11,en12,en13,en14,en15,
		en16,en17};

		Vector2[] p1 = new Vector2[]{screenToMazeVector(1,2),
				screenToMazeVector(1,3),screenToMazeVector(1,4),screenToMazeVector(1,5),
				screenToMazeVector(1,6)};
		Vector2[] p2 = new Vector2[]{screenToMazeVector(2,1),
				screenToMazeVector(3,1),screenToMazeVector(4,1),screenToMazeVector(5,1),
				screenToMazeVector(6,1)};
		Vector2[] p3 = new Vector2[]{screenToMazeVector(10,6),
				screenToMazeVector(11,6),screenToMazeVector(12,6),screenToMazeVector(13,6),
				screenToMazeVector(14,6),screenToMazeVector(15,6),screenToMazeVector(16,6),
				screenToMazeVector(17,6)};
		Vector2[] p4 = new Vector2[]{screenToMazeVector(13,4),
				screenToMazeVector(14,4),screenToMazeVector(15,4)};
		Vector2[] p5 = new Vector2[]{screenToMazeVector(14,2),
				screenToMazeVector(13,2),screenToMazeVector(12,2), screenToMazeVector(11,2)};
		Vector2[] p6 = new Vector2[]{screenToMazeVector(15,3),screenToMazeVector(14,3),
				screenToMazeVector(13,3),screenToMazeVector(12,3),screenToMazeVector(11,3)};
		Vector2[] p7 = new Vector2[]{screenToMazeVector(17,8),screenToMazeVector(16,8),
				screenToMazeVector(15,8),screenToMazeVector(14,8),screenToMazeVector(13,8),
				screenToMazeVector(12,8),screenToMazeVector(11,8),screenToMazeVector(10,8),
				screenToMazeVector(9,8),screenToMazeVector(8,8)};
		Vector2[] p8 = new Vector2[]{screenToMazeVector(20,5),screenToMazeVector(19,5),
				screenToMazeVector(18,5),screenToMazeVector(17,5)};
		Vector2[] p9 = new Vector2[]{screenToMazeVector(21,4),screenToMazeVector(20,4),
				screenToMazeVector(19,4),screenToMazeVector(18,4),screenToMazeVector(17,4)};
		Vector2[] p10 = new Vector2[]{screenToMazeVector(21,6),screenToMazeVector(22,6),
				screenToMazeVector(23,6),screenToMazeVector(24,6),screenToMazeVector(25,6)};
		Vector2[] p11 = new Vector2[]{screenToMazeVector(22,3),screenToMazeVector(21,3),
				screenToMazeVector(20,3),screenToMazeVector(19,3),screenToMazeVector(18,3),
				screenToMazeVector(17,3)};
		Vector2[] p12 = new Vector2[]{screenToMazeVector(23,8),screenToMazeVector(24,8)};
		Vector2[] p13 = new Vector2[]{screenToMazeVector(27,4),screenToMazeVector(27,3),
				screenToMazeVector(27,2),screenToMazeVector(27,1)};
		Vector2[] p14 = new Vector2[]{screenToMazeVector(28,7),screenToMazeVector(28,6),
				screenToMazeVector(28,5),screenToMazeVector(28,4),screenToMazeVector(28,3)};
		Vector2[] p15 = new Vector2[]{screenToMazeVector(30,8),screenToMazeVector(29,8),
				screenToMazeVector(28,8),screenToMazeVector(27,8)};
		Vector2[] p16 = new Vector2[]{screenToMazeVector(31,1),screenToMazeVector(30,1),
				screenToMazeVector(29,1),screenToMazeVector(28,1),screenToMazeVector(27,1),
				screenToMazeVector(26,1),screenToMazeVector(25,1)};
		Vector2[] p17 = new Vector2[]{screenToMazeVector(32,6),screenToMazeVector(31,6),
				screenToMazeVector(30,6)};
		Vector2[][] pathList = new Vector2[][]{p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12,p13,p14,p15,p16,p17};

		int[] dirList = new int[]{Dinosaur.UP, Dinosaur.RIGHT, Dinosaur.RIGHT, Dinosaur.RIGHT,
				Dinosaur.LEFT, Dinosaur.LEFT, Dinosaur.LEFT, Dinosaur.LEFT, Dinosaur.LEFT, Dinosaur.RIGHT,
				Dinosaur.LEFT, Dinosaur.RIGHT, Dinosaur.DOWN, Dinosaur.DOWN, Dinosaur.LEFT, Dinosaur.LEFT,
				Dinosaur.LEFT};

		for (int i = 0; i < 17; i++) {
			en[i].setType(ENEMY);
			en[i].setDrawScale(scale);
			en[i].setTexture(enemyTextureBack);
			en[i].setDirection(dirList[0]);
			addObject(en[i]);
			addEnemy(en[i]);
		}

		for (int i = 0; i < en.length; i++)
			controls.add(new AIController(i,avatar,en,pathList[i]));


//		PointSource fireLight = new PointSource(rayhandler, 512, Color.WHITE, 3, 0, 0);
//		fireLight.setColor(1.0f,1.0f,1.0f,1.0f);
//		fireLight.setSoft(true);
//		fireLight.setActive(true);
//
//		dwidth = fireFlyTexture.getRegionWidth() / (scale.x * 2);
//		dheight = fireFlyTexture.getRegionHeight() / scale.y;
//		FireFly ff = new FireFly(screenToMaze(1), screenToMaze(5), dwidth,0);
//		ff.setType(FIREFLY);
//		ff.setTexture(fireFlyTexture);
//		ff.setDrawScale(scale);
//		addObject(ff);
//		addFireFly(ff);
//		fireLight.attachToBody(ff.getBody(), fireLight.getX(), fireLight.getY(), fireLight.getDirection());

	}

	/**
	 * The core gameplay loop of this world.
	 *
	 * @param dt Number of seconds since last animation frame
	 */
	public void update(float dt) {
		if (rayhandler != null) {
			rayhandler.update();
		}

		// Camera Follow
		float halfWidth = canvas.getCamera().viewportWidth / 2;
		float halfHeight = canvas.getCamera().viewportHeight / 2;

		if ((avatar.getX()/bounds.width)*canvas.getCamera().viewportWidth < halfWidth){
			canvas.getCamera().position.x = halfWidth;
		} else if ((avatar.getX()/bounds.width)*canvas.getCamera().viewportWidth > 2560 - halfWidth){
			canvas.getCamera().position.x = 2560 - halfWidth;
		} else {
			canvas.getCamera().position.x = (avatar.getX()/bounds.width)*canvas.getCamera().viewportWidth;
		}

		if ((avatar.getY()/bounds.height)*canvas.getCamera().viewportHeight < halfHeight){
			canvas.getCamera().position.y = halfHeight;
		} else if ((avatar.getY()/bounds.height)*canvas.getCamera().viewportHeight > 720 - halfHeight){
			canvas.getCamera().position.y = 720 - halfHeight;
		} else {
			canvas.getCamera().position.y = (avatar.getY()/bounds.height)*canvas.getCamera().viewportHeight;
		}
		canvas.getCamera().update();

		raycamera.position.set(avatar.getX(), avatar.getY(),0);
		raycamera.update();
		rayhandler.setCombinedMatrix(raycamera);

		int random = MathUtils.random(4);

//		for(FireFly f: fireFlies){
//			Vector2 sample = new Vector2(screenToMaze(14),screenToMaze(5));
////			Vector2 randomLocation = new Vector2(screenToMaze(MathUtils.random(1,17)),screenToMaze(MathUtils.random(1,9)));
//			random = MathUtils.random(4);
//			Vector2 step = sample.cpy().sub(f.getPosition()).nor().scl(.025f);
//			f.setPosition(f.getX() + step.x, f.getY() + step.y);
//		}



		int direction = avatar.getDirection();

			avatar.setLeftRight(InputHandler.getInstance().getHorizontal());
			avatar.setUpDown(InputHandler.getInstance().getVertical());


////		else {
////			if (avatar.getForm() == Dinosaur.CARNIVORE_FORM && ((Carnivore) avatar).getCharging())
////				((Carnivore) avatar).stopCharge();
////		}

		if (InputHandler.getInstance().didTransform()) {
			if (avatar.canTransform()) {
				if (InputHandler.getInstance().didTransformDoll() &&
						avatar.getForm() != Dinosaur.DOLL_FORM) {
					avatar = avatar.transformToDoll();
					avatar.setTextureSet(dollTextureLeft, dollTextureRight, dollTextureBack, dollTextureFront);
					objects.set(1, avatar);

					SoundController.getInstance().changeBackground(Dinosaur.DOLL_FORM);
					SoundController.getInstance().playTransform();
				} else if (InputHandler.getInstance().didTransformHerbi() &&
						avatar.getForm() != Dinosaur.HERBIVORE_FORM) {
					avatar = avatar.transformToHerbivore();
					avatar.setTextureSet(herbivoreTextureLeft, herbivoreTextureRight, herbivoreTextureBack,
							herbivoreTextureFront);
					objects.set(1, avatar);

					SoundController.getInstance().changeBackground(Dinosaur.HERBIVORE_FORM);
					SoundController.getInstance().playTransform();
				} else if (InputHandler.getInstance().didTransformCarni() &&
						avatar.getForm() != Dinosaur.CARNIVORE_FORM) {
					avatar = avatar.transformToCarnivore();
					avatar.setTextureSet(carnivoreTextureLeft, carnivoreTextureRight, carnivoreTextureBack,
							carnivoreTextureFront);
					objects.set(1, avatar);

					SoundController.getInstance().changeBackground(Dinosaur.CARNIVORE_FORM);
					SoundController.getInstance().playTransform();
				}
			}
		}

		if (avatar.getForm() == Dinosaur.CARNIVORE_FORM && ((Carnivore) avatar).getCharging() &&
				avatar.getLinearVelocity().len2() < 5)
			((Carnivore) avatar).stopCharge();
		if (clone != null &&(removeClone || clone.getRemoved())) {
			clone.deactivatePhysics(world);
			objects.remove(clone);
			removeClone = false;
			clone.setRemoved(false);
			clone = null;
		}

		// Check if Duggi or Clone is on top of button
		if (clone != null && clone.getGridLocation() != null && clone.getGridLocation().equals(new Vector2(16,6))) {
			avatar.setCanExit(true);
			goalDoor.setTexture(goalTile);
		} else {
			avatar.setCanExit(false);
			goalDoor.setTexture(goalClosedTile);
		}
		if (InputHandler.getInstance().didAction()) {
			if (avatar.getForm() == Dinosaur.DOLL_FORM) {
				GameObject cotton= grid[(int)avatarGrid().x-1][(int)avatarGrid().y-1];
				if (cotton != null && cotton.getType() == COTTON) {
					SoundController.getInstance().playCottonPickup();
					cotton.deactivatePhysics(world);
					objects.remove(cotton);
					cottonFlower.remove(cotton);
					grid[(int)((CottonFlower)cotton).getGridLocation().x-1][(int)((CottonFlower)cotton).getGridLocation().y-1] = null;
					avatar.incrementResources();
				}

				else if (clone == null && avatar.getResources() >= 1) {
					Vector2 location = avatarGrid();
					GameObject goal = grid[(int)switchLocation.x-1][(int)switchLocation.y-1];
					float dwidth = dollTextureFront.getRegionWidth() / scale.x;
					removeClone = false;
					if (direction == Dinosaur.UP) {
						if (location.y != GRID_MAX_Y && (objectInFrontOfAvatar()== null || objectInFrontOfAvatar() == goal) ) {
							clone = new Clone(screenToMaze(location.x), screenToMaze(location.y+1), dwidth);
							clone.setGridLocation(location.x, location.y+1);
						}
					}
					else if (direction == Dinosaur.DOWN) {
						if (location.y != 1 && (objectInFrontOfAvatar()== null ||objectInFrontOfAvatar() == goal)) {
							clone = new Clone(screenToMaze(location.x), screenToMaze(location.y-1), dwidth);
							clone.setGridLocation(location.x, location.y-1);
						}
					}
					else if (direction == Dinosaur.LEFT) {
						if (location.x != 1 && (objectInFrontOfAvatar()== null ||objectInFrontOfAvatar() == goal)){
							clone = new Clone(screenToMaze(location.x-1), screenToMaze(location.y), dwidth);
							clone.setGridLocation(location.x-1, location.y);
						}
					}
					else if (direction == Dinosaur.RIGHT) {
						if (location.x != GRID_MAX_X && (objectInFrontOfAvatar()== null ||objectInFrontOfAvatar() == goal)){
							clone = new Clone(screenToMaze(location.x+1), screenToMaze(location.y), dwidth);
							clone.setGridLocation(location.x+1, location.y);
						}
					}

					if (clone != null) {
						clone.setTexture(dollTextureFront);
						clone.setDrawScale(scale);
						clone.setType(CLONE);
						clone.setBodyType(BodyDef.BodyType.StaticBody);
						addObject(clone);
						avatar.decrementResources();
					}


				} else if (clone != null) {
					removeClone = true;
				}
			}
			else if (avatar.getForm() == Dinosaur.HERBIVORE_FORM) {
				GameObject tmp = objectInFrontOfAvatar();
				if (tmp != null && tmp.getType() == EDIBLEWALL && isOnGrid(0.5,0.5)){
					SoundController.getInstance().playEat();
					tmp.deactivatePhysics(world);
					objects.remove(tmp);
					walls.remove(tmp);
					grid[(int)((Wall)tmp).getGridLocation().x-1][(int)((Wall)tmp).getGridLocation().y-1] = null;
					avatar.incrementResources();
				}
			}
			else if (avatar.getForm() == Dinosaur.CARNIVORE_FORM) {
				boolean ate = false;

				for (int i = 0; i < enemies.size(); i++) {
					Enemy tmp = enemies.get(i);
					if (tmp.getStunned() && isInFrontOfAvatar(tmp)
							&& tmp.getPosition().dst2(avatar.getPosition()) < 5.5) {
						SoundController.getInstance().playEat();
						tmp.deactivatePhysics(world);
						objects.remove(tmp);
						enemies.remove(tmp);
						controls.remove(controls.get(i));
						avatar.incrementResources();
						ate = true;
						break;
					}
				}

				if (!ate && !((Carnivore) avatar).inChargeCycle())
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
			if (enemies.get(1).getPosition().y + 0.2 > screenToMaze(3)) {
				enemies.get(1).setTexture(enemyTextureFront);
				enemies.get(1).setDirection(Dinosaur.DOWN);
			}
			else if (enemies.get(1).getPosition().y - 0.2 < screenToMaze(1)) {
				enemies.get(1).setTexture(enemyTextureBack);
				enemies.get(1).setDirection(Dinosaur.UP);
			}

			if (enemies.get(2).getPosition().x +0.2 > screenToMaze(8)) {
				enemies.get(2).setTexture(enemyTextureLeft);
				enemies.get(2).setDirection(Dinosaur.LEFT);
			}
			else if ((enemies.get(2).getPosition().x -0.2 < screenToMaze(6))) {
				enemies.get(2).setTexture(enemyTextureRight);
				enemies.get(2).setDirection(Dinosaur.RIGHT);
			}

			if (enemies.get(3).getPosition().y +0.2 > screenToMaze(3)) {
				enemies.get(3).setTexture(enemyTextureFront);
				enemies.get(3).setDirection(Dinosaur.DOWN);
			}
			else if ((enemies.get(3).getPosition().y -0.2 < screenToMaze(2))) {
				enemies.get(3).setTexture(enemyTextureBack);
				enemies.get(3).setDirection(Dinosaur.UP);
			}

			if (enemies.get(4).getPosition().y +0.2 > screenToMaze(4)) {
				enemies.get(4).setTexture(enemyTextureFront);
				enemies.get(4).setDirection(Dinosaur.DOWN);
			}
			else if ((enemies.get(4).getPosition().y -0.2 < screenToMaze(3))) {
				enemies.get(4).setTexture(enemyTextureBack);
				enemies.get(4).setDirection(Dinosaur.UP);
			}
			if (enemies.get(5).getPosition().y +0.2 > screenToMaze(8)) {
				enemies.get(5).setTexture(enemyTextureLeft);
				enemies.get(5).setDirection(Dinosaur.LEFT);
			}
			else if ((enemies.get(5).getPosition().y -0.2 < screenToMaze(6))) {
				enemies.get(5).setTexture(enemyTextureRight);
				enemies.get(5).setDirection(Dinosaur.RIGHT);
			}

			else if (enemies.get(5).getPosition().x +0.2 > screenToMaze(16)) {
				enemies.get(5).setTexture(enemyTextureBack);
				enemies.get(5).setDirection(Dinosaur.UP);
			}
			else if ((enemies.get(5).getPosition().x -0.2 < screenToMaze(11))) {
				enemies.get(5).setTexture(enemyTextureFront);
				enemies.get(5).setDirection(Dinosaur.DOWN);
			}
			if (enemies.get(6).getPosition().y +0.2 > screenToMaze(4)) {
				enemies.get(6).setTexture(enemyTextureFront);
				enemies.get(6).setDirection(Dinosaur.DOWN);
			}
			else if ((enemies.get(6).getPosition().y -0.2 < screenToMaze(1))) {
				enemies.get(6).setTexture(enemyTextureBack);
				enemies.get(6).setDirection(Dinosaur.UP);
			}

			controls.get(i).getMoveAlongPath();
		}

		hud.update(avatar.getResources(), avatar.getForm());
	}

	/**
	 * Callback method for the start of a collision
	 *
	 * @param contact The two bodies that collided
	 */
	public void beginContact(Contact contact) {
		Fixture fix1 = contact.getFixtureA();
		Fixture fix2 = contact.getFixtureB();

		Body body1 = fix1.getBody();
		Body body2 = fix2.getBody();

		try {
			GameObject bd1 = (GameObject)body1.getUserData();
			GameObject bd2 = (GameObject)body2.getUserData();
			collisionHandler.processCollision(bd1, bd2);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

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

	public GameObject objectInFrontOfAvatar() {
		int direction = avatar.getDirection();
		if (direction == Dinosaur.UP){
			if ((int)avatarGrid().y == GRID_MAX_Y)
				return null;
			else
				return grid[(int)avatarGrid().x-1][(int)avatarGrid().y];
		}
		else if (direction == Dinosaur.DOWN) {
			if ((int)avatarGrid().y == 1)
				return null;
			else
				return grid[(int)avatarGrid().x-1][(int)avatarGrid().y-2];
		}
		else if (direction == Dinosaur.LEFT) {
			if ((int)avatarGrid().x == 1)
				return null;
			else
				return grid[(int)avatarGrid().x-2][(int)avatarGrid().y-1];
		}
		else if (direction == Dinosaur.RIGHT) {
			if ((int)avatarGrid().x == GRID_MAX_X)
				return null;
			else
				return grid[(int)avatarGrid().x][(int)avatarGrid().y-1];
		}
		return null;
	}

	public boolean isAlignedHorizontally(GameObject bd1, GameObject bd2, double offset){
		return (Math.abs(bd1.getY() - bd2.getY()) <= offset);
	}

	public boolean isAlignedVertically(GameObject bd1, GameObject bd2, double offset){
		return (Math.abs(bd1.getX() - bd2.getX()) <= offset);
	}

	public boolean isOnTop(GameObject bd1, GameObject bd2){
		return isAlignedVertically(bd1, bd2, 0.9) && isAlignedHorizontally(bd1, bd2, 0.9);
	}

	public Vector2 avatarGrid(){
		return new Vector2(Math.round((avatar.getX()-1)/2+1), Math.round((avatar.getY()-1)/2+1));
	}

	public boolean isOnGrid(double x, double y){
		float gridx = screenToMaze(avatarGrid().x);
		float gridy = screenToMaze(avatarGrid().y);
		return (Math.abs(avatar.getX() - gridx) <= x) && (Math.abs(avatar.getY() - gridy) <= y);
	}

	public boolean isOverLap(GameObject bd1, GameObject bd2){
		return isAlignedVertically(bd1, bd2, 2.5) && isAlignedHorizontally(bd1, bd2, 2.5);
	}

	/** drawing on screen */
	public int screenToMaze (float f) {
		return (int)(1+2*(f-1));
	}

	public Vector2 screenToMazeVector(float x, float y){
		return new Vector2(screenToMaze(x), screenToMaze(y));
	}


	/**
	 * Creates the ambient lighting for the level
	 *
	 * This is the amount of lighting that the level has without any light sources.
	 * However, if activeLight is -1, this will be ignored and the level will be
	 * completely visible.
	 *
	 * @param  light	the JSON tree defining the light
	 */
	private void initLighting(JsonValue light) {
		raycamera = new OrthographicCamera(bounds.width,bounds.height);
		raycamera.position.set(bounds.width/2.0f, bounds.height/2.0f, 0);
		raycamera.update();

		RayHandler.setGammaCorrection(light.getBoolean("gamma"));
		RayHandler.useDiffuseLight(light.getBoolean("diffuse"));
		rayhandler = new RayHandler(world, Gdx.graphics.getWidth(), Gdx.graphics.getWidth());
		rayhandler.setCombinedMatrix(raycamera);

		float[] color = light.get("color").asFloatArray();
		rayhandler.setAmbientLight(color[0], color[0], color[0], color[0]);
		int blur = light.getInt("blur");
		rayhandler.setBlur(blur > 0);
		rayhandler.setBlurNum(blur);
	}

	/** Unused Screen method */
	public void resize(int width, int height) {}
	/** Unused Screen method */
	public void pause() {}
	/** Unused Screen method */
	public void resume() {}

	/** Unused ContactListener method */
	public void endContact(Contact contact) {}
	/** Unused ContactListener method */
	public void postSolve(Contact contact, ContactImpulse impulse) {}
	/** Unused ContactListener method */
	public void preSolve(Contact contact, Manifold oldManifold) {}
}