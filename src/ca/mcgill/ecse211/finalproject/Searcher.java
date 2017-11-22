package ca.mcgill.ecse211.finalproject;

import lejos.hardware.Sound;



/**
 * A class to search enemy territory for the flag, which is a block of the specified color.
 * The robot will beep three times upon locating the flag, indicating a successful capture.
 *
 * @author Joshua Inscoe
 */
public class Searcher {

  // --------------------------------------------------------------------------------
  // Constants
  // --------------------------------------------------------------------------------

  // Sleep interval between checking if next waypoint has been reached
  private static final long WAIT_INTERVAL = 40;

  // Sleep interval to allow sensor data to stabalize
  private static final long STABALIZE_INTERVAL = 500;

  // Sleep interval between beeps
  private static final long BEEP_INTERVAL = 200;

  // The distance (in tiles) that the robot should stay away from the search zone
  private static final double DISTANCE_TO_SEARCH_ZONE = 0.5;

  // The maximum distance (in centimeters) that the robot will move into the search zone
  private static final double CAPTURE_DISTANCE_THRESHOLD = 25.0;

  // The amount of acceptable error that is allowed in the flag color readings
  private static final double COLOR_ERROR = 0.2;

  // The direction in which the robot will be moving while following the search path
  public enum Direction {
    UNKNOWN,
    CLOCKWISE,
    COUNTER_CLOCKWISE
  };

  // The possible colors of the enemy flag
  public enum FlagColor {
    NONE, RED, BLUE, YELLOW, WHITE
  };

  // The mapping between the FlagColor enum values and actual color values
  public static final float[] COLORS = { -1.0f, 0.0f, 7.0f, 3.0f, 6.0f };


  // --------------------------------------------------------------------------------
  // Variables
  // --------------------------------------------------------------------------------

  // Navigator object for controlling robot navigation
  private Navigator navigator;

  // Driver object for finer control over the robot's movements
  private Driver driver;

  // The current location of the robot
  private Waypoint location;

  // The lower-left and upper-right corners of the enemy zone (in tiles)
  private Waypoint enemyLL;
  private Waypoint enemyUR;

  // Lower and upper limits on waypoint values (in tiles) [inclusive]
  private double lowerLimitX;
  private double lowerLimitY;
  private double upperLimitX;
  private double upperLimitY;

  // The lower-left and upper-right corners of the search zone (in tiles)
  private Waypoint searchLL;
  private Waypoint searchUR;

  // The length and height of the search zone
  private int length;
  private int height;

  // The total number of waypoints (i.e. tiles surrounding the search zone)
  private int wpCount;

  // All waypoints sequenced clockwise starting from the lower-left corner of the search zone
  private Waypoint[] waypoints;

  // True or false indicators signaling whether each side of the search zone is reachable
  private boolean reachSideL;
  private boolean reachSideT;
  private boolean reachSideR;
  private boolean reachSideB;

  // True or false indicators signaling whether the waypoint at the same index is valid
  private boolean[] valid;

  // Index of each of the corners in `waypoints`
  private int cornerLL;
  private int cornerUL;
  private int cornerUR;
  private int cornerLR;

  // The color of the enemy flag
  private FlagColor color;

  // SensorData object for receiving sensor data
  private SensorData sd;

  // The sequence of waypoints to follow when searching for the flag
  private Waypoint[] path;

  // The direction in which the robot will be traveling (clockwise or counter-clockwise)
  private Direction direction;


  // --------------------------------------------------------------------------------
  // Constructors
  // --------------------------------------------------------------------------------

  /**
   * Constructor
   *
   * @param sd SensorData object to access sensor data
   */
  public Searcher(Navigator navigator, Driver driver, SensorData sd) {

    this.navigator = navigator;

    this.driver = driver;

    this.enemyLL = new Waypoint(-1.0, -1.0);
    this.enemyUR = new Waypoint(-1.0, -1.0);

    // The default values will be the entire 12x12 grid.
    this.lowerLimitX = 0.5;
    this.lowerLimitY = 0.5;
    this.upperLimitX = 11.5;
    this.upperLimitY = 11.5;

    this.searchLL = new Waypoint(-1.0, -1.0);
    this.searchUR = new Waypoint(-1.0, -1.0);

    this.location = new Waypoint(-1.0, -1.0);

    this.length = -1;
    this.height = -1;

    this.wpCount = -1;

    this.waypoints = null;

    this.reachSideL = false;
    this.reachSideT = false;
    this.reachSideR = false;
    this.reachSideB = false;

    this.valid = null;

    this.cornerLL = -1;
    this.cornerUL = -1;
    this.cornerUR = -1;
    this.cornerLR = -1;

    this.color = FlagColor.NONE;

    this.sd = sd;

    this.path = null;

    this.direction = Direction.UNKNOWN;
  }


