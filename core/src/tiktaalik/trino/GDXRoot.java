package tiktaalik.trino;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import com.badlogic.gdx.assets.loaders.*;
import com.badlogic.gdx.assets.loaders.resolvers.*;

import tiktaalik.util.*;

/**
 * Root class for a LibGDX.
 */
public class GDXRoot extends Game implements ScreenListener {
	private AssetManager manager; // AssetManager to load game assets
	private Canvas canvas; // Drawing context to display graphics
	private MenuController menu; // Player mode for the asset menu screen
	private LevelController levels; // Player mode for the asset level selection screen
	private GameController controller; // The game controller

	public GDXRoot() {
		manager = new AssetManager();

		FileHandleResolver resolver = new InternalFileHandleResolver();
		manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
		manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));
	}

	/** 
	 * Called when the Application is first created.
	 *
	 * This is method immediately loads assets for the menu screen, and prepares
	 * the asynchronous loader for all other assets.
	 */
	public void create() {
		canvas  = new Canvas();
		menu = new MenuController(canvas,manager,1);

		controller = new GameController();
		controller.preLoadContent(manager);

		menu.setScreenListener(this);
		setScreen(menu);

	}

	/** 
	 * Called when the Application is destroyed. 
	 *
	 * This is preceded by a call to pause().
	 */
	public void dispose() {
		// Call dispose on our children
		setScreen(null);
		controller.unloadContent(manager);
		controller.dispose();

		canvas.dispose();
		canvas = null;
	
		// Unload all of the resources
		manager.clear();
		manager.dispose();
		super.dispose();
	}
	
	/**
	 * Called when the Application is resized.
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		canvas.resize();
		super.resize(width,height);
	}
	
	/**
	 * The given screen has made a request to exit its player mode.
	 *
	 * The value exitCode can be used to implement menu options.
	 *
	 * @param screen   The screen requesting to exit
	 * @param exitCode The state of the screen upon exit
	 */
	public void exitScreen(Screen screen, int exitCode) {
		if (screen == menu) {
			if (MenuController.currState == 2) {
				levels = new LevelController(canvas, manager, 1);
				levels.setScreenListener(this);
				setScreen(levels);
			}
			else if (MenuController.currState == 1) {
				controller.loadContent(manager);
				controller.setScreenListener(this);
				controller.setCanvas(canvas);
				controller.reset();
				setScreen(controller);
			}

			
			menu.dispose();
			menu = null;
		} else if (screen == levels) {
			if (LevelController.levelNum == 1) {
				controller.loadContent(manager);
				controller.setScreenListener(this);
				controller.setCanvas(canvas);
				controller.reset();
				setScreen(controller);
			}
			else if (LevelController.levelNum == 2) {
				controller.loadContent(manager);
				controller.setScreenListener(this);
				controller.setCanvas(canvas);
				controller.reset();
				setScreen(controller);
				controller.nextLevel();
			}
			else if (LevelController.levelNum == 3) {
				controller.loadContent(manager);
				controller.setScreenListener(this);
				controller.setCanvas(canvas);
				controller.reset();
				setScreen(controller);
				controller.nextLevel();
				controller.nextLevel();

			}
		}
		else if (exitCode == GameController.EXIT_NEXT) {
			controller.reset();
			setScreen(controller);
		} else if (exitCode == GameController.EXIT_PREV) {
			controller.reset();
			setScreen(controller);
		} else if (exitCode == GameController.EXIT_QUIT) {
			Gdx.app.exit();
		}
	}

}
