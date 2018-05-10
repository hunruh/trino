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
    private static String MENU_FILE = "trino/mainMenu.wav";
    private static String POP_1_FILE = "trino/pop1.mp3";
    private static String POP_2_FILE = "trino/pop2.mp3";
    private static String POP_3_FILE = "trino/pop3.mp3";
    private static String POP_4_FILE = "trino/pop4.mp3";
    private static String POP_5_FILE = "trino/pop5.mp3";
    private static String POOF_FILE = "trino/poof.mp3";
    private static String CRASH_FILE = "trino/enemyCrash.mp3";
    private static String ALERT_FILE = "trino/alert.mp3";
    private static String MUNCH_FILE = "trino/munch.mp3";
    private static String CRUNCH_FILE = "trino/crunch.mp3";
    private static String FULL_FILE = "trino/resourceFull.mp3";
    private static String CLONE_PLOP_FILE = "trino/plop.mp3";
    private static String TRANSFORM_FILE = "trino/transform.mp3";
    private static String CARN_CHARGE_FILE = "trino/footDrag.mp3";
    private static String DOOR_FILE = "trino/doorOpen.mp3";
    private static String BUBBLE_1_FILE = "trino/bubble1.mp3";
    private static String BUBBLE_2_FILE = "trino/bubble2.mp3";
    private static String BUBBLE_3_FILE = "trino/bubble3.mp3";
    private static String BUBBLE_4_FILE = "trino/bubble4.mp3";

    private Music bgMusic;
    private Music bgDoll;
    private Music bgHerb;
    private Music bgCarn;
    private Music mainMenu;

    private Sound cottonPickUp;
    private Sound eatWall;
    private Sound collideWall;
    private Sound transformSound;
    private Sound crashSound;
    private Sound alertSound;
    private Sound munchSound;
    private Sound crunchSound;
    private Sound fullSound;
    private Sound plopSound;
    private Sound footDragSound;
    private Sound doorOpenSound;
    private Sound bubbleSound;
    private Sound chargeSound;

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
        mainMenu = Gdx.audio.newMusic(Gdx.files.internal(MENU_FILE));

        cottonPickUp = Gdx.audio.newSound(Gdx.files.internal(POP_1_FILE));
        eatWall = Gdx.audio.newSound(Gdx.files.internal(POP_2_FILE));
        collideWall = Gdx.audio.newSound(Gdx.files.internal(POP_5_FILE));
        transformSound = Gdx.audio.newSound(Gdx.files.internal(TRANSFORM_FILE));
        crashSound = Gdx.audio.newSound(Gdx.files.internal(CRASH_FILE));
        alertSound = Gdx.audio.newSound(Gdx.files.internal(ALERT_FILE));
        munchSound = Gdx.audio.newSound(Gdx.files.internal(MUNCH_FILE));
        crunchSound = Gdx.audio.newSound(Gdx.files.internal(CRUNCH_FILE));
        fullSound = Gdx.audio.newSound(Gdx.files.internal(FULL_FILE));
        plopSound = Gdx.audio.newSound(Gdx.files.internal(CLONE_PLOP_FILE));
        footDragSound = Gdx.audio.newSound(Gdx.files.internal(CARN_CHARGE_FILE));
        doorOpenSound = Gdx.audio.newSound(Gdx.files.internal(DOOR_FILE));
        bubbleSound = Gdx.audio.newSound(Gdx.files.internal(BUBBLE_1_FILE));
        chargeSound =  Gdx.audio.newSound(Gdx.files.internal(POP_3_FILE));

    }

    public void playBackground(int form) {
        if (bgMusic != null) {
            bgMusic.pause();
//            bgDoll.pause();
            bgHerb.pause();
            bgCarn.pause();
            mainMenu.pause();
        }
        if (GameController.musicState) {
            GDXRoot.musicScreen = false;
            bgMusic.setLooping(true);
            bgMusic.setVolume(0.10f);
            bgMusic.play();
            bgMusic.pause();
            bgMusic.setPosition(0f);
            bgMusic.play();

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
        else {
            GDXRoot.musicScreen = false;
            bgMusic.setLooping(true);
            bgMusic.setVolume(0.0f);
            bgMusic.play();
            bgMusic.pause();
            bgMusic.setPosition(0f);
            bgMusic.play();

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
    }

    public void mainMenu() {
        if (mainMenu != null) {
            mainMenu.pause();
        }
        if (GDXRoot.musicScreen) {
            System.out.println("hi!");
            mainMenu.setLooping(true);
            mainMenu.setVolume(0.10f);
            mainMenu.play();
            mainMenu.pause();
            mainMenu.setPosition(0f);
            mainMenu.play();
        }
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

    public void playMunch(){
        munchSound.pause();
        if (GameController.soundState) {
            munchSound.play(0.5f);
        }
        else {
            munchSound.play(0.0f);
        }
    }

    public void playPlop(){
        plopSound.pause();
        if (GameController.soundState) {
            plopSound.play(0.5f);
        }
        else {
            plopSound.play(0.0f);
        }
    }

    public void playFull(){
        fullSound.pause();
        if (GameController.soundState) {
            fullSound.play(1f);
        }
        else {
            fullSound.play(0.0f);
        }
    }

    public void playCrunch(){
        crunchSound.pause();
        if (GameController.soundState) {
            crunchSound.play(1f);
        }
        else {
            crunchSound.play(0.0f);
        }
    }

    public void playFootDrag(){
        footDragSound.pause();
        if (GameController.soundState) {
            footDragSound.play(1f);
        }
        else {
            footDragSound.play(0.0f);
        }
    }
    public void playChargeSound(){
        chargeSound.pause();
        if (GameController.soundState) {
            chargeSound.play(1f);
        }
        else {
            chargeSound.play(0.0f);
        }
    }

    public void playDoorOpen(){
        doorOpenSound.pause();
        if (GameController.soundState) {
            doorOpenSound.play(1f);
        }
        else {
            doorOpenSound.play(0.0f);
        }
    }

    public void playBubbleLoop(){
        bubbleSound.pause();
        if (GameController.soundState) {
            bubbleSound.play(1.0f);
        }
        else {
            bubbleSound.stop();
        }
    }
    public void stopBubbleLoop(){
        bubbleSound.stop();
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
            munchSound.dispose();
            crunchSound.dispose();
            fullSound.dispose();
            plopSound.dispose();
            footDragSound.dispose();
            doorOpenSound.dispose();
            bubbleSound.dispose();
            chargeSound.dispose();
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
