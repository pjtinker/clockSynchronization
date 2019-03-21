package clockSynchronization.algorithms;

import clockSynchronization.base.ClockReader;
import clockSynchronization.base.FaultyClock;
import clockSynchronization.base.RandomWalkClock;
import clockSynchronization.base.RandomWalkDriftClock;

public class ClockTesting
{
	public static void main(String[] args)
	{
		ClockReader reader = new ClockReader();
		RandomWalkClock rc = new RandomWalkClock(0, 1e5, 100000);
		FaultyClock fc = new FaultyClock(.01, 1e6);
		RandomWalkDriftClock dc = new RandomWalkDriftClock(.01, .0001, 100000, 1e5);

		reader.addClock(fc);
		reader.addClock(rc);
		reader.addClock(dc);

		reader.printClocks(50);
	}

}
