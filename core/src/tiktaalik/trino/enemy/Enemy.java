package tiktaalik.trino.enemy;

import static tiktaalik.trino.duggi.Dinosaur.*;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import tiktaalik.trino.Canvas;
import tiktaalik.trino.GameObject;
import tiktaalik.trino.duggi.Dinosaur;
import tiktaalik.util.FilmStrip;

public class Enemy extends GameObject {
    private final float STUN_DURATION = 4.0f;

    private static final float ANIMATION_SPEED = 0.175f;
    private FilmStrip[] textureSet;

    protected CircleShape shape; // Shape information for this circle
    private Fixture geometry; // A cache value for the fixture (for resizing)

    private int numFrames[];
    private float animeframe;
    private float collideCooldown;
    private float stunCooldown;
    private boolean faceRight;
    private boolean faceUp;
    private boolean collided;
    private boolean stunned;
    private boolean coolingCharge;
    private boolean loadingCharge;
    private int direction;
    private Vector2 gridLocation;
    private boolean charging;
    private boolean chargeReady;
    private final float CHARGE_COOLDOWN_DURATION = 0.5f;
    private final float CHARGE_LOAD_DURATION = 1.0f;
    private float chargeCooldown;
    private float chargeLoad;


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
        textureSet = new FilmStrip[4];
        numFrames = new int[4];
        animeframe = 0;
        faceRight = true;
        faceUp = false;
        stunned = false;
        collided = false;
        charging = false;
        coolingCharge = false;
        chargeReady = false;
    }

    public void setTextureSet(Texture left, int leftFrames, Texture right, int rightFrames, Texture up, int upFrames,
                              Texture down, int downFrames) {
        numFrames[LEFT] = leftFrames;
        textureSet[LEFT] = new FilmStrip(left,1,leftFrames,leftFrames);
        numFrames[RIGHT] = rightFrames;
        textureSet[RIGHT] = new FilmStrip(right,1,rightFrames,rightFrames);
        numFrames[UP] = upFrames;
        textureSet[UP] = new FilmStrip(up,1,upFrames,upFrames);
        numFrames[DOWN] = downFrames;
        textureSet[DOWN] = new FilmStrip(down,1,downFrames,downFrames);
        origin = new Vector2(textureSet[LEFT].getRegionWidth()/2.0f, textureSet[LEFT].getRegionHeight()/2.0f);
    }

    public void setCollided(boolean collided) {
        if ((collided && collideCooldown <= 0) || !collided)
            this.collided = collided;
    }

    public boolean getCollided() {
        return collided;
    }

    public boolean getCharging() {
        return charging;
    }

    public void setCharging(boolean charging) {
        this.charging = charging;
        if (!charging) {
            coolingCharge = true;
        }
    }

    public void setStunned() {
        stunned = true;
        setLinearDamping(11);
        stunCooldown = 0;
    }

    public void loadCharge() {
        if (!coolingCharge)
            loadingCharge = true;
    }

    public boolean getLoadingCharge() {
        return loadingCharge;
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

        if (loadingCharge) {
            chargeLoad += dt;
            if (chargeLoad >= CHARGE_LOAD_DURATION) {
                setCharging(true);
                loadingCharge = false;
                chargeLoad = 0;
            }
        } else if (coolingCharge && !stunned) {
            chargeCooldown += dt;
            if (chargeCooldown >= CHARGE_COOLDOWN_DURATION) {
                coolingCharge = false;
                chargeCooldown = 0.0f;
            }
        }

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
            animeframe += ANIMATION_SPEED;

            if (animeframe >= numFrames[direction]) {
                animeframe -= numFrames[direction];
            }
        }
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(Canvas canvas) {
        textureSet[direction].setFrame((int)animeframe);
        if (textureSet[direction] != null) {
            canvas.draw(textureSet[direction], Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.x,0,1,1);
        }
    }

    public void drawShadow(Canvas canvas) {
        canvas.drawShadow(shape,getX()*drawScale.x,getY()*drawScale.x,drawScale.x);
//        canvas.drawShadow(shape,getX()*drawScale.x,getY()*drawScale.x-9,drawScale.x);
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

