package tiktaalik.trino;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import tiktaalik.util.FilmStrip;

public abstract class EdibleObject extends GameObject {
    protected static final float ANIMATION_SPEED = 0.175f;

    protected FilmStrip eatenTextureSet;
    protected int numEatenFrames;
    protected float animeframe;
    protected boolean eatInProgress;
    private boolean eaten;

    public EdibleObject(float x, float y) {
        super(x, y);
        eaten = false;
    }

    public void beginEating() {
        eatInProgress = true;
        animeframe = 0;
    }

    public boolean getEaten() {
        return eaten;
    }

    public void setEatAnimation(Texture texture, int frames) {
        numEatenFrames = frames;
        eatenTextureSet = new FilmStrip(texture,1,frames,frames);
        origin = new Vector2( eatenTextureSet.getRegionWidth()/2.0f,  eatenTextureSet.getRegionHeight()/2.0f);
    }

    public void update(float dt) {
        super.update(dt);

        if (eatInProgress) {
            animeframe += ANIMATION_SPEED;
            if (animeframe >= numEatenFrames) {
                eaten = true;
                eatInProgress = false;
            }
        }
    }

    public void draw(Canvas canvas, float offsetX, float offsetY, boolean useAnimationAsFirst) {
        if (eatInProgress || useAnimationAsFirst) {
            if (animeframe >= numEatenFrames)
                return;

            eatenTextureSet.setFrame((int)animeframe);
            if ( eatenTextureSet != null) {
                canvas.draw( eatenTextureSet, Color.WHITE,origin.x,origin.y,getX()*drawScale.x + offsetX,
                        getY()*drawScale.x + offsetY,0,1,1);

            }
        } else if (texture != null && !eaten) {
            canvas.draw(texture, Color.WHITE,origin.x,origin.y,getX()*drawScale.x + offsetX,
                    getY()*drawScale.x + offsetY,0,1,1);
        }
    }
}
