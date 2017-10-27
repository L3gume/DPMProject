package ca.mcgill.ecse211.finalproject;

import lejos.hardware.motor.EV3LargeRegulatedMotor;



public class ZipLine {

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
   * TODO
   */
  public void cross() {
    // ...
  }

}
