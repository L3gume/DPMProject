package ca.mcgill.ecse211.finalproject;



public class Navigator {

  // --------------------------------------------------------------------------------
  // Constants
  // --------------------------------------------------------------------------------


  // --------------------------------------------------------------------------------
  // Variables
  // --------------------------------------------------------------------------------

  private Driver driver;
  private Odometer odometer;
  private SensorData sd;

  private Waypoint source;
  private Waypoint destination;


  /**
   * Constructor
   * @param driver TODO
   * @param odometer TODO
   * @param sd TODO
   */
  public Navigator(Driver driver, Odometer odometer, SensorData sd) {
    this.driver = driver;
    this.odometer = odometer;
    this.sd = sd;
  }


  /**
   * TODO
   */
  public synchronized void navigate() {
    // ...
  }


  /**
   * TODO
   */
  public synchronized void setSource(Waypoint source) {
    this.source = source;
  }

  /**
   * TODO
   */
  public synchronized void setDestination(Waypoint destination) {
    this.destination = destination;
  }

}
