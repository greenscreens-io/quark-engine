/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */
package io.greenscreens;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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

		OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
				.disable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
				.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

		// added for ETL controller support
		OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		OBJECT_MAPPER.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);

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
	 * 
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
	 * 
	 * @param type
	 * @param json
	 * @throws IOException
	 */
	private void parse(final Class<T> type, final String json) throws IOException {

		final JsonFactory factory = new JsonFactory();
		final JsonParser jp = factory.createParser(json);
		final JsonNode jn = OBJECT_MAPPER.readTree(jp);

		if (jn.isArray()) {
			final TypeFactory tf = TypeFactory.defaultInstance();
			final JavaType jt = tf.constructCollectionType(ArrayList.class, type);
			objectList = OBJECT_MAPPER.readValues(jp, jt);
		} else {
			object = OBJECT_MAPPER.treeToValue(jn, type);
		}
	}

	/**
	 * Checks is converted JSON array or single object
	 * 
	 * @return true if it is not array
	 */
	public final boolean isSingle() {
		return object != null;
	}

	/**
	 * Returns JSON data converted Java class instance. If JSON data is array, this
	 * method will return null
	 * 
	 * @return class instance from defined class in constructor
	 */
	public final T getObject() {
		return object;
	}

	/**
	 * Returns JSON data converted Java class instance. If JSON data is object, this
	 * method will return null
	 * 
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
	 * 
	 * @return
	 */
	public static ObjectMapper getJSONEngine() {
		return OBJECT_MAPPER;
	}

	/**
	 * Parse json string to Json Object
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static JsonNode parse(final String data) throws Exception {
		return OBJECT_MAPPER.readTree(data);
	}

	/**
	 * Convert object to json string
	 * 
	 * @param object
	 * @return
	 * @throws Exception
	 */
	public static String stringify(final Object object) throws Exception {
		return OBJECT_MAPPER.writeValueAsString(object);
	}

}
