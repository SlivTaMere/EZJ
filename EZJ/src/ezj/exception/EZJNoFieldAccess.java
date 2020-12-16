package ezj.exception;

public class EZJNoFieldAccess extends EZJException
{

	public EZJNoFieldAccess(Class c, String fieldName)
	{
		super("Can't access to field \""+fieldName+"\" from "+c.getCanonicalName()+". The field needs to be public or needs a getter named \"get\", \"is\" or \"has\" followed by the field name with the 1st letter in upper case.");
	}

}
