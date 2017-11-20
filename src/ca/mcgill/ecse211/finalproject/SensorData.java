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
  private final int COLOR_DATA_SIZE = 20;

  // Reference counts of other objects accessing sensor data
  private int llRefs;
  private int usRefs;
  private int colorRefs;

  // Locks
  private final Object llRefsLock;
  private final Object usRefsLock;
  private final Object colorRefsLock;
  private final Object llDataLock;
  private final Object usDataLock;
  private final Object colorDataLock;
  private final Object llDataDerivLock;
  private final Object usDataDerivLock;
  private final Object llStatsLock;
  private final Object usStatsLock;

  // Circular arrays holding the original sensor data
  private float llData1[];
  private float llData2[];
  private float colorData[];
  private float usData[];

  // Circular arrays holding the derivative of the sensor data
  private float llDataDeriv1[];
  private float llDataDeriv2[];
  private float usDataDeriv[];
  // The next index at which data should be placed in the circular arrays
  private int llIndex1; // sensor left
  private int llIndex2; // sensor right
  private int colorIndex; // sensor mid
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
  private float[] llStats1;
  private float[] usStats;

  /**
   * Constructor
   */
  public SensorData() {
    this.llRefs = 0;
    this.usRefs = 0;
    this.colorRefs = 0;

    this.llData1 = new float[LL_DATA_SIZE];
    this.llData2 = new float[LL_DATA_SIZE];
    this.colorData = new float[LL_DATA_SIZE];
    this.usData = new float[US_DATA_SIZE];
    this.llDataDeriv1 = new float[LL_DATA_SIZE];
    this.llDataDeriv2 = new float[LL_DATA_SIZE];
    this.usDataDeriv = new float[US_DATA_SIZE];

    this.llIndex1 = 0;
    this.llIndex2 = 0;
    this.colorIndex = 0;
    this.usIndex = 0;

    this.llFilled = false;
    this.usFilled = false;

    this.llStats1 = new float[] {0.0f, 0.0f, 0.0f};
    this.usStats = new float[] {0.0f, 0.0f, 0.0f};

    llRefsLock = new Object();
    usRefsLock = new Object();
    colorRefsLock = new Object();
    llDataLock = new Object();
    usDataLock = new Object();
    colorDataLock = new Object();
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
  public void lightLevelHandler(float value, int selection) {
    synchronized (this.llDataLock) {
      // Update moving statistics.
//      synchronized (this.llStatsLock) {
//        if (selection == 1) {
//          this.updateMovingStatistics(this.llStats1, this.llData1, this.llIndex1, value);
//        }
//      }

      // Insert latest sample.
      switch (selection) {
        case 1:
          this.llData1[this.llIndex1] = value;
          break;
        case 2:
          this.llData2[this.llIndex2] = value;
          break;
      }

      // Insert latest sample derivative.
      synchronized (this.llDataDerivLock) {
        float lastValue = 0.f; 
        switch (selection) {
          case 1:
            lastValue = this.llData1[(this.llIndex1 - 1 + LL_DATA_SIZE) % LL_DATA_SIZE];
            this.llDataDeriv1[this.llIndex1] = value - lastValue;
            break;
          case 2:
            lastValue = this.llData2[(this.llIndex2 - 1 + LL_DATA_SIZE) % LL_DATA_SIZE];
            this.llDataDeriv2[this.llIndex2] = value - lastValue;
            break;
        }
      }

      switch (selection) {
        case 1:
          this.llIndex1++;
          this.llIndex1 %= this.LL_DATA_SIZE;
          break;
        case 2:
          this.llIndex2++;
          this.llIndex2 %= this.LL_DATA_SIZE;
          break;
      }

      if (!(this.llFilled) && this.llIndex1 == 0) {
        // Our circular array is now filled.
        this.llFilled = true;
      }
    }
  }
  
  /**
   * Handler method to be called by the sensor poller.
   * 
   * @param value the RGB value read by the front color sensor.
   */
  public void colorHandler(float value) {
    synchronized (colorDataLock) {
      this.colorData[colorIndex] = value;
      this.colorIndex++;
      this.colorIndex %= COLOR_DATA_SIZE;
    }
  }

  /**
   * Handler method to be called by an UltrasonicPoller object.
   *
   * @param value the latest data value returned by the ultrasonic sensor
   */
  public void ultrasonicHandler(float value) {
    synchronized (this.usDataLock) {
      // Update moving statistics.
      synchronized (this.usStatsLock) {
        this.updateMovingStatistics(this.usStats, this.usData, this.usIndex, value);
      }

      // Insert latest sample.
      this.usData[this.usIndex] = value;

      // Insert latest sample derivative.
      synchronized (this.usDataDerivLock) {
        float lastValue = this.usData[(this.usIndex - 1 + US_DATA_SIZE) % US_DATA_SIZE];
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
   * @return the number of external objects which use the ultrasonic sensor data provided by this
   *         object
   */
  public synchronized int getUSRefs() {
    return this.usRefs;
  }
  
  /**
   * Get the number of external objects which access the color data.
   * 
   * @return the number of external objects which use the color sensor data provided by this object
   */
  public synchronized int getColorRefs() {
    return this.colorRefs;
  }

  /**
   * Get a copy of the original light sensor data.
   *
   * @return a double array holding a copy of the original light sensor data
   */
  public float[] getLLData(int selection) {
    float[] data = null;
    if (this.llFilled) {
      synchronized (this.llDataLock) {
        if (selection == 1) {
          data = this.llData1.clone();
        } else if (selection == 2) {
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
   * @param selection the light sensor you want to get data from (1 = left, 2 = right, 3 = middle)
   * @return the latest light sensor data value
   */
  public float getLLDataLatest(int selection) {
    float value;
    // We can safely assume that at least one value has been recorded.
    synchronized (this.llDataLock) {
      if (selection == 1) {
        value = this.llData1[(this.llIndex1 - 1 + LL_DATA_SIZE) % LL_DATA_SIZE];
      } else if (selection == 2) {
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
  public float[] getUSData() {
    float[] data = null;
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
  public float getUSDataLatest() {
    float value;
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
  public float[] getLLDataDeriv() {
    float[] data = null;
    if (this.llFilled) {
      synchronized (this.llDataDerivLock) {
        data = this.llDataDeriv1.clone();
      }
    }
    return data;
  }

  /**
   * Get the latest derivate of the data polled from the light sensor.
   *
   * @return the latest derivative of the light sensor data
   */
  public float getLLDataDerivLatest(int selection) {
    float value = 0.f;
    // We can safely assume that at least one value has been recorded.
    synchronized (this.llDataDerivLock) {
      if (selection == 1) {
        value = this.llDataDeriv1[(this.llIndex1 - 1 + LL_DATA_SIZE) % LL_DATA_SIZE];
      } else if (selection == 2) {
        value = this.llDataDeriv2[(this.llIndex2 - 1 + LL_DATA_SIZE) % LL_DATA_SIZE];
      }
    }
    return value;
  }

  /**
   * Get a copy of the derivate of the ultrasonic sensor data.
   *
   * @return a double array holding a copy of the derive of the original ultrasonic sensor data
   */
  public float[] getUSDataDeriv() {
    float[] data = null;
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
   * Get the latest value polled from the front color sensor.
   * 
   * @return the latest RGB value polled from the front color sensor.
   */
  public float getColorDataLatest() {
    float val;
    synchronized (this.colorDataLock) {
      val = this.colorData[(this.colorIndex - 1 + COLOR_DATA_SIZE) % COLOR_DATA_SIZE];
    }
    return val;
  }
  
  /**
   * Get the last color read by the front sensor, as an integer.
   * 
   * @Return the latest color read by the sensor, as an integer from 1 to 4, 0 if not recognized
   */
  public int getColorLatest() {
    int color = 0;
    synchronized (colorDataLock) {
      float colorData = this.colorData[(this.colorIndex - 1 + COLOR_DATA_SIZE) % COLOR_DATA_SIZE];
      if (colorData > 9 && colorData < 17) {
        color = 1; // RED 
      } else if (colorData > 1 && colorData < 8) {
        color = 2; // BLUE
      } else if (colorData > 15 && colorData < 28) {
        color = 3; // YELLOW
      } else if (colorData > 28 && colorData < 40) {
        color = 4; // WHITE
      }
    }
    return color;
  }
  
  /**
   * Get the moving statistics of the light sensor data.
   *
   * @return a double array holding the average, variance, and standard deviation of the light
   *         sensor data
   */
  public float[] getLLStats() {
    float[] stats = null;
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
   * @return a double array holding the average, variance, and standard deviation of the ultrasonic
   *         sensor data
   */
  public float[] getUSStats() {
    float[] stats = null;
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
   * Increment by one the number of external objects accessing the color sensor data
   */
  public int incrementColorRefs() {
    int refs;
    synchronized (colorData) {
      refs = ++this.colorRefs;
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
   * Decrement by one the number of external objects accessing the color sensor data
   */
  public int decrementColorRefs() {
    int refs;
    synchronized (colorData) {
      refs = --this.colorRefs;
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
  private void updateMovingStatistics(float[] stats, float[] data, int index, float val) {
    float old = data[index];

    float n = data.length;

    float oldAvg = stats[0];
    float oldVar = stats[1];
    float oldDev = stats[2];

    //
    // Reference:
    //
    // [See http://jonisalonen.com/2014/efficient-and-accurate-rolling-standard-deviation]
    //
    float newAvg = oldAvg + (val - old) / n;
    float newVar = oldVar + (val - old) * (val - newAvg + old - oldAvg) / (n - 1);
    float newDev = (float) Math.sqrt(newVar);

    stats[0] = newAvg;
    stats[1] = newVar;
    stats[2] = newDev;
  }
}
