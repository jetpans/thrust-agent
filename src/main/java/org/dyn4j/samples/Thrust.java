/*
 * Copyright (c) 2010-2022 William Bittle  http://www.dyn4j.org/
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions
 *     and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 *     and the following disclaimer in the documentation and/or other materials provided with the
 *     distribution.
 *   * Neither the name of dyn4j nor the names of its contributors may be used to endorse or
 *     promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.dyn4j.samples;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Line2D;
import java.util.*;

import org.apache.commons.math3.util.Pair;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.TimeStep;
import org.dyn4j.geometry.*;
import org.dyn4j.samples.agents.NNBasketballAgent;
import org.dyn4j.samples.agents.NNThrustAgent;
import org.dyn4j.samples.framework.Camera;
import org.dyn4j.samples.framework.SimulationBody;
import org.dyn4j.samples.framework.SimulationFrame;
import org.dyn4j.samples.framework.input.BooleanStateKeyboardInputHandler;
import org.dyn4j.samples.models.NeuralNetwork;
import org.dyn4j.samples.models.Util;
import org.dyn4j.samples.worlds.BodyDescriptor;
import org.dyn4j.samples.worlds.WorldDescriptor;
import org.dyn4j.world.BroadphaseCollisionData;
import org.dyn4j.world.ManifoldCollisionData;
import org.dyn4j.world.NarrowphaseCollisionData;
import org.dyn4j.world.PhysicsWorld;
import org.dyn4j.world.listener.CollisionListener;
import org.dyn4j.samples.worlds.Worlds;
import org.dyn4j.world.listener.CollisionListenerAdapter;
import org.dyn4j.world.listener.StepListener;
import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;

/**
 * Moderately complex scene of a rocket that has propulsion at various points
 * to allow control.  Control is given by the left, right, up, and down keys
 * and applies forces when pressed.
 *
 * @author William Bittle
 * @version 5.0.1
 * @since 3.2.0
 */
public class Thrust extends SimulationFrame {

    /**
     * Entry point for the example application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        NeuralNetwork test = Util.loadNeuralNetworkFromFile("parameterOptimization/itersChoice/iters=5steps=160/2024-05-31_14-43-23/AlphaThruster.obj13");
        Thrust simulation = new Thrust(test, Worlds.get(2));
        simulation.myRandom = new Random(Math.round(10e8 * Math.random()));
//        simulation.myRandom = new Random(0);

        simulation.tickLimit = 1000000;
        simulation.renderGame = true;
        simulation.verbose = true;
        simulation.run();
        simulation.dispose();
        System.out.println("Finished with score of " + simulation.scoring.collectedPoints);

    }

    private static final long serialVersionUID = 3770932661470247325L;

    NNThrustAgent myAgent = null;
    private SimulationBody ship;
    private SimulationBody point;

    private WorldDescriptor worldDescriptor;

    public Map<String, Double> currentGameState = null;
    public boolean terminateOnPointCollection = false;
    // input control
    public Random myRandom = new Random(3);
    private final BooleanStateKeyboardInputHandler up;
    private final BooleanStateKeyboardInputHandler down;
    private final BooleanStateKeyboardInputHandler left;
    private final BooleanStateKeyboardInputHandler right;

    public double velocityScalar = 1;

    public double angularVelocityScalar = 0.9;


    private class ShipUserData {
        double height;
        double width;

        public ShipUserData(double width, double height) {
            this.height = height;
            this.width = width;
        }
    }

    /**
     * Default constructor.
     */
    public Thrust(NeuralNetwork brain) {
        super("Thrust");
        this.worldDescriptor = Worlds.get(0);
        if (brain != null) {
            this.myAgent = new NNThrustAgent(brain, this.canvas);
        }
        if (brain == null) {
            this.up = new BooleanStateKeyboardInputHandler(this.canvas, KeyEvent.VK_UP);
            this.down = new BooleanStateKeyboardInputHandler(this.canvas, KeyEvent.VK_DOWN);
            this.left = new BooleanStateKeyboardInputHandler(this.canvas, KeyEvent.VK_LEFT);
            this.right = new BooleanStateKeyboardInputHandler(this.canvas, KeyEvent.VK_RIGHT);

        } else {
            this.up = this.myAgent.up;
            this.down = this.myAgent.down;
            this.left = this.myAgent.left;
            this.right = this.myAgent.right;
        }
        this.up.install();
        this.down.install();
        this.left.install();
        this.right.install();
    }

