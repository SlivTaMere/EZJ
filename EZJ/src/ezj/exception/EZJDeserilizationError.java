package ezj.exception;

import javax.json.JsonValue;

public class EZJDeserilizationError extends EZJException
{

	public EZJDeserilizationError(JsonValue value, Class type)
	{
		super(value.toString()+" cannot be deserialized as a "+type.getCanonicalName());
	}

}
