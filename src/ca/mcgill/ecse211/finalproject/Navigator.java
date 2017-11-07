package ca.mcgill.ecse211.finalproject;

/**
 * Handles navigating through sets of waypoints as well as avoiding obstacles when they are encountered.
 */
public class Navigator {

  /**
   * Enum describing the state of the navigator.
   */
  public enum Nav_State { IDLE, ROTATING, MOVING, AVOIDING, REACHED_WAYPOINT, DONE }

  // --------------------------------------------------------------------------------
  // Constants
  // --------------------------------------------------------------------------------


  // --------------------------------------------------------------------------------
  // Variables
  // --------------------------------------------------------------------------------

  private Driver driver;
  private Odometer odometer;
  private SensorData sd;

  /*
   * Navigation variables
   */
  private Nav_State cur_state = Nav_State.IDLE;
  private Waypoint[] path; // The set of waypoints the robot will have to travel, initialized by the setPath() method.
  private Waypoint target_pos = null; // Target waypoint
  private int waypoint_progress = -1; // A counter to keep track of our progress (indexing the path array)
  private double angle_to_target_pos; // Angle between the robot's direction and the target waypoint.
  private double dist_to_target_pos; // Distance to target waypoint.
  private double orientation_vect[] = {0.0, 1.0}; // we initially start with a theta of 90 degrees.
  private double min_dist; // Used while moving, we constantly record the new lowest distance to the target
  // point, when it starts going back up, we know we went past the waypoint.
  private boolean done = false; // This will be set to true when we reach the last waypoint, making to
  // program end the navigation thread.

  /*
   * Obstacle avoidance variables
   */
  private boolean obstacle_detected = false;
  private boolean obstacle_avoided = true;

  /**
   * Constructor
   *
   * @param driver Driver object, handles moving the robot.
   * @param odometer Odometer, used to keep track of the robot's position.
   * @param sd SensorData object, used to get the readings from the sensors.
   */
  public Navigator(Driver driver, Odometer odometer, SensorData sd) {
    this.driver = driver;
    this.odometer = odometer;
    this.sd = sd;
  }

  /**
   * Processes the current state, updates it and returns the new state as a string.
   * This method is package-private, only classes in the same package can access it.d
   *
   * @return The new state of the navigator, as a string.
   */
  String process() {
    updateOrientation();
    switch (cur_state) {
      case IDLE:
        cur_state = process_idle();
        break;
      case ROTATING:
        cur_state = process_rotating();
        break;
      case MOVING:
        cur_state = process_moving();
        break;
      case AVOIDING:
        cur_state = process_avoiding();
        break;
      case REACHED_WAYPOINT:
        cur_state = process_reached();
        break;
      case DONE:
        cur_state = process_done();
        break;
      default: break;
    }
    return cur_state.toString();
  }

  /**
   * Processes the IDLE state of the navigator.
   *
   * @return new state, or same if no need to navigate
   */
  private Nav_State process_idle() {
    // Being idle means we just started, intialize stuff and get started.
    target_pos = getNextWaypoint(); // Get the next waypoint in the array, the first one in this
                                    // case.
    if (target_pos != null) { // Compute the distance and angle to the target position, if rotation is needed, set state to
      // rotating, if not: move.
      done = false;
      updateTargetInfo();
      if (Math.abs(angle_to_target_pos) > 0) {
        return Nav_State.ROTATING;
      } else if (dist_to_target_pos > 0) {
        return Nav_State.MOVING;
      }

      // Fallthrough, shouldn't happen.
      //return Nav_State.IDLE;
    }
    // no more target position = done
    return done ? Nav_State.IDLE : Nav_State.DONE;
    }

