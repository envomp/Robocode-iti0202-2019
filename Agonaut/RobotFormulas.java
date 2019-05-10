package Agonaut;

import java.awt.geom.Point2D;

public class RobotFormulas {

    static double bulletSpeed(double power) {
        return 20.0 - (3.0 * power);
    }

    static Point2D.Double project(Point2D sourceLocation, double angle, double length) {
        return new Point2D.Double(sourceLocation.getX() + Math.sin(angle) * length, sourceLocation.getY() + Math.cos(angle) * length);
    }

    static double absBearing(Point2D source, Point2D target) {
        return Math.atan2(target.getX() - source.getX(), target.getY() - source.getY());
    }

    static int sign(double v) {
        return v < 0.0D ? -1 : 1;
    }

    static int minMax(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    static double limit(double min, double value, double max) {
        return Math.max(min, Math.min(value, max));
    }

    static double maxEscapeAngle(double velocity) {
        return Math.asin(8.0 / velocity);
    }

    static double normalize(double angle) {
        if (angle > -Math.PI && angle <= Math.PI) {
            return angle;
        } else {
            while (angle <= Math.PI) {
                angle += 2 * Math.PI;
            }
            while (angle > Math.PI) {
                angle -= 2 * Math.PI;
            }
            return angle;
        }
    }
}
