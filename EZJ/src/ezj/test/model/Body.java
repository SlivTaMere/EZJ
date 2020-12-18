package ezj.test.model;

import ezj.IEZJSerializable;

public class Body implements IEZJSerializable
{
	static public enum Type{
		sedan, coupe, sport, station, hatchback, convertible, suv, minivan, pickup
	}
	
	private Type type;

	public Type getType()
	{
		return type;
	}

	public void setType(Type type)
	{
		this.type = type;
	}
	
	@Override
	public String toString()
	{
		return type.name();
	}
}
