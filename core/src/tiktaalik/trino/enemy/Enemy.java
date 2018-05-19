package tiktaalik.trino.enemy;

import static tiktaalik.trino.duggi.Dinosaur.*;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import tiktaalik.trino.Canvas;
import tiktaalik.trino.EdibleObject;
import tiktaalik.trino.GameObject;
import tiktaalik.trino.SoundController;
import tiktaalik.trino.duggi.Clone;
import tiktaalik.trino.duggi.Dinosaur;
import tiktaalik.util.FilmStrip;

public class Enemy extends EdibleObject {
    private final float STUN_DURATION = 4.0f;

    private static final float ANIMATION_SPEED = 0.175f;
    private FilmStrip[] textureSet;

    private AIController controller;

    protected PolygonShape shape; // Shape information for this circle
    private float vertices[];
    private float radius;
    private Fixture geometry; // A cache value for the fixture (for resizing)

    private int numFrames[];
    private float animeframe;
    private float animeframeForExclamation;
    private float collideCooldown;
    private float stunCooldown;
    private boolean faceRight;
    private boolean faceUp;
    private boolean collided;
    private boolean stunned;
    private boolean coolingCharge;
    private boolean loadingCharge;
    private boolean eatingClone;
    private Clone cloneBeingEaten;
    private int direction;
    private Vector2 gridLocation = new Vector2();
    private boolean charging;
    private boolean chargeReady;
    private final float CHARGE_COOLDOWN_DURATION = 0.5f;
    private final float CHARGE_LOAD_DURATION = 1.0f;
    private float chargeCooldown;
    private float chargeLoad;
    private float timeElapsed;
    private float totalTime = 1.0f;
    private boolean alert = false;
    private int enemyType = CARNIVORE_ENEMY;
    private int ticks;
    private FilmStrip exclamation;

    private static final int STUNNED_LEFT = 12;
    private static final int STUNNED_RIGHT = 13;
    private static final int STUNNED_UP = 14;
    private static final int STUNNED_DOWN = 15;
    private static final int EATING_LEFT = 16;
    private static final int EATING_RIGHT = 17;
    private static final int EATING_UP = 18;
    private static final int EATING_DOWN = 19;

    public static final int CARNIVORE_ENEMY = 16;
    public static final int HERBIVORE_ENEMY = 17;
    public static final int UNKILLABLE_ENEMY = 18;
    public static final int SHADOW_DUGGI = 19;

    private float offset = -0.5f;

    /**
     * Creates a new dinosaur at the given position.
     *
     * @param x  		Initial x position of the avatar center
     * @param y  		Initial y position of the avatar center
     * @param radius	The object radius in physics units
     */
    public Enemy(float x, float y, float radius, int id, int type) {
        super(x,y + 0.4f);
        setDensity(100.0f);
        setFriction(0.0f);
        setName("enemy");

        enemyType = type;

        shape = new PolygonShape();
        this.radius = radius;
        shape = new PolygonShape();
        vertices = new float[16];
        int ctr = 0;
        for (float theta = 0; theta < 2 * Math.PI; theta += ((2.0f * Math.PI)/(vertices.length / 2))) {
            if (ctr >= 16)
                break;

            if (enemyType != HERBIVORE_ENEMY) {
                vertices[ctr++] = (float) (radius * Math.cos(theta) * .8); // x
                vertices[ctr++] = (float) (-radius * Math.sin(theta) * .35) - radius / 4; // y
            } else {
                vertices[ctr++] = (float) (radius * Math.cos(theta) * .6); // x
                vertices[ctr++] = (float) (-radius * Math.sin(theta) * .35) - radius / 1.8f; // y
            }
        }
        shape.set(vertices);

        // Gameplay attributes
        textureSet = new FilmStrip[20];
        numFrames = new int[20];
        animeframe = 0;
        faceRight = true;
        faceUp = false;
        stunned = false;
        collided = false;
        charging = false;
        coolingCharge = false;
        chargeReady = false;
        eatingClone = false;

    }

    public void setAlert(boolean assignment){
        if (assignment){
            totalTime = timeElapsed + 1;
        }
        alert = assignment;
    }

