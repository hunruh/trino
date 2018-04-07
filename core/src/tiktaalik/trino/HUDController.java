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
    private static final String COTTON_RESOURCE_FILE = "trino/cotton_resource.png";
    private static final String MEAT_RESOURCE_FILE = "trino/meat_resource.png";
    private static final String VEGGIE_RESOURCE_FILE = "trino/veggie_resource.png";

    private TextureRegion cottonResource;
    private TextureRegion meatResource;
    private TextureRegion veggieResource;

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

        manager.load(COTTON_RESOURCE_FILE,Texture.class);
        assets.add(COTTON_RESOURCE_FILE);
        manager.load(MEAT_RESOURCE_FILE,Texture.class);
        assets.add(MEAT_RESOURCE_FILE);
        manager.load(VEGGIE_RESOURCE_FILE,Texture.class);
        assets.add(VEGGIE_RESOURCE_FILE);
    }

    public void loadContent(AssetManager manager) {
        if (hudAssetState != GameController.AssetState.LOADING) {
            return;
        }

        cottonResource = createTexture(manager,COTTON_RESOURCE_FILE,true);
        meatResource = createTexture(manager,MEAT_RESOURCE_FILE,true);
        veggieResource = createTexture(manager,VEGGIE_RESOURCE_FILE,true);

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
        cottonResource = null;
        meatResource = null;
        veggieResource = null;
    }

    public void update(int numResources, int transformation) {
        this.numResources = numResources;
        this.transformation = transformation;
    }

    public void draw() {
        canvas.beginOverlay();
        drawDinoMeter(canvas);
        canvas.end();
    }

    private void drawDinoMeter(Canvas canvas) {
        TextureRegion resource;
        if (transformation == Dinosaur.DOLL_FORM)
            resource = cottonResource;
        else if (transformation == Dinosaur.HERBIVORE_FORM)
            resource = veggieResource;
        else
            resource = meatResource;

        for (int i = 0; i < numResources; i++)
            canvas.draw(resource, Color.WHITE, 13 + (60 * i), canvas.getHeight() - 73, 60, 60);
    }
}
