package ezj.test.model;

import ezj.IEZJSerializable;

public class Engine implements IEZJSerializable
{
	private double displacement;
	private String fuel;
	private int numberOfCylinders;
	private boolean turbo;
	
	public double getDisplacement()
	{
		return displacement;
	}
	public void setDisplacement(double displacement)
	{
		this.displacement = displacement;
	}
	public String getFuel()
	{
		return fuel;
	}
	public void setFuel(String fuel)
	{
		this.fuel = fuel;
	}
	public int getNumberOfCylinders()
	{
		return numberOfCylinders;
	}
	public void setNumberOfCylinders(int numberOfCylinders)
	{
		this.numberOfCylinders = numberOfCylinders;
	}
	public boolean hasTurbo()
	{
		return turbo;
	}
	public void setTurbo(boolean turbo)
	{
		this.turbo = turbo;
	}
	
	@Override
	public String toString()
	{
		return fuel+" "+displacement+"L "+numberOfCylinders+" cylinders "+(turbo?" with ":" without ")+" turbo.";
	}
	
	
}
