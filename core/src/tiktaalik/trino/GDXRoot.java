package tiktaalik.trino;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import com.badlogic.gdx.assets.loaders.*;
import com.badlogic.gdx.assets.loaders.resolvers.*;

import tiktaalik.trino.duggi.Dinosaur;
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
	public static boolean musicScreen = true;
	public static boolean shownStudioLogo;

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
			//SoundController.getInstance().dispose();
			if (SoundController.getInstance().playing) {
				SoundController.getInstance().init();
				SoundController.getInstance().mainMenu();
			}
			if (MenuController.currState == 2) {
//				menu.dispose();
//				menu = null;
//				levels = new LevelController(canvas, manager, 1);
//				levels.setScreenListener(this);
//				setScreen(null);
//				screen = levels;
//				setScreen(levels);
//				MenuController.currState = 0;

				System.out.println("loading up level " +(MenuController.levelNum - 1));
				controller.setCurrentLevel(MenuController.levelNum - 1);
				controller.loadContent(manager);
				controller.setScreenListener(this);
				controller.setCanvas(canvas);
				controller.reset();
				setScreen(null);
				screen = controller;
				setScreen(controller);
				MenuController.currState = 0;


			}
			else if (MenuController.currState == 1) {
				controller.loadContent(manager);
				controller.setScreenListener(this);
				controller.setCanvas(canvas);
				controller.reset();
				setScreen(null);
				screen = controller;
				setScreen(controller);
				MenuController.currState = 0;
				SoundController.getInstance().playing = false;
//				menu.dispose();
//				menu = null;

			}
		}
		else if (screen == levels) {
			if (LevelController.menuPress == 1) {
				menu = new MenuController(canvas,manager,1);
				menu.setScreenListener(this);
				screen = null;
				screen = menu;
				setScreen(menu);
			}
			else {
				if (LevelController.levelNum == 1) {
					controller.setCurrentLevel(0);
					controller.loadContent(manager);
					controller.setScreenListener(this);
					controller.setCanvas(canvas);
					controller.reset();
					setScreen(null);
					screen = controller;
					setScreen(controller);
				}
				else if (LevelController.levelNum == 2) {
					controller.setCurrentLevel(1);
					controller.loadContent(manager);
					controller.setScreenListener(this);
					controller.setCanvas(canvas);
					controller.reset();
					setScreen(null);
					screen = controller;
					setScreen(controller);
				}
				else if (LevelController.levelNum == 3) {
					controller.setCurrentLevel(2);
					controller.loadContent(manager);
					controller.setScreenListener(this);
					controller.setCanvas(canvas);
					controller.reset();
					setScreen(null);
					screen = controller;
					setScreen(controller);
				}
				else if (LevelController.levelNum == 4) {
					controller.setCurrentLevel(3);
					controller.loadContent(manager);
					controller.setScreenListener(this);
					controller.setCanvas(canvas);
					controller.reset();
					setScreen(null);
					screen = controller;
					setScreen(controller);

				}
				else if (LevelController.levelNum == 5) {
					controller.setCurrentLevel(4);
					controller.loadContent(manager);
					controller.setScreenListener(this);
					controller.setCanvas(canvas);
					controller.reset();
					setScreen(null);
					screen = controller;
					setScreen(controller);
				}
				else if (LevelController.levelNum == 6) {
					controller.setCurrentLevel(5);
					controller.loadContent(manager);
					controller.setScreenListener(this);
					controller.setCanvas(canvas);
					controller.reset();
					setScreen(null);
					screen = controller;
					setScreen(controller);
				}
				else if (LevelController.levelNum == 7) {
					controller.setCurrentLevel(6);
					controller.loadContent(manager);
					controller.setScreenListener(this);
					controller.setCanvas(canvas);
					controller.reset();
					setScreen(null);
					screen = controller;
					setScreen(controller);
				}
				else if (LevelController.levelNum == 8) {
					controller.setCurrentLevel(7);
					controller.loadContent(manager);
					controller.setScreenListener(this);
					controller.setCanvas(canvas);
					controller.reset();
					setScreen(null);
					screen = controller;
					setScreen(controller);
				}
				else if (LevelController.levelNum == 9) {
					controller.setCurrentLevel(8);
					controller.loadContent(manager);
					controller.setScreenListener(this);
					controller.setCanvas(canvas);
					controller.reset();
					setScreen(null);
					screen = controller;
					setScreen(controller);
				}
				else if (LevelController.levelNum == 10) {
					controller.setCurrentLevel(9);
					controller.loadContent(manager);
					controller.setScreenListener(this);
					controller.setCanvas(canvas);
					controller.reset();
					setScreen(null);
					screen = controller;
					setScreen(controller);
				}
				else if (LevelController.levelNum == 11) {
					controller.setCurrentLevel(10);
					controller.loadContent(manager);
					controller.setScreenListener(this);
					controller.setCanvas(canvas);
					controller.reset();
					setScreen(null);
					screen = controller;
					setScreen(controller);
				}
				else if (LevelController.levelNum == 12) {
					controller.setCurrentLevel(11);
					controller.loadContent(manager);
					controller.setScreenListener(this);
					controller.setCanvas(canvas);
					controller.reset();
					setScreen(null);
					screen = controller;
					setScreen(controller);
				}
				else if (LevelController.levelNum == 13) {
					controller.setCurrentLevel(12);
					controller.loadContent(manager);
					controller.setScreenListener(this);
					controller.setCanvas(canvas);
					controller.reset();
					setScreen(null);
					screen = controller;
					setScreen(controller);
				}
				else if (LevelController.levelNum == 14) {
					controller.setCurrentLevel(13);
					controller.loadContent(manager);
					controller.setScreenListener(this);
					controller.setCanvas(canvas);
					controller.reset();
					setScreen(null);
					screen = controller;
					setScreen(controller);
				}
			}
			LevelController.levelNum = 0;
			LevelController.menuPress = 0;
		}
		else if (screen == controller) {
			musicScreen = false;
			if (controller.menuNum == 1) {
				menu = new MenuController(canvas,manager,1);
				menu.setScreenListener(this);
				screen = null;
				screen = menu;
				setScreen(menu);
				SoundController.getInstance().playing = true;
//				controller.reset();
//				controller.dispose();
//				controller = new GameController();
//				controller.preLoadContent(manager);
				SoundController.getInstance().dispose();
				musicScreen = true;
			}
		}
		else if (exitCode == GameController.EXIT_NEXT) {
			musicScreen = false;
			controller.reset();
			setScreen(controller);
		} else if (exitCode == GameController.EXIT_PREV) {
			musicScreen = false;
			controller.reset();
			setScreen(controller);
		} else if (exitCode == GameController.EXIT_QUIT) {
			musicScreen = false;
			Gdx.app.exit();
		}
	}

}
