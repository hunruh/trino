package tiktaalik.trino.duggi;

import com.badlogic.gdx.math.Vector2;

public class Carnivore extends Dinosaur {
    private final float CHARGE_COOLDOWN_DURATION = 2.0f;
    private final float CHARGE_LOAD_DURATION = 1.0f;

    private boolean charging, chargeReady, coolingCharge, loadingCharge;
    private float chargeCooldown, chargeLoad;
    private int chargeDirection;

    private Vector2 vectorCache;

    public Carnivore(Dinosaur d) {
        super(d);
        charging = false;
        chargeReady = false;
        coolingCharge = false;
        loadingCharge = false;
        chargeCooldown = 0.0f;
        chargeLoad = 0.0f;

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
            loadingCharge = true;
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

    public void applyForce() {
        if (!isActive()) {
            return;
        }

        if (!charging)
            body.setLinearVelocity(getLeftRight(),getUpDown());
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
        loadingCharge = false;
        charging = false;
        coolingCharge = true;
        System.out.println("Stop the madness");
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
    }
}
