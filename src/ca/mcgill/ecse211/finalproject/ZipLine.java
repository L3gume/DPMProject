package ca.mcgill.ecse211.finalproject;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * Handles crossing the zip line
 *
 * @author Justin Tremblay
 * @author Josh Inscoe
 */
public class ZipLine {

  /**
   * Enum describing the current state of the zip line controller
   */
  public enum Zip_State {
    IDLE, ALIGNING, MOVING, ZIPLINING, DONE
  }

  // --------------------------------------------------------------------------------
  // Constants
  // --------------------------------------------------------------------------------


  // --------------------------------------------------------------------------------
  // Variables
  // --------------------------------------------------------------------------------

  private EV3LargeRegulatedMotor zipMotor;
  private Driver driver;
  private Odometer odometer;
  private SensorData sd;

  private Zip_State cur_state = Zip_State.IDLE;
  private boolean done = false;

  /**
   * Constructor
   *
   * @param zipMotor Motor mounted on top of the robot.
   * @param driver Driver object, handles moving the robot.
   * @param sd SensorData object, gives access to the light sensor data.
   */
  public ZipLine(EV3LargeRegulatedMotor zipMotor, Driver driver, SensorData sd) {
    this.zipMotor = zipMotor;
    this.driver = driver;
    this.sd = sd;
  }

  /**
   * Processes the current state, updates it and returns the new state as a string.
   * This method is package-private, only classes in the same package can access it.
   *
   * @return The new state, as a string.
   */
  String process() {
    switch (cur_state) {
      case IDLE:
        cur_state = process_idle();
        break;
      case ALIGNING:
        cur_state = process_aligning();
        break;
      case MOVING:
        cur_state = process_moving();
        break;
      case ZIPLINING:
        cur_state = process_ziplining();
        break;
      case DONE:
        cur_state = process_done();
        break;
    }
    return cur_state.toString();
  }

  /**
   * process the IDLE state
   *
   * @return new state, or same if no need to cross the zip line.
   */
  private Zip_State process_idle() {
    return Zip_State.IDLE;
  }

  /**
   * process the aligning state
   *
   * @return new state, or same if the angle to the zip line is still too high.
   */
  private Zip_State process_aligning() {
    return Zip_State.IDLE;
  }

  /**
   * process the moving state
   *
   * @return new state, or same if still grounded.
   */
  private Zip_State process_moving() {
    return Zip_State.IDLE;
  }

  /**
   * process the ziplining state (when the robot is hanging from the zipline)
   *
   * @return new state, or same if still not grounded.
   */
  private Zip_State process_ziplining() {
    return Zip_State.IDLE;
  }

  /**
   * process the done state
   *
   * @return new state
   */
  private Zip_State process_done() {
    done = true;
    return Zip_State.IDLE;
  }

  /**
   * Tells whether or not we are done crossing the zipline.
   *
   * @return boolean telling whether or not we are done.
   */
  public boolean isDone() {
    return done;
  }
}
