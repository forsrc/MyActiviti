package com.forsrc.activiti.controller;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Controller
@RequestMapping(value = "/editor")
public class MyActivitiController {

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private HistoryService historyService;
	@Autowired
	private RuntimeService runtimeService;

	@ResponseBody
	@GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Model> list() {
		return repositoryService.createModelQuery().list();
	}

	@RequestMapping("/create/{name}")
	public String create(@PathVariable("name") String name, String description) {
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

		RepositoryService repositoryService = processEngine.getRepositoryService();

		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode editorNode = objectMapper.createObjectNode();
		editorNode.put("id", "canvas");
		editorNode.put("resourceId", "canvas");
		ObjectNode stencilSetNode = objectMapper.createObjectNode();
		stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
		editorNode.put("stencilset", stencilSetNode);
		Model modelData = repositoryService.newModel();

		ObjectNode modelObjectNode = objectMapper.createObjectNode();
		modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, name);
		modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
		modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description == null ? "" : description);
		modelData.setMetaInfo(modelObjectNode.toString());
		modelData.setName(name);
		modelData.setKey(UUID.randomUUID().toString());
		// save
		repositoryService.saveModel(modelData);
		try {
			repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return "redirect:/modeler.html?modelId=" + modelData.getId();

	}

	@GetMapping("/edit/{id}")
	public String edit(@PathVariable("id") String id) {

		return "redirect:/modeler.html?modelId=" + id;
	}

	@ResponseBody
	@GetMapping(path = "/publish/{modelId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, String> publish(@PathVariable("modelId") String modelId) {

		Map<String, String> map = new HashMap<String, String>();
		map.put("code", "FAILURE");
		try {
			Model modelData = repositoryService.getModel(modelId);
			byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());
			if (bytes == null) {
				return map;
			}
			JsonNode modelNode = new ObjectMapper().readTree(bytes);
			BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
			Deployment deployment = repositoryService.createDeployment().name(modelData.getName())
					.addBpmnModel(modelData.getKey() + ".bpmn20.xml", model).deploy();
			modelData.setDeploymentId(deployment.getId());
			repositoryService.saveModel(modelData);
			map.put("code", "SUCCESS");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	@ResponseBody
	@GetMapping(path = "/revokePublish/{modelId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, String> revokePublish(@PathVariable("modelId") String modelId) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("code", "FAILURE");
		Model modelData = repositoryService.getModel(modelId);
		if (null != modelData) {
			try {

				repositoryService.deleteDeployment(modelData.getDeploymentId(), true);
				map.put("code", "SUCCESS");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return map;
	}

	@ResponseBody
	@GetMapping(path = "/delete/{modelId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, String> deleteProcessInstance(@PathVariable("modelId") String modelId) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("code", "FAILURE");
		Model modelData = repositoryService.getModel(modelId);
		if (null != modelData) {
			try {
				ProcessInstance pi = runtimeService.createProcessInstanceQuery()
						.processDefinitionKey(modelData.getKey()).singleResult();
				if (null != pi) {
					runtimeService.deleteProcessInstance(pi.getId(), "");
					historyService.deleteHistoricProcessInstance(pi.getId());
				}
				map.put("code", "SUCCESS");
			} catch (Exception e) {

			}
		}
		return map;
	}
}
