package clockSynchronization.base;

public class GridMessage<T> implements Message
{
    private T t;
    private boolean b;
	
    public GridMessage(T t, boolean b)
    {
        this.t = t;
        this.b = b;
    }
    
    public T getMsg()
    {
    	return t;
    }

    public boolean getBool()
    {
        return b;
    }
}