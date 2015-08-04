package soba.util;

/**
 * <code>Timer</code> records time consumed by this program.
 */
public class Timer {

	private long startTimestamp;
	private long timestamp;

	/**
	 * Starts a timer.
	 */
	public Timer() {
		timestamp = System.currentTimeMillis();
		startTimestamp = timestamp;
	}
	
	/**
	 * @return consumed milliseconds since a previous checkpoint.
	 */
	public long checkpoint() {
		long oldTimestamp = timestamp;
		timestamp = System.currentTimeMillis();
		return timestamp - oldTimestamp;
	}
	
	/**
	 * @return consumed milliseconds since the timer is created.
	 */
	public long getTotaltime() {
		return System.currentTimeMillis() - startTimestamp;
	}
}
