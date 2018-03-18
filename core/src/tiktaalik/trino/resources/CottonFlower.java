package tiktaalik.trino.resources;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import tiktaalik.trino.obstacle.BoxObstacle;
import com.badlogic.gdx.math.Vector2;

public class CottonFlower extends BoxObstacle{
    private final int DOLL = 0;
    private int index;

//    private TextureRegion texture;
//    private Vector2 location;

    public CottonFlower(float width, float height) {
        this(0,0,width,height, 0);
    }

    public CottonFlower(float width, float height, int index) {
        this(0,0,width,height, index);
    }

    public CottonFlower(float x, float y, float width, float height, int index) {
        super(x,y,width,height);
        setBodyType(BodyDef.BodyType.StaticBody);
        setName("cotton flower");
        this.index = index;
    }


//    public TextureRegion getTexture() {
//        return texture;
//    }
//
//    public void setTexture(TextureRegion texture) {
//        this.texture = texture;
//    }
//
//    public Vector2 getLocation() {return location;}

    public float getX() {return super.getX();}

    public float getY() {return super.getY();}

//    public void setLocation(Vector2 location) {this.location = location;}

    public boolean canEat(DuggiModel duggi){
        return duggi.getForm() == DOLL;
    }

    public int getIndex(){
        return index;
    }

    public void setIndex(int index){
        this.index = index;
    }


}