  // --------------------------------------------------------------------------------
  // Methods
  // --------------------------------------------------------------------------------

  /**
   * Set the coordinates of the enemy zone.
   *
   * This should be called before calling the `computeSearchPath()` method.
   *
   * @param enemyLL the lower-left corner of the enemy zone
   * @param enemyUR the upper-right corner of the enemy zone
   */
  public void setEnemyZone(Waypoint enemyLL, Waypoint enemyUR) {

    this.enemyLL.x = enemyLL.x;
    this.enemyLL.y = enemyLL.y;

    this.enemyUR.x = enemyUR.x;
    this.enemyUR.y = enemyUR.y;

    return;
  }

  /**
   * Set the coordinates of the search zone.
   *
   * This should be called before calling the `computeSearchPath()` method.
   *
   * @param searchLL the lower-left corner of the search zone
   * @param searchUR the upper-right corner of the search zone
   */
  public void setSearchZone(Waypoint searchLL, Waypoint searchUR) {

    this.searchLL.x = searchLL.x;
    this.searchLL.y = searchLL.y;

    this.searchUR.x = searchUR.x;
    this.searchUR.y = searchUR.y;

    return;
  }

  /**
   * Set the current location of the robot.
   *
   * This should be called before calling the `computeSearchPath()` method.
   *
   * @param location the current location of the robot
   */
  public void setLocation(Waypoint location) {

    this.location.x = location.x;
    this.location.y = location.y;

    return;
  }

  /**
   * Compute the sequence of coordinates to which the robot should travel
   * in search of the enemy flag.
   *
   * This should be called after calling the `setLocation()` method.
   * This should be called after calling the `setSearchZone()` method.
   * This should be called before calling the `search()` method.
   */
  public void computeSearchPath() {

    // Compute the lower/upper limits on the x/y-positions based on the enemy zone.
    this.lowerLimitX = this.enemyLL.x + 0.5;
    this.lowerLimitY = this.enemyLL.y + 0.5;
    this.upperLimitX = this.enemyUR.x - 0.5;
    this.upperLimitY = this.enemyUR.y - 0.5;

    // Compute the length and height of the search zone.
    this.length = Math.abs((int)this.searchUR.x - (int)this.searchLL.x);
    this.height = Math.abs((int)this.searchUR.y - (int)this.searchLL.y);

    // Compute the total number of tiles surrounding the search zone.
    this.wpCount = (2 * this.length) + (2 * this.height) + 4;

    //
    // NOTE:
    //
    // This must be called after setting the `[xy]LL/UR` and `length` and `height` variables.
    // Calling it before will result in undefined behaviour.
    //
    this.computeWaypoints();

    // Are all sides reachable ?
    if (this.reachSideL && this.reachSideT && this.reachSideR && this.reachSideB) {
      //
      // The search zone is _not_ against any wall.
      //

      int pivot = -1;

      Waypoint[] corners = new Waypoint[] {
        this.waypoints[this.cornerLL], this.waypoints[this.cornerUL],
        this.waypoints[this.cornerUR], this.waypoints[this.cornerLR]
      };

      // Find out which corner of the search zone we are closest to.
      int closestCorner = Searcher.findClosestWaypoint(this.location, corners);

      switch (closestCorner) {
        case 0:
          // Start from the lower-left corner.
          pivot = (this.cornerLL - 1 + this.wpCount) % this.wpCount;
          break;

        case 1:
          // Start from the upper-left corner.
          pivot = (this.cornerUL - 1 + this.wpCount) % this.wpCount;
          break;

        case 2:
          // Start from the upper-right corner.
          pivot = (this.cornerUR - 1 + this.wpCount) % this.wpCount;
          break;

        case 3:
          // Start from the lower-right corner.
          pivot = (this.cornerLR - 1 + this.wpCount) % this.wpCount;
          break;

        default:
          // This should be unreachable, but log an error and exit if we do somehow get here.
          String msg = "error: computeSearchPath(): Unknown closest corner";
          System.out.println(msg + ": " + closestCorner);

          System.exit(1);
      }

      // Shift the waypoints such that the waypoint at index zero is the
      // initial waypoint to which we need to navigate.
      this.shiftWaypoints(pivot);

      this.path = this.waypoints;

      this.direction = Direction.CLOCKWISE;

    } else {
      //
      // The search zone is located against (at least) one wall.
      //

      // TODO

      /*
       * TEMPORARY
       *

       */

      String msg = "error: computeSearchPath(): Unsupported search zone";
      System.out.println(msg);

      this.path = null;

      this.direction = Direction.UNKNOWN;

      /*

       *
       * ---
       */
    }

    // --- DEBUG ---

    for (int i = 0, n = this.path.length; i < n; ++i) {
      System.out.println("{ " + this.path[i].x + ", " + this.path[i].y + " }");
    }

    // --- DEBUG ---

    // Remove references to all unreachable waypoints, thus allowing the
    // garbage collector to free up any unused memory.
    this.waypoints = null;

    // This is, also, no longer necessary.
    this.valid = null;

    return;
  }

