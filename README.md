# EZJ
## Easy Java<->Json. Depends on Java API for JSON Processing 1.0 (JSR 353) specification reference implementation (javax.json).

## Serialize and Unserialize simple Java objects to Json.
### Implement IEZJSerializable (no method) to indicate to EZJ you want to serialize your class
### Call EZJ.Serialize
### Implement IEZJCustomSerializer to support class you can't change. Then add it with EZJ.addCustomSerializer
