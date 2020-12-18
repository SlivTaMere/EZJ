package ezj.exception;

import javax.json.JsonValue;

import ezj.IEZJSerializable;

public class EZJNotDeserializable extends EZJException
{

	public EZJNotDeserializable(Class type, JsonValue jsonValue)
	{
		super("Could not deserialize "+jsonValue+" to "+type.getCanonicalName());
	}

}