    public Thrust(NeuralNetwork brain, WorldDescriptor world) {
        super("Thrust");
        this.worldDescriptor = world;
        if (brain != null) {
            this.myAgent = new NNThrustAgent(brain, this.canvas);
        }
        if (brain == null) {
            this.up = new BooleanStateKeyboardInputHandler(this.canvas, KeyEvent.VK_UP);
            this.down = new BooleanStateKeyboardInputHandler(this.canvas, KeyEvent.VK_DOWN);
            this.left = new BooleanStateKeyboardInputHandler(this.canvas, KeyEvent.VK_LEFT);
            this.right = new BooleanStateKeyboardInputHandler(this.canvas, KeyEvent.VK_RIGHT);

        } else {
            this.up = this.myAgent.up;
            this.down = this.myAgent.down;
            this.left = this.myAgent.left;
            this.right = this.myAgent.right;
        }
        this.up.install();
        this.down.install();
        this.left.install();
        this.right.install();
    }

    /* (non-Javadoc)
     * @see org.dyn4j.samples.framework.SimulationFrame#initializeCamera(org.dyn4j.samples.framework.Camera)
     */
    @Override
    protected void initializeCamera(Camera camera) {
        super.initializeCamera(camera);
        camera.scale = 64.0;
    }

    /* (non-Javadoc)
     * @see org.dyn4j.samples.framework.SimulationFrame#printControls()
     */
    @Override
    protected void printControls() {
        super.printControls();

        printControl("Thrust Up", "Up", "Use the up key to apply thrust to move up");
        printControl("Thrust Down", "Down", "Use the down key to apply thrust to move down");
        printControl("Thrust Left", "Left", "Use the left key to apply thrust to move left");
        printControl("Thrust Right", "Right", "Use the right key to apply thrust to move right");
    }

