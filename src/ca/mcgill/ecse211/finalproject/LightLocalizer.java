package ca.mcgill.ecse211.finalproject;

import lejos.hardware.Sound;

/**
 * Performs the light localization
 *
 * @author Justin Tremblay
 * @author Josh Inscoe
 */
public class LightLocalizer {

  // --------------------------------------------------------------------------------
  // Constants
  // --------------------------------------------------------------------------------


  // --------------------------------------------------------------------------------
  // Variables
  // --------------------------------------------------------------------------------

  private Driver dr;
  private Odometer odo;
  private SensorData sd;

  private Waypoint ref_pos;

  /**
   * Constructor
   *
   * @param dr driver object, used for moving the robot.
   * @param odo odometer, used to get angle variation between readings and to inject to the new
   *        value into.
   * @param sd SensorData object, used to get the sensor readings.
   */
  public LightLocalizer(Driver dr, Odometer odo, SensorData sd) {
    this.dr = dr;
    this.odo = odo;
    this.sd = sd;
  }


  /**
   * Performs light localization and injects the new coordinates in the odometer.
   */
  public void localize() {
    boolean found_y = false;
    boolean found_x = false;
    boolean left_stopped = false;
    boolean right_stopped = false;
    boolean turned = false;
    boolean forward = true;
   
    long started_moving_t = 0;

    sd.incrementLLRefs(); // increment the references to the light poller to make the sensorData
                          // start gathering data.
    sleepThread(1); // wait to make sure the sensorData class has time to get some data.
    ref_pos = Localizer.getRefPos(); // Get the reference position from the Localizer class.
    if (ref_pos != FinalProject.DEBUG_START_POS) {
      double ang = Math.toRadians(45) - odo.getTheta();
      if (ang < -Math.toRadians(180)) {
        ang = Math.toRadians(360) + ang;
      }
      dr.rotate(Math.toDegrees(ang), false); // align to 45
      dr.moveBackward(7, false);
    }
    //ref_angle = getReferenceAngle();
    
    double align_ang = -odo.getTheta();
    if (align_ang < -Math.toRadians(180)) {
      align_ang = Math.toRadians(360) + align_ang;
    }
    dr.rotate(Math.toDegrees(align_ang), false); // align to 0

    dr.setSpeedLeftMotor(FinalProject.SPEED_FWD / 2);
    dr.setSpeedRightMotor(FinalProject.SPEED_FWD / 2);
    dr.endlessMoveForward();
    started_moving_t = System.currentTimeMillis();
    while (!(found_y && found_x)) {
      if (!found_y) {
        if (sd.getLLDataLatest(1) < FinalProject.LIGHT_LEVEL_THRESHOLD && !left_stopped) {
          // Left sensor hit the line.
          dr.stopLeftWheel();
          left_stopped = true;
        }
        if (sd.getLLDataLatest(2) < FinalProject.LIGHT_LEVEL_THRESHOLD && !right_stopped) {
          // Right sensor hit the line.
          dr.stopRightWheel();
          right_stopped = true;
        }
        if (left_stopped && right_stopped) {
          Sound.twoBeeps();
          found_y = true;
          left_stopped = false;
          right_stopped = false;
          odo.setX(ref_pos.x * FinalProject.BOARD_TILE_LENGTH - FinalProject.LIGHT_SENSOR_OFFSET);
          odo.setTheta(0);
        }
      } else if (found_y && !found_x) {
        if (!turned) {
          turned = true;
          dr.moveBackward(10, false);
          dr.rotate(90, false);
          dr.setSpeedLeftMotor(FinalProject.SPEED_FWD / 2);
          dr.setSpeedRightMotor(FinalProject.SPEED_FWD / 2);
          dr.endlessMoveForward();
          started_moving_t = System.currentTimeMillis();
        }
        if (sd.getLLDataLatest(1) < FinalProject.LIGHT_LEVEL_THRESHOLD && !left_stopped) {
          // Left sensor hit the line.
          dr.stopLeftWheel();
          left_stopped = true;
        }
        if (sd.getLLDataLatest(2) < FinalProject.LIGHT_LEVEL_THRESHOLD && !right_stopped) {
          dr.stopRightWheel();
          right_stopped = true;
        }
        if (left_stopped && right_stopped) {
          Sound.twoBeeps();
          found_x = true;
          left_stopped = false;
          right_stopped = false;
          odo.setY(ref_pos.y * FinalProject.BOARD_TILE_LENGTH - FinalProject.LIGHT_SENSOR_OFFSET);
          odo.setTheta(Math.toRadians(90));
        }
      }

      /*
       * FAILSAFE - TODO: more testing.
       * 
       * if the robot hasn't found a line after 4 seconds, go backwards. Helps covering the cases
       * where the robot isn't in the bottom left quadrant (the ref position being the origin)
       */
      if ((!found_y || !found_x)
          && System.currentTimeMillis() - started_moving_t > FinalProject.MOVE_TIME_THRESHOLD) {
        if (left_stopped && !right_stopped) {
          if (forward) {
            dr.setSpeedRightMotor(100);
            dr.rightMotorBackward();
            started_moving_t = System.currentTimeMillis();
            forward = false;
          } else {
            dr.setSpeedRightMotor(100);
            dr.rightMotorForward();
            started_moving_t = System.currentTimeMillis();
            forward = true;
          }
        }
        
        if (!left_stopped && right_stopped) {
          if (forward) {
            dr.setSpeedLeftMotor(125);
            dr.leftMotorBackward();
            started_moving_t = System.currentTimeMillis();
            forward = false;
          } else {
            dr.setSpeedLeftMotor(100);
            dr.leftMotorForward();
            started_moving_t = System.currentTimeMillis();
            forward = true;
          }
        }
        
        if (!left_stopped && !right_stopped) {
          if (forward) {
            dr.setSpeedLeftMotor(150);
            dr.setSpeedRightMotor(150);
            dr.endlessMoveBackward();
            started_moving_t = System.currentTimeMillis();
            forward = false;
          } else {
            dr.setSpeedLeftMotor(150);
            dr.setSpeedRightMotor(150);
            dr.endlessMoveForward();
            started_moving_t = System.currentTimeMillis();
            forward = true;
          }
        }
      }
      try {
        Thread.sleep(30);
      } catch (Exception e) {
        System.out.println("can't pause");
      }

    }

    sd.decrementLLRefs();
    Sound.beepSequenceUp();
  }

  /*
   * Not really necessary, this is just to make the risingEdge and fallingEdge methods more
   * readable.
   */
  private void sleepThread(float seconds) {
    try {
      Thread.sleep((long) (seconds * 1000));
    } catch (Exception e) {
      System.out.println("[ULTRASONIC] Can't sleep thread");
      // TODO: handle exception
    }
  }
  
  private double getReferenceAngle() {
    double error = 8.0;
    double theta = Math.toDegrees(odo.getTheta());
    if (theta + error >= 0 && theta - error <= 0) {
      return 0;
    } else if (theta + error >= 360 && theta - error <= 360) {
      return 0;
    } else if (theta + error >= 90 && theta - error <= 90) {
      return 90;
    } else if (theta + error >= 180 && theta - error <= 180) {
      return 180;
    } else if (theta + error >= 270 && theta - error <= 270) {
      return 270;
    } else {
      // Make the robot align to 0.
      dr.rotate(-theta, false);
      return 0;
    }    
  }
}