  /**
   * Processes the ROTATING state of the navigator.
   *
   * @return new state, or same if the angle to the target waypoint is still too high.
   */
  private Nav_State process_rotating() {
    updateTargetInfo();
    if (Math.abs(angle_to_target_pos) > FinalProject.ANGLE_THRESHOLD) {
      // As long as the angle to the target position is bigger than the threshold, keep rotating.
      driver.rotate(Math.toDegrees(angle_to_target_pos), true);
      return Nav_State.ROTATING;
    } else {
      // If our angle is smaller than the threshold, then we can move, start moving!
      if (dist_to_target_pos > FinalProject.DISTANCE_THRESHOLD) {
        min_dist = Double.MAX_VALUE; // reset
        return Nav_State.MOVING;
      } else {
        // if angle AND distance are both small enough, we reached the point (happens rarely from
        // the ROTATING state).
        return Nav_State.REACHED_WAYPOINT;
      }
    }
  }

  /**
   * Processes the MOVING state of the navigator.
   *
   * @return new state, or same if the distance to the target waypoint is still too high.
   */
  private Nav_State process_moving() {
    updateTargetInfo();
    if (Math.abs(angle_to_target_pos) > FinalProject.ANGLE_THRESHOLD) {
      return Nav_State.ROTATING; // We are a bit off, adjust.
    } else if (dist_to_target_pos < min_dist) {
      min_dist = dist_to_target_pos; // min_dist is continuously updated as long as the distance
                                     // gets smaller.
      if (dist_to_target_pos > FinalProject.DISTANCE_THRESHOLD) {
        driver.moveForward(dist_to_target_pos, true);
        return Nav_State.MOVING;
      } else {
        // if angle AND distance are both small enough, we reached the point
        return Nav_State.REACHED_WAYPOINT;
      }
    } else {
      // We missed the point (dist_to_target_pos > min_dist), turn around and get there!
      if (dist_to_target_pos < 5) {
        driver.moveBackward(dist_to_target_pos, false);
      }
      return Nav_State.ROTATING;
    }
  }

  /**
   * Processes the AVOIDING state of the navigator.
   *
   * @return new state, or same if not done avoiding the obstacle.
   */
  private Nav_State process_avoiding() {
    // TODO: Integrate P-Controller for obstacle avoidance (or whatever works)
    return Nav_State.IDLE;
  }

  /**
   * Processes the REACHED_POINT state of the navigator. Gets the next waypoint and restarts navigation if it exists.
   *
   * @return new state.
   */
  private Nav_State process_reached() {
    updateTargetInfo();
    if (dist_to_target_pos < FinalProject.DISTANCE_THRESHOLD) {
      min_dist = Double.MAX_VALUE; // reset
      driver.rotate(0, true);
      target_pos = getNextWaypoint();
      if (target_pos != null) {
        // rotating, if not: move.
        updateTargetInfo();
        done = false;
        if (Math.abs(angle_to_target_pos) > 0) {
          return Nav_State.ROTATING;
        } else if (dist_to_target_pos > 0) {
          return Nav_State.MOVING;
        }
        // Fallthrough, shouldn't happen.
        return Nav_State.IDLE;
      } else {
        return Nav_State.DONE;
      }
    } else {
      return Nav_State.MOVING; // Will have to improve this.
    }
  }

  /**
   * Processes the DONE state of the navigator. Notifies the main controller that navigation is done and resets
   * the navigation variables
   *
   * @return new state, or same if no need to navigate
   */
  private Nav_State process_done() {
    done = true;
    return Nav_State.IDLE;
  }

  /*
   * Math
   */

  /**
   * Computes the angle between the robot and it's target position, from -PI to PI, so that the
   * robot always makes the smallest turn possible.
   *
   * @param vect_to_pos, a vector from the robot's position to the target position.
   * @return the angle between the two vectors, in radians.
   */
  private double angleToPos(double vect_to_pos[]) {
    return Math.atan2(vect_to_pos[1] * orientation_vect[0] - vect_to_pos[0] * orientation_vect[1],
            orientation_vect[0] * vect_to_pos[0] + orientation_vect[1] * vect_to_pos[1]);
  }

