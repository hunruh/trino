package tiktaalik.trino;

import java.util.*;

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
import tiktaalik.trino.environment.*;
import tiktaalik.trino.lights.LightSource;
import tiktaalik.trino.lights.PointSource;
import tiktaalik.util.*;
import tiktaalik.trino.duggi.Dinosaur;

/**
 * Base class for the game controller.
 */
public class GameController implements ContactListener, Screen {
	static final int GAME_READY = 0;
	static final int GAME_RUNNING = 1;
	static final int GAME_PAUSED = 2;
	static final int GAME_LEVEL_END = 3;
	static final int GAME_OVER = 4;

	int state;

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
	private static final String CLONE_FILE  = "trino/clone.png";
	private static final String DOLL_STRIP_FRONT  = "trino/doll_front_strip.png";
	private static final String DOLL_STRIP_LEFT  = "trino/doll_left_strip.png";
	private static final String DOLL_STRIP_RIGHT  = "trino/doll_right_strip.png";
	private static final String DOLL_STRIP_BACK  = "trino/doll_back_strip.png";
	private static final String DOLL_EATING_STRIP_FRONT  = "trino/doll_front_eating_strip.png";
	private static final String DOLL_EATING_STRIP_LEFT  = "trino/doll_left_eating_strip.png";
	private static final String DOLL_EATING_STRIP_RIGHT  = "trino/doll_right_eating_strip.png";
	private static final String DOLL_EATING_STRIP_BACK  = "trino/doll_front_eating_strip.png";
	private static final String DOLL_CLONING_STRIP_FRONT  = "trino/doll_front_cloning_strip.png";
	private static final String HERBIVORE_STRIP_FRONT  = "trino/herbivore_front_strip.png";
	private static final String HERBIVORE_STRIP_LEFT  = "trino/herbivore_left_strip.png";
	private static final String HERBIVORE_STRIP_RIGHT  = "trino/herbivore_right_strip.png";
	private static final String HERBIVORE_STRIP_BACK  = "trino/herbivore_back_strip.png";
	private static final String HERBIVORE_EATING_STRIP_FRONT  = "trino/herbivore_front_eating_strip.png";
	private static final String HERBIVORE_EATING_STRIP_LEFT  = "trino/herbivore_left_eating_strip.png";
	private static final String HERBIVORE_EATING_STRIP_RIGHT  = "trino/herbivore_right_eating_strip.png";
	private static final String HERBIVORE_EATING_STRIP_BACK = "trino/herbivore_back_eating_strip.png";
	private static final String CARNIVORE_STRIP_FRONT  = "trino/carnivore_front_strip.png";
	private static final String CARNIVORE_STRIP_LEFT  = "trino/carnivore_left_strip.png";
	private static final String CARNIVORE_STRIP_RIGHT  = "trino/carnivore_right_strip.png";
	private static final String CARNIVORE_STRIP_BACK  = "trino/carnivore_back_strip.png";
	private static final String CARNIVORE_EATING_STRIP_LEFT  = "trino/carnivore_left_eating_strip.png";
	private static final String CARNIVORE_EATING_STRIP_RIGHT  = "trino/carnivore_right_eating_strip.png";
	private static final String CARNIVORE_CHARGE_STRIP_LEFT = "trino/carnivore_left_charge_strip.png";
	private static final String CARNIVORE_CHARGE_STRIP_RIGHT = "trino/carnivore_right_charge_strip.png";
    private static final String CARNIVORE_ATTACK_STRIP_LEFT = "trino/carnivore_left_attack_strip.png";
	private static final String CARNIVORE_ATTACK_STRIP_RIGHT = "trino/carnivore_right_attack_strip.png";
	private static final String ENEMY_STRIP_FRONT = "trino/enemy_front_strip.png";
	private static final String ENEMY_STRIP_LEFT = "trino/enemy_left_strip.png";
	private static final String ENEMY_STRIP_RIGHT = "trino/enemy_right_strip.png";
	private static final String ENEMY_STRIP_BACK = "trino/enemy_back_strip.png";
	private static final String ENEMY_STUNNED_STRIP_FRONT = "trino/enemy_front_stunned_strip.png";
	private static final String ENEMY_STUNNED_STRIP_LEFT = "trino/enemy_left_stunned_strip.png";
	private static final String ENEMY_STUNNED_STRIP_RIGHT = "trino/enemy_right_stunned_strip.png";
	private static final String ENEMY_STUNNED_STRIP_BACK = "trino/enemy_back_stunned_strip.png";
	private static final String ENEMY_CHARGE_STRIP_LEFT = "trino/enemy_left_charge_strip.png";
	private static final String ENEMY_CHARGE_STRIP_RIGHT = "trino/enemy_right_charge_strip.png";
	private static final String ENEMY_ATTACK_STRIP_LEFT = "trino/enemy_left_attack_strip.png";
	private static final String ENEMY_ATTACK_STRIP_RIGHT = "trino/enemy_right_attack_strip.png";
	private static final String ENEMY_LEFT_EATING_STRIP = "trino/enemy_left_eaten_strip.png";
	private static final String FIREFLY_FILE = "trino/ffNick.png";
	private static final String WALL_FILE = "trino/wall_long.png";
	private static final String EDIBLE_WALL_FILE = "trino/ediblewall_long.png";
	private static final String EDIBLE_WALL_EATING_STRIP = "trino/ediblewall_decay_strip.png";
	private static final String COTTON_FLOWER_FILE = "trino/cotton.png";
	private static final String PATH_FILE = "trino/path.png";
	private static final String SWITCH_FILE = "trino/buttonRough.png";
	private static final String RIVER_FILE = "trino/river.png";
	private static final String BOULDER_FILE = "trino/boulder.png";
	private static final String VICTORY_FILE = "trino/victoryImage.png";
	private static final String GAMEOVER_FILE = "trino/gameoverImage.png";
	private static final String TUTORIAL_FILE = "trino/tutorialOverlay.png";

	// Pause menu assets
	private static final String PAUSE_MENU_FILE = "pause/pauseMenu.png";
	private static final String GRAYOUT_FILE = "pause/greyOut.png";
	private static final String MUSIC_OFF_FILE = "pause/musicOff.png";
	private static final String MUSIC_ON_FILE = "pause/musicOn.png";
	private static final String SOUND_OFF_FILE = "pause/soundOff.png";
	private static final String SOUND_ON_FILE = "pause/soundOn.png";
	private static final String MUSIC_OUTLINE_FILE = "pause/musicOutline.png";
	private static final String MENU_FILE = "pause/menu.png";
	private static final String HELP_FILE = "pause/help.png";
	private static final String RESUME_FILE = "pause/resume.png";
	private static final String RESTART_FILE = "pause/restart.png";
	private static final String OUTLINE_FILE = "pause/menuOutline.png";


	// Texture assets variables
	private BitmapFont displayFont;
	private Hashtable<String, TextureRegion> textureDict = new Hashtable<String, TextureRegion>();
	private Hashtable<String, Texture> filmStripDict = new Hashtable<String, Texture>();

	// GAME CONSTANTS
	private static final int EXIT_COUNT = 60; // How many frames after winning/losing do we continue?
	public static final int EXIT_QUIT = 0; // Exit code for quitting the game
	public static final int EXIT_NEXT = 1; // Exit code for advancing to next level
	public static final int EXIT_PREV = 2; // Exit code for jumping back to previous level

	private static final float WORLD_STEP = 1/60.0f; // The amount of time for a physics engine step
	private static final int WORLD_VELOC = 6; // Number of velocity iterations for the constrain solvers
	private static final int WORLD_POSIT = 2; // Number of position iterations for the constrain solvers

	private static final float DEFAULT_GRAVITY = -0.0f; // The default value of gravity (going down)

	public static final int COTTON = 0;
	public static final int EDIBLEWALL = 1;
	public static final int WALL = 2;
	public static final int ENEMY = 3;
	public static final int GOAL = 4;
	public static final int DUGGI = 5;
	public static final int CLONE = 6;
	public static final int SWITCH = 7;
	public static final int FIREFLY = 8;
	public static final int RIVER = 9;
	public static final int BOULDER = 10;

	// GAME VARIABLES
	private CollisionHandler collisionHandler;
	private HUDController hud;
	private ScreenListener listener; // Listener that will update the player mode when we are done
	protected Canvas canvas;

	private Rectangle cameraBounds;
	protected OrthographicCamera raycamera; // The camera defining the RayHandler view; scale is in physics coordinates
	protected RayHandler rayhandler; // The rayhandler for storing lights, and drawing them
	private LightSource duggiLight; // Duggi's light
	private LightSource[] ffLights; // FireFly lights
	private LightSource[] enemyLights; // Enemy lights
	private float ffLightDsts[];
	private float ffLightChanges[];

	private World world;
	private Level level;

	private int currentLevel = 0;
	private int currentCotton = 0; //for shadow duggi
	private PooledList<Vector2> cottonFlowers= new PooledList<Vector2>();

	private PooledList<AIController> controls = new PooledList<AIController>();
	private PooledList<FireFlyAIController> fireFlyControls = new PooledList<FireFlyAIController>();
	private PooledList<Vector2> AIPath = new PooledList<Vector2>();

