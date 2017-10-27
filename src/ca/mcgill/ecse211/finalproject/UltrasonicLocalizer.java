package ca.mcgill.ecse211.finalproject;



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
   * @param driver TODO
   * @param odometer TODO
   * @param sd TODO
   */
  public UltrasonicLocalizer(Driver driver, Odometer odometer, SensorData sd) {
    this.driver = driver;
    this.odometer = odometer;
    this.sd = sd;
  }


  /**
   * TODO
   */
  public synchronized void localize() {
    // ...
  }

}
