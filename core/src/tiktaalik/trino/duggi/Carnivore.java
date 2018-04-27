package tiktaalik.trino.duggi;

import com.badlogic.gdx.physics.box2d.PolygonShape;
import tiktaalik.trino.Canvas;

public class Carnivore extends Dinosaur {
    private boolean collided;

    public Carnivore(Dinosaur d) {
        super(d);
        collided = false;

        shape = new PolygonShape();
        float vertices[] = new float[16];
        int ctr = 0;
        for (float theta = 0; theta < 2 * Math.PI; theta += ((2.0f * Math.PI)/(vertices.length / 2))) {
            if (ctr >= 16)
                break;

            vertices[ctr++] = (float)(radius * Math.cos(theta) * .78); // x
            vertices[ctr++] = (float)(-radius * Math.sin(theta) * .28) - radius/4; // y
        }
        shape.set(vertices);

        body.destroyFixture(body.getFixtureList().first());
        fixture.shape = shape;
        body.createFixture(fixture);
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
        else {
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

    public void drawShadow(Canvas canvas) {
        canvas.drawShadow(getX()*drawScale.x,getY()*drawScale.x,2*radius*drawScale.x*.75f, radius*drawScale.x);
    }

    public void draw(Canvas canvas) {
        float offsetX = 0;
        if (eating) {
            if (direction == LEFT)
                offsetX = -16.4f;
        }
        super.draw(canvas, offsetX, 0);
    }
}
