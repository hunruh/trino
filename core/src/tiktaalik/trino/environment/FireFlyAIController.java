package tiktaalik.trino.environment;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import tiktaalik.trino.GameObject;

public class FireFlyAIController {
    private FireFly firefly; // The firefly being controlled by this AIController
    private Vector2 goal;
    private Vector2 step;
    private Rectangle gameBounds;

    private float enemySpeed = .025f;

    public FireFlyAIController(int id, FireFly[] fireFlies, Rectangle bounds) {
        this.firefly = fireFlies[id];

        // Choose a random location
        goal = new Vector2(MathUtils.random(bounds.width),MathUtils.random(bounds.height));
        step = new Vector2();
        gameBounds = bounds;
    }

    /**
     * Returns a movement direction that moves towards a goal tile.
     *
     * @return a movement direction that moves towards a goal tile.
     */
    public void getMoveAlongPath() {

        // If firefly is close to goal, pick a new goal
        if (Vector2.dst(firefly.getX(),firefly.getY(),goal.x,goal.y) < 1){
            goal.set(MathUtils.random(gameBounds.width),MathUtils.random(gameBounds.height));
        }

        step = goal.cpy().sub(firefly.getPosition()).nor().scl(.025f);
        firefly.setPosition(firefly.getX() + step.x, firefly.getY() + step.y);
    }
}