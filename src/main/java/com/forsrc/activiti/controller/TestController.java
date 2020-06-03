package com.forsrc.activiti.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/test")
public class TestController {

	@RequestMapping(value = "")
	public ResponseEntity<String> test(String name) {
		return new ResponseEntity<>("hello world. " + (name == null ? "" : name), HttpStatus.OK);
	}

}