	private boolean active; // Whether or not this is an active controller
	private boolean complete; // Whether we have completed this level
	private boolean failed; // Whether we have failed at this world (and need a reset)
	private boolean timeOut; // Whether time ran out or not
	private int countdown; // Countdown active for winning or losing
	private boolean removeClone; // Whether or not the clone should be removed
	private boolean isSwitch; // Whether the tile in front is a switch or not
	private boolean isCotton; // Whether the tile in front is a cotton flower or not
	GameObject tmp;
	float tmpx;
	float tmpy;
	private boolean shadowDuggiGotCotton = true;

	/** Timer */
	float levelTime = 300;
	float totalTime = 300;
	int minutes = 0;
	int seconds = 0;

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
		manager.load(DOLL_STRIP_LEFT, Texture.class);
		assets.add(DOLL_STRIP_LEFT);
		manager.load(DOLL_STRIP_RIGHT, Texture.class);
		assets.add(DOLL_STRIP_RIGHT);
		manager.load(DOLL_STRIP_FRONT, Texture.class);
		assets.add(DOLL_STRIP_FRONT);
		manager.load(DOLL_STRIP_BACK, Texture.class);
		assets.add(DOLL_STRIP_BACK);
		manager.load(DOLL_EATING_STRIP_LEFT, Texture.class);
		assets.add(DOLL_EATING_STRIP_LEFT);
		manager.load(DOLL_EATING_STRIP_RIGHT, Texture.class);
		assets.add(DOLL_EATING_STRIP_RIGHT);
		manager.load(DOLL_EATING_STRIP_FRONT, Texture.class);
		assets.add(DOLL_EATING_STRIP_FRONT);
		manager.load(DOLL_EATING_STRIP_BACK, Texture.class);
		assets.add(DOLL_EATING_STRIP_BACK);
		manager.load(DOLL_CLONING_STRIP_FRONT, Texture.class);
		assets.add(DOLL_CLONING_STRIP_FRONT);
		manager.load(CLONE_FILE, Texture.class);
		assets.add(CLONE_FILE);
		manager.load(HERBIVORE_STRIP_LEFT, Texture.class);
		assets.add(HERBIVORE_STRIP_LEFT);
		manager.load(HERBIVORE_STRIP_RIGHT, Texture.class);
		assets.add(HERBIVORE_STRIP_RIGHT);
		manager.load(HERBIVORE_STRIP_FRONT, Texture.class);
		assets.add(HERBIVORE_STRIP_FRONT);
		manager.load(HERBIVORE_STRIP_BACK, Texture.class);
		assets.add(HERBIVORE_STRIP_BACK);
		manager.load(HERBIVORE_EATING_STRIP_FRONT, Texture.class);
		assets.add(HERBIVORE_EATING_STRIP_FRONT);
		manager.load(HERBIVORE_EATING_STRIP_LEFT, Texture.class);
		assets.add(HERBIVORE_EATING_STRIP_LEFT);
		manager.load(HERBIVORE_EATING_STRIP_RIGHT, Texture.class);
		assets.add(HERBIVORE_EATING_STRIP_RIGHT);
		manager.load(HERBIVORE_EATING_STRIP_BACK, Texture.class);
		assets.add(HERBIVORE_EATING_STRIP_BACK);
		manager.load(CARNIVORE_STRIP_LEFT, Texture.class);
		assets.add(CARNIVORE_STRIP_LEFT);
		manager.load(CARNIVORE_STRIP_RIGHT, Texture.class);
		assets.add(CARNIVORE_STRIP_RIGHT);
		manager.load(CARNIVORE_STRIP_FRONT, Texture.class);
		assets.add(CARNIVORE_STRIP_FRONT);
		manager.load(CARNIVORE_STRIP_BACK, Texture.class);
		assets.add(CARNIVORE_STRIP_BACK);
		manager.load(CARNIVORE_EATING_STRIP_LEFT, Texture.class);
		assets.add(CARNIVORE_EATING_STRIP_LEFT);
		manager.load(CARNIVORE_EATING_STRIP_RIGHT, Texture.class);
		assets.add(CARNIVORE_EATING_STRIP_RIGHT);
        manager.load(CARNIVORE_CHARGE_STRIP_LEFT, Texture.class);
        assets.add(CARNIVORE_CHARGE_STRIP_LEFT);
		manager.load(CARNIVORE_CHARGE_STRIP_RIGHT, Texture.class);
		assets.add(CARNIVORE_CHARGE_STRIP_RIGHT);
        manager.load(CARNIVORE_ATTACK_STRIP_LEFT, Texture.class);
        assets.add(CARNIVORE_ATTACK_STRIP_LEFT);
		manager.load(CARNIVORE_ATTACK_STRIP_RIGHT, Texture.class);
		assets.add(CARNIVORE_ATTACK_STRIP_RIGHT);
		manager.load(WALL_FILE, Texture.class);
		assets.add(WALL_FILE);
		manager.load(EDIBLE_WALL_FILE, Texture.class);
		assets.add(EDIBLE_WALL_FILE);
		manager.load(EDIBLE_WALL_EATING_STRIP, Texture.class);
		assets.add(EDIBLE_WALL_EATING_STRIP);
		manager.load(COTTON_FLOWER_FILE, Texture.class);
		assets.add(COTTON_FLOWER_FILE);
		manager.load(ENEMY_STRIP_FRONT, Texture.class);
		assets.add(ENEMY_STRIP_FRONT);
		manager.load(ENEMY_STRIP_LEFT, Texture.class);
		assets.add(ENEMY_STRIP_LEFT);
		manager.load(ENEMY_STRIP_RIGHT, Texture.class);
		assets.add(ENEMY_STRIP_RIGHT);
		manager.load(ENEMY_STRIP_BACK, Texture.class);
		assets.add(ENEMY_STRIP_BACK);
		manager.load(ENEMY_STUNNED_STRIP_FRONT, Texture.class);
		assets.add(ENEMY_STUNNED_STRIP_FRONT);
		manager.load(ENEMY_STUNNED_STRIP_LEFT, Texture.class);
		assets.add(ENEMY_STUNNED_STRIP_LEFT);
		manager.load(ENEMY_STUNNED_STRIP_RIGHT, Texture.class);
		assets.add(ENEMY_STUNNED_STRIP_RIGHT);
		manager.load(ENEMY_STUNNED_STRIP_BACK, Texture.class);
		assets.add(ENEMY_STUNNED_STRIP_BACK);
		manager.load(ENEMY_CHARGE_STRIP_LEFT, Texture.class);
		assets.add(ENEMY_CHARGE_STRIP_LEFT);
		manager.load(ENEMY_CHARGE_STRIP_RIGHT, Texture.class);
		assets.add(ENEMY_CHARGE_STRIP_RIGHT);
		manager.load(ENEMY_ATTACK_STRIP_LEFT, Texture.class);
		assets.add(ENEMY_ATTACK_STRIP_LEFT);
		manager.load(ENEMY_ATTACK_STRIP_RIGHT, Texture.class);
		assets.add(ENEMY_ATTACK_STRIP_RIGHT);
		manager.load(ENEMY_LEFT_EATING_STRIP, Texture.class);
		assets.add(ENEMY_LEFT_EATING_STRIP);
		manager.load(FIREFLY_FILE, Texture.class);
		assets.add(FIREFLY_FILE);
		manager.load(PATH_FILE, Texture.class);
		assets.add(PATH_FILE);
		manager.load(SWITCH_FILE, Texture.class);
		assets.add(SWITCH_FILE);
		manager.load(RIVER_FILE, Texture.class);
		assets.add(RIVER_FILE);
		manager.load(BOULDER_FILE, Texture.class);
		assets.add(BOULDER_FILE);
		manager.load(VICTORY_FILE, Texture.class);
		assets.add(VICTORY_FILE);
		manager.load(GAMEOVER_FILE, Texture.class);
		assets.add(GAMEOVER_FILE);
		manager.load(TUTORIAL_FILE, Texture.class);
		assets.add(TUTORIAL_FILE);
		manager.load(PAUSE_MENU_FILE, Texture.class);
		assets.add(PAUSE_MENU_FILE);
		manager.load(GRAYOUT_FILE, Texture.class);
		assets.add(GRAYOUT_FILE);
		manager.load(MUSIC_OFF_FILE, Texture.class);
		assets.add(MUSIC_OFF_FILE);
		manager.load(MUSIC_ON_FILE, Texture.class);
		assets.add(MUSIC_ON_FILE);
		manager.load(SOUND_OFF_FILE, Texture.class);
		assets.add(SOUND_OFF_FILE);
		manager.load(SOUND_ON_FILE, Texture.class);
		assets.add(SOUND_ON_FILE);
		manager.load(MUSIC_OUTLINE_FILE, Texture.class);
		assets.add(MUSIC_OUTLINE_FILE);
		manager.load(MENU_FILE, Texture.class);
		assets.add(MENU_FILE);
		manager.load(HELP_FILE, Texture.class);
		assets.add(HELP_FILE);
		manager.load(RESUME_FILE, Texture.class);
		assets.add(RESUME_FILE);
		manager.load(RESTART_FILE, Texture.class);
		assets.add(RESTART_FILE);
		manager.load(OUTLINE_FILE, Texture.class);
		assets.add(OUTLINE_FILE);

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
		textureDict.put("background", createTexture(manager,BACKGROUND_FILE,true));
		textureDict.put("overlay", createTexture(manager,OVERLAY_FILE,true));
		textureDict.put("goalOpenTile", createTexture(manager,GOAL_FILE,true));
		textureDict.put("goalClosedTile", createTexture(manager,GOAL_CLOSED_FILE, true));
		textureDict.put("clone", createTexture(manager,CLONE_FILE,false));
		textureDict.put("fireFly", createTexture(manager, FIREFLY_FILE, false));
		textureDict.put("wall", createTexture(manager,WALL_FILE,false));
		textureDict.put("edibleWall", createTexture(manager, EDIBLE_WALL_FILE, false));
		textureDict.put("cotton", createTexture(manager, COTTON_FLOWER_FILE, false));
		textureDict.put("switch", createTexture(manager, SWITCH_FILE, false));
		textureDict.put("river", createTexture(manager, RIVER_FILE, false));
		textureDict.put("boulder", createTexture(manager, BOULDER_FILE, false));
		textureDict.put("victory", createTexture(manager, VICTORY_FILE, false));
		textureDict.put("gameover", createTexture(manager, GAMEOVER_FILE, false));
		textureDict.put("tutorialOverlay", createTexture(manager, TUTORIAL_FILE, false));
		textureDict.put("pauseMenu", createTexture(manager, PAUSE_MENU_FILE, false));
		textureDict.put("grayOut", createTexture(manager, GRAYOUT_FILE, false));
		textureDict.put("musicOff", createTexture(manager, MUSIC_OFF_FILE, false));
		textureDict.put("musicOn", createTexture(manager, MUSIC_ON_FILE, false));
		textureDict.put("soundOff", createTexture(manager, SOUND_OFF_FILE, false));
		textureDict.put("soundOn", createTexture(manager, SOUND_ON_FILE, false));
		textureDict.put("musicOutline", createTexture(manager, MUSIC_OUTLINE_FILE, false));
		textureDict.put("menuText", createTexture(manager, MENU_FILE, false));
		textureDict.put("helpText", createTexture(manager, HELP_FILE, false));
		textureDict.put("resumeText", createTexture(manager, RESUME_FILE, false));
		textureDict.put("restartText", createTexture(manager, RESTART_FILE, false));
		textureDict.put("outline", createTexture(manager, OUTLINE_FILE, false));