  /**
   * Set the color of the enemy flag.
   *
   * This should be called before calling the `search()` method.
   *
   * @param color the color of the enemy flag
   */
  public void setFlagColor(FlagColor color) {

    this.color = color;

    return;
  }

  /**
   * Search enemy territory for the flag.
   *
   * This should be called after calling the `computeSearchPath()` method.
   * This should be called after calling the `setFlagColor()` method.
   *
   * @return true if the flag was successfully "captured", false otherwise
   */
  public boolean search() {

    boolean found = false;

    double rotateAngle = 0.0;

    // First assert that we have already computed the search path.
    if (this.path == null) {
      String msg = "error: search(): Missing search path";
      System.out.println(msg);
      return false;
    }

    // Set the angle we will need to rotate in order to make turns around corners
    // and to look inwards toward the search zone.
    switch (this.direction) {
      case CLOCKWISE:
        rotateAngle = -90.0;
        break;

      case COUNTER_CLOCKWISE:
        rotateAngle = +90.0;
        break;

      default:
        // UNKNOWN
        String msg = "error: search(): Unknown direction";
        System.out.println(msg);

        return false;
    }

    // Increment reference counts on sensors.
    this.sd.incrementLLRefs();
    this.sd.incrementUSRefs();

    // Navigate to each waypoint in the search path.
    for (int i = 0, n = this.path.length; i < n; ++i) {

      this.navigator.setPath(new Waypoint[] { this.path[i] });

      // Wait until we have reached the next waypoint.
      while (!this.navigator.isDone()) {
        this.navigator.process();
        try {
          // Sleep a little bit to yield processor to other threads while waiting
          // for the Navigator to finish navigating to the next waypoint.
          Thread.sleep(Searcher.WAIT_INTERVAL);
        } catch (Exception e) {
          // ...
        }
      }

      // Have we reached a corner ?
      if (i == this.cornerLL || i == this.cornerUL || i == this.cornerUR || i == this.cornerLR) {

        this.driver.rotate(+rotateAngle, false /* = inst_ret */);

      } else {

        // Look inwards toward the search zone.
        this.driver.rotate(+rotateAngle, false /* = inst_ret */);

        // Check if we are looking at the flag.
        found = this.checkForFlag();

        if (found) {
          // Capture the flag and end the loop.
          this.captureFlag();

          break;
        }

        // Rotate back to original orientation.
        this.driver.rotate(-rotateAngle, false /* = inst_ret */);
      }
    }

    // Decrement reference counts on sensors.
    this.sd.decrementLLRefs();
    this.sd.decrementUSRefs();

    return found;
  }


  // --------------------------------------------------------------------------------
  // Helper Routines
  // --------------------------------------------------------------------------------

