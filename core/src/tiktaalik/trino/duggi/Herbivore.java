package tiktaalik.trino.duggi;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import tiktaalik.trino.Canvas;

public class Herbivore extends Dinosaur {
    protected boolean actionLoop = false;

    public Herbivore(Dinosaur d) {
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

    public int getForm() {
        return HERBIVORE_FORM;
    }

    protected boolean loopAction() {
        return false;
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
