package ca.mcgill.ecse211.finalproject;

import lejos.robotics.SampleProvider;

/**
 * Polls the ultrasonic sensor and sends the data to the SensorData class.
 *
 * @author Josh Inscoe
 */
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
  private float last_dist = 0.f;

  private SensorData sd;


  /**
   * Constructor
   *
   * @param sensor SampleProvider for the robot's ultrasonic sensor.
   * @param sd SensorData object, all sensor data will be passed to it for easier processing and accessing.
   */
  public UltrasonicPoller(SampleProvider sensor, SensorData sd) {
    this.sensor = sensor;
    this.data = new float[sensor.sampleSize()];
    this.sd = sd;
  }


  /**
   * run() method.
   */
  public void run() {
    while (true) {
      // Stop polling data whenever the light level reference count in our
      // SensorData object has reached zero.
      if (this.sd.getUSRefs() > 0) {
        this.sensor.fetchSample(this.data, 0);
        this.sd.ultrasonicHandler(this.data[0] * 100.0f);
        last_dist = this.data[0] * 100.f;
      } else {
        // Sleep indefinitely until this thread is interrupted, signaling that sensor
        // data may, once again, be needed.
//        try {
//          Thread.sleep(Long.MAX_VALUE);
//        } catch (Exception e) {
//          // ...
//        }

        //continue;
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
  
  public synchronized float getLastDist() {
	return last_dist;
  }

}
