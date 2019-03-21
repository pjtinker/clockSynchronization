package clockSynchronization.base;
import java.util.PriorityQueue;

public class NetworkQueue
{
	private PriorityQueue<MessageAndTime> queue;

	public NetworkQueue()
	{
		queue = new PriorityQueue<MessageAndTime>();
	}
	
	public synchronized void sendMessage(Message message, long sendTime, int source, int destination)
	{
		MessageAndTime mat = new MessageAndTime();
		mat.time = sendTime;
		mat.message = message;
		mat.source = source;
		mat.destination = destination;
		queue.add(mat);
		this.notifyAll();
	}

	public synchronized Message recvMessage(int source, int destination)
	{
		while (queue.isEmpty() || queue.peek().destination != destination || queue.peek().source != source)
		{
			try
			{
				this.wait();
			}
			catch (InterruptedException e)
			{
			}
		}
		while (queue.peek().time - System.nanoTime() > 0)
		{
		}
		MessageAndTime mat = queue.poll();
		this.notifyAll();
		return mat.message;

	}

	private static class MessageAndTime implements Comparable<MessageAndTime>
	{
		public long time;
		public Message message;
		public int source;
		public int destination;

		@Override
		public int compareTo(MessageAndTime o)
		{
			if (time < o.time)
				return -1;
			if (time > o.time)
				return 1;
			return 0;
		}

	}
}
