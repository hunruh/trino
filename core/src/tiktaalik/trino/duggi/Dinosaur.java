package tiktaalik.trino.duggi;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import tiktaalik.trino.Canvas;
import tiktaalik.trino.GameObject;
import tiktaalik.util.FilmStrip;

public abstract class Dinosaur extends GameObject {
    public static final int DOLL_FORM = 0;
    public static final int HERBIVORE_FORM = 1;
    public static final int CARNIVORE_FORM = 2;

    // Category Bits
    public static final short dollCatBits = 0x0001;
    public static final short herbCatBits = 0x0002;
    public static final short carnCatBits = 0x0004;
    public static final short enemyCatBits = 0x0008;
    public static final short riverCatBits = 0x0010;
    public static final short cloneCatBits = 0x0011;
    public static final short switchCatBits = 0x0012;
    public static final short wallCatBits = 0x0014;

    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int UP = 2;
    public static final int DOWN = 3;

    public static final int MAX_RESOURCES = 3;
    private final int TRANSFORM_COST = 3;
    protected static final float ANIMATION_SPEED = 0.175f;

    protected FilmStrip[] textureSet;

    protected CircleShape shape; // Shape information for this circle
    private Fixture geometry; // A cache value for the fixture (for resizing)

    protected int numFrames[];
    protected float animeframe;
    protected boolean actionAnimating;
    private boolean canExit;
    private float leftRight; // The current horizontal movement of the character
    private float upDown; // The current vertical movement of the character
    protected int direction;
    private int resourceCnt;

    private static final int ACTION_LOADING_LEFT = 4;
    private static final int ACTION_LOADING_RIGHT = 5;
    private static final int ACTION_LOADING_UP = 6;
    private static final int ACTION_LOADING_DOWN = 7;
    private static final int ACTION_LEFT = 8;
    private static final int ACTION_RIGHT = 9;
    private static final int ACTION_UP = 10;
    private static final int ACTION_DOWN = 11;

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

        textureSet = new FilmStrip[12];
        numFrames = new int[12];
        animeframe = 0;
        resourceCnt = 0;
        actionAnimating = false;
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
        textureSet = new FilmStrip[4];
        numFrames = new int[4];
        animeframe = 0;
        resourceCnt = 0;
        canExit = false;
        actionAnimating = false;
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

    public void setActionLoadingTextureSet(Texture left, int leftFrames, Texture right, int rightFrames, Texture up, int upFrames,
                                    Texture down, int downFrames) {
        numFrames[ACTION_LOADING_LEFT] = leftFrames;
        textureSet[ACTION_LOADING_LEFT] = new FilmStrip(left,1,leftFrames,leftFrames);
        numFrames[ACTION_LOADING_RIGHT] = rightFrames;
        textureSet[ACTION_LOADING_RIGHT] = new FilmStrip(right,1,rightFrames,rightFrames);
        numFrames[ACTION_LOADING_UP] = upFrames;
        textureSet[ACTION_LOADING_UP] = new FilmStrip(up,1,upFrames,upFrames);
        numFrames[ACTION_LOADING_DOWN] = downFrames;
        textureSet[ACTION_LOADING_DOWN] = new FilmStrip(down,1,downFrames,downFrames);
    }

    public void setActionTextureSet(Texture left, int leftFrames, Texture right, int rightFrames, Texture up, int upFrames,
                              Texture down, int downFrames) {
        numFrames[ACTION_LEFT] = leftFrames;
        textureSet[ACTION_LEFT] = new FilmStrip(left,1,leftFrames,leftFrames);
        numFrames[ACTION_RIGHT] = rightFrames;
        textureSet[ACTION_RIGHT] = new FilmStrip(right,1,rightFrames,rightFrames);
        numFrames[ACTION_UP] = upFrames;
        textureSet[ACTION_UP] = new FilmStrip(up,1,upFrames,upFrames);
        numFrames[ACTION_DOWN] = downFrames;
        textureSet[ACTION_DOWN] = new FilmStrip(down,1,downFrames,downFrames);
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
        Filter filter = geometry.getFilterData();
        filter.categoryBits = Dinosaur.dollCatBits;
        filter.maskBits = Dinosaur.enemyCatBits|Dinosaur.riverCatBits|Dinosaur.wallCatBits;
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

        if (((int)animeframe != 0 || getLinearVelocity().len2() > 0) && !actionAnimating) {
            if (getLinearVelocity().len2() == 0 && (int)animeframe >= numFrames[direction] / 2)
                animeframe += ANIMATION_SPEED;
            if (getLinearVelocity().len2() == 0 && (int)animeframe < numFrames[direction] / 2)
                animeframe -= ANIMATION_SPEED;
            else
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
