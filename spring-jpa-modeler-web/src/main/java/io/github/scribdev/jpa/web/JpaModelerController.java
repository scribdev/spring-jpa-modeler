package io.github.scribdev.jpa.web;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.scribdev.jpa.model.JpaModelMappingContext;

@Controller
public class JpaModelerController {

	public static final String DEFAULT_URL = "/jpa-modeler";

	public static final String SQL_QUERY_URL = "/jpa-modeler/query";

	@Autowired
	private JpaModelMappingContext jpaModelMappingContext;

	@RequestMapping(value = DEFAULT_URL, method = RequestMethod.GET, produces = { APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getJpaModel(HttpServletRequest servletRequest) {

		JsonNode model = jpaModelMappingContext.getModel();

		return new ResponseEntity<>(model.toPrettyString(), HttpStatus.OK);
	}

	@RequestMapping(value = SQL_QUERY_URL, method = RequestMethod.GET, produces = { APPLICATION_JSON_VALUE })
	public ResponseEntity<JsonNode> getSelectAllForEntityName(@RequestParam(name = "className") String className) {
		return new ResponseEntity<JsonNode>(jpaModelMappingContext.toSelectAllSqlQuery(className), HttpStatus.OK);
	}
}
