package tiktaalik.trino;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.audio.Sound;
import tiktaalik.trino.duggi.Carnivore;
import tiktaalik.trino.duggi.Clone;
import tiktaalik.trino.duggi.Dinosaur;
import tiktaalik.trino.enemy.Enemy;
import tiktaalik.trino.environment.Wall;
import tiktaalik.trino.environment.Boulder;
import tiktaalik.trino.environment.River;

public class CollisionHandler {
    GameController parent;
    Level level;
    private int cloneTime = 0;

    public CollisionHandler(GameController parent) {

        this.parent = parent;
        this.level = null;
    }
    public void setLevel(Level level){
        this.level = level;
    }

    public void processCollision(GameObject g1, GameObject g2) {
        if (g1.getType() == GameController.DUGGI) {
            if (g2.getType() == GameController.ENEMY) {
                handleCollision((Dinosaur) g1, (Enemy) g2);
            }
            else if (g2.getType() == GameController.WALL || g2.getType() == GameController.EDIBLEWALL ||
                    g2.getType() == GameController.GOAL) {
                handleCollision((Dinosaur) g1, (Wall) g2);
            }
            else if (g2.getType() == GameController.BOULDER) {
                handleCollision((Dinosaur) g1, (Boulder) g2);
            }
            else if (g2.getType() == GameController.RIVER) {
                handleCollision((Dinosaur) g1, (River) g2);
            }
        } else if (g2.getType() == GameController.DUGGI) {
            if (g1.getType() == GameController.ENEMY) {
                handleCollision((Dinosaur) g2, (Enemy) g1);
            }
            else if (g1.getType() == GameController.WALL || g1.getType() == GameController.EDIBLEWALL ||
                    g1.getType() == GameController.GOAL) {
                handleCollision((Dinosaur) g2, (Wall) g1);
            }
            else if (g1.getType() == GameController.BOULDER) {
                handleCollision((Dinosaur) g2, (Boulder) g1);
            }
            else if (g1.getType() == GameController.RIVER) {
                handleCollision((Dinosaur) g2, (River) g1);
            }
        }

        if (g1.getType() == GameController.CLONE) {
            if (g2.getType() == GameController.ENEMY) {
                handleCollision((Clone) g1, (Enemy) g2);
            }
        } else if (g2.getType() == GameController.CLONE) {
            if (g1.getType() == GameController.ENEMY) {
                handleCollision((Clone) g2, (Enemy) g1);
            }
        }

        if (g1.getType() == GameController.ENEMY) {
            if (g2.getType() == GameController.ENEMY) {
                handleCollision((Enemy) g1, (Enemy) g2);
            }
            else if (g2.getType() == GameController.RIVER) {
                handleCollision((Enemy) g1, (River) g2);
            }

            else if (g2.getType() == GameController.WALL || g2.getType() == GameController.EDIBLEWALL || g2.getType() ==
                    GameController.GOAL) {
                handleCollision((Enemy) g1, (Wall) g2);
            }

            else if (g2.getType() == GameController.BOULDER) {
                handleCollision((Enemy) g1, (Boulder) g2);
            }
        }

        else if (g1.getType() == GameController.RIVER) {
            if (g2.getType() == GameController.ENEMY) {
                handleCollision((Enemy) g2, (River) g1);
            }
        }

        else if (g1.getType() == GameController.WALL || g1.getType() == GameController.EDIBLEWALL) {
            if (g2.getType() == GameController.ENEMY) {
                handleCollision((Enemy) g2, (Wall) g1);
            }
        }

        else if (g1.getType() == GameController.BOULDER) {
            if (g2.getType() == GameController.ENEMY) {
                handleCollision((Enemy) g2, (Boulder) g1);
            }
        }
        else if (g1.getType() == GameController.GOAL){
            if (g2.getType() == GameController.ENEMY){
                handleCollision((Enemy)g2, (Wall)g1);
            }
        }
    }

    public void handleCollision(Dinosaur d, Enemy e) {
        if (d.getForm() == Dinosaur.CARNIVORE_FORM && d.getActionInProgress()) {
            if (e.getEnemyType() == Enemy.UNKILLABLE_ENEMY){
                ((Carnivore) d).setCollided(true);
                d.stopAction();
                parent.setFailure(true);
                return;
            }
            e.setStunned();
            ((Carnivore) d).setCollided(true);
            d.stopAction();
        }
        else if (!e.getStunned())
            parent.setFailure(true);
    }

    public void handleCollision(Dinosaur d, Boulder b) {
        if (d.getForm() == Dinosaur.CARNIVORE_FORM && d.getActionInProgress() && !((Carnivore) d).getPushing() &&
                level.isInFrontOfAvatar(b)) {
            level.pushBoulder(d, b);
        } else if (d.getForm() == Dinosaur.CARNIVORE_FORM && !d.getActionInProgress()) {
            ((Carnivore) d).setNextToBoulder(b);
        }
    }

    public void handleCollision(Dinosaur d, Wall w) {
        if (w.getType() == GameController.GOAL) {
            if (d.canExit() || (w.getGoal() && w.getLowered())) {
                parent.setComplete(true);
            }
            else {
                parent.setComplete(false);
            }
        }

        if (level.isInFrontOfAvatar(w)) {
            SoundController.getInstance().playCollide();
        }
    }

    public void handleCollision(Dinosaur d, River r) {

    }

    public void handleCollision(Clone c, Enemy e) {
        c.startCountDown();
        e.setEatingClone(true);
        c.setEnemy(e);
    }

    public void handleCollision(Enemy e1, Enemy e2) {
        e1.setCollided(true);
        e2.setCollided(true);
    }

    public void handleCollision(Enemy e, River r) {
        e.setCollided(true);
    }

    public void handleCollision(Enemy e, Wall w) {
        e.setCollided(true);
    }

    public void handleCollision(Enemy e, Boulder b) {
        e.setCollided(true);
        if (e.getCharging()) {
            e.setStunned();
        }
    }

}
