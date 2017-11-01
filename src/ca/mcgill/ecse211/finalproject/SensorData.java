package ca.mcgill.ecse211.finalproject;

/**
 * Handles processing the sensor data and facilitates access to it.
 *
 * @author Josh Inscoe
 */
public class SensorData {

  // Constants
  private final int LL_DATA_SIZE = 20;
  private final int US_DATA_SIZE = 20;

  // Reference counts of other objects accessing sensor data
  private int llRefs;
  private int usRefs;

  // Locks
  private final Object llRefsLock;
  private final Object usRefsLock;
  private final Object llDataLock;
  private final Object usDataLock;
  private final Object llDataDerivLock;
  private final Object usDataDerivLock;
  private final Object llStatsLock;
  private final Object usStatsLock;

  // Circular arrays holding the original sensor data
  private double llData1[];
  private double llData2[];
  private double llData3[];
  private double usData[];

  // Circular arrays holding the derivative of the sensor data
  private double llDataDeriv[];
  private double usDataDeriv[];

  // The next index at which data should be placed in the circular arrays
  private int llIndex1; // sensor left
  private int llIndex2; // sensor right
  private int llIndex3; // sensor mid
  private int usIndex;

  // Boolean values signalling whether circular arrays are filled
  private boolean llFilled;
  private boolean usFilled;

  //
  // The moving statistics of the values in each circular array
  //
  // Index:
  //
  // 0 - moving average
  // 1 - moving variance
  // 2 - moving standard deviation
  //
  private double[] llStats1;
  private double[] llStats2;
  private double[] llStats3;
  private double[] usStats;

  /**
   * Constructor
   */
  public SensorData() {
    this.llRefs = 0;
    this.usRefs = 0;

    this.llData1 = new double[LL_DATA_SIZE];
    this.llData2 = new double[LL_DATA_SIZE];
    this.llData3 = new double[LL_DATA_SIZE];
    this.usData = new double[US_DATA_SIZE];
    this.llDataDeriv = new double[LL_DATA_SIZE];
    this.usDataDeriv = new double[US_DATA_SIZE];

    this.llIndex1 = 0;
    this.llIndex2 = 0;
    this.llIndex3 = 0;
    this.usIndex = 0;

    this.llFilled = false;
    this.usFilled = false;

    this.llStats1 = new double[] { 0.0f, 0.0f, 0.0f };
    this.llStats2 = new double[] { 0.0f, 0.0f, 0.0f };
    this.llStats3 = new double[] { 0.0f, 0.0f, 0.0f };
    this.usStats = new double[] { 0.0f, 0.0f, 0.0f };

    llRefsLock = new Object();
    usRefsLock = new Object();
    llDataLock = new Object();
    usDataLock = new Object();
    llDataDerivLock = new Object();
    usDataDerivLock = new Object();
    llStatsLock = new Object();
    usStatsLock = new Object();
  }

  /**
   * Handler method to be called by a LightPoller object.
   *
   * @param value the latest data value returned by the light sensor
   */
  public void lightLevelHandler(double value, int sel) {
    synchronized (this.llDataLock) {
      // Update moving statistics.
      synchronized (this.llStatsLock) {
        if (sel == 1) {
          this.updateMovingStatistics(this.llStats1, this.llData1, this.llIndex1, value);
        }
      }


      // Insert latest sample.
      switch (sel) {
        case 1: this.llData1[this.llIndex1++] = value; break;
        case 2: this.llData2[this.llIndex2++] = value; break;
        case 3: this.llData3[this.llIndex3++] = value; break;
      }

      // Insert latest sample derivative.
      synchronized (this.llDataDerivLock) {
        if (sel == 1) {
          double lastValue = this.llData1[(this.llIndex1 - 1 + LL_DATA_SIZE) % LL_DATA_SIZE];
          this.llDataDeriv[this.llIndex1] = value - lastValue;
        }
      }

      this.llIndex1 %= this.LL_DATA_SIZE;
      this.llIndex2 %= this.LL_DATA_SIZE;
      this.llIndex3 %= this.LL_DATA_SIZE;
      if (!(this.llFilled) && this.llIndex1 == 0) {
        // Our circular array is now filled.
        this.llFilled = true;
      }
    }
  }

