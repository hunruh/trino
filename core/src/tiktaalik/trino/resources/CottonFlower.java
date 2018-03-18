package tiktaalik.trino.resources;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class CottonFlower{
    private final int DOLL = 0;

    private TextureRegion texture;
    private Vector2 location;

    public CottonFlower(){
        location = new Vector2(0f,0f);
    }

    public CottonFlower(float x, float y){
        location = (new Vector2(x, y));
    }

    public CottonFlower(Vector2 location){
        this.location = location;
    }

    public TextureRegion getTexture() {
        return texture;
    }

    public void setTexture(TextureRegion texture) {
        this.texture = texture;
    }

    public Vector2 getLocation() {return location;}

    public float getX() {return location.x;}

    public float getY() {return location.y;}

    public void setLocation(Vector2 location) {this.location = location;}

    public boolean canEat(DuggiModel duggi){
        return duggi.getForm() == DOLL;
    }


}
