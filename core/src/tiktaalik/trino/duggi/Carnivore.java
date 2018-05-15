package tiktaalik.trino.duggi;

import com.badlogic.gdx.physics.box2d.PolygonShape;
import tiktaalik.trino.Canvas;
import tiktaalik.trino.environment.Boulder;

public class Carnivore extends Dinosaur {
    private boolean collided;
    private boolean pushing;
    private boolean chargeActive;
    private boolean shakeCamera;

    private Boulder nextToBoulder;

    public Carnivore(Dinosaur d) {
        super(d);
        chargeActive = false;
        collided = false;
        pushing = false;

        shape = new PolygonShape();
        float vertices[] = new float[16];
        int ctr = 0;
        for (float theta = 0; theta < 2 * Math.PI; theta += ((2.0f * Math.PI)/(vertices.length / 2))) {
            if (ctr >= 16)
                break;

            vertices[ctr++] = (float)(radius * 1.15 * Math.cos(theta) * .78); // x
            vertices[ctr++] = (float)(-radius * 1.15 * Math.sin(theta) * .5) - (radius * 1.45f)/4; // y
        }
        shape.set(vertices);

        body.destroyFixture(body.getFixtureList().first());
        fixture.shape = shape;
        body.createFixture(fixture);
    }

    public void setLeftRight(float value) {
        if (!actionInProgress)
            super.setLeftRight(value);
    }

    public void setUpDown(float value) {
        if (!actionInProgress)
            super.setUpDown(value);
    }

    public boolean getPushing() {
        return pushing;
    }

    public boolean getShakeCamera(){return shakeCamera;}

    public void setShakeCamera(boolean assignment){shakeCamera = assignment;}

    public void setPushing(boolean pushing) {
        this.pushing = pushing;
    }

    public Boulder getAdjacentBoulder() {
        return nextToBoulder;
    }

    public void setNextToBoulder(Boulder b) {
        this.nextToBoulder = b;
    }

    public int getForm() {
        return CARNIVORE_FORM;
    }

    protected boolean loopAction() {
        return true;
    }

    public void setCollided(boolean collided) {
        if ((collided) || !collided)
            this.collided = collided;
    }

    public void applyForce() {
        if (!isActive()) {
            return;
        }

        if (!actionInProgress)
            body.setLinearVelocity(getLeftRight(),getUpDown());
        else if (collided) {
            body.setLinearVelocity(0.0f, 0.0f);
            collided = false;
        }
        else if (pushing) {
            chargeActive = true;
            if (actionDirection == LEFT)
                body.setLinearVelocity(-5.0f, 0.0f);
            else if (actionDirection == RIGHT)
                body.setLinearVelocity(5.0f, 0.0f);
            else if (actionDirection == UP)
                body.setLinearVelocity(0.0f, 5.0f);
            else
                body.setLinearVelocity(0.0f, -5.0f);
        } else {
            chargeActive = true;
            if (actionDirection == LEFT)
                body.setLinearVelocity(-15.0f, 0.0f);
            else if (actionDirection == RIGHT)
                body.setLinearVelocity(15.0f, 0.0f);
            else if (actionDirection == UP)
                body.setLinearVelocity(0.0f, 15.0f);
            else
                body.setLinearVelocity(0.0f, -15.0f);
        }
    }

    public void update(float dt) {
        super.update(dt);

        if (!pushing && chargeActive && this.getLinearVelocity().len2() < 0.2f) {
            setCollided(true);
            shakeCamera = true;
            stopAction();
            chargeActive = false;
        }
    }

    public void drawShadow(Canvas canvas) {
        canvas.drawShadow(getX()*drawScale.x,getY()*drawScale.x - 5,2*radius*1.2f*drawScale.x*.75f, radius*1.15f*drawScale.x, shadowOpacity);
    }

    public void draw(Canvas canvas) {
        float offsetX = 1;
        float offsetY = 0;
        if (eating) {
            if (direction == LEFT)
                offsetX = -15.4f;
        }
        else if (direction == UP || direction == DOWN) {
            offsetX = 4.5f;
            offsetY = 9f;
        }
        super.draw(canvas, offsetX, offsetY);
    }
}