  /**
   * Handler method to be called by an UltrasonicPoller object.
   *
   * @param value the latest data value returned by the ultrasonic sensor
   */
  public void ultrasonicHandler(double value) {
    synchronized (this.usDataLock) {
      // Update moving statistics.
      synchronized (this.usStatsLock) {
        this.updateMovingStatistics(this.usStats, this.usData, this.usIndex, value);
      }

      // Insert latest sample.
      this.usData[this.usIndex] = value;

      // Insert latest sample derivative.
      synchronized (this.usDataDerivLock) {
        double lastValue = this.usData[(this.usIndex - 1 + US_DATA_SIZE) % US_DATA_SIZE];
        this.usDataDeriv[this.usIndex] = value - lastValue;
      }

      this.usIndex += 1;
      this.usIndex %= this.US_DATA_SIZE;
      if (!(this.usFilled) && this.usIndex == 0) {
        // Our circular array is now filled.
        this.usFilled = true;
      }
    }
  }

  /**
   * Get the number of external objects which access the light sensor data.
   *
   * @return the number of external objects which use the light sensor data provided by this object
   */
  public synchronized int getLLRefs() {
    return this.llRefs;
  }

  /**
   * Get the number of external objects which access the ultrasonic sensor data.
   *
   * @return the number of external objects which use the ultrasonic sensor data provided by this object
   */
  public synchronized int getUSRefs() {
    return this.usRefs;
  }

  /**
   * Get a copy of the original light sensor data.
   *
   * @return a double array holding a copy of the original light sensor data
   */
  public double[] getLLData(int sel) {
    double[] data = null;
    if (this.llFilled) {
      synchronized (this.llDataLock) {
        if (sel == 1) {
          data = this.llData1.clone();
        } else if (sel == 2) {
          data = this.llData2.clone();
        } else {
          data = null;
        }
      }
    }
    return data;
  }

  /**
   * Get the latest data value polled from the light sensor.
   *
   * @return the latest light sensor data value
   */
  public double getLLDataLatest(int sel) {
    double value;
    // We can safely assume that at least one value has been recorded.
    synchronized (this.llDataLock) {
      if (sel == 1) {
        value = this.llData1[(this.llIndex1 - 1 + LL_DATA_SIZE) % LL_DATA_SIZE];
      } else if (sel == 2) {
        value = this.llData2[(this.llIndex2 - 1 + LL_DATA_SIZE) % LL_DATA_SIZE];
      } else {
        value = 0;
      }
    }
    return value;
  }

  /**
   * Get a copy of the original ultrasonic sensor data.
   *
   * @return a double array holding a copy of the original ultrasonic sensor data
   */
  public double[] getUSData() {
    double[] data = null;
    if (this.usFilled) {
      synchronized (this.usDataLock) {
        data = this.usData.clone();
      }
    }
    return data;
  }

  /**
   * Get the latest data value polled from the ultrasonic sensor.
   *
   * @return the latest ultrasonic sensor data value
   */
  public double getUSDataLatest() {
    double value;
    // We can safely assume that at least one value has been recorded.
    synchronized (this.usDataLock) {
      value = this.usData[(this.usIndex - 1 + US_DATA_SIZE) % US_DATA_SIZE];
    }
    return value;
  }

  /**
   * Get a copy of the derivate of the light sensor data.
   *
   * @return a double array holding a copy of the derive of the original light sensor data
   */
  public double[] getLLDataDeriv() {
    double[] data = null;
    if (this.llFilled) {
      synchronized (this.llDataDerivLock) {
        data = this.llDataDeriv.clone();
      }
    }
    return data;
  }

