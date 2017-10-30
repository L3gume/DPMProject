package ca.mcgill.ecse211.finalproject;

/**
 * Performs the ultrasonic localization
 *
 * @author Justin Tremblay
 * @author Josh Inscoe
 */
public class UltrasonicLocalizer {

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
  public UltrasonicLocalizer(Driver driver, Odometer odometer, SensorData sd) {
    this.driver = driver;
    this.odometer = odometer;
    this.sd = sd;
  }


  /**
   * Performs rising of falling edge localization depending on the distance read by the ultrasonic sensor.
   */
  public void localize() {
    // ...
  }

}
