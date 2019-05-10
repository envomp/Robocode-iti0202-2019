package mrroboto;

import robocode.AdvancedRobot;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.WinEvent;

import java.awt.*;
import java.util.HashSet;

public class MyRobot extends AdvancedRobot {
    private int moveDirection = 1; //which way to move
    private double firePower = 3.0D;

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

    public void onScannedRobot(ScannedRobotEvent e) {
        float sureHitDistance = 9000; //TODO: Calc this
        if (e.getDistance() < sureHitDistance && e.getEnergy() * 1.05 < this.getEnergy() || true) {  // Testing...
            goMeele(e);
        } else {
            goRanged(e);
        }
    }

    private void goMeele(ScannedRobotEvent e) {
        double absBearing = e.getBearingRadians() + getHeadingRadians();//enemies absolute bearing
        double latVel = e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing);//enemies later velocity
        setTurnRadarLeftRadians(getRadarTurnRemainingRadians());//lock on the radar

        double gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing - getGunHeadingRadians()
                + latVel / e.getDistance() * 35);  // TODO: Experiment with this

        //amount to turn our gun, lead just a little bit
        setTurnGunRightRadians(gunTurnAmt);// turn our gun
        setTurnLeft(e.getBearing()); // turn perpendicular to the enemy
        setMaxVelocity(50);
        setAhead(e.getDistance() * moveDirection);// Kamikaze
        fire();
    }

    private void goRanged(ScannedRobotEvent e) {
        //fire();
    }

    private void fire() {
        if (getGunHeat() == 0.0D) {
            setFire(firePower);
        }
    }


    public void onHitWall(HitWallEvent e) {
        moveDirection = -moveDirection;//reverse direction upon hitting a wall
    }

    public void onWin(WinEvent e) {
    }
}
