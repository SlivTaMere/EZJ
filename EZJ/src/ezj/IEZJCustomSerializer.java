package ezj;

import javax.json.JsonValue;

public interface IEZJCustomSerializer<T>
{
	public JsonValue serialize(T o);
	public T deserialize(JsonValue value); 
}