  /**
   * Computes the magnitude of a vector
   *
   * @param v: vector
   * @return the magnitude of the vector.
   */
  private double magnitude(double v[]) {
    return Math.sqrt(Math.pow(v[0], 2) + Math.pow(v[1], 2));
  }

  /**
   * Update all the information regarding out target position:
   * - Vector from robot's current position to target (using odometer and known measurements).
   * - The magnitude of that vector, the distance to our target.
   * - The angle to the traget position, using our orientation unit vector and the vector we just computed.
   */
  private void updateTargetInfo() {
    double x = odometer.getX();
    double y = odometer.getY();

    double dist_x = target_pos.x * FinalProject.BOARD_TILE_LENGTH - x;
    double dist_y = target_pos.y * FinalProject.BOARD_TILE_LENGTH - y;

    double vect_to_target[] = {dist_x, dist_y};
    dist_to_target_pos = magnitude(vect_to_target);
    angle_to_target_pos = angleToPos(vect_to_target);

    if (FinalProject.DEBUG) {
      System.out.println("[NAVIGATION] Current Position: " + x + ", " + y);
      System.out
              .println("[NAVIGATION] Target Position: (" + target_pos.x + "; " + target_pos.y + ")");
      System.out.println("[NAVIGATION] Distance to target: " + dist_to_target_pos);
      System.out.println(
              "[NAVIGATION] Vector to target: [" + vect_to_target[0] + ", " + vect_to_target[1] + "]");
      System.out.println("[NAVIGATION] Angle to target: " + Math.toDegrees(angle_to_target_pos));
    }
  }

  /**
   * Computes a unit vector that points in the robot's orientation, used for determining angles
   * between the robot and the target position at any given time.
   */
  private void updateOrientation() {
    double orientation_angle = odometer.getTheta();
    orientation_vect[0] = Math.cos(orientation_angle);
    orientation_vect[1] = Math.sin(orientation_angle);

    if (FinalProject.DEBUG) {
      System.out.println("[NAVIGATION] Orientation angle: " + Math.toDegrees(orientation_angle));
      System.out.println("[NAVIGATION] Orientation vector: [" + orientation_vect[0] + ", "
              + orientation_vect[1] + "]");
    }
  }

  /*
   * Utility methods
   */

  /**
   * Gets the next waypoint in the path array.
   *
   * @return the next waypoint in the path array.
   */
  private Waypoint getNextWaypoint() {
    if (path != null) {
      if (waypoint_progress + 1 >= path.length) {
        // That's a problem
        if (FinalProject.DEBUG) {
          System.out.println("Error: getting out of bounds of the path array");
        }
        // wait for new path.
        return null;
      }
    } else {
      System.out.println("Path is NULL");
    }
    return path != null ? path[++waypoint_progress] : null;
  }

  /**
   * Gets the target position, as a waypoint.
   *
   * @return current target waypoint
   */
  public Waypoint getTargetPos() {
    return target_pos;
  }

  /*
   * These two methods are meant to guarantee locked access to the obstacle_detected variable for
   * both the navigator and the sensor poller
   */

  /**
   * Returns whether or not an obstacle has been detected,
   *
   * @return boolean obstacle_detected
   */
  public synchronized boolean getObstacleDetected() {
    return obstacle_detected;
  }

  /**
   * Sets the value of the obstacle_detected variable.
   *
   * @param arg a boolean value telling whether or not an obstacle was detected.
   */
  public synchronized void setObstacleDetected(boolean arg) {
    obstacle_detected = arg;
  }

  /**
   * set a new path to navigate
   *
   * @param waypoints an array of waypoints.
   */
  public void setPath(Waypoint[] waypoints) {
    path = waypoints;
    done = false;
    waypoint_progress = -1;
  }

  /**
   * Tells wheter or not the navigator is done navigating.
   *
   * @return a boolean value telling whether or not the navigator is done.
   */
  public boolean isDone() {
    return done;
  }
}
