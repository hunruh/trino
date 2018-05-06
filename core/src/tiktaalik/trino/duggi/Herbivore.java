package tiktaalik.trino.duggi;

import com.badlogic.gdx.physics.box2d.PolygonShape;
import tiktaalik.trino.Canvas;

public class Herbivore extends Dinosaur {
    protected boolean actionLoop = false;
    private boolean upDown = false;
    private float vertices[] = new float[16];

    public Herbivore(Dinosaur d) {
        super(d);
        upDown = false;
        shape = new PolygonShape();

        int ctr = 0;
        for (float theta = 0; theta < 2 * Math.PI; theta += ((2.0f * Math.PI)/(vertices.length / 2))) {
            if (ctr >= 16)
                break;

            vertices[ctr++] = (float)(radius * Math.cos(theta)); // x
            vertices[ctr++] = (float)(-radius * Math.sin(theta) * .5) - radius/4 - 0.3f; // y
        }
        shape.set(vertices);

        body.destroyFixture(body.getFixtureList().first());
        fixture.shape = shape;
        body.createFixture(fixture);
    }

    public int getForm() {
        return HERBIVORE_FORM;
    }

    protected boolean loopAction() {
        return false;
    }

    public void update(float dt) {
        super.update(dt);

        if (direction == UP || direction == DOWN) {
            if (!upDown) {
                upDown = true;

                int ctr = 0;
                for (float theta = 0; theta < 2 * Math.PI; theta += ((2.0f * Math.PI)/(vertices.length / 2))) {
                    if (ctr >= 16)
                        break;

                    vertices[ctr++] = (float)(radius * Math.cos(theta) * .5); // x
                    vertices[ctr++] = (float)(radius * Math.sin(theta) * .8) - radius/4; // y
                }
                shape.set(vertices);

                body.destroyFixture(body.getFixtureList().first());
                fixture.shape = shape;
                body.createFixture(fixture);
            }
        } else {
            if (upDown) {
                upDown = false;

                int ctr = 0;
                for (float theta = 0; theta < 2 * Math.PI; theta += ((2.0f * Math.PI)/(vertices.length / 2))) {
                    if (ctr >= 16)
                        break;

                    vertices[ctr++] = (float)(radius * Math.cos(theta)); // x
                    vertices[ctr++] = (float)(-radius * Math.sin(theta) * .5) - radius/4 - 0.3f; // y
                }
                shape.set(vertices);

                body.destroyFixture(body.getFixtureList().first());
                fixture.shape = shape;
                body.createFixture(fixture);
            }
        }
    }

    public void drawShadow(Canvas canvas) {
        if (upDown)
            canvas.drawShadow(getX()*drawScale.x,getY()*drawScale.x + 8,1.25f*radius*drawScale.x, 1.6f*radius*drawScale.x);
        else
            canvas.drawShadow(getX()*drawScale.x,getY()*drawScale.x - 6,2*radius*drawScale.x, radius*drawScale.x);
    }

    public void draw(Canvas canvas) {
        int offsetX = 0;
        if (eating) {
            if (direction == LEFT)
                offsetX = -37;
        }
        super.draw(canvas, offsetX, 0);
    }
}
