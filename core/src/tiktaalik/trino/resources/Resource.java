package tiktaalik.trino.resources;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public abstract class Resource {
    private TextureRegion texture;
    private Vector2 location;

    public TextureRegion getTexture() {
        return texture;
    }

    public void setTexture(TextureRegion texture) {
        this.texture = texture;
    }

    public Vector2 getLocation() {return location;}

    public void setLocation(Vector2 location) {this.location = location;}





}
