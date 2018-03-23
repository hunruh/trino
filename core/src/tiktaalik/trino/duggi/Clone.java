package tiktaalik.trino.duggi;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import tiktaalik.trino.Canvas;
import tiktaalik.trino.GameObject;

public class Clone extends GameObject {
    protected CircleShape shape; // Shape information for this circle
    private Fixture geometry; // A cache value for thefixture (for resizing)

    private boolean alive;
    private Vector2 gridLocation;

    public Clone(float radius) {
        this(0, 0, radius);
    }

    public Clone(float x, float y, float radius) {
        super(x,y);
        setDensity(1.0f);
        setFriction(0.0f);
        setName("clone");
        setBodyType(BodyDef.BodyType.StaticBody);

        shape = new CircleShape();
        shape.setRadius(radius * 4/5);
        gridLocation = new Vector2();
    }

    public boolean getAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public void update(float dt) {
        super.update(dt);
    }

    public Vector2 getGridLocation() {
        return gridLocation;
    }

    public void setGridLocation(Vector2 location) {
        gridLocation = location;
    }

    public void setGridLocation(float x, float y) {
        gridLocation.x = x;
        gridLocation.y = y;
    }

    public void setLocation(float x, float y) {
        setX(x);
        setY(y);
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
     * Draws the outline of the physics body.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(Canvas canvas) {
        canvas.drawPhysics(shape, Color.RED,getX(),getY(),drawScale.x,drawScale.y);
    }
}
