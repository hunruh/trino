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
	private boolean herbiFormPressed;
	private boolean carniFormPressed;
	private boolean actionPressed;
	private boolean actionPrevious;
	private boolean pausePressed;
	private boolean pausePrevious;
	private boolean mousePressed;
	private boolean mousePrevious;
	private boolean nextLevelPressed;
	private boolean helpPressed;
	private boolean helpPrevious;
	
	/** How much did we move horizontally? */
	private float horizontal;
	/** How much did we move vertically? */
	private float vertical;

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
				((Gdx.input.getX() >= 1209) && (Gdx.input.getX() <= 1260)) &&
				((Gdx.input.getY() >= 13) && (Gdx.input.getY() <= 65))); }

	/**
	 * Returns true if the return to menu button was pressed.
	 *
	 * @return true if the return to menu botton was pressed.
 	 */
	public boolean didReturn() {
		return ((!pausePressed && pausePrevious) || !mousePrevious) && mousePressed &&
				((Gdx.input.getX() >= 541) && (Gdx.input.getX() <= 741)) &&
				((Gdx.input.getY() >= 299) && (Gdx.input.getY() <= 335));
	}

	/**
	 * Return true if the help button was pressed.
	 *
	 * @return true if the help button was pressed.
	 */
	public boolean didHelp() {
		return ((!pausePressed && pausePrevious) || !mousePrevious) && mousePressed &&
				((Gdx.input.getX() >= 596) && (Gdx.input.getX() <= 686)) &&
				((Gdx.input.getY() >= 365) && (Gdx.input.getY() <= 401));
	}

	/**
	 * Return true if the restart button was pressed.
	 *
	 * @return true if the restart button was pressed.
	 */
	public boolean didRestart() {
		return ((!pausePressed && pausePrevious) || !mousePrevious) && mousePressed &&
				((Gdx.input.getX() >= 562) && (Gdx.input.getX() <= 719)) &&
				((Gdx.input.getY() >= 432) && (Gdx.input.getY() <= 467));
	}

	/**
	 * Return true if the resume button was pressed.
	 *
	 * @return true if the resume button was pressed.
	 */
	public boolean didResume() {
		return ((!pausePressed && pausePrevious) || !mousePrevious) && mousePressed &&
				((Gdx.input.getX() >= 569) && (Gdx.input.getX() <= 714)) &&
				((Gdx.input.getY() >= 497) && (Gdx.input.getY() <= 533));
	}

	/**
	 * Return true if the music button was pressed.
	 *
	 * @return true if the music button was pressed.
	 */
	public boolean didMusic() {
		return ((!pausePressed && pausePrevious) || !mousePrevious) && mousePressed &&
				((Gdx.input.getX() >= 525) && (Gdx.input.getX() <= 609)) &&
				((Gdx.input.getY() >= 217) && (Gdx.input.getY() <= 269));
	}

	/**
	 * Return true if the sound button was pressed.
	 *
	 * @return true if the sound button was pressed.
	 */
	public boolean didSound() {
		return ((!pausePressed && pausePrevious) || !mousePrevious) && mousePressed &&
				((Gdx.input.getX() >= 672) && (Gdx.input.getX() <= 756)) &&
				((Gdx.input.getY() >= 217) && (Gdx.input.getY() <= 269));
	}

	/**
	 * Return true if the exit button was pressed.
	 *
	 * @return true if the exit button was pressed.
	 */
	public boolean didExitButton() {
		return ((!pausePressed && pausePrevious) || !mousePrevious) && mousePressed &&
				((Gdx.input.getX() >= 892) && (Gdx.input.getX() <= 932)) &&
				((Gdx.input.getY() >= 493) && (Gdx.input.getY() <= 534));
	}

	/**
	 *
	 * Return true if main menu button is pressed after level is over.
	 *
	 * @return true if the main menu button is pressed after level is over.
	 */
	public boolean didDone() {
		return ((Gdx.input.getX() >= 700) && (Gdx.input.getX() <= 868)) &&
				((Gdx.input.getY() >= 506) && (Gdx.input.getY() <= 535));
	}

	/**
	 *
	 * Return true if restart level button is pressed after level is over.
	 *
	 * @return true if the restart level button is pressed after level is over.
	 */
	public boolean didRestartLevel() {
		return ((Gdx.input.getX() >= 422) && (Gdx.input.getX() <= 555)) &&
				((Gdx.input.getY() >= 506) && (Gdx.input.getY() <= 535));
	}

	/**
	 *
	 * Return true if next level button is pressed after level is over.
	 *
	 * @return true if the next level button is pressed after level is over.
	 */
	public boolean didNextLevel() {
		return (mousePressed && (Gdx.input.getX() >= 758) && (Gdx.input.getX() <= 837)) &&
				((Gdx.input.getY() >= 506) && (Gdx.input.getY() <= 535));
	}

	/**
     * Return the UI element that is being hovered over.
     *
     * @return the UI element that is being hovered over.
     */
    public int didHover() {
        if (((Gdx.input.getX() >= 525) && (Gdx.input.getX() <= 725)) &&
                ((Gdx.input.getY() >= 299) && (Gdx.input.getY() <= 335))) {
            return 2; // hovering over main menu
        }
        else if (((Gdx.input.getX() >= 580) && (Gdx.input.getX() <= 670)) &&
                ((Gdx.input.getY() >= 365) && (Gdx.input.getY() <= 401))) {
            return 3; // hovering over help button
        }
        else if (((Gdx.input.getX() >= 546) && (Gdx.input.getX() <= 703)) &&
                ((Gdx.input.getY() >= 432) && (Gdx.input.getY() <= 467))) {
            return 4; // hovering over restart button
        }
        else if (((Gdx.input.getX() >= 553) && (Gdx.input.getX() <= 698)) &&
                ((Gdx.input.getY() >= 497) && (Gdx.input.getY() <= 533))) {
            return 5; // hovering over resume button
        }
        else if (((Gdx.input.getX() >= 509) && (Gdx.input.getX() <= 593)) &&
                ((Gdx.input.getY() >= 217) && (Gdx.input.getY() <= 269))) {
            return 0; // hovering over music button
        }
        else if (((Gdx.input.getX() >= 656) && (Gdx.input.getX() <= 740)) &&
                ((Gdx.input.getY() >= 217) && (Gdx.input.getY() <= 269))) {
            return 1; // hovering over sound button
        }
        return -1;
    }

	/**
	 * Return true if the doll button was pressed.
	 *
	 * @return true if the doll button was pressed.
	 */
	public boolean didDollHelp() {
		return (!helpPrevious && !mousePrevious && mousePressed && !actionPrevious
				&& ((Gdx.input.getX() >= 388) && (Gdx.input.getX() <= 487)) &&
				((Gdx.input.getY() >= 233) && (Gdx.input.getY() <= 398)));
	}

	/**
	 * Return true if the herbivore button was pressed.
	 *
	 * @return true if the herbivore button was pressed.
	 */
	public boolean didHerbivoreHelp() {
		return (!helpPrevious && !mousePressed
				&& ((Gdx.input.getX() >= 576) && (Gdx.input.getX() <= 705)) &&
				((Gdx.input.getY() >= 233) && (Gdx.input.getY() <= 401)));
	}

	/**
	 * Return true if the carnivore button was pressed.
	 *
	 * @return true if the carnivore button was presesd.
	 */
	public boolean didCarnivoreHelp() {
		return (!helpPrevious && !mousePrevious && mousePressed && !actionPrevious
				&& ((Gdx.input.getX() >= 761) && (Gdx.input.getX() <= 892)) &&
				((Gdx.input.getY() >= 221) && (Gdx.input.getY() <= 398)));
	}

	public boolean didTransformDoll(){
		return dollFormPressed;
	}

	public boolean didTransformHerbi(){
		return herbiFormPressed;
	}

	public boolean didTransformCarni(){
		return carniFormPressed;
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
		helpPrevious = helpPressed;

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
		helpPressed = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
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
	}
}