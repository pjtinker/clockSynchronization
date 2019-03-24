package clockSynchronization.base;

public class LongMessage implements Message
{
	public long l;

	public LongMessage(long l)
	{
		this.l = l;
	}

	public Long getMsg()
	{
		return this.l;
	}
}