		filmStripDict.put("dollLeft", createFilmTexture(manager,DOLL_STRIP_LEFT));
		filmStripDict.put("dollRight", createFilmTexture(manager,DOLL_STRIP_RIGHT));
		filmStripDict.put("dollFront", createFilmTexture(manager,DOLL_STRIP_FRONT));
		filmStripDict.put("dollBack", createFilmTexture(manager,DOLL_STRIP_BACK));
		filmStripDict.put("dollEatingLeft", createFilmTexture(manager,DOLL_EATING_STRIP_LEFT));
		filmStripDict.put("dollEatingRight", createFilmTexture(manager,DOLL_EATING_STRIP_RIGHT));
		filmStripDict.put("dollEatingFront", createFilmTexture(manager,DOLL_EATING_STRIP_FRONT));
		filmStripDict.put("dollEatingBack", createFilmTexture(manager,DOLL_EATING_STRIP_BACK));
		filmStripDict.put("dollCloningFront", createFilmTexture(manager,DOLL_CLONING_STRIP_FRONT));
		filmStripDict.put("carnivoreLeft", createFilmTexture(manager,CARNIVORE_STRIP_LEFT));
		filmStripDict.put("carnivoreRight", createFilmTexture(manager,CARNIVORE_STRIP_RIGHT));
		filmStripDict.put("carnivoreFront", createFilmTexture(manager,CARNIVORE_STRIP_FRONT));
		filmStripDict.put("carnivoreBack", createFilmTexture(manager,CARNIVORE_STRIP_BACK));
		filmStripDict.put("carnivoreEatingLeft", createFilmTexture(manager,CARNIVORE_EATING_STRIP_LEFT));
		filmStripDict.put("carnivoreEatingRight", createFilmTexture(manager,CARNIVORE_EATING_STRIP_RIGHT));
		filmStripDict.put("carnivoreEatingFront", createFilmTexture(manager,CARNIVORE_EATING_STRIP_LEFT));
		filmStripDict.put("carnivoreEatingBack", createFilmTexture(manager,CARNIVORE_EATING_STRIP_LEFT));
        filmStripDict.put("carnivoreChargeLeft", createFilmTexture(manager,CARNIVORE_CHARGE_STRIP_LEFT));
		filmStripDict.put("carnivoreChargeRight", createFilmTexture(manager,CARNIVORE_CHARGE_STRIP_RIGHT));
        filmStripDict.put("carnivoreAttackLeft", createFilmTexture(manager,CARNIVORE_ATTACK_STRIP_LEFT));
		filmStripDict.put("carnivoreAttackRight", createFilmTexture(manager,CARNIVORE_ATTACK_STRIP_RIGHT));
		filmStripDict.put("herbivoreLeft", createFilmTexture(manager,HERBIVORE_STRIP_LEFT));
		filmStripDict.put("herbivoreRight", createFilmTexture(manager,HERBIVORE_STRIP_RIGHT));
		filmStripDict.put("herbivoreFront", createFilmTexture(manager,HERBIVORE_STRIP_FRONT));
		filmStripDict.put("herbivoreBack", createFilmTexture(manager,HERBIVORE_STRIP_BACK));
		filmStripDict.put("herbivoreEatingLeft", createFilmTexture(manager,HERBIVORE_EATING_STRIP_LEFT));
		filmStripDict.put("herbivoreEatingRight", createFilmTexture(manager,HERBIVORE_EATING_STRIP_RIGHT));
		filmStripDict.put("herbivoreEatingFront", createFilmTexture(manager,HERBIVORE_EATING_STRIP_FRONT));
		filmStripDict.put("herbivoreEatingBack", createFilmTexture(manager,HERBIVORE_EATING_STRIP_BACK));
		filmStripDict.put("enemyLeft", createFilmTexture(manager,ENEMY_STRIP_LEFT));
		filmStripDict.put("enemyRight", createFilmTexture(manager,ENEMY_STRIP_RIGHT));
		filmStripDict.put("enemyFront", createFilmTexture(manager,ENEMY_STRIP_FRONT));
		filmStripDict.put("enemyBack", createFilmTexture(manager,ENEMY_STRIP_BACK));
		filmStripDict.put("enemyStunnedLeft", createFilmTexture(manager,ENEMY_STUNNED_STRIP_LEFT));
		filmStripDict.put("enemyStunnedRight", createFilmTexture(manager,ENEMY_STUNNED_STRIP_RIGHT));
		filmStripDict.put("enemyStunnedFront", createFilmTexture(manager,ENEMY_STUNNED_STRIP_FRONT));
		filmStripDict.put("enemyStunnedBack", createFilmTexture(manager,ENEMY_STUNNED_STRIP_BACK));
		filmStripDict.put("enemyChargeLeft", createFilmTexture(manager,ENEMY_CHARGE_STRIP_LEFT));
		filmStripDict.put("enemyChargeRight", createFilmTexture(manager,ENEMY_CHARGE_STRIP_RIGHT));
		filmStripDict.put("enemyAttackLeft", createFilmTexture(manager,ENEMY_ATTACK_STRIP_LEFT));
		filmStripDict.put("enemyAttackRight", createFilmTexture(manager,ENEMY_ATTACK_STRIP_RIGHT));
		filmStripDict.put("enemyLeftEating", createFilmTexture(manager,ENEMY_LEFT_EATING_STRIP));
		filmStripDict.put("edibleWallEating", createFilmTexture(manager, EDIBLE_WALL_EATING_STRIP));

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

