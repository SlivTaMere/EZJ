package ezj.test.model;

import java.util.List;

import ezj.IEZJSerializable;

public class Interior implements IEZJSerializable
{
	private String nonSeatMaterial;
	private List<Seat> seats;
	
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
	public void setSeats(List<Seat> seats)
	{
		this.seats = seats;
	}
	
}
