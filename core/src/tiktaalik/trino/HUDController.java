package tiktaalik.trino;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import tiktaalik.trino.duggi.Dinosaur;

public class HUDController  {
    private GameController.AssetState hudAssetState = GameController.AssetState.EMPTY;
    private Array<String> assets;

    // Textures necessary to support the loading screen
    private static final String BAR_FILE = "hud/bar.png";
    private static final String DINOMETER_TITLE_FILE = "hud/dinometer_title.png";
    private static final String DINOMETER_OUTLINE_FILE = "hud/dinometer_outline.png";
    private static final String COTTON_FILE = "hud/cotton.png";
    private static final String COTTON_LIGHT_FILE = "hud/cotton_light.png";
    private static final String MEAT_FILE = "hud/meat.png";
    private static final String MEAT_LIGHT_FILE = "hud/meat_light.png";
    private static final String VEGGIE_FILE = "hud/veggie.png";
    private static final String VEGGIE_LIGHT_FILE = "hud/veggie_light.png";
    private static final String PAUSE_FILE = "hud/pause_symbol.png";
    private static final String PAUSE_OUTLINE_FILE = "hud/pause_outline.png";
    private static final String CURRENT_FORM_OUTLINE_FILE = "hud/current_form_outline.png";
    private static final String DOLL_FILE = "hud/small_doll.png";
    private static final String CARNIVORE_FILE = "hud/small_carn.png";
    private static final String HERBIVORE_FILE = "hud/small_herb.png";

    private TextureRegion bar;
    private TextureRegion dinometerTitle;
    private TextureRegion dinometerOutline;
    private TextureRegion cotton;
    private TextureRegion cottonLight;
    private TextureRegion meat;
    private TextureRegion meatLight;
    private TextureRegion veggie;
    private TextureRegion veggieLight;
    private TextureRegion pause;
    private TextureRegion pauseOutline;
    private TextureRegion currentFormOutline;
    private TextureRegion doll;
    private TextureRegion carnivore;
    private TextureRegion herbivore;

    private Canvas canvas; // Reference to Canvas created by the root

    private int numResources;
    private int transformation;

    public HUDController() {
        assets = new Array<String>();

        numResources = 0;
        transformation = Dinosaur.DOLL_FORM;
    }

    public void preLoadContent(AssetManager manager) {
        if (hudAssetState != GameController.AssetState.EMPTY) {
            return;
        }

        hudAssetState = GameController.AssetState.LOADING;

        manager.load(BAR_FILE,Texture.class);
        assets.add(BAR_FILE);
        manager.load(DINOMETER_TITLE_FILE,Texture.class);
        assets.add(DINOMETER_TITLE_FILE);
        manager.load(DINOMETER_OUTLINE_FILE,Texture.class);
        assets.add(DINOMETER_OUTLINE_FILE);
        manager.load(COTTON_FILE,Texture.class);
        assets.add(COTTON_FILE);
        manager.load(COTTON_LIGHT_FILE,Texture.class);
        assets.add(COTTON_LIGHT_FILE);
        manager.load(MEAT_FILE,Texture.class);
        assets.add(MEAT_FILE);
        manager.load(MEAT_LIGHT_FILE,Texture.class);
        assets.add(MEAT_LIGHT_FILE);
        manager.load(VEGGIE_FILE,Texture.class);
        assets.add(VEGGIE_FILE);
        manager.load(VEGGIE_LIGHT_FILE,Texture.class);
        assets.add(VEGGIE_LIGHT_FILE);
        manager.load(PAUSE_FILE,Texture.class);
        assets.add(PAUSE_FILE);
        manager.load(PAUSE_OUTLINE_FILE,Texture.class);
        assets.add(PAUSE_OUTLINE_FILE);
        manager.load(CURRENT_FORM_OUTLINE_FILE,Texture.class);
        assets.add(CURRENT_FORM_OUTLINE_FILE);
        manager.load(DOLL_FILE,Texture.class);
        assets.add(DOLL_FILE);
        manager.load(CARNIVORE_FILE,Texture.class);
        assets.add(CARNIVORE_FILE);
        manager.load(HERBIVORE_FILE,Texture.class);
        assets.add(HERBIVORE_FILE);
    }

