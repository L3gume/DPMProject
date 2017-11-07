package ca.mcgill.ecse211.finalproject;

import lejos.hardware.Sound;

import java.awt.*;

/**
 * Uses the ultrasonic and light sensors mounted to the front of the robot to search for the 'flag'.
 * When it is found, the robot beeps to indicate capture.
 * <p>
 * This class works along with the navigator to move around the search zone, looking for the 'flag'.
 */
public class Searcher {
  /**
   * Enum describing the current state of the searcher.
   */
  public enum S_State {
    IDLE, SEARCHING, FOUND
  }

  private SensorData sd;

  private S_State cur_state = S_State.IDLE;
  private double distance = 0.0;
  private Color color_id = null;
  private boolean done = false;

  /**
   * Constructor
   *
   * @param sd SensorData object to access sensor data.
   */
  public Searcher(SensorData sd) {
    this.sd = sd;
  }

  /**
   * Process method, processes the current state of the Searcher and returns a new state (or the same if not done.
   *
   * @return new state (or same) as a string.
   */
  String process() {
    switch (cur_state) {
      case IDLE:
        cur_state = process_idle();
        break;
      case SEARCHING:
        cur_state = process_searching();
        break;
      case FOUND:
        cur_state = process_found();
        break;
      default:
        break;
    }
    return cur_state.toString();
  }

  private S_State process_idle() {
    return S_State.IDLE;
  }

  private S_State process_searching() {
    getSensorData();

    return S_State.IDLE;
  }

  private S_State process_found() {
    signalCapture();

    if (this.sd.getSensorRefs(SensorData.SensorID.US_FRONT) > 0) {
      this.sd.decrementSensorRefs(SensorData.SensorID.US_FRONT);
    }
    if (this.sd.getSensorRefs(SensorData.SensorID.LS_FRONT) > 0) {
      this.sd.decrementSensorRefs(SensorData.SensorID.LS_FRONT);
    }

    return S_State.IDLE;
  }

  private void getSensorData() {
    if (this.sd.getSensorRefs(SensorData.SensorID.US_FRONT) > 0) {
      distance = this.sd.getSensorDataLatest(SensorData.SensorID.US_FRONT);
    } else {
      distance = 0;
      this.sd.incrementSensorRefs(SensorData.SensorID.US_FRONT);
    }

    if (this.sd.getSensorRefs(SensorData.SensorID.LS_FRONT) > 0) {
      // TODO: need color id compatibility in SensorData
    } else {
      color_id = null;
      this.sd.incrementSensorRefs(SensorData.SensorID.LS_FRONT);
    }
  }

  private void signalCapture() {
    Sound.setVolume(100);
    for (int i = 0; i < 3; i++) {
      Sound.beep();
      try {
        Thread.sleep(500);
      } catch (Exception e) {
        // should work since the Searcher is executed from the MainController thread.
      }
    }
  }

  /**
   * Returns true if the searcher has found the flag.
   *
   * @return boolean done
   */
  public boolean isDone() {
    return done;
  }
}
