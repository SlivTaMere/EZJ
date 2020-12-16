package ezj;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

import ezj.exception.EZJException;
import ezj.exception.EZJNotSerializable;
import ezj.test.TestSerialization;
import ezj.test.model.Car;

public class Main
{

	public static void main(String[] args) throws Exception
	{
		Car c = TestSerialization.createTestObject();
		
		/*System.out.println(c.getWheels().getClass().isArray());
		System.out.println(Arrays.toString(c.getWheels().getClass().getInterfaces()));
		
		System.out.println(EZJ.serialize(c.getWheels()));
		System.out.println(EZJ.serialize(c.getInterior().getSeats()));*/
		
		
		/*c.setBody(null);
		
		Object nullObject = c.getClass().getMethod("getBody", null).invoke(c, null);
		System.out.println(nullObject == null);*/

		/*int i = 0;
		Object o = i;
		System.out.println(o instanceof Integer);*/
		
		/*Color[] cr = {Color.BLACK, Color.RED};
		System.out.println(cr);
		Object o = cr;
		String res = cr.getClass().getName() + "@"+Integer.toHexString(o.hashCode());
		System.out.println(res.equals(cr.toString()));
		//System.out.println();
		System.out.println(Object.class.getMethod("toString", null).invoke(o, null));*/
		System.out.println(c.getBody().getType() instanceof Enum);
	}

}
