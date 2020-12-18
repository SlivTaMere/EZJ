package ezj.exception;

public class EZJNoFieldReadAccess extends EZJException
{

	public EZJNoFieldReadAccess(Class c, String fieldName)
	{
		super("Can't read field \""+fieldName+"\" from "+c.getCanonicalName()+". The field needs to be public or needs a getter named \"get\", \"is\" or \"has\" followed by the field name with the 1st letter in upper case.");
	}

}
