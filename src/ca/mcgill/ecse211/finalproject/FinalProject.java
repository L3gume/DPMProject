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
import lejos.robotics.filter.MedianFilter;
import lejos.robotics.filter.MeanFilter;


/**
 * Main class, contains the constants, motors, sensors and the main() method.
 */
public class FinalProject {

  public static final boolean DEBUG = false;

  // --------------------------------------------------------------------------------
  // Constants
  // --------------------------------------------------------------------------------

  // Wifi constants.
  public static final String SERVER_IP = "192.168.43.173"; // CHANGE THIS TO YOUR COMPUTER'S IP
  public static final int TEAM_NB = 6;
  public static final boolean ENABLE_WIFI_DEBUG = false;
  
  // Board-related constants
  public static final double BOARD_TILE_LENGTH = 30.48;

  // Odometry-related constants
  public static final double WHEEL_RADIUS = 2.1;
  public static final double WHEEL_BASE = 15.225;

  // Driver-related constants
  public static final int SPEED_FWD = 175;
  public static final int SPEED_ROT = 100;
  public static final float RIGHT_WHEEL_MULT = 1.003f;

  // Localization-related constants
  public static final int RISING_EDGE_THRESHOLD = 50;
  public static final int FALLING_EDGE_THRESHOLD = 50;
  public static final float LIGHT_LEVEL_THRESHOLD = 0.3f;
  public static final double LIGHT_SENSOR_OFFSET = 2.23;
  public static final long MOVE_TIME_THRESHOLD = 4000; // milliseconds
  public static final Waypoint DEBUG_REF_POS = new Waypoint(1, 6);
  public static final Waypoint DEBUG_START_POS = new Waypoint(1, 1);
  public static final Waypoint DEBUG_ZIP_POS = new Waypoint(2, 6);
  public static final Waypoint DEUBG_ZIP_END = new Waypoint(7, 6);
  
  // Poller-related constants
  public static final long SLEEP_TIME = 20;
  
  // Zipline-related constants
  public static final double ZIPLINE_ORIENTATION = 0.0;						// TODO this will be determined by values inputted over WiFi
  public static final Waypoint ZIPLINE_START_POS = new Waypoint(2, 3);	// TODO this will be determined by values inputted over WiFi
  public static final double ZIPLINE_ORIENTATION_THRESHOLD = Math.toRadians(1); 
  public static final float ZIPLINE_TRAVERSAL_SPEED = 150.f;
  public static final double FLOOR_LIGHT_READING = 0.1;		// TODO: calibrate this
  public static final double FLOOR_READING_FILTER = 20;
  
  // Navigation-related constants
  public static final double ANGLE_THRESHOLD = Math.toRadians(1);
  public static final double DISTANCE_THRESHOLD = 1;


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
    SampleProvider lsMedianl = new MedianFilter(lsSampleProviderl, lsSampleProviderl.sampleSize());
    SensorModes lsSensorr = new EV3ColorSensor(FinalProject.lsPortr);
    SampleProvider lsSampleProviderr = lsSensorr.getMode("Red");
    SampleProvider lsMedianr = new MedianFilter(lsSampleProviderr, lsSampleProviderr.sampleSize());
    SensorModes lsSensorm = new EV3ColorSensor(FinalProject.lsPortm);
    SampleProvider lsSampleProviderm = lsSensorm.getMode("RGB");
    SampleProvider lsMeanm = new MeanFilter(lsSampleProviderm, lsSampleProviderm.sampleSize());


    // Create SensorData object.
    SensorData sd = new SensorData();

    // Create sensorPoller object
    SensorPoller sensorPoller = new SensorPoller(lsMedianl, 
        lsMedianr, lsSampleProviderm, usSampleProvider, sd);


    // Create Odometer object.
    Odometer odometer = new Odometer(FinalProject.leftMotor, FinalProject.rightMotor,
        FinalProject.WHEEL_RADIUS, FinalProject.WHEEL_BASE);

    Driver dr =
        new Driver(FinalProject.leftMotor, FinalProject.rightMotor, FinalProject.zipMotor, null);
    UltrasonicLocalizer ul = new UltrasonicLocalizer(dr, odometer, sd);
    LightLocalizer ll = new LightLocalizer(dr, odometer, sd);
    Localizer loc = new Localizer(ul, ll, dr);   
    Navigator nav = new Navigator(dr, odometer, sd);
    ZipLine zip = new ZipLine(zipMotor,odometer, dr, sd);

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
    MainController cont = new MainController(loc, ul, ll, nav, zip, null);
    // TODO: remove display during demo/competition
    Display disp = new Display(LocalEV3.get().getTextLCD(), odometer, cont, sd, sensorPoller);

    dr.setSpeedLeftMotor(SPEED_ROT);
    dr.setSpeedRightMotor(SPEED_ROT);
    
    sd.incrementColorRefs();
    // Start data threads. 
    sensorPoller.start();
    //odometer.start();
    //disp.start();

    ///cont.start();
    
    // Wheel base test
    //dr.rotate(90, false);
    while (Button.waitForAnyPress() != Button.ID_ESCAPE);
    System.exit(0);
  }
}