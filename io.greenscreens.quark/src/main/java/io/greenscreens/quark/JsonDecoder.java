/*
 * Copyright (C) 2015, 2016  Green Screens Ltd.
 */
package io.greenscreens.quark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Generic JSON decoder used internally
 */
final public class JsonDecoder<T> {

	private T object;

	transient private MappingIterator<T> objectList;

	// protected Class<T> clazz;

	private static final ObjectMapper OBJECT_MAPPER;

	static {
		OBJECT_MAPPER = new ObjectMapper();
		//OBJECT_MAPPER.registerModule(new AfterburnerModule());

		OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).
					disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES).
					disable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT).
					disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

		//added for ETL controller support
		OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		OBJECT_MAPPER.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);

		/*
		OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		// StdDateFormat is ISO8601 since jackson 2.9
		OBJECT_MAPPER.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
		*/

		/*
		* DeserializationConfig dcfg = om.getDeserializationConfig();
		* dcfg.without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).
		* without(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES).
		* without(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
		*
		* SerializationConfig scfg = om.getSerializationConfig();
		* scfg.without(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		* om.setSerializationConfig(scfg); om.setDeserializationConfig(dcfg);
		*/
	}

	/**
	* New Decoder instance for JSON data
 	* @param type - class to which to convert
	* @param json - json data to convert to Java class instance
	* @throws IOException
	*/
	public JsonDecoder(final Class<T> type, final String json) throws IOException {
		super();
		parse(type, json);
	}

	/**
 	* Does actual conversion from JSON string to Java class instance
 	* @param type
	* @param json
	* @throws IOException
 	*/
	private void parse(final Class<T> type, final String json) throws IOException {

		final JsonFactory factory = new JsonFactory();
		final JsonParser jp = factory.createParser(json);

		try {
			final JsonNode jn = OBJECT_MAPPER.readTree(jp);

			if (jn.isArray()) {
				final TypeFactory tf = TypeFactory.defaultInstance();
				final JavaType jt = tf.constructCollectionType(ArrayList.class, type);
				objectList = OBJECT_MAPPER.readValues(jp, jt);
			} else {
				object = OBJECT_MAPPER.treeToValue(jn, type);
			}
		} finally {
			jp.close();
		}

	}

	public static <T> T parse(final Class<T> type, final JsonNode node) {

		if (node == null) return null;

		if (node.isArray()) {
			final TypeFactory tf = TypeFactory.defaultInstance();
			final JavaType jt = tf.constructCollectionType(ArrayList.class, type);
			return OBJECT_MAPPER.convertValue(node, jt);
		} else {
			return OBJECT_MAPPER.convertValue(node, type);
		}
	}

	static public <T> T convert(final Class<T> type, final JsonNode object) throws JsonProcessingException {
		if (object == null) return null;
		return OBJECT_MAPPER.treeToValue(object, type);
	}

	static public <T>  T convert(final JavaType type, final JsonNode object) throws IOException {
		if (object == null) return null;
		return OBJECT_MAPPER.readerFor(type).readValue(object);
	}

	/**
	* Checks is converted JSON array or single object
	* @return true if it is not array
	*/
	public final boolean isSingle() {
		return object != null;
	}

	/**
	* Returns JSON data converted Java class instance.
	* If JSON data is array, this method will return null
	* @return class instance from defined class in constructor
	*/
	public final T getObject() {
		return object;
	}

	/**
	* Returns JSON data converted Java class instance.
	* If JSON data is object, this method will return null	
	* @return class instance from defined class in constructor
	*/
	public final List<T> getObjectList() throws IOException {

		List<T> list = null;
		if (objectList != null) {
			list = objectList.readAll();
		} else if (object != null) {
			list = Arrays.asList(object);
		}

		return list;

	}

	/**
	* Retrieves internal JSON parser engine
	* @return
	*/
	 public static ObjectMapper getJSONEngine() {
		return OBJECT_MAPPER;
	}

	/**
	* Parse json string to Json Object
	 * @param data
	 * @return
	 * @throws Exception
	*/
	public static JsonNode parse(final String data) throws Exception {
		if (data == null) return null;
		return OBJECT_MAPPER.readTree(data);
	}

	@SuppressWarnings("unchecked")
	public static <K extends JsonNode> K parseType(final String data) throws Exception {
		if (data == null) return null;
		return (K) OBJECT_MAPPER.readTree(data);
	}

	/**
	 * Convert object to json string
	 * @param object
	 * @return
	 * @throws Exception
	 */
	public static String stringify(final Object object) throws Exception {
		if (object == null) return null;
		return OBJECT_MAPPER.writeValueAsString(object);
	}

	/**
	 * Get property as Integer
	 * @param node
	 * @param key
	 * @return
	 */
	public static int getInt(final JsonNode node, final String key)  {

		int val = 0;

		if (node != null && node.has(key)) {
			val = node.get(key).asInt(0);
		}

		return val;
	}

	/**
	 * Get property as String
	 * @param node
	 * @param key
	 * @return
	 */
	public static String getStr(final JsonNode node, final String key)  {

		String val = null;

		if (node != null && node.has(key)) {
			val = node.get(key).asText();
		} else {
			val = "";
		}

		return val;
	}

	/**
	 * Compare to object properties as int
	 * @param node
	 * @param filter
	 * @param name
	 * @return
	 */
	public static boolean compareInt(final JsonNode node, final ObjectNode filter, final String name) {

		boolean sts = true;

		if (node == null && filter == null) {
			return sts;
		}

		if (filter.has(name)) {
			int intVal = filter.get(name).asInt(0);
			if (intVal > 0) {
				sts = node.get(name).asInt(0) == intVal;
			}
		}

		return sts;
	}

	/**
	 * Compare to object properties as String
	 * @param node
	 * @param filter
	 * @param name
	 * @return
	 */
	public static boolean compareStr(final JsonNode node, final ObjectNode filter, final String name) {

		boolean sts = true;

		if (node == null && filter == null) {
			return sts;
		}

		if (filter.has(name) && node.has(name)) {

			final String fltVal = filter.get(name).asText("").trim();
			final String strVal = node.get(name).asText("").trim();

			if (fltVal.length() > 0) {
				sts = strVal.equals(fltVal);
			}

		}

		return sts;
	}

	/**
	 * Copy properties from node to node
	 * @param from
	 * @param to
	 */
	public static void copy(final ObjectNode from, final ObjectNode to) {
		if (to != null && from != null) {
			to.setAll(from);
		}
	}

	final static void pro() {
		final List<String> list = Collections.emptyList(); list.stream().filter(s -> s.equals(s)).findFirst().orElse(null);
	}
}