    public void loadContent(AssetManager manager) {
        if (hudAssetState != GameController.AssetState.LOADING) {
            return;
        }

        bar = createTexture(manager,BAR_FILE,true);
        dinometerTitle = createTexture(manager,DINOMETER_TITLE_FILE,true);
        dinometerOutline = createTexture(manager,DINOMETER_OUTLINE_FILE,true);
        cotton = createTexture(manager,COTTON_FILE,true);
        cottonLight = createTexture(manager,COTTON_LIGHT_FILE,true);
        meat = createTexture(manager,MEAT_FILE,true);
        meatLight = createTexture(manager,MEAT_LIGHT_FILE,true);
        veggie = createTexture(manager,VEGGIE_FILE,true);
        veggieLight = createTexture(manager,VEGGIE_LIGHT_FILE,true);
        pause = createTexture(manager,PAUSE_FILE,true);
        pauseOutline = createTexture(manager,PAUSE_OUTLINE_FILE,true);
        currentFormOutline = createTexture(manager,CURRENT_FORM_OUTLINE_FILE,true);
        doll = createTexture(manager,DOLL_FILE,true);
        carnivore = createTexture(manager,CARNIVORE_FILE,true);
        herbivore = createTexture(manager,HERBIVORE_FILE,true);

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
        bar = null;
        dinometerTitle = null;
        dinometerOutline = null;
        cotton = null;
        cottonLight = null;
        meat = null;
        meatLight = null;
        veggie = null;
        veggieLight = null;
        pause = null;
        pauseOutline = null;
        currentFormOutline = null;
        doll = null;
        carnivore = null;
        herbivore = null;
    }

    public void update(int numResources, int transformation) {
        this.numResources = numResources;
        this.transformation = transformation;
    }

    public void draw() {
        canvas.beginOverlay();
        canvas.draw(bar, 0, canvas.getHeight() - 80);
        drawForm(canvas);
        drawDinoMeter(canvas);
        drawPause(canvas);
        canvas.end();
    }

    private void drawForm(Canvas canvas) {
        TextureRegion form;

        if (transformation == Dinosaur.DOLL_FORM)
            form = doll;
        else if (transformation == Dinosaur.HERBIVORE_FORM)
            form = herbivore;
        else
            form = carnivore;

        int x = 56 + (currentFormOutline.getRegionWidth() - form.getRegionWidth())/2;
        int y = 76 - (currentFormOutline.getRegionHeight() - form.getRegionHeight())/2;
        canvas.draw(form, x, canvas.getHeight() - y);
        canvas.draw(currentFormOutline, 56, canvas.getHeight() - 76);
    }

    private void drawDinoMeter(Canvas canvas) {
        TextureRegion resource, lightResource;
        if (transformation == Dinosaur.DOLL_FORM) {
            resource = cotton;
            lightResource = cottonLight;
        }
        else if (transformation == Dinosaur.HERBIVORE_FORM) {
            resource = veggie;
            lightResource = veggieLight;
        }
        else {
            resource = meat;
            lightResource = meatLight;
        }

        int offsetY = 76 - (dinometerOutline.getRegionHeight() - resource.getRegionHeight())/2;
        canvas.draw(dinometerTitle, 170, canvas.getHeight() - 62);
        canvas.draw(dinometerOutline, 366, canvas.getHeight() - 76);
        for (int i = 0; i < numResources; i++)
            canvas.draw(resource, 380 + (40 * i), canvas.getHeight() - offsetY);
        for (int i = numResources; i < Dinosaur.MAX_RESOURCES; i++)
            canvas.draw(lightResource, 380 + (40 * i), canvas.getHeight() - offsetY);

    }

    private void drawPause(Canvas canvas) {
        int x = 1156 + (pauseOutline.getRegionWidth() - pause.getRegionWidth())/2;
        int y = 76 - (pauseOutline.getRegionHeight() - pause.getRegionHeight())/2;
        canvas.draw(pauseOutline, 1156, canvas.getHeight() - 76);
        canvas.draw(pause, x, canvas.getHeight() - y);
    }
}
