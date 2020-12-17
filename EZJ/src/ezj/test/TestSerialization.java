package ezj.test;

import java.awt.Color;
import java.util.ArrayList;

import javax.json.JsonArray;
import javax.json.JsonObject;

import ezj.EZJ;
import ezj.test.model.Body;
import ezj.test.model.Car;
import ezj.test.model.Engine;
import ezj.test.model.Interior;
import ezj.test.model.Seat;
import ezj.test.model.Wheel;

public class TestSerialization
{
	public static Car createTestObject()
	{
		Car c = new Car();
		Body b = new Body();
		b.setType(Body.Type.sport);
		c.setBody(b);
		
		Engine e = new Engine();
		e.setDisplacement(4.5);
		e.setFuel("gas");
		e.setNumberOfCylinders(6);
		e.setTurbo(true);
		c.engine = e;
		
		Interior i = new Interior();
		Color black = Color.black;
		Seat driver = new Seat();		
		driver.setColor(black);
		driver.setMaterial("leather");
		
		Seat passenger = new Seat();
		passenger.setColor(black);
		passenger.setMaterial("leather");
		
		Seat backSeat = new Seat();
		backSeat.setColor(black);
		backSeat.setMaterial("leather");
		ArrayList<Seat> seats = new ArrayList<Seat>();
		seats.add(driver);
		seats.add(passenger);
		seats.add(backSeat);
		i.setSeats(seats);
		i.setNonSeatMaterial("wooden");
		c.setInterior(i);
		
		Wheel fl = new Wheel();
		fl.setColor(Color.red);
		fl.setMaterial("aluminium");
		Wheel fr = new Wheel();
		fr.setColor(Color.blue);
		fr.setMaterial("aluminium");
		Wheel rl = new Wheel();
		rl.setColor(Color.cyan);
		rl.setMaterial("aluminium");
		Wheel rr = new Wheel();
		rr.setColor(Color.pink);
		rr.setMaterial("aluminium");
		
		Wheel[] wheels = {fl, fr, rl, rr};
		c.setWheels(wheels);
		
		return c;
		
		
	}

	public static void main(String[] args) throws Exception
	{
		Car c = TestSerialization.createTestObject();
		EZJ.addCustomSerializer(new ColorSerializer(), Color.class);
		JsonObject jsonrepr = EZJ.serialize(c);
		JsonArray jsona = EZJ.serialize(c.getWheels());
		System.out.println(jsonrepr);
	}
	
}
