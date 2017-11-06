package ca.mcgill.ecse211.finalproject;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * Handles aligning with, crossing, and dismounting the zip line.
 *
 * @author Alex Hale
 * @author Justin Tremblay
 * @author Josh Inscoe
 */
public class ZipLine {

  /**
   * Enum describing the current state of the zip line controller
   */
  public enum Zip_State {
    IDLE, ALIGNING, MOVING, ZIPLINING, DONE
  }

  // --------------------------------------------------------------------------------
  // Object instances of other classes
  // --------------------------------------------------------------------------------
  private EV3LargeRegulatedMotor zipMotor;
  private Driver driver;
  private Odometer odometer;
  private SensorData sd;

  // --------------------------------------------------------------------------------
  // Constants
  // --------------------------------------------------------------------------------


  // --------------------------------------------------------------------------------
  // Variables
  // --------------------------------------------------------------------------------
  private Zip_State cur_state = Zip_State.IDLE;
  private boolean done = false;
  private int floor_filter = 0;

  /**
   * Constructor
   *
   * @param zipMotor Motor mounted on top of the robot.
   * @param driver Driver object, handles moving the robot.
   * @param sd SensorData object, gives access to the light sensor data.
   */
  public ZipLine(EV3LargeRegulatedMotor zipMotor, Driver driver, SensorData sd) {
    this.zipMotor = zipMotor;
    this.driver = driver;
    this.sd = sd;
  }

  /**
   * Processes the current state, updates it and returns the new state as a string.
   * This method is package-private, only classes in the same package can access it.
   *
   * @return The new state, as a string.
   */
  String process() {
    switch (cur_state) {
      case IDLE:
        cur_state = process_idle();
        break;
      case ALIGNING:
        cur_state = process_aligning();
        break;
      case MOVING:
        cur_state = process_moving();
        break;
      case ZIPLINING:
        cur_state = process_ziplining();
        break;
      case DONE:
        cur_state = process_done();
        break;
    }
    return cur_state.toString();
  }

  /**
   * Process the IDLE state. Move to ALIGNING state if haven't crossed yet, otherwise
   * wait here.
   *
   * @return ALIGNING state if not yet crossed, IDLE state if crossed
   */
  private Zip_State process_idle() {
    // no need to suspend the odometer - we'll re-localize after crossing
    
    if (!done) {
      return Zip_State.ALIGNING;   // haven't crossed yet => go to alignment
    } else {
      return Zip_State.IDLE;      // already crossed => chill here
    }
  }

  /**
   * Process the ALIGNING state. Align the robot with the zip line, then send
   * it to MOVING.
   *
   * @return MOVING state if aligned, ALIGNING state if not yet aligned
   */
  private Zip_State process_aligning() {
	// check the error between our current heading and the zipline start point
    double err_theta = angleToPos(odometer, FinalProject.ZIPLINE_START_POS);	// TODO this value is passed in over WiFi, might be a different variable
    
    if (Math.abs(err_theta) > FinalProject.ZIPLINE_ORIENTATION_THRESHOLD) {
    	driver.rotate(err_theta, false);	// rotate to new vector and wait until finished rotating
        return Zip_State.ALIGNING;			// then redo this state to check if we're aligned
    } else {
      return Zip_State.MOVING;		// once we're satisfactorily aligned, ~move~ on to moving
    }
  }

  /**
   * Process the MOVING state. Move the robot onto the starting portion 
   * of the zip line.
   *
   * @return IDLE state if missed the zipline, ZIPLING if we made it on
   */
  private Zip_State process_moving() {
    // start the zipline motor and move forward two blocks
    driver.startTopMotor();
    driver.moveForward(2*FinalProject.BOARD_TILE_LENGTH, false);	// wait while moving
    
    // if we're still on the ground, we missed the zip line - navigate back to start of zip line
    if (sd.getLLDataLatest(1) > FinalProject.FLOOR_LIGHT_READING) {
    	// TODO leave this class, go back to navigating, navigate to start of zip line
    	return Zip_State.IDLE;
    } else {
    	// we're on the zip line!
    	return Zip_State.ZIPLINING;
    }
  }

  /**
   * Process the ZIPLINING state. Detect when the robot has landed, move away from the zip line
   * and head to the DONE state.
   *
   * @return ZIPLINING if we're still ziplining, DONE if we're off the zip line
   */
  private Zip_State process_ziplining() {
	  // if we're getting light readings, we're approaching the floor
	  if (sd.getLLDataLatest(1) > FinalProject.FLOOR_LIGHT_READING) {
	      if (floor_filter < FinalProject.FLOOR_READING_FILTER) {
	        // make sure that we're not getting erroneous readings
	    	  	// we really don't want to be stranded on the zip line!
	    	floor_filter++;
	        return Zip_State.ZIPLINING;
	      } else {
	        // we've arrived at the end of the zipline, and the wheels should be touching the ground
	        driver.moveForward(FinalProject.BOARD_TILE_LENGTH * 2, false); 	// move away from the zipline
	        driver.stopBoth(); 													// stop the main motors
	        return Zip_State.DONE;
	      }
      } else {
    	  // if we're not near the floor, keep ziplining
    	  return Zip_State.ZIPLINING;
      }
  }

  /**
   * Process the DONE state. Stop the zipline motor, reset a few variables, then go back to IDLE.
   *
   * @return IDLE state
   */
  private Zip_State process_done() {
    done = true;
    floor_filter = 0;			// reset the floor filter in case we need to do this again
    return Zip_State.IDLE;
  }

  /**
   * Tells whether or not we are done crossing the zipline.
   *
   * @return boolean telling whether or not we are done.
   */
  public boolean isDone() {
    return done;
  }
  
  /**
   * Helper method that computes the angle difference between the robot's 
   * current position and a waypoint.
   * 
   * @param odo current odometer position
   * @param _pos waypoint we're checking against
   * @return the angle between our current heading and the waypoint
   */
  public static double angleToPos(Odometer odo, Waypoint _pos) {
    // TODO verify the logic in this function - it was just copy-pasted from Lab 5
	double orientation_angle = odo.getTheta();
    double orientation_vect[] = new double[] {Math.cos(orientation_angle), Math.sin(orientation_angle)};
    double vect_to_pos[] = new double [] {_pos.x * FinalProject.BOARD_TILE_LENGTH - odo.getX(), _pos.y * FinalProject.BOARD_TILE_LENGTH - odo.getY()};
    double angle = Math.atan2(vect_to_pos[1] * orientation_vect[0] - vect_to_pos[0] * orientation_vect[1],
        orientation_vect[0] * vect_to_pos[0] + orientation_vect[1] * vect_to_pos[1]);

    return angle;
  }
}
