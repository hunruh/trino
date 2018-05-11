package tiktaalik.trino;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import tiktaalik.trino.duggi.Clone;
import tiktaalik.trino.duggi.Dinosaur;
import tiktaalik.util.FilmStrip;

public class HUDController  {
    private GameController.AssetState hudAssetState = GameController.AssetState.EMPTY;
    private Array<String> assets;

    // FONT FILES
    private static String FONT_FILE = "hud/gyparody/gyparody rg.ttf";
    private static int FONT_SIZE = 32;

    // Textures necessary to support the loading screen
    private static final String DINOMETER_BACKGROUND_FILE = "hud/dinometer_background.png";
    private static final String COTTON_FILE = "hud/cotton.png";
    private static final String COTTON_LIGHT_FILE = "hud/cotton_light.png";
    private static final String MEAT_FILE = "hud/meat.png";
    private static final String MEAT_LIGHT_FILE = "hud/meat_light.png";
    private static final String LEAF_FILE = "hud/leaf.png";
    private static final String LEAF_LIGHT_FILE = "hud/leaf_light.png";
    private static final String PAUSE_LIGHT_FILE = "hud/pause_light.png";
    private static final String PAUSE_DARK_FILE = "hud/pause_dark.png";
    private static final String PAUSE_BACKGROUND_FILE = "hud/pause_background.png";
    private static final String OUTLINE_PRIMARY_FILE = "hud/outline_primary.png";
    private static final String OUTLINE_SECONDARY_FILE = "hud/outline_secondary.png";
    private static final String DOLL_PRIMARY_FILE = "hud/doll_primary.png";
    private static final String DOLL_SECONDARY_FILE = "hud/doll_secondary.png";
    private static final String CARNIVORE_PRIMARY_FILE = "hud/carn_primary.png";
    private static final String CARNIVORE_SECONDARY_FILE = "hud/carn_secondary.png";
    private static final String HERBIVORE_PRIMARY_FILE = "hud/herb_primary.png";
    private static final String HERBIVORE_SECONDARY_FILE = "hud/herb_secondary.png";
    private static final String CLONE_CIRCLE_FILE = "trino/chargedowncircle.png";
    private static final String CLONE_FILE = "trino/clone.png";
    private static final String WOOD_FILE = "trino/wood.png";
    private static final String CLOCK_FILE = "hud/clockImage.png";

    private BitmapFont displayFont;

    private TextureRegion dinometerBackground;
    private TextureRegion cotton;
    private TextureRegion cottonLight;
    private TextureRegion meat;
    private TextureRegion meatLight;
    private TextureRegion leaf;
    private TextureRegion leafLight;
    private TextureRegion pauseDark;
    private TextureRegion pauseLight;
    private TextureRegion pauseBackground;
    private TextureRegion outlinePrimary;
    private TextureRegion outlineSecondary;
    private TextureRegion dollPrimary;
    private TextureRegion dollSecondary;
    private TextureRegion carnivorePrimary;
    private TextureRegion carnivoreSecondary;
    private TextureRegion herbivorePrimary;
    private TextureRegion herbivoreSecondary;
    private TextureRegion cloneImage;
    private TextureRegion wood;
    private TextureRegion clock;

    private Texture cloneCircle;

    private Canvas canvas; // Reference to Canvas created by the root

    private int numResources;
    private int transformation;
    private float cloneTime;
    private float levelTimerCount;
    private boolean flashResourceRed;
    private float stopFlashTime;

    public HUDController() {
        assets = new Array<String>();

        numResources = 0;
        transformation = Dinosaur.DOLL_FORM;
    }