  /**
   * Get the latest derivate of the data polled from the light sensor.
   *
   * @return the latest derivative of the light sensor data
   */
  public double getLLDataDerivLatest() {
    double value;
    // We can safely assume that at least one value has been recorded.
    synchronized (this.llDataDerivLock) {
      value = this.llDataDeriv[(this.llIndex1 - 1 + LL_DATA_SIZE) % LL_DATA_SIZE];
    }
    return value;
  }

  /**
   * Get a copy of the derivate of the ultrasonic sensor data.
   *
   * @return a double array holding a copy of the derive of the original ultrasonic sensor data
   */
  public double[] getUSDataDeriv() {
    double[] data = null;
    if (this.usFilled) {
      synchronized (this.usDataDerivLock) {
        data = this.usDataDeriv.clone();
      }
    }
    return data;
  }

  /**
   * Get the latest derivate of the data polled from the ultrasonic sensor.
   *
   * @return the latest derivative of the ultrasonic sensor data
   */
  public double getUSDataDerivLatest() {
    double value;
    // We can safely assume that at least one value has been recorded.
    synchronized (this.usDataDerivLock) {
      value = this.usDataDeriv[(this.usIndex - 1 + US_DATA_SIZE) % US_DATA_SIZE];
    }
    return value;
  }

  /**
   * Get the moving statistics of the light sensor data.
   *
   * @return a double array holding the average, variance, and standard deviation of the light sensor data
   */
  public double[] getLLStats() {
    double[] stats = null;
    if (this.llFilled) {
      synchronized (this.llStatsLock) {
        stats = this.llStats1.clone();
      }
    }
    return stats;
  }

  /**
   * Get the moving statistics of the ultrasonic sensor data.
   *
   * @return a double array holding the average, variance, and standard deviation of the ultrasonic sensor data
   */
  public double[] getUSStats() {
    double[] stats = null;
    if (this.usFilled) {
      synchronized (this.usStatsLock) {
        stats = this.usStats.clone();
      }
    }
    return stats;
  }

  /**
   * Increment by one the number of external objects accessing the light sensor data.
   */
  public int incrementLLRefs() {
    int refs;
    synchronized (this.llRefsLock) {
      refs = this.llRefs + 1;
      this.llRefs = refs;
    }
    return refs;
  }

  /**
   * Increment by one the number of external objects accessing the ultrasonic sensor data.
   */
  public int incrementUSRefs() {
    int refs;
    synchronized (this.usRefsLock) {
      refs = this.usRefs + 1;
      this.usRefs = refs;
    }
    return refs;
  }

  /**
   * Decrement by one the number of external objects accessing the light sensor data.
   */
  public int decrementLLRefs() {
    int refs;
    synchronized (this.llRefsLock) {
      refs = this.llRefs - 1;
      this.llRefs = refs;
    }
    return refs;
  }

  /**
   * Decrement by one the number of external objects accessing the ultrasonic sensor data.
   */
  public int decrementUSRefs() {
    int refs;
    synchronized (this.usRefsLock) {
      refs = this.usRefs - 1;
      this.usRefs = refs;
    }
    return refs;
  }

  /**
   * Update the moving statistics, `stats`, of the data samples, `data`.
   *
   * @param stats the moving statistics (average, variance, standard deviation) of our data
   * @param data the sample data on which to compute the moving statistics
   * @param index the index of the data value to remove from the moving statistics
   * @param val the new value to add to the moving statistics
   */
  private void updateMovingStatistics(double[] stats, double[] data, int index, double val) {
    double old = data[index];

    double n = data.length;

    double oldAvg = stats[0];
    double oldVar = stats[1];
    double oldDev = stats[2];

    //
    // Reference:
    //
    // [See http://jonisalonen.com/2014/efficient-and-accurate-rolling-standard-deviation]
    //
    double newAvg = oldAvg + (val - old) / n;
    double newVar = oldVar + (val - old) * (val - newAvg + old - oldAvg) / (n - 1);
    double newDev = (double)Math.sqrt((double)newVar);

    stats[0] = newAvg;
    stats[1] = newVar;
    stats[2] = newDev;
  }
}
