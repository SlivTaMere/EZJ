package ezj;

import java.awt.List;
import java.lang.reflect.Array;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import ezj.exception.EZJDeserilizationError;
import ezj.exception.EZJException;
import ezj.exception.EZJInstantiationError;
import ezj.exception.EZJNoFieldReadAccess;
import ezj.exception.EZJNoFieldWriteAccess;
import ezj.exception.EZJNotDeserializable;
import ezj.exception.EZJNotSerializable;

public class EZJ
{
	private static final Map<Class<?>, Class<?>> WRAPPER_TYPE_MAP;
	static {
	    WRAPPER_TYPE_MAP = new HashMap<Class<?>, Class<?>>();
	    WRAPPER_TYPE_MAP.put(Integer.class, Integer.TYPE);
	    WRAPPER_TYPE_MAP.put(Byte.class, Byte.TYPE);
	    WRAPPER_TYPE_MAP.put(Character.class, Character.TYPE);
	    WRAPPER_TYPE_MAP.put(Boolean.class, Boolean.TYPE);
	    WRAPPER_TYPE_MAP.put(Double.class, Double.TYPE);
	    WRAPPER_TYPE_MAP.put(Float.class, Float.TYPE);
	    WRAPPER_TYPE_MAP.put(Long.class, Long.TYPE);
	    WRAPPER_TYPE_MAP.put(Short.class, Short.TYPE);
	    WRAPPER_TYPE_MAP.put(Void.class, Void.TYPE);
	}
	
	static private HashMap<Class, IEZJCustomSerializer> customSerializers = new HashMap<Class, IEZJCustomSerializer>();
	
	/**
	 * Add a custom (de)serializer for the given class
	 * @param serial Implementation of IEZJCustomSerializer that handles (de)serilization for the class c
	 * @param c The class for which to use the custom serializer
	 */
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
		if(customSerializers.containsKey(object.getClass()))
		{
			JsonValue obj = customSerializers.get(object.getClass()).serialize(object);
			if(obj.getValueType() != ValueType.OBJECT)
			{
				throw new EZJException(customSerializers.get(object.getClass()).getClass().getCanonicalName()+" serilize method should return a JsonObject.");
			}
			return (JsonObject) obj;
		}
		
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
						throw new EZJNoFieldReadAccess(object.getClass(), name);
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
	
	private static Collection deserializeToCollection(JsonArray jArray, Field collectionField) throws EZJException
	{
		try
		{
			if(!Collection.class.isAssignableFrom(collectionField.getType()))
			{
				throw new EZJInstantiationError(collectionField.getType());
			}
			ParameterizedType tp = (ParameterizedType) collectionField.getGenericType();			
			Class elementsClass = (Class) tp.getActualTypeArguments()[0];	
			
			//Most of the time the field type will be the interface List or Set and we can't instantiate it.
			//Let's use well known implementation of these two interfaces so the serialized class don't need to specified the exact type of the list.
			//TODO, add a setting to change implementation used and add more interfaces/abstract classes supported.
			
			Collection i;
			if(collectionField.getType().equals(List.class))
			{
				i = new ArrayList<>();
			}
			else if(collectionField.getType().equals(Set.class))
			{
				i = new HashSet<>();
			}
			else
			{
				i = (Collection) collectionField.getType().newInstance();
			}
			
			for(JsonValue children : jArray)
			{
				i.add(fromJsonValue(children, elementsClass));
			}
			
			return i;
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			throw new EZJInstantiationError(collectionField.getType(), e);
		}
	}
	
	private static Object[] deserializeToArray(JsonArray jArray, Field field) throws EZJException
	{
		if(!field.getType().isArray())
		{
			throw new EZJInstantiationError(field.getType());
		}
		
		Class childrenClass = field.getType().getComponentType();
		Object[] array = (Object[]) Array.newInstance(childrenClass, jArray.size());
		
		int i = 0;
		for(JsonValue children : jArray)
		{
			array[i] = fromJsonValue(children, childrenClass);
			i++;
		}
		
		return array;
	}
	
	static public <T> T deserialize(JsonObject jObj, Class<T> c) throws EZJException
	{
		if(jObj.containsKey(c.getSimpleName()))
		{
			return deserializeToObject(jObj.getJsonObject(c.getSimpleName()), c);
		}
		throw new EZJNotDeserializable(c, jObj);
	}
	
	static public <T> void deserializeToCollection(JsonArray jArr, Class<T> collectionElementsType, Collection<T> receiver) throws EZJException
	{
		for(JsonValue jv : jArr)
		{
			if(jv.getValueType() == ValueType.OBJECT && ((JsonObject) jv).containsKey(collectionElementsType.getSimpleName()))
			{
				receiver.add(deserialize((JsonObject) jv, collectionElementsType));
			}
			else
			{
				receiver.add((T) fromJsonValue(jv, collectionElementsType));
			}
			
		}
	}
	
	static public <T> T[] deserializeToArray(JsonArray jArr, Class<T> elementsType) throws EZJException
	{
		T[] array = (T[]) Array.newInstance(elementsType, jArr.size());
		int i = 0;
		for(JsonValue jv : jArr)
		{
			array[i]=(T) fromJsonValue(jv, elementsType);
			i++;
		}
		return array;
	}
	
