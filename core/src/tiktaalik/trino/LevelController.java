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

public class LevelController implements Screen, InputProcessor, ControllerListener {

    private static final String LEVEL_FILE = "trino/levelSelector.png";
    private static final String OVERLAY_FILE = "trino/levelOverlay.png";
    private static final String MENU_FILE = "trino/menuButton.png";

    private static int DEFAULT_BUDGET = 15; // Default budget for asset loader (do nothing but load 60 fps)
    private static int STANDARD_WIDTH  = 1280; // Standard window size (for scaling)
    private static int STANDARD_HEIGHT = 720; // Standard window height (for scaling)
    private static float BAR_HEIGHT_RATIO = 0.25f; // Ratio of the bar height to the screen
    private static float BUTTON_SCALE  = 1.0f; // Amount to scale the play button

    private Color hoverColor = new Color(0.86f, 0.81f, 0.75f, 1);
    private Color selectColor = new Color(0.76f, 0.69f, 0.63f, 1);

    private AssetManager manager; // AssetManager to be loading in the background
    private Canvas canvas; // Reference to Canvas created by the root
    private ScreenListener listener; // Listener that will update the player mode when we are done

    private Texture background;
    private Texture overlay;
    private Texture menu;
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
    private int selectState;
    private int hoverState;
    //private int levelNum;
    /** The amount of time to devote to loading assets (as opposed to on screen hints, etc.) */
    private int   budget;
    /** Whether or not this player mode is still active */
    private boolean active;

    /** Level selected */
    public static int levelNum = 0;
    /** Level hovered */
    public static int levelHover = 0;

    /** Menu selected */
    public int menuState;
    /** Menu hovered */
    public int menuHover;
    public static int menuPress = 0;



    /**
     * Returns true if all assets are loaded and the player is ready to go.
     *
     * @return true if the player is ready to go
     */
    public boolean isReady() {
        return selectState == 2;
    }

    /**
     * Returns true if menu button is clicked
     *
     * @return true if the player wants to go to the menu
     */
    public boolean isMenu() {
        return menuState == 2;
    }

