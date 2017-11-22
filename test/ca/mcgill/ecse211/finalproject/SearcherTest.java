package ca.mcgill.ecse211.finalproject;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.MeanFilter;



/**
 * This class tests the functionality of the Searcher class.
 *
 * @author Joshua Inscoe
 */
public class SearcherTest {

  // --------------------------------------------------------------------------------
  // Constants
  // --------------------------------------------------------------------------------

  private static final long SLEEP_INTERVAL = 250;

  private static final double EPSILON = 0.01;


  // --------------------------------------------------------------------------------
  // Variables
  // --------------------------------------------------------------------------------


  // --------------------------------------------------------------------------------
  // Main Method
  // --------------------------------------------------------------------------------

  public static void main(String[] args) {

    // Start a separate thread to monitor for button presses and terminate this
    // program if it detects that the escape button has been pressed.
    (new Thread() {
      public void run() {
        while (Button.waitForAnyPress() != Button.ID_ESCAPE) {
          try {
            // Sleep for a little bit to avoid hogging the processor.
            Thread.sleep(SearcherTest.SLEEP_INTERVAL);
          } catch (Exception e) {
            // ...
          }
        }

        // Kill the program.
        System.exit(1);
      }
    }).start();

    {

    System.out.println("==================================================");
    System.out.println("testComputeSearchPathSimple1x2");
    System.out.println("==================================================");
    System.out.println("");

    boolean result = SearcherTest.testComputeSearchPathSimple1x2();

    // Print to the console whether the test passed or failed.
    if (result) {
      System.out.println("PASS");
    } else {
      System.out.println("FAIL");
    }

    System.out.println("");

    } // local

    {

    System.out.println("==================================================");
    System.out.println("testComputeSearchPathSimple2x2");
    System.out.println("==================================================");
    System.out.println("");

    boolean result = SearcherTest.testComputeSearchPathSimple2x2();

    // Print to the console whether the test passed or failed.
    if (result) {
      System.out.println("PASS");
    } else {
      System.out.println("FAIL");
    }

    System.out.println("");

    } // local

    {

    System.out.println("==================================================");
    System.out.println("testComputeSearchPathLeftWall2x1");
    System.out.println("==================================================");
    System.out.println("");

    boolean result = SearcherTest.testComputeSearchPathLeftWall2x1();

    // Print to the console whether the test passed or failed.
    if (result) {
      System.out.println("PASS");
    } else {
      System.out.println("FAIL");
    }

    System.out.println("");

    } // local

    {

    System.out.println("==================================================");
    System.out.println("testSearchSimple1x2");
    System.out.println("==================================================");
    System.out.println("");

    boolean result = SearcherTest.testSearchSimple1x2();

    // Print to the console whether the test passed or failed.
    if (result) {
      System.out.println("PASS");
    } else {
      System.out.println("FAIL");
    }

    System.out.println("");

    } // local

    {

    //
    // NOTE:
    //
    // If you want to implement more tests, please follow the pattern above for doing so.
    //

    } // local

    return;
  }


  // --------------------------------------------------------------------------------
  // Tests
  // --------------------------------------------------------------------------------

  /**
   * Test the Searcher class's `computeSearchPath()` method on a simple 1x2 search zone.
   *
   * @return true if the test passed, false otherwise
   */
  private static boolean testComputeSearchPathSimple1x2() {

    //
    // Example:
    //
    //                    (6, 6)
    //                         v
    // -------------------------
    // |   |   |   |   |   |   |
    // -------------------------
    // |   | 5 | 6 | 7 |   |   |
    // -------------------------
    // |   | 4 | X | 8 |   |   |
    // -------------------------
    // |   | 3 | X | 9 |   |   |
    // -------------------------
    // |   | 2 | 1 | 0 |   |   |
    // -------------------------
    // |   |   |   |   |   | L |
    // -------------------------
    // ^
    // (0, 0)
    //
    //
    // L = the starting location of the robot
    // X = the search zone
    //
    // 0-9 = the 0th, 1st, ..., 9th waypoint in the search path
    //

    boolean result = false;

    // This is the path that we are expecting the searcher to compute.
    Waypoint[] expected = new Waypoint[] {
      new Waypoint(3.5, 1.5),
      new Waypoint(2.5, 1.5),
      new Waypoint(1.5, 1.5),
      new Waypoint(1.5, 2.5),
      new Waypoint(1.5, 3.5),
      new Waypoint(1.5, 4.5),
      new Waypoint(2.5, 4.5),
      new Waypoint(3.5, 4.5),
      new Waypoint(3.5, 3.5),
      new Waypoint(3.5, 2.5)
    };

    // These are the lower-left and upper-right corners of the search zone.
    Waypoint enemyLL = new Waypoint(0.0, 0.0);
    Waypoint enemyUR = new Waypoint(6.0, 6.0);

    // These are the lower-left and upper-right corners of the search zone.
    Waypoint searchLL = new Waypoint(2.0, 2.0);
    Waypoint searchUR = new Waypoint(3.0, 4.0);

    // This is the location from which we are expecting the searcher to receive control.
    Waypoint location = new Waypoint(5.5, 0.5);

    // Perform the test.
    result = SearcherTest.testComputeSearchPathImpl(
        expected, enemyLL, enemyUR, searchLL, searchUR, location
        );

    return result;
  }

