package ca.mcgill.ecse211.finalproject;



/**
 * This class holds the data received from a single sensor and it, additionally,
 * implements certain routines for processing such data.
 *
 * @author Joshua Inscoe
 */
class Sensor {

  // --------------------------------------------------------------------------------
  // Constants
  // --------------------------------------------------------------------------------

  public static final int DATA_ARRAY_SIZE = 20;


  // --------------------------------------------------------------------------------
  // Variables
  // --------------------------------------------------------------------------------

  // Sensor id for identification purposes
  private int id;

  // Reference couns of other objects accessing sensor data
  private int refs;

  // Locks
  private Object refsLock;
  private Object dataLock;
  private Object dataDerivLock;
  private Object statsLock;

  // Circular array holding the original sensor data
  private float[] data;

  // Circular array holding the derivative of the sensor data
  private float[] dataDeriv;

  // The next index at which data should be placed in the circular array
  private int index;

  // Boolean values signaling whether circular array are filled
  private boolean filled;

  //
  // The moving statistics of the sensor data
  //
  // Index:
  //
  // 0 - moving average
  // 1 - moving variance
  // 2 - moving standard deviation
  //
  private float[] stats;


  // --------------------------------------------------------------------------------
  // Constructors
  // --------------------------------------------------------------------------------

  /**
   * Constructor
   *
   * @param id the sensor ID with which this object should be associated
   */
  public Sensor(int id) {
    this.id = id;

    this.refs = 0;

    this.refsLock = new Object();
    this.dataLock = new Object();
    this.dataDerivLock = new Object();
    this.statsLock = new Object();

    this.data = new float[Sensor.DATA_ARRAY_SIZE];

    this.dataDeriv = new float[Sensor.DATA_ARRAY_SIZE];

    this.index = 0;

    this.filled = false;

    this.stats = new float[] { 0.0f, 0.0f, 0.0f };
  }


  // --------------------------------------------------------------------------------
  // Handlers
  // --------------------------------------------------------------------------------

  /**
   * Append the new data value, `val`, to the sensor data, and update the derivative of the
   * sensor data as well as the moving statistics.
   *
   * @param val the latest data value received from the sensor
   */
  public void dataHandler(float val) {
    int n = Sensor.DATA_ARRAY_SIZE;

    int index = this.index;

    float old = this.data[index];

    float oldAvg = this.stats[0];
    float oldVar = this.stats[1];

    // Compute moving average, variance, and standard deviation of the sensor data.
    float newAvg = oldAvg + (val - old) / n;
    float newVar = oldVar + (val - old) * (val - newAvg + old - oldAvg) / (n - 1);
    float newDev = (float)Math.sqrt((double)newVar);

    // Update the sensor datat statistics.
    synchronized (this.statsLock) {
      this.stats[0] = newAvg;
      this.stats[1] = newVar;
      this.stats[2] = newDev;
    }

    // Update the latest sensor data value.
    synchronized (this.dataLock) {
      this.data[index] = val;
    }

    // Update the latest sensor data derivative value.
//    synchronized (this.dataDerivLock) {
//      this.dataDeriv[index] = val - this.data[index - 1];
//    }

    index += 1;
    index %= n;

    // Update the current index.
    this.index = index;

    // Update the `this.filled` boolean if we've filled the data sensor array.
    if (!this.filled && index == 0) {
      this.filled = true;
    }

    return;
  }


  // --------------------------------------------------------------------------------
  // Methods
  // --------------------------------------------------------------------------------

  /**
   * @return the sensor ID associated with this object.
   */
  public int getId() {
    return this.id;
  }

  /**
   * @return the number of external classes referencing this object's sensor data.
   */
  public int getRefs() {
    return this.refs;
  }

