package tiktaalik.trino.enemy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import tiktaalik.trino.Canvas;
import tiktaalik.trino.GameObject;
import tiktaalik.trino.InputController;

public class Enemy extends GameObject {
    private static final float MOVE_SPEED = 6.5f;

    protected CircleShape shape; // Shape information for this circle
    private Fixture geometry; // A cache value for the fixture (for resizing)

    private float leftRight; // The current horizontal movement of the character
    private float upDown; // The current vertical movement of the character
    private boolean faceRight;
    private boolean faceUp;
    private int counter; // Counter for enemy movement
    private int id;
    private Vector2 velocity;
    private Vector2 tmp;
    private boolean isAlive;
    private int direction;

    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int UP = 2;
    public static final int DOWN = 3;

    /**
     * Creates a new dinosaur at the given position.
     *
     * @param x  		Initial x position of the avatar center
     * @param y  		Initial y position of the avatar center
     * @param radius	The object radius in physics units
     */
    public Enemy(float x, float y, float radius, int id) {
        super(x,y);
        setDensity(1.0f);
        setFriction(0.0f);
        setName("enemy");

        shape = new CircleShape();
        shape.setRadius(radius * 4/5);

        // Gameplay attributes
        faceRight = true;
        faceUp = false;
        this.id = id;
        velocity = new Vector2();
        isAlive = true;
    }

    public void setId(int newId){
        this.id = newId;
    }

    public int getId(){
        return this.id;
    }

    public float getVX(){return velocity.x;}

    public void setVX(float value) {velocity.x = value;};

    public float getVY(){return velocity.y;}

    public void setVY(float value){velocity.y = value;}

    public Vector2 getVelocity(){return velocity;}

    public void setAlive(boolean alive){
        this.isAlive = alive;
    }

    public boolean isAlive(){
        return this.isAlive;
    }

    public int getCounter() {return counter;}

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
        if (leftRight < 0) {
            faceRight = false;
            faceUp = false;
        } else if (leftRight > 0) {
            faceRight = true;
            faceUp = false;
        }
    }

    /**
     * Sets up/down movement of this character.
     *
     * @param value up/down movement of this character.
     */
    public void setUpDown(float value) {
        upDown = value;
        if (upDown < 0) {
            faceRight = false;
            faceUp = false;
        } else if (upDown > 0) {
            faceRight = false;
            faceUp = true;
        }
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
     * Returns true if this character is facing right
     *
     * @return true if this character is facing right
     */
    public boolean isFacingUp() {
        return faceUp;
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
        counter++;
    }

    public void updateMovement(int action){

        // Determine how we are moving
        boolean movingLeft = (action & InputController.CONTROL_MOVE_LEFT) != 0;
        boolean movingRight = (action & InputController.CONTROL_MOVE_RIGHT) != 0;
        boolean movingUp = (action & InputController.CONTROL_MOVE_UP) != 0;
        boolean movingDown = (action & InputController.CONTROL_MOVE_DOWN) != 0;

        // Process Movement command
        if (movingLeft) {
            velocity.x = -MOVE_SPEED;
            velocity.y = 0;
        } else if (movingRight) {
            velocity.x = MOVE_SPEED;
            velocity.y = 0;
        } else if (movingUp) {
            velocity.y = -MOVE_SPEED;
            velocity.x = 0;
        } else if (movingDown) {
            velocity.y = MOVE_SPEED;
            velocity.x = 0;
        }

        // Update position (Will probably move when we have collision controller
        tmp.set(this.getX(),this.getY());
        tmp.add(this.getVX(), this.getVY());
        this.setPosition(tmp);
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

