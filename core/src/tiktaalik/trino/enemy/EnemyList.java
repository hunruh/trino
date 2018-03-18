/*
 * ShipList.java
 *
 * Like PhotonPool, this class manages a large number of objects in the game, many of
 * which can be deleted.  However, since we are never adding new ships to the game --
 * only taking them away -- this makes this class a lot simpler.
 *
 * Unlike PhotonPool, this method has no update.  Updates are different for players
 * and AI ships, so we have embedded
 *
 * This class does have an important similarity to PhotonPool. It implements
 * Iterable<Ship> so that we can use it in for-each loops. BE VERY CAREFUL with
 * java.util.  Those classes are notorious for memory allocation. You will note that,
 * to save memory, we have exactly one iterator that we reused over and over again.
 * This helps with memory, but it means that this object is not even remotely thread-safe.
 * As there is only one thread in the game-loop, this is acceptable.
 *
 * Author: Walker M. White, Cristian Zaloj
 * Based on original AI Game Lab by Yi Xu and Don Holden, 2007
 * LibGDX version, 1/24/2015
 */

package tiktaalik.trino.enemy;

//LIMIT JAVA.UTIL TO THE INTERFACES
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import tiktaalik.trino.Canvas;
import tiktaalik.trino.GameObject;
//import tiktaalik.trino.obstacle.Obstacle;
import tiktaalik.util.PooledList;

/**
 * This class provides a list of ships for the game.
 *
 * This object may be used in for-each loops.  However, IT IS NOT THREAD-SAFE.
 * For memory reasons, this object is backed by a single iterator object that
 * is reset every single time we start a new for-each loop.
 */
public class EnemyList implements Iterable<Enemy> {

    /** The list of ships managed by this object. */
    private Enemy[] enemies;
    /** The amount of time that has passed since creation (for animation) */
    private float time;
    /** Custom iterator so we can use this object in for-each loops */
    private EnemyIterator iterator = new EnemyIterator();
//    /* Texture assets for enemies */
//    private TextureRegion enemyTexture;


    /**
     * Create a new ShipList with the given number of ships.
     *
     * @param size The number of ships to allocate
     */
    public EnemyList(int size, Vector2 scale, TextureRegion enemyTexture,PooledList<GameObject> objects ) {
        enemies = new Enemy[size];
        float dwidth = enemyTexture.getRegionWidth() / scale.x;
        float dheight = enemyTexture.getRegionHeight() / scale.y;
        for (int ii = 0; ii < size; ii++) {
            enemies[ii] = new Enemy(28.25f, 5.0f,dwidth);
            enemies[ii].setDrawScale(scale);
    		enemies[ii].setTexture(enemyTexture);
            enemies[ii].setId(ii);
            enemies[ii].setIsAlive(true);
            objects.add(enemies[ii]);
        }
    }

//    public void setEnemyTexture(TextureRegion eTexture){
//        this.enemyTexture = eTexture;
//    }
//
//    public TextureRegion getEnemyTexture(){
//        return this.enemyTexture;
//    }

    /**
     * Returns the number of ships in this list
     *
     * @return the number of ships in this list
     */
    public int size() {
        return enemies.length;
    }

    /**
     * Returns the ship for the given (unique) id
     *
     * The value given must be between 0 and size-1.
     *
     * @return the ship for the given id
     */
    public Enemy get(int id) {
        return enemies[id];
    }

    /**
     * Returns the number of ships alive at the end of an update.
     *
     * @return the number of ships alive at the end of an update.
     */
    public int numAlive() {
        int enemiesAlive = 0;
        for (Enemy e : this) {
            if (e.getIsAlive()) {
                enemiesAlive++;
            }
        }
        return enemiesAlive;
    }


    /**
     * Draws the ships to the given canvas.
     *
     * This method draws all of the ships in this list. It should be the second drawing
     * pass in the GameEngine.
     *
     * @param canvas the drawing context
     */
    public void draw(Canvas canvas) {
        // Increment the animation factor
        time += 0.05f;

//        for (Enemy s : this) {
//            // Draw the ship
//            TexturedMesh model = (s.getId() == 0 ? enemyMesh : playerMesh);
//            canvas.drawShip(model, s.getX(), s.getY(), s.getFallAmount(), s.getAngle());
//        }
    }

    public void addObjects(PooledList<GameObject> objects){
        for (Enemy e :this){
            objects.add(e);
        }
    }

    /**
     * Generates the Perlin Noise for the after burner
     *
     * Cristian came up with these numbers (and did not document them :( ).  I have
     * no idea what they mean.
     *
     * @param fx seed value for random noise.
     */
    private float generateNoise(float fx) {
        float noise = (float)(188768.0 * Math.pow(fx, 10));
        noise -= (float)(874256.0 * Math.pow(fx, 9));
        noise += (float)(1701310.0 * Math.pow(fx, 8));
        noise -= (float)(1804590.0 * Math.pow(fx, 7));
        noise += (float)(1130570.0 * Math.pow(fx, 6));
        noise -= (float)(422548.0 * Math.pow(fx, 5));
        noise += (float)(89882.7 * Math.pow(fx, 4));
        noise -= (float)(9425.33 * Math.pow(fx, 3));
        noise += (float)(276.413 * fx * fx);
        noise += (float)(14.3214 * fx);
        return noise;
    }

    /**
     * Returns a ship iterator, satisfying the Iterable interface.
     *
     * This method allows us to use this object in for-each loops.
     *
     * @return a ship iterator.
     */
    public Iterator<Enemy> iterator() {
        // Take a snapshot of the current state and return iterator.
        iterator.pos = 0;
        return iterator;
    }

    /**
     * Implementation of a custom iterator.
     *
     * Iterators are notorious for making new objects all the time.  We make
     * a custom iterator to cut down on memory allocation.
     */
    private class EnemyIterator implements Iterator<Enemy> {
        /** The current position in the ship list */
        public int pos = 0;

        /**
         * Returns true if there are still items left to iterate.
         *
         * @return true if there are still items left to iterate
         */
        public boolean hasNext() {
            return pos < enemies.length;
        }

        /**
         * Returns the next ship.
         *
         * Dead ships are skipped, but inactive ships are not skipped.
         */
        public Enemy next() {
            if (pos >= enemies.length) {
                throw new NoSuchElementException();
            }
            int idx = pos;
            do {
                pos++;
            } while (pos < enemies.length && !enemies[pos].getIsAlive());
            return enemies[idx];
        }

        @Override
        public void remove() {

        }
    }
}