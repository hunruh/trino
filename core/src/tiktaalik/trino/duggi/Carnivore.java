package tiktaalik.trino.duggi;

public class Carnivore extends Dinosaur {
    public Carnivore(Dinosaur d) {
        super(d);
    }

    public int getForm() {
        return CARNIVORE_FORM;
    }
}
