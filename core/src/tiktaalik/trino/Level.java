package tiktaalik.trino;

import static tiktaalik.trino.GameController.*;
import static tiktaalik.trino.duggi.Dinosaur.*;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import tiktaalik.trino.duggi.Clone;
import tiktaalik.trino.duggi.Dinosaur;
import tiktaalik.trino.duggi.Doll;
import tiktaalik.trino.enemy.Enemy;
import tiktaalik.trino.environment.*;
import tiktaalik.trino.lights.LightSource;
import tiktaalik.util.PooledList;

import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;

public class Level {
    private static final float DEFAULT_WIDTH  = 32.0f; // Width of the game world in Box2d units
    private static final float DEFAULT_HEIGHT = 9.0f; // Height of the game world in Box2d units

    protected PooledList<GameObject> objects  = new PooledList<GameObject>(); // All the objects in the world
    protected PooledList<GameObject> drawObjects  = new PooledList<GameObject>(); // Sortable list of objects for draw

    private PooledList<Wall> walls = new PooledList<Wall>();
    private PooledList<CottonFlower> cottonFlowers = new PooledList<CottonFlower>();
    private PooledList<River> rivers = new PooledList<River>();
    private PooledList<Boulder> boulders = new PooledList<Boulder>();
    private PooledList<Enemy> enemies = new PooledList<Enemy>();
    private PooledList<FireFly> fireFlies = new PooledList<FireFly>();

    private GameObject[][] grid;
    private Rectangle bounds;
    private Vector2 scale;

    private World world;

    private Dinosaur avatar;
    private Clone clone;
    private Wall goalDoor;

    private GameObject objectCache;
    private Vector2 locationCache;

    private TextureRegion background;
    private TextureRegion cloneTexture;