    /**
     * Creates a MenuController with the default budget, size and position.
     *
     * @param manager The AssetManager to load in the background
     */
    public LevelController (Canvas canvas, AssetManager manager) {
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
    public LevelController (Canvas canvas, AssetManager manager, int millis) {
        this.manager = manager;
        this.canvas  = canvas;
        budget = millis;


        resize(canvas.getWidth(),canvas.getHeight());

        background = null;
        overlay = null;
        menu = null;
        // No progress so far.
        progress   = 0;
        active = false;

        Gdx.input.setInputProcessor(this);
        active = true;
    }

    /**
     * Called when this screen should release all resources.
     */
    public void dispose() {
        if (background != null) {
            background.dispose();
            background = null;
        }

        if (overlay != null) {
            overlay.dispose();
            overlay = null;
        }

        if (menu != null) {
            menu.dispose();
            menu = null;
        }
    }

    /**
     * Update the status of this player mode.
     *
     * @param delta Number of seconds since last animation frame
     */
    private void update(float delta) {
        if (background == null) {
            manager.update(budget);
            this.progress = manager.getProgress();
            if (progress >= 1.0f) {
                this.progress = 1.0f;
                background = new Texture(LEVEL_FILE);
                overlay = new Texture(OVERLAY_FILE);
                menu = new Texture(MENU_FILE);
                listener.exitScreen(this, 0);
            }
        }

    }

    /**
     * Draw the status of this player mode.
     */

    private void draw() {
        canvas.begin();

            canvas.draw(background, 0,0);

//            Color menuTint;
//            if (menuState == 1) {
//                menuTint = selectColor;
//            }
//            else if (menuHover == 1) {
//                menuTint = hoverColor;
//            }
//            else {
//                menuTint = Color.WHITE;
//            }
//            canvas.draw(menu, menuTint, menu.getWidth()/2, menu.getHeight()/2,
//                centerX, 460, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);
//
//            if (hoverState == 1) {
//                if (levelHover == 1) {
//                    canvas.draw(overlay, 50, 37);
//                }
//                else if (levelHover == 2) {
//                    canvas.draw(overlay, 467, 37);
//                }
//                else if (levelHover == 3) {
//                    canvas.draw(overlay, 874, 37);
//                }
//            }

//            else {
//                overlay.dispose();
//                overlay = null;
//            }
//            Color playTint;
//            if (selectState == 1)
//                playTint = selectColor;
//            else if (hoverState == 1)
//                playTint = hoverColor;
//            else
//                playTint = Color.WHITE;
//
//            Color levelTint;
//            if (selectState == 1)
//                levelTint = selectColor;
//            else if (hoverState == 1)
//                levelTint = hoverColor;
//            else
//                levelTint = Color.WHITE;

        canvas.end();
    }

    /**
     * Updates the progress bar according to loading progress
     *

     * @param canvas The drawing context
     */
//    private void drawProgress(Canvas canvas) {
//
//        if (progress < 0.2f)
//            canvas.draw(statusOne, 0,0);
//        else if (progress >= 0.2f && progress < 0.6f)
//            canvas.draw(statusTwo,  0, 0);
//        else if (progress >= 0.6f)
//            canvas.draw(statusThree, 0, 0);
//    }

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
            if ((isReady() || isMenu()) && listener != null) {
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
        if (background == null || selectState == 2 || menuState == 2) {
            return true;
        }

        // Flip to match graphics coordinates
        screenY = heightY-screenY;


        // level one
        if ((screenX >=  120) && (screenX <= 288) &&
                (screenY >= 516) && (screenY <= 598)) {
            selectState = 1;
            levelNum = 1;
        }
        else if ((screenX >=  342) && (screenX <= 510) &&
                (screenY >= 516) && (screenY <= 598)) {
            selectState = 1;
            levelNum = 2;
        }
        else if ((screenX >=  564) && (screenX <= 732) &&
                (screenY >= 516) && (screenY <= 598)) {
            selectState = 1;
            levelNum = 3;
        }
        else if ((screenX >=  786) && (screenX <= 954) &&
                (screenY >= 516) && (screenY <= 598)) {
            selectState = 1;
            levelNum = 4;
        }
        else if ((screenX >=  1008) && (screenX <= 1176) &&
                (screenY >= 516) && (screenY <= 598)) {
            selectState = 1;
            levelNum = 5;
        }
        else if ((screenX >=  120) && (screenX <= 288) &&
                (screenY >= 400) && (screenY <= 482)) {
            selectState = 1;
            levelNum = 6;
        }
        else if ((screenX >=  342) && (screenX <= 510) &&
                (screenY >= 400) && (screenY <= 482)) {
            selectState = 1;
            levelNum = 7;
        }
        else if ((screenX >=  564) && (screenX <= 732) &&
                (screenY >= 400) && (screenY <= 482)) {
            selectState = 1;
            levelNum = 8;
        }
        else if ((screenX >=  786) && (screenX <= 954) &&
                (screenY >= 400) && (screenY <= 482)) {
            selectState = 1;
            levelNum = 9;
        }
        else if ((screenX >=  1008) && (screenX <= 1176) &&
                (screenY >= 400) && (screenY <= 482)) {
            selectState = 1;
            levelNum = 10;
        }
        else if ((screenX >=  120) && (screenX <= 288) &&
                (screenY >= 284) && (screenY <= 366)) {
            selectState = 1;
            levelNum = 11;
        }
        else if ((screenX >=  342) && (screenX <= 510) &&
                (screenY >= 284) && (screenY <= 366)) {
            selectState = 1;
            levelNum = 12;
        }
        else if ((screenX >=  564) && (screenX <= 732) &&
                (screenY >= 284) && (screenY <= 366)) {
            selectState = 1;
            levelNum = 13;
        }
        else if ((screenX >=  786) && (screenX <= 954) &&
                (screenY >= 284) && (screenY <= 366)) {
            selectState = 1;
            levelNum = 14;
        }

        else if ((screenX >= 914) && (screenX <= 1248) &&
                (screenY >= 638) && (screenY <= 1280)) {
            menuState = 1;
            menuPress = 1;
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
        if (selectState == 1) {
            selectState = 2;
            return false;
        }
        if (menuState == 1) {
            menuState = 2;
            return false;
        }

        return true;
    }

    public boolean mouseMoved(int screenX, int screenY) {
        // Flip to match graphics coordinates
        screenY = heightY-screenY;

        if (background != null) {
            if ((screenX >=  49) && (screenX <= 260) &&
                    (screenY >= 36) && (screenY <= 165)) {
                hoverState = 1;
                levelHover = 1;
            }
            else if ((screenX >=  466) && (screenX <= 674) &&
                    (screenY >= 36) && (screenY <= 165)) {
                hoverState = 1;
                levelHover = 2;
            }
            else if ((screenX >=  873) && (screenX <= 1082) &&
                    (screenY >= 36) && (screenY <= 165)) {
                hoverState = 1;
                levelHover = 3;
            }
            else {
                hoverState = 0;
                levelHover = 0;
            }

            if ((screenX >= 482) && (screenX <= 798) &&
                    (screenY >= 440) && (screenY <= 486)) {
                menuHover = 1;
            }
            else {
                menuHover = 0;
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
            selectState = 2;
            menuState = 2;
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
}