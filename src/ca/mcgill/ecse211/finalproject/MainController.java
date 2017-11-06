package ca.mcgill.ecse211.finalproject;

import java.util.Map;
import ca.mcgill.ecse211.WiFiClient.WifiConnection;
import lejos.hardware.Button;

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
  public enum State {
    IDLE, LOCALIZING, NAVIGATING, ZIPLINING, SEARCHING
  }

  ;

  // --------------------------------------------------------------------------------
  // Game data
  // --------------------------------------------------------------------------------
  
  static Waypoint redTeamStart;
  static Waypoint greenTeamStart;
  
  static int RedTeam; // Red team group number
  static int GreenTeam; // Green team group number
  static int RedCorner; // red team's starting corner
  static int GreenCorner; // green team's starting corner
  static int OG; // Color of green team's flag
  static int OR; // Color of red team's flag
  static Waypoint Red_LL; // lower left hand corner of the red zone.
  static Waypoint Red_UR; // upper right hand corner of the red zone.
  static Waypoint Green_LL; // lower left hand corner of the green zone.
  static Waypoint Green_UR; // upper right hand corner of the green zone.
  static Waypoint ZC_R; // end point corresponding to zip line in red zone.
  static Waypoint ZO_R; // with ZC_R indicates direction of zip line.
  static Waypoint ZC_G; // end point corresponding to zip line in green zone.
  static Waypoint ZO_G; // with ZC_G indicates direction of zip line.
  static Waypoint SH_LL; // lower left corner of horizontal shallow water zone.
  static Waypoint SH_UR; // upper right corner of horizontal shallow water zone.
  static Waypoint SV_LL; // lower left corner of vertical shallow water zone.
  static Waypoint SV_UR; // upper right corner of vertical shallow water zone.
  static Waypoint SR_LL; // lower left corner of red search zone.
  static Waypoint SR_UR; // upper right corner of red search zone.
  static Waypoint SG_LL; // lower left corner of green search zone.
  static Waypoint SG_UR; // upper right corner of green search zone.
  
  
  // --------------------------------------------------------------------------------
  // Constants
  // --------------------------------------------------------------------------------


  // --------------------------------------------------------------------------------
  // Variables
  // --------------------------------------------------------------------------------

  private Localizer loc;
  private UltrasonicLocalizer ul;
  private LightLocalizer ll;
  private Navigator nav;
  private ZipLine zip;
  private Searcher srch;

  private State cur_state = State.IDLE; // Current state of the controller
  private String sub_state = null; // D_State of the currently executing subsystem

  /**
   * Constructor
   *
   * @param loc           Localizer object, manages the localization of the robot.
   * @param ul Ultrasonic localizer, works with the Localizer class to localize the robot.
   * @param ll      Light localizer, works with the Localizer class to localize the robot.
   * @param nav           Navigator, handles navigating the robot through sets of waypoints as well as avoiding obstacles.
   * @param zip             Zipline controller, handles crossing the zip line.
   * @param srch            Searcher object, works with the navigator to look for the 'flag'.
   */
  public MainController(Localizer loc, UltrasonicLocalizer ul, LightLocalizer ll, Navigator nav, ZipLine zip, Searcher srch) {
    this.loc = loc;
    this.ul = ul;
    this.ll = ll;
    this.nav = nav;
    this.zip = zip;
    this.srch = srch;
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

    /*
     * Get the game data from the server before doing anything.
     */
    getGameData();

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
   * <p>
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
      default:
        break;
    }
  }

  /**
   * Processes the IDLE state of the main controller.
   *
   * @return new state, or same one if no goal.
   */
  private State process_idle() {
    // TODO: Integrate the wifi class to determine the robot's goal.
    return State.LOCALIZING;
  }

  /**
   * Processes the LOCALIZING state of the main controller, delegates control to the Localizer.
   *
   * @return new state, or same one if not done.
   */
  private State process_localizing() {
    sub_state = loc.process(); // the localizer handles controlling both the ultrasonic and light localizers.

    if (loc.isDone()) {
      return State.IDLE;
    } else {
      return State.LOCALIZING;
    }

    // This is going to be a fallthrough.
    //return State.IDLE;
  }

  /**
   * Processes the NAVIGATING state of the main controller, delegates control to the Navigator.
   *
   * @return new state, or same one if not done.
   */
  private State process_navigating() {
    sub_state = nav.process();

    if (nav.isDone()) {
      return State.IDLE;
    } else {
      return State.NAVIGATING;
    }

    // This is going to be a fallthrough.
    //return State.IDLE;
  }

  /**
   * Processes the ZIPLINING state of the main controller, delegates control to the Zipline class.
   *
   * @return new state, or same one if not done.
   */
  private State process_ziplining() {
    sub_state = zip.process();

    if (zip.isDone()) {
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
    sub_state = srch.process();

    if (srch.isDone()) {
      // Check for various conditions
    } else {
      return State.SEARCHING;
    }

    // This is going to be a fallthrough.
    return State.IDLE;
  }

  /**
   * Establishes the connection with the server and updates the game variables in order to get the state machine going.
   */
  private void getGameData() {
    WifiConnection conn = new WifiConnection(FinalProject.SERVER_IP, FinalProject.TEAM_NB, FinalProject.ENABLE_WIFI_DEBUG);
    try {
      @SuppressWarnings("rawtypes")
      Map data = conn.getData();
      
      RedTeam = ((Long) data.get("RedTeam")).intValue();
      GreenTeam = ((Long) data.get("GreenTeam")).intValue();
      RedCorner = ((Long) data.get("RedCorner")).intValue();
      GreenCorner = ((Long) data.get("GreenCorner")).intValue();
      OG = ((Long) data.get("OG")).intValue();
      OR = ((Long) data.get("OR")).intValue();
      Red_LL = new Waypoint(((Long) data.get("Red_LL_x")).intValue(), ((Long) data.get("Red_LL_y")).intValue());
      Red_UR = new Waypoint(((Long) data.get("Red_UR_x")).intValue(), ((Long) data.get("Red_UR_y")).intValue());
      Green_LL = new Waypoint(((Long) data.get("Green_LL_x")).intValue(), ((Long) data.get("Green_LL_y")).intValue());
      Green_UR = new Waypoint(((Long) data.get("Green_UR_x")).intValue(), ((Long) data.get("Green_UR_y")).intValue());
      ZC_R = new Waypoint(((Long) data.get("ZC_R_x")).intValue(), ((Long) data.get("ZC_R_y")).intValue());
      ZO_R = new Waypoint(((Long) data.get("ZO_R_x")).intValue(), ((Long) data.get("ZO_R_y")).intValue());
      ZC_G = new Waypoint(((Long) data.get("ZC_G_x")).intValue(), ((Long) data.get("ZC_G_y")).intValue());
      ZO_G = new Waypoint(((Long) data.get("ZO_G_x")).intValue(), ((Long) data.get("ZO_G_y")).intValue());
      SH_LL = new Waypoint(((Long) data.get("SH_LL_x")).intValue(), ((Long) data.get("SH_LL_y")).intValue());
      SH_UR = new Waypoint(((Long) data.get("SH_UR_x")).intValue(), ((Long) data.get("SH_UR_y")).intValue());
      SV_LL = new Waypoint(((Long) data.get("SV_LL_x")).intValue(), ((Long) data.get("SV_LL_y")).intValue());
      SV_UR = new Waypoint(((Long) data.get("SV_UR_x")).intValue(), ((Long) data.get("SV_UR_y")).intValue());
      SG_LL = new Waypoint(((Long) data.get("SG_LL_x")).intValue(), ((Long) data.get("SG_LL_y")).intValue());
      SG_UR = new Waypoint(((Long) data.get("SG_UR_x")).intValue(), ((Long) data.get("SG_UR_y")).intValue());
      SR_LL = new Waypoint(((Long) data.get("SR_LL_x")).intValue(), ((Long) data.get("SR_LL_y")).intValue());
      SR_UR = new Waypoint(((Long) data.get("SR_UR_x")).intValue(), ((Long) data.get("SR_UR_y")).intValue());
      
      switch (RedCorner) {
        case 1:
          redTeamStart = new Waypoint(1, 1);
          break;
        case 2:
          redTeamStart = new Waypoint(11, 1);
          break;
        case 3:
          redTeamStart = new Waypoint(11, 11);
          break;
        case 4:
          redTeamStart = new Waypoint(1, 11);
          break;
      }
      
      switch (GreenCorner) {
        case 1:
          greenTeamStart = new Waypoint(1, 1);
          break;
        case 2:
          greenTeamStart = new Waypoint(11, 1);
          break;
        case 3:
          greenTeamStart = new Waypoint(11, 11);
          break;
        case 4:
          greenTeamStart = new Waypoint(1, 11);
          break;
      }
      
      // TODO: add variables and flags to determine the sequence of states the controller must go through.
      
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
    }
  }
}
