package ca.mcgill.ecse211.finalproject;

import lejos.robotics.SampleProvider;

/**
 * Polls the sensors and sends the data to the SensorData class.
 *
 * @author Alex Hale
 * @author Josh Inscoe
 */
public class SensorPoller extends Thread {

  // --------------------------------------------------------------------------------
  // Constants
  // --------------------------------------------------------------------------------

  private static final long SLEEP_TIME = 20;


  // --------------------------------------------------------------------------------
  // Variables
  // --------------------------------------------------------------------------------

  private SampleProvider lSensorLeft;
  private SampleProvider lSensorRight;
  private SampleProvider lSensorMid;
  private SampleProvider usSensor;
  private float[] lDataLeft;
  private float[] lDataRight;
  private float[] lDataMid;
  private float[] usData;

  private SensorData sd;


  /**
   * Constructor
   *
   * @param lSensorLeft SampleProvider for the robot's left light sensor.
   * @param lSensorRight SampleProvider for the robot's right light sensor.
   * @param lSensorMid SampleProvider for the robot's middle light sensor.
   * @param usSensor SampleProvider for the robot's ultrasonic sensor
   * @param sd SensorData object, all sensor data will be passed to it for easier processing and accessing.
   */
  public SensorPoller(SampleProvider lSensorLeft, SampleProvider lSensorRight, 
    SampleProvider lSensorMid, SampleProvider usSensor, SensorData sd) {
    this.lSensorLeft = lSensorLeft;
    this.lSensorRight = lSensorRight;
    this.lSensorMid = lSensorMid;
    this.usSensor = usSensor;
    this.lDataLeft = new float[lSensorLeft.sampleSize()];
    this.lDataRight = new float[lSensorRight.sampleSize()];
    this.lDataMid = new float[lSensorMid.sampleSize()];
    this.usData = new float[usSensor.sampleSize()];
    this.sd = sd;
  }


  /**
   * run() method.
   */
  public void run() {

    while (true) {
      if (this.sd.getLLRefs() > 0) {
        // Left light sensor
        this.lSensorLeft.fetchSample(this.lDataLeft, 0);
        this.sd.lightLevelHandler(this.lDataLeft[0], 1);

        // Right light sensor
        this.lSensorRight.fetchSample(this.lDataRight, 0);
        this.sd.lightLevelHandler(this.lDataRight[0], 2);

       
      }
      
      if (this.sd.getColorRefs() > 0) {
         // Middle light sensor
        this.lSensorMid.fetchSample(this.lDataMid, 0);
        this.sd.colorHandler((int)this.lDataMid[0]);
      }
      
      if (this.sd.getUSRefs() > 0) {
        // Ultrasonic sensor
        this.usSensor.fetchSample(this.usData, 0);
        this.sd.ultrasonicHandler(this.usData[0] * 100.0f);
      }

      // Sleep for a bit.
      try {
        Thread.sleep(FinalProject.SLEEP_TIME);
      } catch (Exception e) {
        // ...
      }
    }

    // Unreachable
  }

}
