/*
 * InputHandler.java
 *
 * This class buffers in input from the devices and converts it into its
 * semantic meaning. If your game had an option that allows the player to
 * remap the control keys, you would store this information in this class.
 * That way, the main GameEngine does not have to keep track of the current
 * key mapping.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package tiktaalik.trino;

import com.badlogic.gdx.*;
import com.badlogic.gdx.math.*;

/**
 * Class for reading player input. 
 *
 * This supports both a keyboard and X-Box controller. In previous solutions, we only 
 * detected the X-Box controller on start-up.  This class allows us to hot-swap in
 * a controller via the new XBox360Controller class.
 */
public class InputHandler {
	// Sensitivity for moving crosshair with gameplay
	private static final float GP_ACCELERATE = 1.0f;
	private static final float GP_MAX_SPEED  = 10.0f;
	private static final float GP_THRESHOLD  = 0.01f;

	/** The singleton instance of the input controller */
	private static InputHandler theController = null;
	
	/** 
	 * Return the singleton instance of the input controller
	 *
	 * @return the singleton instance of the input controller
	 */
	public static InputHandler getInstance() {
		if (theController == null) {
			theController = new InputHandler();
		}
		return theController;
	}
	
	// Fields to manage buttons
	/** Whether the reset button was pressed. */
	private boolean resetPressed;
	private boolean resetPrevious;

	/** Night mode (FOR TESTING PURPOSES ONLY) */
	private boolean nightPressed;
	private boolean nightPrevious;

	/** Whether the primary action button was pressed. */
	private boolean primePressed;
	private boolean primePrevious;
	/** Whether the secondary action button was pressed. */
	private boolean secondPressed;
	private boolean secondPrevious;
	/** Whether the teritiary action button was pressed. */
	private boolean tertiaryPressed;
	/** Whether the debug toggle was pressed. */
	private boolean debugPressed;
	private boolean debugPrevious;
	/** Whether the exit button was pressed. */
	private boolean exitPressed;
	private boolean exitPrevious;
	/**transformation */
	private boolean dollFormPressed;
	private boolean dollFormPrevious;
	private boolean herbiFormPressed;
	private boolean herbiFormPrevious;
	private boolean carniFormPressed;
	private boolean carniFormPrevious;
	private boolean actionPressed;
	private boolean actionPrevious;
	private boolean pausePressed;
	private boolean pausePrevious;
	private boolean mousePressed;
	private boolean mousePrevious;
	private boolean nextLevelPressed;
	
	/** How much did we move horizontally? */
	private float horizontal;
	/** How much did we move vertically? */
	private float vertical;
	/** The crosshair position (for raddoll) */
	private Vector2 crosshair;
	/** The crosshair cache (for using as a return value) */
	private Vector2 crosscache;
	/** For the gamepad crosshair control */
	private float momentum;

	private float prevHorizontal = 0f;
	private float prevVertical = 0f;
	
	/**
	 * Returns the amount of sideways movement. 
	 *
	 * -1 = left, 1 = right, 0 = still
	 *
	 * @return the amount of sideways movement. 
	 */
	public float getHorizontal() {
		return horizontal;
	}
	
	/**
	 * Returns the amount of vertical movement. 
	 *
	 * -1 = down, 1 = up, 0 = still
	 *
	 * @return the amount of vertical movement. 
	 */
	public float getVertical() {
		return vertical;
	}
	
	/**
	 * Returns the current position of the crosshairs on the screen.
	 *
	 * This value does not return the actual reference to the crosshairs position.
	 * That way this method can be called multiple times without any fair that 
	 * the position has been corrupted.  However, it does return the same object
	 * each time.  So if you modify the object, the object will be reset in a
	 * subsequent call to this getter.
	 *
	 * @return the current position of the crosshairs on the screen.
	 */
	/*public Vector2 getCrossHair() {
		return crosscache.set(crosshair);
	}*/

	/**
	 * Returns true if the primary action button was pressed.
	 *
	 * This is a one-press button. It only returns true at the moment it was
	 * pressed, and returns false at any frame afterwards.
	 *
	 * @return true if the primary action button was pressed.
	 */
	public boolean didPrimary() {
		return primePressed && !primePrevious;
	}

	/**
	 * Returns true if the secondary action button was pressed.
	 *
	 * This is a one-press button. It only returns true at the moment it was
	 * pressed, and returns false at any frame afterwards.
	 *
	 * @return true if the secondary action button was pressed.
	 */
	public boolean didSecondary() {
		return secondPressed && !secondPrevious;
	}

	/**
	 * Returns true if the tertiary action button was pressed.
	 *
	 * This is a sustained button. It will returns true as long as the player
	 * holds it down.
	 *
	 * @return true if the secondary action button was pressed.
	 */
	public boolean didTertiary() {
		return tertiaryPressed;
	}