    public void preLoadContent(AssetManager manager) {
        if (hudAssetState != GameController.AssetState.EMPTY) {
            return;
        }

        // Load the font
        FreetypeFontLoader.FreeTypeFontLoaderParameter size2Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        size2Params.fontFileName = FONT_FILE;
        size2Params.fontParameters.size = FONT_SIZE;
        manager.load(FONT_FILE, BitmapFont.class, size2Params);
        assets.add(FONT_FILE);

        hudAssetState = GameController.AssetState.LOADING;

        manager.load(DINOMETER_BACKGROUND_FILE,Texture.class);
        assets.add(DINOMETER_BACKGROUND_FILE);
        manager.load(COTTON_FILE,Texture.class);
        assets.add(COTTON_FILE);
        manager.load(COTTON_LIGHT_FILE,Texture.class);
        assets.add(COTTON_LIGHT_FILE);
        manager.load(MEAT_FILE,Texture.class);
        assets.add(MEAT_FILE);
        manager.load(MEAT_LIGHT_FILE,Texture.class);
        assets.add(MEAT_LIGHT_FILE);
        manager.load(LEAF_FILE,Texture.class);
        assets.add(LEAF_FILE);
        manager.load(LEAF_LIGHT_FILE,Texture.class);
        assets.add(LEAF_LIGHT_FILE);
        manager.load(PAUSE_DARK_FILE,Texture.class);
        assets.add(PAUSE_DARK_FILE);
        manager.load(PAUSE_LIGHT_FILE,Texture.class);
        assets.add(PAUSE_LIGHT_FILE);
        manager.load(PAUSE_BACKGROUND_FILE,Texture.class);
        assets.add(PAUSE_BACKGROUND_FILE);
        manager.load(OUTLINE_PRIMARY_FILE,Texture.class);
        assets.add(OUTLINE_PRIMARY_FILE);
        manager.load(OUTLINE_SECONDARY_FILE,Texture.class);
        assets.add(OUTLINE_SECONDARY_FILE);
        manager.load(DOLL_PRIMARY_FILE,Texture.class);
        assets.add(DOLL_PRIMARY_FILE);
        manager.load(DOLL_SECONDARY_FILE,Texture.class);
        assets.add(DOLL_SECONDARY_FILE);
        manager.load(CARNIVORE_PRIMARY_FILE,Texture.class);
        assets.add(CARNIVORE_PRIMARY_FILE);
        manager.load(CARNIVORE_SECONDARY_FILE,Texture.class);
        assets.add(CARNIVORE_SECONDARY_FILE);
        manager.load(HERBIVORE_PRIMARY_FILE,Texture.class);
        assets.add(HERBIVORE_PRIMARY_FILE);
        manager.load(HERBIVORE_SECONDARY_FILE,Texture.class);
        assets.add(HERBIVORE_SECONDARY_FILE);
        manager.load(CLONE_CIRCLE_FILE, Texture.class);
        assets.add(CLONE_CIRCLE_FILE);
        manager.load(CLONE_FILE, Texture.class);
        assets.add(CLONE_FILE);
        manager.load(WOOD_FILE, Texture.class);
        assets.add(WOOD_FILE);
        manager.load(CLOCK_FILE, Texture.class);
        assets.add(CLOCK_FILE);
    }

    public void loadContent(AssetManager manager) {
        if (hudAssetState != GameController.AssetState.LOADING) {
            return;
        }
        // Allocate the font
        if (manager.isLoaded(FONT_FILE))
            displayFont = manager.get(FONT_FILE,BitmapFont.class);
        else
            displayFont = null;

        dinometerBackground = createTexture(manager,DINOMETER_BACKGROUND_FILE,true);
        cotton = createTexture(manager,COTTON_FILE,true);
        cottonLight = createTexture(manager,COTTON_LIGHT_FILE,true);
        meat = createTexture(manager,MEAT_FILE,true);
        meatLight = createTexture(manager,MEAT_LIGHT_FILE,true);
        leaf = createTexture(manager,LEAF_FILE,true);
        leafLight = createTexture(manager,LEAF_LIGHT_FILE,true);
        pauseDark = createTexture(manager,PAUSE_DARK_FILE,true);
        pauseLight = createTexture(manager,PAUSE_LIGHT_FILE,true);
        pauseBackground = createTexture(manager,PAUSE_BACKGROUND_FILE,true);
        outlinePrimary = createTexture(manager,OUTLINE_PRIMARY_FILE,true);
        outlineSecondary = createTexture(manager,OUTLINE_SECONDARY_FILE,true);
        dollPrimary = createTexture(manager,DOLL_PRIMARY_FILE,true);
        dollSecondary = createTexture(manager,DOLL_SECONDARY_FILE,true);
        carnivorePrimary = createTexture(manager,CARNIVORE_PRIMARY_FILE,true);
        carnivoreSecondary = createTexture(manager,CARNIVORE_SECONDARY_FILE,true);
        herbivorePrimary = createTexture(manager,HERBIVORE_PRIMARY_FILE,true);
        herbivoreSecondary = createTexture(manager,HERBIVORE_SECONDARY_FILE,true);
        cloneImage = createTexture(manager, CLONE_FILE, true);
        wood = createTexture(manager, WOOD_FILE, true);
        clock = createTexture(manager, CLOCK_FILE, true);

        cloneCircle = createFilmTexture(manager,CLONE_CIRCLE_FILE);

        hudAssetState = GameController.AssetState.COMPLETE;
    }

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

