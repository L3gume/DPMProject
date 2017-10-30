package ca.mcgill.ecse211.finalproject;

/**
 * Main controller
 * This class is the main thread of the program and the root of the state machine that will control every action.
 *
 * @author Justin Tremblay
 */
public class MainController extends Thread {

  /**
   * Enum describing the state of the controller.
   */
  public enum State { IDLE, LOCALIZING, NAVIGATING, ZIPLINING, SEARCHING };

  // --------------------------------------------------------------------------------
  // Constants
  // --------------------------------------------------------------------------------


  // --------------------------------------------------------------------------------
  // Variables
  // --------------------------------------------------------------------------------

  private Localizer localizer;
  private UltrasonicLocalizer ultrasonicLocalizer;
  private LightLocalizer lightLocalizer;
  private Navigator navigator;
  private ZipLine zipLine;

  private State cur_state = State.IDLE; // Current state of the controller
  private String sub_state = null; // D_State of the currently executing subsystem

  /**
   * Constructor
   *
   * @param localizer Localizer object, manages the localization of the robot.
   * @param ultrasonicLocalizer Ultrasonic localizer, works with the Localizer class to localize the robot
   * @param lightLocalizer Light localizer, works with the Localizer class to localize the robot
   * @param navigator Navigator, handles navigating the robot through sets of waypoints as well as avoiding obstacles
   * @param zipLine Zipline controller, handles crossing the zip line.
   */
  public MainController(Localizer localizer, UltrasonicLocalizer ultrasonicLocalizer, LightLocalizer lightLocalizer, Navigator navigator, ZipLine zipLine) {
    this.localizer = localizer;
    this.ultrasonicLocalizer = ultrasonicLocalizer;
    this.lightLocalizer = lightLocalizer;
    this.navigator = navigator;
    this.zipLine = zipLine;
  }


  /**
   * Main control thread, this is where most of the processing will happen.
   */
  public void run() {

    //
    // TODO:
    //
    // Fix up the code below to make it functional in the more general situation, when
    // we are not necessarily in a corner (it should also work if we are in a corner).
    //

    //
    // NOTE:
    //
    // This is a general outline of how zip-line traversal would have worked in Lab 5; however,
    // it will likely be different in the final project, since we may not be in a corner when
    // we need to start looking for the zip-line.
    //

    while (true) {
      process();

      try {
        Thread.sleep(40);
      } catch (Exception e) {
        //...
      }
    }
  }

  /**
   * Root of the robot's state machine. The current state of the robot is processed at every iteration
   * of the run() loop. Depending on the current state, the process method of the corresponding subsystem
   * is called, which processes the subsystem's state.
   *
   * This structure eliminates the problem of accessing variables from multiple threads as everything
   * is essentially done is the same thread.
   */
  private void process() {
    switch (cur_state) {
      case IDLE:
        cur_state = process_idle();
        break;
      case LOCALIZING:
        cur_state = process_localizing();
        break;
      case NAVIGATING:
        cur_state = process_navigating();
        break;
      case ZIPLINING:
        cur_state = process_ziplining();
        break;
      case SEARCHING:
        cur_state = process_searching();
        break;
      default: break;
    }
  }

  /**
   * Processes the IDLE state of the main controller.
   *
   * @return new state, or same one if no goal.
   */
  private State process_idle() {
    // TODO: Integrate the wifi class to determine the robot's goal.
    return State.IDLE;
  }

  /**
   * Processes the LOCALIZING state of the main controller, delegates control to the Localizer.
   *
   * @return new state, or same one if not done.
   */
  private State process_localizing() {
    sub_state = localizer.process(); // the localizer handles controlling both the ultrasonic and light localizers.

    if (localizer.isDone()) {
      // Check for various conditions
    } else {
      return State.LOCALIZING;
    }

    // This is going to be a fallthrough.
    return State.IDLE;
  }

  /**
   * Processes the NAVIGATING state of the main controller, delegates control to the Navigator.
   *
   * @return new state, or same one if not done.
   */
  private State process_navigating() {
    sub_state = navigator.process();

    if (navigator.isDone()) {
      // Check for various conditions
    } else {
      return State.NAVIGATING;
    }

    // This is going to be a fallthrough.
    return State.IDLE;
  }

  /**
   * Processes the ZIPLINING state of the main controller, delegates control to the Zipline class.
   *
   * @return new state, or same one if not done.
   */
  private State process_ziplining() {
    sub_state = zipLine.process();

    if (zipLine.isDone()) {
      // Check for various conditions
    } else {
      return State.ZIPLINING;
    }

    // This is going to be a fallthrough.
    return State.IDLE;
  }

  /**
   * Processes the SEARCHING state of the main controller, delegates control to the Searcher class. This state is a special
   * case as it will also use the navigator to move the robot.
   *
   * @return new state, or same one if not done.
   */
  private State process_searching() {
    // TODO: Implement the Searcher class.
    return State.IDLE;
  }
}
