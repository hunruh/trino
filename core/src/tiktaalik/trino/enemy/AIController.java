package tiktaalik.trino.enemy;

import com.badlogic.gdx.math.Vector2;
import tiktaalik.trino.GameObject;
import tiktaalik.trino.duggi.Dinosaur;

public class AIController {
    public static int LEFT = 0;
    public static int RIGHT = 1;
    public static int FLIP = 2;

    private Enemy enemy; // The ship being controlled by this AIController
    private GameObject target; // The target dinosaur

    private Vector2[] path;
    private int pathStep;
    private Vector2 step;
    private int turnAngle;

    private float speed;
    private float defaultSpeed = .025f;

    public AIController(int id, GameObject duggi, Enemy[] enemies, Vector2[] p, int turnAngle) {
        this.enemy = enemies[id];

        target = duggi;
        pathStep = 1;
        path = p;
        step = new Vector2();
        this.turnAngle = turnAngle;
    }

    public void step(boolean obstacle) {
        if (enemy.getStunned())
            return;

        if (obstacle) {
            if ((enemy.getDirection() == Dinosaur.LEFT && turnAngle == LEFT) ||
                (enemy.getDirection() == Dinosaur.RIGHT && turnAngle == RIGHT) ||
                (enemy.getDirection() == Dinosaur.UP && turnAngle == FLIP)) {
                enemy.setDirection(Dinosaur.DOWN);
            }
            else if ((enemy.getDirection() == Dinosaur.UP && turnAngle == LEFT) ||
                    (enemy.getDirection() == Dinosaur.DOWN && turnAngle == RIGHT) ||
                    (enemy.getDirection() == Dinosaur.RIGHT && turnAngle == FLIP)) {
                enemy.setDirection(Dinosaur.LEFT);
            }
            else if ((enemy.getDirection() == Dinosaur.RIGHT && turnAngle == LEFT) ||
                    (enemy.getDirection() == Dinosaur.LEFT && turnAngle == RIGHT) ||
                    (enemy.getDirection() == Dinosaur.DOWN && turnAngle == FLIP)) {
                enemy.setDirection(Dinosaur.UP);
            }
            else if ((enemy.getDirection() == Dinosaur.DOWN && turnAngle == LEFT) ||
                    (enemy.getDirection() == Dinosaur.UP && turnAngle == RIGHT) ||
                    (enemy.getDirection() == Dinosaur.LEFT && turnAngle == FLIP)) {
                enemy.setDirection(Dinosaur.RIGHT);
            }
        }

        float speed = defaultSpeed;

        if (enemy.getDirection() == Dinosaur.LEFT) {
//            if (enemy.getPosition().cpy().sub(target.getPosition()).x > 3.0f)
//                speed *= 2;

            step.x = -speed;
            step.y = 0;
        } else if (enemy.getDirection() == Dinosaur.RIGHT) {
//            if (enemy.getPosition().cpy().sub(target.getPosition()).x < -3.0f)
//                speed *= 2;

            step.x = speed;
            step.y = 0;
        } else if (enemy.getDirection() == Dinosaur.DOWN) {
//            if (enemy.getPosition().cpy().sub(target.getPosition()).y > 3.0f)
//                speed *= 2;

            step.x = 0;
            step.y = -speed;
        } else if (enemy.getDirection() == Dinosaur.UP) {
//            if (enemy.getPosition().cpy().sub(target.getPosition()).x < -3.0f)
//                speed *= 2;

            step.x = 0;
            step.y = speed;
        }

        enemy.setPosition(enemy.getX() + step.x, enemy.getY() + step.y);
    }
}
