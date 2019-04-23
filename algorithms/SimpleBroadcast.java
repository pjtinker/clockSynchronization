package clockSynchronization.algorithms;

import java.util.Random;

import clockSynchronization.base.Client;
import clockSynchronization.base.ClockReader;
import clockSynchronization.base.FaultyClock;
import clockSynchronization.base.FieldMessage;
import clockSynchronization.base.NetworkLatency;
import clockSynchronization.base.NetworkProxy;
import clockSynchronization.base.NetworkQueue;

public class SimpleBroadcast
{
	public static void main(String[] args)
	{
		int numNodes = 2;

		Random rand = new Random();

		NetworkQueue queue = new NetworkQueue();
		FaultyClock goodClock = new FaultyClock(0, 0);

		NetworkLatency latency = new Latency();

		NetworkProxy proxy1 = new NetworkProxy(queue, goodClock, latency);
		proxy1.setID(0);

		GoodClient good = new GoodClient(proxy1, numNodes);

		ClockReader cr = new ClockReader();
		cr.addClock(goodClock);

		for (int i = 1; i < numNodes; i++)
		{
			FaultyClock fc = new FaultyClock(i/100.0, 1e6);
			NetworkProxy proxy = new NetworkProxy(queue, fc, latency);
			proxy.setID(i);
			BadClient bad = new BadClient(proxy);
			cr.addClock(fc);
			new Thread(bad).start();
		}

		new Thread(good).start();

		cr.printClocks(50);
	}

	static class Latency implements NetworkLatency
	{

		@Override
		public long getLatency(int source, int destination)
		{
			return 500000000L;
			//return 0;
		}

	}

	static class GoodClient extends Client
	{
		int numNodes;

		public GoodClient(NetworkProxy proxy, int numNodes)
		{
			super(proxy);
			this.numNodes = numNodes;
		}

		@Override
		public void run()
		{
			while (true)
			{
				for (int i = 1; i < numNodes; i++)
					proxy.sendMessage(new FieldMessage<Long>(proxy.getTime()), i);
				try
				{
					Thread.sleep(5000);
				}
				catch (InterruptedException e)
				{
				}
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
			while (true)
			{
				FieldMessage<Long> lm = (FieldMessage<Long>) proxy.recvMessage(0);
				proxy.setTime(lm.getMsg());
			}
		}

	}
}