  /**
   * Test the Searcher class's `computeSearchPath()` method on a simple 2x2 search zone.
   *
   * @return true if the test passed, false otherwise
   */
  private static boolean testComputeSearchPathSimple2x2() {

    //
    // Example:
    //
    //                    (6, 6)
    //                         v
    // -------------------------
    // |   |   |   |   |   |   |
    // -------------------------
    // | 9 | A | B | 0 |   |   |
    // -------------------------
    // | 8 | X | X | 1 |   | L |
    // -------------------------
    // | 7 | X | X | 2 |   |   |
    // -------------------------
    // | 6 | 5 | 4 | 3 |   |   |
    // -------------------------
    // |   |   |   |   |   |   |
    // -------------------------
    // ^
    // (0, 0)
    //
    //
    // L = the starting location of the robot
    // X = the search zone
    //
    // 0-9,A-B = the 0th, 1st, ..., 9th, 10th, 11th waypoint in the search path
    //

    boolean result = false;

    // This is the path that we are expecting the searcher to compute.
    Waypoint[] expected = new Waypoint[] {
      new Waypoint(3.5, 4.5),
      new Waypoint(3.5, 3.5),
      new Waypoint(3.5, 2.5),
      new Waypoint(3.5, 1.5),
      new Waypoint(2.5, 1.5),
      new Waypoint(1.5, 1.5),
      new Waypoint(0.5, 1.5),
      new Waypoint(0.5, 2.5),
      new Waypoint(0.5, 3.5),
      new Waypoint(0.5, 4.5),
      new Waypoint(1.5, 4.5),
      new Waypoint(2.5, 4.5)
    };

    // These are the lower-left and upper-right corners of the search zone.
    Waypoint enemyLL = new Waypoint(0.0, 0.0);
    Waypoint enemyUR = new Waypoint(6.0, 6.0);

    // These are the lower-left and upper-right corners of the search zone.
    Waypoint searchLL = new Waypoint(1.0, 2.0);
    Waypoint searchUR = new Waypoint(3.0, 4.0);

    // This is the location from which we are expecting the searcher to receive control.
    Waypoint location = new Waypoint(5.5, 3.5);

    // Perform the test.
    result = SearcherTest.testComputeSearchPathImpl(
        expected, enemyLL, enemyUR, searchLL, searchUR, location
        );

    return result;
  }

  /**
   * Test the Searcher class's `computeSearchPath()` method on a 2x1 search zone,
   * located along the left wall.
   *
   * @return true if the test passed, false otherwise
   */
  private static boolean testComputeSearchPathLeftWall2x1() {

    //
    // Example:
    //
    //                    (6, 6)
    //                         v
    // -------------------------
    // |   |   |   |   |   |   |
    // -------------------------
    // | 6 | 5 | 4 |   |   |   |
    // -------------------------
    // | X | X | 3 |   |   |   |
    // -------------------------
    // | 0 | 1 | 2 |   |   |   |
    // -------------------------
    // |   |   |   |   |   |   |
    // -------------------------
    // |   |   |   |   | L |   |
    // -------------------------
    // ^
    // (0, 0)
    //
    //
    // L = the starting location of the robot
    // X = the search zone
    //
    // 0-6 = the 0th, 1st, ..., 6th waypoint in the search path
    //

    boolean result = false;

    // This is the path that we are expecting the searcher to compute.
    Waypoint[] expected = new Waypoint[] {
      new Waypoint(0.5, 2.5),
      new Waypoint(1.5, 2.5),
      new Waypoint(2.5, 2.5),
      new Waypoint(2.5, 3.5),
      new Waypoint(2.5, 4.5),
      new Waypoint(1.5, 4.5),
      new Waypoint(0.5, 4.5)
    };

    // These are the lower-left and upper-right corners of the search zone.
    Waypoint enemyLL = new Waypoint(0.0, 0.0);
    Waypoint enemyUR = new Waypoint(6.0, 6.0);

    // These are the lower-left and upper-right corners of the search zone.
    Waypoint searchLL = new Waypoint(0.0, 3.0);
    Waypoint searchUR = new Waypoint(2.0, 4.0);

    // This is the location from which we are expecting the searcher to receive control.
    Waypoint location = new Waypoint(4.5, 0.5);

    // Perform the test.
    result = SearcherTest.testComputeSearchPathImpl(
        expected, enemyLL, enemyUR, searchLL, searchUR, location
        );

    return result;
  }

