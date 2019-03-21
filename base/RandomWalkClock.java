package clockSynchronization.base;

import java.util.Random;

public class RandomWalkClock implements Clock, Runnable
{
	private long time;
	private long setTime;
	private Random rand;
	private double mean;
	private double stdev;
	private int interval;

	public RandomWalkClock(double mean, double stdev, int interval)
	{
		rand = new Random();
		this.mean = mean;
		this.stdev = stdev;
		this.interval = interval;
		this.time = System.nanoTime();
		this.setTime = time;
		new Thread(this).start();
	}

	@Override
	public void setTime(long time)
	{
		this.time = time;
		this.setTime = time;
	}

	@Override
	public long getTime()
	{
		return time;
	}

	@Override
	public void run()
	{
		while (true)
		{
			long currentTime = System.nanoTime();
			long elapsedTime = currentTime - setTime;
			time += elapsedTime;
			time += ((rand.nextGaussian() * stdev + mean) * elapsedTime) / interval;
			setTime = currentTime;
			try
			{
				Thread.sleep(0, interval);
			}
			catch (InterruptedException e)
			{
			}
		}

	}

}