    /**
     * Creates game objects and adds them to the world.
     */
    protected void initializeWorld() {
        this.world.setGravity(new Vector2(0, -5));

        // create all your bodies/joints

        // the bounds so we can keep playin
        this.worldDescriptor.getBodyDescriptors().forEach(e -> {
            SimulationBody b = new SimulationBody();
            b.addFixture(Geometry.createRectangle(e.getWidth(), e.getHeight()));
            b.translate(e.getTranslateX(), e.getTranslateY());
            b.setMass(MassType.INFINITE);
            this.world.addBody(b);
        });
        // the ship

        ship = new SimulationBody();
        ship.addFixture(Geometry.createRectangle(0.5, 1.5), 1, 0.2, 0.2);
        BodyFixture bf2 = ship.addFixture(Geometry.createEquilateralTriangle(0.5), 1, 0.2, 0.2);
        bf2.getShape().translate(0, 0.9);
        ship.translate(Util.randomDoubleFromInterval(-3, 3, myRandom.nextDouble()), Util.randomDoubleFromInterval(-3, 3, myRandom.nextDouble()));
        ship.setMass(MassType.NORMAL);
        ship.setUserData(new ShipUserData(0.5, 2));
        ship.setAngularVelocity(Util.randomDoubleFromInterval(-10, 10, myRandom.nextDouble()));
        ship.setLinearVelocity(Util.randomDoubleFromInterval(-10, 10, myRandom.nextDouble()), Util.randomDoubleFromInterval(-10, 10, myRandom.nextDouble()));
        ship.getTransform().setRotation(Util.randomDoubleFromInterval(-Math.PI, Math.PI, myRandom.nextDouble()));

        this.world.addBody(ship);
        this.world.addStepListener(new StepListener<SimulationBody>() {
            @Override
            public void begin(TimeStep timeStep, PhysicsWorld<SimulationBody, ?> physicsWorld) {

            }

            @Override
            public void updatePerformed(TimeStep timeStep, PhysicsWorld<SimulationBody, ?> physicsWorld) {

            }

            @Override
            public void postSolve(TimeStep timeStep, PhysicsWorld<SimulationBody, ?> physicsWorld) {

            }

            @Override
            public void end(TimeStep timeStep, PhysicsWorld<SimulationBody, ?> physicsWorld) {
                if (Thrust.this.myAgent != null && Thrust.super.stepNumber % 3 == 0) {
                    Map<String, Boolean> action = Thrust.this.myAgent.makeAction(Thrust.this.calculateGameState());
                }

                double curX = ship.getTransform().getTranslationX();
                double curY = ship.getTransform().getTranslationY();
                double trashX = point.getWorldCenter().x;
                double trashY = point.getWorldCenter().y;
                double dPath = Math.sqrt(Math.pow(scoring.lastX - curX, 2) + Math.pow(scoring.lastY - curY, 2));
                double dTrash = Math.sqrt(Math.pow(trashX - curX, 2) + Math.pow(trashY - curY, 2));

                if (dTrash < scoring.minUncollectedTrashDistance) {
                    scoring.minUncollectedTrashDistance = dTrash;
                }
                if (currentGameState != null) {
                    double tAngle = Thrust.this.currentGameState.get("trashAngle");
                    if (tAngle > -Math.PI / 48 && tAngle < Math.PI / 48) {
                        scoring.ticksSpentLookingAtTrash++;
                    }
                }

                scoring.pathMade += dPath;
                scoring.lastX = curX;
                scoring.lastY = curY;
            }
        });
        initialisePoint();
    }

    public void initialisePoint() {
        point = new SimulationBody();
        point.addFixture(Geometry.createRectangle(0.5, 0.5));
        Vector2 cords = point.getTransform().getTranslation();
        Transform nextPosition = new Transform();
        nextPosition.setTranslationX(Util.randomDoubleFromInterval(-4, 4, myRandom.nextDouble()));
        nextPosition.setTranslationY(Util.randomDoubleFromInterval(-4, 4, myRandom.nextDouble()));
        point.setTransform(nextPosition);
//        point.translate(4 - myRandom.nextDouble() * 8 - cords.x, 4 - myRandom.nextDouble() - cords.y);
        point.setMass(MassType.INFINITE);
        this.world.addBody(point);

        this.world.addCollisionListener(new CollisionListener<SimulationBody, BodyFixture>() {
            @Override
            public boolean collision(BroadphaseCollisionData<SimulationBody, BodyFixture> broadphaseCollisionData) {
                return true;
            }

            @Override
            public boolean collision(NarrowphaseCollisionData<SimulationBody, BodyFixture> narrowphaseCollisionData) {
                SimulationBody b1 = narrowphaseCollisionData.getBody1();
                SimulationBody b2 = narrowphaseCollisionData.getBody2();
                if (b1 == Thrust.this.ship && b2 == Thrust.this.point) {
                    scoring.minUncollectedTrashDistance = Double.POSITIVE_INFINITY;
                    handlePointCollected();
                    return false;
                }
                if (b1 == Thrust.this.point && b2 == Thrust.this.ship) {
                    scoring.minUncollectedTrashDistance = Double.POSITIVE_INFINITY;
                    handlePointCollected();
                    return false;

                }
                scoring.collisions++;
                return true;
            }

            @Override
            public boolean collision(ManifoldCollisionData<SimulationBody, BodyFixture> manifoldCollisionData) {
                return true;
            }
        });
    }