  /**
   * Compute the coordinates of all tiles surrounding the search zone (even tiles which do not
   * exist or are unreachable by the robot), and place these waypoints into `waypoints` in
   * clockwise order, starting from the lower-left corner.
   *
   * This method also initializes the corner indices, as well as the `valid` boolean array,
   * holding the value of true if a waypoint located at the same index in `waypoints` is
   * reachable, and false otherwise.
   */
  private void computeWaypoints() {

    Waypoint[] waypoints = new Waypoint[this.wpCount];
    boolean[] valid = new boolean[this.wpCount];

    // Indicators signaling whether or not each side of the search zone is reachable
    boolean reachL = (this.searchLL.x >= this.lowerLimitX);
    boolean reachT = (this.searchUR.y <= this.upperLimitY);
    boolean reachR = (this.searchUR.x <= this.upperLimitX);
    boolean reachB = (this.searchLL.y >= this.lowerLimitY);

    int index = 0;

    double x;
    double y;

    // Compute the lower-left corner waypoint.
    x = this.searchLL.x - Searcher.DISTANCE_TO_SEARCH_ZONE;
    y = this.searchLL.y - Searcher.DISTANCE_TO_SEARCH_ZONE;
    waypoints[index] = new Waypoint(x, y);
    valid[index] = reachB && reachL;
    ++index;

    this.cornerLL = index;

    // Compute the left-side waypoints.
    x = this.searchLL.x - Searcher.DISTANCE_TO_SEARCH_ZONE;

    for (int i = 0; i < this.height; ++i) {
      // We add 0.5 in order to get a coordinate that is the midpoint between two points.
      y = this.searchLL.y + i + 0.5;

      waypoints[index] = new Waypoint(x, y);
      valid[index] = reachL;
      ++index;
    }

    // Compute the upper-left corner waypoint.
    x = this.searchLL.x - Searcher.DISTANCE_TO_SEARCH_ZONE;
    y = this.searchUR.y + Searcher.DISTANCE_TO_SEARCH_ZONE;
    waypoints[index] = new Waypoint(x, y);
    valid[index] = reachL && reachT;
    ++index;

    this.cornerUL = index;

    // Compute the top-side waypoints.
    y = this.searchUR.y + Searcher.DISTANCE_TO_SEARCH_ZONE;

    for (int i = 0; i < this.length; ++i) {
      // We add 0.5 in order to get a coordinate that is the midpoint between two points.
      x = this.searchLL.x + i + 0.5;

      waypoints[index] = new Waypoint(x, y);
      valid[index] = reachT;
      ++index;
    }

    // Compute the upper-right corner waypoint.
    x = this.searchUR.x + Searcher.DISTANCE_TO_SEARCH_ZONE;
    y = this.searchUR.y + Searcher.DISTANCE_TO_SEARCH_ZONE;
    waypoints[index] = new Waypoint(x, y);
    valid[index] = reachT && reachR;
    ++index;

    this.cornerUR = index;

    // Compute the right-side waypoints.
    x = this.searchUR.x + Searcher.DISTANCE_TO_SEARCH_ZONE;

    for (int i = 0; i < this.height; ++i) {
      // We subtract 0.5 in order to get a coordinate that is the midpoint between two points.
      y = this.searchUR.y - i - 0.5;

      waypoints[index] = new Waypoint(x, y);
      valid[index] = reachR;
      ++index;
    }

    // Compute the lower-right corner waypoint.
    x = this.searchUR.x + Searcher.DISTANCE_TO_SEARCH_ZONE;
    y = this.searchLL.y - Searcher.DISTANCE_TO_SEARCH_ZONE;
    waypoints[index] = new Waypoint(x, y);
    valid[index] = reachR && reachB;
    ++index;

    this.cornerLR = index;

    // Compute the bottom-side waypoints.
    y = this.searchLL.y - Searcher.DISTANCE_TO_SEARCH_ZONE;

    for (int i = 0; i < this.length; ++i) {
      // We subtract 0.5 in order to get a coordinate that is the midpoint between two points.
      x = this.searchUR.x - i - 0.5;

      waypoints[index] = new Waypoint(x, y);
      valid[index] = reachB;
      ++index;
    }

    this.waypoints = waypoints;

    this.reachSideL = reachL;
    this.reachSideT = reachT;
    this.reachSideR = reachR;
    this.reachSideB = reachB;

    this.valid = valid;

    return;
  }

