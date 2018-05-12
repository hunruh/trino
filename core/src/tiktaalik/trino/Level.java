package tiktaalik.trino;

import static tiktaalik.trino.GameController.*;
import static tiktaalik.trino.duggi.Dinosaur.*;

import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import org.json.simple.JSONObject;
import tiktaalik.trino.duggi.Carnivore;
import tiktaalik.trino.level_editor.LevelParser;
import tiktaalik.trino.level_editor.LevelParser.*;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.World;
import tiktaalik.trino.duggi.Clone;
import tiktaalik.trino.duggi.Dinosaur;
import tiktaalik.trino.duggi.Doll;
import tiktaalik.trino.enemy.Enemy;
import tiktaalik.trino.environment.*;
import tiktaalik.trino.level_editor.SaveFileParser;
import tiktaalik.trino.lights.LightSource;
import tiktaalik.trino.lights.PointSource;
import tiktaalik.util.PooledList;

import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;

public class Level {
    private static final float DEFAULT_WIDTH  = 32.0f; // Width of the game world in Box2d units
    private static final float DEFAULT_HEIGHT = 9.0f; // Height of the game world in Box2d units

    protected PooledList<GameObject> objects  = new PooledList<GameObject>(); // All the objects in the world
    protected PooledList<GameObject> groundObjects  = new PooledList<GameObject>(); // List of ground-level draw objects
    protected PooledList<GameObject> blockObjects = new PooledList<GameObject>(); // Sortable list of objects for draw

    private PooledList<Wall> walls = new PooledList<Wall>();
    private PooledList<CottonFlower> cottonFlowers = new PooledList<CottonFlower>();
    private PooledList<River> rivers = new PooledList<River>();
    private PooledList<Boulder> boulders = new PooledList<Boulder>();
    private PooledList<Enemy> enemies = new PooledList<Enemy>();
    private PooledList<FireFly> fireFlies = new PooledList<FireFly>();
    private PooledList<Switch> switches = new PooledList<Switch>();
    private PooledList<Wall> doors = new PooledList<Wall>();

    private GameObject[][] grid;
    private PooledList<Vector2> cottonFlowerList = new PooledList<Vector2>();//for shadow duggi
    private boolean[][] enemyLocation;
    private Rectangle bounds;
    private Vector2 scale;

    private World world;

    private Dinosaur avatar;
    private Clone clone;
    private Enemy shadowDuggi;

    private GameObject objectCache;
    private Vector2 locationCache;

    private TextureRegion background;
    private TextureRegion cloneTexture;

    private int pixelFactor = 80;
    private int levelWidth;
    private int levelHeight;

    private boolean isNight;

    private int currentLevel;

