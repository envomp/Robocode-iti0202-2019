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

    public void run() {
        setAdjustRadarForRobotTurn(true);
        setBodyColor(new Color(128, 128, 50));
        setGunColor(new Color(50, 50, 20));
        setRadarColor(new Color(200, 200, 70));
        setScanColor(Color.orange);
        setBulletColor(Color.red);
        setAdjustGunForRobotTurn(false);
        scannerScan();
        System.out.println("run");
    }

    private void scannerScan() {
        System.out.println("Scanning");
        setTurnRadarRightRadians(6.283185307179586D);
    }

    public void onScannedRobot(ScannedRobotEvent e) {

        if (e.getDistance() > 100) {
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
    }
}
