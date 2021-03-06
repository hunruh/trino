package tiktaalik.trino.environment;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import tiktaalik.trino.Canvas;
import tiktaalik.trino.GameObject;
import tiktaalik.trino.duggi.Dinosaur;

public class River extends GameObject {
    protected PolygonShape shape; // Shape information for this box
    private Vector2 dimension; // The width and height of the box
    private Vector2 sizeCache; // A cache value for when the user wants to access the dimensions
    private Fixture geometry; // A cache value for the fixture (for resizing)
    private float[] vertices; // Cache of the polygon vertices (for resizing)
    private Vector2 gridLocation;

    private boolean edible;
    private boolean lowered;
    private boolean isCenterTile = false;
    private boolean isTopRiver;
    private boolean isBotRiver;
    private boolean isRightRiver;
    private boolean isLeftRiver;
    private boolean hasRockOnIt;
    private TextureRegion rock;
    private Vector2 rockPosition;

    /**
     * Creates a new dinosaur at the origin.
     *
     * @param width	    The object width in physics units
     * @param height	The object height in physics units
     * @param edible	If the wall can be consumed by the herbivore
     */
    public River(float width, float height, boolean edible) {
        this(0,0,0,0,width,height, edible);
    }

    /**
     * Creates a new dinosaur at the given position.
     *
     * @param x  		Initial x position of the avatar center
     * @param y  		Initial y position of the avatar center
     * @param width	    The object width in physics units
     * @param height	The object height in physics units
     * @param edible	If the wall can be consumed by the herbivore
     */
    public River(int gx, int gy, float x, float y, float width, float height, boolean edible) {
        super(x,y);
        dimension = new Vector2(width,height);
        sizeCache = new Vector2();
        shape = new PolygonShape();
        vertices = new float[8];
        geometry = null;

        // Initialize
        setBodyType(BodyDef.BodyType.StaticBody);
        setName("wall");
        resize(width, height);

        this.edible = edible;
        gridLocation = new Vector2(gx, gy);

        // Has rock on it
        int random = MathUtils.random(2);
        if (random < 2){
            hasRockOnIt = true;
        }
    }

    public boolean getEdible() {
        return edible;
    }

    public boolean getLowered() { return lowered; }
    public void setLowered(boolean lowered) { this.lowered = lowered; }
    public void setCenterTile(boolean center){this.isCenterTile = center;}
    public boolean getisCenterTile() {return isCenterTile;}
    public void setTop(boolean value){isTopRiver = value;}
    public void setBot(boolean value){isBotRiver = value;}
    public void setRight(boolean value){isRightRiver = value;}
    public void setLeft(boolean value){isLeftRiver = value;}
    public boolean getIsTopRiver(){return isTopRiver;}
    public boolean getIsBotRiver(){return isBotRiver;}
    public boolean getIsRightRiver(){return isRightRiver;}
    public boolean getIsLeftRiver(){return isLeftRiver;}
    public void setRock(TextureRegion rock){ this.rock = rock;}
    public TextureRegion getRock(){return rock;}
    public void setRockPosition(Vector2 position){rockPosition = position;}
    public Vector2 getRockPosition(){return rockPosition;}
    public void setHasRockOnIt(boolean value){this.hasRockOnIt = value;}
    public boolean getHasRockOnit() {return hasRockOnIt;}

    /**
     * Returns the dimensions of this box
     *
     * @return the dimensions of this box
     */
    public Vector2 getDimension() {
        return sizeCache.set(dimension);
    }

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

    public void setGridLocation(Vector2 location){
        this.gridLocation = location;
    }

    public Vector2 getGridLocation(){
        return gridLocation;
    }
    /**
     * Reset the polygon vertices in the shape to match the dimension.
     */
    private void resize(float width, float height) {
        // Make the box with the center in the center
        vertices[0] = -width/2.1f;
        vertices[1] = -height/2.5f;
        vertices[2] = -width/2.1f;
        vertices[3] =  height/1.2f;
        vertices[4] =  width/2.1f;
        vertices[5] =  height/1.2f;
        vertices[6] =  width/2.1f;
        vertices[7] = -height/2.5f;
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
        filter.categoryBits = Dinosaur.riverCatBits;
        filter.maskBits = Dinosaur.dollCatBits|Dinosaur.carnCatBits|Dinosaur.enemyCatBits|
                Dinosaur.cloneCatBits|Dinosaur.wallCatBits;

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
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(Canvas canvas) {
        if (texture != null) {
            canvas.draw(texture, Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.x + 12,0,1,1);
        }
    }

    /**
     * Draws the outline of the physics body.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(Canvas canvas) {
        canvas.drawPhysics(shape,Color.RED,getX(),getY(),0,drawScale.x,drawScale.y);
    }
}
