package com.nsdl.ura.bonitasoft.controller;

import java.util.List;

import org.bonitasoft.engine.api.APIClient;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IdentityController {

	@Autowired
	private APIClient apiClient;

	@GetMapping({"/users"})
	public List<User> list() throws BonitaException {
		this.apiClient.login("install", "install");
		try {
			return this.apiClient.getIdentityAPI().searchUsers((new SearchOptionsBuilder(0, 50)).done()).getResult();
		} finally {
			this.apiClient.logout();
		} 
	}
}
