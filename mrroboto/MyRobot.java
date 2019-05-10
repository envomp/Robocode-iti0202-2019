package mrroboto;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.WinEvent;

import java.awt.*;
import java.util.HashSet;

public class MyRobot extends AdvancedRobot {

    private static double BULLET_POWER = 3;
    private static double BULLET_DAMAGE = BULLET_POWER * 4;//Formula for bullet damage.
    private static double BULLET_SPEED = 20 - 3 * BULLET_POWER;//Formula for bullet speed.
    private final static double MAX_BULLET_SPEED = 17; // ?

    private int moveDirection = 1; //which way to move
    static double oldEnemyHeading;
    private static double enemyEnergy;

    @Override
    public void run() {
        setAdjustRadarForRobotTurn(true);
        setBodyColor(new Color(128, 128, 50));
        setGunColor(new Color(50, 50, 20));
        setRadarColor(new Color(200, 200, 70));
        setScanColor(Color.orange);
        setBulletColor(Color.red);
        setAdjustGunForRobotTurn(true);
        turnRadarRightRadians(Double.NEGATIVE_INFINITY);//keep turning radar left
        scannerScan();
        //System.out.println("run");
    }

    private void scannerScan() {
        setTurnRadarRightRadians(6.283185307179586D);
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        enemyEnergy = e.getEnergy();

        float sureHitDistance = 2000; //TODO: Calc this
        if (e.getDistance() < sureHitDistance
                && e.getEnergy() * (1 + e.getDistance() / 2000) < this.getEnergy()//TODO: Calc how much more dmg we take
                || e.getEnergy() <= 0) {
            goMeele(e);
        } else {
            goRanged(e);
        }
    }

    private void goMeele(ScannedRobotEvent e) {
        setTurnLeft(e.getBearing()); // turn to enemy
        setMaxVelocity(100);
        moveDirection = -1;
        setAhead(e.getDistance() * moveDirection);// Kamikaze
        fire(e);
    }

    private void goRanged(ScannedRobotEvent e) {
        setTurnRight(90 + e.getBearing()); // circle
        setMaxVelocity(100);
        moveDirection *= Math.random() > 0.9 ? -1 : 1;
        setAhead(Math.random() * 100 * moveDirection);// Kamikaze
        fire(e);
    }

    private void fire(ScannedRobotEvent e) {
        double absBearing = e.getBearingRadians() + getHeadingRadians();//enemies absolute bearing
        double latVel = e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing);//enemies later velocity
        setTurnRadarLeftRadians(getRadarTurnRemainingRadians());//lock on the radar

        double gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing - getGunHeadingRadians()
                + latVel / Math.sqrt(e.getDistance()));  // TODO: Experiment with this

        //amount to turn our gun, lead just a little bit
        setTurnGunRightRadians(gunTurnAmt);// turn our gun

        if (getGunHeat() == 0.0D) {
            setFire(BULLET_POWER);
        }
    }

    @Override
    public void onHitWall(HitWallEvent e) {
        moveDirection = -moveDirection;//reverse direction upon hitting a wall
    }

    @Override
    public void onBulletHit(BulletHitEvent e) {
        enemyEnergy -= BULLET_DAMAGE;
    }

    @Override
    public void onWin(WinEvent e) {
    }
}
