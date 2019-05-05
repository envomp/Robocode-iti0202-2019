package mrroboto;

import robocode.AdvancedRobot;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.WinEvent;

import java.awt.*;
import java.awt.geom.Point2D;

public class MyRobot extends AdvancedRobot {
    private int moveDirection = 1;//which way to move
    private double firePower = 3.0D;
    private boolean win = false;
    private double enemyVelocity = 0;
    private Point2D.Double myLocation = new Point2D.Double(0, 0);
    private double lateralVelocity = 0;
    private double absBearing = 0;
    private double bulletPower = 0;
    private double enemyEnergy = 0;
    private double enemyDistance = 0;
    private double enemyHeading = 0;
    private double enemyHeadingChange = 0;
    private double oldEnemyHeading = 0;

    public void run() {
        setAdjustRadarForRobotTurn(true);
        setBodyColor(new Color(128, 128, 50));
        setGunColor(new Color(50, 50, 20));
        setRadarColor(new Color(200, 200, 70));
        setScanColor(Color.orange);
        setBulletColor(Color.red);
        setAdjustGunForRobotTurn(false);
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        setTurnRadarLeftRadians(getRadarTurnRemainingRadians());//lock on the radar
        enemyVelocity = e.getVelocity();
        myLocation = new Point2D.Double(getX(), getY());
        lateralVelocity = (getVelocity() * Math.sin(e.getBearingRadians()));
        absBearing = (e.getBearingRadians() + getHeadingRadians());
        bulletPower = (enemyEnergy - e.getEnergy());
        enemyDistance = e.getDistance();
        enemyEnergy = e.getEnergy();
        enemyHeading = e.getHeadingRadians();
        enemyHeadingChange = (enemyHeading - oldEnemyHeading);
        oldEnemyHeading = enemyHeading;

        if (e.getDistance() > 150) {
            this.goRanged(e);
        } else {
            this.goMeele(e);
        }
    }

    private void goMeele(ScannedRobotEvent e) {
        fire();
    }

    private void goRanged(ScannedRobotEvent e) {
        fire();
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
        win = true;
    }
}
