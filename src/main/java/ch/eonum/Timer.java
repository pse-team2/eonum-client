package ch.eonum;

import java.util.Date;

/**
 * Timer class for timing and logging.
 *
 */
public class Timer
{
	long _startTime;

	public Timer()
	{
		this.reset();
	}

	/**
	 * Set Timer back to zero.
	 */
	public void reset()
	{
		_startTime = this.timeNow();
	}

	/**
	 * Returns the time elapsed.
	 */
	public long timeElapsed()
	{
		return this.timeNow() - _startTime;
	}

	/**
	 * Returns the actual time.
	 */
	protected long timeNow()
	{
		return new Date().getTime();
	}
}
