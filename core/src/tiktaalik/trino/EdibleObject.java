package tiktaalik.trino;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import tiktaalik.util.FilmStrip;

public abstract class EdibleObject extends GameObject {
    protected static final float ANIMATION_SPEED = 0.175f;

    protected FilmStrip textureSet;
    protected int numFrames;
    protected float animeframe;
    private boolean eatInProgress;
    private boolean eaten;

    public EdibleObject(float x, float y) {
        super(x, y);
        eaten = false;
    }

    public void beginEating() {
        eatInProgress = true;
    }

    public boolean getEaten() {
        return eaten;
    }

    public void setEatAnimation(Texture texture, int frames) {
        numFrames = frames;
        textureSet = new FilmStrip(texture,1,frames,frames);
        origin = new Vector2(textureSet.getRegionWidth()/2.0f, textureSet.getRegionHeight()/2.0f);
    }

    public void update(float dt) {
        super.update(dt);

        if (eatInProgress) {
            animeframe += ANIMATION_SPEED;
            if (animeframe >= numFrames) {
                eaten = true;
                eatInProgress = false;
            }
        }
    }

    public void draw(Canvas canvas, float offsetX, float offsetY) {
        if (eatInProgress) {
            textureSet.setFrame((int)animeframe);
            if (textureSet != null) {
                canvas.draw(textureSet, Color.WHITE,origin.x,origin.y,getX()*drawScale.x + offsetX,
                        getY()*drawScale.x + offsetY,0,1,1);

            }
        } else if (texture != null && !eaten) {
            canvas.draw(texture, Color.WHITE,origin.x,origin.y,getX()*drawScale.x + offsetX,
                    getY()*drawScale.x + offsetY,0,1,1);
        }
    }
}
