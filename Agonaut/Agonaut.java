package Agonaut;


import robocode.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Random;

public class Agonaut extends AdvancedRobot {
    private final Random random = new Random();

    private static double firePower = 1.9D;
    private static double lateralDirection = 1.0D;
    private static double lastEnemySpeed = 0.0D;
    private static double enemyEnergy = 100.0D;

    private double bulletPower;
    private double lateralVelocity;
    private double absBearing;
    private double enemyDistance;

    private static int velocity;

    private static int[][] hits = new int[16][4];
    private SingleEnemy enemy = new SingleEnemy();

    private int moveDirection = 1;
    private byte radarDirection = 1;
    private double moveAmount;

    private int currCondition;
    private double width;
    private double height;

    private static double[] movePredictions = new double[53];

    private Point2D.Double myLocation;
    private Point2D.Double enemyLocation;
    private ArrayList<EnemyMovePrediction> enemyMovePrediction;
    private ArrayList<Integer> surfDirections;
    private ArrayList<Double> surfAbsBearings;

    private static java.awt.geom.Rectangle2D.Double fieldRectangle;

    private double previousEnemyHeading;
    private double enemyHeading;
    private double enemyHeadingChange;
    private double enemyVelocity;

    public Agonaut() {
    }

    public void run() {
        this.setAdjustRadarForGunTurn(true);
        this.setAdjustGunForRobotTurn(true);
        this.setAdjustRadarForRobotTurn(true);
        this.width = this.getBattleFieldWidth();
        this.height = this.getBattleFieldHeight();
        this.moveAmount = Math.max(this.width, this.height) - 100.0D;
        this.enemyMovePrediction = new ArrayList<EnemyMovePrediction>();
        this.surfDirections = new ArrayList<>();
        this.surfAbsBearings = new ArrayList<>();
        fieldRectangle = new java.awt.geom.Rectangle2D.Double(36.0D, 36.0D, this.width - 36.0D, this.height - 36.0D);
        if (this.getOthers() > 2) {
            this.setTurnLeft(this.getHeading() % 90.0D);
            this.currCondition = 1;
        }

        while (true) {
            if (this.getOthers() > 2) {
                this.doMeleeMovement();
            }

            this.doScanner();

            try {
                this.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }

            this.setColors(
                    new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)),
                    new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)),
                    new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
        }
    }

    private void doMeleeMovement() {
        if (this.getTime() % (long) ((int) (Math.random() * 100.0D + 1.0D)) == 0L) {
            this.moveDirection *= -1;
        }

        this.setMaxVelocity(8.0D);
        switch (this.currCondition) {
            case 1:
                if (this.getTurnRemaining() == 0.0D) {
                    this.setBack(this.moveAmount * (double) this.moveDirection);
                    this.currCondition = 2;
                }
                break;
            case 2:
                if (this.getDistanceRemaining() == 0.0D) {
                    this.setTurnLeft((double) (90 * this.moveDirection));
                    this.currCondition = 4;
                }
                break;
            case 3:
                if (this.getDistanceRemaining() == 0.0D) {
                    this.setTurnLeft(90.0D);
                    this.currCondition = 4;
                }

                this.currCondition = 4;
                break;
            case 4:
                if (this.getTurnRemaining() == 0.0D) {
                    this.setBack(Math.random() * this.moveAmount * (double) this.moveDirection);
                    this.currCondition = 3;
                }
        }

    }

    public void onScannedRobot(ScannedRobotEvent e) {
        if (this.enemy.none() || e.getDistance() < this.enemy.getRadius() - 70.0D || e.getName().equals(this.enemy.getName())) {
            this.enemy.update(e, this);
        }

        if (e.getDistance() < 72.0D) {
            firePower = 3.0D;
        } else {
            firePower = 1.9D;
        }

        this.enemyVelocity = e.getVelocity();
        this.myLocation = new Point2D.Double(this.getX(), this.getY());
        this.lateralVelocity = this.getVelocity() * Math.sin(e.getBearingRadians());
        this.absBearing = e.getBearingRadians() + this.getHeadingRadians();
        this.bulletPower = enemyEnergy - e.getEnergy();
        this.enemyDistance = e.getDistance();
        enemyEnergy = e.getEnergy();
        this.enemyHeading = e.getHeadingRadians();
        this.enemyHeadingChange = this.enemyHeading - this.previousEnemyHeading;
        this.previousEnemyHeading = this.enemyHeading;
        if (this.getOthers() <= 2) {
            this.movePredicting();
            this.GFTarget();
        } else if (this.getOthers() > 2) {
            this.setTurnGunRightRadians(RobotFormulas.normalize(this.circularTargetAngle() - this.getGunHeadingRadians()));
            if (this.getGunHeat() == 0.0D) {
                this.setFire(firePower);
            }
        }

        this.velocityChanger(this);
    }

    private void velocityChanger(AdvancedRobot r) {
        if (enemyEnergy > (enemyEnergy = this.enemy.getEnergy())) {
            this.moveDirection *= -1;
            this.setMaxVelocity((double) Math.min((int) (this.getVelocity() * this.enemy.getSpeed() / (double) velocity), 8));
            int lastVel = velocity + 4 * (int) (this.enemy.getRadius() / 276.0D);
            byte testVel = 3;

            if (hits[lastVel][testVel] < hits[lastVel][velocity]) {
                velocity = testVel;
            }

            if (velocity <= 2) {
                r.onHitWall(null);
            }
        }

    }

    private void GFTarget() {
        MovePrediction movePrediction = new MovePrediction(this);
        if (this.enemyVelocity != 0.0D) {
            lateralDirection = (double) RobotFormulas.sign(
                    this.enemyVelocity * Math.sin(this.enemy.getHeading() - this.absBearing));
        }

        movePrediction.gunLocation = new Point2D.Double(this.getX(), this.getY());
        MovePrediction.targetLocation = RobotFormulas.project(movePrediction.gunLocation, this.absBearing, this.enemyDistance);
        movePrediction.lateralDirection = lateralDirection;
        movePrediction.bulletPower = firePower;
        movePrediction.setSegmentations(this.enemyDistance, this.enemyVelocity, lastEnemySpeed);
        lastEnemySpeed = this.enemyVelocity;
        movePrediction.bearing = this.absBearing;
        this.setTurnGunRightRadians(
                RobotFormulas.normalize(this.absBearing - this.getGunHeadingRadians() + movePrediction.mostVisitedBearingOffset()));
        if (this.getGunHeat() == 0.0D) {
            this.setFire(movePrediction.bulletPower);
        }

        if (this.getEnergy() >= firePower) {
            this.addCustomEvent(movePrediction);
        }

    }

    private void doScanner() {
        if (this.getOthers() <= 2) {
            if (this.enemy.none()) {
                this.setTurnRadarRightRadians(Math.PI * 2);
            } else {
                double turn = this.getHeadingRadians() - this.getRadarHeadingRadians() + this.enemy.getBearing();
                turn += (Math.PI / 18) * (double) this.radarDirection;
                this.setTurnRadarRightRadians(RobotFormulas.normalize(turn));
                this.radarDirection *= -1;
            }
        } else if (this.getOthers() > 2) {
            this.setTurnRadarRightRadians(1.0D);
        }

    }

    private double circularTargetAngle() {
        double futureX = this.enemy.getLat();
        double futureY = this.enemy.getLon();

        for (double time = 0.0D; (time + 1.0D) * RobotFormulas.bulletSpeed(firePower) < Point2D.distance(this.getX(), this.getY(), futureX, futureY); ++time) {
            futureX += Math.sin(this.enemyHeading) * this.enemyVelocity;
            futureY += Math.cos(this.enemyHeading) * this.enemyVelocity;
            this.enemyHeading += this.enemyHeadingChange;
            if (futureX < 36.0D || futureY < 36.0D || futureX > this.width - 36.0D || futureY > this.height - 36.0D) {
                futureX = Math.min(Math.max(36.0D, futureX), this.width - 36.0D);
                futureY = Math.min(Math.max(36.0D, futureY), this.height - 36.0D);
                break;
            }
        }

        return RobotFormulas.normalize(Math.atan2(futureX - this.getX(), futureY - this.getY()));
    }

    private void movePredicting() {
        EnemyMovePrediction movePrediction = new EnemyMovePrediction();
        if (this.bulletPower < 3.01D && this.bulletPower > 0.09D && this.surfDirections.size() > 2) {
            movePrediction.fireTime = this.getTime() - 1L;
            movePrediction.bulletVelocity = RobotFormulas.bulletSpeed(this.bulletPower);
            movePrediction.distanceTraveled = RobotFormulas.bulletSpeed(this.bulletPower);
            movePrediction.direction = this.surfDirections.get(2);
            movePrediction.directAngle = this.surfAbsBearings.get(2);
            movePrediction.fireLocation = (Point2D.Double) this.enemyLocation.clone();
            this.enemyMovePrediction.add(movePrediction);
        }

        this.setTurnRadarRightRadians(RobotFormulas.normalize(this.absBearing - this.getRadarHeadingRadians()) * 2.0D);
        this.surfDirections.add(0, this.lateralVelocity >= 0.0D ? 1 : -1);
        this.surfAbsBearings.add(0, this.absBearing + Math.PI);
        this.enemyLocation = RobotFormulas.project(this.myLocation, this.absBearing, this.enemyDistance);
        this.updatePredictions();
        this.surf();
    }

    public void onRobotDeath(RobotDeathEvent e) {
        if (e.getName().equals(this.enemy.getName())) {
            this.enemy.reset();
        }

    }

    public void onHitWall(HitWallEvent e) {
        this.moveDirection *= -1;
    }


    public void onHitByBullet(HitByBulletEvent e) {
        if (this.getOthers() <= 2) {
            if (!this.enemyMovePrediction.isEmpty()) {
                Point2D.Double hitBulletLocation = new Point2D.Double(e.getBullet().getX(), e.getBullet().getY());
                EnemyMovePrediction prediction = null;

                for (EnemyMovePrediction movePrediction : this.enemyMovePrediction) {
                    if (Math.abs(
                            movePrediction.distanceTraveled - this.myLocation.distance(movePrediction.fireLocation))
                            < 50.0D && Math.round(RobotFormulas.bulletSpeed(e.getBullet().getPower()) * 10.0D)
                            == Math.round(movePrediction.bulletVelocity * 10.0D)) {
                        prediction = movePrediction;
                        break;
                    }
                }

                if (prediction != null) {
                    this.logHit(prediction, hitBulletLocation);
                    this.enemyMovePrediction.remove(this.enemyMovePrediction.lastIndexOf(prediction));
                }
            }
        } else if (this.getOthers() > 2) {
            this.moveDirection *= -1;
        }

    }

    public void onBulletHit(BulletHitEvent e) {
        enemyEnergy = e.getEnergy();
    }

    public void onHitRobot(HitRobotEvent e) {
        this.moveDirection *= -1;
        this.currCondition = 1;
    }

    private void updatePredictions() {
        for (int x = 0; x < this.enemyMovePrediction.size(); ++x) {
            EnemyMovePrediction ew = this.enemyMovePrediction.get(x);
            ew.distanceTraveled = (double) (this.getTime() - ew.fireTime) * ew.bulletVelocity;
            if (ew.distanceTraveled > this.myLocation.distance(ew.fireLocation) + 50.0D) {
                this.enemyMovePrediction.remove(x);
                --x;
            }
        }

    }

    private EnemyMovePrediction getClosestMovePrediction() {
        double closestDistance = 50000.0D;
        EnemyMovePrediction prediction = null;

        for (EnemyMovePrediction movePrediction : this.enemyMovePrediction) {
            double distance = this.myLocation.distance(movePrediction.fireLocation) - movePrediction.distanceTraveled;
            if (distance > movePrediction.bulletVelocity && distance < closestDistance) {
                prediction = movePrediction;
                closestDistance = distance;
            }
        }

        return prediction;
    }

    private static int getFactorIndex(EnemyMovePrediction ew, Point2D.Double targetLocation) {
        return (int) RobotFormulas.limit(0.0D, RobotFormulas.normalize(
                RobotFormulas.absBearing(ew.fireLocation, targetLocation) - ew.directAngle)
                        / RobotFormulas.maxEscapeAngle(ew.bulletVelocity) * (double) ew.direction
                        * (double) ((movePredictions.length - 1) / 2) + (double) ((movePredictions.length - 1) / 2),
                (double) (movePredictions.length - 1));
    }

    private void logHit(EnemyMovePrediction ew, Point2D.Double targetLocation) {
        int index = getFactorIndex(ew, targetLocation);

        for (int x = 0; x < movePredictions.length; ++x) {
            double[] predictionVariable = movePredictions;
            predictionVariable[x] += 1.0D / (Math.pow((double) (index - x), 2.0D) + 1.0D);
        }
    }

    private Point2D.Double predictPosition(EnemyMovePrediction prediction, int direction) {
        Point2D.Double predictedPosition = (Point2D.Double) this.myLocation.clone();
        double predictedVelocity = this.getVelocity();
        double predictedHeading = this.getHeadingRadians();
        int counter = 0;
        boolean intercepted = false;

        do {
            double moveAngle = this.avoidWall(predictedPosition,
                    RobotFormulas.absBearing(prediction.fireLocation, predictedPosition)
                            + (double) direction * (Math.PI / 2), direction) - predictedHeading;
            double moveDir = 1.0D;
            if (Math.cos(moveAngle) < 0.0D) {
                moveAngle += Math.PI;
                moveDir = -1.0D;
            }

            moveAngle = RobotFormulas.normalize(moveAngle);
            double maxTurning = (Math.PI / 720) * (40.0D - 3.0D * Math.abs(predictedVelocity));
            predictedHeading = RobotFormulas.normalize(predictedHeading + RobotFormulas.limit(-maxTurning, moveAngle, maxTurning));
            predictedVelocity += predictedVelocity * moveDir < 0.0D ? 2.0D * moveDir : moveDir;
            predictedVelocity = RobotFormulas.limit(-8.0D, predictedVelocity, 8.0D);
            predictedPosition = RobotFormulas.project(predictedPosition, predictedHeading, predictedVelocity);
            ++counter;
            if (predictedPosition.distance(prediction.fireLocation) < prediction.distanceTraveled
                    + (double) counter * prediction.bulletVelocity + prediction.bulletVelocity) {
                intercepted = true;
            }
        } while (!intercepted && counter < 500);

        return predictedPosition;
    }

    private double lookForDanger(EnemyMovePrediction prediction, int direction) {
        int index = getFactorIndex(prediction, this.predictPosition(prediction, direction));
        return movePredictions[index];
    }

    private void surf() {
        EnemyMovePrediction prediction = this.getClosestMovePrediction();
        if (prediction != null) {
            double dangerLeft = this.lookForDanger(prediction, -1);
            double dangerRight = this.lookForDanger(prediction, 1);
            double goAngle = RobotFormulas.absBearing(prediction.fireLocation, this.myLocation);
            if (dangerLeft < dangerRight) {
                goAngle = this.avoidWall(this.myLocation, goAngle - (Math.PI / 2), -1);
            } else {
                goAngle = this.avoidWall(this.myLocation, goAngle + (Math.PI / 2), 1);
            }
            dodgeAccordingToAngle(this, goAngle);
        }
    }

    private double avoidWall(Point2D.Double botLocation, double angle, int orientation) {
        while (!fieldRectangle.contains(RobotFormulas.project(botLocation, angle, 160.0D))) {
            angle += (double) orientation * 0.05D;
        }
        return angle;
    }

    private static void dodgeAccordingToAngle(AdvancedRobot r, double goAngle) {
        double angle = RobotFormulas.normalize(goAngle - r.getHeadingRadians());
        if (Math.abs(angle) > Math.PI / 2) {
            if (angle < 0.0D) {
                r.setTurnRightRadians(Math.PI + angle);
            } else {
                r.setTurnLeftRadians(Math.PI - angle);
            }
            r.setBack(100.0D);
        } else {
            if (angle < 0.0D) {
                r.setTurnLeftRadians(-1.0D * angle);
            } else {
                r.setTurnRightRadians(angle);
            }
            r.setAhead(100.0D);
        }
    }
}
