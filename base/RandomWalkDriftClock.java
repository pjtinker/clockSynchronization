package clockSynchronization.base;

import java.util.Random;

public class RandomWalkDriftClock implements Clock, Runnable

{
	double drift; // ratio of deviation: s/s or nanosecond/nanosecond
	double precision; // stdev of accuracy in nanoseconds
	long time;
	long setTime;
	Random rand;
	double driftStdev;
	int driftInterval;

	public RandomWalkDriftClock(double drift, double driftStdev, int driftInterval, double precision)
	{
		rand = new Random();
		this.drift = drift;
		this.precision = precision;
		this.time = System.nanoTime();
		this.setTime = time;
		this.driftStdev = driftStdev;
		this.driftInterval = driftInterval;
		new Thread(this).start();
	}

	public long getTime()
	{
		long currentAccurateTime = System.nanoTime();
		long elapsedTimeWithDrift = (long) ((currentAccurateTime - setTime) * (drift + 1));
		long gaussianOffset = (long) (rand.nextGaussian() * precision);

		return time + elapsedTimeWithDrift + gaussianOffset;

	}

	public void setTime(long time)
	{
		setTime = System.nanoTime();
		this.time = time;
	}

	@Override
	public void run()
	{
		while (true)
		{
			drift += (rand.nextGaussian() * driftStdev + 0);
			try
			{
				Thread.sleep(0, driftInterval);
			}
			catch (InterruptedException e)
			{
			}
		}

	}

}
