package ca.mcgill.ecse211.finalproject;

import lejos.hardware.motor.EV3LargeRegulatedMotor;



public class ZipLine {

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
   * @param zipMotor TODO
   * @param driver TODO
   * @param odometer TODO
   * @param sd TODO
   */
  public ZipLine(EV3LargeRegulatedMotor zipMotor, Driver driver, Odometer odometer, SensorData sd) {
    this.zipMotor = zipMotor;
    this.driver = driver;
    this.odometer = odometer;
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
   * @return new state
   */
  private Zip_State process_idle() {
    return Zip_State.IDLE;
  }

  /**
   * process the aligning state
   *
   * @return new state
   */
  private Zip_State process_aligning() {
    return Zip_State.IDLE;
  }

  /**
   * process the moving state
   *
   * @return new state
   */
  private Zip_State process_moving() {
    return Zip_State.IDLE;
  }

  /**
   * process the ziplining state (when the robot is hanging from the zipline)
   *
   * @return new state
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

  public boolean isDone() {
    return done;
  }
}
