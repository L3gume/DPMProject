package ca.mcgill.ecse211.finalproject;

import lejos.hardware.Sound;



/**
 * A class to search enemy territory for the flag, which is a block of the specified color. The
 * robot will beep three times upon locating the flag, indicating a successful capture.
 *
 * @author Joshua Inscoe
 */
public class Searcher {

  // --------------------------------------------------------------------------------
  // Constants
  // --------------------------------------------------------------------------------

  // Sleep interval between checking if next waypoint has been reached
  private static final long WAIT_INTERVAL = 1000;

  // Sleep interval between beeps
  private static final long BEEP_INTERVAL = 200;

  // Lower and upper limits on waypoint values (in tiles) [non-inclusive]
  private static final int LOWER_LIMIT_X = 1;
  private static final int LOWER_LIMIT_Y = 1;
  private static final int UPPER_LIMIT_X = 11;
  private static final int UPPER_LIMIT_Y = 11;

  // The direction in which the robot will be moving while following the search path
  public enum Direction {
    UNKNOWN, CLOCKWISE, COUNTER_CLOCKWISE
  };

  // The possible colors of the enemy flag
  public enum FlagColor {
    RED, BLUE, YELLOW, WHITE
  };

  // The mapping between the FlagColor enum values and actual color values
  public static final int[] COLORS = {1, 2, 3, 4};


  // --------------------------------------------------------------------------------
  // Variables
  // --------------------------------------------------------------------------------
 
  // Navigator object for controlling robot navigation
  private Navigator navigator;

  // Driver object for finer control over the robot's movements
  private Driver driver;

  // The current location of the robot
  private Waypoint location;

  // The lower-left and upper-right corners of the search zone (in centimters)
  private Waypoint searchLL;
  private Waypoint searchUR;

  // The lower-left and upper-right corners of the search zone (in tiles)
  private int xLL;
  private int yLL;
  private int xUR;
  private int yUR;

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
  public Searcher(Navigator navigator, Driver driver, Waypoint searchLL, Waypoint searchUR,
      FlagColor color, SensorData sd) {

    this.navigator = navigator;

    this.driver = driver;

    this.location = new Waypoint(-1.0, -1.0);

    this.searchLL = searchLL;
    this.searchUR = searchUR;

    // Convert coordinates from centimeters to tiles.
    this.xLL = (int) (this.searchLL.x / FinalProject.BOARD_TILE_LENGTH);
    this.yLL = (int) (this.searchLL.y / FinalProject.BOARD_TILE_LENGTH);
    this.xUR = (int) (this.searchUR.x / FinalProject.BOARD_TILE_LENGTH);
    this.yUR = (int) (this.searchUR.y / FinalProject.BOARD_TILE_LENGTH);

    // Compute the length and height of the search zone.
    this.length = Math.abs(this.xUR - this.xLL);
    this.height = Math.abs(this.yUR - this.yLL);

    // Compute the total number of tiles surrounding the search zone.
    this.wpCount = (2 * this.length) + (2 * this.height) + 4;

    //
    // NOTE:
    //
    // This must be called after setting the `[xy]LL/UR` and `length` and `height` variables.
    // Calling it before will result in undefined behaviour.
    //
    this.computeWaypoints();

    this.color = color;

    this.sd = sd;

    this.path = null;

    this.direction = Direction.UNKNOWN;
  }


  // --------------------------------------------------------------------------------
  // Methods
  // --------------------------------------------------------------------------------

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
   * Compute the sequence of coordinates to which the robot should travel in search of the enemy
   * flag.
   *
   * This should be called after calling the `setLocation()` method. This should be called before
   * calling the `search()` method.
   */
  public void computeSearchPath() {

    // Are all sides reachable ?
    if (this.reachSideL && this.reachSideT && this.reachSideR && this.reachSideB) {
      //
      // The search zone is _not_ against any wall.
      //

      int pivot = -1;

      Waypoint[] corners =
          new Waypoint[] {this.waypoints[this.cornerLL], this.waypoints[this.cornerUL],
              this.waypoints[this.cornerUR], this.waypoints[this.cornerLR]};

      // Find out which corner of the search zone we are closest to.
      int closestCorner = Searcher.findClosestWaypoint(this.location, corners);

      switch (closestCorner) {
        case 0:
          // Start from the lower-left corner.
          pivot = this.cornerLL;
          break;

        case 1:
          // Start from the upper-left corner.
          pivot = this.cornerUL;
          break;

        case 2:
          // Start from the upper-right corner.
          pivot = this.cornerUR;
          break;

        case 3:
          // Start from the lower-right corner.
          pivot = this.cornerLR;
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
       * 
       */

      String msg = "error: computeSearchPath(): Unsupported search zone";
      System.out.println(msg);

      System.exit(1);

      /*
       *
       * 
       * ---
       */
    }

    // Remove references to all unreachable waypoints, thus allowing the
    // garbage collector to free up any unused memory.
    this.waypoints = null;

    // This is, also, no longer necessary.
    this.valid = null;

    return;
  }

  /**
   * Search enemy territory for the flag.
   *
   * This should be caled after calling the `computeSearchPath()` method.
   *
   * @return true if the flag was successfully "captured" the flag, false otherwise
   */
  public boolean search() {

    boolean found = false;

    double rotateAngle = 0.0;

    // Increment reference counts on sensors.
    this.sd.incrementUSRefs();
    this.sd.incrementColorRefs();

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

        System.exit(1);
    }

    // Navigate to each waypoint in the search path.
    for (int i = 0, n = this.path.length; i < n; ++i) {

      this.navigator.setPath(new Waypoint[] {this.path[i]});
      this.navigator.process();

      // Wait until we have reached the next waypoint.
      while (!this.navigator.isDone()) {
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

        this.driver.rotate(+rotateAngle, false /* = inst_ret */ );

      } else {

        // Look inwards toward the search zone.
        this.driver.rotate(+rotateAngle, false /* = inst_ret */ );

        // Check if we are looking at the flag.
        found = this.checkForFlag();

        if (found) {
          // Capture the flag and end the loop.
          this.captureFlag();

          break;
        }

        // Rotate back to original orientation.
        this.driver.rotate(-rotateAngle, false /* = inst_ret */ );
      }
    }

    // Decrement reference counts on sensors.
    this.sd.decrementUSRefs();
    this.sd.decrementColorRefs();

    return found;
  }


