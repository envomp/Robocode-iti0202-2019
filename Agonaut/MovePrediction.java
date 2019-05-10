import java.awt.geom.Point2D;

import robocode.AdvancedRobot;
import robocode.Condition;

class MovePrediction extends Condition {
  static Point2D targetLocation;
  double bulletPower;
  Point2D gunLocation;
  double bearing;
  double lateralDirection;
  private static int[][][][] statBuffers = new int[5][5][5][25];
  private int[] buffer;
  private AdvancedRobot robot;
  private double distanceTraveled;

  MovePrediction(AdvancedRobot r) {
    this.robot = r;
  }

  public boolean test() {
    this.advance();
    if (this.hasArrived()) {
      this.buffer[this.currentBin()]++;
      this.robot.removeCustomEvent(this);
    }
    return false;
  }

  double mostVisitedBearingOffset() {
    return this.lateralDirection * 0.05833333333333333D * (double) (this.mostVisitedBin() - 12);
  }

  void setSegmentations(double distance, double velocity, double lastVelocity) {
    int distanceIndex = Math.min(4, (int) (distance / 200.0D));
    int velocityIndex = (int) Math.abs(velocity / 2.0D);
    int lastVelocityIndex = (int) Math.abs(lastVelocity / 2.0D);
    this.buffer = statBuffers[distanceIndex][velocityIndex][lastVelocityIndex];
  }

  private void advance() {
    this.distanceTraveled += RobotFormulas.bulletSpeed(this.bulletPower);
  }

  private boolean hasArrived() {
    return this.distanceTraveled > this.gunLocation.distance(targetLocation) - 18.0D;
  }

  private int currentBin() {
    int bin =
        (int)
            Math.round(
                RobotFormulas.normalize(
                            RobotFormulas.absBearing(this.gunLocation, targetLocation)
                                - this.bearing)
                        / (this.lateralDirection * 0.05833333333333333D)
                    + 12.0D);
    return RobotFormulas.minMax(bin, 0, 24);
  }

  private int mostVisitedBin() {
    int mostVisited = 12;

    for (int i = 0; i < 25; ++i) {
      if (this.buffer[i] > this.buffer[mostVisited]) {
        mostVisited = i;
      }
    }

    return mostVisited;
  }
}
