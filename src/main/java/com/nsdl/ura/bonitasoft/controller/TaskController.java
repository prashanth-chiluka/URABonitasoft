package com.nsdl.ura.bonitasoft.controller;

import java.util.HashMap;
import java.util.List;

import org.bonitasoft.engine.api.APIClient;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class TaskController {

	@Autowired
	private APIClient apiClient;

	@GetMapping({"/tasks"})
	public List<HumanTaskInstance> list() throws BonitaException {
		this.apiClient.login("install", "install");
		List<HumanTaskInstance> result = this.apiClient.getProcessAPI().searchMyAvailableHumanTasks(this.apiClient.getSession().getUserId(), (new SearchOptionsBuilder(0, 100)).done()).getResult();
		this.apiClient.logout();
		return result;
	}

	@GetMapping({"/task/{taskId}/execute"})
	public void executeFirstHumanTask(@PathVariable long taskId) throws BonitaException {
		this.apiClient.login("install", "install");
		User user = this.apiClient.getIdentityAPI().getUserByUserName("scott");
		this.apiClient.logout();
		this.apiClient.login("scott", "bpm");
		this.apiClient.getProcessAPI().assignAndExecuteUserTask(user.getId(), taskId, new HashMap<>());
		this.apiClient.logout();
	}
}
