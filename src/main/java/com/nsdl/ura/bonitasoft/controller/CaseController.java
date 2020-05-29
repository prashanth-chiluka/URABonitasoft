package com.nsdl.ura.bonitasoft.controller;

import java.util.List;

import org.bonitasoft.engine.api.APIClient;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CaseController {
	
	@Autowired
	private APIClient apiClient;

	@GetMapping({"/cases"})
	public List<ProcessInstance> list() throws BonitaException {
		this.apiClient.login("install", "install");
		try {
			return this.apiClient.getProcessAPI().searchOpenProcessInstances((new SearchOptionsBuilder(0, 100)).done()).getResult();
		} finally {
			this.apiClient.logout();
		} 
	}

	@GetMapping({"/completedcases"})
	public List<ArchivedProcessInstance> listCompleted() throws BonitaException {
		this.apiClient.login("install", "install");
		try {
			return this.apiClient.getProcessAPI().searchArchivedProcessInstances((new SearchOptionsBuilder(0, 100)).done()).getResult();
		} finally {
			this.apiClient.logout();
		} 
	}

}
