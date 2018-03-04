package tiktaalik.trino.duggi;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import tiktaalik.trino.GameCanvas;
import tiktaalik.trino.obstacle.CapsuleObstacle;

public class DuggiModel extends CapsuleObstacle {
    public static final int DOLL_FORM = 0;
    public static final int HERBIVORE_FORM = 1;
    public static final int CARNIVORE_FORM = 2;

    // Physics constants
    /** The density of the character */
    private static final float DUGGI_DENSITY = 1.0f;
    private static final float DUGGI_UP = 1.0f;
    /** The factor to multiply by the input */
    private static final float DUGGI_FORCE = 20.0f;
    /** The amount to slow the character down */
    private static final float DUGGI_DAMPING = 10.0f;
    /** The dude is a slippery one */
    private static final float DUGGI_FRICTION = 0.0f;
    /** The maximum character speed */
    private static final float DUGGI_MAXSPEED = 5.0f;
    /** Height of the sensor attached to the player's feet */
    private static final float SENSOR_HEIGHT = 0.05f;
    /** Identifier to allow us to track the sensor in ContactListener */
    private static final String SENSOR_NAME = "DuggiGroundSensor";

    // This is to fit the image to a tigher hitbox
    /** The amount to shrink the body fixture (vertically) relative to the image */
    private static final float DUGGI_VSHRINK = 0.95f;
    /** The amount to shrink the body fixture (horizontally) relative to the image */
    private static final float DUGGI_HSHRINK = 0.7f;
    /** The amount to shrink the sensor fixture (horizontally) relative to the image */
    private static final float DUGGI_SSHRINK = 0.6f;

    private DollModel doll = new DollModel();
    private HerbivoreModel herbivore = new HerbivoreModel();
    private CarnivoreModel carnivore = new CarnivoreModel();

    private FormModel currentForm = doll;

    /** The current horizontal movement of the character */
    private float   movement;
    /** The current vertical movement of the character */
    private float upDown;
    /** Which direction is the character facing */
    private boolean faceRight;
    /** Which direction is the character facing up*/
    private boolean faceUp;
    /** Ground sensor to represent our feet */
    private Fixture sensorFixture;
    private PolygonShape sensorShape;

    /** Cache for internal force calculations */
    private Vector2 forceCache = new Vector2();

    /**
     * Returns left/right movement of this character.
     *
     * This is the result of input times dude force.
     *
     * @return left/right movement of this character.
     */
    public float getMovement() {
        return movement;
    }

    /**
     * Returns up/down movement of this character.
     *
     * This is the result of input times dude force.
     *
     * @return up/down movement of this character.
     */
    public float getUpDown() {return upDown;}

    /**
     * Sets left/right movement of this character.
     *
     * This is the result of input times dude force.
     *
     * @param value left/right movement of this character.
     */
    public void setMovement(float value) {
        movement = value;
        // Change facing if appropriate
        if (movement < 0) {
            faceRight = false;
        } else if (movement > 0) {
            faceRight = true;
        }
    }

    /**
     * Sets up/down movement of this character.
     *
     * This is the result of input times dude force.
     *
     * @param value up/down movement of this character.
     */
    public void setUpDown(float value) {
        upDown = value;
        // Change facing if appropriate
        if (upDown < 0) {
            faceUp = false;
        } else if (upDown > 0) {
            faceUp = true;
        }
    }

    /**
     * Returns how much force to apply to get the dude moving
     *
     * Multiply this by the input to get the movement value.
     *
     * @return how much force to apply to get the dude moving
     */
    public float getForce() {
        return DUGGI_FORCE;
    }

    /**
     * Returns ow hard the brakes are applied to get a dude to stop moving
     *
     * @return ow hard the brakes are applied to get a dude to stop moving
     */
    public float getDamping() {
        return DUGGI_DAMPING;
    }

    /**
     * Returns the upper limit on dude left-right movement.
     *
     * This does NOT apply to vertical movement.
     *
     * @return the upper limit on dude left-right movement.
     */
    public float getMaxSpeed() {
        return DUGGI_MAXSPEED;
    }

    /**
     * Returns the name of the ground sensor
     *
     * This is used by ContactListener
     *
     * @return the name of the ground sensor
     */
    public String getSensorName() {
        return SENSOR_NAME;
    }

    /**
     * Returns true if this character is facing right
     *
     * @return true if this character is facing right
     */
    public boolean isFacingRight() {
        return faceRight;
    }

    /**
     * Creates a new dude at the origin.
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param width		The object width in physics units
     * @param height	The object width in physics units
     */
    public DuggiModel(float width, float height) {
        this(0,0,width,height);
    }

    /**
     * Creates a new dude avatar at the given position.
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param x  		Initial x position of the avatar center
     * @param y  		Initial y position of the avatar center
     * @param width		The object width in physics units
     * @param height	The object width in physics units
     */
    public DuggiModel(float x, float y, float width, float height) {
        super(x,y,width*DUGGI_HSHRINK,height*DUGGI_VSHRINK);
        setDensity(DUGGI_DENSITY);
        setFriction(DUGGI_FRICTION);  /// HE WILL STICK TO WALLS IF YOU FORGET
        setFixedRotation(true);

        // Gameplay attributes
        faceRight = true;

        setName("duggi");
    }

    /**
     * Creates the physics Body(s) for this object, adding them to the world.
     *
     * This method overrides the base method to keep your ship from spinning.
     *
     * @param world Box2D world to store body
     *
     * @return true if object allocation succeeded
     */
    public boolean activatePhysics(World world) {
        // create the box from our superclass
        if (!super.activatePhysics(world)) {
            return false;
        }

        // Ground Sensor
        // -------------
        // We only allow the dude to jump when he's on the ground.
        // Double jumping is not allowed.
        //
        // To determine whether or not the dude is on the ground,
        // we create a thin sensor under his feet, which reports
        // collisions with the world but has no collision response.
        Vector2 sensorCenter = new Vector2(0, -getHeight() / 2);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = DUGGI_DENSITY;
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        sensorShape.setAsBox(DUGGI_SSHRINK*getWidth()/2.0f, SENSOR_HEIGHT, sensorCenter, 0.0f);
        sensorDef.shape = sensorShape;

        sensorFixture = body.createFixture(sensorDef);
        sensorFixture.setUserData(getSensorName());

        return true;
    }


    /**
     * Applies the force to the body of this dude
     *
     * This method should be called after the force attribute is set.
     */
    public void applyForce() {
        if (!isActive()) {
            return;
        }

        body.setLinearVelocity(getMovement(),getUpDown());
    }

    public void setDollTexture(TextureRegion texture) {
        doll.setTexture(texture);
    }

    public void setHerbivoreTexture(TextureRegion texture) {
        herbivore.setTexture(texture);
    }

    public void setCarnivoreTexture(TextureRegion texture) {
        carnivore.setTexture(texture);
    }

    public void setTransformation(int form) {
        if (form == DOLL_FORM)
            currentForm = doll;
        else if (form == HERBIVORE_FORM)
            currentForm = herbivore;
        else if (form == CARNIVORE_FORM)
            currentForm = carnivore;
    }

    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void update(float dt) {
        super.update(dt);
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        float effect = faceRight ? 1.0f : -1.0f;
        canvas.draw(currentForm.getTexture(), Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
    }

    /**
     * Draws the outline of the physics body.
     *
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        canvas.drawPhysics(sensorShape,Color.RED,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
    }
}
