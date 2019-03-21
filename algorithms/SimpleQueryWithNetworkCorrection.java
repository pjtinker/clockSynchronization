package clockSynchronization.algorithms;

import java.util.Random;

import clockSynchronization.base.Client;
import clockSynchronization.base.ClockReader;
import clockSynchronization.base.FaultyClock;
import clockSynchronization.base.LongMessage;
import clockSynchronization.base.NetworkLatency;
import clockSynchronization.base.NetworkProxy;
import clockSynchronization.base.NetworkQueue;

public class SimpleQueryWithNetworkCorrection
{
	public static void main(String[] args)
	{
		NetworkQueue queue = new NetworkQueue();
		FaultyClock goodClock = new FaultyClock(0, 0);
		FaultyClock badClock = new FaultyClock(.01, 1e6);
		// RandomWalkClock badClock = new RandomWalkClock(0, 1e5, 100000);
		// RandomWalkDriftClock badClock = new RandomWalkDriftClock(.01, .001,
		// 100000, 1e6);

		NetworkLatency latency = new Latency();

		NetworkProxy proxy1 = new NetworkProxy(queue, goodClock, latency);
		NetworkProxy proxy2 = new NetworkProxy(queue, badClock, latency);

		GoodClient good = new GoodClient(proxy1);
		BadClient bad = new BadClient(proxy2);

		new Thread(good).start();
		new Thread(bad).start();

		ClockReader cr = new ClockReader();
		cr.addClock(goodClock);
		cr.addClock(badClock);

		cr.printClocks(50);
	}

	static class Latency implements NetworkLatency
	{
		Random rand;

		public Latency()
		{
			rand = new Random();
		}

		@Override
		public long getLatency(int source, int destination)
		{
			return (long) (500000000L + rand.nextGaussian() * 10000000);
		}

	}

	static class GoodClient extends Client
	{

		public GoodClient(NetworkProxy proxy)
		{
			super(proxy);
		}

		@Override
		public void run()
		{
			proxy.setID(1);
			while (true)
			{
				proxy.recvMessage(2);
				proxy.sendMessage(new LongMessage(proxy.getTime()), 2);
			}
		}

	}

	static class BadClient extends Client
	{

		public BadClient(NetworkProxy proxy)
		{
			super(proxy);
		}

		@Override
		public void run()
		{
			proxy.setID(2);
			while (true)
			{
				long startTime = proxy.getTime();
				proxy.sendMessage(new LongMessage(0), 1);
				LongMessage lm = (LongMessage) proxy.recvMessage(1);
				long timeToSendAndReceive = proxy.getTime() - startTime;
				proxy.setTime(lm.l + timeToSendAndReceive / 2);
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
				}
			}
		}

	}
}
