package tiktaalik.trino.resources;
import com.badlogic.gdx.math.Vector2;

public class Resource {
    /**location of the resource on the map*/
    private Vector2 location;

    public Resource(Vector2 location){
        this.location = location;
    }

    public Resource(){
        this.location = new Vector2(0,0);
    }

}
