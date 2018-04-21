package tiktaalik.trino.enemy;

import com.badlogic.gdx.math.Vector2;
import tiktaalik.trino.GameObject;
import tiktaalik.trino.Level;
import tiktaalik.trino.duggi.Dinosaur;
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
    private float chargingMultiplier = 6;

    private Vector2 locationCache;
    private Level level;

    private int chargeDetectionDistance = 5;

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

        if (playerInFrontOfEnemy() && !enemy.getCharging()){
            enemy.loadCharge();
        }

        if (enemy.getLoadingCharge())
            return;

        if (obstacle || enemy.getCollided()) {
            enemy.setCollided(false);
            if (enemy.getCharging()) {
                enemy.setStunned();
                enemy.setCharging(false);
                return;
            }
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
        if (enemy.getCharging())
            speed *= chargingMultiplier;

        if (enemy.getDirection() == Dinosaur.LEFT) {
            step.x = -speed;
            step.y = 0;
        } else if (enemy.getDirection() == Dinosaur.RIGHT) {
            step.x = speed;
            step.y = 0;
        } else if (enemy.getDirection() == Dinosaur.DOWN) {
            step.x = 0;
            step.y = -speed;
        } else if (enemy.getDirection() == Dinosaur.UP) {
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
                    (getTargetGridY() - getEnemyGridY() > 0)) {
                for(int i = 1; i < Math.abs(getEnemyGridY() - getTargetGridY()); i++) {
                    GameObject g = level.getGrid()[(int) locationCache.x][(int) locationCache.y + i];
                    if (g != null) {
                        if (g.getType() == RIVER || g.getType() == WALL || g.getType() == EDIBLEWALL ||
                                g.getType() == BOULDER)
                            return false;
                    }
                }
                return true;
            }
        }
        else if (enemy.getDirection() == Dinosaur.DOWN){
            if(getTargetGridX() == getEnemyGridX() && (getEnemyGridY() - getTargetGridY() < chargeDetectionDistance) &&
                    (getEnemyGridY() - getTargetGridY() > 0)){
                for(int i = 1; i < Math.abs(getEnemyGridY() - getTargetGridY()); i++) {
                    GameObject g = level.getGrid()[(int) locationCache.x][(int) locationCache.y - i];
                    if (g != null) {
                        if (g.getType() == RIVER || g.getType() == WALL || g.getType() == EDIBLEWALL ||
                                g.getType() == BOULDER)
                            return false;
                    }
                }
                return true;
            }
        }
        else if (enemy.getDirection() == Dinosaur.LEFT){
            if(getTargetGridY() == getEnemyGridY() && (getEnemyGridX() - getTargetGridX() < chargeDetectionDistance) &&
                    (getEnemyGridX() - getTargetGridX() > 0)){

                for(int i = 1; i < Math.abs(getEnemyGridX() - getTargetGridX()); i++) {
                    GameObject g = level.getGrid()[(int) locationCache.x - i][(int) locationCache.y];
                    if (g != null) {
                        if (g.getType() == RIVER || g.getType() == WALL || g.getType() == EDIBLEWALL ||
                                g.getType() == BOULDER)
                            return false;
                    }
                }
                return true;
            }
        }
        else if (enemy.getDirection() == Dinosaur.RIGHT){
            if(getTargetGridY() == getEnemyGridY() && (getTargetGridX() - getEnemyGridX() < chargeDetectionDistance) &&
                    (getTargetGridX() - getEnemyGridX() > 0)){
                for(int i = 1; i < Math.abs(getEnemyGridX() - getTargetGridX()); i++) {
                    GameObject g = level.getGrid()[(int) locationCache.x + i][(int) locationCache.y];
                    if (g != null) {
                        if (g.getType() == RIVER || g.getType() == WALL || g.getType() == EDIBLEWALL ||
                                g.getType() == BOULDER)
                            return false;
                    }
                }
                return true;
            }
        }

        return false;
    }
}
