package tiktaalik.trino.duggi;

public class Carnivore extends Dinosaur {
    private final float CHARGE_COOLDOWN_DURATION = 2.0f;
    private final float CHARGE_LOAD_DURATION = 1.0f;

    private boolean charging, chargeReady, coolingCharge, loadingCharge;
    private float chargeCooldown, chargeLoad;

    public Carnivore(Dinosaur d) {
        super(d);
    }

    public int getForm() {
        return CARNIVORE_FORM;
    }

    public void loadCharge() {
        if (!loadingCharge && !charging && !coolingCharge) {
            chargeLoad = 0.0f;
            loadingCharge = true;
        }
    }

    public boolean getCharging() {
        return charging;
    }

    public void stopCharge() {
        this.charging = false;
        this.coolingCharge = true;
    }

    public void update(float dt) {
        super.update(dt);
        if (loadingCharge) {
            chargeLoad += dt;
            if (chargeLoad >= CHARGE_LOAD_DURATION) {
                loadingCharge = false;
                chargeReady = true;
            }
        } else if (coolingCharge) {
            chargeCooldown += dt;
            if (chargeCooldown >= CHARGE_COOLDOWN_DURATION)
                coolingCharge = false;
        }
    }
}
