package tiktaalik.trino.duggi;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import tiktaalik.trino.Canvas;

public class Carnivore extends Dinosaur {
    private final float CHARGE_COOLDOWN_DURATION = 0.5f;
    private final float CHARGE_LOAD_DURATION = 1.0f;

    private boolean charging, chargeReady, coolingCharge, loadingCharge;
    private float chargeCooldown, chargeLoad;
    private int chargeDirection;

    private float collideCooldown;
    private boolean collided;

    private Vector2 vectorCache;

    public Carnivore(Dinosaur d) {
        super(d);
        charging = false;
        chargeReady = false;
        coolingCharge = false;
        loadingCharge = false;
        chargeCooldown = 0.0f;
        chargeLoad = 0.0f;
        collided = false;

        vectorCache = new Vector2();
    }

    public int getForm() {
        return CARNIVORE_FORM;
    }

    public boolean inChargeCycle() {
        return loadingCharge || charging || coolingCharge;
    }

    public void loadCharge() {
        if (!loadingCharge && !charging && !coolingCharge && !chargeReady) {
            animeframe = 0;
            loadingCharge = true;
            actionAnimating = true;
        }
    }

    public boolean chargeReady() {
        return chargeReady;
    }

    public void charge() {
        if (chargeReady) {
            charging = true;
            chargeReady = false;
            chargeDirection = getDirection();
        }
    }

    public void setCollided(boolean collided) {
        if ((collided && collideCooldown <= 0) || !collided)
            this.collided = collided;
    }

    public boolean getCollided() {
        return collided;
    }

    public void applyForce() {
        if (!isActive()) {
            return;
        }

        if (!charging)
            body.setLinearVelocity(getLeftRight(),getUpDown());
        else if (collided) {
            body.setLinearVelocity(0.0f, 0.0f);
            collided = false;
        }
        else {
            if (chargeDirection == LEFT)
                body.setLinearVelocity(-15.0f, 0.0f);
            else if (chargeDirection == RIGHT)
                body.setLinearVelocity(15.0f, 0.0f);
            else if (chargeDirection == UP)
                body.setLinearVelocity(0.0f, 15.0f);
            else
                body.setLinearVelocity(0.0f, -15.0f);
        }
    }

    public boolean getCharging() {
        return charging;
    }

    public void stopCharge() {
        System.out.println("stop charge");

        if (charging)
            coolingCharge = true;

        actionAnimating = false;
        loadingCharge = false;
        charging = false;
        chargeReady = false;
        chargeLoad = 0.0f;
    }

    public void update(float dt) {
        super.update(dt);
        if (loadingCharge) {
            chargeLoad += dt;

            System.out.println("Loading charge... " + chargeLoad);
            if (chargeLoad >= CHARGE_LOAD_DURATION) {
                loadingCharge = false;
                chargeReady = true;
                chargeLoad = 0.0f;
            }
        } else if (coolingCharge) {
            chargeCooldown += dt;
//            System.out.println("Cooling charge... " + chargeCooldown);
            if (chargeCooldown >= CHARGE_COOLDOWN_DURATION) {
                coolingCharge = false;
                chargeCooldown = 0.0f;
            }
        }

        if (loadingCharge || (chargeReady && !charging)) {
            animeframe += ANIMATION_SPEED;
            if (animeframe >= numFrames[direction + 4]) {
                animeframe -= (numFrames[direction + 4] - 3);
            }
        } else if (charging) {
            animeframe += ANIMATION_SPEED;
            if (animeframe >= numFrames[direction + 8]) {
                animeframe -= (numFrames[direction + 8]);
            }
        }
    }

    public void draw(Canvas canvas) {
        int filmStripItem = direction;
        if (loadingCharge || (chargeReady && !charging))
            filmStripItem += 4;
        else if (charging)
            filmStripItem += 8;

        textureSet[filmStripItem].setFrame((int)animeframe);
        if (textureSet[filmStripItem] != null) {
            canvas.draw(textureSet[filmStripItem], Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.x,0,1,1);

        }
    }
}
