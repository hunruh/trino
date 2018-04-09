package tiktaalik.trino.duggi;

import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;

import static tiktaalik.trino.GameController.*;

public class Herbivore extends Dinosaur {
    private Fixture geometry; // A cache value for the fixture (for resizing)

    public Herbivore(Dinosaur d) {
        super(d);
    }

    public int getForm() {
        return HERBIVORE_FORM;
    }

    public void performAction() {

    }

    protected void createFixtures() {
        if (body == null) {
            return;
        }

        releaseFixtures();

        // Create the fixture
        fixture.shape = shape;
        geometry = body.createFixture(fixture);
        Filter filter = geometry.getFilterData();
        filter.categoryBits = HERBIVORE_BYTE;
        filter.maskBits = WALL_BYTE | ENEMY_BYTE | DOLL_BYTE;
        geometry.setFilterData(filter);
        markDirty(false);
    }

    protected void releaseFixtures() {
        if (geometry != null) {
            body.destroyFixture(geometry);
            geometry = null;
        }
    }
}
