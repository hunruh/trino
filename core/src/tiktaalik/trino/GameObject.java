package tiktaalik.trino;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

/**
 * Base model class to support collisions.
 *
 * This class does not provide Shape information, and cannot be instantiated directly.
 */
public abstract class GameObject {
    // Initialization structures to store body information
    protected BodyDef bodyinfo; // Stores the body information for this shape
    protected FixtureDef fixture; // Stores the fixture information for this shape
    protected MassData massdata; // The mass data of this shape (which may override the fixture)
    protected boolean masseffect; // Whether or not to use the custom mass data
    private String nametag; // A tag for debugging purposesZ
    protected Vector2 drawScale; // Drawing scale to convert physics units to pixels

    protected Body body; // The physics body for Box2D
    protected TextureRegion texture; // The texture for the shape
    protected Vector2 origin; // The texture origin for drawing

    // Track garbage collection status
    private boolean toRemove; // Whether the object should be removed on next pass
    private boolean isDirty; // Whether the object has changed shape and needs a new fixture

    // Caching objects
    protected Vector2 positionCache = new Vector2();
    protected Vector2 velocityCache = new Vector2();
    protected Vector2 centroidCache = new Vector2();
    protected Vector2 scaleCache = new Vector2();

    private int type;

    /// BodyDef Methods
    /**
     * Returns the body type for Box2D physics
     *
     * @return the body type for Box2D physics
     */
    public BodyType getBodyType() {
        return (body != null ? body.getType() : bodyinfo.type);
    }

    /**
     * Returns the body type for Box2D physics
     *
     * @return the body type for Box2D physics
     */
    public void setBodyType(BodyType value) {
        if (body != null) {
            body.setType(value);
        } else {
            bodyinfo.type = value;
        }
    }

    /**
     * Returns the current position for this physics body
     *
     * @return the current position for this physics body
     */
    public Vector2 getPosition() {
        return (body != null ? body.getPosition() : positionCache.set(bodyinfo.position));
    }

    /**
     * Sets the current position for this physics body
     *
     * @param value  the current position for this physics body
     */
    public void setPosition(Vector2 value) {
        if (body != null) {
            body.setTransform(value,body.getAngle());
        } else {
            bodyinfo.position.set(value);
        }
    }

    /**
     * Sets the current position for this physics body
     *
     * @param x  the x-coordinate for this physics body
     * @param y  the y-coordinate for this physics body
     */
    public void setPosition(float x, float y) {
        if (body != null) {
            positionCache.set(x,y);
            body.setTransform(positionCache,body.getAngle());
        } else {
            bodyinfo.position.set(x, y);
        }
    }

    /**
     * Returns the x-coordinate for this physics body
     *
     * @return the x-coordinate for this physics body
     */
    public float getX() {
        return (body != null ? body.getPosition().x : bodyinfo.position.x);
    }

    /**
     * Sets the x-coordinate for this physics body
     *
     * @param value  the x-coordinate for this physics body
     */
    public void setX(float value) {
        if (body != null) {
            positionCache.set(value,body.getPosition().y);
            body.setTransform(positionCache,body.getAngle());
        } else {
            bodyinfo.position.x = value;
        }
    }

    /**
     * Returns the y-coordinate for this physics body
     *
     * @return the y-coordinate for this physics body
     */
    public float getY() {
        return (body != null ? body.getPosition().y : bodyinfo.position.y);
    }

    /**
     * Sets the y-coordinate for this physics body
     *
     * @param value  the y-coordinate for this physics body
     */
    public void setY(float value) {
        if (body != null) {
            positionCache.set(body.getPosition().x,value);
            body.setTransform(positionCache,body.getAngle());
        } else {
            bodyinfo.position.y = value;
        }
    }

    /**
     * Returns the linear velocity for this physics body
     *
     * @return the linear velocity for this physics body
     */
    public Vector2 getLinearVelocity() {
        return (body != null ? body.getLinearVelocity() : velocityCache.set(bodyinfo.linearVelocity));
    }

    /**
     * Sets the linear velocity for this physics body
     *
     * @param value  the linear velocity for this physics body
     */
    public void setLinearVelocity(Vector2 value) {
        if (body != null) {
            body.setLinearVelocity(value);
        } else {
            bodyinfo.linearVelocity.set(value);
        }
    }

    /**
     * Returns the x-velocity for this physics body
     *
     * @return the x-velocity for this physics body
     */
    public float getVX() {
        return (body != null ? body.getLinearVelocity().x : bodyinfo.linearVelocity.x);
    }

