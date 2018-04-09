package tiktaalik.trino.environment;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import tiktaalik.trino.Canvas;
import tiktaalik.trino.GameObject;

public class Wall extends GameObject {
    protected PolygonShape shape; // Shape information for this box
    private Vector2 dimension; // The width and height of the box
    private Vector2 sizeCache; // A cache value for when the user wants to access the dimensions
    private Fixture geometry; // A cache value for the fixture (for resizing)
    private float[] vertices; // Cache of the polygon vertices (for resizing)
    private Vector2 gridLocation;

    private boolean edible;
    private boolean lowered;

    /**
     * Creates a new dinosaur at the origin.
     *
     * @param width	    The object width in physics units
     * @param height	The object height in physics units
     * @param edible	If the wall can be consumed by the herbivore
     */
    public Wall(float width, float height, boolean edible) {
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
    public Wall(int gx, int gy, float x, float y, float width, float height, boolean edible) {
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
    }

    public boolean getEdible() {
        return edible;
    }

    public boolean getLowered() { return lowered; }
    public void setLowered(boolean lowered) { this.lowered = lowered; }

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
        vertices[0] = -width/2.0f;
        vertices[1] = -height/2.0f;
        vertices[2] = -width/2.0f;
        vertices[3] =  height/2.0f;
        vertices[4] =  width/2.0f;
        vertices[5] =  height/2.0f;
        vertices[6] =  width/2.0f;
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
        filter.groupIndex = -8;
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
            canvas.draw(texture, Color.WHITE,origin.x,origin.y + 7,getX()*drawScale.x,getY()*drawScale.x,0,1,1);
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
