package tiktaalik.trino.resources;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import tiktaalik.trino.obstacle.BoxObstacle;
import tiktaalik.trino.duggi.DuggiModel;

public class EdibleWall extends BoxObstacle {
    private final int HERBIVORE = 1;
    private int index;

    public EdibleWall(float width, float height) {
        this(0,0,width,height, 0);
    }

    public EdibleWall(float width, float height, int index) {
        this(0,0,width,height, index);
    }

    public EdibleWall(float x, float y, float width, float height, int index) {
        super(x,y,width,height);
        setBodyType(BodyDef.BodyType.StaticBody);
        setName("edible wall");
        this.index = index;
    }

    public boolean canEat(DuggiModel duggi){
        return duggi.getForm() == HERBIVORE;
    }

    public float getX(){
        return super.getX();
    }

    public float getY(){
        return super.getY();
    }

    public int getIndex(){
        return index;
    }

    public void setIndex(int index){
        this.index = index;
    }



}
