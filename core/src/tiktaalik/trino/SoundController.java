package tiktaalik.trino;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Array;
import tiktaalik.trino.duggi.Dinosaur;

public class SoundController {
    private GameController.AssetState soundAssetState = GameController.AssetState.EMPTY;
    private Array<String> assets;

    private static String MUSIC_FILE = "trino/doll_bg.mp3";
    private static String HERBIVORE_BG_FILE = "trino/herbivore_bg.mp3";
    private static String CARNIVORE_BG_FILE = "trino/carnivore_bg.mp3";
    private static String POP_1_FILE = "trino/pop1.mp3";
    private static String POP_2_FILE = "trino/pop2.mp3";
    private static String POP_3_FILE = "trino/pop3.mp3";
    private static String POP_4_FILE = "trino/pop4.mp3";
    private static String POP_5_FILE = "trino/pop5.mp3";
    private static String POOF_FILE = "trino/poof.mp3";
    private static String CRASH_FILE = "trino/enemyCrash.mp3";
    private static String ALERT_FILE = "trino/alert.mp3";

    private Music bgMusic;
    private Music bgDoll;
    private Music bgHerb;
    private Music bgCarn;

    private Sound cottonPickUp;
    private Sound eatWall;
    private Sound collideWall;
    private Sound transformSound;
    private Sound crashSound;
    private Sound alertSound;

    private float dollStartTime = 9.056f;
    private float herbivoreStartTime = 131.387f;
    private float carnivoreStartTime = 253.718f;
    private float dollEndTime = 117.735f;
    private float herbivoreEndTime = 240.066f;
    private float carnivoreEndTime = 362.397f;
    private int currentForm = Dinosaur.DOLL_FORM;

    private static SoundController theController = null;

    public static SoundController getInstance() {
        if (theController == null) {
            theController = new SoundController();
        }
        return theController;
    }

    public void init() {
        bgMusic = Gdx.audio.newMusic(Gdx.files.internal(MUSIC_FILE));
        bgHerb = Gdx.audio.newMusic(Gdx.files.internal(HERBIVORE_BG_FILE));
        bgCarn = Gdx.audio.newMusic(Gdx.files.internal(CARNIVORE_BG_FILE));

        cottonPickUp = Gdx.audio.newSound(Gdx.files.internal(POP_1_FILE));
        eatWall = Gdx.audio.newSound(Gdx.files.internal(POP_2_FILE));
        collideWall = Gdx.audio.newSound(Gdx.files.internal(POP_5_FILE));
        transformSound = Gdx.audio.newSound(Gdx.files.internal(POOF_FILE));
        crashSound = Gdx.audio.newSound(Gdx.files.internal(CRASH_FILE));
        alertSound = Gdx.audio.newSound(Gdx.files.internal(ALERT_FILE));
    }

    public void playBackground(int form) {
        if (bgMusic != null) {
            bgMusic.pause();
//            bgDoll.pause();
            bgHerb.pause();
            bgCarn.pause();
        }

        if (GameController.musicState) {
            bgMusic.setLooping(true);
            bgMusic.setVolume(0.10f);
            bgMusic.play();
            bgMusic.pause();
            bgMusic.setPosition(0f);
            bgMusic.play();
        }
        else {
            bgMusic.setLooping(true);
            bgMusic.setVolume(0.0f);
            bgMusic.play();
            bgMusic.pause();
            bgMusic.setPosition(0f);
            bgMusic.play();
        }
        bgHerb.setLooping(true);
        bgHerb.setVolume(0.0f);
        bgHerb.play();
        bgHerb.pause();
        bgHerb.setPosition(0f);
        bgHerb.play();

        bgCarn.setLooping(true);
        bgCarn.setVolume(0.0f);
        bgCarn.play();
        bgCarn.pause();
        bgCarn.setPosition(0f);
        bgCarn.play();
    }

    public void playCottonPickup() {
        cottonPickUp.pause();
        if (GameController.soundState) {
            cottonPickUp.play(1.0f);
        }
        else {
            cottonPickUp.play(0.0f);
        }
    }

    public void playCollide() {
        collideWall.pause();
        if (GameController.soundState) {
            collideWall.play(1.0f);
        }
        else {
            collideWall.play(0.0f);
        }
    }

    public void playEat() {
        eatWall.pause();
        if (GameController.soundState) {
            eatWall.play(1.0f);
        }
        else {
            eatWall.play(0.0f);
        }
    }

