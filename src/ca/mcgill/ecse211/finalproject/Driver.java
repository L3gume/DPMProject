package ca.mcgill.ecse211.finalproject;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;

/**
 * Driver class, handles moving the robot.
 *
 * @author Justin Tremblay
 * @author Josh Inscoe
 */
public class Driver {

  // --------------------------------------------------------------------------------
  // Constants
  // --------------------------------------------------------------------------------

  /**
   * Enum describing the current state of the driver
   */
  public enum D_State {
	UNKNOWN,
    MOVING_FORWARD,
    MOVING_BACKWARD,
    ROTATING,
    STOPPED
  }

  // --------------------------------------------------------------------------------
  // Variables
  // --------------------------------------------------------------------------------

  private EV3LargeRegulatedMotor leftMotor;
  private EV3LargeRegulatedMotor rightMotor;
  private EV3LargeRegulatedMotor topMotor; // motor for the zip line
  private EV3MediumRegulatedMotor frontMotor; // motor for the sensors


  /**
   * Constructor
   *
   * @param leftMotor Motor powering the left wheel.
   * @param rightMotor Motor powering the right wheel.
   * @param topMotor Motor used to cross the zip line.
   * @param frontMotor Motor to which the sensors are mounted.
   */
  public Driver(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, EV3LargeRegulatedMotor topMotor, EV3MediumRegulatedMotor frontMotor) {
    this.leftMotor = leftMotor;
    this.rightMotor = rightMotor;
    this.topMotor = topMotor;
    this.frontMotor = frontMotor;
  }


  /**
   * Gets the current state of the driver
   *
   * @return D_State, the current state of the driver.
   */
  public D_State getState() {
    // ...
	return D_State.UNKNOWN;
    // ...
  }

  /**
   * Set the speed of the left wheel motor
   *
   * @param speed desired speed of the motor.
   */
  public void setSpeedLeftMotor(float speed) {
    // ...
  }

  /**
   * Set the speed of the right wheel motor
   *
   * @param speed desired speed of the motor.
   */
  public void setSpeedRightMotor(float speed) {
    // ...
  }

  /**
   * Make the robot rotate a certain angle.
   *
   * @param angle The angle to rotate, in radians
   * @param inst_ret boolean, true the immediately return from the method.
   */
  public void rotate(double angle, boolean inst_ret) {
    // ...
  }

  /**
   * Make the robot move forward for a certain distance.
   *
   * @param dist the desired distance to travel, in centimeters
   * @param inst_ret boolean, true the immediately return from the method.
   */
  public void moveForward(double dist, boolean inst_ret) {

  }

  /**
   * Makes the robot move forward indefinitely,
   */
  public void endlessMoveForward() {

  }

  /**
   * Make the robot move backwards for a certain distance.
   *
   * @param dist the desired distance to travel, in centimeters
   * @param inst_ret boolean, true the immediately return from the method.
   */
  public void moveBackward(double dist, boolean inst_ret) {

  }

  /**
   * Makes the robot move backwards indefinitely,
   */
  public void endlessMoveBackward() {

  }

  /**
   * Stops both motors.
   */
  public void stop() {
    // ...
  }

  /**
   * Starts the zip line motor. Makes it keep going until the stopTopMotor() method is called.
   */
  public void startTopMotor() {

  }

  /**
   * Stops the zip line motor.
   */
  public void stopTopMotor() {

  }

  /**
   * Rotates the front motor by a certain amount. Useful when avoiding obstacles.
   *
   * @param angle The desired rotation angle, in radians.
   */
  public void rotateFrontMotor(double angle) {

  }
}
