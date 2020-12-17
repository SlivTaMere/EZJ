package ezj.exception;

public class EZJInstantiationError extends EZJException
{

	public EZJInstantiationError(Class c, ReflectiveOperationException e)
	{
		super("Coud not instantiate "+c.getCanonicalName()+". The class needs a public default constructor without parameters.", e);
	}
	
	public EZJInstantiationError(Class c)
	{
		super("Coud not instantiate "+c.getCanonicalName()+". The class needs a public default constructor without parameters.");
	}

}