    public void handlePointCollected() {
        scoring.collectedPoints++;
//        System.out.println("Total points: " + collectedPoints);
        Vector2 cords = point.getTransform().getTranslation();
        Transform nextPosition = new Transform();
        nextPosition.setTranslationX(Util.randomDoubleFromInterval(-4, 4, myRandom.nextDouble()));
        nextPosition.setTranslationY(Util.randomDoubleFromInterval(-4, 4, myRandom.nextDouble()));
        point.setTransform(nextPosition);
        if (terminateOnPointCollection) tickLimit = 0;

//        point.translate(4 - myRandom.nextDouble() * 8 - cords.x, 4 - myRandom.nextDouble() - cords.y);
    }

    /* (non-Javadoc)
     * @see org.dyn4j.samples.framework.SimulationFrame#render(java.awt.Graphics2D, double)
     */
    @Override
    protected void render(Graphics2D g, double elapsedTime) {
        super.render(g, elapsedTime);
        final double scale = this.getCameraScale();
        final double force = 1000 * elapsedTime * velocityScalar;

        final Vector2 r = new Vector2(ship.getTransform().getRotationAngle() + Math.PI * 0.5);
        final Vector2 c = ship.getWorldCenter();

        // apply thrust
        if (this.up.isActive()) {
            Vector2 f = r.product(force);
            Vector2 p = c.sum(r.product(-0.9));

            ship.applyForce(f);

        }
        if (this.down.isActive()) {
            Vector2 f = r.product(-force);
            Vector2 p = c.sum(r.product(0.9));

            ship.applyForce(f);

        }
        if (this.left.isActive()) {
            Vector2 f1 = r.product(force * 0.1 * angularVelocityScalar).right();
            Vector2 f2 = r.product(force * 0.1 * angularVelocityScalar).left();
            Vector2 p1 = c.sum(r.product(0.9));
            Vector2 p2 = c.sum(r.product(-0.9));

            // apply a force to the top going left
            ship.applyForce(f1, p1);
            // apply a force to the bottom going right
            ship.applyForce(f2, p2);


        }
        if (this.right.isActive()) {
            Vector2 f1 = r.product(force * 0.1 * angularVelocityScalar).left();
            Vector2 f2 = r.product(force * 0.1 * angularVelocityScalar).right();
            Vector2 p1 = c.sum(r.product(0.9));
            Vector2 p2 = c.sum(r.product(-0.9));

            // apply a force to the top going left
            ship.applyForce(f1, p1);
            // apply a force to the bottom going right
            ship.applyForce(f2, p2);
        }


    }


