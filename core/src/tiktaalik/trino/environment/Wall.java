package tiktaalik.trino.environment;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import tiktaalik.trino.*;
import tiktaalik.trino.duggi.Dinosaur;
import tiktaalik.util.FilmStrip;

public class Wall extends EdibleObject {
    protected PolygonShape shape; // Shape information for this box
    private Vector2 dimension; // The width and height of the box
    private Vector2 sizeCache; // A cache value for when the user wants to access the dimensions
    private Fixture geometry; // A cache value for the fixture (for resizing)
    private float[] vertices; // Cache of the polygon vertices (for resizing)
    private Vector2 gridLocation;

    private boolean edible;
    private boolean lowered = false;
    private boolean goal;

    private static final float ANIMATION_SPEED = 0.175f;
    private FilmStrip[] textureSet;
    private int numFrames[];
    private float animeframe;

    private static final int VINE_DROP = 0;
    private static final int DOOR = 1;

    /**
     * Creates a new dinosaur at the given position.
     *
     * @param x  		Initial x position of the avatar center
     * @param y  		Initial y position of the avatar center
     * @param width	    The object width in physics units
     * @param height	The object height in physics units
     * @param edible	If the wall can be consumed by the herbivore
     */
    public Wall(int gx, int gy, float x, float y, float width, float height, boolean edible) {
        super(x,y);
        dimension = new Vector2(width,height);
        sizeCache = new Vector2();
        shape = new PolygonShape();
        vertices = new float[8];
        geometry = null;

        this.edible = edible;

        // Initialize
        setBodyType(BodyDef.BodyType.StaticBody);
        setName("wall");
        resize(width, height);


        gridLocation = new Vector2(gx, gy);

        textureSet = new FilmStrip[2];
        numFrames = new int[2];
        animeframe = 0;
    }

    public boolean getLowered() { return lowered; }

    public boolean getAnimLowered() {
        return lowered && animeframe == numFrames[DOOR] - 1;
    }

    public void setLowered(boolean lowered) {
        this.lowered = lowered;

    }

    public boolean getGoal() { return goal; }

    public void setGoal(boolean goal) { this.goal = goal; }

    /**
     * Sets the dimensions of this box
     *
     * @param value  the dimensions of this box
     */
    public void setDimension(Vector2 value) {
        setDimension(value.x, value.y);
    }

    /**
     * Sets the dimensions of this box
     *
     * @param width   The width of this box
     * @param height  The height of this box
     */
    public void setDimension(float width, float height) {
        dimension.set(width, height);
        markDirty(true);
        resize(width, height);
    }

    /**
     * Returns the box width
     *
     * @return the box width
     */
    public float getWidth() {
        return dimension.x;
    }

    /**
     * Sets the box width
     *
     * @param value  the box width
     */
    public void setWidth(float value) {
        sizeCache.set(value,dimension.y);
        setDimension(sizeCache);
    }

    /**
     * Returns the box height
     *
     * @return the box height
     */
    public float getHeight() {
        return dimension.y;
    }

    /**
     * Sets the box height
     *
     * @param value  the box height
     */
    public void setHeight(float value) {
        sizeCache.set(dimension.x,value);
        setDimension(sizeCache);
    }

    public void setVineTextureSet(Texture vine, int vineFrames) {
        numFrames[VINE_DROP] = vineFrames;
        textureSet[VINE_DROP] = new FilmStrip(vine,1,vineFrames,vineFrames);
    }

    public void setDoorTextureSet(Texture door, int frames) {
        numFrames[DOOR] = frames;
        textureSet[DOOR] = new FilmStrip(door, 1, frames, frames);
    }

    public Vector2 getGridLocation(){
        return gridLocation;
    }
    /**
     * Reset the polygon vertices in the shape to match the dimension.
     */
    private void resize(float width, float height) {
        // Make the box with the center in the center
        if (edible) {
            vertices[0] = -width/2.5f;
            vertices[1] = -height/2.0f;
            vertices[2] = -width/2.5f;
            vertices[3] =  height/1.2f;
        } else {
            vertices[0] = -width/2.2f;
            vertices[1] = -height/2.0f;
            vertices[2] = -width/2.2f;
            vertices[3] =  height/1.2f;
        }
        vertices[4] =  width/2.2f;
        vertices[5] =  height/1.2f;
        vertices[6] =  width/2.2f;
        vertices[7] = -height/2.0f;
        shape.set(vertices);
    }

    /**
     * Create new fixtures for this body, defining the shape
     */
    protected void createFixtures() {
        if (body == null) {
            return;
        }

        releaseFixtures();

        // Create the fixture
        fixture.shape = shape;
        geometry = body.createFixture(fixture);
        Filter filter = geometry.getFilterData();
        filter.categoryBits = Dinosaur.wallCatBits;
        filter.maskBits = Dinosaur.dollCatBits|Dinosaur.herbCatBits|Dinosaur.carnCatBits|
                Dinosaur.enemyCatBits|Dinosaur.riverCatBits|Dinosaur.cloneCatBits|Dinosaur.switchCatBits|Dinosaur.enemyHerbCatBits;
        geometry.setFilterData(filter);
        markDirty(false);
    }

    /**
     * Release the fixtures for this body, reseting the shape
     */
    protected void releaseFixtures() {
        if (geometry != null) {
            body.destroyFixture(geometry);
            geometry = null;
        }
    }

    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     *
     * @param dt Number of seconds since last animation frame
     */
    public void update(float dt) {
        super.update(dt);
        if (lowered){
            Filter filter = geometry.getFilterData();
            filter.categoryBits = Dinosaur.goalCatBits;
            filter.maskBits = Dinosaur.dollCatBits|Dinosaur.herbCatBits|Dinosaur.carnCatBits;
            geometry.setFilterData(filter);
            this.setSensor(true);

            if (animeframe < numFrames[DOOR] - 1)
                animeframe += ANIMATION_SPEED;
            else
                animeframe = numFrames[DOOR] - 1;
        } else {
            Filter filter = geometry.getFilterData();
            filter.categoryBits = Dinosaur.wallCatBits;
            filter.maskBits = Dinosaur.dollCatBits|Dinosaur.herbCatBits|Dinosaur.carnCatBits|
                    Dinosaur.enemyCatBits|Dinosaur.riverCatBits|Dinosaur.cloneCatBits|Dinosaur.switchCatBits;
            geometry.setFilterData(filter);
            this.setSensor(false);

            if (animeframe > 0)
                animeframe -= ANIMATION_SPEED;
            else
                animeframe = 0;
        }

    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(Canvas canvas) {
        if (textureSet[DOOR] != null) {
            System.out.println((int)animeframe);
            textureSet[DOOR].setFrame((int)animeframe);
            canvas.draw(textureSet[DOOR], Color.WHITE, origin.x, origin.y, getX() * drawScale.x - 7,
                    getY() * drawScale.x - 7, 0, 1, 1);
        } else
            super.draw(canvas, 0, 7, edible);
    }

    /**
     * Draws the outline of the physics body.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(Canvas canvas) {
        canvas.drawPhysics(shape,Color.RED,getX(),getY()/2,0,drawScale.x,drawScale.y);
    }
}
