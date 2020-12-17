package ezj;

import java.awt.Color;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.json.JsonArray;

import com.googlecode.genericdao.dao.DAOUtil;

import ezj.exception.EZJException;
import ezj.exception.EZJNotSerializable;
import ezj.test.TestSerialization;
import ezj.test.model.Car;
import ezj.test.model.Seat;

@SuppressWarnings("unchecked")
public class Main
{

	@SuppressWarnings({ "rawtypes", "unused" })
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
		System.out.println(o instanceof Integer);
		System.out.println(Enum.class.isAssignableFrom(c.getBody().getType().getClass()));*/
		
		/*Color[] cr = {Color.BLACK, Color.RED};
		System.out.println(cr);
		Object o = cr;
		String res = cr.getClass().getName() + "@"+Integer.toHexString(o.hashCode());
		System.out.println(res.equals(cr.toString()));
		//System.out.println();
		System.out.println(Object.class.getMethod("toString", null).invoke(o, null));*/
		//System.out.println(c.getBody().getType() instanceof Enum);
		ParameterizedType tp = (ParameterizedType) c.getInterior().getClass().getDeclaredField("seats").getGenericType();
		System.out.println((Class) tp.getActualTypeArguments()[0]);
		System.out.println(Collection.class.isAssignableFrom(c.getInterior().getClass().getDeclaredField("seats").getType()));
	}
	
	static public <T> Collection<T> deserializeCollection(Class<Collection<T>> fieldType) throws InstantiationException, IllegalAccessException
	{
		Collection<T> inst = fieldType.newInstance();
		ParameterizedType tp = (ParameterizedType) inst.getClass().getGenericSuperclass();
		Type[] cls = tp.getActualTypeArguments();
		System.out.println(cls[0].getClass());
		System.out.println(cls[0].getTypeName());
		
	
		return null;
	}
	
	static private final class __<T> // generic helper class which does only provide type information
	{
	    private __()
	    {
	    }
	}

}