	/**
	 * Returns true if the reset button was pressed.
	 *
	 * @return true if the reset button was pressed.
	 */
	public boolean didReset() {
		return resetPressed && !resetPrevious;
	}

	/**
	 * Returns true if the night button was pressed.
	 *
	 * @return true if the night button was pressed.
	 */
	public boolean didNight() {
		return nightPressed && !nightPrevious;
	}

	/**
	 * Returns true if the pause button was pressed.
	 *
	 * @return true if the pause button was pressed.
	 */
	public boolean didPause() {
		return (pausePressed && !pausePrevious) || (mousePressed && !mousePrevious &&
				((Gdx.input.getX() >= 1157) && (Gdx.input.getX() <= 1216)) &&
				((Gdx.input.getY() >= 11) && (Gdx.input.getY() <= 71))); }

	/**
	 * Returns true if the return to menu button was pressed.
	 *
	 * @return true if the return to menu botton was pressed.
 	 */
	public boolean didReturn() {
		return ((!pausePressed && pausePrevious) || !mousePrevious) && mousePressed &&
				((Gdx.input.getX() >= 573) && (Gdx.input.getX() <= 676)) &&
				((Gdx.input.getY() >= 224) && (Gdx.input.getY() <= 261));
	}

	/**
	 * Return true if the help button was pressed.
	 *
	 * @return true if the help button was pressed.
	 */
	public boolean didHelp() {
		return ((!pausePressed && pausePrevious) || !mousePrevious) && mousePressed &&
				((Gdx.input.getX() >= 583) && (Gdx.input.getX() <= 676)) &&
				((Gdx.input.getY() >= 293) && (Gdx.input.getY() <= 330));
	}

	/**
	 * Return true if the restart button was pressed.
	 *
	 * @return true if the restart button was pressed.
	 */
	public boolean didRestart() {
		return ((!pausePressed && pausePrevious) || !mousePrevious) && mousePressed &&
				((Gdx.input.getX() >= 557) && (Gdx.input.getX() <= 718)) &&
				((Gdx.input.getY() >= 363) && (Gdx.input.getY() <= 400));
	}

	/**
	 * Return true if the resume button was pressed.
	 *
	 * @return true if the resume button was pressed.
	 */
	public boolean didResume() {
		return ((!pausePressed && pausePrevious) || !mousePrevious) && mousePressed &&
				((Gdx.input.getX() >= 565) && (Gdx.input.getX() <= 714)) &&
				((Gdx.input.getY() >= 428) && (Gdx.input.getY() <= 465));
	}

	/**
	 * Return true if the music button was pressed.
	 *
	 * @return true if the music button was pressed.
	 */
	public boolean didMusic() {
		return ((!pausePressed && pausePrevious) || !mousePrevious) && mousePressed &&
				((Gdx.input.getX() >= 725) && (Gdx.input.getX() <= 764)) &&
				((Gdx.input.getY() >= 499) && (Gdx.input.getY() <= 540));
	}

	/**
	 * Return true if the sound button was pressed.
	 *
	 * @return true if the sound button was pressed.
	 */
	public boolean didSound() {
		return ((!pausePressed && pausePrevious) || !mousePrevious) && mousePressed &&
				((Gdx.input.getX() >= 782) && (Gdx.input.getX() <= 829)) &&
				((Gdx.input.getY() >= 511) && (Gdx.input.getY() <= 540));
	}

	/**
	 * Return true if the exit button was pressed.
	 *
	 * @return true if the exit  button was pressed.
	 */
	public boolean didExitButton() {
		return ((!pausePressed && pausePrevious) || !mousePrevious) && mousePressed &&
				((Gdx.input.getX() >= 1100) && (Gdx.input.getX() <= 1141)) &&
				((Gdx.input.getY() >= 156) && (Gdx.input.getY() <= 197));
	}
	/**
	 * Returns true if the player wants to go toggle the debug mode.
	 *
	 * @return true if the player wants to go toggle the debug mode.
	 */
	public boolean didDebug() {
		return debugPressed && !debugPrevious;
	}
	
	/**
	 * Returns true if the exit button was pressed.
	 *
	 * @return true if the exit button was pressed.
	 */
	public boolean didExit() {
		return exitPressed && !exitPrevious;
	}

	public boolean didTransformDoll(){
		return dollFormPressed && !dollFormPrevious;
	}

	public boolean didTransformHerbi(){
		return herbiFormPressed && !herbiFormPrevious;
	}

	public boolean didTransformCarni(){
		return carniFormPressed && !carniFormPrevious;
	}

	public boolean didTransform() {
		return didTransformDoll() || didTransformHerbi() || didTransformCarni();
	}

	public boolean didAction() {
		return actionPressed && !actionPrevious;
	}

	public boolean didActionRelease() {
		return !actionPressed && actionPrevious;
	}

