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

    if (sd.getUSRefs() > 0) {
      sd.decrementUSRefs();
    }
    if (sd.getLLRefs() > 0) {
      sd.decrementLLRefs();
    }

    return S_State.IDLE;
  }

  private void getSensorData() {
    if (sd.getUSRefs() > 0) {
      distance = sd.getUSDataLatest();
    } else {
      distance = 0;
      sd.incrementUSRefs();
    }

    if (sd.getLLRefs() > 0) {
      // TODO: need color id compatibility in SensorData
    } else {
      color_id = null;
      sd.incrementLLRefs();
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
}
