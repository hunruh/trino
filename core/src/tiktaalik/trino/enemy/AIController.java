package tiktaalik.trino.enemy;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.math.Vector2;
import tiktaalik.trino.GameController;
import tiktaalik.trino.GameObject;
import tiktaalik.trino.Level;
import tiktaalik.trino.duggi.Dinosaur;
import tiktaalik.trino.environment.FireFly;
import tiktaalik.trino.environment.Wall;
import tiktaalik.util.PooledList;
import static tiktaalik.trino.GameController.*;

public class AIController {
    public static int LEFT = 0;
    public static int RIGHT = 1;
    public static int FLIP = 2;

    private Enemy enemy; // The ship being controlled by this AIController
    private GameObject target; // The target dinosaur

    private Vector2 step;
    private int turnAngle;

    private float defaultSpeed = .025f;

    private Vector2 locationCache;
    private Level level;

    private int chargeDetectionDistance = 5;
    private boolean charging = false;

    public AIController(int id, GameObject duggi, PooledList<Enemy> enemies, int turnAngle, Level level) {
        this.enemy = enemies.get(id);

        target = duggi;
        step = new Vector2();
        this.turnAngle = turnAngle;
        this.level = level;
        locationCache = new Vector2();
    }

    public void step(boolean obstacle) {
        if (enemy.getStunned())
            return;

        if (playerInFrontOfEnemy()){
            if(enemy.getCollided()){
                enemy.setStunned();
                return;
            }


            if (enemy.getDirection() == Dinosaur.LEFT)
                enemy.getBody().setLinearVelocity(-10.0f, 0.0f);
            else if (enemy.getDirection() == Dinosaur.RIGHT)
                enemy.getBody().setLinearVelocity(10.0f, 0.0f);
            else if (enemy.getDirection() == Dinosaur.UP)
                enemy.getBody().setLinearVelocity(0.0f, 10.0f);
            else
                enemy.getBody().setLinearVelocity(0.0f, -10.0f);

            return;
        }

        if (obstacle || enemy.getCollided()) {
            enemy.setCollided(false);
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
//            if (enemy.getPosition().cpy().sub(target.getPosition()).x < 3.0f)
//                speed *= 2;

            step.x = -speed;
            step.y = 0;
        } else if (enemy.getDirection() == Dinosaur.RIGHT) {
//            if (enemy.getPosition().cpy().sub(target.getPosition()).x > -3.0f)
//                speed *= 2;

            step.x = speed;
            step.y = 0;
        } else if (enemy.getDirection() == Dinosaur.DOWN) {
//            if (enemy.getPosition().cpy().sub(target.getPosition()).y < 3.0f)
//                speed *= 2;

            step.x = 0;
            step.y = -speed;
        } else if (enemy.getDirection() == Dinosaur.UP) {
//            if (enemy.getPosition().cpy().sub(target.getPosition()).x > -3.0f)
//                speed *= 2;

            step.x = 0;
            step.y = speed;
        }

        enemy.setPosition(enemy.getX() + step.x, enemy.getY() + step.y);
    }
    public int getEnemyGridX() {

        return Math.round((enemy.getX() - 1) / 2);
    }

    public int getEnemyGridY() {
        return Math.round((enemy.getY() - 1) / 2);
    }

    public int getTargetGridX() {

        return Math.round((target.getX() - 1) / 2);
    }

    public int getTargetGridY() {
        return Math.round((target.getY() - 1) / 2);
    }

    public boolean playerInFrontOfEnemy() {
        locationCache.set(getEnemyGridX(), getEnemyGridY());
        if (enemy.getDirection() == Dinosaur.UP){
            if(getTargetGridX() == getEnemyGridX() && (getTargetGridY() - getEnemyGridY() < chargeDetectionDistance) &&
                    (getTargetGridY() - getEnemyGridY() > 0)){
                for(int i = 1; i < chargeDetectionDistance; i++) {
                    if (level.getGrid()[(int) locationCache.x][(int) locationCache.y + i] != null) {
                        if (level.getGrid()[(int) locationCache.x][(int) locationCache.y + i].getType() != 5) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        else if (enemy.getDirection() == Dinosaur.DOWN){
            if(getTargetGridX() == getEnemyGridX() && (getEnemyGridY() - getTargetGridY() < chargeDetectionDistance) &&
                    (getEnemyGridY() - getTargetGridY() > 0)){
                for(int i = 1; i < chargeDetectionDistance; i++) {
                    if (level.getGrid()[(int) locationCache.x][(int) locationCache.y - i] != null) {
                        if (level.getGrid()[(int) locationCache.x][(int) locationCache.y - i].getType() != 5) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        else if (enemy.getDirection() == Dinosaur.LEFT){
            if(getTargetGridY() == getEnemyGridY() && (getEnemyGridX() - getTargetGridX() < chargeDetectionDistance) &&
                    (getEnemyGridX() - getTargetGridX() > 0)){

                for(int i = 1; i < chargeDetectionDistance; i++) {
                    if (level.getGrid()[(int) locationCache.x - i][(int) locationCache.y] != null) {
                        if (level.getGrid()[(int) locationCache.x - i][(int) locationCache.y].getType() != 5) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        else if (enemy.getDirection() == Dinosaur.RIGHT){
            if(getTargetGridY() == getEnemyGridY() && (getTargetGridX() - getEnemyGridX() < 5) &&
                    (getTargetGridX() - getEnemyGridX() > 0)){
                for(int i = 1; i < chargeDetectionDistance; i++) {
                    if (level.getGrid()[(int) locationCache.x + i][(int) locationCache.y] != null) {
                        if (level.getGrid()[(int) locationCache.x + i][(int) locationCache.y].getType() == 2) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }

        return false;
    }
}