  /**
   * Test the Searcher class's `search()` method on a simple 1x2 search zone.
   *
   * @return true if the test passed, false otherwise
   */
  private static boolean testSearchSimple1x2() {

    //
    // Example:
    //
    //            (4, 4)
    //                 v
    // -----------------
    // | 5 | 6 | 7 |   |
    // -----------------
    // | 4 | X | 8 |   |
    // -----------------
    // | 3 | X | 9 |   |
    // -----------------
    // | 2 | 1 | 0 | L |
    // -----------------
    // ^
    // (0, 0)
    //
    //
    // L = the starting location of the robot
    // X = the search zone
    //
    // 0-9 = the 0th, 1st, ..., 9th waypoint in the search path
    //

    boolean result = false;

    // This is the search path that we want the searcher to have the robot follow.
    Waypoint[] path = new Waypoint[] {
      new Waypoint(2.5, 0.5),
      new Waypoint(1.5, 0.5),
      new Waypoint(0.5, 0.5),
      new Waypoint(0.5, 1.5),
      new Waypoint(0.5, 2.5),
      new Waypoint(0.5, 3.5),
      new Waypoint(1.5, 3.5),
      new Waypoint(2.5, 3.5),
      new Waypoint(2.5, 2.5),
      new Waypoint(2.5, 1.5)
    };

    // These are the indices of the corner tiles in `path`.
    int[] corners = new int[] { 0, 2, 5, 7 };

    // This is the direction in which we want the robot to travel while searching.
    Searcher.Direction direction = Searcher.Direction.CLOCKWISE;

    // This is the color of the enemy flag for which the searcher will search.
    Searcher.FlagColor color = Searcher.FlagColor.BLUE;

    // This is the location from which we are expecting the searcher to receive control.
    Waypoint location = new Waypoint(3.5, 0.5);

    // This is the corner in which the robot should start.
    int startCorner = 1;

    // Perform the test.
    result = SearcherTest.testSearchImpl(
        path, corners, direction, color, location, startCorner
        );

    return result;
  }


  // --------------------------------------------------------------------------------
  // Test Implementations
  // --------------------------------------------------------------------------------

