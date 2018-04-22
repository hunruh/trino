package tiktaalik.trino.duggi;

import com.badlogic.gdx.graphics.Color;
import tiktaalik.trino.Canvas;

public class Herbivore extends Dinosaur {
    protected boolean actionLoop = false;

    public Herbivore(Dinosaur d) {
        super(d);
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
