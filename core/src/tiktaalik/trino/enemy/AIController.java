package tiktaalik.trino.enemy;

import com.badlogic.gdx.math.Vector2;
import tiktaalik.trino.GameController;
import tiktaalik.trino.GameObject;
import tiktaalik.trino.Level;
import tiktaalik.trino.SoundController;
import tiktaalik.trino.duggi.Dinosaur;
import tiktaalik.trino.duggi.Herbivore;
import tiktaalik.util.PooledList;

import static tiktaalik.trino.GameController.*;

public class AIController {
    public static int LEFT = 0;
    public static int RIGHT = 1;
    public static int FLIP = 2;
    private static float OFFSET = 0.03f;

    private Enemy enemy; // The ship being controlled by this AIController
    private Dinosaur target; // The target dinosaur

    private Vector2 step;
    private int turnAngle;

    private boolean justAvoided; //for shadow duggi

    private static float SHADOW_DUGGI_SPEED = .05f;
    private float defaultSpeed = .035f;
    private float chargingMultiplier = 6;

    private Vector2 locationCache;
    private Level level;

    private int chargeDetectionDistance = 5;

    public AIController(int id, Dinosaur duggi, PooledList<Enemy> enemies, int turnAngle, Level level) {
        this.enemy = enemies.get(id);

        target = duggi;
        step = new Vector2();
        this.turnAngle = turnAngle;
        this.level = level;
        locationCache = new Vector2();
    }

    public AIController(int id, Dinosaur duggi, PooledList<Enemy> enemies, int turnAngle, int type, Level level) {
        this.enemy = enemies.get(id);

        target = duggi;
        step = new Vector2();
        this.turnAngle = turnAngle;
        this.level = level;
        locationCache = new Vector2();
        this.enemy.setEnemyType(type);
    }