	static private <T> T deserializeToObject(JsonObject jObj, Class<T> c) throws EZJException
	{
		
		if(customSerializers.containsKey(c))
		{
			return (T) customSerializers.get(c).deserialize(jObj);
		}
		
		T object = null;
		try
		{
			object = c.newInstance();
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			throw new EZJInstantiationError(c, e); 
		}
		//JsonObject values = jObj.getJsonObject(c.getSimpleName());
		
		for(Field field : object.getClass().getDeclaredFields())
		{
				String fieldName = field.getName();
				if(jObj.containsKey(fieldName))
				{
					Object fieldValue = fromJsonValue(jObj.get(fieldName), field);
					if(Modifier.isPublic(field.getModifiers()))//direct access to the field value
					{
						try
						{
							field.set(object, fieldValue);
						}
						catch (IllegalArgumentException | IllegalAccessException e)
						{//should not happen as we tested for public
							e.printStackTrace();
						}
					}
					else//we need to fond and use a setter
					{
						String upperName = fieldName.substring(0, 1).toUpperCase()+fieldName.substring(1, fieldName.length());
					
						String[] possibleMethodNames = {"set"+upperName, fieldName};
						//we may have lost the primitive type to its wrapper counterpart. Retrieving it.
						Class[] possibleTypes;
						if(WRAPPER_TYPE_MAP.containsKey(field.getType()))
						{
							possibleTypes = new Class[] {field.getType(), WRAPPER_TYPE_MAP.get(field.getType())};
						}
						else
						{
							possibleTypes = new Class[] {field.getType()};
						}
						//fieldValue.getClass().
						
						Method method = null;
						for(String nameTested : possibleMethodNames)
						{
							for (Class paramTypeTested : possibleTypes)
							{
								//System.out.println(nameTested + " " + paramTypeTested.getCanonicalName());
								try
								{
									method = object.getClass().getMethod(nameTested, new Class[] {paramTypeTested});
								}
								catch (NoSuchMethodException | SecurityException e)
								{
									continue;
								}
								break;
							}
							if(method != null)
							{
								break;
							}
						}
						if(method == null)
						{
							throw new EZJNoFieldWriteAccess(object.getClass(), fieldName);
						}
						
						try
						{
							method.invoke(object, new Object[] {fieldValue});
						}
						catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
						{
							//Yeah i don't know
							e.printStackTrace();
						}
					}
				}	
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
	
	static public Object fromJsonValue(JsonValue value, Field field) throws EZJException
	{
		return fromJsonValue(value, field, field.getType());
	}
	
	static public Object fromJsonValue(JsonValue value, Class type) throws EZJException
	{
		return fromJsonValue(value, null, type);
	}
	
	static public Object fromJsonValue(JsonValue value, Field field, Class type) throws EZJException
	{
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
		else if(IEZJSerializable.class.isAssignableFrom(type))
		{
			if(value.getValueType() == ValueType.OBJECT)
			{
				return deserializeToObject((JsonObject) value, type);
			}
			throw new EZJDeserilizationError(value, type);
			
		}
		else if(Iterable.class.isAssignableFrom(type))
		{
			if(value.getValueType() == ValueType.ARRAY && field != null)
			{
				return deserializeToCollection((JsonArray) value, field);
			}
			throw new EZJDeserilizationError(value, type);
		}
		else if(type.isArray())
		{
			if(value.getValueType() == ValueType.ARRAY && field != null)
			{
				return deserializeToArray((JsonArray) value, field);
			}
			throw new EZJDeserilizationError(value, type);
		}
		else if(BigInteger.class.isAssignableFrom(type))
		{
			if(value.getValueType() == ValueType.NUMBER)
			{
				return ((JsonNumber) value).bigIntegerValueExact();
			}
			throw new EZJDeserilizationError(value, type);
		}
		else if(BigDecimal.class.isAssignableFrom(type))
		{
			if(value.getValueType() == ValueType.NUMBER)
			{
				return ((JsonNumber) value).bigDecimalValue();
			}
			throw new EZJDeserilizationError(value, type);
		}
		else if(Long.class.isAssignableFrom(type) || type.equals(Long.TYPE))
		{
			if(value.getValueType() == ValueType.NUMBER)
			{
				return ((JsonNumber) value).longValueExact();
			}
			throw new EZJDeserilizationError(value, type);
		}
		else if(Double.class.isAssignableFrom(type) || type.equals(Double.TYPE))
		{
			if(value.getValueType() == ValueType.NUMBER)
			{
				return ((JsonNumber) value).doubleValue();
			}
			throw new EZJDeserilizationError(value, type);
		}
		else if(Float.class.isAssignableFrom(type) || type.equals(Float.TYPE))
		{
			if(value.getValueType() == ValueType.NUMBER)
			{
				return (float) ((JsonNumber) value).doubleValue();
			}
			throw new EZJDeserilizationError(value, type);
		}
		else if(Integer.class.isAssignableFrom(type) || type.equals(Integer.TYPE))
		{
			if(value.getValueType() == ValueType.NUMBER)
			{
				return ((JsonNumber) value).intValueExact();
			}
			throw new EZJDeserilizationError(value, type);
		}
		else if(Short.class.isAssignableFrom(type) || type.equals(Short.TYPE))
		{
			if(value.getValueType() == ValueType.NUMBER)
			{
				return (short) ((JsonNumber) value).intValueExact();
			}
			throw new EZJDeserilizationError(value, type);
		}
		else if(Byte.class.isAssignableFrom(type) || type.equals(Byte.TYPE))
		{
			if(value.getValueType() == ValueType.NUMBER)
			{
				return (byte) ((JsonNumber) value).intValueExact();
			}
			throw new EZJDeserilizationError(value, type);
		}
		else if(Enum.class.isAssignableFrom(type))
		{
			if(value.getValueType() == ValueType.STRING)
			{
				return Enum.valueOf(type, ((JsonString) value).getString());
			}
			throw new EZJDeserilizationError(value, type);
		}
		else if(customSerializers.containsKey(type))
		{
			return customSerializers.get(type).deserialize(value);
		}
		else
		{
			if(value.getValueType() == ValueType.STRING)
			{
				return ((JsonString) value).getString();
			}
			throw new EZJNotDeserializable(type, value);
		}
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
