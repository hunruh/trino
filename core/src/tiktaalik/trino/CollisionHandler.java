package tiktaalik.trino;

import com.badlogic.gdx.Game;
import tiktaalik.trino.duggi.Carnivore;
import tiktaalik.trino.duggi.Dinosaur;
import tiktaalik.trino.enemy.Enemy;
import tiktaalik.trino.environment.Wall;

public class CollisionHandler {
    public void processCollision(GameObject g1, GameObject g2) {
        if (g1.getType() == GameController.DUGGI) {
            if (g2.getType() == GameController.ENEMY)
                handleCollision((Dinosaur)g1, (Enemy)g2);
            else if (g2.getType() == GameController.WALL)
                handleCollision((Dinosaur)g1, (Wall)g2);
        } else if (g2.getType() == GameController.DUGGI) {
            if (g1.getType() == GameController.ENEMY)
                handleCollision((Dinosaur)g2, (Enemy)g1);
            else if (g1.getType() == GameController.WALL)
                handleCollision((Dinosaur)g2, (Wall)g1);
        }
    }

    public void handleCollision(Dinosaur duggi, Enemy e) {
        if (duggi.getForm() == Dinosaur.CARNIVORE_FORM && ((Carnivore) duggi).getCharging())
            e.setStunned();
//        else if (!e.getStunned())
//            setFailure(true);
    }

    public void handleCollision(Dinosaur duggi, Wall w) {
//        if (isInFrontOfAvatar(bd2)){
//            // play sound effect
//            collideWall.pause();
//            collideWall.play(1.0f);
//        }
    }

    public void handleCollision(GameObject bd1, GameObject bd2) {
//        if (bd1.getType() == GameController.CLONE){
//            if (bd2.getType() == GameController.ENEMY)
//                removeClone = true;
//        }
//        else if (bd2.getType() == GameController.CLONE){
//            if (bd1.getType() == GameController.ENEMY)
//                removeClone = true;
//        }
//        else
//            removeClone = false;
//
//        if (bd1.getType() == GameController.DUGGI){
//            if (bd2.getType() == GameController.GOAL) {
//                if (canExit) {
//                    setComplete(true);
//                }
//            }
//
//        }
//        else if (bd2.getType() == GameController.DUGGI){
//            if (bd1.getType() == GameController.GOAL) {
//                if (canExit)
//                    setComplete(true);
//            }
//        }
    }
}
