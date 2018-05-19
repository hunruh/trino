package tiktaalik.trino.level_editor;
import com.badlogic.gdx.math.Vector2;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import tiktaalik.trino.duggi.Dinosaur;
import tiktaalik.util.PooledList;

import java.io.*;

public class SaveFileParser {
    JSONObject obj = new JSONObject();
    JSONParser parser = new JSONParser();
    JSONArray levels = new JSONArray();

    public void parse(String path) throws Exception {
//        InputStream in = getClass().getResourceAsStream(path);
//        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        FileReader reader = new FileReader(path);
        obj = (JSONObject) parser.parse(reader);
        levels = (JSONArray) obj.get("Levels");
        reader.close();
    }

    public boolean[] levelCompletionArray(){
        boolean[] results = new boolean[levels.size()];
        for (int i = 0; i < levels.size(); i++){
            if (((JSONObject)(levels.get(i))).get("Completed").equals("true"))
                results[i] = true;
            else
                results[i] = false;
        }
        return results;
    }

    public long[] levelScoreArray(){
        long[] results = new long[levels.size()];
        for (int i = 0; i < levels.size(); i++){
            results[i] = (Long)((JSONObject)(levels.get(i))).get("Score");
        }
        return results;
    }


    public long[] levelTimeArray(){
        long[] results = new long[levels.size()];
        for (int i = 0; i < levels.size(); i++){
            results[i] = (Long)((JSONObject)(levels.get(i))).get("Time");
        }
        return results;
    }

    public long[] levelStarsArray(){
        long[] results = new long[levels.size()];
        for (int i = 0; i < levels.size(); i++){
            results[i] = ((Long)((JSONObject)(levels.get(i))).get("Stars"));
        }
        return results;
    }

    public void changeLevelCompletion(int level, boolean value){
        ((JSONObject)(levels.get(level))).put("Completed", value);
    }

    public void changeLevelScore(int level, int value){
        ((JSONObject)(levels.get(level))).put("Score", value);
    }

    public void changeLevelTime(int level, int value){
        ((JSONObject)(levels.get(level))).put("Time", value);
    }

    public void changeLevelStars(int level, int value){
        ((JSONObject)(levels.get(level))).put("Stars", value);
    }

    public void printObj(){
        System.out.println(obj);
    }

    public void writeToFile(String path) throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        writer.write(obj.toString());

        writer.close();

    }

    public void clearLevel(int level){
        changeLevelCompletion(level, false);
        changeLevelScore(level, 0);
        changeLevelTime(level, 0);
        changeLevelStars(level, 0);
    }

    public void clearAll(){
        for (int i = 0; i < levels.size(); i++){
            clearLevel(i);
        }
    }
}