    public Level(World world) {
        this.bounds = new Rectangle(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        this.world = world;
        scale = new Vector2();
        locationCache = new Vector2();
        grid = new GameObject[(int) bounds.width][(int) bounds.height];
    }

    public boolean inBounds(GameObject g) {
        boolean horiz = (bounds.x <= g.getX() && g.getX() <= bounds.x+bounds.width);
        boolean vert  = (bounds.y <= g.getY() && g.getY() <= bounds.y+bounds.height);
        return horiz && vert;
    }

    protected void addObject(GameObject g) {
        assert inBounds(g) : "Object is not in bounds";

        switch (g.getType()) {
            case WALL:
            case EDIBLEWALL:
                walls.add((Wall) g);
                break;
            case COTTON:
                cottonFlowers.add((CottonFlower) g);
                break;
            case ENEMY:
                enemies.add((Enemy) g);
                break;
            case FIREFLY:
                fireFlies.add((FireFly) g);
                break;
            case RIVER:
                rivers.add((River) g);
                break;
            case BOULDER:
                boulders.add((Boulder) g);
                break;
        }

        objects.add(g);

        if (g.getType() != COTTON && g.getType() != SWITCH)
            g.activatePhysics(world);
    }

    public void removeObject(GameObject g) {
        switch (g.getType()) {
            case WALL:
            case EDIBLEWALL:
                walls.remove(g);
                grid[(int)((Wall)g).getGridLocation().x][(int)((Wall)g).getGridLocation().y] = null;
                break;
            case COTTON:
                cottonFlowers.remove(g);
                grid[(int)((CottonFlower)g).getGridLocation().x][(int)((CottonFlower)g).getGridLocation().y] = null;
                break;
            case ENEMY:
                enemies.remove(g);
                break;
            case FIREFLY:
                fireFlies.remove(g);
                break;
            case RIVER:
                rivers.remove(g);
                break;
            case BOULDER:
                boulders.remove(g);
                break;
            case CLONE:
                clone.setRemoved(false);
                clone = null;
                break;
        }

        g.deactivatePhysics(world);
        objects.remove(g);
    }

    public Rectangle getBounds() {
        return bounds;
    }
    
    public int getWidth() {
        return (int) bounds.width;
    }
    
    public int getHeight() {
        return (int) bounds.height;
    }

    public GameObject getGridObject(int x, int y) {
        return grid[x][y];
    }

    public Dinosaur getAvatar() {
        return avatar;
    }

    public void setAvatar(Dinosaur avatar) {
        this.avatar = avatar;
        objects.set(1, this.avatar);
    }

    public Clone getClone() {
        return clone;
    }

    public void placeClone(int x, int y) {
        clone = new Clone(screenToMaze(x), screenToMaze(y), cloneTexture.getRegionWidth() / (scale.x * 2));
        clone.setGridLocation(x, y);
        clone.setDrawScale(scale);
        clone.setType(CLONE);
        clone.setTexture(cloneTexture);
        clone.setBodyType(BodyDef.BodyType.StaticBody);
        addObject(clone);
    }

    public void removeClone() {
        removeObject(clone);
    }

    public Wall getGoalDoor() {
        return goalDoor;
    }

    public Enemy getEnemy(int idx) {
        return enemies.get(idx);
    }

    public PooledList<Enemy> getEnemies() {
        return enemies;
    }

    public FireFly getFirefly(int idx) {
        return fireFlies.get(idx);
    }

    public PooledList<FireFly> getFireFlies() {
        return fireFlies;
    }

    public PooledList<GameObject> getObjects() {
        return objects;
    }

    public void populate(Hashtable<String, TextureRegion> textureDict, LightSource avatarLight,
                         int canvasWidth, int canvasHeight) {
        scale = new Vector2(canvasWidth/bounds.getWidth(), canvasHeight/bounds.getHeight());

        float dwidth;
        float dheight;

        // Set permanent textures
        background = textureDict.get("background");
        cloneTexture = textureDict.get("dollFront");

        // Create player character
        // It is important that this is always created first, as transformations must swap the first element
        // in the objects list
        dwidth = textureDict.get("dollFront").getRegionWidth() / (scale.x * 2);
        avatar = new Doll(screenToMaze(8), screenToMaze(6), dwidth);
        avatar.setType(DUGGI);
        avatar.setTextureSet(textureDict.get("dollLeft"), textureDict.get("dollRight"), textureDict.get("dollBack"),
                textureDict.get("dollFront"));
        avatar.setDrawScale(scale);
        addObject(avatar);
        avatarLight.attachToBody(avatar.getBody(), avatarLight.getX(), avatarLight.getY(), avatarLight.getDirection());

        /** Adding cotton flowers */
        TextureRegion cottonTexture = textureDict.get("cotton");
        dwidth = cottonTexture.getRegionWidth() / scale.x;
        dheight = cottonTexture.getRegionHeight() / scale.y;
        CottonFlower cf1 = new CottonFlower(0,4, screenToMaze(0), screenToMaze(4), dwidth, dheight);
        CottonFlower cf2 = new CottonFlower(9,3, screenToMaze(9), screenToMaze(3), dwidth, dheight);
        CottonFlower cf3 = new CottonFlower(11,6,screenToMaze(11), screenToMaze(6), dwidth, dheight);
        CottonFlower cf4 = new CottonFlower(14,0,screenToMaze(14), screenToMaze(0), dwidth, dheight);
        CottonFlower cf5 = new CottonFlower(17,7,screenToMaze(17), screenToMaze(7), dwidth, dheight);
        CottonFlower cf6 = new CottonFlower(24,7,screenToMaze(24), screenToMaze(7), dwidth, dheight);
        CottonFlower cf7 = new CottonFlower(28,3,screenToMaze(28), screenToMaze(3), dwidth, dheight);
        CottonFlower cf8 = new CottonFlower(31,2,screenToMaze(31), screenToMaze(2), dwidth, dheight);
        CottonFlower cf9 = new CottonFlower(31,6,screenToMaze(31), screenToMaze(6), dwidth, dheight);
        CottonFlower[] cf = new CottonFlower[] {cf1, cf2, cf3, cf4, cf5, cf6, cf7, cf8, cf9};
        for (int i = 0; i < 9; i++) {
            cf[i].setBodyType(BodyDef.BodyType.StaticBody);
            cf[i].setDrawScale(scale);
            cf[i].setTexture(cottonTexture);
            cf[i].setType(COTTON);
            addObject(cf[i]);
            grid[(int)cf[i].getGridLocation().x][(int)cf[i].getGridLocation().y] = cf[i];
        }

        // Adding river
//        dwidth = textureDict.get("river").getRegionWidth() / scale.x;
//        dheight = textureDict.get("river").getRegionHeight() / scale.y;
//        River r1 = new River(4,3,screenToMaze(4),screenToMaze(3),dwidth,dheight, false);
//        River r2 = new River(4,4,screenToMaze(4),screenToMaze(4),dwidth,dheight, false);
//        River r3 = new River(4,5,screenToMaze(4),screenToMaze(5),dwidth,dheight, false);
//        River r4 = new River(4,6,screenToMaze(4),screenToMaze(6),dwidth,dheight, false);
//        River r5 = new River(4,7,screenToMaze(4),screenToMaze(7),dwidth,dheight, false);
//        River r6 = new River(5,3,screenToMaze(5),screenToMaze(3),dwidth,dheight, false);
//        River r7 = new River(5,4,screenToMaze(5),screenToMaze(4),dwidth,dheight, false);
//        River r8 = new River(5,5,screenToMaze(5),screenToMaze(5),dwidth,dheight, false);
//        River r9 = new River(5,6,screenToMaze(5),screenToMaze(6),dwidth,dheight, false);
//        River r10 = new River(5,7,screenToMaze(5),screenToMaze(7),dwidth,dheight, false);
//        River r11 = new River(12,5,screenToMaze(12),screenToMaze(5),dwidth,dheight, false);
//        River r12 = new River(13,5,screenToMaze(13),screenToMaze(5),dwidth,dheight, false);
//        River r13 = new River(14,5,screenToMaze(14),screenToMaze(5),dwidth,dheight, false);
//        River r14 = new River(15,5,screenToMaze(15),screenToMaze(5),dwidth,dheight, false);
//        River r15 = new River(16,5,screenToMaze(16),screenToMaze(5),dwidth,dheight, false);
//        River r16 = new River(19,2,screenToMaze(19),screenToMaze(2),dwidth,dheight, false);
//        River r17 = new River(20,2,screenToMaze(20),screenToMaze(2),dwidth,dheight, false);
//        River r18 = new River(21,2,screenToMaze(21),screenToMaze(2),dwidth,dheight, false);
//        River r19 = new River(22,2,screenToMaze(22),screenToMaze(2),dwidth,dheight, false);
//        River r20 = new River(23,2,screenToMaze(23),screenToMaze(2),dwidth,dheight, false);
//        River r21 = new River(24,2,screenToMaze(24),screenToMaze(2),dwidth,dheight, false);
//        River[] riv = new River[] {r1,r2,r3,r4,r5,r6,r7,r8,r9,r10,r11,r12,r13,r14,r15,r16,r17,r18,r19,r20,r21};
//        for (int i = 0; i < 21; i++) {
//            riv[i].setBodyType(BodyDef.BodyType.StaticBody);
//            riv[i].setDrawScale(scale);
//            riv[i].setTexture(textureDict.get("river"));
//            riv[i].setType(RIVER);
//            addObject(riv[i]);
//            grid[(int)riv[i].getGridLocation().x-1][(int)riv[i].getGridLocation().y-1] = riv[i];
//        }

        dwidth = textureDict.get("boulder").getRegionWidth() / scale.x;
        dheight = textureDict.get("boulder").getRegionHeight() / scale.y;
        Boulder b1 = new Boulder(6,1,screenToMaze(6),screenToMaze(1),dwidth,dheight, false);
        Boulder b2 = new Boulder(7,2,screenToMaze(7),screenToMaze(2),dwidth,dheight, false);
        Boulder b3 = new Boulder(25,5,screenToMaze(25),screenToMaze(5),dwidth,dheight, false);
        Boulder b4 = new Boulder(25,6,screenToMaze(25),screenToMaze(6),dwidth,dheight, false);
        Boulder b5 = new Boulder(25,7,screenToMaze(25),screenToMaze(7),dwidth,dheight, false);
        Boulder[] b = new Boulder[] {b1,b2,b3,b4,b5};
        for (int i = 0; i < 5; i++) {
            b[i].setBodyType(BodyDef.BodyType.StaticBody);
            b[i].setDrawScale(scale);
            b[i].setTexture(textureDict.get("boulder"));
            b[i].setType(BOULDER);
            addObject(b[i]);
            grid[(int)b[i].getGridLocation().x][(int)b[i].getGridLocation().y] = b[i];
        }

        // Switch
        dwidth = textureDict.get("switch").getRegionWidth() / scale.x;
        dheight = textureDict.get("switch").getRegionHeight() / scale.y;
        // Switch texture
        Switch s = new Switch(12,4,screenToMaze(12),screenToMaze(4),dwidth,dheight);
        s.setBodyType(BodyDef.BodyType.StaticBody);
        s.setDrawScale(scale);
        s.setTexture(textureDict.get("switch"));
        s.setType(SWITCH);
        addObject(s);
        grid[(int)s.getGridLocation().x][(int)s.getGridLocation().y] = s;

        /** Adding inedible walls */
        Wall iw1 = new Wall(0,0,screenToMaze(0), screenToMaze(1), dwidth, dheight, false);
        Wall iw2 = new Wall(0,6,screenToMaze(0), screenToMaze(7), dwidth, dheight, false);
        Wall iw3 = new Wall(1,4,screenToMaze(1), screenToMaze(5), dwidth, dheight, false);
        Wall iw4 = new Wall(2,2,screenToMaze(2), screenToMaze(3), dwidth, dheight, false);
        Wall iw5 = new Wall(2,3,screenToMaze(2), screenToMaze(4), dwidth, dheight, false);
        Wall iw6 = new Wall(2,4,screenToMaze(2), screenToMaze(5), dwidth, dheight, false);
        Wall iw7 = new Wall(2,5,screenToMaze(2), screenToMaze(6), dwidth, dheight, false);
        Wall iw8 = new Wall(2,6,screenToMaze(2), screenToMaze(7), dwidth, dheight, false);
        Wall iw9 = new Wall(5,2,screenToMaze(5), screenToMaze(3), dwidth, dheight, false);
        Wall iw10 = new Wall(5,4,screenToMaze(5), screenToMaze(5), dwidth, dheight, false);
        Wall iw11 = new Wall(5,5,screenToMaze(5), screenToMaze(6), dwidth, dheight, false);
        Wall iw12 = new Wall(5,6,screenToMaze(5), screenToMaze(7), dwidth, dheight, false);
        Wall iw13 = new Wall(6,3,screenToMaze(6), screenToMaze(4), dwidth, dheight, false);
        Wall iw14 = new Wall(6,4,screenToMaze(6), screenToMaze(5), dwidth, dheight, false);
        Wall iw15 = new Wall(6,5,screenToMaze(6), screenToMaze(6), dwidth, dheight, false);
        Wall iw16 = new Wall(7,4,screenToMaze(7), screenToMaze(5), dwidth, dheight, false);
        Wall iw17 = new Wall(8,3,screenToMaze(8), screenToMaze(4), dwidth, dheight, false);
        Wall iw18 = new Wall(8,4,screenToMaze(8), screenToMaze(5), dwidth, dheight, false);
        Wall iw19 = new Wall(9,4,screenToMaze(9), screenToMaze(5), dwidth, dheight, false);
        Wall iw20 = new Wall(10,4,screenToMaze(10), screenToMaze(4), dwidth, dheight, false);
        Wall iw21 = new Wall(10,6,screenToMaze(10), screenToMaze(6), dwidth, dheight, false);
        Wall iw22 = new Wall(12,6,screenToMaze(12), screenToMaze(6), dwidth, dheight, false);
        Wall iw23 = new Wall(14,6,screenToMaze(14), screenToMaze(6), dwidth, dheight, false);
        Wall iw24 = new Wall(15,0,screenToMaze(15), screenToMaze(0), dwidth, dheight, false);
        Wall iw25 = new Wall(15,1,screenToMaze(15), screenToMaze(1), dwidth, dheight, false);
        Wall iw26 = new Wall(15,2,screenToMaze(15), screenToMaze(2), dwidth, dheight, false);
        Wall iw27 = new Wall(15,3,screenToMaze(15), screenToMaze(3), dwidth, dheight, false);
        Wall iw28 = new Wall(16,1,screenToMaze(16), screenToMaze(1), dwidth, dheight, false);
        Wall iw29 = new Wall(16,6,screenToMaze(16), screenToMaze(6), dwidth, dheight, false);
        Wall iw30 = new Wall(17,5,screenToMaze(17), screenToMaze(5), dwidth, dheight, false);
        Wall iw31 = new Wall(18,5,screenToMaze(18), screenToMaze(5), dwidth, dheight, false);
        Wall iw32 = new Wall(18,6,screenToMaze(18), screenToMaze(6), dwidth, dheight, false);
        Wall iw33 = new Wall(18,7,screenToMaze(18), screenToMaze(7), dwidth, dheight, false);
        Wall iw34 = new Wall(19,5,screenToMaze(19), screenToMaze(5), dwidth, dheight, false);
        Wall iw35 = new Wall(20,4,screenToMaze(20), screenToMaze(4), dwidth, dheight, false);
        Wall iw36 = new Wall(21,3,screenToMaze(21), screenToMaze(3), dwidth, dheight, false);
        Wall iw37 = new Wall(21,6,screenToMaze(21), screenToMaze(6), dwidth, dheight, false);
        Wall iw38 = new Wall(21,7,screenToMaze(21), screenToMaze(7), dwidth, dheight, false);
        Wall iw39 = new Wall(22,2,screenToMaze(22), screenToMaze(2), dwidth, dheight, false);
        Wall iw40 = new Wall(22,6,screenToMaze(22), screenToMaze(6), dwidth, dheight, false);
        Wall iw41 = new Wall(24,1,screenToMaze(24), screenToMaze(1), dwidth, dheight, false);
        Wall iw42 = new Wall(24,4,screenToMaze(24), screenToMaze(4), dwidth, dheight, false);
        Wall iw43 = new Wall(25,4,screenToMaze(25), screenToMaze(4), dwidth, dheight, false);
        Wall iw44 = new Wall(26,4,screenToMaze(26), screenToMaze(4), dwidth, dheight, false);
        Wall iw45 = new Wall(28,4,screenToMaze(28), screenToMaze(4), dwidth, dheight, false);
        Wall iw46 = new Wall(29,2,screenToMaze(29), screenToMaze(2), dwidth, dheight, false);
        Wall iw47 = new Wall(29,3,screenToMaze(29), screenToMaze(3), dwidth, dheight, false);
        Wall iw48 = new Wall(29,4,screenToMaze(29), screenToMaze(4), dwidth, dheight, false);
        Wall iw49 = new Wall(31,1,screenToMaze(31), screenToMaze(1), dwidth, dheight, false);


        Wall ew1 = new Wall(5,3,screenToMaze(5), screenToMaze(3), dwidth, dheight, true);
        Wall ew2 = new Wall(5,7,screenToMaze(5), screenToMaze(7), dwidth, dheight, true);
        Wall ew3 = new Wall(6,6,screenToMaze(6), screenToMaze(6), dwidth, dheight, true);
        Wall ew4 = new Wall(7,1,screenToMaze(7), screenToMaze(1), dwidth, dheight, true);
        Wall ew5 = new Wall(8,5,screenToMaze(8), screenToMaze(5), dwidth, dheight, true);
        Wall ew6 = new Wall(8,6,screenToMaze(8), screenToMaze(6), dwidth, dheight, true);
        Wall ew7 = new Wall(9,1,screenToMaze(9), screenToMaze(1), dwidth, dheight, true);
        Wall ew8 = new Wall(11,3,screenToMaze(11), screenToMaze(3), dwidth, dheight, true);
        Wall ew9 = new Wall(21,4,screenToMaze(21), screenToMaze(4), dwidth, dheight, true);
        Wall ew10 = new Wall(22,3,screenToMaze(22), screenToMaze(3), dwidth, dheight, true);
        Wall ew11 = new Wall(22,4,screenToMaze(22), screenToMaze(4), dwidth, dheight, true);
        Wall ew12 = new Wall(23,3,screenToMaze(23), screenToMaze(3), dwidth, dheight, true);
        Wall ew13 = new Wall(24,3,screenToMaze(24), screenToMaze(3), dwidth, dheight, true);
        Wall ew14 = new Wall(27,1,screenToMaze(27), screenToMaze(1), dwidth, dheight, true);
        Wall ew15 = new Wall(28,1,screenToMaze(28), screenToMaze(1), dwidth, dheight, true);
        Wall ew16 = new Wall(28,5,screenToMaze(28), screenToMaze(5), dwidth, dheight, true);
        Wall ew17 = new Wall(30,7,screenToMaze(30), screenToMaze(7), dwidth, dheight, true);
        Wall ew18 = new Wall(31,7,screenToMaze(31), screenToMaze(7), dwidth, dheight, true);

        Wall[] iw = new Wall[] {iw1, iw2, iw3, iw4, iw5, iw6, iw7, iw8, iw9, iw10, iw11, iw12, iw13, iw14,
                iw15, iw16, iw17, iw18, iw19, iw20, iw21, iw22, iw23, iw24, iw25, iw26, iw27, iw28,
                iw29, iw30, iw31, iw32, iw33, iw34, iw35, iw36, iw37, iw38, iw39, iw40, iw41, iw42, iw43,
                iw44, iw45, iw46, iw47, iw48, iw49, ew1, ew2, ew3, ew4, ew5, ew6, ew7, ew8,
                ew9, ew10, ew11, ew12, ew13, ew14, ew15, ew16, ew17, ew18};

        for (int i = iw.length - 1; i >= 0; i--) {
            iw[i].setBodyType(BodyDef.BodyType.StaticBody);
            iw[i].setDrawScale(scale);
            if (iw[i].getEdible()) {
                iw[i].setTexture(textureDict.get("edibleWall"));
                iw[i].setType(EDIBLEWALL);
            }
            else {

                iw[i].setTexture(textureDict.get("wall"));
                iw[i].setType(WALL);
            }
            addObject(iw[i]);
            grid[(int)iw[i].getGridLocation().x][(int)iw[i].getGridLocation().y] = iw[i];
        }

        for (int i = 0; i < getWidth(); i++) {
            Wall w1 = new Wall(i,0,screenToMaze(i), screenToMaze(0), dwidth, dheight, false);
            w1.setBodyType(BodyDef.BodyType.StaticBody);
            w1.setDrawScale(scale);
            w1.setTexture(textureDict.get("wall"));
            w1.setType(WALL);
            addObject(w1);
            grid[(int)w1.getGridLocation().x][(int)w1.getGridLocation().y] = w1;
            Wall w2 = new Wall(i,getHeight() - 1,screenToMaze(i), screenToMaze(getHeight() - 1), dwidth, dheight, false);
            w2.setBodyType(BodyDef.BodyType.StaticBody);
            w2.setDrawScale(scale);
            w2.setTexture(textureDict.get("wall"));
            w2.setType(WALL);
            addObject(w2);
            grid[(int)w2.getGridLocation().x][(int)w2.getGridLocation().y] = w2;
        }

        for (int i = 0; i < getHeight(); i++) {
            Wall w1 = new Wall(0,i,screenToMaze(0), screenToMaze(i), dwidth, dheight, false);
            w1.setBodyType(BodyDef.BodyType.StaticBody);
            w1.setDrawScale(scale);
            w1.setTexture(textureDict.get("wall"));
            w1.setType(WALL);
            addObject(w1);
            grid[(int)w1.getGridLocation().x][(int)w1.getGridLocation().y] = w1;
            Wall w2 = new Wall(getWidth() - 1,i,screenToMaze(getWidth() - 1), screenToMaze(i), dwidth, dheight, false);
            w2.setBodyType(BodyDef.BodyType.StaticBody);
            w2.setDrawScale(scale);
            w2.setTexture(textureDict.get("wall"));
            w2.setType(WALL);
            addObject(w2);
            grid[(int)w2.getGridLocation().x][(int)w2.getGridLocation().y] = w2;
        }

        // Add level goal
        dwidth = textureDict.get("goalOpenTile").getRegionWidth() / scale.x;
        dheight = textureDict.get("goalOpenTile").getRegionHeight() / scale.y;
        goalDoor = new Wall(8,4,screenToMaze(8), screenToMaze(4), dwidth, dheight, false);
        goalDoor.setBodyType(BodyDef.BodyType.StaticBody);
        goalDoor.setSensor(true);
        goalDoor.setDrawScale(scale);
        goalDoor.setTexture(textureDict.get("goalClosedTile"));
        goalDoor.setName("exit");
        goalDoor.setType(GOAL);
        addObject(goalDoor);

        // Create enemy
        dwidth = textureDict.get("enemyFront").getRegionWidth() / (scale.x * 2);

        // Adding the rest of the enemies; they're static right now
        Enemy en2 = new Enemy(screenToMaze(2), screenToMaze(1), dwidth,1);
        Enemy en3 = new Enemy(screenToMaze(9), screenToMaze(5), dwidth,2);
        Enemy en4 = new Enemy(screenToMaze(12), screenToMaze(3), dwidth,3);
        Enemy en5 = new Enemy(screenToMaze(13), screenToMaze(1), dwidth,4);
        Enemy en6 = new Enemy(screenToMaze(14), screenToMaze(2), dwidth,5);
        Enemy en7 = new Enemy(screenToMaze(16), screenToMaze(6), dwidth,6);
        Enemy en8 = new Enemy(screenToMaze(19), screenToMaze(4), dwidth,7);
        Enemy en9 = new Enemy(screenToMaze(20), screenToMaze(3), dwidth,8);
        Enemy en10 = new Enemy(screenToMaze(20), screenToMaze(5), dwidth,9);
        Enemy en11 = new Enemy(screenToMaze(22), screenToMaze(2), dwidth,10);
        Enemy en12 = new Enemy(screenToMaze(22), screenToMaze(6), dwidth,11);
        Enemy en13 = new Enemy(screenToMaze(26), screenToMaze(3), dwidth,12);
        Enemy en14 = new Enemy(screenToMaze(27), screenToMaze(4), dwidth,13);
        Enemy en15 = new Enemy(screenToMaze(29), screenToMaze(6), dwidth,14);
        Enemy en16 = new Enemy(screenToMaze(30), screenToMaze(1), dwidth,15);
        Enemy en17 = new Enemy(screenToMaze(30), screenToMaze(5), dwidth,16);
        Enemy[] en = new Enemy[]{en2,en3,en4,en5,en6,en7,en8,en9,en10,en11,en12,en13,en14,en15,
                en16,en17};

        for (int i = 0; i < 16; i++) {
            en[i].setType(ENEMY);
            en[i].setDrawScale(scale);
            en[i].setTextureSet(textureDict.get("enemyLeft"), textureDict.get("enemyRight"),
                    textureDict.get("enemyBack"), textureDict.get("enemyFront"));
            addObject(en[i]);
        }

        dwidth = textureDict.get("fireFly").getRegionWidth() / (scale.x * 2);
        for (int i = 0; i < 5; i++){
            FireFly ff = new FireFly(MathUtils.random(bounds.width), MathUtils.random(bounds.height), dwidth);
            ff.setType(FIREFLY);
            ff.setTexture(textureDict.get("fireFly"));
            ff.setDrawScale(scale);
            addObject(ff);
        }
    }

    public void draw(Canvas canvas) {
        canvas.begin();

        Iterator<PooledList<GameObject>.Entry> iterator = objects.entryIterator();
        while (iterator.hasNext()) {
            PooledList<GameObject>.Entry entry = iterator.next();
            drawObjects.add(entry.getValue());
        }

        Collections.sort(drawObjects, new Comparator<GameObject>() {
            @Override
            public int compare(GameObject g1, GameObject g2) {
                if (g1.getType() == SWITCH)
                    return -1;
                if (g2.getType() == SWITCH)
                    return 1;

                if (g1.getType() == COTTON)
                    return -1;
                if (g2.getType() == COTTON)
                    return 1;

                if (g1.getType() == FIREFLY)
                    return 1;
                if (g2.getType() == FIREFLY)
                    return -1;

                if ((int)(g2.getY() - g1.getY()) == 0)
                    return (int)(g2.getX() - g1.getX());

                return (int)(g2.getY() - g1.getY());
            }
        });
        canvas.draw(background, 0, 0);
        canvas.draw(background, 1270, 0);
        for(GameObject g : drawObjects)
            g.draw(canvas);

        canvas.end();

        drawObjects.clear();
    }

    public int getAvatarGridX() {
        return Math.round((avatar.getX() - 1) / 2);
    }

    public int getAvatarGridY() {
        return Math.round((avatar.getY() - 1) / 2);
    }

    public boolean isInFrontOfAvatar(GameObject bd) {
        int direction = avatar.getDirection();
        locationCache.set(getAvatarGridX(), getAvatarGridY());

        if (bd.getType() != WALL && bd.getType() != COTTON && bd.getType() != EDIBLEWALL){
            if (isAlignedHorizontally(avatar, bd, 0.7)){
                if (direction == LEFT)
                    return bd.getX() <= avatar.getX();
                else if (direction == RIGHT)
                    return bd.getX() >= avatar.getX();
                else return false;
            }
            else if (isAlignedVertically(avatar, bd, 0.7)){
                if (direction == UP) {
                    return bd.getY() >= avatar.getY();
                }
                else if (direction == DOWN) {
                    return bd.getY() <= avatar.getY();
                }
                else return false;
            }
        }
        else {
            if (bd.getType() == WALL){
                if (direction == LEFT) {
                    if (((Wall) bd).getGridLocation().x + 1 == locationCache.x &&
                            ((Wall) bd).getGridLocation().y == locationCache.y)
                        return true;
                }
                else if (direction == RIGHT){
                    if (((Wall) bd).getGridLocation().x - 1 == locationCache.x &&
                            ((Wall) bd).getGridLocation().y == locationCache.y)
                        return true;
                }
                else if (direction == UP){
                    if (((Wall) bd).getGridLocation().x == locationCache.x &&
                            ((Wall) bd).getGridLocation().y + 1 == locationCache.y)
                        return true;
                }
                else if (direction == DOWN){
                    if (((Wall) bd).getGridLocation().x == locationCache.x &&
                            ((Wall) bd).getGridLocation().y - 1 == locationCache.y)
                        return true;
                }
                return false;
            }
        }
        return false;
    }

    public GameObject objectInFrontOfAvatar() {
        int direction = avatar.getDirection();
        locationCache.set(getAvatarGridX(), getAvatarGridY());

        if (direction == UP) {
            if ((int)locationCache.y == getHeight())
                return null;
            else
                return grid[(int)locationCache.x][(int)locationCache.y + 1];
        }
        else if (direction == DOWN) {
            if ((int)locationCache.y == 0)
                return null;
            else
                return grid[(int)locationCache.x][(int)locationCache.y - 1];
        }
        else if (direction == LEFT) {
            if ((int)locationCache.x == 0)
                return null;
            else
                return grid[(int)locationCache.x - 1][(int)locationCache.y];
        }
        else if (direction == RIGHT) {
            if ((int)locationCache.x == getWidth())
                return null;
            else
                return grid[(int)locationCache.x + 1][(int)locationCache.y];
        }
        return null;
    }

    public boolean objectInFrontOfEnemy(Enemy e) {
        int direction = e.getDirection();

        objectCache = null;
        if (direction == UP) {
            locationCache.set(Math.round((e.getX() - 1) / 2), (float)Math.floor((e.getY() - 1) / 2));
            objectCache = grid[(int)locationCache.x][(int)locationCache.y + 1];
        }
        else if (direction == DOWN) {
            locationCache.set(Math.round((e.getX() - 1) / 2), (float)Math.ceil((e.getY() - 1) / 2));
            objectCache = grid[(int)locationCache.x][(int)locationCache.y - 1];
        }
        else if (direction == LEFT) {
            locationCache.set((float)Math.ceil((e.getX() - 1) / 2), Math.round((e.getY() - 1) / 2));
            objectCache = grid[(int)locationCache.x - 1][(int)locationCache.y];
        }
        else {
            locationCache.set((float)Math.floor((e.getX() - 1) / 2), Math.round((e.getY() - 1) / 2));
            objectCache = grid[(int)locationCache.x + 1][(int)locationCache.y];
        }

        if (objectCache == null)
            return false;

        return objectCache.getType() == WALL || objectCache.getType() == EDIBLEWALL ||
                objectCache.getType() == RIVER || objectCache.getType() == BOULDER;
    }

    public boolean isAlignedHorizontally(GameObject bd1, GameObject bd2, double offset){
        return (Math.abs(bd1.getY() - bd2.getY()) <= offset);
    }

    public boolean isAlignedVertically(GameObject bd1, GameObject bd2, double offset){
        return (Math.abs(bd1.getX() - bd2.getX()) <= offset);
    }

    public boolean isOnGrid(double x, double y){
        float gridx = screenToMaze(Math.round((avatar.getX() - 1) / 2));
        float gridy = screenToMaze(Math.round((avatar.getY() - 1) / 2));
        return (Math.abs(avatar.getX() - gridx) <= x) && (Math.abs(avatar.getY() - gridy) <= y);
    }

    /** drawing on screen */
    public int screenToMaze (float f) {
        return (int)(1 + 2 * f);
    }

    public void dispose() {
        for(GameObject g : objects)
            g.deactivatePhysics(world);
        objects.clear();
        walls.clear();
        cottonFlowers.clear();
        rivers.clear();
        boulders.clear();
        enemies.clear();
        fireFlies.clear();

        objects = null;
        walls = null;
        cottonFlowers = null;
        rivers = null;
        boulders = null;
        enemies = null;
        fireFlies = null;
        clone = null;
        scale = null;
        locationCache = null;
        objectCache = null;
    }
}
