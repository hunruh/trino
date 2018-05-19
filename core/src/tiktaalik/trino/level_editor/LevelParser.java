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
        InputStream in = getClass().getResourceAsStream(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//        FileReader reader = new FileReader(path);
        obj = (JSONObject)parser.parse(reader);
        levels = (JSONArray)obj.get("Levels");
    }

    public boolean isNightLevel(int level){
        String tmp = (String)((JSONObject)(levels.get(level))).get("Night");
        if (tmp.equals("yes")) return true;
        else return false;
    }

    public boolean isPanningLevel(int level){
        String tmp = (String)((JSONObject)(levels.get(level))).get("Panning");
        if (tmp.equals("yes")) return true;
        else return false;
    }

    public Vector2 getLevelDimension(int level){
        JSONObject tmp = (JSONObject)((JSONObject)(levels.get(level))).get("Dimension");
        return (new Vector2((Long)tmp.get("width"), (Long)tmp.get("height")));
    }

    public Long getLevelTime(int level) {
        Long tmp = (Long)((JSONObject)(levels.get(level))).get("Time");
        return tmp;
    }

    /**
     * the key should only be switch goal or player
     * @param level
     * @param key
     * @return
     */
    public Vector2 getObjectLocation(int level, String key){
        JSONObject tmp = (JSONObject)((JSONObject)((JSONObject)(levels.get(level))).get("GameObjects")).get(key);
        return (new Vector2((Long)(tmp.get("x")),(Long)(tmp.get("y"))));
    }

    public int getPlayerInitialOrientation(int level){
        JSONArray tmp = (JSONArray)((JSONObject)((JSONObject)(levels.get(level))).get("GameObjects")).get("Player");
        String orientation = (String)((JSONObject)(tmp.get(0))).get("direction");
        if (orientation.equals("Left")) return Dinosaur.LEFT;
        else if (orientation.equals("Right")) return Dinosaur.RIGHT;
        else if (orientation.equals("Up")) return Dinosaur.UP;
        else if (orientation.equals("Down")) return Dinosaur.DOWN;
        else return -1;
    }

    public PooledList<String[]> getEnemiesInformation(int level){
        PooledList<String[]> tmp = new PooledList<String[]>();
        JSONArray enemies = (JSONArray)((JSONObject)((JSONObject)(levels.get(level))).get("GameObjects")).get("Enemies");
        for (int i = 0; i < enemies.size(); i++){
            JSONObject e = (JSONObject)(enemies.get(i));
            tmp.add(new String[]{(String)e.get("direction"), (String)e.get("type"), (String)e.get("movement")});
        }
        return tmp;
    }


    public PooledList<Vector2> getAssetList(int level, String key){
        JSONArray resources = (JSONArray)((JSONObject)((JSONObject)(levels.get(level))).get("GameObjects")).get(key);
        PooledList<Vector2> tmp = new PooledList<Vector2>();
        for (int i = 0; i < resources.size(); i++){
            JSONObject r = (JSONObject)(resources.get(i));
            tmp.add(new Vector2((Long)(r.get("x")),(Long)(r.get("y"))));
        }
        return tmp;
    }

    public long getStarTime(int level, int star){
        JSONObject tmp = (JSONObject)((JSONObject)(levels.get(level))).get("Stars");
        if (star == 2)
            return (Long)((tmp.get("three")));
        else if (star == 1)
            return (Long)(tmp.get("two"));
        else
            return (Long)(tmp.get("one"));
    }





}
