package clockSynchronization.algorithms;

import java.util.ArrayList;
import java.util.Random;

import clockSynchronization.base.Client;
import clockSynchronization.base.ClockReader;
import clockSynchronization.base.FaultyClock;
import clockSynchronization.base.FieldMessage;
import clockSynchronization.base.NetworkLatency;
import clockSynchronization.base.Message;
import clockSynchronization.base.NetworkProxy;
import clockSynchronization.base.NetworkQueue;

public class Berkeley
{
	/**
	 * Berkeley's clock synchronization algorithm. Node 0: master
	 * 
	 */
	public static void main(String[] args)
	{
		NetworkQueue queue = new NetworkQueue();

		/*
		 * FaultyClock badClock0 = new FaultyClock(.01, 1e6); FaultyClock
		 * badClock1 = new FaultyClock(.02, 1e6); FaultyClock badClock2 = new
		 * FaultyClock(-.015, 1e6); FaultyClock badClock3 = new
		 * FaultyClock(-.01, 1e6);
		 */

		FaultyClock badClock0 = new FaultyClock(.0, 1e6);
		FaultyClock badClock1 = new FaultyClock(.0, 1e6);
		FaultyClock badClock2 = new FaultyClock(0, 1e6);
		FaultyClock badClock3 = new FaultyClock(0, 1e6);

		NetworkLatency latency = new Latency();

		NetworkProxy proxy1 = new NetworkProxy(queue, badClock0, latency);
		NetworkProxy proxy2 = new NetworkProxy(queue, badClock1, latency);
		NetworkProxy proxy3 = new NetworkProxy(queue, badClock2, latency);
		NetworkProxy proxy4 = new NetworkProxy(queue, badClock3, latency);

		BerkeleySlaveClient slave1 = new BerkeleySlaveClient(proxy2, 1);
		BerkeleySlaveClient slave2 = new BerkeleySlaveClient(proxy3, 2);
		BerkeleySlaveClient slave3 = new BerkeleySlaveClient(proxy4, 3);

		ArrayList<BerkeleySlaveClient> slaves = new ArrayList<BerkeleySlaveClient>();

		slaves.add(slave1);
		slaves.add(slave2);
		slaves.add(slave3);

		ClockReader cr = new ClockReader();

		cr.addClock(badClock0);
		cr.addClock(badClock1);
		cr.addClock(badClock2);
		cr.addClock(badClock3);

		BerkeleyMasterClient master = new BerkeleyMasterClient(proxy1, 0, slaves);

		for (BerkeleySlaveClient slave : slaves)
		{
			new Thread(slave).start();
			// System.out.printf("Slave %d running...%n", slave.getID());
		}

		new Thread(master).start();
		// System.out.println("Master running...");

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
			// return 500000000L;
		}

	}

	static class BerkeleyMasterClient extends Client
	{

		private int id;
		private ArrayList<BerkeleySlaveClient> slaves;

		public BerkeleyMasterClient(NetworkProxy proxy, int id, ArrayList<BerkeleySlaveClient> slaves)
		{
			super(proxy);
			this.id = id;
			this.slaves = slaves;
		}

		@Override
		public void run()
		{
			proxy.setID(this.id);

			long[] slaveTimeDiffs = new long[slaves.size()];
			int[] sourceId = new int[1];
			while (true)
			{
				try
				{
					Thread.sleep(2000);
				}
				catch (InterruptedException e)
				{
				}

				long startTime = proxy.getTime();
				for (int i = 0; i < slaves.size(); i++)
				{
					proxy.sendMessage(new FieldMessage<Long>(0L), i + 1);
				}

				for (int i = 0; i < slaves.size(); i++)
				{
					FieldMessage<Long> msg = (FieldMessage<Long>) proxy.recvAnyMessage(sourceId);
					slaveTimeDiffs[sourceId[0] - 1] = startTime - msg.getMsg() + (proxy.getTime() - startTime) / 2;
				}

				long avgTimeDiff = 0;

				for (int i = 0; i < slaves.size(); i++)
				{
					avgTimeDiff += slaveTimeDiffs[i];
				}

				avgTimeDiff /= slaves.size() + 1;

				for (int i = 0; i < slaves.size(); i++)
				{
					proxy.sendMessage(new FieldMessage<Long>(slaveTimeDiffs[i] - avgTimeDiff), i + 1);
				}

				proxy.setTime(proxy.getTime() - avgTimeDiff);

			}

		}
	}

	static class BerkeleySlaveClient extends Client
	{
		/**
		 * Passing IntegerMessage to slaves signals a request for time. Passing
		 * a LongMessage indicates that message contains a clock update.
		 */
		private int id;

		public BerkeleySlaveClient(NetworkProxy proxy, int id)
		{
			super(proxy);
			this.id = id;
		}

		public int getID()
		{
			return this.id;
		}

		@Override
		public void run()
		{
			proxy.setID(this.id);
			boolean waitingForRequest = true;
			while (true)
			{
				FieldMessage<Long> recvmsg = (FieldMessage<Long>) proxy.recvMessage(0);
				// Check message type and handle accordingly
				if (waitingForRequest)
				{
					// System.out.printf("Time request message received at slave: %d%n", this.id);
					proxy.sendMessage(new FieldMessage<Long>(proxy.getTime()), 0);
					waitingForRequest = false;

				}
				else
				{
					// System.out.printf("Time adjustment received at slave:
					// %d%n", this.id);
					proxy.setTime(proxy.getTime() + (long) recvmsg.getMsg());
					// System.out.printf("New time at slave %d -> %d%n",this.id, proxy.getTime());
					waitingForRequest = true;
				}

			}

		}
	}

}