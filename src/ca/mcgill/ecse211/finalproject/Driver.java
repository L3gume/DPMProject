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
   * Set the speed of the left wheel motor
   *
   * @param speed desired speed of the motor.
   */
  public void setSpeedLeftMotor(float speed) {
    leftMotor.setSpeed(speed);
  }

  /**
   * Set the speed of the right wheel motor
   *
   * @param speed desired speed of the motor.
   */
  public void setSpeedRightMotor(float speed) {
    rightMotor.setSpeed(speed * FinalProject.RIGHT_WHEEL_MULT);
  }

  /**
   * Make the robot rotate a certain angle.
   *
   * @param angle_deg The angle to rotate, in degrees
   * @param inst_ret boolean, true the immediately return from the method.
   */
  public void rotate(double angle_deg, boolean inst_ret) {
    setSpeedLeftMotor(FinalProject.SPEED_ROT);
    setSpeedRightMotor(FinalProject.SPEED_ROT);
    leftMotor.synchronizeWith(new EV3LargeRegulatedMotor[] {rightMotor});
    leftMotor.rotate(-convertAngle(angle_deg), true);
    rightMotor.rotate(convertAngle(angle_deg), inst_ret);
    leftMotor.endSynchronization();
  }

  /**
   * Make the robot move forward for a certain distance.
   *
   * @param dist the desired distance to travel, in centimeters
   * @param inst_ret boolean, true the immediately return from the method.
   */
  public void moveForward(double dist, boolean inst_ret) {
    setSpeedLeftMotor(FinalProject.SPEED_FWD);
    setSpeedRightMotor(FinalProject.SPEED_FWD);
    leftMotor.synchronizeWith(new EV3LargeRegulatedMotor[] {rightMotor});
    leftMotor.rotate(convertDistance(dist), true);
    rightMotor.rotate(convertDistance(dist), inst_ret);
    leftMotor.endSynchronization();
  }

  /**
   * Makes the robot move forward indefinitely,
   */
  public void endlessMoveForward() {
    leftMotor.synchronizeWith(new EV3LargeRegulatedMotor[] {rightMotor});
    leftMotor.forward();
    rightMotor.forward();
    leftMotor.endSynchronization();
  }

  /**
   * Make the robot move backwards for a certain distance.
   *
   * @param dist the desired distance to travel, in centimeters
   * @param inst_ret boolean, true the immediately return from the method.
   */
  public void moveBackward(double dist, boolean inst_ret) {
    setSpeedLeftMotor(FinalProject.SPEED_FWD);
    setSpeedRightMotor(FinalProject.SPEED_FWD);
    leftMotor.synchronizeWith(new EV3LargeRegulatedMotor[] {rightMotor});
    leftMotor.rotate(-convertDistance(dist), true);
    rightMotor.rotate(-convertDistance(dist), inst_ret);
    leftMotor.endSynchronization();
  }

  /**
   * Makes the robot move backwards indefinitely,
   */
  public void endlessMoveBackward() {
    leftMotor.synchronizeWith(new EV3LargeRegulatedMotor[] {rightMotor});
    leftMotor.backward();
    rightMotor.backward();
    leftMotor.endSynchronization();
  }

  /**
   * Stops both motors.
   */
  public void stopBoth() {
    leftMotor.synchronizeWith(new EV3LargeRegulatedMotor[] {rightMotor});
    leftMotor.stop(true);
    rightMotor.stop(true);
    leftMotor.endSynchronization();
  }
  
  /**
   * Stops the left wheel.
   */
  public void stopLeftWheel() {
    leftMotor.stop();
  }
  
  /**
   * Stops the right wheel.
   */
  public void stopRightWheel() {
    rightMotor.stop();
  }
  
  /**
   * Makes the left wheel go forward at half the regular speed.
   */
  public void leftMotorForward() {
    leftMotor.forward();
  }
  
  /**
   * Makes the right wheel go forward at half the regular speed.
   */
  public void rightMotorForward() {
    rightMotor.forward();
  }
  
  /**
   * Makes the left wheel go backward at half the regular speed.
   */
  public void leftMotorBackward() {
    leftMotor.backward();
  }
  
  /**
   * Makes the right wheel go backward at half the regular speed.
   */
  public void rightMotorBackward() {
    rightMotor.backward();
  }
  
  /**
   * Starts the zip line motor. Makes it keep going until the stopTopMotor() method is called.
   */
  public void startTopMotor() {
    topMotor.backward(); // actually has to spin backwards
  }

  /**
   * Stops the zip line motor.
   */
  public void stopTopMotor() {
    topMotor.setSpeed(FinalProject.ZIPLINE_TRAVERSAL_SPEED);
    topMotor.stop();
  }

  /**
   * Rotates the front motor by a certain amount. Useful when avoiding obstacles.
   *
   * @param angle The desired rotation angle, in degrees
   */
  public void rotateFrontMotor(int angle) {
    frontMotor.setSpeed(75);
    frontMotor.rotate(angle, true);
  }

  /**
   * Avoid the obstacle in front of the robot.
   *
   * @param dist the distance read by the ultrasonic sensor.
   */
  public void avoidObstacle(float dist) {
    // TODO
  }

  /**
   * Helper methods
   */
  private static int convertDistance(double distance) {
    return (int) ((180.0 * distance) / (Math.PI * FinalProject.WHEEL_RADIUS));
  }

  private static int convertAngle(double angle) {
    return convertDistance(Math.PI * FinalProject.WHEEL_BASE * angle / 360.0);
  }
}