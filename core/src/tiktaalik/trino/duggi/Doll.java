package tiktaalik.trino.duggi;

import tiktaalik.trino.Canvas;

public class Doll extends Dinosaur {
    public Doll(Dinosaur d) {
        super(d);
    }

    public Doll(float x, float y, float radius) {
        super(x, y, radius);
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