    /**
     * Sets the x-velocity for this physics body
     *
     * @param value  the x-velocity for this physics body
     */
    public void setVX(float value) {
        if (body != null) {
            velocityCache.set(value,body.getLinearVelocity().y);
            body.setLinearVelocity(velocityCache);
        } else {
            bodyinfo.linearVelocity.x = value;
        }
    }

    /**
     * Returns the y-velocity for this physics body
     *
     * @return the y-velocity for this physics body
     */
    public float getVY() {
        return (body != null ? body.getLinearVelocity().y : bodyinfo.linearVelocity.y);
    }

    /**
     * Sets the y-velocity for this physics body
     *
     * @param value  the y-velocity for this physics body
     */
    public void setVY(float value) {
        if (body != null) {
            velocityCache.set(body.getLinearVelocity().x,value);
            body.setLinearVelocity(velocityCache);
        } else {
            bodyinfo.linearVelocity.y = value;
        }
    }

    /**
     * Returns true if the body is active
     *
     * @return true if the body is active
     */
    public boolean isActive() {
        return (body != null ? body.isActive() : bodyinfo.active);
    }

    /**
     * Sets whether the body is active
     *
     * @param value  whether the body is active
     */
    public void setActive(boolean value) {
        if (body != null) {
            body.setActive(value);
        } else {
            bodyinfo.active = value;
        }
    }

    /**
     * Returns true if the body is awake
     *
     * @return true if the body is awake
     */
    public boolean isAwake() {
        return (body != null ? body.isAwake() : bodyinfo.awake);
    }

    /**
     * Sets whether the body is awake
     *
     * @param value  whether the body is awake
     */
    public void setAwake(boolean value) {
        if (body != null) {
            body.setAwake(value);
        } else {
            bodyinfo.awake = value;
        }
    }

    /**
     * Returns false if this body should never fall asleep
     *
     * @return false if this body should never fall asleep
     */
    public boolean isSleepingAllowed() {
        return (body != null ? body.isSleepingAllowed() : bodyinfo.allowSleep);
    }

    /**
     * Sets whether the body should ever fall asleep
     *
     * @param value  whether the body should ever fall asleep
     */
    public void setSleepingAllowed(boolean value) {
        if (body != null) {
            body.setSleepingAllowed(value);
        } else {
            bodyinfo.allowSleep = value;
        }
    }

    /**
     * Returns the gravity scale to apply to this body
     *
     * @return the gravity scale to apply to this body
     */
    public float getGravityScale() {
        return (body != null ? body.getGravityScale() : bodyinfo.gravityScale);
    }

    /**
     * Sets the gravity scale to apply to this body
     *
     * @param value  the gravity scale to apply to this body
     */
    public void setGravityScale(float value) {
        if (body != null) {
            body.setGravityScale(value);
        } else {
            bodyinfo.gravityScale = value;
        }
    }

    /**
     * Returns the linear damping for this body.
     *
     * @return the linear damping for this body.
     */
    public float getLinearDamping() {
        return (body != null ? body.getLinearDamping() : bodyinfo.linearDamping);
    }

    /**
     * Sets the linear damping for this body.
     *
     * @param value  the linear damping for this body.
     */
    public void setLinearDamping(float value) {
        if (body != null) {
            body.setLinearDamping(value);
        } else {
            bodyinfo.linearDamping = value;
        }
    }

    /**
     * Returns the angular damping for this body.
     *
     * @return the angular damping for this body.
     */
    public float getAngularDamping() {
        return (body != null ? body.getAngularDamping() : bodyinfo.angularDamping);
    }

    /**
     * Sets the angular damping for this body.
     *
     * @param value  the angular damping for this body.
     */
    public void setAngularDamping(float value) {
        if (body != null) {
            body.setAngularDamping(value);
        } else {
            bodyinfo.angularDamping = value;
        }
    }

    /**
     * Copies the state from the given body to the body def.
     */
    protected void setBodyState(Body body) {
        bodyinfo.type   = body.getType();
        bodyinfo.angle  = body.getAngle();
        bodyinfo.active = body.isActive();
        bodyinfo.awake  = body.isAwake();
        bodyinfo.bullet = body.isBullet();
        bodyinfo.position.set(body.getPosition());
        bodyinfo.linearVelocity.set(body.getLinearVelocity());
        bodyinfo.allowSleep = body.isSleepingAllowed();
        bodyinfo.fixedRotation = body.isFixedRotation();
        bodyinfo.gravityScale  = body.getGravityScale();
        bodyinfo.angularDamping = body.getAngularDamping();
        bodyinfo.linearDamping  = body.getLinearDamping();
    }

    public void setType(int type){
        this.type = type;
    }

    public int getType(){
        return type;
    }

    /// FixtureDef Methods
    /**
     * Returns the density of this body
     *
     * @return the density of this body
     */
    public float getDensity() {
        return fixture.density;
    }

