package clockSynchronization.base;

public class IntegerMessage implements Message
{
	public int i;

	public IntegerMessage(int i)
	{
		this.i = i;
	}

	public Integer getMsg()
	{
		return this.i;
	}
}
