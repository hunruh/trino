package tiktaalik.trino.duggi;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import tiktaalik.trino.Canvas;
import tiktaalik.trino.GameObject;
import tiktaalik.util.FilmStrip;

public abstract class Dinosaur extends GameObject {
    private final float ACTION_COOLDOWN_DURATION = 0.5f;
    private final float ACTION_LOAD_DURATION = 1.0f;

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

    protected PolygonShape shape; // Shape information for this ellipse
    protected float radius;
    private Fixture geometry; // A cache value for the fixture (for resizing)

    protected int numFrames[];
    protected float animeframe;
    protected boolean actionAnimating;
    private boolean canExit;

    protected int actionDirection;
    protected boolean actionInProgress, actionComplete, actionReady, coolingAction, loadingAction, eating;
    protected float actionCooldown, actionLoad;
    private boolean canBeSeen = true;
    private float leftRight; // The current horizontal movement of the character
    private float upDown; // The current vertical movement of the character
    protected int direction;
    private int resourceCnt;
    private float prevValueProgCircle = 1.0f;
    private int ticks = 0;
    private Color tint = Color.WHITE;
    private int canBeSeenTimeStamp = 0;
    private int stealthDuration = 1000;

    public static final int ACTION_LOADING_LEFT = 4;
    public static final int ACTION_LOADING_RIGHT = 5;
    public static final int ACTION_LOADING_UP = 6;
    public static final int ACTION_LOADING_DOWN = 7;
    public static final int ACTION_LEFT = 8;
    public static final int ACTION_RIGHT = 9;
    public static final int ACTION_UP = 10;
    public static final int ACTION_DOWN = 11;
    public static final int EATING_LEFT = 12;
    public static final int EATING_RIGHT = 13;
    public static final int EATING_UP = 14;
    public static final int EATING_DOWN = 15;

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
        radius = d.radius;
        texture = d.texture;
        origin = d.origin;
        setName(d.getName());
        setType(d.getType());

        canExit = d.canExit;
        leftRight = d.leftRight;
        upDown = d.upDown;
        direction = d.direction;

        textureSet = new FilmStrip[16];
        numFrames = new int[16];
        animeframe = 0;
        resourceCnt = 0;
        eating = false;
        actionAnimating = false;
        body.setUserData(this);

        // Actions
        actionComplete = false;
        actionInProgress = false;
        actionReady = false;
        coolingAction = false;
        loadingAction = false;
        actionCooldown = 0.0f;
        actionLoad = 0.0f;
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

        this.radius = radius;

        // Gameplay attributes
        direction = RIGHT;
        textureSet = new FilmStrip[16];
        numFrames = new int[16];
        animeframe = 0;
        resourceCnt = 0;
        canExit = false;
        actionAnimating = false;
        eating = false;

        // Actions
        actionComplete = false;
        actionInProgress = false;
        actionReady = false;
        coolingAction = false;
        loadingAction = false;
        actionCooldown = 0.0f;
        actionLoad = 0.0f;
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

    public void setEatingTextureSet(Texture left, int leftFrames, Texture right, int rightFrames, Texture up, int upFrames,
                                    Texture down, int downFrames) {
        numFrames[EATING_LEFT] = leftFrames;
        textureSet[EATING_LEFT] = new FilmStrip(left,1,leftFrames,leftFrames);
        numFrames[EATING_RIGHT] = rightFrames;
        textureSet[EATING_RIGHT] = new FilmStrip(right,1,rightFrames,rightFrames);
        numFrames[EATING_UP] = upFrames;
        textureSet[EATING_UP] = new FilmStrip(up,1,upFrames,upFrames);
        numFrames[EATING_DOWN] = downFrames;
        textureSet[EATING_DOWN] = new FilmStrip(down,1,downFrames,downFrames);
    }

    public float getActionLoadValue(){return actionLoad;}
    public boolean getActionReady(){return actionReady;}
    public boolean getActionAnimating(){return actionAnimating;}

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
        if (eating) {
            leftRight = 0;
            return;
        }

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
        if (eating) {
            upDown = 0;
            return;
        }

