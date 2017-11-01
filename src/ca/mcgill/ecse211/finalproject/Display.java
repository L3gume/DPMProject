package ca.mcgill.ecse211.finalproject;

import lejos.hardware.lcd.TextLCD;

/**
 * Handles displaying information on the EV3's lcd panel
 *
 * @author Josh Inscoe
 */
public class Display extends Thread {

	// --------------------------------------------------------------------------------
	// Constants
	// --------------------------------------------------------------------------------
	private static final long DISPLAY_PERIOD = 250;
	private Odometer odo;
	private SensorData sd;
	private UltrasonicPoller up;
	private TextLCD t;
	
	
	// --------------------------------------------------------------------------------
	// Variables
	// --------------------------------------------------------------------------------

	/**
	 * Constructor
	 *
	 * @param t
	 *            TextLCD to write to
	 * @param odo
	 *            Odometer to get position and heading from
	 * @param mc
	 *            MainController to get state information
	 * @param sd
	 *            SensorData object to get sensor readings
	 */
	public Display(final TextLCD t, Odometer odo, MainController mc, SensorData sd, UltrasonicPoller up) {
		this.t = t;
		this.odo = odo;
		//this.mc = mc;
		this.sd = sd;
		this.up = up;
	}

	/**
	 * run() method, updates the displayed information periodically.
	 */
	public void run() {
		long displayStart, displayEnd;
		double[] position = new double[3];

		// clear the display once
		t.clear();

		while (true) {
			displayStart = System.currentTimeMillis();

			// clear the lines for displaying odometry information
			t.drawString("X:              ", 0, 0);
			t.drawString("Y:              ", 0, 1);
			t.drawString("T:              ", 0, 2);

			// get the odometry information
			odo.getPosition(position, new boolean[] { true, true, true });

			// display odometry information
			for (int i = 0; i < 3; i++) {
				t.drawString(formattedDoubleToString(position[i], 2), 3, i);
			}

			t.drawString("dist: " + sd.getUSDataLatest(), 0, 5);
			t.drawString("dist: " + up.getLastDist(), 0, 6);
//			t.drawString("distance: " + ul.getDist(), 0, 4);
//			t.drawString("Light Level: " + cp.lightl, 0, 5);
//			t.drawString("State: " + cont.getCurrentState(), 0, 6); // Display the current state of the controller
//			t.drawString("Sub: " + cont.getCurSubState(), 0, 7); // Display the substate.

			// throttle the OdometryDisplay
			displayEnd = System.currentTimeMillis();
			if (displayEnd - displayStart < DISPLAY_PERIOD) {
				try {
					Thread.sleep(DISPLAY_PERIOD - (displayEnd - displayStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that OdometryDisplay will be interrupted
					// by another thread
				}
			}
		}
	}

	/**
	 * Format a double to a String.
	 * 
	 * @param x
	 *            - double to be converted
	 * @param places
	 *            - number of decimal places
	 * @return double in form of String
	 */
	private static String formattedDoubleToString(double x, int places) {
		String result = "";
		String stack = "";
		long t;

		// put in a minus sign as needed
		if (x < 0.0)
			result += "-";

		// put in a leading 0
		if (-1.0 < x && x < 1.0)
			result += "0";
		else {
			t = (long) x;
			if (t < 0)
				t = -t;

			while (t > 0) {
				stack = Long.toString(t % 10) + stack;
				t /= 10;
			}

			result += stack;
		}

		// put the decimal, if needed
		if (places > 0) {
			result += ".";

			// put the appropriate number of decimals
			for (int i = 0; i < places; i++) {
				x = Math.abs(x);
				x = x - Math.floor(x);
				x *= 10.0;
				result += Long.toString((long) x);
			}
		}

		return result;
	}
}
