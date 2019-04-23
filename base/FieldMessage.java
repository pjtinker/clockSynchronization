package clockSynchronization.base;

public class FieldMessage<T> implements Message
{
	private T t;
	
    public FieldMessage(T t)
    {
    	this.t = t;
    }
    
    public T getMsg()
    {
    	return t;
    }
}
