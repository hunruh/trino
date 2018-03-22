package tiktaalik.trino.enemy;

import com.badlogic.gdx.math.Vector2;
import tiktaalik.trino.GameObject;
import tiktaalik.trino.enemy.Enemy;
import tiktaalik.util.PooledList;

import java.util.*;

/**
 * InputController corresponding to AI control.
 *
 * REMEMBER: As an implementation of InputController you will have access to
 * the control code constants in that interface.  You will want to use them.
 */
public class AIController {
    /**
     * Enumeration to encode the finite state machine.
     */
    private static enum FSMState {
        /** The ship just spawned */
        SPAWN,
        /** The ship is patrolling around without a target */
        WANDER,
        /** The ship has a target, but must get closer */
        CHASE,
        /** The ship has a target and is attacking it */
        ATTACK
    }

    // Constants for chase algorithms
    /** How close a target must be for us to chase it */
    private static final int CHASE_DIST  = 9;
    /** How close a target must be for us to attack it */
    private static final int ATTACK_DIST = 4;

    // Instance Attributes
    /** The ship being controlled by this AIController */
    private Enemy enemy;
    /** The ship's current state in the FSM */
    private FSMState state;
    /** The target ship (to chase or attack). */
    private GameObject target;
    /** The ship's next action (may include firing). */
    private int move; // A ControlCode
    /** The number of ticks since we started this controller */
    private long ticks;

    // Custom fields for AI algorithms
    //#region ADD YOUR CODE:

    /** Last known location */
    private Vector2 lastKnown;

    private Vector2[] path;
    private int pathStep;

    private float cacheDistance;
    private Vector2 cacheDirection;
    private Vector2 step;

    private float enemySpeed = .025f;
    private float elapsed = 0.01f;

    private int currentOrientation;


    //#endregion

    /**
     * Creates an AIController for the ship with the given id.
     *
     * @param id The unique ship identifier

     */
    public AIController(int id, GameObject duggi,Enemy[] enemies, Vector2[] p) {
        this.enemy = enemies[id];
        //System.out.println("the enemy id is called for AI controller " + id);
        //System.out.println("the first vector for path is " + p[0]);

        state = FSMState.SPAWN;
        ticks = 0;

        // Select an initial target
        target = duggi;
        pathStep = 1;
        path = p;
        step = new Vector2();
    }

    /**
     * Returns the action selected by this InputController
     *
     * The returned int is a bit-vector of more than one possible input
     * option. This is why we do not use an enumeration of Control Codes;
     * Java does not (nicely) provide bitwise operation support for enums.
     *
     * This function tests the environment and uses the FSM to chose the next
     * action of the ship. This function SHOULD NOT need to be modified.  It
     * just contains code that drives the functions that you need to implement.
     *
     * @return the action selected by this InputController
     */
    public int getAction() {
        // Increment the number of ticks.
       // System.out.println("GetAction called for " + enemy.getId());
        ticks++;
        // Do not need to rework ourselves every frame. Just every 10 ticks.
        if ((ticks) % 100 == 0) {


            // Process the FSM
            changeStateIfApplicable();

            // Pathfinding
//            pathStep = (pathStep + 1) % path.length;
//            getMoveAlongPath();
        }

        getMoveAlongPath();

        return move;
    }

    // FSM Code for Targeting (MODIFY ALL THE FOLLOWING METHODS)

    /**
     * Change the state of the ship.
     *
     * A Finite State Machine (FSM) is just a collection of rules that,
     * given a current state, and given certain observations about the
     * environment, chooses a new state. For example, if we are currently
     * in the ATTACK state, we may want to switch to the CHASE state if the
     * target gets out of range.
     */
    private void changeStateIfApplicable() {
        // Add initialization code as necessary
        //#region PUT YOUR CODE HERE

        //#endregion

        // Next state depends on current state.
        switch (state) {
            case SPAWN: // Do not pre-empt with FSMState in a case
                // Insert checks and spawning-to-??? transition code here
                //#region PUT YOUR CODE HERE

                // Switch to Wander state if spawned
                state = FSMState.WANDER;
                //#endregion
                break;

            case WANDER: // Do not pre-empt with FSMState in a case
                // Insert checks and moving-to-??? transition code here
                //#region PUT YOUR CODE HERE

                //#endregion
                break;

            case CHASE: // Do not pre-empt with FSMState in a case
                // insert checks and chasing-to-??? transition code here
                //#region PUT YOUR CODE HERE

                //#endregion
                break;

            case ATTACK: // Do not pre-empt with FSMState in a case
                // insert checks and attacking-to-??? transition code here
                //#region PUT YOUR CODE HERE

                //#endregion
                break;

            default:
                // Unknown or unhandled state, should never get here
                assert (false);
                state = FSMState.WANDER; // If debugging is off
                break;
        }

    }

    /**
     * Returns a movement direction that moves towards a goal tile.
     *
     * This is one of the longest parts of the assignment. Implement
     * breadth-first search (from 2110) to find the best goal tile
     * to move to. However, just return the movement direction for
     * the next step, not the entire path.
     *
     * The value returned should be a control code.  See PlayerController
     * for more information on how to use control codes.
     *
     * @return a movement direction that moves towards a goal tile.
     */
    public void getMoveAlongPath() {
        if (enemy.getStunned())
            return;

        if (path[pathStep].cpy().sub(enemy.getPosition()).len() < 0.2f){
            pathStep = (pathStep + 1) % path.length;
            enemySpeed = 0.025f;
        } else if (path[(pathStep + 1) % path.length].cpy().sub(target.getPosition()).len() < 0.5f){
            pathStep = (pathStep + 1) % path.length;
            enemySpeed = 0.05f;

        } else if (path[(pathStep + 2) % path.length].cpy().sub(target.getPosition()).len() < 0.5f) {
            pathStep = (pathStep + 2) % path.length;
            enemySpeed = 0.05f;

        }

        step = path[pathStep].cpy().sub(enemy.getPosition()).nor().scl(enemySpeed);
        enemy.setPosition(enemy.getX() + step.x, enemy.getY() + step.y);
    }
}
