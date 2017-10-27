package ca.mcgill.ecse211.finalproject;



public class ZipLineController extends Thread {

  // --------------------------------------------------------------------------------
  // Constants
  // --------------------------------------------------------------------------------


  // --------------------------------------------------------------------------------
  // Variables
  // --------------------------------------------------------------------------------

  private UltrasonicLocalizer ultrasonicLocalizer;
  private LightLocalizer lightLocalizer;
  private Navigator navigator;
  private ZipLine zipLine;


  /**
   * Constructor
   *
   * @param ultrasonicLocalizer TODO
   * @param lightLocalizer TODO
   * @param navigator TODO
   * @param zipLine TODO
   */
  public ZipLineController(
      UltrasonicLocalizer ultrasonicLocalizer, LightLocalizer lightLocalizer,
      Navigator navigator, ZipLine zipLine
      ) {
    this.ultrasonicLocalizer = ultrasonicLocalizer;
    this.lightLocalizer = lightLocalizer;
    this.navigator = navigator;
    this.zipLine = zipLine;
  }


  /**
   * TODO
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

    // Step #1: Localize using the ultrasonic sensor.
    this.ultrasonicLocalizer.localize();

    // Step #2: Localize using the light sensor (this includes angle correction).
    this.lightLocalizer.localize();

    // Step #3: Navigate to the zip-line.
    this.navigator.navigate();

    // Step #4: Cross the zip-line.
    this.zipLine.cross();

  }

}
