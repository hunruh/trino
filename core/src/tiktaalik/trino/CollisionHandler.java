package tiktaalik.trino;

import tiktaalik.trino.duggi.Carnivore;
import tiktaalik.trino.duggi.Clone;
import tiktaalik.trino.duggi.Dinosaur;
import tiktaalik.trino.enemy.Enemy;
import tiktaalik.trino.environment.Wall;
import tiktaalik.trino.environment.Boulder;
import tiktaalik.trino.environment.River;

public class CollisionHandler {
    GameController parent;

    public CollisionHandler(GameController parent) {
        this.parent = parent;
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

            else if (g2.getType() == GameController.WALL || g2.getType() == GameController.EDIBLEWALL) {
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
    }

    public void handleCollision(Dinosaur duggi, Enemy e) {
        if (duggi.getForm() == Dinosaur.CARNIVORE_FORM && ((Carnivore) duggi).getCharging()) {
            e.setStunned();
            ((Carnivore) duggi).setCollided(true);
        }
        else if (!e.getStunned())
            parent.setFailure(true);
    }

    public void handleCollision(Dinosaur duggi, Boulder b) {
        if (duggi.getForm() == Dinosaur.CARNIVORE_FORM && ((Carnivore) duggi).getCharging()) {
            b.setPushed();
            ((Carnivore) duggi).setCollided(true);
        }
    }

    public void handleCollision(Dinosaur d, Wall w) {
        if (w.getType() == GameController.GOAL) {

            if (d.canExit()) {
                parent.setComplete(true);
            }
            else {
                parent.setComplete(false);
            }
        }
        if (d.getForm() == Dinosaur.CARNIVORE_FORM && ((Carnivore) d).getCharging()) {
            ((Carnivore) d).setCollided(true);
        }

//        if (parent.isInFrontOfAvatar(w))
//            SoundController.getInstance().playCollide();
    }

    public void handleCollision(Clone c, Enemy e) {
        c.setRemoved(true);
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
    }
}
