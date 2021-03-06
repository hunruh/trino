package tiktaalik.trino.duggi;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import tiktaalik.trino.Canvas;
import tiktaalik.trino.GameObject;
import tiktaalik.trino.enemy.Enemy;
import tiktaalik.util.FilmStrip;

public class Clone extends GameObject {
    private float radius;
    protected PolygonShape shape; // Shape information for this circle
    private Fixture geometry; // A cache value for thefixture (for resizing)

    private boolean removed = false;
    private Vector2 gridLocation;

    private float totalTime = 60.0f;
    private float timeElapsed;
    private Enemy enemyEating;

    private FilmStrip idleTextureSet;
    private int numIdleFrames;
    private float animeframe;

    public Clone(float x, float y, float radius) {
        super(x,y);
        setDensity(1.0f);
        setFriction(0.0f);
        setName("clone");
        setBodyType(BodyDef.BodyType.StaticBody);

        shape = new PolygonShape();
        float vertices[] = new float[16];
        int ctr = 0;
        for (float theta = 0; theta < 2 * Math.PI; theta += ((2.0f * Math.PI)/(vertices.length / 2))) {
            if (ctr >= 16)
                break;

            vertices[ctr++] = (float)(radius * Math.cos(theta) * .85); // x
            vertices[ctr++] = (float)(-radius * Math.sin(theta) * .5) - radius/2; // y
        }
        shape.set(vertices);
        gridLocation = new Vector2();
        this.radius = radius;
    }

    public void setIdleTextureSet(Texture idle, int frames) {
        numIdleFrames = frames;
        idleTextureSet = new FilmStrip(idle,1,frames,frames);
        origin.set(idle.getWidth()/(2.0f * frames), idle.getHeight()/2.0f);
    }

    public void setEnemy(Enemy e){
        enemyEating = e;
    }

    public float getCloneTime(){return timeElapsed;}

    public boolean getRemoved() { return removed; }

    public void setRemoved(boolean removed) { this.removed = removed; }

    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     *
     * @param dt Number of seconds since last animation frame
     */
    public void update(float dt) {
        super.update(dt);

        animeframe += Dinosaur.ANIMATION_SPEED;
        if (animeframe >= numIdleFrames)
            animeframe -= numIdleFrames;

        timeElapsed += dt;
        if (timeElapsed > totalTime) {
            if (enemyEating != null){
                enemyEating.setEatingClone(false, this);
            }
            removed = true;
        }
    }

    public Vector2 getGridLocation() {
        return gridLocation;
    }

    public void setGridLocation(float x, float y) {
        gridLocation.x = x;
        gridLocation.y = y;
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
        filter.categoryBits = Dinosaur.cloneCatBits;
        filter.maskBits = Dinosaur.enemyCatBits|Dinosaur.riverCatBits|Dinosaur.wallCatBits|Dinosaur.enemyHerbCatBits;
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
    
    public void draw(Canvas canvas) {
        idleTextureSet.setFrame((int)animeframe);
        if (idleTextureSet != null) {
            canvas.draw(idleTextureSet, Color.WHITE,origin.x,origin.y,getX()*drawScale.x,
                    getY()*drawScale.x,0,1,1);
        }
    }

    public void drawProgressCircle(Canvas canvas){


        Color newColor = new Color(0,1,0,1);

        if (totalTime- timeElapsed> 0){
            if (totalTime - timeElapsed > 0.75f*totalTime){
                newColor = new Color(0, 1, 0f, 1);
                CircleShape progressCircle = new CircleShape();
                progressCircle.setRadius((float)(totalTime - timeElapsed)/1000f);
                canvas.drawProgressCircle(progressCircle, newColor, 2,getX() * drawScale.x, getY() * drawScale.x, drawScale.x);
            }else if (totalTime - timeElapsed > 0.50f*totalTime){
                newColor = new Color(1, 1, 0f, 1);
                CircleShape progressCircle = new CircleShape();
                progressCircle.setRadius((float)(totalTime - timeElapsed)/1000f);
                canvas.drawProgressCircle(progressCircle, newColor, 2,getX() * drawScale.x, getY() * drawScale.x, drawScale.x);
            } else {
                newColor = new Color(1f, 0, 0f, 1);
                CircleShape progressCircle = new CircleShape();
                progressCircle.setRadius((float)(totalTime - timeElapsed)/1000f);
                canvas.drawProgressCircle(progressCircle, newColor, 2,getX() * drawScale.x, getY() * drawScale.x, drawScale.x);
            }
        }


    }

    public void drawShadow(Canvas canvas) {
        canvas.drawShadow(getX()*drawScale.x,getY()*drawScale.x - radius * drawScale.x - 4,
                2*radius*drawScale.x*.85f, radius*drawScale.x);
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
