package tiktaalik.trino.level_editor;
import com.badlogic.gdx.math.Vector2;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import tiktaalik.trino.duggi.Dinosaur;

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
        if (orientation == "left") return Dinosaur.LEFT;
        else if (orientation == "right") return Dinosaur.RIGHT;
        else if (orientation == "up") return Dinosaur.UP;
        else if (orientation == "down") return Dinosaur.DOWN;
        else return -1;
    }

    public int[][] getResourceList(int level, int type){
        JSONArray enemy = (JSONArray)((JSONObject)(levels.get(level))).get("Enemy");
        int[][] tmp = new int[enemy.size()][3];
        for (int i = 0; i < enemy.size(); i++){
            JSONObject e = (JSONObject)(enemy.get(i));

        }
        return null;

    }




}
