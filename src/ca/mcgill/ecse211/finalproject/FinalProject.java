package ca.mcgill.ecse211.finalproject;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.MeanFilter;

/**
 * Main class, contains the constants, motors, sensors and the main() method.
 */
public class FinalProject {

  public static final boolean DEBUG = false;



  // --------------------------------------------------------------------------------
  // Constants
  // --------------------------------------------------------------------------------

  // Board-related constants
  public static final double BOARD_TILE_LENGTH = 30.48;

  // Odometry-related constants
  public static final double WHEEL_RADIUS = 2.1;
  public static final double WHEEL_BASE = 15.225;

  // Driver-related constants
  public static final int SPEED_FWD = 175;
  public static final int SPEED_ROT = 100;

  // Localization-related constants
  public static final int RISING_EDGE_THRESHOLD = 50;
  public static final int FALLING_EDGE_THRESHOLD = 70;
  public static final float LIGHT_LEVEL_THRESHOLD = 0.30f;
  public static final double LIGHT_SENSOR_OFFSET = 1.8;
  public static final long MOVE_TIME_THRESHOLD = 3000; // milliseconds
  public static final Waypoint DEBUG_REF_POS = new Waypoint(1, 1);


  // --------------------------------------------------------------------------------
  // Variables
  // --------------------------------------------------------------------------------

