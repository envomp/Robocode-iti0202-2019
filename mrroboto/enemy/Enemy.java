package mrroboto.enemy;

import robocode.ScannedRobotEvent;

import java.util.Objects;

public class Enemy {

    private String name;
    private double distance;
    private double energy;
    private double bearingRadians;
    private double headingRadians;
    private double velocity;
    private boolean isSentry;


    public Enemy(ScannedRobotEvent e) {
        name = e.getName();
        distance = e.getDistance();
        energy = e.getEnergy();
        bearingRadians = e.getBearingRadians();
        headingRadians = e.getHeadingRadians();
        velocity = e.getVelocity();
        isSentry = e.isSentryRobot();
    }

    public String getName() {
        return name;
    }

    public double getDistance() {
        return distance;
    }

    public double getEnergy() {
        return energy;
    }

    public double getBearingRadians() {
        return bearingRadians;
    }

    public double getHeadingRadians() {
        return headingRadians;
    }

    public double getVelocity() {
        return velocity;
    }

    public boolean isSentry() {
        return isSentry;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Enemy enemy = (Enemy) o;
        return isSentry == enemy.isSentry &&
                name.equals(enemy.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, isSentry);
    }
}
