package clockSynchronization.base;

public class NetworkProxy
{
	private NetworkQueue queue;
	private NetworkLatency latency;
	private Clock clock;
	private int id;

	public NetworkProxy(NetworkQueue queue, Clock clock, NetworkLatency latency)
	{
		this.queue = queue;
		this.clock = clock;
		this.latency = latency;
		clock.setTime(System.nanoTime());
	}

	public long getTime()
	{
		return clock.getTime();
	}

	public void setTime(long time)
	{
		clock.setTime(time);
	}

	public void sendMessage(Message m, int destination)
	{
		queue.sendMessage(m, System.nanoTime() + latency.getLatency(id, destination), id, destination);
	}

	public Message recvMessage(int source)
	{
		return queue.recvMessage(source, id);
	}

	public void setID(int id)
	{
		this.id = id;
	}
}