  /**
   * Test the Searcher class's `computeSearchPath()` method.
   *
   * @param expected the search path that the searcher is expected to compute
   * @param enemyLL the lower-left corner of the enemy zone
   * @param enemyUR the upper-right corner of the enemy zone
   * @param searchLL the lower-left corner of the search zone
   * @param searchUR the upper-right corner of the search zone
   * @param location the location at which the searcher is given control of the robot
   *
   * @return true if the searcher generated the the expected search path, false otherwise
   */
  private static boolean testComputeSearchPathImpl(
      Waypoint[] expected, Waypoint enemyLL, Waypoint enemyUR, Waypoint searchLL, Waypoint searchUR, Waypoint location
      ) {

    boolean result = true;

    // Because we are only interested in the search path and will not be calling the Searcher
    // class's `search()` method, it is okay to use 'null' for the constructor arguments.
    Searcher searcher = new Searcher(null, null, null);

    // Initialize the searcher with the controlled input data.
    searcher.setEnemyZone(enemyLL, enemyUR);
    searcher.setSearchZone(searchLL, searchUR);
    searcher.setLocation(location);

    // Since we will not actually be performing the search, we can just set this to 'NONE'.
    searcher.setFlagColor(Searcher.FlagColor.NONE);

    System.out.println("Computing search path...");

    // Get the start time of the computation.
    long t1 = System.currentTimeMillis();

    // Compute the search path.
    searcher.computeSearchPath();

    // Get the end time of the computation.
    long t2 = System.currentTimeMillis();

    // Get a copy of the search path.
    Waypoint[] computed = searcher.getSearchPath();

    if (computed == null) {
      System.out.println("Search path could not be computed.");
      System.out.println("");

      return false;
    } else {
      System.out.println("Search path successfully computed.");
      System.out.println("");
    }

    // Print the expected search path to the console.
    System.out.println("Expected Search Path");
    System.out.println("--------------------");
    for (int i = 0, n = expected.length; i < n; ++i) {
      System.out.println(String.format("{ %.2f, %.2f }", expected[i].x, expected[i].y));
    }
    System.out.println("");

    // Print the computed search path to the console.
    System.out.println("Computed Search Path");
    System.out.println("--------------------");
    for (int i = 0, n = computed.length; i < n; ++i) {
      System.out.println(String.format("{ %.2f, %.2f }", computed[i].x, computed[i].y));
    }
    System.out.println("");

    String seconds = String.format("%d", (t2 - t1) / 1000);
    String milliseconds = String.format("%03d", (t2 - t1) % 1000);

    // Print out the time it took to compute the search path to the console.
    System.out.println("Computation Time: " + seconds + "." + milliseconds + " s");
    System.out.println("");

    // If the length of our computed search path does not match that of our expected one,
    // then our test has failed, since the paths cannot be equal.
    if (computed.length != expected.length) {
      return false;
    }

    // Check that the computed search path matches the expected one.
    for (int i = 0, n = computed.length; i < n; ++i) {
      if (Math.abs(computed[i].x - expected[i].x) >= SearcherTest.EPSILON) {
        result = false;
        break;
      }
      if (Math.abs(computed[i].y - expected[i].y) >= SearcherTest.EPSILON) {
        result = false;
        break;
      }
    }

    return result;
  }

