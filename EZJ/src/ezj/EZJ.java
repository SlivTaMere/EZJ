package ezj;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import ezj.exception.EZJException;
import ezj.exception.EZJNoFieldAccess;
import ezj.exception.EZJNotSerializable;

public class EZJ
{
	static private HashMap<Class, IEZJCustomSerializer> customSerializers = new HashMap<Class, IEZJCustomSerializer>();
	
	static public void addCustomSerializer(IEZJCustomSerializer serial, Class c)
	{
		customSerializers.put(c, serial);
	}
	
	static public JsonArray serialize(Object[] objects) throws EZJException
	{
		JsonArrayBuilder jab = Json.createArrayBuilder();
		for(Object o : objects)
		{
			jab.add(toJsonValue(o));
		}
		
		return jab.build();
	}
	
	static public JsonArray serialize(Iterable objects) throws EZJException
	{
		JsonArrayBuilder jab = Json.createArrayBuilder();
		for(Object o : objects)
		{
			jab.add(toJsonValue(o));
		}
		
		return jab.build();
	}
	
	static public JsonObject serializeOuter(IEZJSerializable object) throws EZJException
	{
		JsonObjectBuilder root = Json.createObjectBuilder();
		root.add(object.getClass().getSimpleName(), serializeInner(object));
		return root.build();
	}
	
	static public JsonObject serializeInner(IEZJSerializable object) throws EZJException
	{
		JsonObjectBuilder job = Json.createObjectBuilder();
		for(Field field : object.getClass().getDeclaredFields())
		{
				String name = field.getName();
				Object value = null;
				if(Modifier.isPublic(field.getModifiers()))//direct access to the field value
				{
					try
					{
						value = field.get(object);
					}
					catch (IllegalArgumentException | IllegalAccessException e)
					{//should not happen as we tested for accessibility
						e.printStackTrace();
					}
				}
				else//we need to use a getter
				{
					String upperName = name.substring(0, 1).toUpperCase()+name.substring(1, name.length());
				
					String[] possibleMethodNames = {"get"+upperName, "is"+upperName, "has"+upperName};
					Method method = null;
					for(String nameTested : possibleMethodNames)
					{
						try
						{
							method = object.getClass().getMethod(nameTested, (Class[]) null);
						}
						catch (NoSuchMethodException | SecurityException e)
						{
							continue;
						}
						break;
					}
					if(method == null)
					{
						throw new EZJNoFieldAccess(object.getClass(), name);
					}
					
					try
					{
						value = method.invoke(object, (Object[]) null);
					}
					catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
					{
						//Yeah i don't know
						e.printStackTrace();
					}
				}
				job.add(name, toJsonValue(value));
				
		}
		
		return job.build();
	}
	
	

	static public JsonValue toJsonValue(Object value) throws EZJException
	{
		//Lame way to get a JsonValue
		JsonObjectBuilder job = Json.createObjectBuilder();
		String name = "tmpNameWithNoMeaning";
		
		if(value == null)//we may have called the getter and got a null value
		{
			job.addNull(name);
		}
		else if(value instanceof IEZJSerializable)
		{
			job.add(name, serializeInner((IEZJSerializable) value));
		}
		else if(value instanceof Iterable)
		{
			job.add(name, serialize((Iterable) value));
		}
		else if(value.getClass().isArray())
		{
			job.add(name, serialize((Object[]) value));
		}
		else if(value instanceof BigInteger)
		{
			job.add(name, (BigInteger) value);
		}
		else if(value instanceof BigDecimal)
		{
			job.add(name, (BigDecimal) value);
		}
		else if(value instanceof Long)
		{
			job.add(name, (Long) value);
		}
		else if(isDecimal(value))
		{
			job.add(name, (Double) value);
		}
		else if(isInteger(value))
		{
			job.add(name, (Integer) value);
		}
		else if(value instanceof Boolean)
		{
			job.add(name, (Boolean) value);
		}
		else if(value instanceof Enum)
		{
			job.add(name, ((Enum) value).name());
		}
		else if(customSerializers.containsKey(value.getClass()))
		{
			job.add(name, customSerializers.get(value.getClass()).serialize(value));
		}
		else
		{
			String internal = value.getClass().getName() + "@"+Integer.toHexString(value.hashCode());
			String toString = value.toString();
			
			if(!internal.equals(toString))//check if toString is implemented
			{
				job.add(name, toString);
			}
			else
			{
				throw new EZJNotSerializable(value);
			}
		}
		
		
		return job.build().get(name);
	}
		
	static public boolean isDecimal(Object o)
	{
		return	o instanceof Double || 
				o instanceof Float;
	}
	
	static public boolean isInteger(Object o)
	{
		return o instanceof Integer ||
				o instanceof Byte ||
				o instanceof Short;
	}
}
