package tiktaalik.trino.duggi;

public class Herbivore extends Dinosaur {
    public Herbivore(Dinosaur d) {
        super(d);
    }

    public int getForm() {
        return HERBIVORE_FORM;
    }

    public void performAction() {

    }

}
