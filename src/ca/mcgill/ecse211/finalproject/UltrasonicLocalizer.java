package ca.mcgill.ecse211.finalproject;

import lejos.hardware.Sound;
import sun.applet.Main;

/**
 * Performs the ultrasonic localization
 *
 * @author Justin Tremblay
 * @author Josh Inscoe
 */
public class UltrasonicLocalizer {

  public enum Mode {RISING_EDGE, FALLING_EDGE}

  // --------------------------------------------------------------------------------
  // Constants
  // --------------------------------------------------------------------------------


  // --------------------------------------------------------------------------------
  // Variables
  // --------------------------------------------------------------------------------

  private Driver driver;
  private Odometer odo;
  private SensorData sd;

  private Mode cur_mode = Mode.RISING_EDGE;
  private Waypoint ref_pos = null;
  private double ref_angle = 0;

  private double theta1 = 0;
  private double theta2 = 0;
  /**
   * Constructor
   *
   * @param driver driver object, used for moving the robot.
   * @param odo    odo, used to get angle variation between readings and to inject to the new value into.
   * @param sd     SensorData object, used to get the sensor readings.
   */
  public UltrasonicLocalizer(Driver driver, Odometer odo, SensorData sd) {
    this.driver = driver;
    this.odo = odo;
    this.sd = sd;
  }


  /**
   * Performs rising of falling edge localization depending on the distance read by the ultrasonic sensor.
   */
  public void localize() {
    sd.incrementUSRefs();
    ref_pos = Localizer.getRefPos();
    sleepThread(1);
    determineMode();
    determineRefAngle();

    driver.rotate(360, true);
    wait(cur_mode);
    theta1 = odo.getTheta(); // Record the current theta.

    if (FinalProject.DEBUG) {
      System.out.println("theta1: " + theta1);
    }

    driver.rotate(-360, true);

    sleepThread(3); // Wait for a bit.

    wait(cur_mode);
    driver.rotate(0, true);
    theta2 = odo.getTheta();

    if (FinalProject.DEBUG) {
      System.out.println("theta2: " + theta2);
    }

    computeOrientation();
    sd.decrementUSRefs();
  }

  /**
   * Computes the orientation of the robot using the recorded angles.
   */
  private void computeOrientation() {
    // compute the error with the reference angle (taken from the reference position).
    double theta_err = ref_angle - ((theta1 + theta2) / 2);

    if (FinalProject.DEBUG) {
      System.out.println("current heading: " + Math.toDegrees(odo.getTheta()) + " error: "
          + Math.toDegrees(theta_err));
    }

    // Set the odo's new orientation.
    odo.setTheta(odo.getTheta() + theta_err);
    Sound.beepSequenceUp();
  }

  /*
   * Utility methods, getters and setters.
   */

  /*
   * Not really necessary, this is just to make the risingEdge and fallingEdge methods more
   * readable.
   */
  private void wait(Mode m) {
    Sound.setVolume(70);
    if (m == Mode.FALLING_EDGE) {
      while (sd.getUSDataLatest() > FinalProject.FALLING_EDGE_THRESHOLD) ; // Wait until we capture a falling
      // edge.
      Sound.beep();
    } else {
      while (sd.getUSDataLatest() < FinalProject.RISING_EDGE_THRESHOLD) ; // Wait until we capture a rising edge.
      Sound.beep();
    }
  }

  /*
   * Not really necessary, this is just to make the risingEdge and fallingEdge methods more
   * readable.
   */
  private void sleepThread(float seconds) {
    try {
      Thread.sleep((long) (seconds * 1000));
    } catch (Exception e) {
      System.out.println("[ULTRASONIC] Can't sleep thread");
      // TODO: handle exception
    }
  }

  private void determineMode() {
    if (sd.getUSDataLatest() > 50) {
      cur_mode = Mode.FALLING_EDGE;
    } else {
      cur_mode = Mode.RISING_EDGE;
    }
  }

  private void determineRefAngle() {
    switch (MainController.is_red ? MainController.RedCorner : MainController.GreenCorner) {
      case 0:
        ref_angle = cur_mode == Mode.FALLING_EDGE ? Math.toRadians(225) : Math.toRadians(45);
        break;
      case 1:
        ref_angle = cur_mode == Mode.FALLING_EDGE ? Math.toRadians(315) : Math.toRadians(135);
        break;
      case 2:
        ref_angle = cur_mode == Mode.FALLING_EDGE ? Math.toRadians(45) : Math.toRadians(225);
        break;
      case 3:
        ref_angle = cur_mode == Mode.FALLING_EDGE ? Math.toRadians(135) : Math.toRadians(315);
        break;
      default:
        break;
    }
    
  }
}
