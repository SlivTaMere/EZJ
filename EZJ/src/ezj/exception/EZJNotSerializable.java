package ezj.exception;

import ezj.IEZJSerializable;

public class EZJNotSerializable extends EZJException
{

	public EZJNotSerializable(Object o)
	{
		super(o.getClass().getCanonicalName()+" does not implements "+IEZJSerializable.class.getCanonicalName()+" and can't be serialized as a JSON primitive.");
	}

}
