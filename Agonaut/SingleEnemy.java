import robocode.Robot;
import robocode.ScannedRobotEvent;

class SingleEnemy {
  private String name;

  private double bearing;
  private double energy;
  private double heading;
  private double lat;
  private double lon;
  private double radius;
  private double speed;

  SingleEnemy() {
    this.reset();
  }

  void reset() {
    this.bearing = 0.0D;
    this.radius = 0.0D;
    this.energy = 0.0D;
    this.heading = 0.0D;
    this.speed = 0.0D;
    this.name = "";
    this.lat = 0.0D;
    this.lon = 0.0D;
  }

  void update(ScannedRobotEvent e, Robot robot) {
    double bearingDegree = robot.getHeading() + e.getBearing();
    if (bearingDegree < 0.0D) {
      bearingDegree += 360.0D;
    }
    this.name = e.getName();

    this.bearing = e.getBearingRadians();
    this.energy = e.getEnergy();
    this.heading = e.getHeadingRadians();
    this.lat = robot.getX() + Math.sin(Math.toRadians(bearingDegree)) * e.getDistance();
    this.lon = robot.getY() + Math.cos(Math.toRadians(bearingDegree)) * e.getDistance();
    this.radius = e.getDistance();
    this.speed = e.getVelocity();
  }

  boolean none() {
    return this.name.length() == 0;
  }

  String getName() {
    return name;
  }

  double getBearing() {
    return bearing;
  }

  double getEnergy() {
    return energy;
  }

  double getHeading() {
    return heading;
  }

  double getLat() {
    return lat;
  }

  double getLon() {
    return lon;
  }

  double getRadius() {
    return radius;
  }

  double getSpeed() {
    return speed;
  }
}
