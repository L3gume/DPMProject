package ca.mcgill.ecse211.finalproject;


/**
 * Handles the localization of the robot, manages both the ultrasonic and light localizers.
 * 
 * @author Justin Tremblay
 */
public class Localizer {

  /*
   * TODO: rework most of this crap and rewrite javadoc comments
   */

  /* References to other classes */
  private UltrasonicLocalizer ul;
  private LightLocalizer ll;
  private Driver dr;

  private boolean skip_ultrasonic = false; // Tells whether or not to skip the ultrasonic localization.
  private boolean done = false; // Tells whether or not we are done localizing.
  
  private static Waypoint ref_pos;

  /**
   * Enum representing the state of the localizer.
   */
  public enum Loc_State {
    IDLE, NOT_LOCALIZED, ULTRASONIC, LIGHT, DONE
  }

  private Loc_State cur_state = Loc_State.IDLE;

  /**
   * Constructor
   * 
   * @param ul UltrasonicLocalizer, performs rising or falling edge localization to determine the robot's heading
   * @param ll LightLocalization, works alongside the ultrasonic localizer to determine the robot's
   *        position with respect to a reference position.
   * @param dr Driver, handles moving the robot.
   */
  public Localizer(UltrasonicLocalizer ul, LightLocalizer ll, Driver dr) {
    this.ul = ul;
    this.ll = ll;
    this.dr = dr;

    ref_pos = FinalProject.DEBUG_REF_POS;
  }

  /**
   * process() method. Is called every iteration of the MainController thread if its current state is LOCALIZING
   *
   * @return current state, as a string.
   */
   String process() {
      switch (cur_state) {
        case IDLE:
          cur_state = process_idle();
          break;
        case NOT_LOCALIZED:
          cur_state = process_notLocalized();
          break;
        case ULTRASONIC:
          cur_state = process_ultrasonic();
          break;
        case LIGHT:
          cur_state = process_light();
          break;
        case DONE:
          cur_state = process_done();
          break;
        default: // Should not happen.
          break;
      }
      
      /*
       * Space reserved for special cases, shouldn't be needed here.
       */
      
      return cur_state.toString(); // return the current Loc_State as a string (controller sub-state)
  }

  /* D_State processing methods */

  /**
   * Processes the IDLE state of the localizer. Checks for conditions before returning a new state (or the same)
   *
   * @return new state.
   */
  private Loc_State process_idle() {
    done = false;
    return Loc_State.NOT_LOCALIZED;
  }

  /**
   * Processes the NOT_LOCALIZED state of the localizer. Transitions into either ULTRASONIC or LIGHT depending on the current parameters of the system.
   *
   * @return new state.
   */
  private Loc_State process_notLocalized() {
//    dr.rotate(360, true, true); // Start rotating
    if (!ref_pos.equals(MainController.greenTeamStart)) {
      skip_ultrasonic = true;
    }
    // Fancy ternary nonsense!
    return skip_ultrasonic ? Loc_State.LIGHT : Loc_State.ULTRASONIC;
  }

  /**
   * Processes the ULTRASONIC state of the localizer. Checks for various conditions before and after calling the UltrasonicLocalizer's localize() method.
   *
   * @return new state.
   */
  private Loc_State process_ultrasonic() {
    ul.localize();
    return Loc_State.LIGHT; // Go directly to light localization.
  }

  /**
   * Processes the Light state of the localizer. Checks for various conditions before and after calling the LightLocalizer's localize() method.
   *
   * @return new state.
   */
  private Loc_State process_light() {
    ll.localize();
    return Loc_State.DONE;
  }

  /**
   * Processes the DONE state of the localizer. Notifies the MainController that the localization is done and resets local variables.
   *
   * @return new state.
   */
  private Loc_State process_done() {
    done = true;
    
    // reset
    if (skip_ultrasonic) {
      skip_ultrasonic = false;
    }
    return Loc_State.IDLE;
  }

  /**
   * Gets the current reference position.
   *
   * @return Waypoint representing the current reference position.
   */
  public static Waypoint getRefPos() {
    return ref_pos;
  }
  
  /**
   * 
   */
  public void setRefPos(Waypoint new_pos) {
	  Localizer.ref_pos = new_pos;
  }

  /**
   * Returns true if we are done localizing.
   *
   * @return boolean done.
   */
  public boolean isDone() {
    return done;
  }
}