  /**
   * Test the Searcher class's `search()` method.
   *
   * The robot must be placed at the one of the grid corners so that it can first localize
   * before proceeding to perform the search for the enemy flag.
   *
   * @param path the search path that the robot is expected to follow
   * @param corners the indices of `path` which are the LL, UL, UR, and LR corners (-1 if not reachable)
   * @param direction the direction the robot is expected to move during its search
   * @param color the color of the enemy flag
   * @param location the location at which the searcher is given control of the robot
   * @param startCorner the corner (0 - 3) of the grid to which the robot will initially localize
   *
   * @return true if the searcher generated the the expected search path, false otherwise
   */
  private static boolean testSearchImpl(
      Waypoint[] path, int[] corners, Searcher.Direction direction, Searcher.FlagColor color, Waypoint location, int startCorner
      ) {

    boolean result = true;

    // Calculate the position that the robot to which should initially localize.
    Waypoint referencePosition = new Waypoint(location.x, location.y);

    switch (startCorner) {
      case 0:
        referencePosition.x += 0.5;
        referencePosition.y += 0.5;

        break;

      case 1:
        referencePosition.x -= 0.5;
        referencePosition.y += 0.5;

        break;

      case 2:
        referencePosition.x -= 0.5;
        referencePosition.y -= 0.5;

        break;

      case 3:
        referencePosition.x += 0.5;
        referencePosition.y -= 0.5;

        break;

      default:
        String msg = "error: testSearchImpl(): Invalid starting corner";
        System.out.println(msg + ": " + startCorner);

        return false;
    }

    // Wheel motors
    EV3LargeRegulatedMotor motorL = FinalProject.leftMotor;
    EV3LargeRegulatedMotor motorR = FinalProject.rightMotor;

    // Zip-line motor
    EV3LargeRegulatedMotor motorZ = FinalProject.zipMotor;

    // Sensor ports
    Port lsPortL = FinalProject.lsPortl;
    Port lsPortR = FinalProject.lsPortr;
    Port lsPortF = FinalProject.lsPortm;
    Port usPortF = FinalProject.usPort;

    // Initialize left light sensor.
    SensorModes lsSensorL = new EV3ColorSensor(lsPortL);
    SampleProvider lsSampleProviderL = lsSensorL.getMode("Red");
    SampleProvider lsMedianL = new MeanFilter(lsSampleProviderL, lsSampleProviderL.sampleSize());

    // Initialize right light sensor.
    SensorModes lsSensorR = new EV3ColorSensor(lsPortR);
    SampleProvider lsSampleProviderR = lsSensorR.getMode("Red");
    SampleProvider lsMedianR = new MeanFilter(lsSampleProviderR, lsSampleProviderR.sampleSize());

    // Initialize front light sensor.
    SensorModes lsSensorF = new EV3ColorSensor(lsPortF);
    SampleProvider lsSampleProviderF = lsSensorF.getMode("Red");
    SampleProvider lsMedianF = new MeanFilter(lsSampleProviderF, lsSampleProviderF.sampleSize());

    // Initialize front ultrasonic sensor.
    SensorModes usSensorF = new EV3UltrasonicSensor(usPortF);
    SampleProvider usSampleProviderF = usSensorF.getMode("Distance");

    // Create the SensorData object.
    SensorData sd = new SensorData();

    // Create the SensorPoller object.
    SensorPoller sensorPoller = new SensorPoller(lsSampleProviderL, lsSampleProviderR, lsSampleProviderF, usSampleProviderF, sd);

    // Create the Odometer object.
    Odometer odometer = new Odometer(motorL, motorR, FinalProject.WHEEL_RADIUS, FinalProject.WHEEL_BASE);

    // Create the Driver object.
    Driver driver = new Driver(motorL, motorR, motorZ, null /* = frontMotor */);

    // Set the speed at which the Driver should drive the robot.
    driver.setSpeedLeftMotor(FinalProject.SPEED_ROT);
    driver.setSpeedRightMotor(FinalProject.SPEED_ROT);

    // Create the Navigator object.
    Navigator navigator = new Navigator(driver, odometer, sd);

    // Create the UltrasonicLocalizer object.
    UltrasonicLocalizer ultrasonicLocalizer = new UltrasonicLocalizer(driver, odometer, sd);

    // Create the LightLocalizer object.
    LightLocalizer lightLocalizer = new LightLocalizer(driver, odometer, sd);

    // Create the Localizer object.
    Localizer localizer = new Localizer(ultrasonicLocalizer, lightLocalizer, driver);

    // The ultrasonic and light localizers depend on some static variables in the MainController
    // and Localizer classes, so we will manually set these values for our test.
    MainController.is_red = true;
    MainController.redTeamStart = referencePosition;
    MainController.RedCorner = startCorner;
    localizer.setRefPos(referencePosition);

    // Start all data-capturing threads.
    sensorPoller.start();
    odometer.start();

    // Construct the searcher with functional Navigator, Driver, and SensorData parameters.
    Searcher searcher = new Searcher(navigator, driver, sd);

    // The enemy and search zone information is only used for computing the search path,
    // but since we are providing our own search path for the robot to follow, we can just
    // use dummy values here.
    searcher.setEnemyZone(new Waypoint(-1.0, -1.0), new Waypoint(-1.0, -1.0));
    searcher.setSearchZone(new Waypoint(-1.0, -1.0), new Waypoint(-1.0, -1.0));

    // Initialize the searcher with the controlled input data.
    searcher.setSearchPath(path, corners, direction);
    searcher.setLocation(location);
    searcher.setFlagColor(color);

    System.out.println("Performing ultrasonic localization...");

    // First localize using the ultrasonic localizer.
    ultrasonicLocalizer.localize();

    System.out.println("Performing light localization...");

    // Next localize using the light localizer.
    lightLocalizer.localize();

    System.out.println("Localization complete.");
    System.out.println("");

    System.out.println("Searching for enemy flag...");

    // Get the start time of the computation.
    long t1 = System.currentTimeMillis();

    // Perform the search.
    result = searcher.search();

    // Get the end time of the computation.
    long t2 = System.currentTimeMillis();

    if (!result) {
      System.out.println("Flag could not be captured.");
      System.out.println("");
    } else {
      System.out.println("Flag successfully captured.");
      System.out.println("");
    }

    String seconds = String.format("%d", (t2 - t1) / 1000);
    String milliseconds = String.format("%03d", (t2 - t1) % 1000);

    // Print out the time it took to perform the search to the console.
    System.out.println("Search Time: " + seconds + "." + milliseconds + " s");
    System.out.println("");

    return result;
  }

}