    public void playTransform() {
        transformSound.pause();
        if (GameController.soundState) {
            transformSound.play(1.0f);
        }
        else {
            transformSound.play(0.0f);
        }
    }

    public void playCrash(){
        crashSound.pause();
        if (GameController.soundState) {
            crashSound.play(0.1f);
        }
        else {
            crashSound.play(0.0f);
        }
    }

    public void playAlert(){
        alertSound.pause();
        if (GameController.soundState) {
            alertSound.play(0.1f);
        }
        else {
            alertSound.play(0.0f);
        }
    }

    /** Change the music based on timestamp */
    public void changeBackground(int form){
        if (GameController.musicState) {
            if (form == Dinosaur.DOLL_FORM){
                bgMusic.setVolume(0.10f);
                bgHerb.setVolume(0.0f);
                bgCarn.setVolume(0.0f);
            } else if (form == Dinosaur.HERBIVORE_FORM){
                bgHerb.setVolume(0.10f);
                bgMusic.setVolume(0.0f);
                bgCarn.setVolume(0.0f);
            } else {
                bgHerb.setVolume(0.0f);
                bgMusic.setVolume(0.0f);
                bgCarn.setVolume(0.10f);
            }
        }
        else {
            bgMusic.setVolume(0.0f);
            bgHerb.setVolume(0.0f);
            bgCarn.setVolume(0.0f);
        }
        currentForm = form;
//        bgMusic.pause();
//        float seconds = bgMusic.getPosition();
//        seconds = formToSeconds(currentForm, seconds);
//        bgMusic.setVolume(0.10f);
//        bgMusic.play();
//        bgMusic.pause();
//        bgMusic.setPosition(newMusicPosition(form,seconds));
//        bgMusic.play();
    }

    private Music formToMusic(int form) {
        if (form == Dinosaur.DOLL_FORM)
            return bgDoll;
        else if (form == Dinosaur.HERBIVORE_FORM)
            return bgHerb;
        else
            return bgCarn;
    }

    private float formToSeconds(int form, float seconds){
        if (form == Dinosaur.DOLL_FORM)
            return seconds - dollStartTime;
        else if (form == Dinosaur.HERBIVORE_FORM)
            return seconds - herbivoreStartTime;
        else
            return seconds - carnivoreStartTime;
    }

    private float newMusicPosition (int form, float seconds){
        if (form == Dinosaur.DOLL_FORM)
            return dollStartTime + seconds;
        else if (form == Dinosaur.HERBIVORE_FORM)
            return herbivoreStartTime + seconds;
        else
            return carnivoreStartTime + seconds;

    }

    public void dispose() {
        if (bgMusic != null) {
            bgMusic.dispose();
//            bgDoll.dispose();
            bgHerb.dispose();
            bgCarn.dispose();
            cottonPickUp.dispose();
            eatWall.dispose();
            collideWall.dispose();
            transformSound.dispose();
            crashSound.dispose();
            alertSound.dispose();
        }
    }

    public void checkMusicEnd(){
        //.println("song position at " + bgMusic.getPosition());
        if (currentForm == Dinosaur.DOLL_FORM) {
            if (bgMusic.isPlaying()) {
                if (bgMusic.getPosition() >= dollEndTime) {
                    System.out.println("beyond end time, begin looping");
                    bgMusic.pause();
                    bgMusic.setVolume(0.10f);
                    bgMusic.play();
                    bgMusic.pause();
                    bgMusic.setPosition(dollStartTime);
                    bgMusic.play();
                }
            }
        } else if (currentForm == Dinosaur.HERBIVORE_FORM) {
            if (bgMusic.isPlaying()) {
                if (bgMusic.getPosition() >= herbivoreEndTime) {
                    bgMusic.pause();
                    bgMusic.setVolume(0.10f);
                    bgMusic.play();
                    bgMusic.pause();
                    bgMusic.setPosition(herbivoreStartTime);
                    bgMusic.play();
                }
            }
        } else {
            if (bgMusic.isPlaying()) {
                if (bgMusic.getPosition() >= carnivoreEndTime) {
                    bgMusic.pause();
                    bgMusic.setVolume(0.10f);
                    bgMusic.play();
                    bgMusic.pause();
                    bgMusic.setPosition(carnivoreStartTime);
                    bgMusic.play();
                }
            }
        }

    }
}
