package clockSynchronization.base;

import java.util.Random;

public class FaultyClock implements Clock
{
	double drift; // ratio of deviation: s/s or nanosecond/nanosecond
	double precision; // stdev of accuracy in nanoseconds
	long time;
	long setTime;
	Random rand;

	public FaultyClock(double drift, double precision)
	{
		rand = new Random();
		this.drift = drift;
		this.precision = precision;
		this.time=System.nanoTime();
		this.setTime=time;
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

}
