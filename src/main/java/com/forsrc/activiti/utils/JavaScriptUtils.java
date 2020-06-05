package com.forsrc.activiti.utils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class JavaScriptUtils {

	public static final ScriptEngineManager MANAGER = new ScriptEngineManager();
	public static final ScriptEngine SCRIPT_ENGINE_JAVASCRIPT = MANAGER.getEngineByName("nashorn");

	public static Handler handler = new Handler() {
		TextNode textNode = null;

		@Override
		public JsonNode handle(String fieldName, JsonNode jsonNode) {
			String text = jsonNode.textValue();
			if (text == null) {
				return jsonNode;
			}
			text = text.trim();
			if (text == null || !text.startsWith("```") || !text.endsWith("```")) {
				return jsonNode;
			}
			text = text.substring(3, text.length() - 3);
			// text = text.replaceAll("```", "");
			// System.out.println(text);
			try {
				Object rt = SCRIPT_ENGINE_JAVASCRIPT.eval(text);
				// System.out.println(rt);
				if (rt != null) {
					textNode = new TextNode(rt.toString());
					return textNode;
				}
			} catch (ScriptException e) {
				e.printStackTrace();
				return new TextNode(String.format("``` %s\n/** [ERROR] [%s] %s\n```", text,
						e.getClass().getSimpleName(), e.getMessage()));
			}
			return jsonNode;

		}
	};

	public static void iterator(JsonNode root, Handler handler) {
		root.fieldNames().forEachRemaining((fieldName) -> {
			JsonNode jsonNode = root.get(fieldName);
			if (jsonNode == null) {
				return;
			}

			if (jsonNode.isArray()) {
				ArrayNode arrayNode = (ArrayNode) jsonNode;
				arrayNode.forEach((arr) -> {
					iterator(arr, handler);
				});
				return;
			}

			if (jsonNode.isObject()) {
				iterator(jsonNode, handler);
				return;
			}
			JsonNode handleJsonNode = handler.handle(fieldName, jsonNode);
			if (jsonNode != null && handleJsonNode != null && !jsonNode.equals(handleJsonNode)) {
				((ObjectNode) root).put(fieldName, handleJsonNode);
			}
		});
	}

	public static interface Handler {
		public JsonNode handle(String fieldName, JsonNode jsonNode);
	}

	public static void main(String[] args) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		// @formatter:off
		String jsonString = "{" + "\"name\": \" ``` function name() {return 'forsrc';/** */} name(); ```\","
				+ "\"age\": \"``` 32y; ```\","
				+ "\"array\" : [1, 2, 3],"
				+ "\"sub\" : {\"date\": \"``` new Date().toISOString(); ```\"}" 
				+ "}";
		// @formatter:on

		JsonNode root = objectMapper.readTree(jsonString);
		System.out.println(root);
		root.fieldNames().forEachRemaining((key) -> System.out.println(root.get(key)));
		System.out.println(root);
		iterator(root, handler);
		System.out.println(root);
	}

}
