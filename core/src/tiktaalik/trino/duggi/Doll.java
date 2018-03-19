package tiktaalik.trino.duggi;

public class Doll extends Dinosaur {
    public Doll(Dinosaur d) {
        super(d);
    }

    public Doll(float x, float y, float radius) {
        super(x, y, radius);
    }

    public int getForm() {
        return DOLL_FORM;
    }

    public void performAction() {

    }
}
