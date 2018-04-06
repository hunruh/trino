package tiktaalik.trino.enemy;

import com.badlogic.gdx.math.Vector2;
import tiktaalik.trino.GameObject;
import tiktaalik.trino.duggi.Dinosaur;

public class AIController {
    private Enemy enemy; // The ship being controlled by this AIController
    private GameObject target; // The target ship (to chase or attack)

    private Vector2[] path;
    private int pathStep;
    private Vector2 step;

    private float enemySpeed = .025f;

    public AIController(int id, GameObject duggi, Enemy[] enemies, Vector2[] p) {
        this.enemy = enemies[id];

        // Select an initial target
        target = duggi;
        pathStep = 1;
        path = p;
        step = new Vector2();
    }

    /**
     * Returns a movement direction that moves towards a goal tile.
     *
     * @return a movement direction that moves towards a goal tile.
     */
    public void getMoveAlongPath() {
        if (enemy.getStunned())
            return;

        if (path[pathStep].cpy().sub(enemy.getPosition()).len() < 0.2f){
            pathStep = (pathStep + 1) % path.length;
            enemySpeed = 0.025f;
        } else if (path[(pathStep + 1) % path.length].cpy().sub(target.getPosition()).len() < 0.5f){
            pathStep = (pathStep + 1) % path.length;
            enemySpeed = 0.05f;

        } else if (path[(pathStep + 2) % path.length].cpy().sub(target.getPosition()).len() < 0.5f) {
            pathStep = (pathStep + 2) % path.length;
            enemySpeed = 0.05f;

        }

        step = path[pathStep].cpy().sub(enemy.getPosition()).nor().scl(enemySpeed);
        enemy.setPosition(enemy.getX() + step.x, enemy.getY() + step.y);

        if (step.x < 0)
            enemy.setDirection(Dinosaur.LEFT);
        else if (step.x > 0)
            enemy.setDirection(Dinosaur.RIGHT);
        else if (step.y < 0)
            enemy.setDirection(Dinosaur.DOWN);
        else if (step.y > 0)
            enemy.setDirection(Dinosaur.UP);
    }
}
