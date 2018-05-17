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

import static com.badlogic.gdx.math.MathUtils.random;

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
	private static String FONT_FILE = "hud/gyparody/gyparody rg.ttf";
	private static int FONT_SIZE = 64;

	// Texture files
	private static final String BACKGROUND_FILE = "trino/background.png";
	private static final String OVERLAY_FILE = "trino/overlay.png";
	private static final String GOAL_FILE = "trino/openExitPlaceHolder.png";
	private static final String GOAL_CLOSED_FILE = "trino/exitClosedPlaceholder.png";
	private static final String GREEN_DOOR_STRIP = "trino/green_door.png";
	private static final String YELLOW_DOOR_STRIP = "trino/yellow_door.png";
	private static final String BLUE_DOOR_STRIP = "trino/blue_door.png";
	private static final String RED_DOOR_STRIP = "trino/red_door.png";
	private static final String DOOR_FILE_ONE = "trino/openExitPlaceHolder1.png";
	private static final String DOOR_CLOSED_FILE_ONE = "trino/exitClosedPlaceholder1.png";
	private static final String DOOR_FILE_TWO = "trino/openExitPlaceHolder2.png";
	private static final String DOOR_CLOSED_FILE_TWO = "trino/exitClosedPlaceHolder2.png";
	private static final String DOOR_FILE_THREE = "trino/openExitPlaceHolder3.png";
	private static final String DOOR_CLOSED_FILE_THREE = "trino/exitClosedPlaceHolder3.png";
	private static final String DOOR_FLASHING_STRIP = "trino/door_flashing.png";
	private static final String CLONE_FILE  = "trino/clone.png";
	private static final String DOLL_STRIP_FRONT  = "trino/doll_front_strip.png";
	private static final String DOLL_STRIP_LEFT  = "trino/doll_left_strip.png";
	private static final String DOLL_STRIP_RIGHT  = "trino/doll_right_strip.png";
	private static final String DOLL_STRIP_BACK  = "trino/doll_back_strip.png";
	private static final String DOLL_EATING_STRIP_FRONT  = "trino/doll_front_eating_strip.png";
	private static final String DOLL_EATING_STRIP_LEFT  = "trino/doll_left_eating_strip.png";
	private static final String DOLL_EATING_STRIP_RIGHT  = "trino/doll_right_eating_strip.png";
	private static final String DOLL_EATING_STRIP_BACK  = "trino/doll_back_eating_strip.png";
	private static final String DOLL_CLONING_STRIP_FRONT  = "trino/doll_front_cloning_strip.png";
	private static final String HERBIVORE_STRIP_FRONT  = "trino/herbivore_front_strip.png";
	private static final String HERBIVORE_STRIP_LEFT  = "trino/herbivore_left_strip.png";
	private static final String HERBIVORE_STRIP_RIGHT  = "trino/herbivore_right_strip.png";
	private static final String HERBIVORE_STRIP_BACK  = "trino/herbivore_back_strip.png";
	private static final String HERBIVORE_DIVING_STRIP_LEFT  = "trino/herbivore_left_diving_strip.png";
	private static final String HERBIVORE_SWIMMING_STRIP_LEFT  = "trino/herbivore_left_swimming_strip.png";
	private static final String HERBIVORE_SWIMMING_STRIP_RIGHT  = "trino/herbivore_right_swimming_strip.png";
	private static final String HERBIVORE_SWIMMING_STRIP_FRONT  = "trino/herbivore_front_swimming_strip.png";
	private static final String HERBIVORE_ENEMY_SWIMMING_STRIP_BACK = "trino/herbivore_enemy_back_swimming_strip.png";
	private static final String HERBIVORE_ENEMY_SWIMMING_STRIP_LEFT  = "trino/herbivore_enemy_left_swimming_strip.png";
	private static final String HERBIVORE_ENEMY_SWIMMING_STRIP_RIGHT  = "trino/herbivore_enemy_right_swimming_strip.png";
	private static final String HERBIVORE_ENEMY_SWIMMING_STRIP_FRONT  = "trino/herbivore_enemy_front_swimming_strip.png";
	private static final String HERBIVORE_SWIMMING_STRIP_BACK = "trino/herbivore_back_swimming_strip.png";
	private static final String HERBIVORE_GOING_IN_STRIP_FRONT = "trino/herbivore_front_going_in_strip.png";
	private static final String HERBIVORE_GOING_IN_STRIP_BACK = "trino/herbivore_back_going_in_strip.png";
	private static final String HERBIVORE_GOING_IN_STRIP_LEFT = "trino/herbivore_left_going_in_strip.png";
	private static final String HERBIVORE_GOING_IN_STRIP_RIGHT = "trino/herbivore_right_going_in_strip.png";
	private static final String HERBIVORE_GOING_OUT_STRIP_FRONT = "trino/herbivore_front_going_out_strip.png";
	private static final String HERBIVORE_GOING_OUT_STRIP_BACK = "trino/herbivore_back_going_out_strip.png";
	private static final String HERBIVORE_GOING_OUT_STRIP_RIGHT = "trino/herbivore_right_going_out_strip.png";
	private static final String HERBIVORE_GOING_OUT_STRIP_LEFT = "trino/herbivore_left_going_out_strip.png";
	private static final String HERBIVORE_EATING_STRIP_FRONT  = "trino/herbivore_front_eating_strip.png";
	private static final String HERBIVORE_EATING_STRIP_LEFT  = "trino/herbivore_left_eating_strip.png";
	private static final String HERBIVORE_EATING_STRIP_RIGHT  = "trino/herbivore_right_eating_strip.png";
	private static final String HERBIVORE_EATING_STRIP_BACK = "trino/herbivore_back_eating_strip.png";
	private static final String HERBIVORE_PLACECAMO_STRIP_FRONT = "trino/herbivore_front_placeCamo.png";
	private static final String HERBIVORE_PLACECAMO_STRIP_LEFT = "trino/herbivore_left_placeCamo.png";
	private static final String HERBIVORE_PLACECAMO_STRIP_RIGHT = "trino/herbivore_right_placeCamo.png";
	private static final String HERBIVORE_PLACECAMO_STRIP_BACK = "trino/herbivore_back_placeCamo.png";
	private static final String HERBIVORE_CAMO_STRIP_FRONT = "trino/herbivore_front_camo.png";
	private static final String HERBIVORE_CAMO_STRIP_LEFT = "trino/herbivore_left_camo.png";
	private static final String HERBIVORE_CAMO_STRIP_RIGHT = "trino/herbivore_right_camo.png";
	private static final String HERBIVORE_CAMO_STRIP_BACK = "trino/herbivore_back_camo.png";
	private static final String CARNIVORE_STRIP_FRONT  = "trino/carnivore_front_strip.png";
	private static final String CARNIVORE_STRIP_LEFT  = "trino/carnivore_left_strip.png";
	private static final String CARNIVORE_STRIP_RIGHT  = "trino/carnivore_right_strip.png";
	private static final String CARNIVORE_STRIP_BACK  = "trino/carnivore_back_strip.png";
	private static final String CARNIVORE_EATING_STRIP_FRONT  = "trino/carnivore_front_eating_strip.png";
	private static final String CARNIVORE_EATING_STRIP_LEFT  = "trino/carnivore_left_eating_strip.png";
	private static final String CARNIVORE_EATING_STRIP_RIGHT  = "trino/carnivore_right_eating_strip.png";
	private static final String CARNIVORE_EATING_STRIP_BACK = "trino/carnivore_back_eating_strip.png";
	private static final String CARNIVORE_CHARGE_STRIP_FRONT = "trino/carnivore_front_charge_strip.png";
	private static final String CARNIVORE_CHARGE_STRIP_LEFT = "trino/carnivore_left_charge_strip.png";
	private static final String CARNIVORE_CHARGE_STRIP_RIGHT = "trino/carnivore_right_charge_strip.png";
	private static final String CARNIVORE_CHARGE_STRIP_BACK = "trino/carnivore_back_charge_strip.png";
	private static final String CARNIVORE_ATTACK_STRIP_FRONT = "trino/carnivore_front_attack_strip.png";
	private static final String CARNIVORE_ATTACK_STRIP_LEFT = "trino/carnivore_left_attack_strip.png";
	private static final String CARNIVORE_ATTACK_STRIP_RIGHT = "trino/carnivore_right_attack_strip.png";
	private static final String CARNIVORE_ATTACK_STRIP_BACK = "trino/carnivore_back_attack_strip.png";
	private static final String ENEMY_STRIP_FRONT = "trino/enemy_front_strip.png";
	private static final String ENEMY_STRIP_LEFT = "trino/enemy_left_strip.png";
	private static final String ENEMY_STRIP_RIGHT = "trino/enemy_right_strip.png";
	private static final String ENEMY_STRIP_BACK = "trino/enemy_back_strip.png";
	private static final String UNKILLABLE_ENEMY_STRIP_FRONT = "trino/unkillable_enemy_front.png";
	private static final String UNKILLABLE_ENEMY_STRIP_LEFT = "trino/unkillable_enemy_left.png";
	private static final String UNKILLABLE_ENEMY_STRIP_RIGHT = "trino/unkillable_enemy_right.png";
	private static final String UNKILLABLE_ENEMY_STRIP_BACK = "trino/unkillable_enemy_back.png";
	private static final String ENEMY_STUNNED_STRIP_FRONT = "trino/enemy_front_stunned_strip.png";
	private static final String ENEMY_STUNNED_STRIP_LEFT = "trino/enemy_left_stunned_strip.png";
	private static final String ENEMY_STUNNED_STRIP_RIGHT = "trino/enemy_right_stunned_strip.png";
	private static final String ENEMY_STUNNED_STRIP_BACK = "trino/enemy_back_stunned_strip.png";
	private static final String ENEMY_CHARGE_STRIP_LEFT = "trino/enemy_left_charge_strip.png";
	private static final String ENEMY_CHARGE_STRIP_RIGHT = "trino/enemy_right_charge_strip.png";
	private static final String ENEMY_CHARGE_STRIP_FRONT = "trino/enemy_front_charge_strip.png";
	private static final String ENEMY_CHARGE_STRIP_BACK = "trino/enemy_back_charge_strip.png";
	private static final String ENEMY_ATTACK_STRIP_LEFT = "trino/enemy_left_attack_strip.png";
	private static final String ENEMY_ATTACK_STRIP_RIGHT = "trino/enemy_right_attack_strip.png";
	private static final String ENEMY_ATTACK_STRIP_FRONT = "trino/enemy_front_attack_strip.png";
	private static final String ENEMY_ATTACK_STRIP_BACK = "trino/enemy_back_attack_strip.png";
	private static final String ENEMY_EATING_STRIP_FRONT = "trino/enemy_eating_front_strip.png";
	private static final String ENEMY_EATING_STRIP_BACK = "trino/enemy_eating_back_strip.png";
	private static final String ENEMY_EATING_STRIP_LEFT = "trino/enemy_eating_left_strip.png";
	private static final String ENEMY_EATING_STRIP_RIGHT = "trino/enemy_eating_right_strip.png";
	private static final String ENEMY_LEFT_EATING_STRIP = "trino/enemy_left_eaten_strip.png";
	private static final String DOLL_TO_HERB_STRIP = "trino/dollToHerb.png";
	private static final String DOLL_TO_CARN_STRIP = "trino/dollToCarn.png";
	private static final String HERB_TO_DOLL_STRIP = "trino/herbToDoll.png";
	private static final String HERB_TO_CARN_STRIP = "trino/herbToCarn.png";
	private static final String CARN_TO_DOLL_STRIP = "trino/carnToDoll.png";
	private static final String CARN_TO_HERB_STRIP = "trino/carnToHerb.png";
	private static final String VINE_DROP_STRIP = "trino/vine_falling.png";
	private static final String EXCLAMATION_STRIP = "trino/exclamation.png";
	private static final String FIREFLY_FILE = "trino/ffNick.png";
	private static final String FIREFLY_PURPLE_FILE = "trino/ffPurple.png";
	private static final String FIREFLY_BLUE_FILE = "trino/ffBlue.png";
	private static final String FIREFLY_PINK_FILE = "trino/ffPink.png";
	private static final String ROCK_1_FILE = "trino/rock1.png";
	private static final String ROCK_2_FILE = "trino/rock2.png";
	private static final String ROCK_3_FILE = "trino/rock3.png";
	private static final String FISH_1_FILE = "trino/fish1.png";
	private static final String FISH_2_FILE = "trino/fish2.png";
	private static final String FISH_3_FILE = "trino/fish3.png";
	private static final String WALL_FILE = "trino/wall_long.png";
	private static final String WALL_2_FILE = "trino/wall2.png";
	private static final String WALL_3_FILE = "trino/wall3.png";
	private static final String EDIBLE_WALL_FILE = "trino/ediblewall_long.png";
	private static final String EDIBLE_WALL_EATING_STRIP = "trino/ediblewall_decay_strip.png";
	private static final String COTTON_FLOWER_FILE = "trino/cotton.png";
	private static final String PATH_FILE = "trino/path.png";
	private static final String SWITCH_FILE = "trino/button.png";
	private static final String SWITCH_FILE_ONE = "trino/button1.png";
	private static final String SWITCH_FILE_TWO = "trino/button2.png";
	private static final String SWITCH_FILE_THREE = "trino/button3.png";
    private static final String SWITCH_DOWN_FILE = "trino/buttonPressed.png";
    private static final String SWITCH_DOWN_FILE_ONE = "trino/buttonPressed1.png";
    private static final String SWITCH_DOWN_FILE_TWO = "trino/buttonPressed2.png";
    private static final String SWITCH_DOWN_FILE_THREE = "trino/buttonPressed3.png";
	private static final String RIVER_FILE = "trino/river.png";
	private static final String BOULDER_FILE = "trino/boulder.png";
	private static final String VICTORY_FILE = "trino/victoryImage.png";
	private static final String GAMEOVER_FILE = "trino/gameoverImage.png";
	private static final String TUTORIAL_FILE = "trino/tutorialOverlay.png";
	private static final String SWING_OUT_STRIP = "trino/vineswingtocamera.png";
	private static final String SWING_IN_STRIP = "trino/vineswingout.png";

	// Pause menu assets
	private static final String PAUSE_MENU_FILE = "pause/pauseMenu.png";
	private static final String GRAYOUT_FILE = "pause/greyOut.png";
	private static final String MUSIC_OFF_FILE = "pause/musicOff.png";
	private static final String MUSIC_ON_FILE = "pause/musicOn.png";
	private static final String SOUND_OFF_FILE = "pause/soundOff.png";
	private static final String SOUND_ON_FILE = "pause/soundOn.png";
	private static final String MENU_FILE = "pause/menu.png";
	private static final String HELP_FILE = "pause/help.png";
	private static final String RESUME_FILE = "pause/resume.png";
	private static final String RESTART_FILE = "pause/restart.png";

	// Help menu assets
	private static final String HELP_MENU_FILE = "help/help.png";
	private static final String OUTLINE_FILE = "help/outline.png";
	private static final String EXIT_FILE = "help/exit.png";
	private static final String ICON_DOLL_FILE = "help/sprite_doll.png";
	private static final String ICON_HERBIVORE_FILE = "help/sprite_herbivore.png";
	private static final String ICON_CARNIVORE_FILE = "help/sprite_carnivore.png";
	private static final String ONE_FILE = "help/key_one.png";
	private static final String TWO_FILE = "help/key_two.png";
	private static final String THREE_FILE = "help/key_three.png";
	private static final String SPACE_FILE = "help/key_space.png";
	private static final String DOLL_SPECIAL_FILE = "help/doll_clone.png";
	private static final String HERBIVORE_SPECIAL_FILE = "help/herbivore_camouflage.png";
	private static final String CARNIVORE_SPECIAL_FILE = "help/carnivore_charge.png";
	private static final String DOLL_RESOURCE_FILE = "help/doll_cotton.png";
	private static final String HERBIVORE_RESOURCE_FILE = "help/herbivore_wall.png";
	private static final String CARNIVORE_RESOURCE_FILE = "help/carnivore_stun.png";
	private static final String DOLL_HEADER_FILE = "help/doll_header.png";
	private static final String HERBIVORE_HEADER_FILE = "help/herbivore_header.png";
	private static final String CARNIVORE_HEADER_FILE = "help/carnivore_header.png";
	private static final String DOLL_FORM_FILE = "help/form_doll.png";
	private static final String HERBIVORE_FORM_FILE = "help/form_herbivore.png";
	private static final String CARNIVORE_FORM_FILE = "help/form_carnivore.png";
	private static final String HOLD_FILE = "help/key_hold.png";
	private static final String PRESS_FILE = "help/text_press.png";
	private static final String SELECT_FILE = "help/text_select.png";
	private static final String DOLL_TRANSFORM_FILE = "help/text_doll_transform.png";
	private static final String HERBIVORE_TRANSFORM_FILE = "help/text_herbivore_transform.png";
	private static final String CARNIVORE_TRANSFORM_FILE = "help/text_carnivore_transform.png";
	private static final String DOLL_SPECIAL_TEXT_FILE = "help/text_doll_special.png";
	private static final String HERBIVORE_SPECIAL_TEXT_FILE = "help/text_herbivore_special.png";
	private static final String CARNIVORE_SPECIAL_TEXT_FILE = "help/text_carnivore_special.png";
	private static final String EAT_FILE = "help/text_eat.png";
	private static final String RIVER_CORNER_LEFT_TOP_FILE = "trino/river_top_left.png";
	private static final String RIVER_CORNER_LEFT_BOT_FILE = "trino/river_left_bottom.png";
    private static final String RIVER_CORNER_RIGHT_TOP_FILE = "trino/river_top_right.png";
    private static final String RIVER_CORNER_RIGHT_BOT_FILE = "trino/river_right_bottom.png";
    private static final String RIVER_CENTER_FILE = "trino/river.png";
    private static final String RIVER_LEFT_EDGE_FILE = "trino/river_left.png";
    private static final String RIVER_RIGHT_EDGE_FILE = "trino/river_right.png";
    private static final String RIVER_TOP_EDGE_FILE = "trino/river_top.png";
    private static final String RIVER_BOT_EDGE_FILE = "trino/river_bottom.png";
    private static final String RIVER_TOP_3_FILE = "trino/river_top_3_sides.png";
    private static final String RIVER_BOT_3_FILE = "trino/river_bottom_3_sides.png";
    private static final String RIVER_LEFT_3_FILE = "trino/river_left_3_sides.png";
    private static final String RIVER_RIGHT_3_FILE = "trino/river_right_3_sides.png";
    private static final String RIVER_VERT_2_FILE = "trino/river_one_width.png";
    private static final String RIVER_HOR_2_FILE = "trino/river_one_height.png";
    private static final String CORNER_BOTTOM_LEFT = "trino/corner_bottom_left.png";
    private static final String CORNER_BOTTOM_RIGHT = "trino/corner_bottom_right.png";
    private static final String CORNER_TOP_LEFT = "trino/corner_top_left.png";
    private static final String CORNER_TOP_RIGHT = "trino/corner_top_right.png";
    private static final String LONG_VINE_FILE = "trino/vine_long.png";
    private static final String WATER_SHINE_FILE = "trino/watershine.png";

	// Tutorial menus
	private static final String TUT_ONE_A = "tutorial/move1.png";
	private static final String TUT_ONE_B = "tutorial/move2.png";
	private static final String TUT_ONE_C = "tutorial/cloneDrop1.png";
	private static final String TUT_ONE_D = "tutorial/cloneDrop2.png";
	private static final String TUT_TWO_A = "tutorial/dollPickUpCotton1.png";
	private static final String TUT_TWO_B = "tutorial/dollPickUpCotton2.png";
	private static final String TUT_TWO_C = "tutorial/herbRiver1.png";
	private static final String TUT_TWO_D = "tutorial/herbRiver2.png";
	private static final String TUT_THREE_A = "tutorial/herbEating1.png";
    private static final String TUT_THREE_B = "tutorial/herbEating2.png";
	private static final String TUT_FOUR_A = "tutorial/camo1.png";
	private static final String TUT_FOUR_B = "tutorial/camo2.png";
	private static final String TUT_SIX_A = "tutorial/stun1.png";
	private static final String TUT_SIX_B = "tutorial/stun2.png";
	private static final String TUT_SIX_C = "tutorial/carnEating1.png";
	private static final String TUT_SIX_D = "tutorial/carnEating2.png";
	private static final String TUT_SEVEN_A = "tutorial/moveBoulder1.png";
	private static final String TUT_SEVEN_B = "tutorial/moveBoulder2.png";
	private static final String TUT_EIGHT_A = "tutorial/unkillableWarning.png";
	private static final String TUT_NINE_A = "tutorial/multiSwitch1.png";
	private static final String TUT_NINE_B = "tutorial/multiSwitch2.png";

	// Texture assets variables
	private BitmapFont displayFont;
	private Hashtable<String, TextureRegion> textureDict = new Hashtable<String, TextureRegion>();
	private Hashtable<String, Texture> filmStripDict = new Hashtable<String, Texture>();

	// GAME CONSTANTS
	private static final int EXIT_COUNT = 0; // How many frames after winning/losing do we continue?
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
	private float ffLightDsts[];
	private float ffLightChanges[];

	private FilmStrip swingInStrip;
	private FilmStrip swingOutStrip;

	private World world;
	private Level level;

	private int currentLevel;

	private PooledList<AIController> controls = new PooledList<AIController>();
	private PooledList<FireFlyAIController> fireFlyControls = new PooledList<FireFlyAIController>();

	private boolean active; // Whether or not this is an active controller
	private boolean complete; // Whether we have completed this level
	private boolean failed; // Whether we have failed at this world (and need a reset)
	private boolean timeOut; // Whether time ran out or not
	private int countdown; // Countdown active for winning or losing
	private boolean removeClone; // Whether or not the clone should be removed
	public static int menuNum = 0;
	public static boolean musicState = true;
	public static boolean soundState = true;
	private int playDoorDown = 0;
	private int playDoorUp = 0;
	private int playDoorSound = -1;
	private float swingAnimeFrame = 0;
	private float elapsed;
	private float duration;
	private float radius;
	private float randomAngle;
	private float intensity;
	private boolean transform = false;
	private Vector2 currentRiver;
	private Color hoverColor = new Color(2.55f, 2.48f, 2.40f, 1); // for UI hovering

	/** Timer */
	float levelTime = 60;
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
		manager.load(GREEN_DOOR_STRIP, Texture.class);
		assets.add(GREEN_DOOR_STRIP);
		manager.load(YELLOW_DOOR_STRIP, Texture.class);
		assets.add(YELLOW_DOOR_STRIP);
		manager.load(BLUE_DOOR_STRIP, Texture.class);
		assets.add(BLUE_DOOR_STRIP);
		manager.load(RED_DOOR_STRIP, Texture.class);
		assets.add(RED_DOOR_STRIP);
		manager.load(GOAL_FILE,Texture.class);
		assets.add(GOAL_FILE);
		manager.load(GOAL_CLOSED_FILE,Texture.class);
		assets.add(GOAL_CLOSED_FILE);
		manager.load(DOOR_FILE_ONE, Texture.class);
		assets.add(DOOR_FILE_ONE);
		manager.load(DOOR_CLOSED_FILE_ONE, Texture.class);
		assets.add(DOOR_CLOSED_FILE_ONE);
		manager.load(DOOR_FILE_TWO, Texture.class);
		assets.add(DOOR_FILE_TWO);
		manager.load(DOOR_CLOSED_FILE_TWO, Texture.class);
		assets.add(DOOR_CLOSED_FILE_TWO);
		manager.load(DOOR_FILE_THREE, Texture.class);
		assets.add(DOOR_FILE_THREE);
		manager.load(DOOR_CLOSED_FILE_THREE, Texture.class);
		assets.add(DOOR_CLOSED_FILE_THREE);
		manager.load(DOOR_FLASHING_STRIP, Texture.class);
		assets.add(DOOR_FLASHING_STRIP);
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
		manager.load(HERBIVORE_DIVING_STRIP_LEFT, Texture.class);
		assets.add(HERBIVORE_DIVING_STRIP_LEFT);
		manager.load(HERBIVORE_SWIMMING_STRIP_LEFT, Texture.class);
		assets.add(HERBIVORE_SWIMMING_STRIP_LEFT);
		manager.load(HERBIVORE_SWIMMING_STRIP_RIGHT, Texture.class);
		assets.add(HERBIVORE_SWIMMING_STRIP_RIGHT);
		manager.load(HERBIVORE_SWIMMING_STRIP_BACK, Texture.class);
		assets.add(HERBIVORE_SWIMMING_STRIP_BACK);
		manager.load(HERBIVORE_SWIMMING_STRIP_FRONT, Texture.class);
		assets.add(HERBIVORE_SWIMMING_STRIP_FRONT);
		manager.load(HERBIVORE_ENEMY_SWIMMING_STRIP_LEFT, Texture.class);
		assets.add(HERBIVORE_ENEMY_SWIMMING_STRIP_LEFT);
		manager.load(HERBIVORE_ENEMY_SWIMMING_STRIP_RIGHT, Texture.class);
		assets.add(HERBIVORE_ENEMY_SWIMMING_STRIP_RIGHT);
		manager.load(HERBIVORE_ENEMY_SWIMMING_STRIP_BACK, Texture.class);
		assets.add(HERBIVORE_ENEMY_SWIMMING_STRIP_BACK);
		manager.load(HERBIVORE_ENEMY_SWIMMING_STRIP_FRONT, Texture.class);
		assets.add(HERBIVORE_ENEMY_SWIMMING_STRIP_FRONT);
		manager.load(HERBIVORE_GOING_IN_STRIP_FRONT, Texture.class);
		assets.add(HERBIVORE_GOING_IN_STRIP_FRONT);
		manager.load(HERBIVORE_GOING_IN_STRIP_BACK, Texture.class);
		assets.add(HERBIVORE_GOING_IN_STRIP_BACK);
		manager.load(HERBIVORE_GOING_IN_STRIP_LEFT, Texture.class);
		assets.add(HERBIVORE_GOING_IN_STRIP_LEFT);
		manager.load(HERBIVORE_GOING_IN_STRIP_RIGHT, Texture.class);
		assets.add(HERBIVORE_GOING_IN_STRIP_RIGHT);
		manager.load(HERBIVORE_GOING_OUT_STRIP_FRONT, Texture.class);
		assets.add(HERBIVORE_GOING_OUT_STRIP_FRONT);
		manager.load(HERBIVORE_GOING_OUT_STRIP_BACK, Texture.class);
		assets.add(HERBIVORE_GOING_OUT_STRIP_BACK);
		manager.load(HERBIVORE_GOING_OUT_STRIP_RIGHT, Texture.class);
		assets.add(HERBIVORE_GOING_OUT_STRIP_RIGHT);
		manager.load(HERBIVORE_GOING_OUT_STRIP_LEFT, Texture.class);
		assets.add(HERBIVORE_GOING_OUT_STRIP_LEFT);
		manager.load(HERBIVORE_EATING_STRIP_FRONT, Texture.class);
		assets.add(HERBIVORE_EATING_STRIP_FRONT);
		manager.load(HERBIVORE_EATING_STRIP_LEFT, Texture.class);
		assets.add(HERBIVORE_EATING_STRIP_LEFT);
		manager.load(HERBIVORE_EATING_STRIP_RIGHT, Texture.class);
		assets.add(HERBIVORE_EATING_STRIP_RIGHT);
		manager.load(HERBIVORE_EATING_STRIP_BACK, Texture.class);
		assets.add(HERBIVORE_EATING_STRIP_BACK);
		manager.load(HERBIVORE_PLACECAMO_STRIP_BACK, Texture.class);
		assets.add(HERBIVORE_PLACECAMO_STRIP_BACK);
		manager.load(HERBIVORE_PLACECAMO_STRIP_FRONT, Texture.class);
		assets.add(HERBIVORE_PLACECAMO_STRIP_FRONT);
		manager.load(HERBIVORE_PLACECAMO_STRIP_RIGHT, Texture.class);
		assets.add(HERBIVORE_PLACECAMO_STRIP_RIGHT);
		manager.load(HERBIVORE_PLACECAMO_STRIP_LEFT, Texture.class);
		assets.add(HERBIVORE_PLACECAMO_STRIP_LEFT);
		manager.load(HERBIVORE_CAMO_STRIP_LEFT, Texture.class);
		assets.add(HERBIVORE_CAMO_STRIP_LEFT);
		manager.load(HERBIVORE_CAMO_STRIP_RIGHT, Texture.class);
		assets.add(HERBIVORE_CAMO_STRIP_RIGHT);
		manager.load(HERBIVORE_CAMO_STRIP_BACK, Texture.class);
		assets.add(HERBIVORE_CAMO_STRIP_BACK);
		manager.load(HERBIVORE_CAMO_STRIP_FRONT, Texture.class);
		assets.add(HERBIVORE_CAMO_STRIP_FRONT);
		manager.load(CARNIVORE_STRIP_LEFT, Texture.class);
		assets.add(CARNIVORE_STRIP_LEFT);
		manager.load(CARNIVORE_STRIP_RIGHT, Texture.class);
		assets.add(CARNIVORE_STRIP_RIGHT);
		manager.load(CARNIVORE_STRIP_FRONT, Texture.class);
		assets.add(CARNIVORE_STRIP_FRONT);
		manager.load(CARNIVORE_STRIP_BACK, Texture.class);
		assets.add(CARNIVORE_STRIP_BACK);
		manager.load(CARNIVORE_EATING_STRIP_FRONT, Texture.class);
		assets.add(CARNIVORE_EATING_STRIP_FRONT);
		manager.load(CARNIVORE_EATING_STRIP_LEFT, Texture.class);
		assets.add(CARNIVORE_EATING_STRIP_LEFT);
		manager.load(CARNIVORE_EATING_STRIP_RIGHT, Texture.class);
		assets.add(CARNIVORE_EATING_STRIP_RIGHT);
		manager.load(CARNIVORE_EATING_STRIP_BACK, Texture.class);
		assets.add(CARNIVORE_EATING_STRIP_BACK);
		manager.load(CARNIVORE_CHARGE_STRIP_FRONT, Texture.class);
		assets.add(CARNIVORE_CHARGE_STRIP_FRONT);
		manager.load(CARNIVORE_CHARGE_STRIP_LEFT, Texture.class);
		assets.add(CARNIVORE_CHARGE_STRIP_LEFT);
		manager.load(CARNIVORE_CHARGE_STRIP_RIGHT, Texture.class);
		assets.add(CARNIVORE_CHARGE_STRIP_RIGHT);
		manager.load(CARNIVORE_CHARGE_STRIP_BACK, Texture.class);
		assets.add(CARNIVORE_CHARGE_STRIP_BACK);
		manager.load(CARNIVORE_ATTACK_STRIP_FRONT, Texture.class);
		assets.add(CARNIVORE_ATTACK_STRIP_FRONT);
		manager.load(CARNIVORE_ATTACK_STRIP_LEFT, Texture.class);
		assets.add(CARNIVORE_ATTACK_STRIP_LEFT);
		manager.load(CARNIVORE_ATTACK_STRIP_RIGHT, Texture.class);
		assets.add(CARNIVORE_ATTACK_STRIP_RIGHT);
		manager.load(CARNIVORE_ATTACK_STRIP_BACK, Texture.class);
		assets.add(CARNIVORE_ATTACK_STRIP_BACK);
		manager.load(DOLL_TO_HERB_STRIP, Texture.class);
		assets.add(DOLL_TO_HERB_STRIP);
		manager.load(DOLL_TO_CARN_STRIP, Texture.class);
		assets.add(DOLL_TO_CARN_STRIP);
		manager.load(HERB_TO_DOLL_STRIP, Texture.class);
		assets.add(HERB_TO_DOLL_STRIP);
		manager.load(HERB_TO_CARN_STRIP, Texture.class);
		assets.add(HERB_TO_CARN_STRIP);
		manager.load(CARN_TO_DOLL_STRIP, Texture.class);
		assets.add(CARN_TO_DOLL_STRIP);
		manager.load(CARN_TO_HERB_STRIP, Texture.class);
		assets.add(CARN_TO_HERB_STRIP);
		manager.load(WALL_FILE, Texture.class);
		assets.add(WALL_FILE);
		manager.load(WALL_2_FILE, Texture.class);
		assets.add(WALL_2_FILE);
		manager.load(WALL_3_FILE, Texture.class);
		assets.add(WALL_3_FILE);
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
		manager.load(ENEMY_CHARGE_STRIP_BACK, Texture.class);
		assets.add(ENEMY_CHARGE_STRIP_BACK);
		manager.load(ENEMY_CHARGE_STRIP_FRONT, Texture.class);
		assets.add(ENEMY_CHARGE_STRIP_FRONT);
		manager.load(ENEMY_ATTACK_STRIP_LEFT, Texture.class);
		assets.add(ENEMY_ATTACK_STRIP_LEFT);
		manager.load(ENEMY_ATTACK_STRIP_RIGHT, Texture.class);
		assets.add(ENEMY_ATTACK_STRIP_RIGHT);
		manager.load(ENEMY_ATTACK_STRIP_FRONT, Texture.class);
		assets.add(ENEMY_ATTACK_STRIP_FRONT);
		manager.load(ENEMY_ATTACK_STRIP_BACK, Texture.class);
		assets.add(ENEMY_ATTACK_STRIP_BACK);
		manager.load(ENEMY_LEFT_EATING_STRIP, Texture.class);
		assets.add(ENEMY_LEFT_EATING_STRIP);
		manager.load(ENEMY_EATING_STRIP_FRONT, Texture.class);
		assets.add(ENEMY_EATING_STRIP_FRONT);
		manager.load(ENEMY_EATING_STRIP_BACK, Texture.class);
		assets.add(ENEMY_EATING_STRIP_BACK);
		manager.load(ENEMY_EATING_STRIP_LEFT, Texture.class);
		assets.add(ENEMY_EATING_STRIP_LEFT);
		manager.load(ENEMY_EATING_STRIP_RIGHT, Texture.class);
		assets.add(ENEMY_EATING_STRIP_RIGHT);
		manager.load(UNKILLABLE_ENEMY_STRIP_FRONT, Texture.class);
		assets.add(UNKILLABLE_ENEMY_STRIP_FRONT);
		manager.load(UNKILLABLE_ENEMY_STRIP_LEFT, Texture.class);
		assets.add(UNKILLABLE_ENEMY_STRIP_LEFT);
		manager.load(UNKILLABLE_ENEMY_STRIP_RIGHT, Texture.class);
		assets.add(UNKILLABLE_ENEMY_STRIP_RIGHT);
		manager.load(UNKILLABLE_ENEMY_STRIP_BACK, Texture.class);
		assets.add(UNKILLABLE_ENEMY_STRIP_BACK);
		manager.load(VINE_DROP_STRIP, Texture.class);
		assets.add(VINE_DROP_STRIP);
		manager.load(EXCLAMATION_STRIP, Texture.class);
		assets.add(EXCLAMATION_STRIP);
		manager.load(FIREFLY_FILE, Texture.class);
		assets.add(FIREFLY_FILE);
		manager.load(FIREFLY_PURPLE_FILE, Texture.class);
		assets.add(FIREFLY_PURPLE_FILE);
		manager.load(FIREFLY_BLUE_FILE, Texture.class);
		assets.add(FIREFLY_BLUE_FILE);
		manager.load(FIREFLY_PINK_FILE, Texture.class);
		assets.add(FIREFLY_PINK_FILE);
		manager.load(ROCK_1_FILE, Texture.class);
		assets.add(ROCK_1_FILE);
		manager.load(ROCK_2_FILE, Texture.class);
		assets.add(ROCK_2_FILE);
		manager.load(ROCK_3_FILE, Texture.class);
		assets.add(ROCK_3_FILE);
		manager.load(FISH_1_FILE, Texture.class);
		assets.add(FISH_1_FILE);
		manager.load(FISH_2_FILE, Texture.class);
		assets.add(FISH_2_FILE);
		manager.load(FISH_3_FILE, Texture.class);
		assets.add(FISH_3_FILE);
		manager.load(PATH_FILE, Texture.class);
		assets.add(PATH_FILE);
		manager.load(SWITCH_FILE, Texture.class);
		assets.add(SWITCH_FILE);
		manager.load(SWITCH_FILE_ONE, Texture.class);
		assets.add(SWITCH_FILE_ONE);
		manager.load(SWITCH_FILE_TWO, Texture.class);
		assets.add(SWITCH_FILE_TWO);
		manager.load(SWITCH_FILE_THREE, Texture.class);
		assets.add(SWITCH_FILE_THREE);
		manager.load(RIVER_FILE, Texture.class);
		assets.add(SWITCH_DOWN_FILE);
		manager.load(SWITCH_DOWN_FILE, Texture.class);
		assets.add(SWITCH_DOWN_FILE_ONE);
		manager.load(SWITCH_DOWN_FILE_ONE, Texture.class);
		assets.add(SWITCH_DOWN_FILE_TWO);
		manager.load(SWITCH_DOWN_FILE_TWO, Texture.class);
		assets.add(SWITCH_DOWN_FILE_THREE);
		manager.load(SWITCH_DOWN_FILE_THREE, Texture.class);
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
		manager.load(MENU_FILE, Texture.class);
		assets.add(MENU_FILE);
		manager.load(HELP_FILE, Texture.class);
		assets.add(HELP_FILE);
		manager.load(RESUME_FILE, Texture.class);
		assets.add(RESUME_FILE);
		manager.load(RESTART_FILE, Texture.class);
		assets.add(RESTART_FILE);
		manager.load(HELP_MENU_FILE, Texture.class);
		assets.add(HELP_MENU_FILE);
		manager.load(OUTLINE_FILE, Texture.class);
		assets.add(OUTLINE_FILE);
		manager.load(EXIT_FILE, Texture.class);
		assets.add(EXIT_FILE);
		manager.load(ICON_DOLL_FILE, Texture.class);
		assets.add(ICON_DOLL_FILE);
		manager.load(ICON_HERBIVORE_FILE, Texture.class);
		assets.add(ICON_HERBIVORE_FILE);
		manager.load(ICON_CARNIVORE_FILE, Texture.class);
		assets.add(ICON_CARNIVORE_FILE);
		manager.load(ONE_FILE, Texture.class);
		assets.add(ONE_FILE);
		manager.load(TWO_FILE, Texture.class);
		assets.add(TWO_FILE);
		manager.load(THREE_FILE, Texture.class);
		assets.add(THREE_FILE);
		manager.load(SPACE_FILE, Texture.class);
		assets.add(SPACE_FILE);
		manager.load(DOLL_SPECIAL_FILE, Texture.class);
		assets.add(DOLL_SPECIAL_FILE);
		manager.load(HERBIVORE_SPECIAL_FILE, Texture.class);
		assets.add(HERBIVORE_SPECIAL_FILE);
		manager.load(CARNIVORE_SPECIAL_FILE, Texture.class);
		assets.add(CARNIVORE_SPECIAL_FILE);
		manager.load(DOLL_RESOURCE_FILE, Texture.class);
		assets.add(DOLL_RESOURCE_FILE);
		manager.load(HERBIVORE_RESOURCE_FILE, Texture.class);
		assets.add(HERBIVORE_RESOURCE_FILE);
		manager.load(CARNIVORE_RESOURCE_FILE, Texture.class);
		assets.add(CARNIVORE_RESOURCE_FILE);
		manager.load(DOLL_HEADER_FILE, Texture.class);
		assets.add(DOLL_HEADER_FILE);
		manager.load(HERBIVORE_HEADER_FILE, Texture.class);
		assets.add(HERBIVORE_HEADER_FILE);
		manager.load(CARNIVORE_HEADER_FILE, Texture.class);
		assets.add(CARNIVORE_HEADER_FILE);
		manager.load(DOLL_FORM_FILE, Texture.class);
		assets.add(DOLL_FORM_FILE);
		manager.load(HERBIVORE_FORM_FILE, Texture.class);
		assets.add(HERBIVORE_FORM_FILE);
		manager.load(CARNIVORE_FORM_FILE, Texture.class);
		assets.add(CARNIVORE_FORM_FILE);
		manager.load(HOLD_FILE, Texture.class);
		assets.add(HOLD_FILE);
		manager.load(PRESS_FILE, Texture.class);
		assets.add(PRESS_FILE);
		manager.load(SELECT_FILE, Texture.class);
		assets.add(SELECT_FILE);
		manager.load(DOLL_TRANSFORM_FILE, Texture.class);
		assets.add(DOLL_TRANSFORM_FILE);
		manager.load(HERBIVORE_TRANSFORM_FILE, Texture.class);
		assets.add(HERBIVORE_TRANSFORM_FILE);
		manager.load(CARNIVORE_TRANSFORM_FILE, Texture.class);
		assets.add(CARNIVORE_TRANSFORM_FILE);
		manager.load(DOLL_SPECIAL_TEXT_FILE, Texture.class);
		assets.add(DOLL_SPECIAL_TEXT_FILE);
		manager.load(HERBIVORE_SPECIAL_TEXT_FILE, Texture.class);
		assets.add(HERBIVORE_SPECIAL_TEXT_FILE);
		manager.load(CARNIVORE_SPECIAL_TEXT_FILE, Texture.class);
		assets.add(CARNIVORE_SPECIAL_TEXT_FILE);
		manager.load(EAT_FILE, Texture.class);
		assets.add(EAT_FILE);
		manager.load(RIVER_BOT_3_FILE, Texture.class);
		assets.add(RIVER_BOT_3_FILE);
		manager.load(RIVER_TOP_3_FILE, Texture.class);
		assets.add(RIVER_TOP_3_FILE);
		manager.load(RIVER_HOR_2_FILE, Texture.class);
		assets.add(RIVER_HOR_2_FILE);
        manager.load(RIVER_VERT_2_FILE, Texture.class);
        assets.add(RIVER_VERT_2_FILE);
		manager.load(RIVER_LEFT_3_FILE, Texture.class);
		assets.add(RIVER_LEFT_3_FILE);
		manager.load(RIVER_RIGHT_3_FILE, Texture.class);
		assets.add(RIVER_RIGHT_3_FILE);
		manager.load(RIVER_BOT_EDGE_FILE, Texture.class);
		assets.add(RIVER_BOT_EDGE_FILE);
		manager.load(RIVER_LEFT_EDGE_FILE, Texture.class);
		assets.add(RIVER_LEFT_EDGE_FILE);
		manager.load(RIVER_RIGHT_EDGE_FILE, Texture.class);
		assets.add(RIVER_RIGHT_EDGE_FILE);
		manager.load(RIVER_TOP_EDGE_FILE, Texture.class);
		assets.add(RIVER_TOP_EDGE_FILE);
		manager.load(RIVER_CENTER_FILE, Texture.class);
		assets.add(RIVER_CENTER_FILE);
		manager.load(RIVER_CORNER_LEFT_BOT_FILE, Texture.class);
		assets.add(RIVER_CORNER_LEFT_BOT_FILE);
		manager.load(RIVER_CORNER_LEFT_TOP_FILE, Texture.class);
		assets.add(RIVER_CORNER_LEFT_TOP_FILE);
		manager.load(RIVER_CORNER_RIGHT_BOT_FILE, Texture.class);
		assets.add(RIVER_CORNER_RIGHT_BOT_FILE);
		manager.load(RIVER_CORNER_RIGHT_TOP_FILE, Texture.class);
		assets.add(RIVER_CORNER_RIGHT_TOP_FILE);
		manager.load(CORNER_BOTTOM_LEFT, Texture.class);
		assets.add(CORNER_BOTTOM_LEFT);
		manager.load(CORNER_BOTTOM_RIGHT, Texture.class);
		assets.add(CORNER_BOTTOM_RIGHT);
		manager.load(CORNER_TOP_LEFT, Texture.class);
		assets.add(CORNER_TOP_LEFT);
		manager.load(CORNER_TOP_RIGHT, Texture.class);
		assets.add(CORNER_TOP_RIGHT);
		manager.load(LONG_VINE_FILE, Texture.class);
		assets.add(LONG_VINE_FILE);
        manager.load(WATER_SHINE_FILE, Texture.class);
        assets.add(WATER_SHINE_FILE);
        manager.load(SWING_IN_STRIP, Texture.class);
        assets.add(SWING_IN_STRIP);
        manager.load(SWING_OUT_STRIP, Texture.class);
        assets.add(SWING_OUT_STRIP);
		manager.load(TUT_ONE_A, Texture.class);
		assets.add(TUT_ONE_A);
		manager.load(TUT_ONE_B, Texture.class);
		assets.add(TUT_ONE_B);
		manager.load(TUT_ONE_C, Texture.class);
		assets.add(TUT_ONE_C);
		manager.load(TUT_ONE_D, Texture.class);
		assets.add(TUT_ONE_D);
		manager.load(TUT_TWO_A, Texture.class);
		assets.add(TUT_TWO_A);
		manager.load(TUT_TWO_B, Texture.class);
		assets.add(TUT_TWO_B);
		manager.load(TUT_TWO_C, Texture.class);
		assets.add(TUT_TWO_C);
		manager.load(TUT_TWO_D, Texture.class);
		assets.add(TUT_TWO_D);
		manager.load(TUT_THREE_A, Texture.class);
		assets.add(TUT_THREE_A);
		manager.load(TUT_THREE_B, Texture.class);
		assets.add(TUT_THREE_B);
		manager.load(TUT_FOUR_A, Texture.class);
		assets.add(TUT_FOUR_A);
		manager.load(TUT_FOUR_B, Texture.class);
		assets.add(TUT_FOUR_B);
		manager.load(TUT_SIX_A, Texture.class);
		assets.add(TUT_SIX_A);
		manager.load(TUT_SIX_B, Texture.class);
		assets.add(TUT_SIX_B);
		manager.load(TUT_SIX_C, Texture.class);
		assets.add(TUT_SIX_C);
		manager.load(TUT_SIX_D, Texture.class);
		assets.add(TUT_SIX_D);
		manager.load(TUT_SEVEN_A, Texture.class);
		assets.add(TUT_SEVEN_A);
		manager.load(TUT_SEVEN_B, Texture.class);
		assets.add(TUT_SEVEN_B);
		manager.load(TUT_EIGHT_A, Texture.class);
		assets.add(TUT_EIGHT_A);
		manager.load(TUT_NINE_A, Texture.class);
		assets.add(TUT_NINE_A);
        manager.load(TUT_NINE_B, Texture.class);
        assets.add(TUT_NINE_B);

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
		textureDict.put("background", createTexture(manager,BACKGROUND_FILE,false));
		textureDict.put("overlay", createTexture(manager,OVERLAY_FILE,false));
		textureDict.put("goalOpenTile", createTexture(manager,GOAL_FILE,false));
		textureDict.put("goalClosedTile", createTexture(manager,GOAL_CLOSED_FILE, false));
		textureDict.put("doorOpenTileOne", createTexture(manager,DOOR_FILE_ONE,false));
		textureDict.put("doorClosedTileOne", createTexture(manager,DOOR_CLOSED_FILE_ONE,false));
		textureDict.put("doorOpenTileTwo", createTexture(manager,DOOR_FILE_TWO,false));
		textureDict.put("doorClosedTileTwo", createTexture(manager,DOOR_CLOSED_FILE_TWO,false));
		textureDict.put("doorOpenTileThree", createTexture(manager,DOOR_FILE_THREE, false));
		textureDict.put("doorClosedTileThree", createTexture(manager,DOOR_CLOSED_FILE_THREE, false));
		textureDict.put("clone", createTexture(manager,CLONE_FILE,false));
		textureDict.put("fireFly", createTexture(manager, FIREFLY_FILE, false));
		textureDict.put("fireFlyPurple", createTexture(manager, FIREFLY_PURPLE_FILE, false));
		textureDict.put("fireFlyBlue", createTexture(manager, FIREFLY_BLUE_FILE, false));
		textureDict.put("fireFlyPink", createTexture(manager, FIREFLY_PINK_FILE, false));
		textureDict.put("rock1", createTexture(manager, ROCK_1_FILE, false));
		textureDict.put("rock2", createTexture(manager, ROCK_2_FILE, false));
		textureDict.put("rock3", createTexture(manager, ROCK_3_FILE, false));
		textureDict.put("fish1", createTexture(manager, FISH_1_FILE, false));
		textureDict.put("fish2", createTexture(manager, FISH_2_FILE, false));
		textureDict.put("fish3", createTexture(manager, FISH_3_FILE, false));
		textureDict.put("wall", createTexture(manager,WALL_FILE,false));
		textureDict.put("wall2", createTexture(manager,WALL_2_FILE,false));
		textureDict.put("wall3", createTexture(manager,WALL_3_FILE,false));
		textureDict.put("edibleWall", createTexture(manager, EDIBLE_WALL_FILE, false));
		textureDict.put("cotton", createTexture(manager, COTTON_FLOWER_FILE, false));
		textureDict.put("switch", createTexture(manager, SWITCH_FILE, false));
		textureDict.put("switchone", createTexture(manager, SWITCH_FILE_ONE, false));
		textureDict.put("switchtwo", createTexture(manager, SWITCH_FILE_TWO, false));
		textureDict.put("switchthree", createTexture(manager, SWITCH_FILE_THREE, false));
		textureDict.put("switchoff", createTexture(manager, SWITCH_DOWN_FILE, false));
		textureDict.put("switchoneoff", createTexture(manager, SWITCH_DOWN_FILE_ONE, false));
		textureDict.put("switchtwooff", createTexture(manager, SWITCH_DOWN_FILE_TWO, false));
		textureDict.put("switchthreeoff", createTexture(manager, SWITCH_DOWN_FILE_THREE, false));
		textureDict.put("river", createTexture(manager, RIVER_FILE, false));
		textureDict.put("riverCenter", createTexture(manager, RIVER_CENTER_FILE, false));
        textureDict.put("riverCornerLeftTop", createTexture(manager, RIVER_CORNER_LEFT_TOP_FILE, false));
        textureDict.put("riverCornerLeftBot", createTexture(manager, RIVER_CORNER_LEFT_BOT_FILE, false));
        textureDict.put("riverCornerRightTop", createTexture(manager,RIVER_CORNER_RIGHT_TOP_FILE, false));
        textureDict.put("riverCornerRightBot", createTexture(manager, RIVER_CORNER_RIGHT_BOT_FILE, false));
        textureDict.put("riverLeftEdge", createTexture(manager, RIVER_LEFT_EDGE_FILE, false));
        textureDict.put("riverRightEdge", createTexture(manager, RIVER_RIGHT_EDGE_FILE, false));
        textureDict.put("riverTopEdge", createTexture(manager, RIVER_TOP_EDGE_FILE, false));
        textureDict.put("riverBotEdge", createTexture(manager, RIVER_BOT_EDGE_FILE, false));
        textureDict.put("riverLeft3Sides", createTexture(manager, RIVER_LEFT_3_FILE, false));
        textureDict.put("riverRight3Sides", createTexture(manager, RIVER_RIGHT_3_FILE, false));
        textureDict.put("riverTop3Sides", createTexture(manager, RIVER_TOP_3_FILE, false));
        textureDict.put("riverBot3Sides", createTexture(manager, RIVER_BOT_3_FILE, false));
        textureDict.put("riverVert2Sides", createTexture(manager, RIVER_VERT_2_FILE, false));
        textureDict.put("riverHor2Sides", createTexture(manager, RIVER_HOR_2_FILE, false));
        textureDict.put("cornerBottomLeft", createTexture(manager, CORNER_BOTTOM_LEFT, false));
        textureDict.put("cornerBottomRight", createTexture(manager, CORNER_BOTTOM_RIGHT, false));
        textureDict.put("cornerTopLeft", createTexture(manager, CORNER_TOP_LEFT, false));
        textureDict.put("cornerTopRight", createTexture(manager, CORNER_TOP_RIGHT, false));
        textureDict.put("longVine", createTexture(manager, LONG_VINE_FILE, false));
        textureDict.put("watershine", createTexture(manager, WATER_SHINE_FILE, false));
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
		textureDict.put("menuText", createTexture(manager, MENU_FILE, false));
		textureDict.put("helpText", createTexture(manager, HELP_FILE, false));
		textureDict.put("resumeText", createTexture(manager, RESUME_FILE, false));
		textureDict.put("restartText", createTexture(manager, RESTART_FILE, false));
		textureDict.put("helpMenu", createTexture(manager, HELP_MENU_FILE, false));
		textureDict.put("outline", createTexture(manager, OUTLINE_FILE, false));
		textureDict.put("exit", createTexture(manager, EXIT_FILE, false));
		textureDict.put("dollIcon", createTexture(manager, ICON_DOLL_FILE, false));
		textureDict.put("herbivoreIcon", createTexture(manager, ICON_HERBIVORE_FILE, false));
		textureDict.put("carnivoreIcon", createTexture(manager, ICON_CARNIVORE_FILE, false));
		textureDict.put("oneKey", createTexture(manager, ONE_FILE, false));
		textureDict.put("twoKey", createTexture(manager, TWO_FILE, false));
		textureDict.put("threeKey", createTexture(manager, THREE_FILE, false));
		textureDict.put("spaceKey", createTexture(manager, SPACE_FILE, false));
		textureDict.put("dollSpecial", createTexture(manager, DOLL_SPECIAL_FILE, false));
		textureDict.put("herbivoreSpecial", createTexture(manager, HERBIVORE_SPECIAL_FILE, false));
		textureDict.put("carnivoreSpecial", createTexture(manager, CARNIVORE_SPECIAL_FILE, false));
		textureDict.put("dollResource", createTexture(manager, DOLL_RESOURCE_FILE, false));
		textureDict.put("herbivoreResource", createTexture(manager, HERBIVORE_RESOURCE_FILE, false));
		textureDict.put("carnivoreResource", createTexture(manager, CARNIVORE_RESOURCE_FILE, false));
		textureDict.put("dollHeader", createTexture(manager, DOLL_HEADER_FILE, false));
		textureDict.put("herbivoreHeader", createTexture(manager, HERBIVORE_HEADER_FILE, false));
		textureDict.put("carnivoreHeader", createTexture(manager, CARNIVORE_HEADER_FILE, false));
		textureDict.put("dollForm", createTexture(manager, DOLL_FORM_FILE, false));
		textureDict.put("herbivoreForm", createTexture(manager, HERBIVORE_FORM_FILE, false));
		textureDict.put("carnivoreForm", createTexture(manager, CARNIVORE_FORM_FILE, false));
		textureDict.put("holdText", createTexture(manager, HOLD_FILE, false));
		textureDict.put("pressText", createTexture(manager, PRESS_FILE, false));
		textureDict.put("selectText", createTexture(manager, SELECT_FILE, false));
		textureDict.put("dollTransform", createTexture(manager, DOLL_TRANSFORM_FILE, false));
		textureDict.put("herbivoreTransform", createTexture(manager, HERBIVORE_TRANSFORM_FILE, false));
		textureDict.put("carnivoreTransform", createTexture(manager, CARNIVORE_TRANSFORM_FILE, false));
		textureDict.put("dollText", createTexture(manager, DOLL_SPECIAL_TEXT_FILE, false));
		textureDict.put("herbivoreText", createTexture(manager, HERBIVORE_SPECIAL_TEXT_FILE, false));
		textureDict.put("carnivoreText", createTexture(manager, CARNIVORE_SPECIAL_TEXT_FILE, false));
		textureDict.put("eat", createTexture(manager, EAT_FILE, false));
		textureDict.put("1a", createTexture(manager, TUT_ONE_A, false));
		textureDict.put("1b", createTexture(manager, TUT_ONE_B, false));
		textureDict.put("1c", createTexture(manager, TUT_ONE_C, false));
		textureDict.put("1d", createTexture(manager, TUT_ONE_D, false));
		textureDict.put("2a", createTexture(manager, TUT_TWO_A, false));
		textureDict.put("2b", createTexture(manager, TUT_TWO_B, false));
		textureDict.put("2c", createTexture(manager, TUT_TWO_C, false));
		textureDict.put("2d", createTexture(manager, TUT_TWO_D, false));
		textureDict.put("3a", createTexture(manager, TUT_THREE_A, false));
		textureDict.put("3b", createTexture(manager, TUT_THREE_B, false));
		textureDict.put("4a", createTexture(manager, TUT_FOUR_A, false));
		textureDict.put("4b", createTexture(manager, TUT_FOUR_B, false));
        textureDict.put("6a", createTexture(manager, TUT_SIX_A, false));
        textureDict.put("6b", createTexture(manager, TUT_SIX_B, false));
        textureDict.put("6c", createTexture(manager, TUT_SIX_C, false));
        textureDict.put("6d", createTexture(manager, TUT_SIX_D, false));
        textureDict.put("7a", createTexture(manager, TUT_SEVEN_A, false));
        textureDict.put("7b", createTexture(manager, TUT_SEVEN_B, false));
        textureDict.put("8a", createTexture(manager, TUT_EIGHT_A, false));
        textureDict.put("9a", createTexture(manager, TUT_NINE_A, false));
        textureDict.put("9b", createTexture(manager, TUT_NINE_B, false));

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
		filmStripDict.put("carnivoreEatingFront", createFilmTexture(manager,CARNIVORE_EATING_STRIP_FRONT));
		filmStripDict.put("carnivoreEatingBack", createFilmTexture(manager,CARNIVORE_EATING_STRIP_BACK));
		filmStripDict.put("carnivoreChargeFront", createFilmTexture(manager,CARNIVORE_CHARGE_STRIP_FRONT));
		filmStripDict.put("carnivoreChargeLeft", createFilmTexture(manager,CARNIVORE_CHARGE_STRIP_LEFT));
		filmStripDict.put("carnivoreChargeRight", createFilmTexture(manager,CARNIVORE_CHARGE_STRIP_RIGHT));
		filmStripDict.put("carnivoreChargeBack", createFilmTexture(manager,CARNIVORE_CHARGE_STRIP_BACK));
		filmStripDict.put("carnivoreAttackFront", createFilmTexture(manager,CARNIVORE_ATTACK_STRIP_FRONT));
		filmStripDict.put("carnivoreAttackLeft", createFilmTexture(manager,CARNIVORE_ATTACK_STRIP_LEFT));
		filmStripDict.put("carnivoreAttackRight", createFilmTexture(manager,CARNIVORE_ATTACK_STRIP_RIGHT));
		filmStripDict.put("carnivoreAttackBack", createFilmTexture(manager,CARNIVORE_ATTACK_STRIP_BACK));
		filmStripDict.put("herbivoreLeft", createFilmTexture(manager,HERBIVORE_STRIP_LEFT));
		filmStripDict.put("herbivoreRight", createFilmTexture(manager,HERBIVORE_STRIP_RIGHT));
		filmStripDict.put("herbivoreFront", createFilmTexture(manager,HERBIVORE_STRIP_FRONT));
		filmStripDict.put("herbivoreBack", createFilmTexture(manager,HERBIVORE_STRIP_BACK));
		filmStripDict.put("herbivoreDivingLeft", createFilmTexture(manager,HERBIVORE_DIVING_STRIP_LEFT));
		filmStripDict.put("herbivoreSwimmingLeft", createFilmTexture(manager,HERBIVORE_SWIMMING_STRIP_LEFT));
		filmStripDict.put("herbivoreSwimmingRight", createFilmTexture(manager,HERBIVORE_SWIMMING_STRIP_RIGHT));
		filmStripDict.put("herbivoreSwimmingFront", createFilmTexture(manager,HERBIVORE_SWIMMING_STRIP_FRONT));
		filmStripDict.put("herbivoreSwimmingBack", createFilmTexture(manager,HERBIVORE_SWIMMING_STRIP_BACK));
		filmStripDict.put("herbivoreEnemySwimmingLeft", createFilmTexture(manager,HERBIVORE_ENEMY_SWIMMING_STRIP_LEFT));
		filmStripDict.put("herbivoreEnemySwimmingRight", createFilmTexture(manager,HERBIVORE_ENEMY_SWIMMING_STRIP_RIGHT));
		filmStripDict.put("herbivoreEnemySwimmingFront", createFilmTexture(manager,HERBIVORE_ENEMY_SWIMMING_STRIP_FRONT));
		filmStripDict.put("herbivoreEnemySwimmingBack", createFilmTexture(manager,HERBIVORE_ENEMY_SWIMMING_STRIP_BACK));
		filmStripDict.put("herbivoreGoingInFront", createFilmTexture(manager, HERBIVORE_GOING_IN_STRIP_FRONT));
		filmStripDict.put("herbivoreGoingInBack", createFilmTexture(manager, HERBIVORE_GOING_IN_STRIP_BACK));
		filmStripDict.put("herbivoreGoingInRight", createFilmTexture(manager, HERBIVORE_GOING_IN_STRIP_RIGHT));
		filmStripDict.put("herbivoreGoingInLeft", createFilmTexture(manager, HERBIVORE_GOING_IN_STRIP_LEFT));
		filmStripDict.put("herbivoreGoingOutFront", createFilmTexture(manager, HERBIVORE_GOING_OUT_STRIP_FRONT));
		filmStripDict.put("herbivoreGoingOutBack", createFilmTexture(manager, HERBIVORE_GOING_OUT_STRIP_BACK));
		filmStripDict.put("herbivoreGoingOutRight", createFilmTexture(manager, HERBIVORE_GOING_OUT_STRIP_RIGHT));
		filmStripDict.put("herbivoreGoingOutLeft", createFilmTexture(manager, HERBIVORE_GOING_OUT_STRIP_LEFT));
		filmStripDict.put("herbivoreEatingLeft", createFilmTexture(manager,HERBIVORE_EATING_STRIP_LEFT));
		filmStripDict.put("herbivoreEatingRight", createFilmTexture(manager,HERBIVORE_EATING_STRIP_RIGHT));
		filmStripDict.put("herbivoreEatingFront", createFilmTexture(manager,HERBIVORE_EATING_STRIP_FRONT));
		filmStripDict.put("herbivoreEatingBack", createFilmTexture(manager,HERBIVORE_EATING_STRIP_BACK));
		filmStripDict.put("herbivorePlaceCamoLeft", createFilmTexture(manager,HERBIVORE_PLACECAMO_STRIP_LEFT));
		filmStripDict.put("herbivorePlaceCamoRight", createFilmTexture(manager,HERBIVORE_PLACECAMO_STRIP_RIGHT));
		filmStripDict.put("herbivorePlaceCamoFront", createFilmTexture(manager,HERBIVORE_PLACECAMO_STRIP_FRONT));
		filmStripDict.put("herbivorePlaceCamoBack", createFilmTexture(manager,HERBIVORE_PLACECAMO_STRIP_BACK));
		filmStripDict.put("herbivoreCamoLeft", createFilmTexture(manager,HERBIVORE_CAMO_STRIP_LEFT));
		filmStripDict.put("herbivoreCamoRight", createFilmTexture(manager,HERBIVORE_CAMO_STRIP_RIGHT));
		filmStripDict.put("herbivoreCamoFront", createFilmTexture(manager,HERBIVORE_CAMO_STRIP_FRONT));
		filmStripDict.put("herbivoreCamoBack", createFilmTexture(manager,HERBIVORE_CAMO_STRIP_BACK));
		filmStripDict.put("enemyLeft", createFilmTexture(manager,ENEMY_STRIP_LEFT));
		filmStripDict.put("enemyRight", createFilmTexture(manager,ENEMY_STRIP_RIGHT));
		filmStripDict.put("enemyFront", createFilmTexture(manager,ENEMY_STRIP_FRONT));
		filmStripDict.put("enemyBack", createFilmTexture(manager,ENEMY_STRIP_BACK));
		filmStripDict.put("unkillableEnemyFront", createFilmTexture(manager, UNKILLABLE_ENEMY_STRIP_FRONT));
		filmStripDict.put("unkillableEnemyLeft", createFilmTexture(manager, UNKILLABLE_ENEMY_STRIP_LEFT));
		filmStripDict.put("unkillableEnemyRight", createFilmTexture(manager, UNKILLABLE_ENEMY_STRIP_RIGHT));
		filmStripDict.put("unkillableEnemyBack", createFilmTexture(manager, UNKILLABLE_ENEMY_STRIP_BACK));
		filmStripDict.put("enemyStunnedLeft", createFilmTexture(manager,ENEMY_STUNNED_STRIP_LEFT));
		filmStripDict.put("enemyStunnedRight", createFilmTexture(manager,ENEMY_STUNNED_STRIP_RIGHT));
		filmStripDict.put("enemyStunnedFront", createFilmTexture(manager,ENEMY_STUNNED_STRIP_FRONT));
		filmStripDict.put("enemyStunnedBack", createFilmTexture(manager,ENEMY_STUNNED_STRIP_BACK));
		filmStripDict.put("enemyChargeLeft", createFilmTexture(manager,ENEMY_CHARGE_STRIP_LEFT));
		filmStripDict.put("enemyChargeRight", createFilmTexture(manager,ENEMY_CHARGE_STRIP_RIGHT));
		filmStripDict.put("enemyChargeBack", createFilmTexture(manager,ENEMY_CHARGE_STRIP_BACK));
		filmStripDict.put("enemyChargeFront", createFilmTexture(manager,ENEMY_CHARGE_STRIP_FRONT));
		filmStripDict.put("enemyAttackLeft", createFilmTexture(manager,ENEMY_ATTACK_STRIP_LEFT));
		filmStripDict.put("enemyAttackRight", createFilmTexture(manager,ENEMY_ATTACK_STRIP_RIGHT));
		filmStripDict.put("enemyAttackFront", createFilmTexture(manager,ENEMY_ATTACK_STRIP_FRONT));
		filmStripDict.put("enemyAttackBack", createFilmTexture(manager,ENEMY_ATTACK_STRIP_BACK));
		filmStripDict.put("enemyLeftEating", createFilmTexture(manager,ENEMY_LEFT_EATING_STRIP));
		filmStripDict.put("enemyEatingFront", createFilmTexture(manager, ENEMY_EATING_STRIP_FRONT));
		filmStripDict.put("enemyEatingBack", createFilmTexture(manager, ENEMY_EATING_STRIP_BACK));
		filmStripDict.put("enemyEatingLeft", createFilmTexture(manager, ENEMY_EATING_STRIP_LEFT));
		filmStripDict.put("enemyEatingRight", createFilmTexture(manager, ENEMY_EATING_STRIP_RIGHT));
		filmStripDict.put("edibleWallEating", createFilmTexture(manager, EDIBLE_WALL_EATING_STRIP));
		filmStripDict.put("dollToHerb", createFilmTexture(manager, DOLL_TO_HERB_STRIP));
		filmStripDict.put("dollToCarn", createFilmTexture(manager, DOLL_TO_CARN_STRIP));
		filmStripDict.put("herbToDoll", createFilmTexture(manager, HERB_TO_DOLL_STRIP));
		filmStripDict.put("herbToCarn", createFilmTexture(manager, HERB_TO_CARN_STRIP));
		filmStripDict.put("carnToDoll", createFilmTexture(manager, CARN_TO_DOLL_STRIP));
		filmStripDict.put("carnToHerb", createFilmTexture(manager, CARN_TO_HERB_STRIP));
		filmStripDict.put("vineDrop", createFilmTexture(manager, VINE_DROP_STRIP));
		filmStripDict.put("exclamation", createFilmTexture(manager, EXCLAMATION_STRIP));
		filmStripDict.put("greenDoor", createFilmTexture(manager, GREEN_DOOR_STRIP));
		filmStripDict.put("yellowDoor", createFilmTexture(manager, YELLOW_DOOR_STRIP));
		filmStripDict.put("blueDoor", createFilmTexture(manager, BLUE_DOOR_STRIP));
		filmStripDict.put("redDoor", createFilmTexture(manager, RED_DOOR_STRIP));
		filmStripDict.put("doorFlashing", createFilmTexture(manager, DOOR_FLASHING_STRIP));

		swingInStrip = new FilmStrip(createFilmTexture(manager, SWING_IN_STRIP),1,10,10);
		swingOutStrip = new FilmStrip(createFilmTexture(manager, SWING_OUT_STRIP),1,11,11);

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
			region.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Nearest);
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
		if (value) {
			countdown = EXIT_COUNT;
			SoundController.getInstance().playLevelWin();
		}


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

	public void setCurrentLevel(int l){currentLevel = l;}

	/**
	 * Creates a new game world
	 *
	 * @param gravity	The gravitational force on this Box2d world
	 */
	protected GameController(Vector2 gravity) {
		assets = new Array<String>();
		world = new World(gravity,false);
		level = new Level(world, 0);
		complete = false;
		failed = false;
		timeOut = false;
		active = false;
		countdown = -1;
		cameraBounds = new Rectangle(0,0, 32.0f,18.0f);
		collisionHandler = new CollisionHandler(this);
		hud = new HUDController();

		state = GAME_READY;
	}

	protected GameController(int l) {
		assets = new Array<String>();
		world = new World(new Vector2(0, DEFAULT_GRAVITY),false);
		currentLevel = l;
		level = new Level(world, currentLevel);
		complete = false;
		failed = false;
		timeOut = false;
		active = false;
		countdown = -1;
		cameraBounds = new Rectangle(0,0, 32.0f,18.0f);
		collisionHandler = new CollisionHandler(this);
		hud = new HUDController();

		state = GAME_READY;
	}

	public void nextLevel(){

		if (currentLevel == 19)
			currentLevel = 0;
		else
			currentLevel++;

		reset();
	}

	/**
	 * Resets the status of the game so that we can play again.
	 *
	 * This method disposes of the world and creates a new one.
	 */
	public void reset() {
		totalTime = level.getLevelTime();
		Vector2 gravity = new Vector2(world.getGravity() );

		level.dispose();
		world.dispose();
		controls.clear();
		fireFlyControls.clear();

		world = new World(gravity,false);
		world.setContactListener(this);

		state = GAME_READY;

		if (musicState) {
			SoundController.getInstance().dispose();
			SoundController.getInstance().init();
			SoundController.getInstance().playBackground(Dinosaur.DOLL_FORM);
		}
		else {
			SoundController.getInstance().dispose();
			SoundController.getInstance().init();
			SoundController.getInstance().playBackground(Dinosaur.DOLL_FORM);
		}

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
		level.populate(textureDict, filmStripDict, duggiLight, canvas.getWidth(), canvas.getHeight());
		collisionHandler.setLevel(level);

		// Set the lighting
//		float value = 1.0f - level.getCurrentLevel()/40.0f;
//		if (level.getCurrentLevel() > 5){
//			duggiLight.setActive(true);
//		}
//		rayhandler.setAmbientLight(1.0f, value, value, value);


		// This should be set before init lighting - should be moved when we load in the json
		cameraBounds = new Rectangle(0,0,32,18);

		// Init Enemy AI controllers
		for (int i = 0; i < level.getEnemies().size(); i++) {
			AIController controller = new AIController(i, level.getAvatar(), level.getEnemies(), AIController.FLIP, level,this);
			controls.add(controller);
			level.getEnemy(i).setController(controller);
		}

		// Init FireFlies
		ffLights = new LightSource[level.getFireFlies().size()];
		ffLightDsts = new float[level.getFireFlies().size()];
		ffLightChanges = new float[level.getFireFlies().size()];
		Rectangle levelBounds = new Rectangle(0f,0f,level.getLevelWidth()/80.0f, level.getLevelHeight()/80.0f );
		for (int i = 0; i < level.getFireFlies().size(); i++) {
			fireFlyControls.add(new FireFlyAIController(i, level.getFireFlies(), levelBounds));


			PointSource fireLight = new PointSource(rayhandler, 256, Color.WHITE, 2, 0, 0);
			if(level.getFirefly(i).getTexture() == textureDict.get("fireFlyPurple")){
				fireLight.setColor(Color.PURPLE);
			} else if (level.getFirefly(i).getTexture() == textureDict.get("fireFlyBlue")){
				fireLight.setColor(Color.BLUE);
			} else if (level.getFirefly(i).getTexture() == textureDict.get("fireFlyPink")){
				fireLight.setColor(Color.PINK);
			} else {
				fireLight.setColor(0.96f,0.67f,0.10f,0.15f);
			}
			Color color = fireLight.getColor();
			color.a = 0.15f;
			fireLight.setColor(color);

			fireLight.setXray(true);
			fireLight.setActive(true);
			ffLights[i] = fireLight;
			ffLightDsts[i] = random(2.0f);
			ffLightChanges[i] = random(0.005f, 0.015f);
			fireLight.attachToBody(level.getFirefly(i).getBody(), fireLight.getX(), fireLight.getY(),
					fireLight.getDirection());
		}

		playDoorSound = -1;
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

		// Handle nightmode
//		if (input.didNight()) {
//			if (duggiLight.isActive()) {
//				duggiLight.setActive(false);
//				rayhandler.setAmbientLight(1.0f, 1.0f, 1.0f, 1.0f);
//			} else {
//				duggiLight.setActive(true);
//				rayhandler.setAmbientLight(0.05f, 0.05f, 0.05f, 0.05f);
//			}
//		}

		if (input.isNextLevelPressed()){
			nextLevel();

		}

		// Reset level when colliding with enemy
		if (countdown > 0) {
			countdown--;
			totalTime = level.getLevelTime();;
		} else if (countdown == 0) {
			if (failed || timeOut) {
				state = GAME_OVER;
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

		if (currentLevel == 0) {
			canvas.beginOverlay();
			if ((seconds % 4 >= 2)) {
				canvas.draw(textureDict.get("1a"), 35, 260);
				canvas.draw(textureDict.get("1c"), 1000, 260);
			}
			else {
				canvas.draw(textureDict.get("1b"), 35, 260);
				canvas.draw(textureDict.get("1d"), 1000, 260);
			}
			canvas.end();
		}

		else if (currentLevel == 1) {
			canvas.beginOverlay();
			if ((seconds % 4 >= 2)) {
				canvas.draw(textureDict.get("2a"), 35, 260);
				canvas.draw(textureDict.get("2c"), 1000, 260);
			}
			else {
				canvas.draw(textureDict.get("2b"), 35, 260);
				canvas.draw(textureDict.get("2d"), 1000, 260);
			}
			canvas.end();
		}

		else if (currentLevel == 2) {
			canvas.beginOverlay();
			if ((seconds % 4 >= 2)) {
				canvas.draw(textureDict.get("3a"), 35, 260);
			}
			else {
				canvas.draw(textureDict.get("3b"), 35, 260);
			}
			canvas.end();
		}

		else if (currentLevel == 3) {
			canvas.beginOverlay();
			if ((seconds % 4 >= 2)) {
				canvas.draw(textureDict.get("4a"), 35, 260);
			}
			else {
				canvas.draw(textureDict.get("4b"), 35, 260);
			}
			canvas.end();
		}
        else if (currentLevel == 5) {
            canvas.beginOverlay();
            if ((seconds % 4 >= 2)) {
                canvas.draw(textureDict.get("6a"), 35, 260);
                canvas.draw(textureDict.get("6c"), 1000, 260);
            }
            else {
                canvas.draw(textureDict.get("6b"), 35, 260);
                canvas.draw(textureDict.get("6d"), 1000, 260);
            }
            canvas.end();
        }
        else if (currentLevel == 6) {
            canvas.beginOverlay();
            if ((seconds % 4 >= 2)) {
                canvas.draw(textureDict.get("7a"), 35, 260);
            }
            else {
                canvas.draw(textureDict.get("7b"), 35, 260);
            }
            canvas.end();
        }
        else if (currentLevel == 7) {
            canvas.beginOverlay();
            canvas.draw(textureDict.get("8a"), 35, 260);
            canvas.end();
        }
        else if (currentLevel == 8) {
            canvas.beginOverlay();
            if ((seconds % 4 >= 2)) {
                canvas.draw(textureDict.get("9a"), 35, 260);
            }
            else {
                canvas.draw(textureDict.get("9b"), 35, 260);
            }
            canvas.end();
        }

        if (state == GAME_LEVEL_END) {
			swingOutStrip.setFrame((int)swingAnimeFrame);
			if (swingOutStrip != null) {
				canvas.beginOverlay();
				canvas.draw(swingOutStrip, Color.WHITE,0,0,0,43,0,1,1);
				canvas.end();
			}
		}


		if (state == GAME_OVER) {
			displayFont.setColor(Color.YELLOW);
			if (complete && !failed) {
				displayFont.setColor(Color.RED);
				canvas.beginOverlay();
				canvas.draw(textureDict.get("victory"), Color.WHITE, canvas.getWidth() / 2 - textureDict.get("victory").getRegionWidth() * .75f / 2,
						canvas.getHeight() / 2 - textureDict.get("victory").getRegionHeight() * .75f / 2,
						textureDict.get("victory").getRegionWidth() * .75f, textureDict.get("victory").getRegionHeight() * .75f);
				//canvas.drawTextCentered("EATEN ALIVE!", displayFont, 0.0f);
				if (totalTime >= 2*levelTime/3) {
//					System.out.println("THREE STAR!");
					canvas.draw(textureDict.get("victory"), Color.WHITE, canvas.getWidth() / 4 - textureDict.get("victory").getRegionWidth() * .75f / 4,
							canvas.getHeight() / 4 - textureDict.get("victory").getRegionHeight() * .75f / 4,
							textureDict.get("victory").getRegionWidth() * .75f, textureDict.get("victory").getRegionHeight() * .75f);
					canvas.draw(textureDict.get("victory"), Color.WHITE, 3*canvas.getWidth() / 4 - textureDict.get("victory").getRegionWidth() *3 * .75f / 4,
							canvas.getHeight() / 4 - textureDict.get("victory").getRegionHeight() * 3 * .75f / 4,
							textureDict.get("victory").getRegionWidth() * .75f, textureDict.get("victory").getRegionHeight() * .75f);
				}
				else if (totalTime >= levelTime/3) {
//					System.out.println("TWO STAR!");
					canvas.draw(textureDict.get("victory"), Color.WHITE, canvas.getWidth() / 4 - textureDict.get("victory").getRegionWidth() * .75f / 4,
							canvas.getHeight() / 4 - textureDict.get("victory").getRegionHeight() * .75f / 4,
							textureDict.get("victory").getRegionWidth() * .75f, textureDict.get("victory").getRegionHeight() * .75f);
				}
//				else {
//					//one star here!
//				}

				canvas.end();

			}
			else if (failed && !complete) {
				displayFont.setColor(Color.RED);
				canvas.beginOverlay();
				canvas.draw(textureDict.get("gameover"), Color.WHITE, canvas.getWidth() / 2 - textureDict.get("gameover").getRegionWidth() * .75f / 2,
						canvas.getHeight() / 2 - textureDict.get("victory").getRegionHeight() * .75f / 2,
						textureDict.get("gameover").getRegionWidth() * .75f, textureDict.get("gameover").getRegionHeight() * .75f);
				canvas.end();
			}
			else if (timeOut) {
				displayFont.setColor(Color.RED);
				canvas.beginOverlay();
				canvas.draw(textureDict.get("gameover"), Color.WHITE, canvas.getWidth() / 2 - textureDict.get("gameover").getRegionWidth() * .75f / 2,
						canvas.getHeight() / 2 - textureDict.get("gameover").getRegionHeight() * .75f / 2,
						textureDict.get("gameover").getRegionWidth() * .75f, textureDict.get("gameover").getRegionHeight() * .75f);
				canvas.end();
			}
		}

		if (state == GAME_PAUSED) {
				displayFont.setColor(Color.YELLOW);
				if (menuNum == 0) {
					canvas.beginOverlay();
					canvas.draw(textureDict.get("grayOut"), -9, 0);
					canvas.draw(textureDict.get("pauseMenu"), 297, 110);
					if (musicState) {
						if (InputHandler.getInstance().didHover() == 0) {
							canvas.draw(textureDict.get("musicOn"), hoverColor,510, 451,84,52);
						}
						else {
							canvas.draw(textureDict.get("musicOn"), 510, 451); // music button
						}
					}
					else {
						if (InputHandler.getInstance().didHover() == 0) {
							canvas.draw(textureDict.get("musicOff"), hoverColor,510, 451,84,52);
						}
						else {
							canvas.draw(textureDict.get("musicOff"), 510, 451); // music button
						}
					}
					if (soundState) {
						if (InputHandler.getInstance().didHover() == 1) {
							canvas.draw(textureDict.get("soundOn"), hoverColor,656, 451,84,52);
						}
						else {
							canvas.draw(textureDict.get("soundOn"), 656, 451); // sound button
						}
					}
					else {
						if (InputHandler.getInstance().didHover() == 1) {
							canvas.draw(textureDict.get("soundOff"), hoverColor,656, 451,84,52);
						}
						else {
							canvas.draw(textureDict.get("soundOff"), 656, 451); // sound button
						}
					}
					if (InputHandler.getInstance().didHover() == 2) {
						canvas.draw(textureDict.get("menuText"),hoverColor,525,385,200,36);
					}
					else {
						canvas.draw(textureDict.get("menuText"), 525, 385); // menu text
					}
					if (InputHandler.getInstance().didHover() == 3) {
						canvas.draw(textureDict.get("helpText"), hoverColor,580, 320,90,36);
					}
					else {
						canvas.draw(textureDict.get("helpText"), 580, 320); // help text
					}
					if (InputHandler.getInstance().didHover() == 4) {
						canvas.draw(textureDict.get("restartText"), hoverColor,546, 253,157,35);
					}
					else {
						canvas.draw(textureDict.get("restartText"), 546, 253); // restart text
					}
					if (InputHandler.getInstance().didHover() == 5) {
						canvas.draw(textureDict.get("resumeText"),hoverColor,553, 187,145,35);
					}
					else {
						canvas.draw(textureDict.get("resumeText"),553, 187); // resume text
					}
					canvas.end();

					if (InputHandler.getInstance().didReturn()) {
						menuNum = 1;
						resetCamera();
						listener.exitScreen(this, 0);
						menuNum = 0;
					}
					else if (InputHandler.getInstance().didHelp()) {
						menuNum = 2;
					}
					else if (InputHandler.getInstance().didRestart()) {
						menuNum = 0;
						reset();

					}
					else if (InputHandler.getInstance().didResume()) {
						menuNum = 0;
						state = GAME_RUNNING;
					}
					else if (InputHandler.getInstance().didMusic()) {
//						menuNum = 5;
						musicState = !musicState;
						int form = level.getAvatar().getForm();
						SoundController.getInstance().changeBackground(form);

					}
					else if (InputHandler.getInstance().didSound()) {
//						menuNum = 6;
						soundState = !soundState;
					}
				}
				if (menuNum == 2) {
					canvas.beginOverlay();
					canvas.draw(textureDict.get("helpMenu"), 127, 100);
					canvas.draw(textureDict.get("exit"), 1100, 524);
					canvas.draw(textureDict.get("dollForm"), 264, 173);
					canvas.draw(textureDict.get("herbivoreForm"), 557, 173);
					canvas.draw(textureDict.get("carnivoreForm"), 889, 173);
					canvas.draw(textureDict.get("dollIcon"), 213, 223);
					canvas.draw(textureDict.get("herbivoreIcon"), 595, 226);
					canvas.draw(textureDict.get("carnivoreIcon"), 909, 224);
					canvas.draw(textureDict.get("selectText"), 399, 514);
					canvas.end();
					if (InputHandler.getInstance().didExitButton()) {
						menuNum = 0;
					}
					else if (InputHandler.getInstance().didDollHelp()) {
						menuNum = 7;
					}
					else if (InputHandler.getInstance().didHerbivoreHelp()) {
						menuNum = 8;
					}
				}
				if (menuNum == 7) {
					canvas.beginOverlay();
					canvas.draw(textureDict.get("helpMenu"), 127, 100);
					canvas.draw(textureDict.get("exit"), 1100, 524);
					canvas.draw(textureDict.get("dollIcon"), 215, 222);
					canvas.draw(textureDict.get("dollHeader"),496, 532);
					canvas.draw(textureDict.get("pressText"), 480, 424);
					canvas.draw(textureDict.get("pressText"), 480, 297);
					canvas.draw(textureDict.get("holdText"), 480, 172);
					canvas.draw(textureDict.get("oneKey"), 611, 379);
					canvas.draw(textureDict.get("spaceKey"), 588, 267);
					canvas.draw(textureDict.get("spaceKey"), 577, 146);
					canvas.draw(textureDict.get("dollTransform"), 723, 402);
					canvas.draw(textureDict.get("eat"), 823, 298);
					canvas.draw(textureDict.get("dollResource"), 951, 280);
					canvas.draw(textureDict.get("dollText"), 796, 150);
					canvas.draw(textureDict.get("dollSpecial"), 992, 141);
					canvas.end();

					if (InputHandler.getInstance().didExitButton()) {
						menuNum = 0;
					}
				}
				if (menuNum == 8) {
					canvas.beginOverlay();
					canvas.draw(textureDict.get("helpMenu"), 127, 100);
					canvas.draw(textureDict.get("exit"), 1100, 524);
					canvas.draw(textureDict.get("herbivoreIcon"), 215, 222);
					canvas.draw(textureDict.get("herbivoreHeader"),496, 532);
					canvas.draw(textureDict.get("pressText"), 480, 424);
					canvas.draw(textureDict.get("pressText"), 480, 297);
					canvas.draw(textureDict.get("holdText"), 480, 172);
					canvas.draw(textureDict.get("twoKey"), 611, 379);
					canvas.draw(textureDict.get("spaceKey"), 588, 267);
					canvas.draw(textureDict.get("spaceKey"), 577, 146);
					canvas.draw(textureDict.get("herbivoreTransform"), 723, 402);
					canvas.draw(textureDict.get("eat"), 823, 298);
					canvas.draw(textureDict.get("herbivoreResource"), 964, 254);
					canvas.draw(textureDict.get("herbivoreText"), 796, 150);
					canvas.draw(textureDict.get("herbivoreSpecial"), 992, 141);
					canvas.end();

					if (InputHandler.getInstance().didExitButton()) {
						menuNum = 0;
				}
			}
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
		totalTime = level.getLevelTime();
		levelTime = level.getLevelTime();
		state = GAME_RUNNING;
	}

	private void updateRunning(float dt) {
		if (failed && !complete) {
			state = GAME_OVER;
		}
		else if (complete && !failed) {
			state = GAME_LEVEL_END;
			swingAnimeFrame = 0;
		}
		else {
			if (level.getSwitches().size() == 0) {
				level.getAvatar().setCanExit(true);
				level.getDoor(0).setLowered(true);
				level.getDoor(0).setTexture(textureDict.get("goalOpenTile"));

			}
			else {
				if (level.getClone() != null){
					for (int i = 0; i < level.getSwitches().size(); i++){
						if ((Math.abs(level.getClone().getX() - level.getSwitch(i).getX()) < 1f &&
								Math.abs(level.getClone().getY() - 0.75f - level.getSwitch(i).getY()) < 1f)||
								(Math.abs(level.getAvatar().getX() - level.getSwitch(i).getX()) < 1.5f &&
										Math.abs(level.getAvatar().getY() - 0.75f - level.getSwitch(i).getY()) < 1f)){
							if (i == 0) {
								level.getAvatar().setCanExit(true);
							}

							level.getDoor(i).setLowered(true);
							if (i == 0) {
								level.getDoor(i).setTexture(textureDict.get("goalOpenTile"));
                                level.getSwitch(0).setTexture(textureDict.get("switchoff"));
							}
							else if (i == 1) {
								level.getDoor(i).setTexture(textureDict.get("doorOpenTileOne"));
                                level.getSwitch(1).setTexture(textureDict.get("switchoneoff"));
							}
							else if (i == 2) {
								level.getDoor(i).setTexture(textureDict.get("doorOpenTileTwo"));
                                level.getSwitch(2).setTexture(textureDict.get("switchtwooff"));
							}
							else if (i == 3) {
								level.getDoor(i).setTexture(textureDict.get("doorOpenTileThree"));
                                level.getSwitch(3).setTexture(textureDict.get("switchthreeoff"));
							}
						} else if (!doorHasEnemyOnTop(level.getDoor(i)) && !doorHasPlayerOnTop(level.getDoor(i))) {

							if (i == 0){
								level.getAvatar().setCanExit(false);
							}
							level.getDoor(i).setLowered(false);
							if (i == 0) {
								level.getDoor(i).setTexture(textureDict.get("goalClosedTile"));
                                level.getSwitch(0).setTexture(textureDict.get("switch"));
							}
							else if (i == 1) {
								level.getDoor(i).setTexture(textureDict.get("doorClosedTileOne"));
                                level.getSwitch(1).setTexture(textureDict.get("switchone"));
							}
							else if (i == 2) {
								level.getDoor(i).setTexture(textureDict.get("doorClosedTileTwo"));
                                level.getSwitch(2).setTexture(textureDict.get("switchtwo"));
							}
							else if (i == 3) {
								level.getDoor(i).setTexture(textureDict.get("doorClosedTileThree"));
                                level.getSwitch(3).setTexture(textureDict.get("switchthree"));
							}

						}
					}
				} else {
					for (int i = 0; i < level.getSwitches().size(); i++) {
						if (Math.abs(level.getAvatar().getX() - level.getSwitch(i).getX()) < 1.5f &&
								Math.abs(level.getAvatar().getY() - 0.75f - level.getSwitch(i).getY()) < 1f) {
							if (i == 0) {
								level.getAvatar().setCanExit(true);
							}
							level.getDoor(i).setLowered(true);
							if (i == 0) {
								level.getDoor(i).setTexture(textureDict.get("goalOpenTile"));
								level.getSwitch(0).setTexture(textureDict.get("switchoff"));
							} else if (i == 1) {
								level.getDoor(i).setTexture(textureDict.get("doorOpenTileOne"));
								level.getSwitch(1).setTexture(textureDict.get("switchoneoff"));
							} else if (i == 2) {
								level.getDoor(i).setTexture(textureDict.get("doorOpenTileTwo"));
								level.getSwitch(2).setTexture(textureDict.get("switchtwooff"));
							} else if (i == 3) {
								level.getDoor(i).setTexture(textureDict.get("doorOpenTileThree"));
								level.getSwitch(3).setTexture(textureDict.get("switchthreeoff"));
							}
						} else if (!doorHasEnemyOnTop(level.getDoor(i)) && !doorHasPlayerOnTop(level.getDoor(i))) {

							if (i == 0) {
								level.getAvatar().setCanExit(false);
							}
							level.getDoor(i).setLowered(false);
							if (i == 0) {
								level.getDoor(i).setTexture(textureDict.get("goalClosedTile"));
								level.getSwitch(0).setTexture(textureDict.get("switch"));
							} else if (i == 1) {
								level.getDoor(i).setTexture(textureDict.get("doorClosedTileOne"));
								level.getSwitch(1).setTexture(textureDict.get("switchone"));
							} else if (i == 2) {
								level.getDoor(i).setTexture(textureDict.get("doorClosedTileTwo"));
								level.getSwitch(2).setTexture(textureDict.get("switchtwo"));
							} else if (i == 3) {
								level.getDoor(i).setTexture(textureDict.get("doorClosedTileThree"));
								level.getSwitch(3).setTexture(textureDict.get("switchthree"));
							}

						}
					}
				}
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
				raycamera.position.x = 2*(level.getLevelWidth()/80.0f) - cameraBounds.width / 2;
			} else {
				canvas.getCamera().position.x = (avatar.getX() / cameraBounds.width) * canvas.getCamera().viewportWidth;
				raycamera.position.x = avatar.getX();
			}

			if ((avatar.getY() / cameraBounds.height) * canvas.getCamera().viewportHeight < halfHeight) {
				canvas.getCamera().position.y = halfHeight;
				raycamera.position.y = cameraBounds.height / 2;
			} else if ((avatar.getY() / cameraBounds.height) * canvas.getCamera().viewportHeight > level.getLevelHeight() - halfHeight) {
				canvas.getCamera().position.y = level.getLevelHeight() - halfHeight;
				raycamera.position.y = 2*(level.getLevelHeight()/80f) - cameraBounds.height / 2;
			} else {
				canvas.getCamera().position.y = (avatar.getY() / cameraBounds.height) * canvas.getCamera().viewportHeight;
				raycamera.position.y = avatar.getY();
			}

			// Only shake when required. Thank you smilne for the code.
			// Code used from https://www.netprogs.com/libgdx-screen-shaking/
			if (elapsed < duration) {
				// Calculate the amount of shake based on how long it has been shaking already
				float currentPower = intensity * canvas.getCamera().zoom * ((duration - elapsed) / duration);
				float x = (random.nextFloat() - 0.5f) * currentPower;
				float y = (random.nextFloat() - 0.5f) * currentPower;
				canvas.getCamera().translate(-x, -y);

				// Increase the elapsed time by the delta provided.
				elapsed += dt;
			}

			// Handle camera shaking for when duggi carn collides
			if (avatar.getForm() == Dinosaur.CARNIVORE_FORM){
				if (((Carnivore)avatar).getShakeCamera()){
					if (level.objectInFrontOfAvatar() != null){
						if (level.objectInFrontOfAvatar().getType() == WALL){
							shake(500,500,5f);
						}
					}
					((Carnivore)avatar).setShakeCamera(false);
				}
			}

			canvas.getCamera().update();
			raycamera.update();
			rayhandler.setCombinedMatrix(raycamera);

			// Process FireFly updates
			for (int i = 0; i < fireFlyControls.size(); i++) {
				if (level.getAvatar().canExit()) {
					fireFlyControls.get(i).setGoal(level.getDoor(0).getPosition());
					fireFlyControls.get(i).getMoveToGoal(level.getDoor(0).getPosition());
				} else {
					if (fireFlyControls.get(i).getGoal().equals(level.getDoor(0).getPosition())){
						fireFlyControls.get(i).resetGoal();
					}
					fireFlyControls.get(i).getMoveAlongPath();
				}
			}

			// Process Door Sound
			if (level.getAvatar().canExit() && playDoorSound < 0){
				playDoorSound = 0;
				playDoorSound++;
				SoundController.getInstance().playDoorOpen();
			} else if (!level.getAvatar().canExit() && playDoorSound > 0){
				playDoorSound = 0;
				playDoorSound--;
				SoundController.getInstance().playDoorOpen();
			}

			for (int i = 0; i < ffLights.length; i++) {
				if (ffLightDsts[i] > 2.5f) {
					ffLightChanges[i] *= -1;
					ffLightDsts[i] = 2.5f;
				} else if (ffLightDsts[i] < 1.0f) {
					ffLightChanges[i] *= -1;
					ffLightDsts[i] = 1.0f;
				}

				ffLightDsts[i] += ffLightChanges[i];
				ffLights[i].setDistance(ffLightDsts[i]);
			}


			// Process enemy updates
			for (int i = 0; i < level.getEnemies().size(); i++)
				controls.get(i).step();

			if (avatar.getForm() == Dinosaur.HERBIVORE_FORM){
				avatar.setIsSwimming(true);
				int frames = 7;
				if (avatar.getDirection() == Dinosaur.UP){
					frames = 8;
				}

				if (animationFrameForGoingIn(frames, avatar.getDirection()) != -1 && !isOnRiverTile() &&
						avatar.getDirection() == Dinosaur.DOWN && !avatar.getEating()){
					avatar.setTextureSet(filmStripDict.get("herbivoreGoingInLeft"), 7,
							filmStripDict.get("herbivoreGoingInRight"), 7,
							filmStripDict.get("herbivoreGoingInBack"), 8,
							filmStripDict.get("herbivoreGoingInFront"), 7);

					avatar.forceFrame(animationFrameForGoingIn(frames, avatar.getDirection()));
					avatar.setOffsetSwim(((float)animationFrameForGoingIn(frames, avatar.getDirection())/(float)frames)*
						avatar.getmaxOffsetSwim());

				}
				else if (animationFrameForNotCenterTileGoingIn() != -1 && avatar.getDirection() != Dinosaur.DOWN
						&& !avatar.getEating()){
					avatar.setTextureSet(filmStripDict.get("herbivoreGoingInLeft"), 7,
							filmStripDict.get("herbivoreGoingInRight"), 7,
							filmStripDict.get("herbivoreGoingInBack"), 8,
							filmStripDict.get("herbivoreGoingInFront"), 7);

					avatar.forceFrame(animationFrameForNotCenterTileGoingIn());
					avatar.setOffsetSwim(((float)animationFrameForNotCenterTileGoingIn()/(float)frames)*
							avatar.getmaxOffsetSwim());
				}
				else if (animationFrameForNotCenterTileGoingOut() != -1 && !avatar.getEating()){
					avatar.setTextureSet(filmStripDict.get("herbivoreGoingOutLeft"), 7,
							filmStripDict.get("herbivoreGoingOutRight"), 7,
							filmStripDict.get("herbivoreGoingOutBack"), 8,
							filmStripDict.get("herbivoreGoingOutFront"), 7);

					avatar.forceFrame(animationFrameForNotCenterTileGoingOut());
					avatar.setOffsetSwim(avatar.getmaxOffsetSwim() - (((float)animationFrameForNotCenterTileGoingOut()/(float)frames)*
							avatar.getmaxOffsetSwim()));
				}
				else if (isOnRiverTile()){
					avatar.setCanBeSeen(true);
					avatar.setOffsetSwim(avatar.getmaxOffsetSwim());
					avatar.setIsSwimming(true);

					avatar.setTextureSet(filmStripDict.get("herbivoreSwimmingLeft"), 7,
							filmStripDict.get("herbivoreSwimmingRight"), 7,
							filmStripDict.get("herbivoreSwimmingBack"), 8,
							filmStripDict.get("herbivoreSwimmingFront"), 7);

				}
				else if (avatar.getCanBeSeen()){
					avatar.setIsSwimming(false);
					avatar.setTextureSet(filmStripDict.get("herbivoreLeft"), 7,
							filmStripDict.get("herbivoreRight"), 7,
							filmStripDict.get("herbivoreBack"), 8,
							filmStripDict.get("herbivoreFront"), 8);
				}

			}

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

			if (InputHandler.getInstance().didTransform() && !isOnRiverTile()) {
//				if (avatar.canTransform()) {
				if (true) {
					if (InputHandler.getInstance().didTransformDoll() &&
							avatar.getForm() != Dinosaur.DOLL_FORM) {

						if (avatar.getForm() == Dinosaur.HERBIVORE_FORM){
							avatar.setTransformTextureSet(filmStripDict.get("herbToDoll"), 11);
						} else{
							avatar.setTransformTextureSet(filmStripDict.get("carnToDoll"), 11);
						}
						transform = true;
						avatar.setTransform(true);
						avatar.setTransformToForm(0);

						SoundController.getInstance().changeBackground(Dinosaur.DOLL_FORM);
						SoundController.getInstance().playTransform();

					} else if (InputHandler.getInstance().didTransformHerbi() &&
							avatar.getForm() != Dinosaur.HERBIVORE_FORM) {

						if (avatar.getForm() == Dinosaur.DOLL_FORM){
							avatar.setTransformTextureSet(filmStripDict.get("dollToHerb"), 11);
						} else{
							avatar.setTransformTextureSet(filmStripDict.get("carnToHerb"), 11);
						}

						transform = true;
						avatar.setTransform(true);
						avatar.setTransformToForm(1);

						SoundController.getInstance().changeBackground(Dinosaur.HERBIVORE_FORM);
						SoundController.getInstance().playTransform();

					} else if (InputHandler.getInstance().didTransformCarni() &&
							avatar.getForm() != Dinosaur.CARNIVORE_FORM) {
						if (avatar.getForm() == Dinosaur.DOLL_FORM){
							avatar.setTransformTextureSet(filmStripDict.get("dollToCarn"), 11);
						} else{
							avatar.setTransformTextureSet(filmStripDict.get("herbToCarn"), 11);
						}

						transform = true;
						avatar.setTransform(true);
						avatar.setTransformToForm(2);

						SoundController.getInstance().changeBackground(Dinosaur.CARNIVORE_FORM);
						SoundController.getInstance().playTransform();
					}
				}
			}

			if (transform && !avatar.getTransform()){
				transform = false;
				avatar.forceFrame(0);
				if (avatar.getTransformNumber() == 0){
					avatar = avatar.transformToDoll();
					avatar.setCanBeSeen(true);

					//Change the filter data
					Filter filter = avatar.getFilterData();
					filter.categoryBits = Dinosaur.dollCatBits;
					filter.maskBits = Dinosaur.enemyCatBits|Dinosaur.riverCatBits|Dinosaur.wallCatBits|Dinosaur.goalCatBits;
					avatar.setFilterData(filter);
					avatar.setTextureSet(filmStripDict.get("dollLeft"), 8,
							filmStripDict.get("dollRight"), 8,
							filmStripDict.get("dollBack"), 8,
							filmStripDict.get("dollFront"), 8);
					avatar.setEatingTextureSet(filmStripDict.get("dollEatingLeft"), 7,
							filmStripDict.get("dollEatingRight"), 7,
							filmStripDict.get("dollEatingBack"), 6,
							filmStripDict.get("dollEatingFront"), 7);
					avatar.setActionTextureSet(filmStripDict.get("dollCloningFront"), 12,
							filmStripDict.get("dollCloningFront"), 12,
							filmStripDict.get("dollCloningFront"), 12,
							filmStripDict.get("dollCloningFront"), 12);


					level.setAvatar(avatar);

				} else if (avatar.getTransformNumber() == 1){
					avatar = avatar.transformToHerbivore();
					avatar.setCanBeSeen(true);

					//Change the filter data
					Filter filter = avatar.getFilterData();
					filter.categoryBits = Dinosaur.herbCatBits;
					filter.maskBits = Dinosaur.enemyCatBits|Dinosaur.wallCatBits|Dinosaur.enemyHerbCatBits|Dinosaur.goalCatBits;
					avatar.setFilterData(filter);

					avatar.setTextureSet(filmStripDict.get("herbivoreLeft"), 7,
							filmStripDict.get("herbivoreRight"), 7,
							filmStripDict.get("herbivoreBack"), 8,
							filmStripDict.get("herbivoreFront"), 8);

					avatar.setEatingTextureSet(filmStripDict.get("herbivoreEatingLeft"), 10,
							filmStripDict.get("herbivoreEatingRight"), 10,
							filmStripDict.get("herbivoreEatingBack"), 10,
							filmStripDict.get("herbivoreEatingFront"), 10);
					avatar.setActionLoadingTextureSet(filmStripDict.get("herbivorePlaceCamoLeft"), 12, 0,
							filmStripDict.get("herbivorePlaceCamoRight"), 12, 0,
							filmStripDict.get("herbivorePlaceCamoBack"), 10, 0,
							filmStripDict.get("herbivorePlaceCamoFront"),  12, 0);


					level.setAvatar(avatar);
				} else {
					avatar = avatar.transformToCarnivore();
					avatar.setCanBeSeen(true);

					Filter filter = avatar.getFilterData();
					filter.categoryBits = Dinosaur.carnCatBits;
					filter.maskBits = Dinosaur.enemyCatBits|Dinosaur.riverCatBits|Dinosaur.wallCatBits|Dinosaur.goalCatBits;
					avatar.setFilterData(filter);
					avatar.setTextureSet(filmStripDict.get("carnivoreLeft"), 10,
							filmStripDict.get("carnivoreRight"), 10,
							filmStripDict.get("carnivoreBack"), 8,
							filmStripDict.get("carnivoreFront"), 10);
					avatar.setEatingTextureSet(filmStripDict.get("carnivoreEatingLeft"), 8,
							filmStripDict.get("carnivoreEatingRight"), 8,
							filmStripDict.get("carnivoreEatingBack"), 9,
							filmStripDict.get("carnivoreEatingFront"), 12);
					avatar.setActionLoadingTextureSet(filmStripDict.get("carnivoreChargeLeft"), 15, 12,
							filmStripDict.get("carnivoreChargeRight"), 15, 12,
							filmStripDict.get("carnivoreChargeBack"), 8, 4,
							filmStripDict.get("carnivoreChargeFront"),  11, 5);
					avatar.setActionTextureSet(filmStripDict.get("carnivoreAttackLeft"), 9,
							filmStripDict.get("carnivoreAttackRight"), 9,
							filmStripDict.get("carnivoreAttackBack"), 6,
							filmStripDict.get("carnivoreAttackFront"), 10);

					level.setAvatar(avatar);
				}
			}

			// Sound for transformation
			if(InputHandler.getInstance().didTransform()){
				if (InputHandler.getInstance().didTransformDoll() &&
						avatar.getForm() != Dinosaur.DOLL_FORM && level.getAvatar().getResources() < 3){
					SoundController.getInstance().playFull();
					hud.flashResourceBar();
					shake(500f,500f,5f);


				} else if (InputHandler.getInstance().didTransformHerbi() &&
						avatar.getForm() != Dinosaur.HERBIVORE_FORM && level.getAvatar().getResources() < 3){
					SoundController.getInstance().playFull();
					hud.flashResourceBar();
					shake(500f,500f,5f);
				}
				else if (InputHandler.getInstance().didTransformCarni() &&
						avatar.getForm() != Dinosaur.CARNIVORE_FORM && level.getAvatar().getResources() < 3){
					SoundController.getInstance().playFull();
					hud.flashResourceBar();
					shake(500f,500f,5f);
				}

			}

			if (avatar.getForm() == Dinosaur.HERBIVORE_FORM && isOnRiverTile()){
				enterRiverTileFirst(new Vector2(level.getAvatarGridX(), level.getAvatarGridY()));
			}

			if (level.getClone() != null && (removeClone || level.getClone().getRemoved())) {
				removeClone = false;
				level.removeClone();
			}

			if (InputHandler.getInstance().didAction()) {
				if (avatar.getForm() == Dinosaur.DOLL_FORM) {
					GameObject cotton = level.getGridObject(level.getAvatarGridX(), level.getAvatarGridY());
					if (cotton != null && cotton.getType() == COTTON) {
						if (avatar.getResources() < 3){
							SoundController.getInstance().playCottonPickup();
							level.removeObject(cotton);
							avatar.incrementResources();
						} else {
							SoundController.getInstance().playFull();
							hud.flashResourceBar();
							shake(500f,500f,5f);
						}

					} else {
						if (!avatar.inActionCycle()) {
							SoundController.getInstance().playChargeSound();
							avatar.loadAction();
						}
					}
				} else if (avatar.getForm() == Dinosaur.HERBIVORE_FORM) {
					GameObject tmp = level.objectInFrontOfAvatar();
					float dist = level.getStraightDist(avatar.getDirection(), tmp, avatar);
					if (tmp != null && tmp.getType() == EDIBLEWALL && dist < 6.5) {
						if (!avatar.inActionCycle()){
							SoundController.getInstance().playChargeSound();
							avatar.loadAction();
						}
//						if (avatar.getActionLoadValue() == 0){
//							SoundController.getInstance().playEat();
//							((EdibleObject) tmp).beginEating();
//							avatar.incrementResources();
//						} else if (!avatar.inActionCycle()){
//							avatar.loadAction();
//						}
					}
				} else if (avatar.getForm() == Dinosaur.CARNIVORE_FORM) {
					boolean ate = false;

					for (int i = 0; i < level.getEnemies().size(); i++) {
						Enemy tmp = level.getEnemy(i);
						if (tmp.getStunned() && level.isInFrontOfAvatar(tmp)
								&& tmp.getPosition().dst2(avatar.getPosition()) < 5.5) {
							if (avatar.getResources() < 3){
								SoundController.getInstance().playCrunch();
								if (!tmp.getEatInProgress()){
									avatar.incrementResources();
								}
								tmp.beginEating();
								ate = true;
								break;
							} else {
								SoundController.getInstance().playFull();
								hud.flashResourceBar();
								shake(500f,500f,5f);
								break;
							}

						}
					}

					if (!ate && !avatar.inActionCycle()) {
						SoundController.getInstance().playFootDrag();
						avatar.loadAction();
					}

				}
			}

			if (level.getAvatar().getActionComplete()) {
				if (level.getAvatar().getForm() == Dinosaur.DOLL_FORM) {
					if (level.getClone() != null) {
						level.removeClone();
					}
					level.getAvatar().useAction();
					SoundController.getInstance().playPlop();
					level.placeClone();
				} else if (level.getAvatar().getForm() == Dinosaur.HERBIVORE_FORM){
					level.getAvatar().useAction();
					GameObject tmp = level.objectInFrontOfAvatar();
					if (tmp != null && tmp.getType() == EDIBLEWALL && tmp.getPosition().dst2(avatar.getPosition()) < 7f) {
						SoundController.getInstance().playMunch();
						avatar.setCanBeSeen(false);
						avatar.setTextureSet(filmStripDict.get("herbivoreCamoLeft"), 7,
								filmStripDict.get("herbivoreCamoRight"), 7,
								filmStripDict.get("herbivoreCamoBack"), 8,
								filmStripDict.get("herbivoreCamoFront"), 8);
					}
				}
			}

			// Charge Sounds
			if (avatar.getActionLoadValue() > 0.25f){
				SoundController.getInstance().playChargeSound();
			}
			if (avatar.getActionReady()){
				SoundController.getInstance().playChargeSound();

			}

			if (InputHandler.getInstance().didActionRelease()) {
				if (avatar.getForm() == Dinosaur.HERBIVORE_FORM && avatar.getActionLoadValue() < 0.25f && avatar.getActionLoadValue()>0) {
					GameObject tmp = level.objectInFrontOfAvatar();

					float dist = level.getStraightDist(avatar.getDirection(), tmp, avatar);
					if (tmp != null && tmp.getType() == EDIBLEWALL && dist < 6.5) {
						if (avatar.getResources() < 3){
							SoundController.getInstance().playMunch();
							if (!((EdibleObject) tmp).getEatInProgress()){
								avatar.incrementResources();
							}
							((EdibleObject) tmp).beginEating();
						} else {
							SoundController.getInstance().playFull();
							hud.flashResourceBar();
							shake(500f,500f,5f);
						}
					}
				}

				if (avatar.actionReady()) {
					avatar.beginAction();
					if (avatar.getForm() == Dinosaur.CARNIVORE_FORM &&
							((Carnivore) avatar).getAdjacentBoulder() != null &&
							level.isInFrontOfAvatar(((Carnivore) avatar).getAdjacentBoulder())) {
						level.pushBoulder(avatar, ((Carnivore) avatar).getAdjacentBoulder());
					}
				} else {
					avatar.stopAction();
				}
			}

			if (InputHandler.getInstance().didPause()) {
				state = GAME_PAUSED;
				return;
			}

			avatar.applyForce();

			hud.update(avatar.getResources(), avatar.getForm(), level.getClone(), totalTime);
		}
	}

	private void updatePaused() {
		if (InputHandler.getInstance().didPause()) {
			state = GAME_RUNNING;
			return;
		}
	}

	private void updateLevelEnd() {
		swingAnimeFrame += 0.175f;
		if (swingAnimeFrame >= 11) {
			state = GAME_OVER;
		}
	}

	private void updateGameOver() {
		if (failed && !complete) {
			if (InputHandler.getInstance().didPause()) {
				state = GAME_RUNNING;
				nextLevel();
			}
		} else if (timeOut) {
			if (InputHandler.getInstance().didPause()) {
				state = GAME_RUNNING;
				nextLevel();
			}
		}

		if (complete && !failed) {
			if (InputHandler.getInstance().didPause()) {
				state = GAME_RUNNING;
				nextLevel();
			}
		}

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

	/** Unused ContactListener method */
	public void endContact(Contact contact) {
		Fixture fix1 = contact.getFixtureA();
		Fixture fix2 = contact.getFixtureB();

		Body body1 = fix1.getBody();
		Body body2 = fix2.getBody();

		try {
			GameObject bd1 = (GameObject)body1.getUserData();
			GameObject bd2 = (GameObject)body2.getUserData();

			if (bd1.getType() == DUGGI && ((Dinosaur) bd1).getForm() == Dinosaur.CARNIVORE_FORM
					&& bd2.getType() == BOULDER) {
				((Carnivore) bd1).setNextToBoulder(null);
			}
			if (bd2.getType() == DUGGI && ((Dinosaur) bd2).getForm() == Dinosaur.CARNIVORE_FORM
					&& bd1.getType() == BOULDER) {
				((Carnivore) bd2).setNextToBoulder(null);
			}
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
		duggiLight.setColor(0.96f,0.85f,0.03f,0.55f);
		duggiLight.setXray(true);
		duggiLight.setActive(false);

	}


	// Set shake fields. Thank you smilne for the code. Code used from https://www.netprogs.com/libgdx-screen-shaking/
	public void shake(float radius, float duration, float intensity){
		this.elapsed = 0;
		this.duration = duration / 1000f;
		this.radius = radius;
		this.randomAngle = random.nextFloat() % 360f;
		this.intensity = intensity;
	}

	private boolean isOnRiverTile(){
		for (River r : level.getRivers()){
			if (r.getGridLocation().x == level.getAvatarGridX() && r.getGridLocation().y == level.getAvatarGridY()) {
				if (!r.getIsBotRiver()) {
					if (level.getAvatar().getY() < r.getY()+.25f){
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	private int animationFrameForGoingIn(int numFrames, int direction){
		float smallestDistance = Float.MAX_VALUE;
		River river = null;
		float detectionRadius = 1.6f;
		float offsetX = 0;
		float offsetY = 0;

		GameObject g;
		if (direction == Dinosaur.UP){
			g = level.getGridObject(level.getAvatarGridX(),level.getAvatarGridY()+1);
			if (g != null){
				if (g.getType() == RIVER){
					river = (River) g;
				}
			}
		} else if (direction == Dinosaur.DOWN){
			g = level.getGridObject(level.getAvatarGridX(),level.getAvatarGridY()-1);
			if (g != null){
				if (g.getType() == RIVER){
					river = (River) g;
				}
			}
		} else if (direction == Dinosaur.LEFT){
			g = level.getGridObject(level.getAvatarGridX()-1,level.getAvatarGridY());
			if (g != null){
				if (g.getType() == RIVER){
					river = (River) g;
				}
			}
		} else if (direction == Dinosaur.RIGHT){
			g = level.getGridObject(level.getAvatarGridX()+1,level.getAvatarGridY());
			if (g != null){
				if (g.getType() == RIVER){
					river = (River) g;
				}
			}
		}

		if (river == null){
			return -1;
		} else {
			smallestDistance = Vector2.dst(level.getAvatar().getX(), level.getAvatar().getY(), river.getX(),
					river.getY());
		}

		if (direction == Dinosaur.DOWN || direction == Dinosaur.UP){
			if (river.getIsBotRiver() && river.getIsTopRiver()){
				return -1;
			}
		}
		else if (direction == Dinosaur.LEFT || direction == Dinosaur.RIGHT){
			if (river.getIsLeftRiver() && river.getIsRightRiver()){
				return -1;
			}
		}

		// Offset the smallest distance so animation doesn't cut off
		if (smallestDistance <= detectionRadius && !river.getisCenterTile()){
			int frame = numFrames - (int)((smallestDistance/detectionRadius) * (float)numFrames);
			if (smallestDistance < 0){
				frame = 0;
			}
			return frame;
		} else {
			return -1;
		}
	}

	public int animationFrameForNotCenterTileGoingIn(){
		if (!isOnRiverTile()){
			return -1;
		}

		River theRiverImOn;
		theRiverImOn = (River) level.getGridObject(level.getAvatarGridX(),level.getAvatarGridY());
		float x0 = theRiverImOn.getX() - 1f;
		float x1 = theRiverImOn.getX() - 5/7f;
		float x2 = theRiverImOn.getX() - 3/7f;
		float x3 = theRiverImOn.getX() - 1/7f;
		float x4 = theRiverImOn.getX() + 1/7f;
		float x5 = theRiverImOn.getX() + 3/7f;
		float x6 = theRiverImOn.getX() + 5/7f;
		float x7 = theRiverImOn.getX() + 1f;

		float y0 = theRiverImOn.getY() - 1f;
		float y1 = theRiverImOn.getY() - 5/7f;
		float y2 = theRiverImOn.getY() - 3/7f;
		float y3 = theRiverImOn.getY() - 1/7f;
		float y4 = theRiverImOn.getY() + 1/7f;
		float y5 = theRiverImOn.getY() + 3/7f;
		float y6 = theRiverImOn.getY() + 5/7f;
		float y7 = theRiverImOn.getY() + 1f;

		float avatarX = level.getAvatar().getX();
		float avatarY = level.getAvatar().getY();

		if (!theRiverImOn.getisCenterTile()){
			if (level.getAvatar().getDirection() == Dinosaur.RIGHT){
				if (!theRiverImOn.getIsRightRiver() || (!theRiverImOn.getIsTopRiver() && theRiverImOn.getIsLeftRiver())
						|| (!theRiverImOn.getIsBotRiver() && theRiverImOn.getIsLeftRiver())){
					return -1;
				}

				if (avatarX > x0 &&  avatarX < x1){
					return 0;
				}
				else if (avatarX > x1 && avatarX < x2){
					return 1;
				}
				else if (avatarX > x2 && avatarX < x3){
					return 2;
				}
				else if (avatarX > x3 && avatarX < x4){
					return 3;
				}
				else if (avatarX > x4 && avatarX < x5){
					return 4;
				}
				else if (avatarX > x5 && avatarX < x6){
					return 5;
				}
				else {
					return 6;
				}
			} else if (level.getAvatar().getDirection() == Dinosaur.LEFT){

				if (!theRiverImOn.getIsLeftRiver() || (!theRiverImOn.getIsBotRiver() && theRiverImOn.getIsRightRiver())
						|| (!theRiverImOn.getIsTopRiver() && theRiverImOn.getIsRightRiver()) ){
					return -1;
				}
				if (avatarX > x0 &&  avatarX < x1){
					return 6;
				}
				else if (avatarX > x1 && avatarX < x2){
					return 5;
				}
				else if (avatarX > x2 && avatarX < x3){
					return 4;
				}
				else if (avatarX > x3 && avatarX < x4){
					return 3;
				}
				else if (avatarX > x4 && avatarX < x5){
					return 2;
				}
				else if (avatarX > x5 && avatarX < x6){
					return 1;
				}
				else {
					return 0;
				}
			} else if (level.getAvatar().getDirection() == Dinosaur.DOWN){

				if (!theRiverImOn.getIsBotRiver() || (!theRiverImOn.getIsRightRiver() && theRiverImOn.getIsTopRiver())
						|| (!theRiverImOn.getIsLeftRiver() && theRiverImOn.getIsTopRiver())){
					return -1;
				}
				if (avatarY > y0 &&  avatarY < y1){
					return 6;
				}
				else if (avatarY > y1 && avatarY < y2){
					return 5;
				}
				else if (avatarY > y2 && avatarY < y3){
					return 4;
				}
				else if (avatarY > y3 && avatarY < y4){
					return 3;
				}
				else if (avatarY > y4 && avatarY < y5){
					return 2;
				}
				else if (avatarY> y5 && avatarY < y6){
					return 1;
				}
				else {
					return 0;
				}
			}
			else if (level.getAvatar().getDirection() == Dinosaur.UP){
				if (!theRiverImOn.getIsTopRiver() || (!theRiverImOn.getIsRightRiver() && theRiverImOn.getIsBotRiver())
						|| (!theRiverImOn.getIsLeftRiver() && theRiverImOn.getIsBotRiver())){
					return -1;
				}
				if (avatarY > y0 &&  avatarY < y1){
					return 0;
				}
				else if (avatarY > y1 && avatarY < y2){
					return 1;
				}
				else if (avatarY > y2 && avatarY < y3){
					return 2;
				}
				else if (avatarY > y3 && avatarY < y4){
					return 3;
				}
				else if (avatarY > y4 && avatarY < y5){
					return 4;
				}
				else if (avatarY> y5 && avatarY < y6){
					return 5;
				}
				else {
					return 6;
				}
			}

		}
		return -1;
	}

	public int animationFrameForNotCenterTileGoingOut(){
		if (!isOnRiverTile()){
			return -1;
		}

		River theRiverImOn;
		theRiverImOn = (River) level.getGridObject(level.getAvatarGridX(),level.getAvatarGridY());
		float x0 = theRiverImOn.getX() - 1f;
		float x1 = theRiverImOn.getX() - 5/7f;
		float x2 = theRiverImOn.getX() - 3/7f;
		float x3 = theRiverImOn.getX() - 1/7f;
		float x4 = theRiverImOn.getX() + 1/7f;
		float x5 = theRiverImOn.getX() + 3/7f;
		float x6 = theRiverImOn.getX() + 5/7f;
		float x7 = theRiverImOn.getX() + 1f;

		float y0 = theRiverImOn.getY() - 1f;
		float y1 = theRiverImOn.getY() - 5/7f;
		float y2 = theRiverImOn.getY() - 3/7f;
		float y3 = theRiverImOn.getY() - 1/7f;
		float y4 = theRiverImOn.getY() + 1/7f;
		float y5 = theRiverImOn.getY() + 3/7f;
		float y6 = theRiverImOn.getY() + 5/7f;
		float y7 = theRiverImOn.getY() + 1f;

		float avatarX = level.getAvatar().getX();
		float avatarY = level.getAvatar().getY();

		if (!theRiverImOn.getisCenterTile()){
			if (level.getAvatar().getDirection() == Dinosaur.RIGHT){
				if (theRiverImOn.getIsRightRiver() || (theRiverImOn.getIsTopRiver() && !theRiverImOn.getIsLeftRiver())
						|| (theRiverImOn.getIsBotRiver() && !theRiverImOn.getIsLeftRiver())){
					return -1;
				}

				if (avatarX > x0 &&  avatarX < x1){
					return 0;
				}
				else if (avatarX > x1 && avatarX < x2){
					return 1;
				}
				else if (avatarX > x2 && avatarX < x3){
					return 2;
				}
				else if (avatarX > x3 && avatarX < x4){
					return 3;
				}
				else if (avatarX > x4 && avatarX < x5){
					return 4;
				}
				else if (avatarX > x5 && avatarX < x6){
					return 5;
				}
				else {
					return 6;
				}
			} else if (level.getAvatar().getDirection() == Dinosaur.LEFT){

				if (theRiverImOn.getIsLeftRiver() || (theRiverImOn.getIsBotRiver() && !theRiverImOn.getIsRightRiver())
						|| (theRiverImOn.getIsTopRiver() && !theRiverImOn.getIsRightRiver()) ){
					return -1;
				}
				if (avatarX > x0 &&  avatarX < x1){
					return 6;
				}
				else if (avatarX > x1 && avatarX < x2){
					return 5;
				}
				else if (avatarX > x2 && avatarX < x3){
					return 4;
				}
				else if (avatarX > x3 && avatarX < x4){
					return 3;
				}
				else if (avatarX > x4 && avatarX < x5){
					return 2;
				}
				else if (avatarX > x5 && avatarX < x6){
					return 1;
				}
				else {
					return 0;
				}
			} else if (level.getAvatar().getDirection() == Dinosaur.DOWN){

				if (theRiverImOn.getIsBotRiver() || (theRiverImOn.getIsRightRiver() && !theRiverImOn.getIsTopRiver())
						|| (theRiverImOn.getIsLeftRiver() && !theRiverImOn.getIsTopRiver())){
					return -1;
				}
				if (avatarY > y0 &&  avatarY < y1){
					return 6;
				}
				else if (avatarY > y1 && avatarY < y2){
					return 5;
				}
				else if (avatarY > y2 && avatarY < y3){
					return 4;
				}
				else if (avatarY > y3 && avatarY < y4){
					return 3;
				}
				else if (avatarY > y4 && avatarY < y5){
					return 2;
				}
				else if (avatarY> y5 && avatarY < y6){
					return 1;
				}
				else {
					return 0;
				}
			}
			else if (level.getAvatar().getDirection() == Dinosaur.UP){
				if (theRiverImOn.getIsTopRiver() || (theRiverImOn.getIsRightRiver() && !theRiverImOn.getIsBotRiver())
						|| (theRiverImOn.getIsLeftRiver() && !theRiverImOn.getIsBotRiver())){
					return -1;
				}
				if (avatarY > y0 &&  avatarY < y1){
					return 0;
				}
				else if (avatarY > y1 && avatarY < y2){
					return 1;
				}
				else if (avatarY > y2 && avatarY < y3){
					return 2;
				}
				else if (avatarY > y3 && avatarY < y4){
					return 3;
				}
				else if (avatarY > y4 && avatarY < y5){
					return 4;
				}
				else if (avatarY> y5 && avatarY < y6){
					return 5;
				}
				else {
					return 6;
				}
			}

		}
		return -1;
	}


	// Returns true if enemy is on top of door or near it
	private boolean doorHasEnemyOnTop(Wall door){
		for (Enemy e: level.getEnemies()){
			if (e.getGridLocation().x == door.getGridLocation().x && e.getGridLocation().y == door.getGridLocation().y ||
					e.getGridLocation().x == door.getGridLocation().x+1 && e.getGridLocation().y == door.getGridLocation().y
					|| e.getGridLocation().x == door.getGridLocation().x-1 && e.getGridLocation().y == door.getGridLocation().y
					|| e.getGridLocation().x == door.getGridLocation().x && e.getGridLocation().y == door.getGridLocation().y+1
					|| e.getGridLocation().x == door.getGridLocation().x && e.getGridLocation().y == door.getGridLocation().y-1){
				return true;
			}
		}
		return false;
	}

	// Returns true if enemy is on top of door or near it
	private boolean doorHasPlayerOnTop(Wall door){

			if (level.getAvatarGridX() == door.getGridLocation().x && level.getAvatarGridY() == door.getGridLocation().y ||
					level.getAvatarGridX() == door.getGridLocation().x+1 && level.getAvatarGridY() == door.getGridLocation().y
					|| level.getAvatarGridX() == door.getGridLocation().x-1 && level.getAvatarGridY() == door.getGridLocation().y
					|| level.getAvatarGridX() == door.getGridLocation().x && level.getAvatarGridY() == door.getGridLocation().y+1
					|| level.getAvatarGridX() == door.getGridLocation().x && level.getAvatarGridY() == door.getGridLocation().y-1){
				return true;
			}

		return false;
	}

	private void enterRiverTileFirst(Vector2 position){
		if (currentRiver == null){
			currentRiver = position;
		}

		if (!currentRiver.equals(position)){
			int random = MathUtils.random(1);
			if (random == 0){
				SoundController.getInstance().playWaterSplash();
			}
			currentRiver = position;
		}

	}

	private void resetCamera(){
		canvas.getCamera().position.x = canvas.getWidth()/2.0f;
		canvas.getCamera().position.y = canvas.getHeight()/2.0f;
		canvas.getCamera().update();
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
	public void postSolve(Contact contact, ContactImpulse impulse) {}
	/** Unused ContactListener method */
	public void preSolve(Contact contact, Manifold oldManifold) {}
}