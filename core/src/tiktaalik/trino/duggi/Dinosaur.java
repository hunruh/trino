package tiktaalik.trino.duggi;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import tiktaalik.trino.Canvas;
import tiktaalik.trino.GameObject;

public abstract class Dinosaur extends GameObject {
    public static final int DOLL_FORM = 0;
    public static final int HERBIVORE_FORM = 1;
    public static final int CARNIVORE_FORM = 2;

    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int UP = 2;
    public static final int DOWN = 3;

    public static final int MAX_RESOURCES = 3;
    private final int TRANSFORM_COST = 3;

    private TextureRegion[] textureSet;

    protected CircleShape shape; // Shape information for this circle
    private Fixture geometry; // A cache value for the fixture (for resizing)

    private boolean canExit;
    private float leftRight; // The current horizontal movement of the character
    private float upDown; // The current vertical movement of the character
    private int direction;
    private int resourceCnt;

    public Doll transformToDoll() {
        return new Doll(this);
    }

    public Carnivore transformToCarnivore() {
        return new Carnivore(this);
    }

    public Herbivore transformToHerbivore() {
        return new Herbivore(this);
    }

    protected Dinosaur(Dinosaur d) {
        bodyinfo = d.bodyinfo;
        fixture = d.fixture;
        massdata = d.massdata;
        masseffect = d.masseffect;
        drawScale = d.drawScale;
        body = d.body;
        shape = d.shape;
        texture = d.texture;
        origin = d.origin;
        setName(d.getName());
        setType(d.getType());

        canExit = d.canExit;
        leftRight = d.leftRight;
        upDown = d.upDown;
        direction = d.direction;

        textureSet = new TextureRegion[4];
        resourceCnt = 0;
        body.setUserData(this);
    }

    /**
     * Creates a new dinosaur at the origin.
     *
     * @param radius	The object radius in physics units
     */
    public Dinosaur(float radius) {
        this(0,0,radius);
    }

    /**
     * Creates a new dinosaur at the given position.
     *
     * @param x  		Initial x position of the avatar center
     * @param y  		Initial y position of the avatar center
     * @param radius	The object radius in physics units
     */
    public Dinosaur(float x, float y, float radius) {
        super(x,y);
        setDensity(1.0f);
        setFriction(0.0f);
        setName("duggi");

        shape = new CircleShape();
        shape.setRadius(radius * 4/5);

        // Gameplay attributes
        direction = RIGHT;
        textureSet = new TextureRegion[4];
        resourceCnt = 0;
        canExit = false;
    }

    public void setTextureSet(TextureRegion left, TextureRegion right, TextureRegion up, TextureRegion down) {
        textureSet[LEFT] = left;
        textureSet[RIGHT] = right;
        textureSet[UP] = up;
        textureSet[DOWN] = down;
    }

    /**
     * Returns left/right movement of this character.
     *
     * @return left/right movement of this character.
     */
    public float getLeftRight() {
        return leftRight;
    }

    /**
     * Returns up/down movement of this character.
     *
     * @return up/down movement of this character.
     */
    public float getUpDown() {return upDown;}

    /**
     * Sets left/right movement of this character.
     *
     * @param value left/right movement of this character.
     */
    public void setLeftRight(float value) {
        leftRight = value;
        if (leftRight < 0)
            direction = LEFT;
        else if (leftRight > 0)
            direction = RIGHT;
    }

    /**
     * Sets up/down movement of this character.
     *
     * @param value up/down movement of this character.
     */
    public void setUpDown(float value) {
        upDown = value;
        if (upDown < 0)
            direction = DOWN;
        else if (upDown > 0)
            direction = UP;
    }

    public boolean canExit() {
        return canExit;
    }

    public void setCanExit(boolean canExit) {
        this.canExit = canExit;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(float value) {
        if (value == 0) {
            direction = LEFT;
        }
        else if (value == 1) {
            direction = RIGHT;
        }
        else if (value == 2) {
            direction = UP;
        }
        else if (value == 3) {
            direction = DOWN;
        }
    }

    public void incrementResources() {
        if (resourceCnt < MAX_RESOURCES) {
            resourceCnt += 1;
        }
    }

    public void decrementResources(){
        if (resourceCnt > 0)
            resourceCnt -= 1;
    }

    public int getResources() {
        return resourceCnt;
    }

    public boolean canTransform() {
        return resourceCnt >= TRANSFORM_COST;
    }

    /**
     * Applies the force to the body of the dinosaur
     */
    public void applyForce() {
        if (!isActive()) {
            return;
        }

        body.setLinearVelocity(getLeftRight(),getUpDown());
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
     * Updates the object's physics state (NOT GAME LOGIC).
     *
     * @param dt Number of seconds since last animation frame
     */
    public void update(float dt) {
        super.update(dt);
        setTexture(textureSet[direction]);
    }

    /**
     * Draws the outline of the physics body.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(Canvas canvas) {
        canvas.drawPhysics(shape,Color.RED,getX(),getY(),drawScale.x,drawScale.y);
    }

    public abstract int getForm();
}