  /**
   * Query the SensorData object for data from the front light and ultrasonic sensors to
   * determine whether or not we are currently looking at the enemy flag.
   *
   * @return true if we are looking at the flag, false otherwise
   */
  private boolean checkForFlag() {

    float[] usData = null;

    while (usData == null) {
      try {
        // Sleep a little bit so that the ultrasonic sensor data has time to stabalize.
        Thread.sleep(Searcher.STABALIZE_INTERVAL);
      } catch (Exception e) {
        // ...
      }

      // It is unlikely that this will return 'null', but still check.
      usData = this.sd.getUSData();
    }

    float distance = 0.0f;

    // Compute the average of the stabalized data value received from the ultrasonic sensor
    // to detect (1) whether or not there is an obstacle in front of us, and (2) the color of
    // the obstacle (if any).
    for (int i = 0, n = usData.length; i < n; ++i) {
      distance += usData[i];
    }

    distance /= usData.length;

    if (distance > Searcher.CAPTURE_DISTANCE_THRESHOLD) {
      // The object (if there even is one) is too far away to be checked.
      return false;
    }

    //
    // NOTE:
    //
    // This is necessary because our front-mounted light sensor only returns valid data
    // when it is very close to the object whose color it is trying to detect.
    //

    // Move forward, right up to object.
    this.driver.moveForward(distance, false /* = inst_ret */);

    //
    // ---
    //

    float[] llData = null;

    while (llData == null) {
      try {
        // Sleep a little bit so that the ultrasonic sensor data has time to stabalize.
        Thread.sleep(Searcher.STABALIZE_INTERVAL);
      } catch (Exception e) {
        // ...
      }

      // It is unlikely that this will return 'null', but still check.
      llData = this.sd.getUSData();
    }

    float color = 0.0f;

    // Compute the average of the stabalized data value received from the light sensor
    // to get a more accurate value of the color in front of us.
    for (int i = 0, n = llData.length; i < n; ++i) {
      color += llData[i];
    }

    color /= llData.length;

    // Check if color value is too low.
    if (color < Searcher.COLORS[this.color.ordinal()] - Searcher.COLOR_ERROR) {
      // Move backward, away from the object.
      this.driver.moveBackward(distance, false /* = inst_ret */);

      return false;
    }
    // Check if color value is too high.
    if (color > Searcher.COLORS[this.color.ordinal()] + Searcher.COLOR_ERROR) {
      // Move backward, away from the object.
      this.driver.moveBackward(distance, false /* = inst_ret */);

      return false;
    }

    return true;
  }

  /**
   * Beep three times, signaling that the flag has been "captured".
   */
  private void captureFlag() {

    // Beep 1
    Sound.beep();

    try {
      // Wait a little bit to give times between the beeps.
      Thread.sleep(Searcher.BEEP_INTERVAL);
    } catch (Exception e) {
      // ...
    }

    // Beep 2
    Sound.beep();

    try {
      // Wait a little bit to give times between the beeps.
      Thread.sleep(Searcher.BEEP_INTERVAL);
    } catch (Exception e) {
      // ...
    }

    // Beep 3
    Sound.beep();

    return;
  }

  /**
   * Swap all waypoints before index, `pivot`, with all other starting at `pivot`,
   * and update the indices of the search zone corners.
   *
   * @param pivot the index at which to swap
   */
  private void shiftWaypoints(int pivot) {

    Waypoint[] waypoints = new Waypoint[this.wpCount];

    // Swap all waypoints before index, `pivot`, with all those starting at `pivot`.
    for (int i = 0; i < this.wpCount; ++i) {
      waypoints[i] = this.waypoints[(pivot + i) % this.wpCount];
    }

    this.waypoints = waypoints;

    // Update the indices of the search zone corners.
    this.cornerLL = ((this.cornerLL - pivot) + this.wpCount) % this.wpCount;
    this.cornerUL = ((this.cornerUL - pivot) + this.wpCount) % this.wpCount;
    this.cornerUR = ((this.cornerUR - pivot) + this.wpCount) % this.wpCount;
    this.cornerLR = ((this.cornerLR - pivot) + this.wpCount) % this.wpCount;

    return;
  }

  /**
   * Find the waypoint in `to` that is the shortest distance from the waypoint, `from`.
   *
   * @param from the waypoint from which to calculate distances
   * @param to the array of waypoints to which to calculate distances
   *
   * @return the index in `to` of the closest waypoint
   */
  private static int findClosestWaypoint(Waypoint from, Waypoint[] to) {

    double distanceMinimum;
    double distance;

    int index = -1;

    distanceMinimum = Double.MAX_VALUE;

    for (int i = 0, n = to.length; i < n; ++i) {
      // Compute the distance from `from` to the current waypoint in `to`.
      distance = Searcher.computeDistance(from, to[i]);

      if (distance < distanceMinimum) {
        // Set the new minimum distance, and update the index of the closest waypoint.
        distanceMinimum = distance;
        index = i;
      }
    }

    return index;
  }

  /**
   * Compute the distance between waypoints `a` and `b`.
   *
   * @param a the first waypoint
   * @param b the second waypoint
   *
   * @return the distance between the two waypoints
   */
  private static double computeDistance(Waypoint a, Waypoint b) {

    return Math.sqrt(Math.pow((b.x - a.x), 2.0) + Math.pow((b.y - a.y), 2.0));
  }

}
