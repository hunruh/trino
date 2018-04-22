package tiktaalik.trino.duggi;

public class Doll extends Dinosaur {
    protected boolean actionLoop = false;

    public Doll(Dinosaur d) {
        super(d);
    }

    public Doll(float x, float y, float radius) {
        super(x, y, radius);
    }

    public int getForm() {
        return DOLL_FORM;
    }

    protected boolean loopAction() {
        return false;
    }
}
