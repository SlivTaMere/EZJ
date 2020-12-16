package ezj.test.model;

import java.awt.Color;

import ezj.IEZJSerializable;

public class Car implements IEZJSerializable
{
	private Body body;
	public Engine engine;
	private Wheel[] wheels;
	private Color color;
	private Interior interior;
	
	public Body getBody()
	{
		return body;
	}
	public void setBody(Body body)
	{
		this.body = body;
	}
	
	public Wheel[] getWheels()
	{
		return wheels;
	}
	public void setWheels(Wheel[] wheels)
	{
		this.wheels = wheels;
	}
	public Color getColor()
	{
		return color;
	}
	public void setColor(Color color)
	{
		this.color = color;
	}
	public Interior getInterior()
	{
		return interior;
	}
	public void setInterior(Interior interior)
	{
		this.interior = interior;
	}
	
	
	
}