  // Wheel motors
  public static final EV3LargeRegulatedMotor leftMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
  public static final EV3LargeRegulatedMotor rightMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));

  // Zip-line motor
  public static final EV3LargeRegulatedMotor zipMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));

  // Sensor ports
  private static final Port usPort = LocalEV3.get().getPort("S4");
  private static final Port lsPortl = LocalEV3.get().getPort("S1");
  private static final Port lsPortr = LocalEV3.get().getPort("S2");
  private static final Port lsPortm = LocalEV3.get().getPort("S3");


  // --------------------------------------------------------------------------------
  // Main method
  // --------------------------------------------------------------------------------

  /**
   * Main method of the program, this is where all the objects are initialized and all the threads
   * are started.
   */
  public static void main(String[] args) {
    // Suppress the warning we would reserve, since we do not close certain resources below.
    @SuppressWarnings("resource")

    // Get a handle to the EV3 LCD screen.
    final TextLCD t = LocalEV3.get().getTextLCD();

    // Initialize the ultrasonic and light sensors.
    SensorModes usSensor = new EV3UltrasonicSensor(FinalProject.usPort);
    SampleProvider usSampleProvider = usSensor.getMode("Distance");
    SensorModes lsSensorl = new EV3ColorSensor(FinalProject.lsPortl);
    SampleProvider lsSampleProviderl = lsSensorl.getMode("Red");
    SampleProvider lsMedianl = new MeanFilter(lsSampleProviderl, lsSampleProviderl.sampleSize());
    SensorModes lsSensorr = new EV3ColorSensor(FinalProject.lsPortr);
    SampleProvider lsSampleProviderr = lsSensorr.getMode("Red");
    SampleProvider lsMedianr = new MeanFilter(lsSampleProviderr, lsSampleProviderr.sampleSize());
    SensorModes lsSensorm = new EV3ColorSensor(FinalProject.lsPortm);
    SampleProvider lsSampleProviderm = lsSensorm.getMode("Red");
    SampleProvider lsMedianm = new MeanFilter(lsSampleProviderm, lsSampleProviderm.sampleSize());


    // Create SensorData object.
    SensorData sd = new SensorData();

    // Create UltrasonicPoller and LightPoller objects.
    UltrasonicPoller usPoller = new UltrasonicPoller(usSampleProvider, sd);
    LightPoller lsPoller =
        new LightPoller(lsMedianl, lsMedianr, lsMedianm, sd);
    



    // Create Odometer object.
    Odometer odometer = new Odometer(FinalProject.leftMotor, FinalProject.rightMotor,
        FinalProject.WHEEL_RADIUS, FinalProject.WHEEL_BASE);

    Driver dr =
        new Driver(FinalProject.leftMotor, FinalProject.rightMotor, FinalProject.zipMotor, null);
    UltrasonicLocalizer ul = new UltrasonicLocalizer(dr, odometer, sd);
    LightLocalizer ll = new LightLocalizer(dr, odometer, sd);
    Localizer loc = new Localizer(ul, ll, dr);
    Display disp = new Display(LocalEV3.get().getTextLCD(), odometer, null, sd, usPoller);
    //
    // TODO:
    //
    //
    // Construct the following objects:
    //
    // UltrasonicLocalizer
    // LightLocalizer
    // Navigator
    // ZipLine
    //

    // Create MainController object.
    // MainController zipLineController = new MainController( [> ... <] );

    dr.setSpeedLeftMotor(SPEED_ROT);
    dr.setSpeedRightMotor(SPEED_ROT);
    // Start data threads.
    usPoller.start();
    lsPoller.start();
    odometer.start();
    disp.start();

    Button.waitForAnyPress();
    (new Thread() {
      public void run() {
        // Kill this program whenever the escape button is pressed on the EV3.
        while (Button.waitForAnyPress() != Button.ID_ESCAPE);
        System.exit(0);
      }
    }).start();
    
    ul.localize();
    ll.localize();
    // Wheel base test
    //dr.rotate(90, false);
    while (Button.waitForAnyPress() != Button.ID_ESCAPE);
    System.exit(0);
  }


  // --------------------------------------------------------------------------------
  // Helper methods
  // --------------------------------------------------------------------------------

  /**
   * Display the main menu, querying the user for a pair of X/Y-coordinates.
   *
   * @param t the EV3 LCD display to which the main menu should be output
   * @param title the title to display in the menu
   * @param llim the lower limit allowed for an X/Y-coordinate
   * @param ulim the upper limit allowed for an X/Y-coordinate
   * @return a Waypoint object holding an X/Y-coordinate pair
   */
  private static Waypoint getCoordinates(final TextLCD t, String title, int llim, int ulim) {
    boolean done = false;

    // Clear the display.
    t.clear();

    t.drawString(title, 0, 0);
    t.drawString("-----------------", 0, 1);
    t.drawString("                 ", 0, 2);

    int[] coords = new int[] {0, 0};

    int index = 0;

    while (!done) {
      int buttonChoice = -1;

      // Clear the current x/y-coordinate values.
      t.drawString("X:               ", 0, 3);
      t.drawString("Y:               ", 0, 4);

      // Print the current x/y-coordinate values.
      t.drawString(String.format("%2d", coords[0]), 3, 3);
      t.drawString(String.format("%2d", coords[1]), 3, 4);

      // Draw the indicator showing which coordinate value is currently selected.
      t.drawString("<--", 12, 3 + index);

      buttonChoice = Button.waitForAnyPress();

      switch (buttonChoice) {
        // Select the x-coordinate for modification.
        case Button.ID_UP:
          index = 0;

          break;

        // Select the y-coordinate for modification.
        case Button.ID_DOWN:
          index = 1;

          break;

        // Decrease the currently selected coordinate value.
        case Button.ID_LEFT:
          if (coords[index] > llim) {
            coords[index] -= 1;
          }

          break;

        // Increase the currently selected coordinate value.
        case Button.ID_RIGHT:
          if (coords[index] < ulim) {
            coords[index] += 1;
          }

          break;

        // Submit selected coordinates.
        case Button.ID_ENTER:
          done = true;

          break;

        // Exit.
        default:
          // Button.ID_ESCAPE
          System.exit(0);
      }
    }

    // Convert the X/Y-coordinate (currently in grid lines) to centimeters.
    double x = (double) (coords[0]) * FinalProject.BOARD_TILE_LENGTH;
    double y = (double) (coords[1]) * FinalProject.BOARD_TILE_LENGTH;

    Waypoint waypoint = new Waypoint(x, y);

    return waypoint;
  }
}
