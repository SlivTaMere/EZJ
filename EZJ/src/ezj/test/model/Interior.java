package ezj.test.model;

import java.util.ArrayList;
import java.util.List;

import ezj.IEZJSerializable;

public class Interior implements IEZJSerializable
{
	private String nonSeatMaterial;
	private ArrayList<Seat> seats;
	
	public String getNonSeatMaterial()
	{
		return nonSeatMaterial;
	}
	public void setNonSeatMaterial(String nonSeatMaterial)
	{
		this.nonSeatMaterial = nonSeatMaterial;
	}
	public List<Seat> getSeats()
	{
		return seats;
	}
	public void setSeats(ArrayList<Seat> seats)
	{
		this.seats = seats;
	}
	
	@Override
	public String toString()
	{
		String str = nonSeatMaterial;
		for(Seat s : seats)
		{
			str += "\n\t"+s; 
		}
		return str;
	}
	
	
}
