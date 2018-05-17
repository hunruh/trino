/*

 * Class for the Menu/Loading view
 */
package tiktaalik.trino;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.controllers.*;
import tiktaalik.util.*;

public class MenuController implements Screen, InputProcessor, ControllerListener {

	private static final String BACKGROUND_FILE = "trino/menu2.png";
	private static final String PROGRESS_FILE_ONE = "trino/load1.png";
	private static final String PROGRESS_FILE_TWO = "trino/load2.png";
	private static final String PROGRESS_FILE_THREE = "trino/load3.png";
	private static final String STUDIO_FILE = "trino/studioLogo.png";
	private static final String PLAY_BTN_FILE = "trino/startButton.png";
	private static final String LEVEL_SELECT_BTN_FILE = "trino/levelSelectButton.png";
	private static final String LOADING_FILE = "trino/loading.png";
	private static final String LOADING_DOLL_FILE = "trino/loading_doll.png";
	private static final String BLACK_BACKGROUND_FILE = "trino/blackBackground.png";
	private static final String LEVEL_BUTTON_FILE = "trino/wood.png";
	private static final String CLICK_SOUND_FILE = "trino/click.mp3";
    private static String FONT_FILE = "hud/gyparody/gyparody rg.ttf";
    private static int FONT_SIZE = 64;
    private BitmapFont displayFont;

	private Color hoverColor = new Color(0.86f, 0.81f, 0.75f, 1);
	private Color selectColor = new Color(0.76f, 0.69f, 0.63f, 1);

	private Texture background; // Background texture for start-up
	private Texture playButton; // Play button to display when done
	private Texture levelSelectButton; // Level Select button to display when done
	private Texture statusOne; // Loading texture 1
	private Texture statusTwo; // Loading texture 2
	private Texture statusThree; // Loading texture 3
	private Texture blackBackground; // black background for loading
	private Texture loadingText;
	private Texture studioLogo; // Studio logo
	private FilmStrip loadingDuggi; // Loading duggi walking animation
	private float animeFrame;
	private Texture duggiWalking;
	private Texture levelButton;
	private Vector2[] levelButtonPositions;
	private Sound click;

	private static int DEFAULT_BUDGET = 15; // Default budget for asset loader (do nothing but load 60 fps)
	private static int STANDARD_WIDTH  = 1280; // Standard window size (for scaling)
	private static int STANDARD_HEIGHT = 720; // Standard window height (for scaling)
	private static float BAR_HEIGHT_RATIO = 0.25f; // Ratio of the bar height to the screen
	private static float BUTTON_SCALE  = 1.0f; // Amount to scale the play button

	private AssetManager manager; // AssetManager to be loading in the background
	private Canvas canvas; // Reference to Canvas created by the root
	private ScreenListener listener; // Listener that will update the player mode when we are done

	/** The y-coordinate of the center of the progress bar */
	private int centerY;
	/** The x-coordinate of the center of the progress bar */
	private int centerX;
	/** The height of the canvas window (necessary since sprite origin != screen origin) */
	private int heightY;
	/** Scaling factor for when the student changes the resolution. */
	private float scale;

	/** Current progress (0 to 1) of the asset manager */
	private float progress;
	/** The current state of the play button */
	private int playPressState;
	private int playHoverState;
	private int levelPressState;
	private int levelHoverState;
	/** The amount of time to devote to loading assets (as opposed to on screen hints, etc.) */
	private int   budget;
	/** Whether or not this player mode is still active */
	private boolean active;
	Long startTime = System.currentTimeMillis();

	public static int currState = 0;

	private boolean panningToLevelSelect;
	private boolean panningToMainMenu;
	private boolean onLevelSelectScreen;
	private float cameraOffset;

    /** Level selected */
    public static int levelNum = 0;

