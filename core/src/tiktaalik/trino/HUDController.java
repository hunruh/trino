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
    private static final String ONE_FILE = "hud/1.png";
    private static final String TWO_FILE = "hud/2.png";
    private static final String THREE_FILE = "hud/3.png";
    private static final String BAR_FILE = "hud/bar.png";
    private static final String DINOMETER_OUTLINE_FILE = "hud/dino_meter_outline.png";
    private static final String COTTON_FILE = "hud/cotton.png";
    private static final String COTTON_LIGHT_FILE = "hud/cotton_light.png";
    private static final String MEAT_FILE = "hud/meat.png";
    private static final String MEAT_LIGHT_FILE = "hud/meat_light.png";
    private static final String LEAF_FILE = "hud/leaf.png";
    private static final String LEAF_LIGHT_FILE = "hud/leaf_light.png";
    private static final String PAUSE_SYMBOL_LIGHT_FILE = "hud/pause_symbol_light.png";
    private static final String PAUSE_SYMBOL_DARK_FILE = "hud/pause_symbol_dark.png";
    private static final String PAUSE_OUTLINE_FILE = "hud/pause_outline.png";
    private static final String CURRENT_FORM_OUTLINE_FILE = "hud/current_form_outline.png";
    private static final String DOLL_FILE = "hud/doll.png";
    private static final String DOLL_TINY_FILE = "hud/doll_tiny.png";
    private static final String CARNIVORE_FILE = "hud/carn.png";
    private static final String CARNIVORE_TINY_FILE = "hud/carn_tiny.png";
    private static final String HERBIVORE_FILE = "hud/herb.png";
    private static final String HERBIVORE_TINY_FILE = "hud/herb_tiny.png";

    private TextureRegion one;
    private TextureRegion two;
    private TextureRegion three;
    private TextureRegion bar;
    private TextureRegion dinometerOutline;
    private TextureRegion cotton;
    private TextureRegion cottonLight;
    private TextureRegion meat;
    private TextureRegion meatLight;
    private TextureRegion leaf;
    private TextureRegion leafLight;
    private TextureRegion pauseSymbolDark;
    private TextureRegion pauseSymbolLight;
    private TextureRegion pauseOutline;
    private TextureRegion currentFormOutline;
    private TextureRegion doll;
    private TextureRegion dollTiny;
    private TextureRegion carnivore;
    private TextureRegion carnivoreTiny;
    private TextureRegion herbivore;
    private TextureRegion herbivoreTiny;

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

        manager.load(ONE_FILE,Texture.class);
        assets.add(ONE_FILE);
        manager.load(TWO_FILE,Texture.class);
        assets.add(TWO_FILE);
        manager.load(THREE_FILE,Texture.class);
        assets.add(THREE_FILE);
        manager.load(BAR_FILE,Texture.class);
        assets.add(BAR_FILE);
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
        manager.load(LEAF_FILE,Texture.class);
        assets.add(LEAF_FILE);
        manager.load(LEAF_LIGHT_FILE,Texture.class);
        assets.add(LEAF_LIGHT_FILE);
        manager.load(PAUSE_SYMBOL_DARK_FILE,Texture.class);
        assets.add(PAUSE_SYMBOL_DARK_FILE);
        manager.load(PAUSE_SYMBOL_LIGHT_FILE,Texture.class);
        assets.add(PAUSE_SYMBOL_LIGHT_FILE);
        manager.load(PAUSE_OUTLINE_FILE,Texture.class);
        assets.add(PAUSE_OUTLINE_FILE);
        manager.load(CURRENT_FORM_OUTLINE_FILE,Texture.class);
        assets.add(CURRENT_FORM_OUTLINE_FILE);
        manager.load(DOLL_FILE,Texture.class);
        assets.add(DOLL_FILE);
        manager.load(DOLL_TINY_FILE,Texture.class);
        assets.add(DOLL_TINY_FILE);
        manager.load(CARNIVORE_FILE,Texture.class);
        assets.add(CARNIVORE_FILE);
        manager.load(CARNIVORE_TINY_FILE,Texture.class);
        assets.add(CARNIVORE_TINY_FILE);
        manager.load(HERBIVORE_FILE,Texture.class);
        assets.add(HERBIVORE_FILE);
        manager.load(HERBIVORE_TINY_FILE,Texture.class);
        assets.add(HERBIVORE_TINY_FILE);
    }

    public void loadContent(AssetManager manager) {
        if (hudAssetState != GameController.AssetState.LOADING) {
            return;
        }

        one = createTexture(manager,ONE_FILE,true);
        two = createTexture(manager,TWO_FILE,true);
        three = createTexture(manager,THREE_FILE,true);
        bar = createTexture(manager,BAR_FILE,true);
        dinometerOutline = createTexture(manager,DINOMETER_OUTLINE_FILE,true);
        cotton = createTexture(manager,COTTON_FILE,true);
        cottonLight = createTexture(manager,COTTON_LIGHT_FILE,true);
        meat = createTexture(manager,MEAT_FILE,true);
        meatLight = createTexture(manager,MEAT_LIGHT_FILE,true);
        leaf = createTexture(manager,LEAF_FILE,true);
        leafLight = createTexture(manager,LEAF_LIGHT_FILE,true);
        pauseSymbolDark = createTexture(manager,PAUSE_SYMBOL_DARK_FILE,true);
        pauseSymbolLight = createTexture(manager,PAUSE_SYMBOL_LIGHT_FILE,true);
        pauseOutline = createTexture(manager,PAUSE_OUTLINE_FILE,true);
        currentFormOutline = createTexture(manager,CURRENT_FORM_OUTLINE_FILE,true);
        doll = createTexture(manager,DOLL_FILE,true);
        dollTiny = createTexture(manager,DOLL_TINY_FILE,true);
        carnivore = createTexture(manager,CARNIVORE_FILE,true);
        carnivoreTiny = createTexture(manager,CARNIVORE_TINY_FILE,true);
        herbivore = createTexture(manager,HERBIVORE_FILE,true);
        herbivoreTiny = createTexture(manager,HERBIVORE_TINY_FILE,true);

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
        one = null;
        two = null;
        three = null;
        bar = null;
        dinometerOutline = null;
        cotton = null;
        cottonLight = null;
        meat = null;
        meatLight = null;
        leaf = null;
        leafLight = null;
        pauseSymbolDark = null;
        pauseSymbolLight = null;
        pauseOutline = null;
        currentFormOutline = null;
        doll = null;
        dollTiny = null;
        carnivore = null;
        carnivoreTiny = null;
        herbivore = null;
        herbivoreTiny = null;
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
        int padding = 12;
        int rootX = 59 + padding;
        float width = currentFormOutline.getRegionWidth() - 2 * padding;

        if (transformation == Dinosaur.DOLL_FORM) {
            canvas.draw(doll, rootX + width/6 - doll.getRegionWidth()/2, canvas.getHeight() - 66);
            canvas.draw(two, rootX + width/2 - one.getRegionWidth()/2, canvas.getHeight() - 31);
            canvas.draw(herbivoreTiny, rootX + width/2 - herbivoreTiny.getRegionWidth()/2, canvas.getHeight() - 66);
            canvas.draw(three, rootX + 5 * width/6 - three.getRegionWidth()/2, canvas.getHeight() - 31);
            canvas.draw(carnivoreTiny, rootX + 5 * width/6 - carnivoreTiny.getRegionWidth()/2, canvas.getHeight() - 66);
        }
        else if (transformation == Dinosaur.HERBIVORE_FORM) {
            canvas.draw(one, rootX + width/6 - one.getRegionWidth()/2, canvas.getHeight() - 31);
            canvas.draw(dollTiny, rootX + width/6 - dollTiny.getRegionWidth()/2, canvas.getHeight() - 66);
            canvas.draw(herbivore, rootX + width/2 - herbivore.getRegionWidth()/2, canvas.getHeight() - 66);
            canvas.draw(three, rootX + 5 * width/6 - three.getRegionWidth()/2, canvas.getHeight() - 31);
            canvas.draw(carnivoreTiny, rootX + 5 * width/6 - carnivoreTiny.getRegionWidth()/2, canvas.getHeight() - 66);
        }
        else {
            canvas.draw(one, rootX + width/6 - one.getRegionWidth()/2, canvas.getHeight() - 31);
            canvas.draw(dollTiny, rootX + width/6 - dollTiny.getRegionWidth()/2, canvas.getHeight() - 66);
            canvas.draw(two, rootX + width/2 - one.getRegionWidth()/2, canvas.getHeight() - 31);
            canvas.draw(herbivoreTiny, rootX + width/2 - herbivoreTiny.getRegionWidth()/2, canvas.getHeight() - 66);
            canvas.draw(carnivore, rootX + 5 * width/6 - carnivore.getRegionWidth()/2, canvas.getHeight() - 66);
        }

        canvas.draw(currentFormOutline, 59, canvas.getHeight() - 71);
    }

    private void drawDinoMeter(Canvas canvas) {
        int padding = 18;
        int rootX = 595 + padding;
        float width = dinometerOutline.getRegionWidth() - 2 * padding;

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

        int offsetY = 72 - (dinometerOutline.getRegionHeight() - resource.getRegionHeight())/2;
        canvas.draw(dinometerOutline, 595, canvas.getHeight() - 72);
        for (int i = 0; i < numResources; i++)
            canvas.draw(resource, rootX + (1 + i * 2) * width/6 - resource.getRegionWidth()/2,
                    canvas.getHeight() - offsetY);
        for (int i = numResources; i < Dinosaur.MAX_RESOURCES; i++)
            canvas.draw(lightResource, rootX + (1 + i * 2) * width/6 - lightResource.getRegionWidth()/2,
                    canvas.getHeight() - offsetY);

    }

    private void drawPause(Canvas canvas) {
        int x = 1156 + (pauseOutline.getRegionWidth() - pauseSymbolLight.getRegionWidth())/2;
        int y = 72 - (pauseOutline.getRegionHeight() - pauseSymbolLight.getRegionHeight())/2;
        canvas.draw(pauseOutline, 1156, canvas.getHeight() - 72);
        canvas.draw(pauseSymbolLight, x, canvas.getHeight() - y);
    }
}
