package tiktaalik.trino.duggi;

public class Carnivore extends Dinosaur {
    private boolean collided;

    public Carnivore(Dinosaur d) {
        super(d);
        collided = false;
    }

    public int getForm() {
        return CARNIVORE_FORM;
    }

    public void setCollided(boolean collided) {
        if ((collided) || !collided)
            this.collided = collided;
    }

    public void applyForce() {
        if (!isActive()) {
            return;
        }

        if (!actionInProgress)
            body.setLinearVelocity(getLeftRight(),getUpDown());
        else if (collided) {
            body.setLinearVelocity(0.0f, 0.0f);
            collided = false;
        }
        else {
            if (actionDirection == LEFT)
                body.setLinearVelocity(-15.0f, 0.0f);
            else if (actionDirection == RIGHT)
                body.setLinearVelocity(15.0f, 0.0f);
            else if (actionDirection == UP)
                body.setLinearVelocity(0.0f, 15.0f);
            else
                body.setLinearVelocity(0.0f, -15.0f);
        }
    }
}