    public void step(boolean obstacle) {
        if (enemy.getStunned())
            return;

        if (playerInFrontOfEnemy() && !enemy.getCharging()){

            if (level.getAvatar().getCanBeSeen()){
                SoundController.getInstance().playAlert();
                enemy.setAlert(true);
                enemy.loadCharge();
            }


        }

        if (enemy.getLoadingCharge())
            return;

        if (obstacle || enemy.getCollided()) {
            enemy.setCollided(false);
            if (enemy.getCharging()) {
                SoundController.getInstance().playCrash();
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
        enemy.setGridLocation(getEnemyGridX(), getEnemyGridY());
    }

    public boolean step(PooledList<Vector2> path, boolean obstacle, int inFront){
        //System.out.println("inFront " + inFront);
        if (obstacle) return false;
        if (path.size() == 0) return false;
        //if (enemy.getCollided()) return;
        int x = getEnemyGridX();
        int y = getEnemyGridY();
        //System.out.println("current ["+x+","+y+"]");
        float tmpx = enemy.getX();
        float tmpy = enemy.getY();
        //System.out.println("current ["+tmpx+","+tmpy+"]");

        float gridx = path.getHead().x*2+1;
        float gridy = path.getHead().y*2+1;
        //System.out.println("gridx " + gridx + ", gridy " + gridy );
        //System.out.println("path head [" + path.getHead().x + "," + path.getHead().y + "]");
        float dx = gridx - tmpx;
        float dy = gridy - tmpy;

        if (Math.abs(dx) >= Math.abs(dy)){
            if (dx < -OFFSET)
                enemy.setDirection(Dinosaur.LEFT);
            else if (dx > OFFSET)
                enemy.setDirection(Dinosaur.RIGHT);
        }
        else{
            if (dy < -OFFSET)
                enemy.setDirection(Dinosaur.DOWN);
            else if (dy > OFFSET)
                enemy.setDirection(Dinosaur.UP);
        }

        if (path.getHead().x == x && path.getHead().y == y){
            if (Math.abs(dx) <= OFFSET && Math.abs(dy) <= OFFSET) {
                path.removeHead();
                if (justAvoided) justAvoided = false;
            }
            else{
                //System.out.println("adjusting stuff, dx " + dx + ", dy" + dy);
                if (dx < -OFFSET) tmpx = tmpx - SHADOW_DUGGI_SPEED;
                else if (dx > OFFSET) tmpx = tmpx + SHADOW_DUGGI_SPEED;
                if (dy < -OFFSET) tmpy = tmpy - SHADOW_DUGGI_SPEED;
                else if (dy > OFFSET) tmpy = tmpy + SHADOW_DUGGI_SPEED;
                //System.out.println("after adjust, x " + tmpx +", y" + tmpy);
                enemy.setPosition(tmpx, tmpy);
                return false;
            }
        }
        if (path.size() == 0) return true;
        if (inFront == -1 && !justAvoided) {
            if (path.getHead().y == 0 &&
                    (level.getGridObject((int)path.getHead().x, (int)path.getHead().y - 1).getType()!= GameController.EDIBLEWALL ||
                    level.getGridObject((int)path.getHead().x, (int)path.getHead().y - 1).getType()!= GameController.WALL ||
                    level.getGridObject((int)path.getHead().x, (int)path.getHead().y - 1).getType()!= GameController.RIVER ||
                    level.getGridObject((int)path.getHead().x, (int)path.getHead().y - 1).getType()!= GameController.BOULDER ||
                    level.getGridObject((int)path.getHead().x, (int)path.getHead().y - 1).getType()!= GameController.GOAL) ||
                    level.getEnemyLocation()[(int)path.getHead().x][(int)path.getHead().y-1]){
                if (dx > 0.5){
                    if (path.getHead().y == 0 &&
                            (level.getGridObject((int)path.getHead().x+1, (int)path.getHead().y - 1).getType()!= GameController.EDIBLEWALL ||
                            level.getGridObject((int)path.getHead().x+1, (int)path.getHead().y - 1).getType()!= GameController.WALL ||
                            level.getGridObject((int)path.getHead().x+1, (int)path.getHead().y - 1).getType()!= GameController.RIVER ||
                            level.getGridObject((int)path.getHead().x+1, (int)path.getHead().y - 1).getType()!= GameController.BOULDER ||
                            level.getGridObject((int)path.getHead().x+1, (int)path.getHead().y - 1).getType()!= GameController.GOAL) ||
                            level.getEnemyLocation()[(int)path.getHead().x+1][(int)path.getHead().y-1]){
                        path.add(0,new Vector2(path.getHead().x, path.getHead().y + 1));
                    }
                }
                else{
                    if (path.getHead().y == 0 &&
                            (level.getGridObject((int)path.getHead().x+1, (int)path.getHead().y - 1).getType()!= GameController.EDIBLEWALL ||
                                    level.getGridObject((int)path.getHead().x-1, (int)path.getHead().y - 1).getType()!= GameController.WALL ||
                                    level.getGridObject((int)path.getHead().x-1, (int)path.getHead().y - 1).getType()!= GameController.RIVER ||
                                    level.getGridObject((int)path.getHead().x-1, (int)path.getHead().y - 1).getType()!= GameController.BOULDER ||
                                    level.getGridObject((int)path.getHead().x-1, (int)path.getHead().y - 1).getType()!= GameController.GOAL) ||
                            level.getEnemyLocation()[(int)path.getHead().x+1][(int)path.getHead().y-1]){
                        path.add(0,new Vector2(path.getHead().x, path.getHead().y + 1));
                    }
                }
            }
            else{
                if (dx > 0.5){
                    if (path.getHead().y == 0 &&
                            (level.getGridObject((int)path.getHead().x+1, (int)path.getHead().y + 1).getType()!= GameController.EDIBLEWALL ||
                                    level.getGridObject((int)path.getHead().x+1, (int)path.getHead().y + 1).getType()!= GameController.WALL ||
                                    level.getGridObject((int)path.getHead().x+1, (int)path.getHead().y + 1).getType()!= GameController.RIVER ||
                                    level.getGridObject((int)path.getHead().x+1, (int)path.getHead().y + 1).getType()!= GameController.BOULDER ||
                                    level.getGridObject((int)path.getHead().x+1, (int)path.getHead().y + 1).getType()!= GameController.GOAL) ||
                            level.getEnemyLocation()[(int)path.getHead().x+1][(int)path.getHead().y+1]){
                        path.add(0,new Vector2(path.getHead().x, path.getHead().y + 1));
                    }
                }
                else{
                    if (path.getHead().y == 0 &&
                            (level.getGridObject((int)path.getHead().x+1, (int)path.getHead().y + 1).getType()!= GameController.EDIBLEWALL ||
                                    level.getGridObject((int)path.getHead().x-1, (int)path.getHead().y + 1).getType()!= GameController.WALL ||
                                    level.getGridObject((int)path.getHead().x-1, (int)path.getHead().y + 1).getType()!= GameController.RIVER ||
                                    level.getGridObject((int)path.getHead().x-1, (int)path.getHead().y + 1).getType()!= GameController.BOULDER ||
                                    level.getGridObject((int)path.getHead().x-1, (int)path.getHead().y + 1).getType()!= GameController.GOAL) ||
                            level.getEnemyLocation()[(int)path.getHead().x+1][(int)path.getHead().y+1]){
                        path.add(0,new Vector2(path.getHead().x, path.getHead().y - 1));
                    }
                }
            }
            justAvoided = true;
        }
        else if (inFront == 1 && !justAvoided) {
            if (path.getHead().x == 0 &&
                    (level.getGridObject((int)path.getHead().x -1, (int)path.getHead().y).getType()!= GameController.EDIBLEWALL ||
                    level.getGridObject((int)path.getHead().x-1, (int)path.getHead().y).getType()!= GameController.WALL ||
                    level.getGridObject((int)path.getHead().x-1, (int)path.getHead().y).getType()!= GameController.RIVER ||
                    level.getGridObject((int)path.getHead().x-1, (int)path.getHead().y).getType()!= GameController.BOULDER ||
                    level.getGridObject((int)path.getHead().x-1, (int)path.getHead().y).getType()!= GameController.GOAL) ||
                    level.getEnemyLocation()[(int)path.getHead().x-1][(int)path.getHead().y]){
                if (dx > 0.5){
                    if (path.getHead().y == 0 &&
                            (level.getGridObject((int)path.getHead().x-1, (int)path.getHead().y + 1).getType()!= GameController.EDIBLEWALL ||
                                    level.getGridObject((int)path.getHead().x-1, (int)path.getHead().y + 1).getType()!= GameController.WALL ||
                                    level.getGridObject((int)path.getHead().x-1, (int)path.getHead().y + 1).getType()!= GameController.RIVER ||
                                    level.getGridObject((int)path.getHead().x-1, (int)path.getHead().y + 1).getType()!= GameController.BOULDER ||
                                    level.getGridObject((int)path.getHead().x-1, (int)path.getHead().y + 1).getType()!= GameController.GOAL) ||
                            level.getEnemyLocation()[(int)path.getHead().x-1][(int)path.getHead().y+1]){
                        path.add(0,new Vector2(path.getHead().x-1, path.getHead().y ));
                    }
                }
                else{
                    if (path.getHead().y == 0 &&
                            (level.getGridObject((int)path.getHead().x-1, (int)path.getHead().y - 1).getType()!= GameController.EDIBLEWALL ||
                                    level.getGridObject((int)path.getHead().x-1, (int)path.getHead().y - 1).getType()!= GameController.WALL ||
                                    level.getGridObject((int)path.getHead().x-1, (int)path.getHead().y - 1).getType()!= GameController.RIVER ||
                                    level.getGridObject((int)path.getHead().x-1, (int)path.getHead().y - 1).getType()!= GameController.BOULDER ||
                                    level.getGridObject((int)path.getHead().x-1, (int)path.getHead().y - 1).getType()!= GameController.GOAL) ||
                            level.getEnemyLocation()[(int)path.getHead().x+1][(int)path.getHead().y-1]){
                        path.add(0,new Vector2(path.getHead().x-1, path.getHead().y - 1));
                    }
                }
            }
            else{
                if (dy > 0.5){
                    if (path.getHead().x == 0 &&
                            (level.getGridObject((int)path.getHead().x+1, (int)path.getHead().y + 1).getType()!= GameController.EDIBLEWALL ||
                                    level.getGridObject((int)path.getHead().x+1, (int)path.getHead().y + 1).getType()!= GameController.WALL ||
                                    level.getGridObject((int)path.getHead().x+1, (int)path.getHead().y + 1).getType()!= GameController.RIVER ||
                                    level.getGridObject((int)path.getHead().x+1, (int)path.getHead().y + 1).getType()!= GameController.BOULDER ||
                                    level.getGridObject((int)path.getHead().x+1, (int)path.getHead().y + 1).getType()!= GameController.GOAL) ||
                            level.getEnemyLocation()[(int)path.getHead().x+1][(int)path.getHead().y+1]){
                        path.add(0,new Vector2(path.getHead().x+1, path.getHead().y ));
                    }
                }
                else{
                    if (path.getHead().x == 0 &&
                            (level.getGridObject((int)path.getHead().x+1, (int)path.getHead().y - 1).getType()!= GameController.EDIBLEWALL ||
                                    level.getGridObject((int)path.getHead().x+1, (int)path.getHead().y - 1).getType()!= GameController.WALL ||
                                    level.getGridObject((int)path.getHead().x+1, (int)path.getHead().y - 1).getType()!= GameController.RIVER ||
                                    level.getGridObject((int)path.getHead().x+1, (int)path.getHead().y - 1).getType()!= GameController.BOULDER ||
                                    level.getGridObject((int)path.getHead().x+1, (int)path.getHead().y - 1).getType()!= GameController.GOAL) ||
                            level.getEnemyLocation()[(int)path.getHead().x+1][(int)path.getHead().y-1]){
                        path.add(0,new Vector2(path.getHead().x + 1, path.getHead().y ));
                    }
                }
            }
            justAvoided = true;
        }

        if (Math.abs(tmpx - gridx)<SHADOW_DUGGI_SPEED){
            tmpx = gridx;
        }
        else if (path.getHead().x > x){
            tmpx += SHADOW_DUGGI_SPEED;
        }
        else {
            tmpx -= SHADOW_DUGGI_SPEED;
        }
        if (Math.abs(tmpy - gridy)<SHADOW_DUGGI_SPEED){
            tmpy = gridy;
        }
        else if (path.getHead().y > y){
            tmpy += SHADOW_DUGGI_SPEED;
        }
        else {
            tmpy -= SHADOW_DUGGI_SPEED;
        }
        //System.out.println("after ["+x+","+y+"]");
        enemy.setPosition(tmpx, tmpy);
        enemy.setGridLocation(getEnemyGridX(), getEnemyGridY());
        return false;

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