  /**
   * Copy this object's sensor data into `data`.
   *
   * @param data an array into which the sensor data should be copied
   *
   * @return true if the data was successfully copied, false otherwise
   */
  public boolean getData(float[] data) {
    boolean copied = false;

    if (this.filled) {
      copied = true;

      synchronized (this.dataLock) {
        for (int i = 0; i < Sensor.DATA_ARRAY_SIZE; ++i) {
          data[i] = this.data[i];
        }
      }
    }

    return copied;
  }

  /**
   * Copy the derivative of this object's sensor data into `dataDeriv`.
   *
   * @param dataDeriv an array into which the derivative of the sensor data should be copied
   *
   * @return true if the data was successfully copied, false otherwise
   */
  public boolean getDataDeriv(float[] dataDeriv) {
    boolean copied = false;

    if (this.filled) {
      copied = true;

      synchronized (this.dataDerivLock) {
        for (int i = 0; i < Sensor.DATA_ARRAY_SIZE; ++i) {
          dataDeriv[i] = this.dataDeriv[i];
        }
      }
    }

    return copied;
  }

  /**
   * @return the latest data value received from the sensor
   */
  public float getDataLatest() {
    float value = -1.0f;

    synchronized (this.dataLock) {
      value = this.data[this.index];
    }

    return value;
  }

  /**
   * @return the most-recently calculated derivative of the sensor data
   */
  public float getDataDerivLatest() {
    float value = -1.0f;

    synchronized (this.dataDerivLock) {
      value = this.dataDeriv[this.index];
    }

    return value;
  }

  /**
   * Copy the moving statistics (mean, variance, and standard deviation) of this object's
   * sensor data into `stats`.
   *
   * @param stats an array into which the moving statistics should be copied
   *
   * @return true if the moving statistics were successfully copied, false otherwise
   */
  public boolean getStats(float[] stats) {
    boolean copied = false;

    if (this.filled) {
      copied = true;

      synchronized (this.statsLock) {
        stats[0] = this.stats[0];
        stats[1] = this.stats[1];
        stats[2] = this.stats[2];
      }
    }

    return copied;
  }

  /**
   * Increment the number of external classes referencing this object's sensor data.
   */
  public void incrementRefs() {
    synchronized (this.refsLock) {
      this.refs += 1;
    }

    return;
  }

  /**
   * Decrement the number of external classes referencing this object's sensor data.
   */
  public void decrementRefs() {
    synchronized (this.refsLock) {
      this.refs -= 1;
    }

    return;
  }

}


/**
 * This class fascilitates access to all sensors' data.
 *
 * @author Joshua Inscoe
 */
public class SensorData {

  //
  // NOTE:
  //
  // US/us = ultrasonic sensor
  // LS/ls = light sensor
  //


  // --------------------------------------------------------------------------------
  // Constants
  // --------------------------------------------------------------------------------

  // The size of the circular arrays used to hold the data of each sensor
  public final int SENSOR_DATA_ARRAY_SIZE = Sensor.DATA_ARRAY_SIZE;

  // The sensor IDs of our four sensors
  public enum SensorID {
    US_FRONT,
    LS_FRONT,
    LS_RIGHT,
    LS_LEFT
  };


  // --------------------------------------------------------------------------------
  // Variables
  // --------------------------------------------------------------------------------

  // Objects to hold the data of our four sensors
  private Sensor usFront;
  private Sensor lsFront;
  private Sensor lsRight;
  private Sensor lsLeft;

  // --------------------------------------------------------------------------------
  // Constructors
  // --------------------------------------------------------------------------------

  /**
   * Constructor
   */
  public SensorData() {
    this.usFront = new Sensor(SensorID.US_FRONT.ordinal());
    this.lsFront = new Sensor(SensorID.LS_FRONT.ordinal());
    this.lsRight = new Sensor(SensorID.LS_RIGHT.ordinal());
    this.lsLeft = new Sensor(SensorID.LS_LEFT.ordinal());
  }


  // --------------------------------------------------------------------------------
  // Handlers
  // --------------------------------------------------------------------------------

