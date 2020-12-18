package ezj.exception;

public class EZJNoFieldWriteAccess extends EZJException
{

	public EZJNoFieldWriteAccess(Class c, String fieldName)
	{
		super("Can't write field \""+fieldName+"\" from "+c.getCanonicalName()+". The field needs to be public or needs a setter named \"set\" followed by the field name with the 1st letter in upper case or named like the field with a single paramter that is the same type of the field.");
	}

}
