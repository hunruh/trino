package tiktaalik.trino.duggi;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import tiktaalik.trino.Canvas;
import tiktaalik.util.FilmStrip;

public class Herbivore extends Dinosaur {
    protected boolean actionLoop = false;
    private boolean upDown = false;
    private float vertices[] = new float[16];

    protected FilmStrip[] swimmingTextureSet;
    protected int numSwimmingFrames[];

    protected FilmStrip[] camoTextureSet;
    protected int numCamoFrames[];

    public Herbivore(Dinosaur d) {
        super(d);
        swimmingTextureSet = new FilmStrip[8];
        numSwimmingFrames = new int[8];
        camoTextureSet = new FilmStrip[4];
        numCamoFrames = new int[4];
        upDown = false;
        shape = new PolygonShape();

        int ctr = 0;
        for (float theta = 0; theta < 2 * Math.PI; theta += ((2.0f * Math.PI)/(vertices.length / 2))) {
            if (ctr >= 16)
                break;

            vertices[ctr++] = (float)(radius * Math.cos(theta)); // x
            vertices[ctr++] = (float)(-radius * Math.sin(theta) * .5) - radius/4 - 0.3f; // y
        }
        shape.set(vertices);

        body.destroyFixture(body.getFixtureList().first());
        fixture.shape = shape;
        body.createFixture(fixture);
    }

    public int getForm() {
        return HERBIVORE_FORM;
    }

    protected boolean loopAction() {
        return false;
    }

    public void setSwimmingTextureSet(Texture leftDiving, int leftDivingFrames,
                                      Texture leftSwimming, int leftSwimmingFrames,
                                      Texture rightDiving, int rightDivingFrames,
                                      Texture rightSwimming, int rightSwimmingFrames,
                                      Texture upDiving, int upDivingFrames,
                                      Texture upSwimming, int upSwimmingFrames,
                                      Texture downDiving, int downDivingFrames,
                                      Texture downSwimming, int downSwimmingFrames) {
        numSwimmingFrames[LEFT] = leftDivingFrames;
        swimmingTextureSet[LEFT] = new FilmStrip(leftDiving,1,leftDivingFrames,leftDivingFrames);
        numSwimmingFrames[LEFT + 4] = leftSwimmingFrames;
        swimmingTextureSet[LEFT + 4] = new FilmStrip(leftSwimming,1,leftSwimmingFrames,leftSwimmingFrames);
        numSwimmingFrames[RIGHT] = rightDivingFrames;
        swimmingTextureSet[RIGHT] = new FilmStrip(rightDiving,1,rightDivingFrames,rightDivingFrames);
        numSwimmingFrames[RIGHT + 4] = rightSwimmingFrames;
        swimmingTextureSet[RIGHT + 4] = new FilmStrip(rightSwimming,1,rightSwimmingFrames,rightSwimmingFrames);
        numSwimmingFrames[UP] = upDivingFrames;
        swimmingTextureSet[UP] = new FilmStrip(upDiving,1,upDivingFrames,upDivingFrames);
        numSwimmingFrames[UP + 4] = upSwimmingFrames;
        swimmingTextureSet[UP + 4] = new FilmStrip(upSwimming,1,upSwimmingFrames,upSwimmingFrames);
        numSwimmingFrames[DOWN] = downDivingFrames;
        swimmingTextureSet[DOWN] = new FilmStrip(downDiving,1,downDivingFrames,downDivingFrames);
        numSwimmingFrames[DOWN + 4] = downSwimmingFrames;
        swimmingTextureSet[DOWN + 4] = new FilmStrip(downSwimming,1,downSwimmingFrames,downSwimmingFrames);
    }

    public void update(float dt) {
        super.update(dt);

        if (direction == UP || direction == DOWN) {
            if (!upDown) {
                upDown = true;

                int ctr = 0;
                for (float theta = 0; theta < 2 * Math.PI; theta += ((2.0f * Math.PI)/(vertices.length / 2))) {
                    if (ctr >= 16)
                        break;

                    vertices[ctr++] = (float)(radius * Math.cos(theta) * .5); // x
                    vertices[ctr++] = (float)(radius * Math.sin(theta) * .8) - radius/4; // y
                }
                shape.set(vertices);

                body.destroyFixture(body.getFixtureList().first());
                fixture.shape = shape;
                body.createFixture(fixture);
            }
        } else {
            if (upDown) {
                upDown = false;

                int ctr = 0;
                for (float theta = 0; theta < 2 * Math.PI; theta += ((2.0f * Math.PI)/(vertices.length / 2))) {
                    if (ctr >= 16)
                        break;

                    vertices[ctr++] = (float)(radius * Math.cos(theta)); // x
                    vertices[ctr++] = (float)(-radius * Math.sin(theta) * .5) - radius/4 - 0.3f; // y
                }
                shape.set(vertices);

                body.destroyFixture(body.getFixtureList().first());
                fixture.shape = shape;
                body.createFixture(fixture);
            }
        }
    }

    public void drawShadow(Canvas canvas) {
        if (upDown)
            canvas.drawShadow(getX()*drawScale.x,getY()*drawScale.x + 8,1.25f*radius*drawScale.x, 1.6f*radius*drawScale.x, shadowOpacity);
        else
            canvas.drawShadow(getX()*drawScale.x,getY()*drawScale.x - 6,2*radius*drawScale.x, radius*drawScale.x, shadowOpacity);
    }

    public void draw(Canvas canvas) {
        int offsetX = 0;
        if (eating || actionAnimating) {
            if (direction == LEFT)
                offsetX = -37;
        }
        super.draw(canvas, offsetX, 0);
    }
}
