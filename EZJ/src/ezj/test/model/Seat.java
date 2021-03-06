package ezj.test.model;

import java.awt.Color;

import ezj.IEZJSerializable;

public class Seat implements IEZJSerializable
{
	private String material;
	private Color color;
	public String getMaterial()
	{
		return material;
	}
	public void setMaterial(String material)
	{
		this.material = material;
	}
	public Color getColor()
	{
		return color;
	}
	public void setColor(Color color)
	{
		this.color = color;
	}
	@Override
	public String toString()
	{
		return "Seat in "+color+" "+material;
	}
	
	
	
}
