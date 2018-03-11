package tiktaalik.trino.resources;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import tiktaalik.trino.obstacle.BoxObstacle;
import tiktaalik.trino.duggi.DuggiModel;

public class EdibleWall extends BoxObstacle {
    private final int HERBIVORE = 1;

    public EdibleWall(float width, float height) {
        this(0,0,width,height);
    }

    public EdibleWall(float x, float y, float width, float height) {
        super(x,y,width,height);
        setBodyType(BodyDef.BodyType.StaticBody);
        setName("edible wall");
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
    


}