    public Level(World world, int lvl) {
        this.bounds = new Rectangle(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        this.world = world;
        scale = new Vector2();
        locationCache = new Vector2();
        currentLevel = lvl;
        grid = new GameObject[(int) bounds.width][(int) bounds.height];
        enemyLocation = new boolean[(int) bounds.width][(int) bounds.height];
    }

    public int getCurrentLevel(){
        return currentLevel;
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
            case SWITCH:
                switches.add((Switch) g);
                break;
            case GOAL:
                doors.add((Wall) g);
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
            case SWITCH:
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

    public boolean setGridObject(int x, int y, GameObject g, boolean overwrite) {
        if (grid[x][y] != null && !overwrite)
            return false;

        grid[x][y] = g;
        return true;
    }

    public GameObject[][] getGrid() { return grid; }

    public Dinosaur getAvatar() {
        return avatar;
    }

    public int getLevelWidth(){return levelWidth;}

    public int getLevelHeight(){return levelHeight;}

    public void setAvatar(Dinosaur avatar) {
        this.avatar = avatar;
        objects.set(1, this.avatar);
    }

    public Clone getClone() {
        return clone;
    }

    public void placeClone() {
        clone = new Clone(avatar.getX(), avatar.getY(), 40 / (scale.x * 2));
        clone.setGridLocation(getAvatarGridX(), getAvatarGridY());
        clone.setDrawScale(scale);
        clone.setType(CLONE);
        clone.setTexture(cloneTexture);
        clone.setBodyType(BodyDef.BodyType.StaticBody);
        addObject(clone);
    }

    public void removeClone() {
        removeObject(clone);
    }

    public void pushBoulder(Dinosaur d, Boulder b) {
        if (!b.getInMotion()) {
            float targetX = b.getX();
            float targetY = b.getY();
            int gridX = (int)b.getGridLocation().x;
            int gridY = (int)b.getGridLocation().y;
            int targetGridX = gridX;
            int targetGridY = gridY;

            if (d.getDirection() == Dinosaur.LEFT) {
                if (getGridObject(gridX - 1, gridY) != null) {
                    if (getGridObject(gridX - 1, gridY).getType() != GameController.COTTON &&
                            getGridObject(gridX - 1, gridY).getType() != GameController.SWITCH)
                        return;
                }

                if (isEnemyOnSquare(gridX - 1, gridY))
                    return;

                targetX = 1 + 2 * (gridX - 1);
                targetGridX = gridX - 1;
            }
            else if (d.getDirection() == Dinosaur.RIGHT) {
                if (getGridObject(gridX + 1, gridY) != null) {
                    if (getGridObject(gridX + 1, gridY).getType() != GameController.COTTON &&
                            getGridObject(gridX + 1, gridY).getType() != GameController.SWITCH)
                        return;
                }

                if (isEnemyOnSquare(gridX + 1, gridY))
                    return;

                targetX = 1 + 2 * (gridX + 1);
                targetGridX = gridX + 1;
            }
            else if (d.getDirection() == Dinosaur.UP) {
                if (getGridObject(gridX, gridY + 1) != null) {
                    if (getGridObject(gridX, gridY + 1).getType() != GameController.COTTON &&
                            getGridObject(gridX, gridY + 1).getType() != GameController.SWITCH)
                        return;
                }

                if (isEnemyOnSquare(gridX, gridY + 1))
                    return;

                targetY = 1 + 2 * (gridY + 1);
                targetGridY = gridY + 1;
            }
            else if (d.getDirection() == Dinosaur.DOWN) {
                if (getGridObject(gridX, gridY - 1) != null) {
                    if (getGridObject(gridX, gridY - 1).getType() != GameController.COTTON &&
                            getGridObject(gridX, gridY-1).getType() != GameController.SWITCH)
                        return;
                }

                if (isEnemyOnSquare(gridX, gridY - 1))
                    return;

                targetY = 1 + 2 * (gridY - 1);
                targetGridY = gridY - 1;
            }

            // Move the boulder on the grid. Do not null the grid tile if it is a cotton flower
            if (getGridObject(gridX, gridY).getType() == GameController.BOULDER)
                setGridObject(gridX, gridY, null, true);
            setGridObject(targetGridX, targetGridY, b, false);

            b.setTargetDestination(targetX, targetY);
            b.setInMotion(true, ((Carnivore) d));
            ((Carnivore) d).setPushing(true);
        }
    }

    //public Wall getGoalDoor() {
//        return goalDoor;
//    }

    public Enemy getEnemy(int idx) {
        return enemies.get(idx);
    }

    public PooledList<Enemy> getEnemies() {
        return enemies;
    }

    public Boulder getBoulder(int idx) { return boulders.get(idx); }

    public PooledList<Boulder> getBoulders() { return boulders; }

    public FireFly getFirefly(int idx) {
        return fireFlies.get(idx);
    }

    public PooledList<FireFly> getFireFlies() {
        return fireFlies;
    }

    public Switch getSwitch(int idx) { return switches.get(idx); }

    public PooledList<Switch> getSwitches() { return switches; }

    public Wall getDoor(int idx) { return doors.get(idx); }

    public PooledList<Wall> getDoors() { return doors; }

    public PooledList<River> getRivers() {return rivers;}

    public CottonFlower getCottonFlower(int idx) { return cottonFlowers.get(idx); }

    public PooledList<CottonFlower> getCottonFlowers() { return cottonFlowers; }

    public PooledList<GameObject> getObjects() {
        return objects;
    }

    public Enemy getShadowDuggi(){return shadowDuggi;}

    public boolean getIsNight(){return isNight;}

    public PooledList<Vector2>getCottonFlowerList(){return cottonFlowerList;}

    public void populate(Hashtable<String, TextureRegion> textureDict, Hashtable<String, Texture> filmStripDict,
                         LightSource avatarLight, int canvasWidth, int canvasHeight){
        scale = new Vector2(canvasWidth/bounds.getWidth(), canvasHeight/bounds.getHeight());

        SaveFileParser savefileparser = new SaveFileParser();
        try {
            savefileparser.parse("jsons/save.json");
        } catch(Exception e){
            System.out.println("oops");
        }
        savefileparser.printObj();
        savefileparser.changeLevelCompletion(0, true);
        savefileparser.printObj();
        try {
            savefileparser.writeToFile("jsons/save.json");
        } catch(Exception e){
            System.out.println("oops");
        }



        LevelParser parser = new LevelParser();
        try {
//            parser.parse("/trino/example.json");
            parser.parse("trino/example.json");
        } catch(Exception e) {
            System.out.println("oops dude");
        }

        float dwidth;
        float dheight;

        //System.out.println("rdjgheks"+parser.getLevelDimension(0).y);
        levelHeight = pixelFactor * (int)((double)((float)parser.getLevelDimension(currentLevel).y));
        levelWidth = pixelFactor * (int)((double)((float)parser.getLevelDimension(currentLevel).x));

        bounds.x = levelWidth/pixelFactor;
        bounds.y = levelHeight/pixelFactor;

        // Set permanent textures
        background = textureDict.get("background");
        cloneTexture = textureDict.get("clone");

        PooledList<Vector2> tmp = new PooledList<Vector2>();
        // Create player character
        // It is important that this is always created first, as transformations must swap the first element
        // in the objects list
        dwidth = textureDict.get("clone").getRegionWidth() / (scale.x * 2);


        tmp = parser.getAssetList(currentLevel, "Player");
        for(int i = 0; i < tmp.size(); i++) {
            float x = (tmp.get(i)).x;
            float y = (tmp.get(i)).y - 1;
            avatar = new Doll(screenToMaze(x), screenToMaze(y), dwidth);

            avatar.setType(DUGGI);
            avatar.setTextureSet(filmStripDict.get("dollLeft"), 8,
                    filmStripDict.get("dollRight"), 8,
                    filmStripDict.get("dollBack"), 8,
                    filmStripDict.get("dollFront"), 8);
            avatar.setEatingTextureSet(filmStripDict.get("dollEatingLeft"), 7,
                    filmStripDict.get("dollEatingRight"), 7,
                    filmStripDict.get("dollEatingBack"), 6,
                    filmStripDict.get("dollEatingFront"), 7);
            avatar.setActionTextureSet(filmStripDict.get("dollCloningFront"), 12,
                    filmStripDict.get("dollCloningFront"), 12,
                    filmStripDict.get("dollCloningFront"), 12,
                    filmStripDict.get("dollCloningFront"), 12);
            avatar.setDrawScale(scale);

            //Change filter data to that of the doll form
            //Change the filter data
            Filter filter = avatar.getFilterData();
            filter.categoryBits = 0x0004;
            avatar.setFilterData(filter);

            addObject(avatar);
            avatarLight.attachToBody(avatar.getBody(), avatarLight.getX(), avatarLight.getY(), avatarLight.getDirection());
        }

        /** Adding cotton flowers */
        TextureRegion cottonTexture = textureDict.get("cotton");
        dwidth = cottonTexture.getRegionWidth() / scale.x;
        dheight = cottonTexture.getRegionHeight() / scale.y;
        tmp = parser.getAssetList(currentLevel, "Cottons");
        for(int i = 0; i < tmp.size(); i++){
            float x = (tmp.get(i)).x;
            float y = (tmp.get(i)).y-1;
            CottonFlower cf = new CottonFlower((int)x, (int)y, screenToMaze(x), screenToMaze(y), dwidth, dheight);
            cf.setBodyType(BodyDef.BodyType.StaticBody);
            cf.setDrawScale(scale);
            cf.setTexture(cottonTexture);
            cf.setType(COTTON);
            addObject(cf);
            grid[(int)cf.getGridLocation().x][(int)cf.getGridLocation().y] = cf;
            cottonFlowerList.add(new Vector2(cf.getGridLocation().x,cf.getGridLocation().y));
        }

        // Adding river
        dwidth = textureDict.get("river").getRegionWidth() / scale.x;
        dheight = textureDict.get("river").getRegionHeight() / scale.y;
        tmp = parser.getAssetList(currentLevel, "Rivers");
        for(int i = 0; i < tmp.size(); i++){
            float x = (tmp.get(i)).x;
            float y = (tmp.get(i)).y-1;
            River riv = new River((int)x, (int)y, screenToMaze(x), screenToMaze(y), dwidth, dheight, false);
            riv.setBodyType(BodyDef.BodyType.StaticBody);
            riv.setDrawScale(scale);
            riv.setTexture(textureDict.get("river"));
            riv.setType(RIVER);
            addObject(riv);
            grid[(int)riv.getGridLocation().x][(int)riv.getGridLocation().y] = riv;
        }


        dwidth = textureDict.get("boulder").getRegionWidth() / scale.x;
        dheight = textureDict.get("boulder").getRegionHeight() / scale.y;
        tmp = parser.getAssetList(currentLevel, "Boulders");
        for(int i = 0; i < tmp.size(); i++){
            float x = (tmp.get(i)).x;
            float y = (tmp.get(i)).y-1;
            Boulder b = new Boulder((int)x, (int)y, screenToMaze(x), screenToMaze(y), dwidth, dheight, false);
            b.setBodyType(BodyDef.BodyType.StaticBody);
            b.setDrawScale(scale);
            b.setTexture(textureDict.get("boulder"));
            b.setType(BOULDER);
            addObject(b);
            grid[(int)b.getGridLocation().x][(int)b.getGridLocation().y] = b;
        }

        // Switch
        dwidth = textureDict.get("switch").getRegionWidth() / scale.x;
        dheight = textureDict.get("switch").getRegionHeight() / scale.y;
        // Switch texture
        tmp = parser.getAssetList(currentLevel, "Switch");
        for(int i = 0; i < tmp.size(); i++){
            float x = (tmp.get(i)).x;
            float y = (tmp.get(i)).y-1;
            Switch switches = new Switch((int)x, (int)y, screenToMaze(x), screenToMaze(y), dwidth, dheight);
            switches.setBodyType(BodyDef.BodyType.StaticBody);
            switches.setDrawScale(scale);
            if (i == 0) {
                switches.setTexture(textureDict.get("switch"));
            }
            else if (i == 1) {
                switches.setTexture(textureDict.get("switchone"));
            }
            else if (i == 2) {
                switches.setTexture(textureDict.get("switchtwo"));
            }
            else if (i == 3) {
                switches.setTexture(textureDict.get("switchthree"));
            }
            switches.setType(SWITCH);
            addObject(switches);
            grid[(int)switches.getGridLocation().x][(int)switches.getGridLocation().y] = switches;
        }


        dwidth = textureDict.get("wall").getRegionWidth() / scale.x;
        dheight = textureDict.get("wall").getRegionHeight() / scale.y;
        tmp = parser.getAssetList(currentLevel, "Walls");
        for(int i = 0; i < tmp.size(); i++){
            float x = (tmp.get(i)).x;
            float y = (tmp.get(i)).y - 1;
            Wall iw = new Wall((int)x, (int)y, screenToMaze(x), screenToMaze(y), dwidth, dheight, false);
            iw.setBodyType(BodyDef.BodyType.StaticBody);
            iw.setDrawScale(scale);
            iw.setTexture(textureDict.get("wall"));
            iw.setType(WALL);
            addObject(iw);
            grid[(int)iw.getGridLocation().x][(int)iw.getGridLocation().y] = iw;
        }

        tmp = parser.getAssetList(currentLevel, "EdibleWalls");
        for(int i = 0; i < tmp.size(); i++) {
            float x = (tmp.get(i)).x;
            float y = (tmp.get(i)).y-1;
            Wall ew = new Wall((int) x, (int) y, screenToMaze(x), screenToMaze(y), dwidth, dheight, true);
            ew.setBodyType(BodyDef.BodyType.StaticBody);
            ew.setDrawScale(scale);
            ew.setTexture(textureDict.get("edibleWall"));
            ew.setType(EDIBLEWALL);
            ew.setEatAnimation(filmStripDict.get("edibleWallEating"), 8);
            addObject(ew);
            grid[(int) ew.getGridLocation().x][(int) ew.getGridLocation().y] = ew;
        }

        // Add level goal
        dwidth = textureDict.get("goalOpenTile").getRegionWidth() / scale.x;
        dheight = textureDict.get("goalOpenTile").getRegionHeight() / scale.y;
        tmp = parser.getAssetList(currentLevel, "Goal");
        for(int i = 0; i < tmp.size(); i++) {
            float x = (tmp.get(i)).x;
            float y = (tmp.get(i)).y - 1;
            Wall goalDoor = new Wall((int) x, (int) y, screenToMaze(x), screenToMaze(y), dwidth, dheight, false);
            goalDoor.setBodyType(BodyDef.BodyType.StaticBody);
            //goalDoor.setSensor(true);
            goalDoor.setDrawScale(scale);
            goalDoor.setTexture(textureDict.get("goalClosedTile"));
            goalDoor.setName("exit");
            goalDoor.setType(GOAL);
            if (i == 0) {
                goalDoor.setGoal(true);
            }
            else {
                goalDoor.setGoal(false);
            }
            addObject(goalDoor);
            grid[(int) goalDoor.getGridLocation().x][(int) goalDoor.getGridLocation().y] = goalDoor;
        }

        // Create enemy
        dwidth = filmStripDict.get("enemyFront").getWidth() / (10 * (scale.x * 2));
        System.out.println("currentlevel " + currentLevel);
        tmp = parser.getAssetList(currentLevel, "Enemies");
        PooledList<String[]> dir = parser.getEnemiesInformation(currentLevel);
        for(int i = 0; i < tmp.size(); i++) {
            float x = (tmp.get(i)).x;
            float y = (tmp.get(i)).y-1;
            Enemy en = new Enemy(screenToMaze(x), screenToMaze(y) + 0.4f, dwidth, i+1);
            String sd = dir.get(i)[0];
            String et = dir.get(i)[1];
            System.out.println(sd);
            int d = 0;
            int type = -1;
            if (sd.equals("Up")) d = 2;
            else if (sd.equals("Down")) d = 3;
            else if (sd.equals("Left")) d = 0;
            else if (sd.equals("Right")) d = 1;
            else d = -1;
            if (et.equals("Carni")) type = Enemy.CARNIVORE_ENEMY;
            else if (et.equals("Herbi")) type = Enemy.HERBIVORE_ENEMY;
            else if (et.equals("Unkillable")) type = Enemy.UNKILLABLE_ENEMY;
            System.out.println(type);
            System.out.println(et);
            System.out.println("d is " + d);
            en.setType(ENEMY);
            en.setDrawScale(scale);
            if (type == Enemy.UNKILLABLE_ENEMY){
                en.setTextureSet(filmStripDict.get("unkillableEnemyLeft"), 10,
                        filmStripDict.get("unkillableEnemyRight"), 10,
                        filmStripDict.get("unkillableEnemyBack"), 8,
                        filmStripDict.get("unkillableEnemyFront"), 10);
                en.setActionLoadingTextureSet(filmStripDict.get("enemyChargeLeft"), 15,
                        filmStripDict.get("enemyChargeRight"), 15,
                        filmStripDict.get("enemyChargeLeft"), 15,
                        filmStripDict.get("enemyChargeLeft"), 15);
                en.setActionTextureSet(filmStripDict.get("enemyAttackLeft"), 9,
                        filmStripDict.get("enemyAttackRight"), 9,
                        filmStripDict.get("enemyAttackLeft"), 9,
                        filmStripDict.get("enemyAttackLeft"), 9);
                en.setStunnedTextureSet(filmStripDict.get("enemyStunnedLeft"), 3,
                        filmStripDict.get("enemyStunnedRight"), 3,
                        filmStripDict.get("enemyStunnedBack"), 3,
                        filmStripDict.get("enemyStunnedFront"), 3);
                en.setEatAnimation(filmStripDict.get("enemyLeftEating"), 6);
            }
            else if (type == Enemy.CARNIVORE_ENEMY){
                en.setTextureSet(filmStripDict.get("enemyLeft"), 10,
                        filmStripDict.get("enemyRight"), 10,
                        filmStripDict.get("enemyBack"), 8,
                        filmStripDict.get("enemyFront"), 10);
                en.setActionLoadingTextureSet(filmStripDict.get("enemyChargeLeft"), 15,
                        filmStripDict.get("enemyChargeRight"), 15,
                        filmStripDict.get("enemyChargeBack"), 8,
                        filmStripDict.get("enemyChargeFront"), 9);
                en.setActionTextureSet(filmStripDict.get("enemyAttackLeft"), 9,
                        filmStripDict.get("enemyAttackRight"), 9,
                        filmStripDict.get("enemyAttackBack"), 6,
                        filmStripDict.get("enemyAttackFront"), 10);
                en.setStunnedTextureSet(filmStripDict.get("enemyStunnedLeft"), 3,
                        filmStripDict.get("enemyStunnedRight"), 3,
                        filmStripDict.get("enemyStunnedBack"), 3,
                        filmStripDict.get("enemyStunnedFront"), 3);
                en.setEatingTextureSet(filmStripDict.get("carnivoreEatingLeft"), 8,
                        filmStripDict.get("carnivoreEatingRight"), 8,
                        filmStripDict.get("carnivoreEatingBack"), 8,
                        filmStripDict.get("carnivoreEatingFront"), 12);
            }
            else {
                en.setTextureSet(filmStripDict.get("herbivoreSwimmingLeft"), 8,
                        filmStripDict.get("herbivoreSwimmingRight"), 8,
                        filmStripDict.get("herbivoreSwimmingBack"), 4,
                        filmStripDict.get("herbivoreSwimmingFront"), 4);
            }
            en.setDirection(d);
            en.setEnemyType(type);
            en.setGridLocation(x,y);

            addObject(en);
            enemyLocation[(int)x][(int)y] = true;
        }
        
        dwidth = textureDict.get("fireFly").getRegionWidth() / (scale.x * 2);
        for (int i = 0; i < 10; i++){
            FireFly ff = new FireFly(MathUtils.random(bounds.width),
                    MathUtils.random(2*bounds.height), dwidth);
            ff.setType(FIREFLY);
            int random = MathUtils.random(4);
            if (random == 0){
                random = MathUtils.random(3);
                if (random == 0){
                    ff.setTexture(textureDict.get("fireFlyPurple"));
                } else if (random == 1){
                    ff.setTexture(textureDict.get("fireFlyBlue"));
                } else if (random == 2){
                    ff.setTexture(textureDict.get("fireFlyPink"));
                }
            } else {
                ff.setTexture(textureDict.get("fireFly"));
            }
            ff.setDrawScale(scale);
            addObject(ff);
        }
        isNight = parser.isNightLevel(currentLevel);
    }

    public void draw(Canvas canvas) {
        canvas.begin();

        Iterator<PooledList<GameObject>.Entry> iterator = objects.entryIterator();
        while (iterator.hasNext()) {
            PooledList<GameObject>.Entry entry = iterator.next();
            GameObject g = entry.getValue();
            if (g.getType() == COTTON || g.getType() == SWITCH ||
                    (g.getType() == GOAL && ((Wall) g).getLowered()))
                groundObjects.add(entry.getValue());
            else
                blockObjects.add(entry.getValue());
        }

        Collections.sort(blockObjects, new Comparator<GameObject>() {
            @Override
            public int compare(GameObject g1, GameObject g2) {
                if (g1.getType() == RIVER)
                    return -1;
                if (g2.getType() == RIVER)
                    return 1;

                if (((g1.getType() == WALL || g1.getType() == GOAL || g1.getType() == EDIBLEWALL) &&
                        (g2.getType() == DUGGI || g2.getType() == CLONE || g2.getType() == ENEMY)) &&
                        Math.abs(screenToMaze(g1.getY()) - screenToMaze(g2.getY())) <= 2)
                    return -1;
                if (((g2.getType() == WALL || g2.getType() == GOAL || g2.getType() == EDIBLEWALL) &&
                        (g1.getType() == DUGGI || g1.getType() == CLONE || g1.getType() == ENEMY)) &&
                        Math.abs(screenToMaze(g1.getY()) - screenToMaze(g2.getY())) <= 2)
                    return 1;

                if (g1.getType() == FIREFLY)
                    return 1;
                if (g2.getType() == FIREFLY)
                    return -1;

                return (int)(g2.getY()*g2.getDrawScale().x - g1.getY()*g1.getDrawScale().x);
            }
        });
        canvas.draw(background, 0, 0);
        canvas.draw(background, 1270, 0);
        canvas.end();

        canvas.begin();
        for(GameObject g : groundObjects)
            g.draw(canvas);
        canvas.end();

        canvas.beginShadows();
        avatar.drawShadow(canvas);
        for(Enemy e : enemies) {
            e.drawShadow(canvas);
        }
        if (clone != null)
            clone.drawShadow(canvas);
        canvas.endShadows();

        canvas.begin();
        for(GameObject g : blockObjects)
            g.draw(canvas);
        canvas.end();

        canvas.beginProgressCircle();
        avatar.drawProgressCircle(canvas, avatar.getActionLoadValue());
        if (clone!= null){
            clone.drawProgressCircle(canvas);
        }
        for(Enemy e : enemies) {
            e.drawProgressCircle(canvas);
        }
        canvas.endProgressCircle();

        groundObjects.clear();
        blockObjects.clear();
    }

    public int getAvatarGridX() {
        return Math.round((avatar.getX() - 1) / 2);
    }

    public int getAvatarGridY() {
        return Math.round((avatar.getY() - 1) / 2);
    }

    public boolean[][] getEnemyLocation() {return enemyLocation;}

    public void setEnemyLocation(int x, int y, boolean value) {enemyLocation[x][y] = value;}

    public boolean isEnemyOnSquare(int gridX, int gridY) {
        for (Enemy e : enemies) {
            float dx = ((e.getX() - 1) / 2) - gridX;
            float dy = ((e.getY() - 1) / 2) - gridY;
            if (Math.abs(dx) < 0.5 && Math.abs(dy) < 0.5)
                return true;
        }

        return false;
    }

    public float getStraightDist(int direction, GameObject bd1, GameObject bd2) {
        if (bd1 == null || bd2 == null)
            return -1;

        if (direction == UP || direction == DOWN)
            return Math.abs(bd1.getPosition().y - bd2.getPosition().y);
        else
            return Math.abs(bd1.getPosition().x - bd2.getPosition().x);
    }

    public boolean isInFrontOfAvatar(GameObject bd) {
        int direction = avatar.getDirection();
        locationCache.set(getAvatarGridX(), getAvatarGridY());

        if (bd.getType() != WALL && bd.getType() != COTTON && bd.getType() != EDIBLEWALL){
            if ((direction == LEFT && bd.getX() <= avatar.getX()) ||
                    (direction == RIGHT && bd.getX() >= avatar.getX()))
                return bd.getPosition().dst2(avatar.getPosition()) < 4.5;
            if ((direction == UP && bd.getY() >= avatar.getY()) ||
                    (direction == DOWN && bd.getY() <= avatar.getY())) {
                return bd.getPosition().dst2(avatar.getPosition()) < 3.5;
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
                            ((Wall) bd).getGridLocation().y == locationCache.y + 1)
                        return true;
                }
                else if (direction == DOWN){
                    if (((Wall) bd).getGridLocation().x == locationCache.x &&
                            ((Wall) bd).getGridLocation().y == locationCache.y - 1)
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
        switches.clear();
        doors.clear();

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
        switches = null;
        doors = null;
    }
}
