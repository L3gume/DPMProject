package ca.mcgill.ecse211.finalproject;

import lejos.hardware.motor.EV3LargeRegulatedMotor;



public class Odometer extends Thread {

  // --------------------------------------------------------------------------------
  // Constants
  // --------------------------------------------------------------------------------

  private static final long SLEEP_TIME = 20;

  private static final double THETA_MIN = 0.0;
  private static final double THETA_MAX = 2.0 * Math.PI;


  // --------------------------------------------------------------------------------
  // Variables
  // --------------------------------------------------------------------------------
  
  private Object lock;

  private EV3LargeRegulatedMotor leftMotor;
  private EV3LargeRegulatedMotor rightMotor;
  private double wheelRadius;
  private double track;
  
  private double x;
  private double y;
  private double theta;


  /**
   * Constructor
   * @param leftMotor TODO
   * @param rightMotor TODO
   * @param wheelRadius TODO
   * @param track TODO
   */
  public Odometer(
      EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
      double wheelRadius, double track
      ) {
    // Initalize this thread's lock object.
    this.lock = new Object();

    this.leftMotor = leftMotor;
    this.rightMotor = rightMotor;
    this.wheelRadius = wheelRadius;
    this.track = track;
  }


  /**
   * TODO
   */
  public void run() {
    long start;
    long end;

    int lastTachoL;
    int lastTachoR;
    int currentTachoL;
    int currentTachoR;

    double distL;
    double distR;

    double deltaT; // delta theta (in degrees)
    double deltaD; // delta distance (i.e. the displacement)
    double deltaX; // delta X distance (i.e. the displacement w/ respect to X)
    double deltaY; // delta Y distance (i.e. the displacement w/ respect to Y)

    double tmpTheta;
    
    lastTachoL = this.leftMotor.getTachoCount();
    lastTachoR = this.rightMotor.getTachoCount();

    while (true) {
      start = System.currentTimeMillis();

      // Get the next tacho counts and update the last tacho counts.
      currentTachoL = this.leftMotor.getTachoCount();
      currentTachoR = this.rightMotor.getTachoCount();
      distL = Math.PI * this.wheelRadius * (currentTachoL - lastTachoL) / 180.0;
      distR = Math.PI * this.wheelRadius * (currentTachoR - lastTachoR) / 180.0;
      lastTachoL = currentTachoL;
      lastTachoR = currentTachoR;

      // Get the current change in position.
      deltaD = (distL + distR) * 0.5;

      // Get the current change in orientation.
      deltaT = (distL - distR) / FinalProject.TRACK;

      synchronized (this.lock) {
        tmpTheta = this.theta + deltaT;

        // Ensure that the new theta is between 0 and 2*pi radians.
        while (tmpTheta > Odometer.THETA_MAX) {
          tmpTheta -= Odometer.THETA_MAX;
        }
        while (tmpTheta < Odometer.THETA_MIN) {
          tmpTheta += Odometer.THETA_MAX;
        }

        deltaX = deltaD * Math.sin(tmpTheta);
        deltaY = deltaD * Math.sin(tmpTheta);

        // Update the current values of x, y, and theta.
        this.theta = tmpTheta;
        this.x += deltaX;
        this.y += deltaY;
      }

      end = System.currentTimeMillis();

      // Sleep for a little bit if the time elapsed is less than the sleep time.
      if ((end - start) < Odometer.SLEEP_TIME) {
        try {
          Thread.sleep(Odometer.SLEEP_TIME - (end - start));
        } catch (Exception e) {
          // ...
        }
      }
    }

    // Unreachable
  }


  /**
   * TODO
   */
  public double getX() {
    double x;
    synchronized (this.lock) {
      x = this.x;
    }
    return x;
  }

  /**
   * TODO
   */
  public double getY() {
    double y;
    synchronized (this.lock) {
      y = this.y;
    }
    return y;
  }

  /**
   * TODO
   */
  public double getTheta() {
    double theta;
    synchronized (this.lock) {
      theta = this.theta;
    }
    return theta;
  }

  /**
   * TODO
   */
  public void getPosition(double[] position, boolean[] update) {
    synchronized (this.lock) {
      position[0] = (update[0]) ? this.x : position[0];
      position[1] = (update[1]) ? this.y : position[1];
      position[2] = (update[2]) ? this.theta : position[2];
    }
  }

  /**
   * TODO
   */
  public void setPosition(double[] position, boolean[] update) {
    synchronized (this.lock) {
      this.x = (update[0]) ? position[0] : this.x;
      this.y = (update[1]) ? position[1] : this.y;
      this.theta = (update[2]) ? position[2] : this.theta;
    }
  }

}
