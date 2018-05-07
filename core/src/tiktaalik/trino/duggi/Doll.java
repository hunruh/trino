package tiktaalik.trino.duggi;

import com.badlogic.gdx.physics.box2d.PolygonShape;
import tiktaalik.trino.Canvas;

public class Doll extends Dinosaur {
    public Doll(Dinosaur d) {
        super(d);
        shape = new PolygonShape();
        float vertices[] = new float[16];
        int ctr = 0;
        for (float theta = 0; theta < 2 * Math.PI; theta += ((2.0f * Math.PI)/(vertices.length / 2))) {
            if (ctr >= 16)
                break;

            vertices[ctr++] = (float)(radius * Math.cos(theta) * .85); // x
            vertices[ctr++] = (float)(-radius * Math.sin(theta) * .5) - radius/4; // y
        }
        shape.set(vertices);

        body.destroyFixture(body.getFixtureList().first());
        fixture.shape = shape;
        body.createFixture(fixture);
    }

    public Doll(float x, float y, float radius) {
        super(x, y, radius);
        shape = new PolygonShape();
        float vertices[] = new float[16];
        int ctr = 0;
        for (float theta = 0; theta < 2 * Math.PI; theta += ((2.0f * Math.PI)/(vertices.length / 2))) {
            if (ctr >= 16)
                break;

            vertices[ctr++] = (float)(radius * Math.cos(theta) * .85); // x
            vertices[ctr++] = (float)(-radius * Math.sin(theta) * .5) - radius/4; // y
        }
        shape.set(vertices);
    }

    public int getForm() {
        return DOLL_FORM;
    }

    protected boolean loopAction() {
        return false;
    }

    public void draw(Canvas canvas) {
        float offsetX = 0;
        if (eating) {
            if (direction == LEFT)
                offsetX = -11;
        }
        super.draw(canvas, offsetX, 0);
    }
}
