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

    private static String DOLL_BG_FILE = "trino/doll_bg.mp3";
    private static String HERBIVORE_BG_FILE = "trino/herbivore_bg.mp3";
    private static String CARNIVORE_BG_FILE = "trino/carnivore_bg.mp3";
    private static String POP_1_FILE = "trino/pop1.mp3";
    private static String POP_2_FILE = "trino/pop2.mp3";
    private static String POP_3_FILE = "trino/pop3.mp3";
    private static String POP_4_FILE = "trino/pop4.mp3";
    private static String POP_5_FILE = "trino/pop5.mp3";
    private static String POOF_FILE = "trino/poof.mp3";

    private Music bgMusic;
    private Music bgDoll;
    private Music bgHerb;
    private Music bgCarn;

    private Sound cottonPickUp;
    private Sound eatWall;
    private Sound collideWall;
    private Sound transformSound;

    private static SoundController theController = null;

    public static SoundController getInstance() {
        if (theController == null) {
            theController = new SoundController();
        }
        return theController;
    }

    public void init() {
        bgDoll = Gdx.audio.newMusic(Gdx.files.internal(DOLL_BG_FILE));
        bgHerb = Gdx.audio.newMusic(Gdx.files.internal(HERBIVORE_BG_FILE));
        bgCarn = Gdx.audio.newMusic(Gdx.files.internal(CARNIVORE_BG_FILE));

        cottonPickUp = Gdx.audio.newSound(Gdx.files.internal(POP_1_FILE));
        eatWall = Gdx.audio.newSound(Gdx.files.internal(POP_2_FILE));
        collideWall = Gdx.audio.newSound(Gdx.files.internal(POP_5_FILE));
        transformSound = Gdx.audio.newSound(Gdx.files.internal(POOF_FILE));
    }

    public void playBackground(int form) {
        if (bgMusic != null) {
            bgMusic.pause();
            bgDoll.pause();
            bgHerb.pause();
            bgCarn.pause();
        }

        bgMusic = formToMusic(form);
        bgMusic.setLooping(true);
        bgMusic.setVolume(0.10f);
        bgMusic.setPosition(0);
        bgMusic.play();
    }

    public void playCottonPickup() {
        cottonPickUp.pause();
        cottonPickUp.play(1.0f);
    }

    public void playCollide() {
        collideWall.pause();
        collideWall.play(1.0f);
    }

    public void playEat() {
        eatWall.pause();
        eatWall.play(1.0f);
    }

    public void playTransform() {
        transformSound.pause();
        transformSound.play(1.0f);
    }

    /** Change the music based on timestamp */
    public void changeBackground(int form){
        bgMusic.pause();
        float seconds = bgMusic.getPosition();
        bgMusic = formToMusic(form);
        bgMusic.setLooping(true);
        bgMusic.setVolume(0.10f);
        bgMusic.play();
        bgMusic.pause();
        bgMusic.setPosition(seconds);
        bgMusic.play();
    }

    private Music formToMusic(int form) {
        if (form == Dinosaur.DOLL_FORM)
            return bgDoll;
        else if (form == Dinosaur.HERBIVORE_FORM)
            return bgHerb;
        else
            return bgCarn;
    }

    public void dispose() {
        if (bgMusic != null) {
            bgMusic.dispose();
            bgDoll.dispose();
            bgHerb.dispose();
            bgCarn.dispose();
            cottonPickUp.dispose();
            eatWall.dispose();
            collideWall.dispose();
            transformSound.dispose();
        }
    }
}
