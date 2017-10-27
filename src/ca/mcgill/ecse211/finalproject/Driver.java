package ca.mcgill.ecse211.finalproject;

import lejos.hardware.motor.EV3LargeRegulatedMotor;



public class Driver {

  // --------------------------------------------------------------------------------
  // Constants
  // --------------------------------------------------------------------------------

  public static enum State {
	UNKNOWN,
    MOVING_FORWARD,
    MOVING_BACKWARD,
    ROTATING,
    STOPPED
  };

  // --------------------------------------------------------------------------------
  // Variables
  // --------------------------------------------------------------------------------

  private EV3LargeRegulatedMotor leftMotor;
  private EV3LargeRegulatedMotor rightMotor;


  /**
   * Constructor
   * @param leftMotor TODO
   * @param rightMotor TODO
   */
  public Driver(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor) {
    this.leftMotor = leftMotor;
    this.rightMotor = rightMotor;
  }


  /**
   * TODO
   */
  public State getState( /* TODO */ ) {
    // ...
	return State.UNKNOWN;
    // ...
  }

  /**
   * TODO
   */
  public void setSpeedLeftMotor( /* TODO */ ) {
    // ...
  }

  /**
   * TODO
   */
  public void setSpeedRightMotor( /* TODO */ ) {
    // ...
  }

  /**
   * TODO
   */
  public void rotate( /* TODO */ ) {
    // ...
  }

  /**
   * TODO
   */
  public void stop( /* TODO */ ) {
    // ...
  }

}
