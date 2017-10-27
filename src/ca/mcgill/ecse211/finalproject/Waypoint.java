package ca.mcgill.ecse211.finalproject;



public class Waypoint {

  // --------------------------------------------------------------------------------
  // Constants
  // --------------------------------------------------------------------------------


  // --------------------------------------------------------------------------------
  // Variables
  // --------------------------------------------------------------------------------

  public double x;
  public double y;


  /**
   * Constructor
   * @param x the position in x (in centimeters)
   * @param y the position in y (in centimeters)
   */
  public Waypoint(double x, double y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Constructor
   * @param coords the position in x (index 0) and y (index 1) (in centimeters)
   */
  public Waypoint(double[] coords) {
    this.x = coords[0];
    this.y = coords[1];
  }

}