    public Map<String, Double> calculateGameState() {

        Map<String, Double> myState = new HashMap<>();
        SimulationBody myShip = this.ship;
        double shipX = this.ship.getWorldCenter().x;
        double shipY = this.ship.getWorldCenter().y;
        ShipUserData shipData = (ShipUserData) this.ship.getUserData();
        double shipH = shipData.height;
        double shipW = shipData.width;
        double trashX = this.point.getWorldCenter().x;
        double trashY = this.point.getWorldCenter().y;


        double dN = 10e8;
        double dE = 10e8;
        double dS = 10e8;
        double dW = 10e8;

        double distanceToTrash = Math.sqrt(Math.pow(shipX - trashX, 2) + Math.pow(shipY - trashY, 2));

        double velocityLinear = this.ship.getLinearVelocity().getMagnitude();
        double velocityLinearAngle = Util.normalizeAngle(this.ship.getLinearVelocity().getDirection() - Math.PI / 2);

        double velocityAngular = this.ship.getAngularVelocity();

        double orientation = this.ship.getTransform().getRotationAngle();


        double trashAngle = Util.normalizeAngle(Math.atan2(trashY - shipY, trashX - shipX) - orientation - Math.PI / 2);
        double canSeeTrash = 1;

        LineIntersector myIntersector = new RobustLineIntersector();
        Coordinate shipCord = new Coordinate(shipX, shipY);
        double forward = Util.normalizeAngle(orientation + Math.PI / 2);
        double left = Util.normalizeAngle(forward + Math.PI / 2);
        double back = Util.normalizeAngle(forward - Math.PI);
        double right = Util.normalizeAngle(forward - Math.PI / 2);
        Coordinate shipForward = Util.getDistantCoordinate(shipX, shipY, forward);
        Coordinate shipBackward = Util.getDistantCoordinate(shipX, shipY, back);
        Coordinate shipLeft = Util.getDistantCoordinate(shipX, shipY, left);
        Coordinate shipRight = Util.getDistantCoordinate(shipX, shipY, right);
        for (BodyDescriptor b : this.worldDescriptor.getBodyDescriptors()) {
            double h = b.getHeight();
            double w = b.getWidth();
            double x = b.getTranslateX();
            double y = b.getTranslateY();

            double[] bX = {x - w / 2, x + w / 2};
            double[] bY = {y + h / 2, y - h / 2};

            if (canSeeTrash == 1) {
                myIntersector.computeIntersection(new Coordinate(shipX, shipY), new Coordinate(trashX, trashY), new Coordinate(bX[0], bY[0]), new Coordinate(bX[1], bY[1]));
                if (myIntersector.hasIntersection()) {
                    canSeeTrash = 0;
                }
            }
            List<Pair<Coordinate, Coordinate>> lines = new ArrayList<>();
            lines.add(new Pair<>(new Coordinate(bX[0], bY[0]), new Coordinate(bX[1], bY[0])));
            lines.add(new Pair<>(new Coordinate(bX[0], bY[1]), new Coordinate(bX[1], bY[1])));


            lines.add(new Pair<>(new Coordinate(bX[0], bY[0]), new Coordinate(bX[0], bY[1])));

            lines.add(new Pair<>(new Coordinate(bX[1], bY[0]), new Coordinate(bX[1], bY[1])));

            for (Pair<Coordinate, Coordinate> line : lines) {

                Coordinate line1 = line.getFirst();
                Coordinate line2 = line.getSecond();

                double dForward = Util.getDistanceToIntersection(shipCord, shipForward, line1, line2, myIntersector);
                double dBackward = Util.getDistanceToIntersection(shipCord, shipBackward, line1, line2, myIntersector);
                double dLeft = Util.getDistanceToIntersection(shipCord, shipLeft, line1, line2, myIntersector);
                double dRight = Util.getDistanceToIntersection(shipCord, shipRight, line1, line2, myIntersector);
                if (dForward < dN) {
                    dN = dForward;
                }
                if (dBackward < dS) {
                    dS = dBackward;
                }

                if (dLeft < dW) {
                    dW = dLeft;
                }

                if (dRight < dE) {
                    dE = dRight;
                }

            }
        }


        myState.put("dFront", dN - shipH / 2);
        myState.put("dBack", dS - shipH / 2);
        myState.put("dLeft", dW - shipW / 2);
        myState.put("dRight", dE - shipW / 2);

        myState.put("lVelocity", velocityLinear);
        myState.put("lVelocityAngle", velocityLinearAngle);
        myState.put("aVelocity", velocityAngular);
        myState.put("orientation", orientation);
        myState.put("dTrash", distanceToTrash);
        myState.put("canSeeTrash", canSeeTrash);
        myState.put("trashAngle", trashAngle);

        this.currentGameState = myState;
        return myState;
    }


    public ThrustScoring scoring = new ThrustScoring();

    public class ThrustScoring {
        public double minUncollectedTrashDistance = 1e8;
        public int ticksSpentLookingAtTrash = 0;
        public double pathMade = 0;
        public double lastX;
        public double lastY;

        public int collectedPoints = 0;

        public int collisions;

    }
}