    /**
     * Sets the density of this body
     *
     * @param value  the density of this body
     */
    public void setDensity(float value) {
        fixture.density = value;
        if (body != null) {
            for(Fixture f : body.getFixtureList()) {
                f.setDensity(value);
            }
        }
    }

    /**
     * Returns the friction coefficient of this body
     *
     * @return the friction coefficient of this body
     */
    public float getFriction() {
        return fixture.friction;
    }

    /**
     * Sets the friction coefficient of this body
     *
     * @param value  the friction coefficient of this body
     */
    public void setFriction(float value) {
        fixture.friction = value;
        if (body != null) {
            for(Fixture f : body.getFixtureList()) {
                f.setFriction(value);
            }
        }
    }

    /**
     * Returns the restitution of this body
     *
     * @return the restitution of this body
     */
    public float getRestitution() {
        return fixture.restitution;
    }

    /**
     * Sets the restitution of this body
     *
     * @param value  the restitution of this body
     */
    public void setRestitution(float value) {
        fixture.restitution = value;
        if (body != null) {
            for(Fixture f : body.getFixtureList()) {
                f.setRestitution(value);
            }
        }
    }

    /**
     * Returns true if this object is a sensor.
     *
     * @return true if this object is a sensor.
     */
    public boolean isSensor() {
        return fixture.isSensor;
    }

    /**
     * Sets whether this object is a sensor.
     *
     * @param value  whether this object is a sensor.
     */
    public void setSensor(boolean value) {
        fixture.isSensor = value;
        if (body != null) {
            for(Fixture f : body.getFixtureList()) {
                f.setSensor(value);
            }
        }
    }

    /**
     * Returns the filter data for this object (or null if there is none)
     *
     * @return the filter data for this object (or null if there is none)
     */
    public Filter getFilterData() {
        return fixture.filter;
    }

    /**
     * Sets the filter data for this object
     *
     * A value of null removes all collision filters.
     *
     * @param value  the filter data for this object
     */
    public void setFilterData(Filter value) {
        if (value !=  null) {
            fixture.filter.categoryBits = value.categoryBits;
            fixture.filter.groupIndex = value.groupIndex;
            fixture.filter.maskBits   = value.maskBits;
        } else {
            fixture.filter.categoryBits = 0x0001;
            fixture.filter.groupIndex = 0;
            fixture.filter.maskBits   = -1;
        }
        if (body != null) {
            for(Fixture f : body.getFixtureList()) {
                f.setFilterData(value);
            }
        }
    }

    // MassData Methods
    /**
     * Returns the center of mass of this body
     *
     * @return the center of mass for this physics body
     */
    public Vector2 getCentroid() {
        return (body != null ? body.getLocalCenter() : centroidCache.set(massdata.center));
    }

    /**
     * Sets the center of mass for this physics body
     *
     * This method does not keep a reference to the parameter.
     *
     * @param value  the center of mass for this physics body
     */
    public void setCentroid(Vector2 value) {
        if (!masseffect) {
            masseffect = true;
            massdata.I = getInertia();
            massdata.mass = getMass();
        }
        massdata.center.set(value);
        if (body != null) {
            body.setMassData(massdata);
        }
    }

    /**
     * Returns the rotational inertia of this body
     *
     * @return the rotational inertia of this body
     */
    public float getInertia() {
        return (body != null ? body.getInertia() : massdata.I);
    }

    /**
     * Sets the rotational inertia of this body
     *
     * @param value  the rotational inertia of this body
     */
    public void setInertia(float value) {
        if (!masseffect) {
            masseffect = true;
            massdata.center.set(getCentroid());
            massdata.mass = getMass();
        }
        massdata.I = value;
        if (body != null) {
            body.setMassData(massdata); // Protected accessor?
        }
    }

    /**
     * Returns the mass of this body
     *
     * @return the mass of this body
     */
    public float getMass() {
        return  (body != null ? body.getMass() : massdata.mass);
    }

    /**
     * Sets the mass of this body
     *
     * @param value  the mass of this body
     */
    public void setMass(float value) {
        if (!masseffect) {
            masseffect = true;
            massdata.center.set(getCentroid());
            massdata.I = getInertia();
        }
        massdata.mass = value;
        if (body != null) {
            body.setMassData(massdata);
        }
    }

    /**
     * Resets this body to use the mass computed from the its shape and density
     */
    public void resetMass() {
        masseffect = false;
        if (body != null) {
            body.resetMassData();
        }
    }

    /**
     * Returns the Box2D body for this object.
     *
     * @return the Box2D body for this object.
     */
    public Body getBody() {
        return body;
    }

