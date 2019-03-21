package clockSynchronization.base;

import java.util.ArrayList;
import java.util.List;

public class ClockReader
{
	private List<Clock> clocks;

	public ClockReader()
	{
		clocks = new ArrayList<Clock>();
	}

	public void addClock(Clock c)
	{
		clocks.add(c);
	}

	public void printClocks(long interval)
	{
		long startTime = System.nanoTime();
		while (true)
		{
			System.out.print((System.nanoTime() - startTime) / 1e9 + "\t");
			for (Clock c : clocks)
			{
				System.out.print((c.getTime() - startTime) / 1e9 + "\t");
			}
			System.out.println((System.nanoTime() - startTime) / 1e9);

			try
			{
				Thread.sleep(interval);
			}
			catch (InterruptedException e)
			{
			}
		}
	}
}
