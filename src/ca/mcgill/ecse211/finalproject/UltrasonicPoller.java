package ca.mcgill.ecse211.finalproject;

import lejos.robotics.SampleProvider;



public class UltrasonicPoller extends Thread {

  // --------------------------------------------------------------------------------
  // Constants
  // --------------------------------------------------------------------------------

  private static final long SLEEP_TIME = 20;


  // --------------------------------------------------------------------------------
  // Variables
  // --------------------------------------------------------------------------------

  private SampleProvider sensor;
  private float[] data;

  private SensorData sd;


  /**
   * Constructor
   * @param sensor TODO
   * @param sd TODO
   */
  public UltrasonicPoller(SampleProvider sensor, SensorData sd) {
    this.sensor = sensor;
    this.data = new float[sensor.sampleSize()];
    this.sd = sd;
  }


  /**
   * TODO
   */
  public void run() {
    while (true) {
      // Stop polling data whenever the light level reference count in our
      // SensorData object has reached zero.
      if (this.sd.getLLRefs() > 0) {
        this.sensor.fetchSample(this.data, 0);
        this.sd.ultrasonicHandler(this.data[0] * 100.0f);
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
        Thread.sleep(UltrasonicPoller.SLEEP_TIME);
      } catch (Exception e) {
        // ...
      }
    }

    // Unreachable
  }

}