	private Texture createFilmTexture(AssetManager manager, String file) {
		if (manager.isLoaded(file)) {
			Texture texture = manager.get(file, Texture.class);
			texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
			return texture;
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
	 * Sets whether level time is up.
	 *
	 * @param value whether the level time is up.
	 */
	public void setTimeout(boolean value) {
		if (value)
			countdown = EXIT_COUNT;

		timeOut = value;
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
	}

	/**
	 * Creates a new game world with the default values.
	 */
	protected GameController() {
		this(new Vector2(0, DEFAULT_GRAVITY));

		jsonReader = new JsonReader();
		setComplete(false);
		setFailure(false);
		setTimeout(false);
		world.setContactListener(this);

	}

	/**
	 * Creates a new game world
	 *
	 * @param gravity	The gravitational force on this Box2d world
	 */
	protected GameController(Vector2 gravity) {
		assets = new Array<String>();
		world = new World(gravity,false);
		level = new Level(world, currentLevel);
		complete = false;
		failed = false;
		timeOut = false;
		active = false;
		countdown = -1;
		cameraBounds = new Rectangle(0,0, 32.0f,18.0f);
		collisionHandler = new CollisionHandler(this);
		hud = new HUDController();
		shadowDuggiGotCotton = true;
		//cottonFlowers = level.getCottonFlowerList();
		//////System.out.println("cotton flowers "+cottonFlowers);

		isSwitch = false;
		isCotton = false;

		state = GAME_READY;
	}

	public void nextLevel(){

		if (currentLevel == 7)
			currentLevel = 0;
		else
			currentLevel++;

		reset();
	}


	public void setCurrentLevel(int l){currentLevel = l;}

	/**
	 * Resets the status of the game so that we can play again.
	 *
	 * This method disposes of the world and creates a new one.
	 */
	public void reset() {
        totalTime = levelTime;
		Vector2 gravity = new Vector2(world.getGravity() );

		level.dispose();
		world.dispose();
		controls.clear();
		fireFlyControls.clear();

		world = new World(gravity,false);
		world.setContactListener(this);

		SoundController.getInstance().dispose();
		SoundController.getInstance().init();
		SoundController.getInstance().playBackground(Dinosaur.DOLL_FORM);

		setComplete(false);
		setFailure(false);
		setTimeout(false);

		// Reload the json each time
		levelFormat = jsonReader.parse(Gdx.files.internal("jsons/level.json"));

		// Create the lighting if appropriate
		if (levelFormat.has("lighting"))
			initLighting(levelFormat.get("lighting"));

		// Init the level
		level = new Level(world, currentLevel);
		////System.out.println("before populate");
		level.populate(textureDict, filmStripDict, duggiLight, canvas.getWidth(), canvas.getHeight());
		//cottonFlowers = level.getCottonFlowerList();
		collisionHandler.setLevel(level);
		//shadowDuggiGotCotton = true;

		// Set the lighting
		if (level.getIsNight()){
			duggiLight.setActive(true);
			rayhandler.setAmbientLight(0.05f, 0.05f, 0.05f, 0.05f);
		}else {
			duggiLight.setActive(false);
			rayhandler.setAmbientLight(1.0f,1.0f,1.0f,1.0f);
		}

		// This should be set before init lighting - should be moved when we load in the json
		cameraBounds = new Rectangle(0,0,32,18);

		// Init Enemy AI controllers
		for (int i = 0; i < level.getEnemies().size(); i++) {
			AIController controller = new AIController(i, level.getAvatar(), level.getEnemies(), AIController.FLIP, level);
			controls.add(controller);
			level.getEnemy(i).setController(controller);
		}

		// Init FireFlies
		ffLights = new LightSource[level.getFireFlies().size()];
		ffLightDsts = new float[level.getFireFlies().size()];
		ffLightChanges = new float[level.getFireFlies().size()];
		for (int i = 0; i < level.getFireFlies().size(); i++) {
			fireFlyControls.add(new FireFlyAIController(i, level.getFireFlies(), level.getBounds()));

			PointSource fireLight = new PointSource(rayhandler, 256, Color.WHITE, 2, 0, 0);
			fireLight.setColor(0.85f,0.85f,0.95f,0.85f);
			fireLight.setXray(true);
			fireLight.setActive(true);
			ffLights[i] = fireLight;
			ffLightDsts[i] = MathUtils.random(2.0f);
			ffLightChanges[i] = MathUtils.random(0.005f, 0.015f);
			fireLight.attachToBody(level.getFirefly(i).getBody(), fireLight.getX(), fireLight.getY(),
					fireLight.getDirection());
		}

//		enemyLights = new LightSource[level.getEnemies().size()];
//
//		for(int i = 0; i < level.getEnemies().size(); i++) {
//
//			if (level.getEnemy(i).getDirection() == Dinosaur.RIGHT){
//				////System.out.println("reached right enemy eyes");
//				PointSource enemyEyes = new PointSource(rayhandler, 256, Color.RED, 0.5f, 0, 0);
//				enemyEyes.setColor(1, 0, 0, 1);
//				enemyEyes.setXray(true);
//				enemyEyes.setActive(true);
//				enemyLights[i] = enemyEyes;
//				enemyEyes.attachToBody(level.getEnemy(i).getBody(), 0.2f, .75f, enemyEyes.getDirection());
//			} else if (level.getEnemy(i).getDirection() == Dinosaur.LEFT){
//				////System.out.println("reached left enemy eyes");
//				PointSource enemyEyes2 = new PointSource(rayhandler, 256, Color.RED, 0.5f, 0, 0);
//				enemyEyes2.setColor(1, 0, 0, 1);
//				enemyEyes2.setXray(true);
//				enemyEyes2.setActive(true);
//				enemyLights[i] = enemyEyes2;
//				enemyEyes2.attachToBody(level.getEnemy(i).getBody(), 0f, .75f, enemyEyes2.getDirection());
//			}
//
//		}

	}

	/**
	 * Returns whether to process the update loop
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

		// Temporary hardcoding
		// Handle nightmode
		
		if (input.didNight()) {
			if (duggiLight.isActive()) {
				duggiLight.setActive(false);
				rayhandler.setAmbientLight(1.0f, 1.0f, 1.0f, 1.0f);
			} else {
				duggiLight.setActive(true);
				rayhandler.setAmbientLight(0.05f, 0.05f, 0.05f, 0.05f);
			}
		}

		if (input.isNextLevelPressed()){
			nextLevel();

		}

		// Reset level when colliding with enemy
		if (countdown > 0) {
			countdown--;
			totalTime = levelTime;
		} else if (countdown == 0) {
			if (failed || timeOut) {
				reset();
			}
			else if (complete) {
				nextLevel();
			}
		}

		return true;
	}

	/**
	 *
	 *
	 * Processes physics
	 *
	 * @param dt Number of seconds since last animation frame
	 */
	public void postUpdate(float dt) {
		// Turn the physics engine crank.
		world.step(WORLD_STEP,WORLD_VELOC,WORLD_POSIT);

		// Garbage collect the deleted objects.
		Iterator<PooledList<GameObject>.Entry> iterator = level.getObjects().entryIterator();
		while (iterator.hasNext()) {
			PooledList<GameObject>.Entry entry = iterator.next();
			GameObject g = entry.getValue();

			if (g.getType() == EDIBLEWALL || g.getType() == ENEMY) {
				if (((EdibleObject) g).getEaten()) {
					level.removeObject(g);

					if (g.getType() == ENEMY)
						controls.remove(((Enemy) g).getController());
				}
			}

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

		level.draw(canvas);

		// Now draw the shadows
		if (rayhandler != null)
			rayhandler.render();


		canvas.beginOverlay();
		canvas.draw(textureDict.get("overlay"),0,0);
		canvas.end();


		if (state == GAME_PAUSED) {
			displayFont.setColor(Color.YELLOW);
			int menuHeight = canvas.getHeight();
			int outlineHeight = textureDict.get("outline").getRegionHeight()/4;
			int outlineWidth = textureDict.get("outline").getRegionWidth()/4;
			int musicHeight = textureDict.get("musicOutline").getRegionHeight()/4;
			int musicWidth = textureDict.get("musicOutline").getRegionWidth()/4;
			canvas.beginOverlay();
			canvas.draw(textureDict.get("grayOut"), -9, 0);
			canvas.draw(textureDict.get("pauseMenu"), 430, 147);
			canvas.draw(textureDict.get("musicOutline"), 526, menuHeight-172); // music outline
			canvas.draw(textureDict.get("musicOutline"), 736, menuHeight-172); // sound outline
			canvas.draw(textureDict.get("outline"), 471, menuHeight-263); // menu outline
			canvas.draw(textureDict.get("outline"), 471, menuHeight-354); // help outline
			canvas.draw(textureDict.get("outline"), 471, menuHeight-445); // restart outline
			canvas.draw(textureDict.get("outline"), 471, menuHeight-536); // resume outline
			canvas.draw(textureDict.get("musicOn"), 543, menuHeight-183+musicHeight); // music button
			canvas.draw(textureDict.get("soundOn"), 753, menuHeight-183+musicHeight); // sound button
			canvas.draw(textureDict.get("menuText"), 636, menuHeight-263+outlineHeight); // menu text
			canvas.draw(textureDict.get("helpText"), 641, menuHeight-354+outlineHeight); // help text
			canvas.draw(textureDict.get("restartText"), 613, menuHeight-445+outlineHeight); // restart text
			canvas.draw(textureDict.get("resumeText"),619, menuHeight-536+outlineHeight); // resume text
//			canvas.drawTextCentered("PAUSED!", displayFont, 0.0f);
//			////System.out.println("X");
//			////System.out.println(Gdx.input.getX());
			////System.out.println("Y");
			////System.out.println(Gdx.input.getY());
			if (InputHandler.getInstance().didReturn()) {
				////System.out.println("TEST");
			}
			canvas.end();
		}

		if (currentLevel == 0) {
			canvas.beginOverlay();
			canvas.draw(textureDict.get("tutorialOverlay"), 779, 0);
			canvas.end();
		}


        if (state == GAME_READY || state == GAME_RUNNING || state == GAME_OVER || state == GAME_PAUSED) {
            displayFont.setColor(Color.WHITE);
            canvas.beginOverlay();
            if (seconds < 10) {
                canvas.drawTextCorner(Integer.toString(minutes)+":0"+Integer.toString(seconds), displayFont, 0.0f);
            }
            else if (seconds == 60) {
                canvas.drawTextCorner(Integer.toString(minutes+1)+":00", displayFont, 0.0f);
            }
            else {
                canvas.drawTextCorner(Integer.toString(minutes)+":"+Integer.toString(seconds), displayFont, 0.0f);
            }
            //canvas.drawTextCorner(Float.toString(totalTime), displayFont, 0.0f);
            canvas.end();
        }

		// Final message
		if (complete && !failed) {
			displayFont.setColor(Color.YELLOW);
			canvas.beginOverlay();
			canvas.draw(textureDict.get("victory"),Color.WHITE,canvas.getWidth()/2 - textureDict.get("victory").getRegionWidth()*.75f/2,
					canvas.getHeight()/2 - textureDict.get("victory").getRegionHeight()*.75f/2,
					textureDict.get("victory").getRegionWidth()*.75f,textureDict.get("victory").getRegionHeight()*.75f);
//			canvas.drawTextCentered("DUGGI ESCAPED!", displayFont, 0.0f);
			canvas.end();
		} else if (failed) {
			state = GAME_OVER;
			displayFont.setColor(Color.RED);
			canvas.beginOverlay();
			canvas.draw(textureDict.get("gameover"), Color.WHITE,canvas.getWidth()/2 - textureDict.get("gameover").getRegionWidth()*.75f/2,
					canvas.getHeight()/2 - textureDict.get("gameover").getRegionHeight()*.75f/2,
					textureDict.get("gameover").getRegionWidth()*.75f,textureDict.get("gameover").getRegionHeight()*.75f);
			//canvas.drawTextCentered("EATEN ALIVE!", displayFont, 0.0f);
			canvas.end();
		} else if (timeOut) {
			state = GAME_OVER;
			displayFont.setColor(Color.RED);
			canvas.beginOverlay();
			canvas.draw(textureDict.get("gameover"),Color.WHITE,canvas.getWidth()/2 - textureDict.get("gameover").getRegionWidth()*.75f/2,
					canvas.getHeight()/2 - textureDict.get("gameover").getRegionHeight()*.75f/2,
					textureDict.get("gameover").getRegionWidth()*.75f,textureDict.get("gameover").getRegionHeight()*.75f);
			//canvas.drawTextCentered("TIME'S UP!", displayFont, 0.0f);
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
				if (state == GAME_RUNNING) {
					postUpdate(delta);
					totalTime -= delta;

					minutes = (int)totalTime / 60;
					seconds = (int)(totalTime % 60);

					timeout();
				}
					draw(delta);
					hud.draw();
			}
		}
	}

	public void timeout() {
		if (totalTime <= 0) {
			setTimeout(true);
			timeOut = true;
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
	 * The core gameplay loop of this world.
	 *
	 * @param dt Number of seconds since last animation frame
	 */
	public void update(float dt) {
		//SoundController.getInstance().checkMusicEnd();

		switch (state) {
			case GAME_READY:
				updateReady();
				break;
			case GAME_RUNNING:
				updateRunning(dt);
				break;
			case GAME_PAUSED:
				updatePaused();
				break;
			case GAME_LEVEL_END:
				updateLevelEnd();
				break;
			case GAME_OVER:
				updateGameOver();
				break;
		}
	}

	private void updateReady() {
		state = GAME_RUNNING;
	}

	private void updateRunning(float dt) {
		if (failed) {
			state = GAME_OVER;
		}
		else {

			////System.out.println("path size " + AIPath.size());
//			// clear the old lights
//			for(LightSource l: enemyLights){
//
//			}
//
//			for(int i = 0; i < level.getEnemies().size(); i++) {
//
//				if (level.getEnemy(i).getDirection() == Dinosaur.RIGHT){
//					PointSource enemyEyes = new PointSource(rayhandler, 256, Color.RED, 0.5f, 0, 0);
//					enemyEyes.setColor(1, 0, 0, 1);
//					enemyEyes.setXray(true);
//					if (!level.getEnemy(i).getStunned() &&
//							!level.getEnemy(i).getCharging() && !level.getEnemy(i).getCollided() && !level.getEnemy(i).getLoadingCharge()
//							&& !level.getEnemy(i).getEaten()){
//						enemyEyes.setActive(true);
//					} else {
//						enemyEyes.setActive(false);
//					}
//					enemyLights[i] = enemyEyes;
//					enemyEyes.attachToBody(level.getEnemy(i).getBody(), 0.2f, .75f, enemyEyes.getDirection());
//				} else if (level.getEnemy(i).getDirection() == Dinosaur.LEFT){
//					PointSource enemyEyes2 = new PointSource(rayhandler, 256, Color.RED, 0.5f, 0, 0);
//					enemyEyes2.setColor(1, 0, 0, 1);
//					enemyEyes2.setXray(true);
//					if (!level.getEnemy(i).getStunned() &&
//							!level.getEnemy(i).getCharging() && !level.getEnemy(i).getCollided() && !level.getEnemy(i).getLoadingCharge()
//							&& !level.getEnemy(i).getEaten()){
//						enemyEyes2.setActive(true);
//					} else {
//						enemyEyes2.setActive(false);
//					}
//					enemyLights[i] = enemyEyes2;
//					enemyEyes2.attachToBody(level.getEnemy(i).getBody(), 0f, .75f, enemyEyes2.getDirection());
//				}
//
//			}

		    if (level.getClone() != null){
		        if (Math.abs(level.getClone().getX() - level.getSwitch(0).getX()) < 1.5 &&
                        Math.abs(level.getClone().getX() - level.getSwitch(0).getX()) > 0 &&
                        Math.abs(level.getClone().getY() - level.getSwitch(0).getY()) < 1.5 &&
                        Math.abs(level.getClone().getY() - level.getSwitch(0).getY()) > 0){
		            //////System.out.println("success");
                    level.getAvatar().setCanExit(true);
                    level.getGoalDoor().setLowered(true);
                    level.getGoalDoor().setTexture(textureDict.get("goalOpenTile"));
                } else {
		            level.getAvatar().setCanExit(false);
					level.getGoalDoor().setLowered(false);
                    level.getGoalDoor().setTexture(textureDict.get("goalClosedTile"));
                }
            } else {
				level.getAvatar().setCanExit(false);
				level.getGoalDoor().setLowered(false);
				level.getGoalDoor().setTexture(textureDict.get("goalClosedTile"));
			}
			if (rayhandler != null) {
				SoundController.getInstance().checkMusicEnd();
				if (rayhandler != null)
					rayhandler.update();
			}
			Dinosaur avatar = level.getAvatar();

			// Process camera updates
			float halfWidth = canvas.getCamera().viewportWidth / 2;
			float halfHeight = canvas.getCamera().viewportHeight / 2;

			if ((avatar.getX() / cameraBounds.width) * canvas.getCamera().viewportWidth < halfWidth) {
				canvas.getCamera().position.x = halfWidth;
				raycamera.position.x = cameraBounds.width / 2;
			} else if ((avatar.getX() / cameraBounds.width) * canvas.getCamera().viewportWidth > level.getLevelWidth() - halfWidth) {
				canvas.getCamera().position.x = level.getLevelWidth() - halfWidth;
				raycamera.position.x = cameraBounds.width - cameraBounds.width / 2;
			} else {
				canvas.getCamera().position.x = (avatar.getX() / cameraBounds.width) * canvas.getCamera().viewportWidth;
				raycamera.position.x = avatar.getX();
			}

			if ((avatar.getY() / cameraBounds.height) * canvas.getCamera().viewportHeight < halfHeight) {
				canvas.getCamera().position.y = halfHeight;
				raycamera.position.y = cameraBounds.height / 2;
			} else if ((avatar.getY() / cameraBounds.height) * canvas.getCamera().viewportHeight > level.getLevelHeight() - halfHeight) {
				canvas.getCamera().position.y = level.getLevelHeight() - halfHeight;
				raycamera.position.y = cameraBounds.height - cameraBounds.height / 2;
			} else {
				canvas.getCamera().position.y = (avatar.getY() / cameraBounds.height) * canvas.getCamera().viewportHeight;
				raycamera.position.y = avatar.getY();
			}

			canvas.getCamera().update();
			raycamera.update();
			rayhandler.setCombinedMatrix(raycamera);

			// Process FireFly updates
			for (FireFlyAIController ffAI : fireFlyControls)
				ffAI.getMoveAlongPath();

			for (int i = 0; i < ffLights.length; i++) {
				if (ffLightDsts[i] > 2) {
					ffLightChanges[i] *= -1;
					ffLightDsts[i] = 2;
				} else if (ffLightDsts[i] < 0.5f) {
					ffLightChanges[i] *= -1;
					ffLightDsts[i] = 0.5f;
				}

				ffLightDsts[i] += ffLightChanges[i];
				ffLights[i].setDistance(ffLightDsts[i]);
			}


			// Process enemy updates
			for (int i = 0; i < level.getEnemies().size(); i++)
				controls.get(i).step();

			//this is hard coded in rn
			//also naming stuff real bad bc im lazy
			/*
			int inFrontOfShadowDuggi = 0;
			boolean block = false;
			Enemy sd = level.getShadowDuggi();
			Vector2 shadowDuggi = level.getShadowDuggi().getGridLocation();
			for (int i = 0; i < level.getEnemies().size()-1; i++)
				controls.get(i).step(level.objectInFrontOfEnemy(level.getEnemy(i)));

				if (shadowDuggi.y == tmp.y && (((shadowDuggi.x - tmp.x) == 1 &&
						(sd.getDirection() == Dinosaur.LEFT && etmp.getDirection() == Dinosaur.RIGHT)) ||
						((shadowDuggi.x - tmp.x) == -1 &&
						(sd.getDirection() == Dinosaur.RIGHT && etmp.getDirection() == Dinosaur.LEFT)))){
					inFrontOfShadowDuggi = -1;
				}
				else if (shadowDuggi.x == tmp.x && (shadowDuggi.y - tmp.y == -1 &&
						(sd.getDirection() == Dinosaur.UP && etmp.getDirection() == Dinosaur.DOWN) ||
						shadowDuggi.y - tmp.y == 1 &&
						(sd.getDirection() == Dinosaur.DOWN && etmp.getDirection() == Dinosaur.UP))){
					inFrontOfShadowDuggi = 1;
				}
				else{
					float dx = shadowDuggi.x - tmp.x;
					float dy = shadowDuggi.y - tmp.y;
					int typex = -1;
					int typey = -1;
					if (tmp.x-dx >= 0 && tmp.x-dx < level.getLevelWidth()) {
						//System.out.println(level.getGrid()[(int) (tmp.x - dx)][(int) (tmp.y)]);
						if (Math.abs(dx) <= 1 && level.getGrid()[(int) (tmp.x - dx)][(int) (tmp.y)] != null)
							typex = level.getGrid()[(int) (tmp.x - dx)][(int) (tmp.y)].getType();
					}
					if (tmp.y-dy >= 0 && tmp.y-dy < level.getLevelHeight()) {
						if (Math.abs(dy) <= 1 && level.getGrid()[(int) (tmp.x)][(int) (tmp.y - dy)] != null)
							typey = level.getGridObject((int) (tmp.x), (int) (tmp.y - dy)).getType();
					}
					if (Math.abs(dx) <= 1 && (typex == WALL || typex == EDIBLEWALL || typex == RIVER ||
							typex == BOULDER || typex == GOAL)){
						inFrontOfShadowDuggi = -1;
					}
					else if (Math.abs(dy) <= 1 && (typey == WALL || typey == EDIBLEWALL || typey == RIVER ||
							typey == BOULDER || typey == GOAL)){
						inFrontOfShadowDuggi = 1;
					}
				}

				if ((shadowDuggi.x - tmp.x) == -1 && shadowDuggi.y == tmp.y && sd.getDirection() == Dinosaur.RIGHT
						&&(etmp.getDirection() != Dinosaur.RIGHT && etmp.getDirection() != Dinosaur.LEFT)){
					block = true;
				}
				else if ((shadowDuggi.x - tmp.x) == 1 && shadowDuggi.y == tmp.y && sd.getDirection() == Dinosaur.LEFT
						&&(etmp.getDirection() != Dinosaur.RIGHT && etmp.getDirection() != Dinosaur.LEFT)){
					block = true;
				}
				else if ((shadowDuggi.y - tmp.y) == -1 && shadowDuggi.x == tmp.x && sd.getDirection() == Dinosaur.UP
						&&(etmp.getDirection() != Dinosaur.UP && etmp.getDirection() != Dinosaur.DOWN)){
					block = true;
				}
				else if ((shadowDuggi.y - tmp.y) == 1 && shadowDuggi.x == tmp.x && sd.getDirection() == Dinosaur.DOWN
						&&(etmp.getDirection() != Dinosaur.UP && etmp.getDirection() != Dinosaur.DOWN)){
					block = true;
				}

			}
			shadowDuggiGotCotton = controls.get(level.getEnemies().size()-1).step(AIPath, block, inFrontOfShadowDuggi);
			if(shadowDuggiGotCotton) {
				//System.out.println("i shouldnt be doingthe ai tbh");
				//cottonFlowers.remove(currentCotton);
			}
			*/

			// Process avatar updates
			int direction = avatar.getDirection();
			if (avatar.getActionReady() || avatar.getActionLoadValue() > 0 ||
					(avatar.getForm() == Dinosaur.DOLL_FORM && avatar.getActionAnimating())) {
				avatar.setLeftRight(0);
				avatar.setUpDown(0);
			}
			else{
				avatar.setLeftRight(InputHandler.getInstance().getHorizontal());
				avatar.setUpDown(InputHandler.getInstance().getVertical());
			}

			if (InputHandler.getInstance().didTransform()) {
//				if (avatar.canTransform()) {
				if (true) {
					if (InputHandler.getInstance().didTransformDoll() &&
							avatar.getForm() != Dinosaur.DOLL_FORM) {

						avatar = avatar.transformToDoll();
                        avatar.setCanBeSeen(true);

						//Change the filter data
						Filter filter = avatar.getFilterData();
						filter.categoryBits = Dinosaur.dollCatBits;
						filter.maskBits = Dinosaur.enemyCatBits|Dinosaur.riverCatBits|Dinosaur.wallCatBits;
						avatar.setFilterData(filter);
						avatar.setTextureSet(filmStripDict.get("dollLeft"), 8,
								filmStripDict.get("dollRight"), 8,
								filmStripDict.get("dollBack"), 8,
								filmStripDict.get("dollFront"), 8);
						avatar.setEatingTextureSet(filmStripDict.get("dollEatingLeft"), 7,
								filmStripDict.get("dollEatingRight"), 7,
								filmStripDict.get("dollEatingBack"), 7,
								filmStripDict.get("dollEatingFront"), 7);
						avatar.setActionTextureSet(filmStripDict.get("dollCloningFront"), 12,
								filmStripDict.get("dollCloningFront"), 12,
								filmStripDict.get("dollCloningFront"), 12,
								filmStripDict.get("dollCloningFront"), 12);

						level.setAvatar(avatar);

						SoundController.getInstance().changeBackground(Dinosaur.DOLL_FORM);
						SoundController.getInstance().playTransform();
					} else if (InputHandler.getInstance().didTransformHerbi() &&
							avatar.getForm() != Dinosaur.HERBIVORE_FORM) {
						avatar = avatar.transformToHerbivore();
                        avatar.setCanBeSeen(true);

						//Change the filter data
						Filter filter = avatar.getFilterData();
						filter.categoryBits = Dinosaur.herbCatBits;
						filter.maskBits = Dinosaur.enemyCatBits|Dinosaur.wallCatBits;
						avatar.setFilterData(filter);
						avatar.setTextureSet(filmStripDict.get("herbivoreLeft"), 7,
								filmStripDict.get("herbivoreRight"), 7,
								filmStripDict.get("herbivoreBack"), 8,
								filmStripDict.get("herbivoreFront"), 8);
						avatar.setEatingTextureSet(filmStripDict.get("herbivoreEatingLeft"), 10,
								filmStripDict.get("herbivoreEatingRight"), 10,
								filmStripDict.get("herbivoreEatingBack"), 10,
								filmStripDict.get("herbivoreEatingFront"), 10);

						level.setAvatar(avatar);

						SoundController.getInstance().changeBackground(Dinosaur.HERBIVORE_FORM);
						SoundController.getInstance().playTransform();
					} else if (InputHandler.getInstance().didTransformCarni() &&
							avatar.getForm() != Dinosaur.CARNIVORE_FORM) {
						avatar = avatar.transformToCarnivore();
                        avatar.setCanBeSeen(true);

						Filter filter = avatar.getFilterData();
						filter.categoryBits = Dinosaur.carnCatBits;
						filter.maskBits = Dinosaur.enemyCatBits|Dinosaur.riverCatBits|Dinosaur.wallCatBits;
						avatar.setFilterData(filter);
						avatar.setTextureSet(filmStripDict.get("carnivoreLeft"), 10,
								filmStripDict.get("carnivoreRight"), 10,
								filmStripDict.get("carnivoreBack"), 8,
								filmStripDict.get("carnivoreFront"), 10);
						avatar.setEatingTextureSet(filmStripDict.get("carnivoreEatingLeft"), 8,
								filmStripDict.get("carnivoreEatingRight"), 8,
								filmStripDict.get("carnivoreEatingBack"), 8,
								filmStripDict.get("carnivoreEatingFront"), 8);
						avatar.setActionLoadingTextureSet(filmStripDict.get("carnivoreChargeLeft"), 15,
								filmStripDict.get("carnivoreChargeRight"), 15,
								filmStripDict.get("carnivoreChargeLeft"), 15,
								filmStripDict.get("carnivoreChargeLeft"), 15);
						avatar.setActionTextureSet(filmStripDict.get("carnivoreAttackLeft"), 9,
								filmStripDict.get("carnivoreAttackRight"), 9,
								filmStripDict.get("carnivoreAttackLeft"), 9,
								filmStripDict.get("carnivoreAttackLeft"), 9);

						level.setAvatar(avatar);

						SoundController.getInstance().changeBackground(Dinosaur.CARNIVORE_FORM);
						SoundController.getInstance().playTransform();
					}
				}
			}

			GameObject b = level.objectInFrontOfAvatar();
			for (int i = 0; i < level.getBoulders().size(); i++) {
				if (b != null && b.getType() == BOULDER &&
						Math.abs(level.getBoulder(i).getGridLocation().x - level.getAvatarGridX()) <= 1
						&& Math.abs(level.getBoulder(i).getGridLocation().y - level.getAvatarGridY()) <= 1 &&
						b == level.getBoulder(i) && avatar.getForm() == Dinosaur.CARNIVORE_FORM &&
						((Carnivore) avatar).getActionInProgress()) {
					level.getBoulder(i).setBodyType(BodyDef.BodyType.DynamicBody);

					if (direction == Dinosaur.RIGHT) {
						if (level.getGridObject((int) (((Boulder) b).getGridLocation().x + 1),
								(int) (((Boulder) b).getGridLocation().y)) != null &&
								level.getGridObject((int) (((Boulder) b).getGridLocation().x + 1),
										(int) (((Boulder) b).getGridLocation().y)).getType() != SWITCH) {
						} else {
							for (int k = 0; k < level.getSwitches().size(); k++) {
								if (level.getGrid()[(int) (((Boulder) b).getGridLocation().x + 1)]
										[(int) (((Boulder) b).getGridLocation().y)] == level.getSwitch(k)) {
									isSwitch = true;
									tmp = level.getSwitch(k);
									tmpx = ((Switch) tmp).getGridLocation().x;
									tmpy = ((Switch) tmp).getGridLocation().y;
								}
								if (level.getGrid()[(int) (((Boulder) b).getGridLocation().x)]
										[(int) (((Boulder) b).getGridLocation().y)] == level.getSwitch(k)) {
									tmp = level.getSwitch(k);
								}
								if (level.getGrid()[(int) (((Boulder) b).getGridLocation().x - 1)]
										[(int) (((Boulder) b).getGridLocation().y)] == level.getSwitch(k)) {
									tmp = level.getSwitch(k);
								}
							}
							for (int m = 0; m < level.getCottonFlowers().size(); m++) {
								if (level.getGrid()[(int) (((Boulder) b).getGridLocation().x + 1)]
										[(int) (((Boulder) b).getGridLocation().y)] == level.getCottonFlower(m)) {
									isCotton = true;
									tmp = level.getCottonFlower(m);
									tmpx = ((CottonFlower) tmp).getGridLocation().x;
									tmpy = ((CottonFlower) tmp).getGridLocation().y;
								}
							}

							level.getBoulder(i).setVX(125);
							level.getGrid()[(int) (((Boulder) b).getGridLocation().x + 1)]
									[(int) (((Boulder) b).getGridLocation().y)] = b;

							level.getGrid()[(int) (((Boulder) b).getGridLocation().x)]
									[(int) (((Boulder) b).getGridLocation().y)] = null;
							((Boulder) b).setGridLocation(((Boulder) b).getGridLocation().x + 1.0f, ((Boulder) b).getGridLocation().y);
							if (isSwitch == true && ((Boulder) b).getGridLocation().x != tmpx) {
								((Switch) tmp).setGridLocation(tmpx, tmpy);
								level.getGrid()[(int) tmpx][(int) tmpy] = tmp;
								isSwitch = false;
							} else if (isCotton == true && ((Boulder) b).getGridLocation().x != tmpx) {
								((CottonFlower) tmp).setGridLocation(tmpx, tmpy);
								level.getGrid()[(int) tmpx][(int) tmpy] = tmp;
								isCotton = false;
							}

							for (int n = 0; n < level.getEnemies().size(); n++) {
								if (Math.abs(level.getBoulder(i).getGridLocation().x - (level.getEnemy(n).getX() - 1) / 2) <= 1
										&& Math.abs(level.getBoulder(i).getGridLocation().y - (level.getEnemy(n).getY() - 1) / 2) <= 1) {
									level.getEnemy(n).setVX(1);
								}
							}
						}

					} else if (direction == Dinosaur.LEFT) {
						if (level.getGridObject((int) (((Boulder) b).getGridLocation().x + 1),
								(int) (((Boulder) b).getGridLocation().y)) != null &&
								level.getGridObject((int) (((Boulder) b).getGridLocation().x + 1),
										(int) (((Boulder) b).getGridLocation().y)).getType() != SWITCH &&
								level.getGridObject((int) (((Boulder) b).getGridLocation().x + 1),
										(int) (((Boulder) b).getGridLocation().y)).getType() != COTTON) {
						} else {
							for (int k = 0; k < level.getSwitches().size(); k++) {
								if (level.getGrid()[(int) (((Boulder) b).getGridLocation().x - 1)]
										[(int) (((Boulder) b).getGridLocation().y)] == level.getSwitch(k)) {
									isSwitch = true;
									tmp = level.getSwitch(k);
									tmpx = ((Switch) tmp).getGridLocation().x;
									tmpy = ((Switch) tmp).getGridLocation().y;
								}
								if (level.getGrid()[(int) (((Boulder) b).getGridLocation().x)]
										[(int) (((Boulder) b).getGridLocation().y)] == level.getSwitch(k)) {
									tmp = level.getSwitch(k);
								}
								if (level.getGrid()[(int) (((Boulder) b).getGridLocation().x + 1)]
										[(int) (((Boulder) b).getGridLocation().y)] == level.getSwitch(k)) {
									tmp = level.getSwitch(k);
								}
							}
							for (int m = 0; m < level.getCottonFlowers().size(); m++) {
								if (level.getGrid()[(int) (((Boulder) b).getGridLocation().x - 1)]
										[(int) (((Boulder) b).getGridLocation().y)] == level.getCottonFlower(m)) {
									isCotton = true;
									tmp = level.getCottonFlower(m);
									tmpx = ((CottonFlower) tmp).getGridLocation().x;
									tmpy = ((CottonFlower) tmp).getGridLocation().y;
								}
							}
							level.getBoulder(i).setVX(-125);
							level.getGrid()[(int) (((Boulder) b).getGridLocation().x - 1)]
									[(int) (((Boulder) b).getGridLocation().y)] = b;

							level.getGrid()[(int) (((Boulder) b).getGridLocation().x)]
									[(int) (((Boulder) b).getGridLocation().y)] = null;
							((Boulder) b).setGridLocation(((Boulder) b).getGridLocation().x - 1.0f, ((Boulder) b).getGridLocation().y);
							if (isSwitch == true && ((Boulder) b).getGridLocation().x != tmpx) {
								((Switch) tmp).setGridLocation(tmpx, tmpy);
								level.getGrid()[(int) tmpx][(int) tmpy] = tmp;
								isSwitch = false;
							} else if (isCotton == true && ((Boulder) b).getGridLocation().x != tmpx) {
								((CottonFlower) tmp).setGridLocation(tmpx, tmpy);
								level.getGrid()[(int) tmpx][(int) tmpy] = tmp;
								isCotton = false;
							}

							for (int n = 0; n < level.getEnemies().size(); n++) {
								if (Math.abs(level.getBoulder(i).getGridLocation().x - (level.getEnemy(n).getX() - 1) / 2) <= 1
										&& Math.abs(level.getBoulder(i).getGridLocation().y - (level.getEnemy(n).getY() - 1) / 2) <= 1) {
									level.getEnemy(n).setVX(-1);
								}
							}

						}
					} else if (direction == Dinosaur.UP) {
						if (level.getGridObject((int) (((Boulder) b).getGridLocation().x),
								(int) (((Boulder) b).getGridLocation().y + 1)) != null &&
								level.getGridObject((int) (((Boulder) b).getGridLocation().x),
										(int) (((Boulder) b).getGridLocation().y + 1)).getType() != SWITCH) {
						} else {
							for (int k = 0; k < level.getSwitches().size(); k++) {
								if (level.getGrid()[(int) (((Boulder) b).getGridLocation().x)]
										[(int) (((Boulder) b).getGridLocation().y + 1)] == level.getSwitch(k)) {
									isSwitch = true;
									tmp = level.getSwitch(k);
									tmpx = ((Switch) tmp).getGridLocation().x;
									tmpy = ((Switch) tmp).getGridLocation().y;
								}
								if (level.getGrid()[(int) (((Boulder) b).getGridLocation().x)]
										[(int) (((Boulder) b).getGridLocation().y)] == level.getSwitch(k)) {
									tmp = level.getSwitch(k);
								}
								if (level.getGrid()[(int) (((Boulder) b).getGridLocation().x)]
										[(int) (((Boulder) b).getGridLocation().y - 1)] == level.getSwitch(k)) {
									tmp = level.getSwitch(k);
								}
							}
							for (int m = 0; m < level.getCottonFlowers().size(); m++) {
								if (level.getGrid()[(int) (((Boulder) b).getGridLocation().x)]
										[(int) (((Boulder) b).getGridLocation().y + 1)] == level.getCottonFlower(m)) {
									isCotton = true;
									tmp = level.getCottonFlower(m);
									tmpx = ((CottonFlower) tmp).getGridLocation().x;
									tmpy = ((CottonFlower) tmp).getGridLocation().y;
								}
							}
							level.getBoulder(i).setVY(125);
							level.getGrid()[(int) (((Boulder) b).getGridLocation().x)]
									[(int) (((Boulder) b).getGridLocation().y + 1)] = b;

							level.getGrid()[(int) (((Boulder) b).getGridLocation().x)]
									[(int) (((Boulder) b).getGridLocation().y)] = null;
							((Boulder) b).setGridLocation(((Boulder) b).getGridLocation().x, ((Boulder) b).getGridLocation().y + 1.0f);
							if (isSwitch == true && ((Boulder) b).getGridLocation().y != tmpy) {
								((Switch) tmp).setGridLocation(tmpx, tmpy);
								level.getGrid()[(int) tmpx][(int) tmpy] = tmp;
								isSwitch = false;
							} else if (isCotton == true && ((Boulder) b).getGridLocation().x != tmpx) {
								((CottonFlower) tmp).setGridLocation(tmpx, tmpy);
								level.getGrid()[(int) tmpx][(int) tmpy] = tmp;
								isCotton = false;
							}

							for (int n = 0; n < level.getEnemies().size(); n++) {
								if (Math.abs(level.getBoulder(i).getGridLocation().x - (level.getEnemy(n).getX() - 1) / 2) <= 1
										&& Math.abs(level.getBoulder(i).getGridLocation().y - (level.getEnemy(n).getY() - 1) / 2) <= 1) {
									level.getEnemy(n).setVY(1);
								}
							}

						}
					} else if (direction == Dinosaur.DOWN) {
						if (level.getGridObject((int) (((Boulder) b).getGridLocation().x),
								(int) (((Boulder) b).getGridLocation().y - 1)) != null &&
								level.getGridObject((int) (((Boulder) b).getGridLocation().x),
										(int) (((Boulder) b).getGridLocation().y - 1)).getType() != SWITCH) {
						} else {
							for (int k = 0; k < level.getSwitches().size(); k++) {
								if (level.getGrid()[(int) (((Boulder) b).getGridLocation().x)]
										[(int) (((Boulder) b).getGridLocation().y - 1)] == level.getSwitch(k)) {
									isSwitch = true;
									tmp = level.getSwitch(k);
									tmpx = ((Switch) tmp).getGridLocation().x;
									tmpy = ((Switch) tmp).getGridLocation().y;
								}
								if (level.getGrid()[(int) (((Boulder) b).getGridLocation().x)]
										[(int) (((Boulder) b).getGridLocation().y)] == level.getSwitch(k)) {
									tmp = level.getSwitch(k);
								}
								if (level.getGrid()[(int) (((Boulder) b).getGridLocation().x)]
										[(int) (((Boulder) b).getGridLocation().y + 1)] == level.getSwitch(k)) {
									tmp = level.getSwitch(k);
								}
							}
							for (int m = 0; m < level.getCottonFlowers().size(); m++) {
								if (level.getGrid()[(int) (((Boulder) b).getGridLocation().x)]
										[(int) (((Boulder) b).getGridLocation().y - 1)] == level.getCottonFlower(m)) {
									isCotton = true;
									tmp = level.getCottonFlower(m);
									tmpx = ((CottonFlower) tmp).getGridLocation().x;
									tmpy = ((CottonFlower) tmp).getGridLocation().y;
								}
							}
							level.getBoulder(i).setVY(-125);
							level.getGrid()[(int) (((Boulder) b).getGridLocation().x)]
									[(int) (((Boulder) b).getGridLocation().y - 1)] = b;

							level.getGrid()[(int) (((Boulder) b).getGridLocation().x)]
									[(int) (((Boulder) b).getGridLocation().y)] = null;
							((Boulder) b).setGridLocation(((Boulder) b).getGridLocation().x, ((Boulder) b).getGridLocation().y - 1.0f);
							if (isSwitch == true && ((Boulder) b).getGridLocation().y != tmpy) {
								((Switch) tmp).setGridLocation(tmpx, tmpy);
								level.getGrid()[(int) tmpx][(int) tmpy] = tmp;
								isSwitch = false;
							} else if (isCotton == true && ((Boulder) b).getGridLocation().x != tmpx) {
								((CottonFlower) tmp).setGridLocation(tmpx, tmpy);
								level.getGrid()[(int) tmpx][(int) tmpy] = tmp;
								isCotton = false;
							}

							for (int n = 0; n < level.getEnemies().size(); n++) {
								if (Math.abs(level.getBoulder(i).getGridLocation().x - (level.getEnemy(n).getX() - 1) / 2) <= 1
										&& Math.abs(level.getBoulder(i).getGridLocation().y - (level.getEnemy(n).getY() - 1) / 2) <= 1) {
									level.getEnemy(n).setVY(-1);
								}
							}

						}
					}
					avatar.stopAction();
				} else {
					level.getBoulder(i).setBodyType(BodyDef.BodyType.StaticBody);
				}
			}

			if (level.getClone() != null && (removeClone || level.getClone().getRemoved())) {
				removeClone = false;
				level.removeClone();
			}

			// Check if Duggi or Clone is on top of button
			//		if (level.getClone() != null && level.getClone().getGridLocation() != null &&
			//				level.getClone().getGridLocation().equals(new Vector2(12,4))) {
			//			avatar.setCanExit(true);
			//			level.getGoalDoor().setTexture(textureDict.get("goalOpenTile"));
			//		} else {
			//			avatar.setCanExit(false);
			//			level.getGoalDoor().setTexture(textureDict.get("goalClosedTile"));
			//		}
			if (InputHandler.getInstance().didAction()) {
				if (avatar.getForm() == Dinosaur.DOLL_FORM) {
					GameObject cotton = level.getGridObject(level.getAvatarGridX(), level.getAvatarGridY());
					if (cotton != null && cotton.getType() == COTTON && avatar.getResources() < 3) {
						SoundController.getInstance().playCottonPickup();
						level.removeObject(cotton);
						avatar.incrementResources();
					} else {
						if (!avatar.inActionCycle())
							avatar.loadAction();
					}
				} else if (avatar.getForm() == Dinosaur.HERBIVORE_FORM) {
					GameObject tmp = level.objectInFrontOfAvatar();
					if (tmp != null && tmp.getType() == EDIBLEWALL && tmp.getPosition().dst2(avatar.getPosition()) < 5.5) {

						if (!avatar.inActionCycle()){
							avatar.loadAction();
						}
//						if (avatar.getActionLoadValue() == 0){
//							SoundController.getInstance().playEat();
//							((EdibleObject) tmp).beginEating();
//							avatar.incrementResources();
//						} else if (!avatar.inActionCycle()){
//							avatar.loadAction();
//						}
					} else {
//					    avatar.setCanBeSeen(!avatar.getCanBeSeen());
                    }
				} else if (avatar.getForm() == Dinosaur.CARNIVORE_FORM) {
					boolean ate = false;

					for (int i = 0; i < level.getEnemies().size(); i++) {
						Enemy tmp = level.getEnemy(i);
						if (tmp.getStunned() && level.isInFrontOfAvatar(tmp)
								&& tmp.getPosition().dst2(avatar.getPosition()) < 5.5 && avatar.getResources() < 3) {
							SoundController.getInstance().playEat();
							if (!tmp.getEatInProgress()){
								avatar.incrementResources();
							}
							tmp.beginEating();
							ate = true;
							break;
						}
					}

					if (!ate && !avatar.inActionCycle())
						avatar.loadAction();
				}
			}

			if (level.getAvatar().getActionComplete()) {
				if (level.getAvatar().getForm() == Dinosaur.DOLL_FORM) {
                    if (level.getClone() != null) {
                        level.removeClone();
                    }
					level.getAvatar().useAction();
					level.placeClone();
				} else if (level.getAvatar().getForm() == Dinosaur.HERBIVORE_FORM){
					level.getAvatar().useAction();
					GameObject tmp = level.objectInFrontOfAvatar();
					if (tmp != null && tmp.getType() == EDIBLEWALL && tmp.getPosition().dst2(avatar.getPosition()) < 5.5) {
						SoundController.getInstance().playEat();
						((EdibleObject) tmp).beginEating();
						avatar.setCanBeSeen(false);
					}
				}
			}


			if (InputHandler.getInstance().didActionRelease()) {

				if (avatar.getForm() == Dinosaur.HERBIVORE_FORM && avatar.getActionLoadValue() < 0.25f && avatar.getActionLoadValue()>0) {
					GameObject tmp = level.objectInFrontOfAvatar();
					if (tmp != null && tmp.getType() == EDIBLEWALL && tmp.getPosition().dst2(avatar.getPosition()) < 5.5
							&& avatar.getResources() < 3) {
						SoundController.getInstance().playEat();
						if (!((EdibleObject) tmp).getEatInProgress()){
							avatar.incrementResources();
						}
						((EdibleObject) tmp).beginEating();
					}
				}

				if (avatar.actionReady()) {
					avatar.beginAction();
				} else {
					avatar.stopAction();
				}
			}

			if (InputHandler.getInstance().didPause()) {
				state = GAME_PAUSED;
				return;
			}

			avatar.applyForce();

			hud.update(avatar.getResources(), avatar.getForm());
		}
	}

	private void updatePaused() {
		if (InputHandler.getInstance().didPause()) {
			state = GAME_RUNNING;
			return;
		}
	}

	private void updateLevelEnd() {
		state = GAME_READY;
	}

	private void updateGameOver() {
		state = GAME_READY;
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

	/**
	 * Creates the ambient lighting for the level
	 *
	 * @param  light	the JSON tree defining the light
	 */
	private void initLighting(JsonValue light) {
		raycamera = new OrthographicCamera(cameraBounds.width, cameraBounds.height);
		raycamera.position.set(cameraBounds.width/2.0f, cameraBounds.height/2.0f, 0);
		raycamera.update();

		RayHandler.setGammaCorrection(light.getBoolean("gamma"));
		RayHandler.useDiffuseLight(light.getBoolean("diffuse"));
		rayhandler = new RayHandler(world, Gdx.graphics.getWidth(), Gdx.graphics.getWidth());
		rayhandler.setCombinedMatrix(raycamera);
		rayhandler.setAmbientLight(1.0f,1.0f,1.0f,1.0f);


		duggiLight = new PointSource(rayhandler, 256, Color.WHITE, 8, 0, 0.4f);
		duggiLight.setColor(0.85f,0.85f,0.95f,0.85f);
		duggiLight.setXray(true);
		duggiLight.setActive(false);

	}

	/**
	 * Dispose of all (non-static) resources allocated to this mode.
	 */
	public void dispose() {
		if (rayhandler != null) {
			rayhandler.dispose();
			rayhandler = null;
		}

		world.dispose();
		controls.clear();
		fireFlyControls.clear();

		level = null;
		ffLights = null;
		enemyLights = null;
		controls = null;
		fireFlyControls = null;
		world = null;
		canvas = null;
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