  // --------------------------------------------------------------------------------
  // Helper Routines
  // --------------------------------------------------------------------------------

  /**
   * Compute the coordinates of all tiles surrounding the search zone (even tiles which do not exist
   * or are unreachable by the robot), and place these waypoints into `waypoints` in clockwise
   * order, starting from the lower-left corner.
   *
   * This method also initializes the corner indices, as well as the `valid` boolean array, holding
   * the value of true if a waypoint located at the same index in `waypoints` is reachable, and
   * false otherwise.
   */
  private void computeWaypoints() {

    Waypoint[] waypoints = new Waypoint[this.wpCount];
    boolean[] valid = new boolean[this.wpCount];

    // Indicators signaling whether or not each side of the search zone is reachable
    boolean reachL = (this.xLL > Searcher.LOWER_LIMIT_X);
    boolean reachT = (this.yUR < Searcher.UPPER_LIMIT_Y);
    boolean reachR = (this.xUR < Searcher.UPPER_LIMIT_X);
    boolean reachB = (this.yLL > Searcher.LOWER_LIMIT_Y);

    int index = 0;

    double x;
    double y;

    // Compute the lower-left corner waypoint.
    waypoints[index] = new Waypoint(this.searchLL.x, this.searchLL.y);
    valid[index] = reachB && reachL;
    ++index;

    this.cornerLL = index;

    // Compute the left-side waypoints.
    x = this.searchLL.x - FinalProject.BOARD_TILE_LENGTH;

    for (int i = 0; i < this.height; ++i) {
      // We add 0.5 in order to get a coordinate that is the midpoint between two points.
      y = ((double) (this.yLL + i) + 0.5) * FinalProject.BOARD_TILE_LENGTH;

      waypoints[index] = new Waypoint(x, y);
      valid[index] = reachL;
      ++index;
    }

    // Compute the upper-left corner waypoint.
    waypoints[index] = new Waypoint(this.searchLL.x, this.searchUR.y);
    valid[index] = reachL && reachT;
    ++index;

    this.cornerUL = index;

    // Compute the top-side waypoints.
    y = this.searchUR.y + FinalProject.BOARD_TILE_LENGTH;

    for (int i = 0; i < this.length; ++i) {
      // We add 0.5 in order to get a coordinate that is the midpoint between two points.
      x = ((double) (this.xLL + i) + 0.5) * FinalProject.BOARD_TILE_LENGTH;

      waypoints[index] = new Waypoint(x, y);
      valid[index] = reachT;
      ++index;
    }

    // Compute the upper-right corner waypoint.
    waypoints[index] = new Waypoint(this.searchUR.x, this.searchUR.y);
    valid[index] = reachT && reachR;
    ++index;

    this.cornerUR = index;

    // Compute the right-side waypoints.
    x = this.searchUR.x + FinalProject.BOARD_TILE_LENGTH;

    for (int i = 0; i < this.height; ++i) {
      // We subtract 0.5 in order to get a coordinate that is the midpoint between two points.
      y = ((double) (this.yUR - i) - 0.5) * FinalProject.BOARD_TILE_LENGTH;

      waypoints[index] = new Waypoint(x, y);
      valid[index] = reachR;
      ++index;
    }

    // Compute the lower-right corner waypoint.
    waypoints[index] = new Waypoint(this.searchUR.x, this.searchLL.y);
    valid[index] = reachR && reachB;
    ++index;

    this.cornerLR = index;

    // Compute the bottom-side waypoints.
    y = this.searchLL.y - FinalProject.BOARD_TILE_LENGTH;

    for (int i = 0; i < this.length; ++i) {
      // We subtract 0.5 in order to get a coordinate that is the midpoint between two points.
      x = ((double) (this.xUR - i) - 0.5) * FinalProject.BOARD_TILE_LENGTH;

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
   * Query the SensorData object for data from the front light and ultrasonic sensors to determine
   * whether or not we are currently looking at the enemy flag.
   *
   * @return true if we are looking at the flag, false otherwise
   */
  private boolean checkForFlag() {
    sd.incrementColorRefs();
    
    int target_color = MainController.is_red ? MainController.OR : MainController.OG;
    int cur_color = sd.getColorDataLatest();
    
    
    sd.decrementColorRefs();
    return false;
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
   * Swap all waypoints before index, `pivot`, with all other starting at `pivot`, and update the
   * indices of the search zone corners.
   *
   * @param pivot the index at which to swap
   */
  private void shiftWaypoints(int pivot) {

    waypoints = new Waypoint[this.wpCount];

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
