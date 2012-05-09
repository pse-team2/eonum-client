package ch.eonum;

import java.util.Date;

/**
 * Timer class for timing and logging.
 */
public class Timer
{
	long _startTime;

	/**
	 * Creates a new Timer instance or resets an existing one.
	 */
	public Timer()
	{
		this.reset();
	}

	/**
	 * Sets the Timer back to zero.
	 */
	public void reset()
	{
		_startTime = this.timeNow();
	}

	/**
	 * Returns the time elapsed. Does not reset the timer.
	 * 
	 * @return Elapsed time in milliseconds.
	 */
	public long timeElapsed()
	{
		return this.timeNow() - _startTime;
	}

	/**
	 * Returns the actual time.
	 * 
	 * @return Actual System Time in milliseconds.
	 * @see Date#getTime()
	 */
	protected long timeNow()
	{
		return new Date().getTime();
	}
}