	/**
	 * Returns true if level select.
	 *
	 * @return true if the player wants to go to level selection
	 */
	public boolean isSelect() {
		if (levelPressState == 2) {
			levelPressState = 0;
			levelHoverState = 0;
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Returns true if all assets are loaded and the player is ready to go.
	 *
	 * @return true if the player is ready to go
	 */
	public boolean isReady() {
		if (playPressState == 2) {
			playPressState = 0;
			playHoverState = 0;
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Creates a MenuController with the default budget, size and position.
	 *
	 * @param manager The AssetManager to load in the background
	 */
	public MenuController(Canvas canvas, AssetManager manager) {
		this(canvas, manager,DEFAULT_BUDGET);
	}

	/**
	 * Creates a MenuController with the default size and position.
	 *
	 * The budget is the number of milliseconds to spend loading assets each animation
	 * frame.  This allows you to do something other than load assets.  An animation
	 * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to
	 * do something else.  This is how game companies animate their loading screens.
	 *
	 * @param manager The AssetManager to load in the background
	 * @param millis The loading budget in milliseconds
	 */
	public MenuController(Canvas canvas, AssetManager manager, int millis) {
		this.manager = manager;
		this.canvas  = canvas;
		budget = millis;


		resize(canvas.getWidth(),canvas.getHeight());

		playButton = null;
		levelSelectButton = null;
		background = null;
		statusOne  = new Texture(PROGRESS_FILE_ONE);
		statusTwo  = new Texture(PROGRESS_FILE_TWO);
		statusThree  = new Texture(PROGRESS_FILE_THREE);
		blackBackground = new Texture(BLACK_BACKGROUND_FILE);
		loadingText = new Texture(LOADING_FILE);
		studioLogo = new Texture(STUDIO_FILE);
		duggiWalking = new Texture(LOADING_DOLL_FILE);
		duggiWalking.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		loadingDuggi = new FilmStrip(duggiWalking,1,8, 8);

		// No progress so far.
		progress   = 0;
		playPressState = 0;
		playHoverState = 0;
		levelPressState = 0;
		levelHoverState = 0;
		active = false;

		Gdx.input.setInputProcessor(this);
		active = true;
	}

	/**
	 * Called when this screen should release all resources.
	 */
	public void dispose() {

		 background.dispose();
		 statusOne.dispose();
		 statusTwo.dispose();
		 statusThree.dispose();
		 blackBackground.dispose();
		 loadingText.dispose();
		 duggiWalking.dispose();
		 levelButton.dispose();
		 studioLogo.dispose();
		 click.dispose();
		 blackBackground = null;
		 background = null;
		 statusOne  = null;
		 statusTwo = null;
		 statusThree = null;
		 loadingText = null;
		 duggiWalking = null;
		 levelButton = null;
		 click = null;
		 if (playButton != null) {
			 playButton.dispose();
			 playButton = null;
		 }
		if (levelSelectButton != null) {
			levelSelectButton.dispose();
			levelSelectButton = null;
		}
	}

	/**
	 * Update the status of this player mode.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	private void update(float delta) {
		//System.out.println("menu curr state is " + currState);
	    if (panningToLevelSelect){
            canvas.getCamera().position.x += 20;
            if (canvas.getCamera().position.x >= 1920){
                canvas.getCamera().position.x = 1920;
                onLevelSelectScreen = true;
                panningToLevelSelect = false;
            }
            canvas.getCamera().update();
        } else if (panningToMainMenu){
            canvas.getCamera().position.x -= 20;
            if (canvas.getCamera().position.x <= 640){
                canvas.getCamera().position.x = 640;
                panningToMainMenu = false;
            }
            canvas.getCamera().update();
        }

		animeFrame+=0.35f;
		if (animeFrame >= 7){
			animeFrame = 0;
		}

		if (playButton == null) {
		    if ((System.currentTimeMillis()-startTime)/1000 >= 5) {
                manager.update(budget);
            }
            this.progress = manager.getProgress();
				if (progress >= 1.0f) {
					this.progress = 1.0f;
					background = new Texture(BACKGROUND_FILE);
					playButton = new Texture(PLAY_BTN_FILE);
					playButton.setFilter(TextureFilter.Linear, TextureFilter.Linear);
					levelSelectButton = new Texture(LEVEL_SELECT_BTN_FILE);
					levelSelectButton.setFilter(TextureFilter.Linear, TextureFilter.Linear);
					listener.exitScreen(this, 0);

                    levelButton = new Texture(LEVEL_BUTTON_FILE);
                    levelButtonPositions = new Vector2[20];

                    // Load the click sound
					click = Gdx.audio.newSound(Gdx.files.internal(CLICK_SOUND_FILE));

                    // Load the font
                    FreetypeFontLoader.FreeTypeFontLoaderParameter size2Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
                    size2Params.fontFileName = FONT_FILE;
                    size2Params.fontParameters.size = FONT_SIZE;
                    manager.load(FONT_FILE, BitmapFont.class, size2Params);

                    // Allocate the font
                    if (manager.isLoaded(FONT_FILE))
                        displayFont = manager.get(FONT_FILE,BitmapFont.class);
                    else
                        displayFont = null;
				}

		}
	}

	/**
	 * Draw the status of this player mode.
	 */
	private void draw() {
        canvas.clear();
		canvas.begin();
		if ((System.currentTimeMillis()-startTime)/1000 < 5 && progress == 0 && playButton == null && !GDXRoot.shownStudioLogo) {
			canvas.draw(studioLogo,0,0);
		}
		else {
            GDXRoot.shownStudioLogo = true;
			if (playButton == null) {
				drawProgress(canvas);
			} else {
				canvas.draw(background, 0, 0);
                //canvas.draw(background, 1280, 0);
				Color playTint;
				if (playPressState == 1)
					playTint = selectColor;
				else if (playHoverState == 1)
					playTint = hoverColor;
				else
					playTint = Color.WHITE;
				canvas.draw(playButton, playTint, playButton.getWidth() / 2, playButton.getHeight() / 2,
						centerX, centerY + 75, 0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);

				Color levelTint;
				if (levelPressState == 1)
					levelTint = selectColor;
				else if (levelHoverState == 1)
					levelTint = hoverColor;
				else
					levelTint = Color.WHITE;
				canvas.draw(levelSelectButton, levelTint, levelSelectButton.getWidth() / 2, levelSelectButton.getHeight() / 2,
						centerX, centerY - 25, 0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);

				drawLevelButtons(canvas,20);

			}
		}
		canvas.end();
	}

	/**
	 * Updates the progress bar according to loading progress
	 *

	 * @param canvas The drawing context
	 */
	private void drawProgress(Canvas canvas) {
//		if (progress < 0.2f)
//			canvas.draw(statusOne, 0,0);
//		else if (progress >= 0.2f && progress < 0.6f)
//			canvas.draw(statusTwo,  0, 0);
//		else if (progress >= 0.6f)
//			canvas.draw(statusThree, 0, 0);

		canvas.draw(blackBackground, canvas.width/2.0f,canvas.height/2.0f);
		canvas.draw(loadingText, 825, 50);
		loadingDuggi.setFrame((int)animeFrame);
		Vector2 origin = new Vector2(loadingDuggi.getRegionWidth()/ 2.0f, loadingDuggi.getRegionHeight()/2.0f);
		if (loadingDuggi != null) {
			canvas.draw(loadingDuggi, Color.WHITE,origin.x,origin.y,1200, 80,0,0.75f,0.75f);
		}
	}

	private void drawLevelButtons(Canvas canvas, int numLevels){
	    float size = 250f;
	    float xCurrent = 1280 + size;
	    float yCurrent = 720 - size/2;
	    for (int i = 0; i < numLevels; i++){
	        if (xCurrent >= 2560 - size ){
	            xCurrent = 1280 + size;
	            yCurrent -= size/2;
            }

            // Draw the level buttons
            canvas.draw(levelButton, Color.WHITE, levelButton.getWidth()/2,levelButton.getHeight()/2,
                    xCurrent, yCurrent, 0,0.50f,0.50f );
	        canvas.drawText(""+(i+1),displayFont,xCurrent-15f,yCurrent+15f);
	        levelButtonPositions[i] = new Vector2(xCurrent - 1280f,yCurrent);

	        xCurrent += size;
        }

        // Draw the mainmenu button
        // Draw the level buttons
        canvas.draw(levelButton, Color.WHITE, levelButton.getWidth()/2,levelButton.getHeight()/2,
                1400, 720, 0,0.50f,0.50f );
        canvas.drawText("BACK",displayFont,1350f,715f);

        canvas.drawText("LEVEL SELECT", displayFont, 1800, 715);

    }

    private void checkLevelButtonPressed(float x, float y){
	    float size = 250f;
	    for (int i = 0; i < levelButtonPositions.length; i++){
	        //System.out.println("item " + i + "is " + levelButtonPositions[i]);
	        if (x > (levelButtonPositions[i].x - size/3f) && x < (levelButtonPositions[i].x + size/3f) &&
                    y > (levelButtonPositions[i].y - size/5f) && y < (levelButtonPositions[i].y + size/5f)){
	            //System.out.println("level " + (i+1) + "pressed");
	            levelNum = i + 1;
	            levelPressState = 1;
	            playClick();
            }

        }
    }
    private void playClick(){
		if (click != null){
			click.pause();
			click.play();
		}
	}
	/**
	 * Called when the Screen should render itself.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	public void render(float delta) {
		if (active) {
			update(delta);
			draw();

			// We are are ready, notify our listener
			if ((isReady() || isSelect()) && listener != null) {
				listener.exitScreen(this, 0);
			}
		}
	}

	/**
	 * Called when the Screen is resized.

	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		float sx = ((float)width)/STANDARD_WIDTH;
		float sy = ((float)height)/STANDARD_HEIGHT;
		scale = (sx < sy ? sx : sy);


		centerY = (int)(BAR_HEIGHT_RATIO*height);
		centerX = width/2;
		heightY = height;
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

/*
	 * Called when the screen was touched or a mouse button was pressed.
	 *
	 * @param screenX the x-coordinate of the mouse on the screen
	 * @param screenY the y-coordinate of the mouse on the screen
	 * @param pointer the button or touch finger number
	 * @return whether to hand the event to other listeners.
	 */
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (playButton == null || playPressState == 2) {
			return true;
		}

		// Flip to match graphics coordinates
		screenY = heightY-screenY;



		if (!panningToLevelSelect && !panningToMainMenu && currState == 0){
            if (!onLevelSelectScreen){
                // Play button is a circle.
                if ((screenX > centerX - playButton.getWidth()/2) && (screenX < centerX + playButton.getWidth()/2) &&
                        (screenY > centerY + 75 - playButton.getHeight()/2) && (screenY < centerY + 75 + playButton.getHeight()/2)) {
                    playPressState = 1;
                    playClick();
                }
                if ((screenX > centerX - levelSelectButton.getWidth()/2) && (screenX < centerX + levelSelectButton.getWidth()/2) &&
                        (screenY > centerY - 25 - levelSelectButton.getHeight()/2) && (screenY < centerY - 25 + levelSelectButton.getHeight()/2)) {
                    panningToLevelSelect = true;
                    playClick();

                }
            } else if (onLevelSelectScreen) {
                //System.out.println("reached checklevelbuttonpressed from touchdown");
                //System.out.println("screenX is " +screenX);
                //System.out.println("screenY is " +screenY);
                // Check if main menu in level select is pressed
                if (screenX > 0f && screenX < 250f && screenY > 680 && screenY < 720){
                    panningToMainMenu = true;
                    onLevelSelectScreen = false;
                    playClick();
                }
                checkLevelButtonPressed(screenX,screenY);
            }
        }

		return false;
	}

	/**
	 * Called when a finger was lifted or a mouse button was released.
	 *

	 * @param screenX the x-coordinate of the mouse on the screen
	 * @param screenY the y-coordinate of the mouse on the screen
	 * @param pointer the button or touch finger number
	 * @return whether to hand the event to other listeners.
	 */
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if (playPressState == 1) {
			playPressState = 2;
			currState = 1;
			return false;
		}
		if (levelPressState == 1) {
			levelPressState = 2;
			currState = 2;
			return false;
		}
		return true;
	}

	public boolean mouseMoved(int screenX, int screenY) {
		// Flip to match graphics coordinates
		screenY = heightY-screenY;

		if (playButton != null) {
			if ((screenX > centerX - playButton.getWidth()/2) && (screenX < centerX + playButton.getWidth()/2) &&
				(screenY > centerY + 75 - playButton.getHeight()/2) && (screenY < centerY + 75 + playButton.getHeight()/2)) {
				playHoverState = 1;
			} else {
				playHoverState = 0;
			}

			if ((screenX > centerX - levelSelectButton.getWidth()/2) && (screenX < centerX + levelSelectButton.getWidth()/2) &&
					(screenY > centerY - 25 - levelSelectButton.getHeight()/2) && (screenY < centerY - 25 + levelSelectButton.getHeight()/2)) {
				levelHoverState = 1;
			} else {
				levelHoverState = 0;
			}
		}

		return true;
	}

	/**
	 * Called when a key is released.

	 *
	 * @param keycode the key released
	 * @return whether to hand the event to other listeners.
	 */
	public boolean keyUp(int keycode) {
		if (keycode == Input.Keys.N || keycode == Input.Keys.P) {
			playPressState = 2;
			return false;
		}
		return true;
	}


	/* Unused Screen method */
	public void pause() {}
	/* Unused Screen method */
	public void resume() {}
	/* Unused ControllerListener method */
	public boolean buttonDown(Controller controller, int buttonCode) {
		return true;
	}
	/* Unused ControllerListener method */
	public boolean buttonUp(Controller controller, int buttonCode) {
		return true;
	}
	/* Unused InputProcessor method */
	public boolean keyDown(int keycode) {
		return true;
	}
	/* Unused InputProcessor method */
	public boolean keyTyped(char character) {
		return true;
	}
	/* Unused InputProcessor method */
	public boolean scrolled(int amount) { 
		return true; 
	}
	/* Unused InputProcessor method */
	public boolean touchDragged(int screenX, int screenY, int pointer) { 
		return true; 
	}
	/* Unused ControllerListener method */

	public void connected (Controller controller) {}
	/* Unused ControllerListener method */
	public void disconnected (Controller controller) {}
	/* Unused ControllerListener method */
	public boolean axisMoved (Controller controller, int axisCode, float value) {
		return true;
	}
	/* Unused ControllerListener method */
	public boolean povMoved (Controller controller, int povCode, PovDirection value) {
		return true;
	}
	/* Unused ControllerListener method */
	public boolean xSliderMoved (Controller controller, int sliderCode, boolean value) {
		return true;
	}
	/* Unused ControllerListener method */
	public boolean ySliderMoved (Controller controller, int sliderCode, boolean value) {
		return true;
	}
	/* Unused ControllerListener method */
	public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
		return true;
	}

	private Texture createFilmTexture(AssetManager manager, String file) {
		if (manager.isLoaded(file)) {
			Texture texture = manager.get(file, Texture.class);
			texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
			return texture;
		}
		return null;
	}
}