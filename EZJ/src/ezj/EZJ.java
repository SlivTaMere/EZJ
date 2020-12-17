package ezj;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import ezj.exception.EZJDeserilizationError;
import ezj.exception.EZJException;
import ezj.exception.EZJInstantiationError;
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
			JsonObjectBuilder job = Json.createObjectBuilder();
			job.add(o.getClass().getSimpleName(), serializeInner(o));
			jab.add(job);
		}
		
		return jab.build();
	}
	
	static public JsonArray serialize(Collection objects) throws EZJException
	{
		JsonArrayBuilder jab = Json.createArrayBuilder();
		for(Object o : objects)
		{
			JsonObjectBuilder job = Json.createObjectBuilder();
			job.add(o.getClass().getSimpleName(), serializeInner(o));
			jab.add(job);
		}
		
		return jab.build();
	}
	
	static private JsonArray serializeInner(Object[] objects) throws EZJException
	{
		JsonArrayBuilder jab = Json.createArrayBuilder();
		for(Object o : objects)
		{
			jab.add(toJsonValue(o));
		}
		
		return jab.build();
	}
	
	static private JsonArray serializeInner(Collection objects) throws EZJException
	{
		JsonArrayBuilder jab = Json.createArrayBuilder();
		for(Object o : objects)
		{
			jab.add(toJsonValue(o));
		}
		
		return jab.build();
	}
	
	static public JsonObject serialize(IEZJSerializable object) throws EZJException
	{
		JsonObjectBuilder root = Json.createObjectBuilder();
		root.add(object.getClass().getSimpleName(), serializeInner(object));
		return root.build();
	}
	
	static private JsonObject serializeInner(Object object) throws EZJException
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
						//continue;
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
	
	private static Collection deserializeCollection(JsonArray jArray, Field collectionField) throws EZJException
	{
		try
		{
			if(!Collection.class.isAssignableFrom(collectionField.getType()))
			{
				throw new EZJInstantiationError(collectionField.getType());
			}
			ParameterizedType tp = (ParameterizedType) collectionField.getGenericType();			
			Class elementsClass = (Class) tp.getActualTypeArguments()[0];			
			Collection i = (Collection) collectionField.getType().newInstance();			
			for(JsonValue children : jArray)
			{
				i.add(fromJsonValue(children, null));
			}
			
			return i;
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			throw new EZJInstantiationError(collectionField.getType(), e);
		}
	}
	
	static public <T> T deserialize(JsonObject jObj, Class<T> c) throws EZJInstantiationError
	{
		T object = null;
		try
		{
			object = c.newInstance();
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			throw new EZJInstantiationError(c, e); 
		}
		JsonObject values = jObj.getJsonObject(c.getSimpleName());
		for(Field field : object.getClass().getDeclaredFields())
		{
				String name = field.getName();
				if(values.containsKey(name))
				{
					
				}
				
				if(Modifier.isPublic(field.getModifiers()))//direct access to the field value
				{
					try
					{
						field.set(object);
					}
					catch (IllegalArgumentException | IllegalAccessException e)
					{//should not happen as we tested for public
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
		
		
		
		return object;
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
		else if(value instanceof Collection)
		{
			job.add(name, serializeInner((Collection) value));
		}
		else if(value.getClass().isArray())
		{
			job.add(name, serializeInner((Object[]) value));
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
	
	static public Object fromJsonValue(JsonValue value, Field field, Class type) throws EZJException
	{
		if(field == null)
		{
			
		}
		if(value.equals(JsonValue.NULL))
		{
			return null;
		}
		else if(value.equals(JsonValue.FALSE))
		{
			return false;
		}
		else if(value.equals(JsonValue.TRUE))
		{
			return true;
		}
		else if(IEZJSerializable.class.isAssignableFrom(field.getType()))
		{
			if(value.getValueType() == ValueType.OBJECT)
			{
				return deserialize((JsonObject) value, field.getType());
			}
			throw new EZJDeserilizationError(value, field.getType());
			
		}
		else if(Iterable.class.isAssignableFrom(field.getType()))
		{
			if(value.getValueType() == ValueType.ARRAY)
			{
				return deserializeIterable((JsonArray) value, field.getType());
			}
			throw new EZJDeserilizationError(value, field.getType());
		}
		else if(value.getClass().isArray())
		{
			job.add(name, serializeInner((Object[]) value));
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
	
	private final class __<T> // generic helper class which does only provide type information
	{
	    private __()
	    {
	    }
	}
}
