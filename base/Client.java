package clockSynchronization.base;

public class Client implements Runnable
{
	protected NetworkProxy proxy;

	public Client(NetworkProxy proxy)
	{
		this.proxy = proxy;
	}

	@Override
	public void run()
	{
		// TODO Auto-generated method stub

	}

}
