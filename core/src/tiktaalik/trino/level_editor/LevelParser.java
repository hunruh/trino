package tiktaalik.trino.level_editor;
import com.badlogic.gdx.math.Vector2;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import tiktaalik.trino.duggi.Dinosaur;
import tiktaalik.util.PooledList;

import java.io.*;

public class LevelParser {
    JSONObject obj = new JSONObject();
    JSONParser parser = new JSONParser();
    JSONArray levels = new JSONArray();
    public void parse(String path) throws Exception{
        FileReader reader = new FileReader(path);
        obj = (JSONObject)parser.parse(reader);
        levels = (JSONArray)obj.get("Levels");
        System.out.println(levels);
    }

    public boolean isNightLevel(int level){
        String tmp = (String)((JSONObject)(levels.get(level))).get("Night");
        if (tmp == "yes") return true;
        else return false;
    }

    /**
     * the key should only be switch goal or player
     * @param level
     * @param key
     * @return
     */
    public Vector2 getObjectLocation(int level, String key){
        JSONObject tmp = (JSONObject)((JSONObject)(levels.get(level))).get("key");
        return (new Vector2((Integer)(tmp.get("x")),(Integer)(tmp.get("y"))));
    }

    public int getPlayerInitialOrientation(int level){
        JSONObject tmp = (JSONObject)((JSONObject)(levels.get(level))).get("Player");
        String orientation = (String)tmp.get("Orientation");
        if (orientation == "Left") return Dinosaur.LEFT;
        else if (orientation == "Right") return Dinosaur.RIGHT;
        else if (orientation == "Up") return Dinosaur.UP;
        else if (orientation == "Down") return Dinosaur.DOWN;
        else return -1;
    }

    

    public PooledList<Vector2> getCottonList(int level){
        System.out.println("in getcottonlist");
        System.out.println(levels.get(level));
        JSONArray resources = (JSONArray)((JSONObject)((JSONObject)(levels.get(level))).get("GameObjects")).get("Cottons");
        System.out.println(resources.size());
        PooledList<Vector2> tmp = new PooledList<Vector2>();
        for (int i = 0; i < resources.size(); i++){
            JSONObject r = (JSONObject)(resources.get(i));
            tmp.add(new Vector2((Integer)(r.get("x")),(Integer)(r.get("y"))));
        }
        return tmp;

    }




}
