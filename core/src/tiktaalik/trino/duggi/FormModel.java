package tiktaalik.trino.duggi;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public abstract class FormModel {
    private TextureRegion texture;

    public TextureRegion getTexture() {
        return texture;
    }

    public void setTexture(TextureRegion texture) {
        this.texture = texture;
    }
}