    /**
     * Creates the physics Body(s) for this object, adding them to the world.
     *
     * @param world Box2D world to store body
     *
     * @return true if object allocation succeeded
     */
    public boolean activatePhysics(World world) {
        bodyinfo.active = true;
        body = world.createBody(bodyinfo);
        body.setUserData(this);

        // Only initialize if a body was created.
        if (body != null) {
            createFixtures();
            return true;
        }

        bodyinfo.active = false;
        return false;
    }

    /**
     * Destroys the physics Body(s) of this object if applicable,
     * removing them from the world.
     *
     * @param world Box2D world that stores body
     */
    public void deactivatePhysics(World world) {
        if (body != null) {
            setBodyState(body);
            world.destroyBody(body);
            body = null;
            bodyinfo.active = false;
        }
    }

    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     *
     * @param dt Timing values from parent loop
     */
    public void update(float dt) {
        // Recreate the fixture object if dimensions changed.
        if (isDirty()) {
            createFixtures();
        }
    }

    // Garbage Collection Methods
    /**
     * Returns true if our object has been flagged for garbage collection
     *
     * @return true if our object has been flagged for garbage collection
     */
    public boolean isRemoved() {
        return toRemove;
    }

    /**
     * Sets whether our object has been flagged for garbage collection
     *
     * @param value  whether our object has been flagged for garbage collection
     */
    public void markRemoved(boolean value) {
        toRemove = value;
    }

    /**
     * Returns true if the shape information must be updated.
     *
     * @return true if the shape information must be updated.
     */
    public boolean isDirty() {
        return isDirty;
    }

    /**
     * Sets whether the shape information must be updated.
     *
     * @param value  whether the shape information must be updated.
     */
    public void markDirty(boolean value) {
        isDirty = value;
    }

    // DRAWING METHODS
    /**
     * Returns the drawing scale for this physics object
     *
     *
     * @return the drawing scale for this physics object
     */
    public Vector2 getDrawScale() {
        scaleCache.set(drawScale);
        return scaleCache;
    }

    /**
     * Sets the drawing scale for this physics object
     *
     * @param value  the drawing scale for this physics object
     */
    public void setDrawScale(Vector2 value) {
        setDrawScale(value.x,value.y);
    }

    /**
     * Sets the drawing scale for this physics object
     *
     *
     * @param x  the x-axis scale for this physics object
     * @param y  the y-axis scale for this physics object
     */
    public void setDrawScale(float x, float y) {
        drawScale.set(x,y);
    }

    /**
     * Returns the object texture for drawing purposes.
     *
     * @return the object texture for drawing purposes.
     */
    public TextureRegion getTexture() {
        return texture;
    }

    /**
     * Sets the object texture for drawing purposes.
     *
     * @param value  the object texture for drawing purposes.
     */
    public void setTexture(TextureRegion value) {
        texture = value;
        origin.set(texture.getRegionWidth()/2.0f, texture.getRegionHeight()/2.0f);
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(Canvas canvas) {
        if (texture != null) {
            canvas.draw(texture, Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.x,0,1,1);
        }
    }

    // DEBUG METHODS
    /**
     * Returns the physics object tag.
     *
     * A tag is a string attached to an object, in order to identify it in debugging.
     *
     * @return the physics object tag.
     */
    public String getName() {
        return nametag;
    }

    /**
     * Sets the physics object tag.
     *
     * A tag is a string attached to an object, in order to identify it in debugging.
     *
     * @param  value    the physics object tag
     */
    public void setName(String value) {
        nametag = value;
    }

    /**
     * Create a new physics object at the origin.
     */
    protected GameObject() {
        this(0,0);
    }

    /**
     * Create a new physics object
     *
     * @param x Initial x position in world coordinates
     * @param y Initial y position in world coordinates
     */
    protected GameObject(float x, float y) {
        toRemove = false;

        bodyinfo = new BodyDef();
        bodyinfo.awake  = true;
        bodyinfo.allowSleep = true;
        bodyinfo.gravityScale = 1.0f;
        bodyinfo.position.set(x,y);
        bodyinfo.fixedRotation = true;
        bodyinfo.type = BodyType.DynamicBody;

        fixture = new FixtureDef();
        masseffect = false;
        massdata = new MassData();
        drawScale = new Vector2(1,1);

        origin = new Vector2();
        body = null;
    }

    // Abstract Methods
    /**
     * Create new fixtures for this body, defining the shape
     */
    protected abstract void createFixtures();

    /**
     * Release the fixtures for this body, reseting the shape
     */
    protected abstract void releaseFixtures();

    /**
     * Draws the outline of the physics body.
     *
     * @param canvas Drawing context
     */
    public abstract void drawDebug(Canvas canvas);
}