    public void unloadContent(AssetManager manager) {
        for(String s : assets) {
            if (manager.isLoaded(s)) {
                manager.unload(s);
            }
        }
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    public void dispose() {
        dinometerBackground = null;
        cotton = null;
        cottonLight = null;
        meat = null;
        meatLight = null;
        leaf = null;
        leafLight = null;
        pauseDark = null;
        pauseLight = null;
        pauseBackground = null;
        outlinePrimary = null;
        outlineSecondary = null;
        dollPrimary = null;
        dollSecondary = null;
        carnivorePrimary = null;
        carnivoreSecondary = null;
        herbivorePrimary = null;
        herbivoreSecondary = null;
        cloneCircle = null;
        cloneImage = null;
        wood = null;
        clock = null;
    }

    public void update(int numResources, int transformation, Clone clone, float levelTimerCount) {
        this.numResources = numResources;
        this.transformation = transformation;
        if (clone != null){
            this.cloneTime = clone.getCloneTime();
        } else {
            this.cloneTime = 60.0f;
        }
        this.levelTimerCount = levelTimerCount;
        if (levelTimerCount <= stopFlashTime){
            flashResourceRed = false;
        }
    }

    public void draw() {
        canvas.beginOverlay();
        drawForm(canvas);
        drawDinoMeter(canvas);
        drawCloneCircle(canvas);
        drawLevelTimer(canvas,levelTimerCount);
        drawPause(canvas);
        canvas.end();
    }

    private void drawForm(Canvas canvas) {
        Vector2 origin = new Vector2(wood.getRegionWidth()/2.0f, wood.getRegionHeight()/2.0f);
        Color color = Color.WHITE;

        if (cloneTime >= 60.0f){
            color = new Color(0.75f,0.75f,0.75f,1);
        }

        canvas.draw(wood, color, origin.x, origin.y, -75f,canvas.getHeight() - 105.0f
                ,0,-0.80f,0.80f);


        if (numResources == 3){
            if ((int) levelTimerCount % 2 == 0){
                color = Color.GREEN;
            } else {
                color = Color.WHITE;
            }

        }
        else if (flashResourceRed){
            color = new Color(1f,0.5f,0.5f,1f);
        } else {
            color = Color.WHITE;
        }
        canvas.draw(wood, color, origin.x, origin.y, 160.0f,canvas.getHeight() - 20.0f
                ,0,0.80f,0.80f);
        if (transformation == Dinosaur.DOLL_FORM) {
            canvas.draw(dollPrimary, 7, canvas.getHeight() - 82);
            canvas.draw(outlinePrimary, 7, canvas.getHeight() - 82);
            canvas.draw(herbivoreSecondary, 70, canvas.getHeight() - 76);
            canvas.draw(outlineSecondary, 70, canvas.getHeight() - 76);
            canvas.draw(carnivoreSecondary, 54, canvas.getHeight() - 92);
            canvas.draw(outlineSecondary, 54, canvas.getHeight() - 92);
        }
        else if (transformation == Dinosaur.HERBIVORE_FORM) {
            canvas.draw(herbivorePrimary, 7, canvas.getHeight() - 82);
            canvas.draw(outlinePrimary, 7, canvas.getHeight() - 82);
            canvas.draw(dollSecondary, 70, canvas.getHeight() - 76);
            canvas.draw(outlineSecondary, 70, canvas.getHeight() - 76);
            canvas.draw(carnivoreSecondary, 54, canvas.getHeight() - 92);
            canvas.draw(outlineSecondary, 54, canvas.getHeight() - 92);
        }
        else {
            canvas.draw(carnivorePrimary, 7, canvas.getHeight() - 82);
            canvas.draw(outlinePrimary, 7, canvas.getHeight() - 82);
            canvas.draw(dollSecondary, 70, canvas.getHeight() - 76);
            canvas.draw(outlineSecondary, 70, canvas.getHeight() - 76);
            canvas.draw(herbivoreSecondary, 54, canvas.getHeight() - 92);
            canvas.draw(outlineSecondary, 54, canvas.getHeight() - 92);;
        }
    }

    private void drawDinoMeter(Canvas canvas) {
        int padding = 18;
        int rootX = 120 + padding;
        float width = dinometerBackground.getRegionWidth() - 2 * padding;

        TextureRegion resource, lightResource;
        if (transformation == Dinosaur.DOLL_FORM) {
            resource = cotton;
            lightResource = cottonLight;
        }
        else if (transformation == Dinosaur.HERBIVORE_FORM) {
            resource = leaf;
            lightResource = leafLight;
        }
        else {
            resource = meat;
            lightResource = meatLight;
        }

        int offsetY = 67 - (dinometerBackground.getRegionHeight() - resource.getRegionHeight())/2;
        //canvas.draw(dinometerBackground, 120, canvas.getHeight() - 67);
        for (int i = 0; i < numResources; i++)
            canvas.draw(resource, rootX + (1 + i * 2) * width/6 - resource.getRegionWidth()/2,
                    canvas.getHeight() - offsetY + 5f);
        for (int i = numResources; i < Dinosaur.MAX_RESOURCES; i++)
            canvas.draw(lightResource, rootX + (1 + i * 2) * width/6 - lightResource.getRegionWidth()/2,
                    canvas.getHeight() - offsetY + 5f);

    }

    private void drawPause(Canvas canvas) {
        //canvas.draw(pauseBackground, 1213, canvas.getHeight() - 67);
        Vector2 origin = new Vector2(wood.getRegionWidth()/2.0f, wood.getRegionHeight()/2.0f);
        canvas.draw(wood, Color.WHITE, origin.x, origin.y, canvas.getWidth() + 80.0f,canvas.getHeight() - 5.0f
                ,0,0.75f,0.75f);
        canvas.draw(pauseLight, 1229, canvas.getHeight() - 54);
    }

    private void drawCloneCircle(Canvas canvas){
        FilmStrip fs = new FilmStrip(cloneCircle,1,12,12);
        Vector2 origin = new Vector2(fs.getRegionWidth()/2.0f, fs.getRegionHeight()/2.0f);
        int frame =  (int) ((cloneTime / 60.0f) * (float) 11);
        fs.setFrame(frame);

        Color color = Color.WHITE;

        if (cloneTime >= 60.0f){
            color = new Color(0.75f,0.75f,0.75f,1);
        }

        canvas.draw(fs, color,origin.x,origin.y, 45.0f,
                canvas.getHeight()/2.0f + 215.0f,0,0.10f,0.10f);
        origin = new Vector2(cloneImage.getRegionWidth()/2.0f, cloneImage.getRegionHeight()/2.0f);
        canvas.draw(cloneImage, color, origin.x, origin.y, 45.0f,canvas.getHeight()/2.0f + 230.0f
                ,0,.75f,.75f);

    }

    private void drawLevelTimer(Canvas canvas, float totalTime ) {
        Vector2 origin = new Vector2(clock.getRegionWidth()/2.0f, clock.getRegionHeight()/2.0f);
        canvas.draw(clock, Color.WHITE, origin.x, origin.y, 165.0f,canvas.getHeight() - 78.0f
                ,0,0.20f,0.20f);

        int minutes = (int) totalTime / 60;
        int seconds = (int) (totalTime % 60);
        displayFont.setColor(Color.WHITE);
        if (seconds < 10) {
            canvas.drawText(Integer.toString(minutes) + ":0" + Integer.toString(seconds), displayFont,
                    190f, canvas.getHeight() - 62f);
        } else if (seconds == 60) {
            canvas.drawText(Integer.toString(minutes + 1) + ":00", displayFont,190f,
                    canvas.getHeight() - 65f);
        } else {
            canvas.drawText(Integer.toString(minutes) + ":" + Integer.toString(seconds), displayFont,
                    190f, canvas.getHeight() - 65f);
        }
    }

    public void flashResourceBar(){
        if (levelTimerCount > 0){
            stopFlashTime = levelTimerCount - 1f;
        } else {
            stopFlashTime = 0;
        }
        flashResourceRed = true;
    }
}