	public boolean isNextLevelPressed() {
		return nextLevelPressed;
	}

	/**
	 * Creates a new input controller
	 * 
	 * The input controller attempts to connect to the X-Box controller at device 0,
	 * if it exists.  Otherwise, it falls back to the keyboard control.
	 */
	public InputHandler() {
		// If we have a game-pad for id, then use it.
		crosshair = new Vector2();
		crosscache = new Vector2();
	}

	/**
	 * Reads the input for the player and converts the result into game logic.
	 */
	public void readInput() {
		// Copy state from last animation frame
		// Helps us ignore buttons that are held down
		primePrevious  = primePressed;
		secondPrevious = secondPressed;
		resetPrevious  = resetPressed;
		nightPrevious = nightPressed;
		debugPrevious  = debugPressed;
		exitPrevious = exitPressed;
		actionPrevious = actionPressed;
		pausePrevious = pausePressed;
		mousePrevious = mousePressed;

		readKeyboard();
	}

	/**
	 * Reads input from the keyboard.
	 */
	private void readKeyboard() {
		resetPressed = Gdx.input.isKeyPressed(Input.Keys.R);
		nightPressed = Gdx.input.isKeyPressed(Input.Keys.N);
		dollFormPressed = Gdx.input.isKeyPressed(Input.Keys.NUM_1) || Gdx.input.isKeyPressed(Input.Keys.NUMPAD_1);
		herbiFormPressed = Gdx.input.isKeyPressed(Input.Keys.NUM_2) || Gdx.input.isKeyPressed(Input.Keys.NUMPAD_2);
		carniFormPressed = Gdx.input.isKeyPressed(Input.Keys.NUM_3) || Gdx.input.isKeyPressed(Input.Keys.NUMPAD_3);
		actionPressed = Gdx.input.isKeyPressed(Input.Keys.SPACE);
		pausePressed = Gdx.input.isKeyPressed(Input.Keys.ESCAPE);
		mousePressed = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
		//debugPressed = (Gdx.input.isKeyPressed(Input.Keys.D));
		debugPressed = (Gdx.input.isKeyPressed(Input.Keys.D));
		primePressed = (Gdx.input.isKeyPressed(Input.Keys.UP));
		nextLevelPressed = (Gdx.input.isKeyPressed(Input.Keys.S)||Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT));

		exitPressed  = (Gdx.input.isKeyPressed(Input.Keys.E));
		// Directional controls
		prevHorizontal = horizontal;
		prevVertical = vertical;
		horizontal = 0.0f;
		vertical = 0.0f;
		if (Gdx.input.isKeyPressed(Input.Keys.UP) && Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
			if (prevVertical > 0){
				vertical += 5.0f;
			} else if (prevHorizontal > 0) {
				horizontal += 5.0f;
			}
		}
		else if (Gdx.input.isKeyPressed(Input.Keys.UP) && Gdx.input.isKeyPressed(Input.Keys.LEFT)){
			if (prevVertical > 0){
				vertical += 5.0f;
			} else if (prevHorizontal < 0) {
				horizontal -= 5.0f;
			}
		}
		else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) && Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
			if (prevVertical < 0){
				vertical -= 5.0f;
			} else if (prevHorizontal > 0) {
				horizontal += 5.0f;
			}
		}
		else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) && Gdx.input.isKeyPressed(Input.Keys.LEFT)){
			if (prevVertical < 0){
				vertical -= 5.0f;
			} else if (prevHorizontal < 0){
				horizontal -= 5.0f;
			}
		}

		else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && !Gdx.input.isKeyPressed(Input.Keys.UP) &&
				!Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
			horizontal += 5.0f;
		}
		else if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && !Gdx.input.isKeyPressed(Input.Keys.UP) &&
				!Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
			horizontal -= 5.0f;
		}

		else if (Gdx.input.isKeyPressed(Input.Keys.UP) && !Gdx.input.isKeyPressed(Input.Keys.RIGHT) &&
				!Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			vertical += 5.0f;
		}
		else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) && !Gdx.input.isKeyPressed(Input.Keys.RIGHT) &&
				!Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			vertical -= 5.0f;
		}
		
		// Mouse results
        tertiaryPressed = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
		//crosshair.set(Gdx.input.getX(), Gdx.input.getY());
		//crosshair.scl(1/scale.x,-1/scale.y);
		//crosshair.y += bounds.height;
		//clampPosition(bounds);
	}
	
	/**
	 * Clamp the cursor position so that it does not go outside the window
	 *
	 * While this is not usually a problem with mouse control, this is critical 
	 * for the gamepad controls.
	 *//*
	private void clampPosition(Rectangle bounds) {
		crosshair.x = Math.max(bounds.x, Math.min(bounds.x+bounds.width, crosshair.x));
		crosshair.y = Math.max(bounds.y, Math.min(bounds.y+bounds.height, crosshair.y));
	}*/
}