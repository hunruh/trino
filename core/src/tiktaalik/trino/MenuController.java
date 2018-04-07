/*

 * Class for the Menu/Loading view
 */
package tiktaalik.trino;

import com.badlogic.gdx.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.controllers.*;
import tiktaalik.util.*;

public class MenuController implements Screen, InputProcessor, ControllerListener {

	private static final String BACKGROUND_FILE = "trino/menu.png";
	private static final String PROGRESS_FILE_ONE = "trino/load1.png";
	private static final String PROGRESS_FILE_TWO = "trino/load2.png";
	private static final String PROGRESS_FILE_THREE = "trino/load3.png";
	private static final String PLAY_BTN_FILE = "shared/play.png";


	private Texture background; // Background texture for start-up
	private Texture playButton; // Play button to display when done
	private Texture statusOne; // Loading texture 1
	private Texture statusTwo; // Loading texture 2
	private Texture statusThree; // Loading texture 3


	private static int DEFAULT_BUDGET = 15; // Default budget for asset loader (do nothing but load 60 fps)
	private static int STANDARD_WIDTH  = 1280; // Standard window size (for scaling)
	private static int STANDARD_HEIGHT = 720; // Standard window height (for scaling)
	private static float BAR_HEIGHT_RATIO = 0.25f; // Ratio of the bar height to the screen
	private static float BUTTON_SCALE  = 0.60f; // Amount to scale the play button

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
	private int   pressState;
	/** The amount of time to devote to loading assets (as opposed to on screen hints, etc.) */
	private int   budget;
	/** Whether or not this player mode is still active */
	private boolean active;

	/**
	 * Returns true if all assets are loaded and the player is ready to go.
	 *
	 * @return true if the player is ready to go
	 */
	public boolean isReady() {
		return pressState == 2;
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
		background = null;
		statusOne  = new Texture(PROGRESS_FILE_ONE);
		statusTwo  = new Texture(PROGRESS_FILE_TWO);
		statusThree  = new Texture(PROGRESS_FILE_THREE);

		// No progress so far.
		progress   = 0;
		pressState = 0;
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
		 background = null;
		 statusOne  = null;
		 statusTwo = null;
		 statusThree = null;
		 if (playButton != null) {
			 playButton.dispose();
			 playButton = null;
		 }
	}

	/**
	 * Update the status of this player mode.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	private void update(float delta) {
		if (playButton == null) {
			manager.update(budget);
			this.progress = manager.getProgress();
			if (progress >= 1.0f) {
				this.progress = 1.0f;
				background = new Texture(BACKGROUND_FILE);
				playButton = new Texture(PLAY_BTN_FILE);
				playButton.setFilter(TextureFilter.Linear, TextureFilter.Linear);
			}
		}
	}

	/**
	 * Draw the status of this player mode.
	 */
	private void draw() {
		canvas.begin();
		if (playButton == null) {
			drawProgress(canvas);
		} else {
			canvas.draw(background, 0,0);
			Color tint = (pressState == 1 ? Color.GRAY: new Color(0x4DC068ff));
			canvas.draw(playButton, tint, playButton.getWidth()/2, playButton.getHeight()/2,
					centerX, centerY - playButton.getHeight()/3, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);
		}
		canvas.end();
	}

	/**
	 * Updates the progress bar according to loading progress
	 *

	 * @param canvas The drawing context
	 */
	private void drawProgress(Canvas canvas) {

		if (progress < 0.2f)
			canvas.draw(statusOne, 0,0);
		else if (progress >= 0.2f && progress < 0.6f)
			canvas.draw(statusTwo,  0, 0);
		else if (progress >= 0.6f)
			canvas.draw(statusThree, 0, 0);
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
			if (isReady() && listener != null) {
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
		if (playButton == null || pressState == 2) {
			return true;
		}

		// Flip to match graphics coordinates
		screenY = heightY-screenY;


		// Play button is a circle.
		float radius = BUTTON_SCALE*scale*playButton.getWidth()/2.0f;
		float dist = (screenX-centerX)*(screenX-centerX)+(screenY-centerY+playButton.getHeight()/3)*(screenY-centerY+playButton.getHeight()/3);
		if (dist < radius*radius) {
			pressState = 1;
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
		if (pressState == 1) {
			pressState = 2;
			return false;
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
			pressState = 2;
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
	public boolean mouseMoved(int screenX, int screenY) { 
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
}