package ca.mcgill.ecse211.finalproject;

/**
 * Performs the light localization
 *
 * @author Justin Tremblay
 * @author Josh Inscoe
 */
public class LightLocalizer {

  // --------------------------------------------------------------------------------
  // Constants
  // --------------------------------------------------------------------------------


  // --------------------------------------------------------------------------------
  // Variables
  // --------------------------------------------------------------------------------

  private Driver driver;
  private Odometer odometer;
  private SensorData sd;

  /**
   * Constructor
   *
   * @param driver driver object, used for moving the robot.
   * @param odometer odometer, used to get angle variation between readings and to inject to the new value into.
   * @param sd SensorData object, used to get the sensor readings.
   */
  public LightLocalizer(Driver driver, Odometer odometer, SensorData sd) {
    this.driver = driver;
    this.odometer = odometer;
    this.sd = sd;
  }


  /**
   * Performs light localization and injects the new coordinates in the odometer.
   */
  public void localize() {
    // ...
  }

}
