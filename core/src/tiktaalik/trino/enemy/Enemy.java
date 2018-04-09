package tiktaalik.trino.enemy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import tiktaalik.trino.Canvas;
import tiktaalik.trino.GameObject;
import tiktaalik.trino.duggi.Dinosaur;

public class Enemy extends GameObject {
    private final float COLLIDE_RESET_DURATION = 1.0f;
    private final float STUN_DURATION = 4.0f;

    private TextureRegion[] textureSet;

    protected CircleShape shape; // Shape information for this circle
    private Fixture geometry; // A cache value for the fixture (for resizing)

    private float collideCooldown;
    private float stunCooldown;
    private boolean faceRight;
    private boolean faceUp;
    private boolean collided;
    private boolean stunned;
    private int direction;
    private Vector2 gridLocation;


    /**
     * Creates a new dinosaur at the given position.
     *
     * @param x  		Initial x position of the avatar center
     * @param y  		Initial y position of the avatar center
     * @param radius	The object radius in physics units
     */
    public Enemy(float x, float y, float radius, int id) {
        super(x,y);
        setDensity(100.0f);
        setFriction(0.0f);
        setName("enemy");

        shape = new CircleShape();
        shape.setRadius(radius * 4/5);

        // Gameplay attributes
        textureSet = new TextureRegion[4];
        faceRight = true;
        faceUp = false;
        stunned = false;
        collided = false;
    }

    public void setTextureSet(TextureRegion left, TextureRegion right, TextureRegion up, TextureRegion down) {
        textureSet[Dinosaur.LEFT] = left;
        textureSet[Dinosaur.RIGHT] = right;
        textureSet[Dinosaur.UP] = up;
        textureSet[Dinosaur.DOWN] = down;
    }

    public void setCollided(boolean collided) {
        if ((collided && collideCooldown <= 0) || !collided)
            this.collided = collided;
    }

    public boolean getCollided() {
        return collided;
    }

    public void setStunned() {
        stunned = true;
        setLinearDamping(11);
        stunCooldown = 0;
    }

    public boolean getStunned(){
        return stunned;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(float value) {
        if (value == 0) {
            direction = Dinosaur.LEFT;
        } else if (value == 1) {
            direction = Dinosaur.RIGHT;
        } else if (value == 2) {
            direction = Dinosaur.UP;
        } else if (value == 3) {
            direction = Dinosaur.DOWN;
        }
    }


    public void setGridLocation(Vector2 location){
        this.gridLocation = location;
    }

    public void setGridLocation(float x, float y) {
        gridLocation.x = x;
        gridLocation.y = y;
    }

    public Vector2 getGridLocation(){
        return gridLocation;
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
        filter.categoryBits = 0x0004;
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

        if (collideCooldown > 0)
            collideCooldown += dt;

        if (stunned) {
            if (getLinearVelocity().len2() < 5)
                setBodyType(BodyDef.BodyType.StaticBody);

            stunCooldown += dt;
            if (stunCooldown > STUN_DURATION) {
                setBodyType(BodyDef.BodyType.DynamicBody);
                setLinearDamping(0);
                stunned = false;
            }
        } else {
            setTexture(textureSet[direction]);
        }
    }

    /**
     * Draws the outline of the physics body.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(Canvas canvas) {
        canvas.drawPhysics(shape,Color.RED,getX(),getY(),drawScale.x,drawScale.y);
    }
}

