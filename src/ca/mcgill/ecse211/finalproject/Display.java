package ca.mcgill.ecse211.finalproject;

import lejos.hardware.lcd.TextLCD;

/**
 * Handles displaying information on the EV3's lcd panel
 *
 * @author Josh Inscoe
 */
public class Display extends Thread {

  // --------------------------------------------------------------------------------
  // Constants
  // --------------------------------------------------------------------------------
  private final TextLCD t;
  private Odometer odo;
  private MainController mc;
  private SensorData sd;

  // --------------------------------------------------------------------------------
  // Variables
  // --------------------------------------------------------------------------------


  /**
   * Constructor
   *
   * @param t TextLCD to write to
   * @param odo Odometer to get position and heading from
   * @param mc MainController to get state information
   * @param sd SensorData object to get sensor readings
   */
  public Display(final TextLCD t, Odometer odo, MainController mc, SensorData sd) {
    this.t = t;
    this.odo = odo;
    this.mc = mc;
    this.sd = sd;
  }


  /**
   * run() method, updates the displayed information periodically.
   */
  public void run() {
    // ...
  }

}