  /**
   * Append the new data value, `value`, to the sensor data of the sensor associated with sensor
   * ID, `id`, and update the derivative of the sensor data as well as the moving statistics.
   *
   * @param id the sensor ID of the sensor on which to operate
   * @param value the latest data value received from the sensor
   */
  public void sensorDataHandler(SensorID id, float value) {
    this.getSensor(id).dataHandler(value);

    return;
  }


  // --------------------------------------------------------------------------------
  // Methods
  // --------------------------------------------------------------------------------

  /**
   * @param id the sensor ID of the sensor on which to operate
   *
   * @return the number of external classes referencing the specified sensor's data.
   */
  public int getSensorRefs(SensorID id) {
    return this.getSensor(id).getRefs();
  }

  /**
   * Copy the sensor data of the sensor associated with sensor ID, `id`, into `data`.
   *
   * @param id the sensor ID of the sensor on which to operate
   * @param data an array into which the data should be copied
   *
   * @return true if the data was successfully copied, false otherwise
   */
  public boolean getSensorData(SensorID id, float[] data) {
    return this.getSensor(id).getData(data);
  }

  /**
   * Copy into `dataDeriv` the derivative of the sensor data associated with the sensor with
   * sensor ID, `id`.
   *
   * @param id the sensor ID of the sensor on which to operate
   * @param dataDeriv an array into which the derivative of the sensor data should be copied
   *
   * @return true if the data was successfully copied, false otherwise
   */
  public boolean getSensorDataDeriv(SensorID id, float[] dataDeriv) {
    return this.getSensor(id).getDataDeriv(dataDeriv);
  }

  /**
   * @param id the sensor ID of the sensor on which to operate
   *
   * @return the latest data value received from the sensor
   */
  public float getSensorDataLatest(SensorID id) {
    return this.getSensor(id).getDataLatest();
  }

  /**
   * @param id the sensor ID of the sensor on which to operate
   *
   * @return the most-recently calculated derivative of the sensor data
   */
  public float getSensorDataDerivLatest(SensorID id) {
    return this.getSensor(id).getDataDerivLatest();
  }

  /**
   * Copy into `stats` the moving statistics (mean, variance, and standard deviation)
   * associated with the sensor with sensor ID, `id`,
   *
   * @param stats an array into which the moving statistics should be copied
   *
   * @return true if the moving statistics were successfully copied, false otherwise
   */
  public boolean getSensorStats(SensorID id, float[] stats) {
    return this.getSensor(id).getStats(stats);
  }

  /**
   * Increment the number of external classes referencing the data associated with the sensor
   * with sensor ID, `id`.
   *
   * @param id the sensor ID of the sensor on which to operate
   */
  public void incrementSensorRefs(SensorID id) {
    this.getSensor(id).incrementRefs();

    return;
  }

  /**
   * Decrement the number of external classes referencing the data associated with the sensor
   * with sensor ID, `id`.
   *
   * @param id the sensor ID of the sensor on which to operate
   */
  public void decrementSensorRefs(SensorID id) {
    this.getSensor(id).decrementRefs();

    return;
  }


  // --------------------------------------------------------------------------------
  // Helper Routines
  // --------------------------------------------------------------------------------

  /**
   * Return a handle to the Sensor object with sensor ID, `id`.
   *
   * @param the sensor ID of the Sensor object to return
   *
   * @return a handle to the Sensor object, or null if no Sensor object is associated
   * with the given sensor ID
   */
  private Sensor getSensor(SensorID id) {
    Sensor sensor = null;

    switch (id) {
      case US_FRONT:
        sensor = this.usFront;

        break;

      case LS_FRONT:
        sensor = this.lsFront;

        break;

      case LS_RIGHT:
        sensor = this.lsRight;

        break;

      case LS_LEFT:
        sensor = this.lsLeft;

        break;

      default:
        // There is no sensor associated with sensor id, `id`.
        String errorMsg = "error: class SensorData: getSensor(): `id`: Unknown sensor ID";
        System.err.println(errorMsg + ": " + id);

        break;
    }

    return sensor;
  }

}
