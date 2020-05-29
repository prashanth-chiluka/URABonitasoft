package com.nsdl.ura.bonitasoft.controller;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.APIClient;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProcessController {

	@Autowired
	private APIClient apiClient;

	@GetMapping({"/processes"})
	public List<ProcessDeploymentInfo> list() throws BonitaException {
		this.apiClient.login("install", "install");
		List<ProcessDeploymentInfo> result = this.apiClient.getProcessAPI().searchProcessDeploymentInfos((new SearchOptionsBuilder(0, 100)).done()).getResult();
		result.forEach(x -> System.out.println("Process ID: "+ getProcessIdAsString(x)));
		this.apiClient.logout();
		return result;
	}

	public static String getProcessIdAsString(ProcessDeploymentInfo processIdAsString) {
		return String.valueOf(processIdAsString.getProcessId());
	}

	@GetMapping({"/process/{id}/undeploy"})
	public void uninstall(@PathVariable long id) throws BonitaException {
		this.apiClient.login("install", "install");
		this.apiClient.getProcessAPI().disableAndDeleteProcessDefinition(id);
		this.apiClient.logout();
	}

	@SuppressWarnings("unchecked")
	@GetMapping({"/process/{id}/start"})
	public void startProcess(@PathVariable long id) throws BonitaException {
		this.apiClient.login("install", "install");
		this.apiClient.getProcessAPI().startProcessWithInputs(id, (Map<String, Serializable>) new HashMap<String, Serializable>().put("amount", Double.valueOf(12000.0D)));
		this.apiClient.logout();
	}

}
