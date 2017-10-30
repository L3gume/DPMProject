package ca.mcgill.ecse211.finalproject;

import lejos.robotics.SampleProvider;

/**
 * Polls the light sensor and sends the data to the SensorData class.
 *
 * @author Josh Inscoe
 */
public class LightPoller extends Thread {

  // --------------------------------------------------------------------------------
  // Constants
  // --------------------------------------------------------------------------------

  private static final long SLEEP_TIME = 20;


  // --------------------------------------------------------------------------------
  // Variables
  // --------------------------------------------------------------------------------

  private SampleProvider sensorLeft;
  private SampleProvider sensorRight;
  private SampleProvider sensorMid;
  private float[] dataLeft;
  private float[] dataRight;
  private float[] dataMid;

  private SensorData sd;


  /**
   * Constructor
   *
   * @param sensorLeft SampleProvider for the robot's left light sensor.
   * @param sensorRight SampleProvider for the robot's right light sensor.
   * @param sensorMid SampleProvider for the robot's middle light sensor.
   * @param sd SensorData object, all sensor data will be passed to it for easier processing and accessing.
   */
  public LightPoller(SampleProvider sensorLeft, SampleProvider sensorRight, SampleProvider sensorMid, SensorData sd) {
    this.sensorLeft = sensorLeft;
    this.sensorRight = sensorRight;
    this.sensorMid = sensorMid;
    this.dataLeft = new float[sensorLeft.sampleSize()];
    this.dataRight = new float[sensorRight.sampleSize()];
    this.dataMid = new float[sensorMid.sampleSize()];
    this.sd = sd;
  }


  /**
   * run() method.
   */
  public void run() {
    while (true) {
      // Stop polling data whenever the light level reference count in our
      // SensorData object has reached zero.
      if (this.sd.getLLRefs() > 0) {
        this.sensorLeft.fetchSample(this.dataLeft, 0);
        this.sd.lightLevelHandler(this.dataLeft[0] * 100.0f);
      } else {
        // Sleep indefinitely until this thread is interrupted, signaling that sensor
        // data may, once again, be needed.
        try {
          Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
          // ...
        }

        continue;
      }

      // Sleep for a bit.
      try {
        Thread.sleep(LightPoller.SLEEP_TIME);
      } catch (Exception e) {
        // ...
      }
    }

    // Unreachable
  }

}