    public void setController(AIController controller) {
        this.controller = controller;
    }

    public AIController getController() {
        return controller;
    }

    public int getEnemyType() {return enemyType;}

    public void setEatingClone(boolean assignment, Clone c) {
        if (assignment){
            animeframe = 0f;
            charging = false;
            chargeLoad = 0;
            loadingCharge = false;
            charging = false;
            cloneBeingEaten = c;
        }
        eatingClone = assignment;
    }

    public boolean getEatingClone(){
        return eatingClone;
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

    public void setStunnedTextureSet(Texture left, int leftFrames, Texture right, int rightFrames,
                                     Texture up, int upFrames, Texture down, int downFrames) {
        numFrames[STUNNED_LEFT] = leftFrames;
        textureSet[STUNNED_LEFT] = new FilmStrip(left,1,leftFrames,leftFrames);
        numFrames[STUNNED_RIGHT] = rightFrames;
        textureSet[STUNNED_RIGHT] = new FilmStrip(right,1,rightFrames,rightFrames);
        numFrames[STUNNED_UP] = upFrames;
        textureSet[STUNNED_UP] = new FilmStrip(up,1,upFrames,upFrames);
        numFrames[STUNNED_DOWN] = downFrames;
        textureSet[STUNNED_DOWN] = new FilmStrip(down,1,downFrames,downFrames);
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
        numFrames[16] = leftFrames;
        textureSet[16] = new FilmStrip(left,1,leftFrames,leftFrames);
        numFrames[17] = rightFrames;
        textureSet[17] = new FilmStrip(right,1,rightFrames,rightFrames);
        numFrames[18] = upFrames;
        textureSet[18] = new FilmStrip(up,1,upFrames,upFrames);
        numFrames[19] = downFrames;
        textureSet[19] = new FilmStrip(down,1,downFrames,downFrames);
    }

    public void setExclamationTextureSet(Texture alert){
        this.exclamation = new FilmStrip(alert,1,12,12);
    }

    public void beginEating() {
        eatInProgress = true;
        animeframe = 0;

        if (numEatenFrames == 0) {
            numEatenFrames = numFrames[direction];
        }
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

        if (enemyType == UNKILLABLE_ENEMY || enemyType == HERBIVORE_ENEMY) {
            stunned = false;
            return;
        }
        animeframe = 0;
        stunned = true;
        setLinearDamping(11);
        stunCooldown = 0;
        chargeLoad = 0;
        loadingCharge = false;
        charging = false;
    }

    public void loadCharge() {
        if (!coolingCharge  &&
                (enemyType!= UNKILLABLE_ENEMY && enemyType!= HERBIVORE_ENEMY))
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
        filter.categoryBits = Dinosaur.enemyHerbCatBits;
        filter.maskBits = Dinosaur.wallCatBits|Dinosaur.carnCatBits|Dinosaur.herbCatBits|Dinosaur.enemyHerbCatBits|
                Dinosaur.dollCatBits|Dinosaur.cloneCatBits;
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
        timeElapsed += dt;
        ticks++;
        if (timeElapsed > totalTime) {
            alert = false;
        }

        // Change the collision filter for herbivore enemy
        if (enemyType == HERBIVORE_ENEMY) {

//            if (ticks % 200 == 0){
//                int random = MathUtils.random(1);
//                if (random == 0){
//                    SoundController.getInstance().playWaterSplash();
//                }
//            }

            Filter filter = geometry.getFilterData();
            filter.categoryBits = Dinosaur.enemyHerbCatBits;
            filter.maskBits = Dinosaur.wallCatBits|Dinosaur.carnCatBits|Dinosaur.herbCatBits|
                    Dinosaur.dollCatBits|Dinosaur.cloneCatBits|Dinosaur.enemyHerbCatBits;
            geometry.setFilterData(filter);
        } else {
            Filter filter = geometry.getFilterData();
            filter.categoryBits = Dinosaur.enemyCatBits;
            filter.maskBits = Dinosaur.dollCatBits|Dinosaur.herbCatBits|Dinosaur.carnCatBits|
                    Dinosaur.enemyCatBits|Dinosaur.riverCatBits|Dinosaur.cloneCatBits|Dinosaur.wallCatBits;
            geometry.setFilterData(filter);

        }

        if (alert){
            animeframeForExclamation += 1.5f*ANIMATION_SPEED;
            if (animeframeForExclamation >= 11){
                alert = false;
                animeframeForExclamation = 0;
            }
        }

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

            if (!eatInProgress)
                stunCooldown += dt;
            if (stunCooldown > STUN_DURATION) {
                setBodyType(BodyDef.BodyType.DynamicBody);
                setLinearDamping(0);
                stunned = false;
            }
        }

        animeframe += ANIMATION_SPEED;

        if (eatingClone){
            if (animeframe >= numFrames[direction + 16]) {
                cloneBeingEaten.setRemoved(true);
                eatingClone = false;
                animeframe = 0;
            }
        }
        else if (loadingCharge || (chargeReady && !charging)) {
            if (animeframe >= numFrames[direction + 4]) {
                animeframe -= (numFrames[direction + 4] - 3);
            }
        } else if (charging) {
            if (animeframe >= numFrames[direction + 8]) {
                animeframe -= (numFrames[direction + 8]);
            }
        } else if (stunned) {
            if (animeframe >= numFrames[direction + 12]) {
                animeframe -= numFrames[direction + 12];
            }
        } else {
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
        float offsetX = 0;
        float offsetY = 9;
        if (direction == UP || direction == DOWN) {
            offsetX = 3.5f;
            offsetY = 13f;
        }

        if (enemyType == UNKILLABLE_ENEMY) {
            if (direction == UP || direction == DOWN) {
                offsetX = 1f;
                offsetY = 13f;
            } else {
                offsetX = -9f;
                offsetY = 5;
            }
        } else if (enemyType == HERBIVORE_ENEMY){
            offsetY = -20f;
        }


        if (eatInProgress && eatenTextureSet != null) {
            if (animeframe >= numEatenFrames)
                return;

            eatenTextureSet.setFrame((int)animeframe);
            canvas.draw(eatenTextureSet, Color.WHITE,origin.x,origin.y,getX()*drawScale.x + offsetX,
                    getY()*drawScale.x + offsetY,0,1,1);

            return;
        }

        int filmStripItem = direction;
        if (loadingCharge || (chargeReady && !charging))
            filmStripItem += 4;
        else if (charging)
            filmStripItem += 8;
        else if (stunned)
            filmStripItem += 12;
        else if (eatingClone)
            filmStripItem += 16;

        textureSet[filmStripItem].setFrame((int)animeframe);
        if (textureSet[filmStripItem] != null) {
            canvas.draw(textureSet[filmStripItem], Color.WHITE,origin.x,origin.y,getX()*drawScale.x + offsetX,
                    getY()*drawScale.x + offsetY,0,1,1);
        }

        if (alert){
            Vector2 originExclamation = new Vector2(exclamation.getRegionWidth()/2.0f, exclamation.getRegionHeight()/2.0f);
            exclamation.setFrame((int)animeframeForExclamation);
            float xOffset = 0;
            if (getDirection() == Dinosaur.LEFT){
                xOffset = -1f;
            } else if (getDirection() == Dinosaur.RIGHT){
                xOffset = 0;
            } else {
                xOffset = -0.5f;
            }
            canvas.draw(exclamation, Color.WHITE, originExclamation.x, originExclamation.y, (getX()+xOffset)*drawScale.x,
                    (getY()+1.35f)*drawScale.x, 0, 1,1);
        }
    }

    public void drawShadow(Canvas canvas) {
        canvas.drawShadow(getX()*drawScale.x,getY()*drawScale.x,2*radius*drawScale.x*.75f, radius*drawScale.x);
    }

    public void drawProgressCircle(Canvas canvas){
      if (alert && (enemyType!= UNKILLABLE_ENEMY && enemyType!= HERBIVORE_ENEMY)) {
          Color newColor = new Color(1, 0, 0, 1);
          CircleShape progressCircle = new CircleShape();
          progressCircle.setRadius(0.075f);
          canvas.drawProgressCircle(progressCircle, newColor, 3, getX() * drawScale.x, getY() * drawScale.x, drawScale.x);
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
}

