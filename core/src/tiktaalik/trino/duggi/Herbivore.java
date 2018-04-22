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
        int filmStripItem = direction;
        if (loadingAction || (actionReady && !actionInProgress))
            filmStripItem += 4;
        else if (actionInProgress)
            filmStripItem += 8;
        else if (eating) {
            if (direction == LEFT)
                offsetX = -37;
            filmStripItem += 12;
        }

        textureSet[filmStripItem].setFrame((int)animeframe);
        if (textureSet[filmStripItem] != null) {
            canvas.draw(textureSet[filmStripItem], Color.WHITE,origin.x,origin.y,getX()*drawScale.x + offsetX,getY()*drawScale.x,0,1,1);

        }
    }
}