        upDown = value;
        if (upDown < 0)
            direction = DOWN;
        else if (upDown > 0)
            direction = UP;
    }

    public boolean getActionComplete() {
        return actionComplete;
    }

    public void useAction() {
        actionComplete = false;
    }

    public boolean inActionCycle() {
        return loadingAction || actionInProgress || coolingAction;
    }

    public void loadAction() {
        if (!loadingAction && !actionInProgress && !coolingAction && !actionReady) {
            animeframe = 0;
            loadingAction = true;
            actionAnimating = true;
        }
    }

    public boolean actionReady() {
        return actionReady;
    }

    public void beginAction() {
        if (actionReady) {
            actionInProgress = true;
            actionReady = false;
            actionDirection = getDirection();
        }
    }

    public boolean getActionInProgress() {
        return actionInProgress;
    }

    public void stopAction() {
        if (actionInProgress)
            coolingAction = true;

        actionAnimating = false;
        loadingAction = false;
        actionInProgress = false;
        actionReady = false;
        actionLoad = 0.0f;
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
        eating = true;
        animeframe = 0;
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

    public boolean getCanBeSeen(){
        return canBeSeen;
    }
    public void setCanBeSeen(boolean assignment){
        if (!assignment){
            canBeSeenTimeStamp = ticks + stealthDuration;
        }
        canBeSeen = assignment;
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
        ticks++;

        if (ticks % 10 == 0){
            if (prevValueProgCircle == 1){
                prevValueProgCircle = 1.25f;
            } else {
                prevValueProgCircle = 1;
            }
        }
        if (ticks >= canBeSeenTimeStamp){
            canBeSeen = true;
        }

        if (canBeSeen){
            tint = Color.WHITE;
        }else {
            tint = Color.GREEN;
        }

        if ((loadingAction || (actionReady && !actionInProgress)) && textureSet[ACTION_LOADING_LEFT] != null) {
            animeframe += ANIMATION_SPEED;
            if (animeframe >= numFrames[direction + 4]) {
                animeframe -= (numFrames[direction + 4] - 3);
            }
        } else if (actionInProgress) {
            animeframe += ANIMATION_SPEED;
            if (animeframe >= numFrames[direction + 8]) {
                if (loopAction())
                    animeframe -= (numFrames[direction + 8]);
                else {
                    stopAction();
                    actionComplete = true;
                    animeframe = 0;
                }
            }
        } else if (eating) {
            animeframe += ANIMATION_SPEED;
            if (animeframe >= numFrames[direction + 12]) {
                eating = false;
                animeframe = 0;
            }
        } else if (((int)animeframe != 0 || getLinearVelocity().len2() > 0) && !actionAnimating) {
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

        if (loadingAction) {
            actionLoad += dt;

            if (actionLoad >= ACTION_LOAD_DURATION) {
                loadingAction = false;
                actionReady = true;
                actionLoad = 0.0f;
            }
        } else if (coolingAction) {
            actionCooldown += dt;

            if (actionCooldown >= ACTION_COOLDOWN_DURATION) {
                coolingAction = false;
                actionCooldown = 0.0f;
            }
        }
    }

    public void draw(Canvas canvas) {
        draw(canvas, 0, 0);
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(Canvas canvas, float offsetX, float offsetY) {
        int filmStripItem = direction;
        if ((loadingAction || (actionReady && !actionInProgress)) && textureSet[ACTION_LOADING_LEFT] != null)
            filmStripItem += 4;
        else if (actionInProgress)
            filmStripItem += 8;
        else if (eating)
            filmStripItem += 12;

        textureSet[filmStripItem].setFrame((int)animeframe);
        if (textureSet[filmStripItem] != null) {
            canvas.draw(textureSet[filmStripItem], tint,origin.x,origin.y,getX()*drawScale.x + offsetX,
                    getY()*drawScale.x + offsetY,0,1,1);
        }
    }

    public void drawShadow(Canvas canvas) {
        canvas.drawShadow(getX()*drawScale.x,getY()*drawScale.x,2*radius*drawScale.x*.85f, radius*drawScale.x);
    }

    public void drawProgressCircle(Canvas canvas, float value){

        if (value > 1){
            value = 1;
        }
        Color newColor = new Color(0,1,0,1);

        if (value > 0.10 && value <= 0.5) {
            newColor = new Color(1, 0, 0, 1);
            CircleShape progressCircle = new CircleShape();
            progressCircle.setRadius(value / 10);
            canvas.drawProgressCircle(progressCircle, newColor,0, getX() * drawScale.x, getY() * drawScale.x, drawScale.x);
        } else if (value > 0.5 && value < 1){
            newColor = new Color(1,1,0,1);
            CircleShape progressCircle = new CircleShape();
            progressCircle.setRadius(value/10);
            canvas.drawProgressCircle(progressCircle,newColor,0,getX()*drawScale.x,getY()*drawScale.x,drawScale.x);
        } else if (actionReady){
            newColor = new Color(0,1,0,1);
            CircleShape progressCircle = new CircleShape();
            progressCircle.setRadius(prevValueProgCircle/10);
            canvas.drawProgressCircle(progressCircle,newColor,0,getX()*drawScale.x,getY()*drawScale.x,drawScale.x);
        }

        if (canBeSeenTimeStamp - ticks > 0){
            newColor = new Color(0.133f, 0.545f, 0.133f, 1);
            CircleShape progressCircle = new CircleShape();
            progressCircle.setRadius((float)(canBeSeenTimeStamp - ticks)/10000f);
            canvas.drawProgressCircle(progressCircle, newColor, 1,getX() * drawScale.x, getY() * drawScale.x, drawScale.x);
        }


    }

    /**
     * Draws the outline of the physics body.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(Canvas canvas) {
        canvas.drawPhysics(shape,Color.RED,getX(),getY()/2,0,drawScale.x,drawScale.y);
    }

    public abstract int getForm();
    protected abstract boolean loopAction();
}
