package ezj.test;

import java.awt.Color;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import ezj.IEZJCustomSerializer;

public class ColorSerializer implements IEZJCustomSerializer<Color>
{

	@Override
	public JsonValue serialize(Color o)
	{
		JsonObjectBuilder job = Json.createObjectBuilder();
		job.add("r", o.getRed());
		job.add("g", o.getGreen());
		job.add("b", o.getBlue());
		return job.build();		
	}

	@Override
	public Color deserialize(JsonValue value)
	{
		JsonObject obj = (JsonObject) value;
		return new Color(obj.getInt("r"), obj.getInt("g"),obj.getInt("b"));
	}

	
	
}
