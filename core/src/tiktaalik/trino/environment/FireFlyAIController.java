package tiktaalik.trino.environment;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import tiktaalik.util.PooledList;

public class FireFlyAIController {
    private FireFly firefly; // The firefly being controlled by this AIController
    private Vector2 goal;
    private Vector2 step;
    private Rectangle gameBounds;
    private float radius = 750f;
    private float fireFlyCircleScale = 3f;

    public FireFlyAIController(int id, PooledList<FireFly> fireFlies, Rectangle bounds) {
        this.firefly = fireFlies.get(id);

        // Choose a random location
        goal = new Vector2(MathUtils.random(2*bounds.width),MathUtils.random(2*bounds.height));
        step = new Vector2();
        gameBounds = bounds;
    }

    public void setGoal(Vector2 position){
        goal = position;
    }

    public Vector2 getGoal(){return goal;}

    public void resetGoal(){
        goal = new Vector2(MathUtils.random(2*gameBounds.width),MathUtils.random(2*gameBounds.height));
    }

    /**
     * Returns a movement direction that moves towards a goal tile.
     *
     * @return a movement direction that moves towards a goal tile.
     */
    public void getMoveAlongPath() {
        this.firefly.setLinearVelocity(Vector2.Zero);
        // If firefly is close to goal, pick a new goal
        if (Vector2.dst(firefly.getX(),firefly.getY(),goal.x,goal.y) < 1){
            goal.set(MathUtils.random(2*gameBounds.width),MathUtils.random(2*gameBounds.height));
        }

        step = goal.cpy().sub(firefly.getPosition()).nor().scl(.025f);
        firefly.setPosition(firefly.getX() + step.x, firefly.getY() + step.y);
    }

    public void getMoveToGoal(Vector2 position) {
        position.y = position.y + 0.75f;
        // If firefly is close to goal, pick a new goal
        if (Vector2.dst(firefly.getX(),firefly.getY(),position.x,position.y) < 1f){
            updateCircular(1, position);
        } else {
            step = position.cpy().sub(firefly.getPosition()).nor().scl(.025f);
            firefly.setPosition(firefly.getX() + step.x, firefly.getY() + step.y);
        }

    }

    public void updateCircular(float speed, Vector2 center){
        Vector2 radius = center.cpy().sub(this.firefly.getPosition());
        radius.x = fireFlyCircleScale * radius.x;
        radius.y = fireFlyCircleScale * radius.y;
        Vector2 force = radius.rotate90(1).nor().scl(speed);
        this.firefly.setLinearVelocity(new Vector2(force.x, force.y));
    }
}