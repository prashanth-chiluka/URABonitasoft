package com.nsdl.ura.bonitasoft.service;

import org.bonitasoft.engine.api.APIClient;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.User;

public interface ILoanRequestService {
	
	public APIClient getApiClient();
	public void bonitaAppLauncher() throws Exception;
	public void loginAsTenantAdministrator() throws BonitaException;
	public void switchToOtherUser(User newUser) throws BonitaException;
	public User createNewUser(String userName, String password, String firstName, String lastName) throws BonitaException;
	public void createAndExecuteProcess(User initiator, User validator) throws Exception;
	public HumanTaskInstance waitForUserTask(User user, long processInstanceId, String userTaskName);
	public void removeUser(User newUser) throws BonitaException;
	public void waitForProcessToFinish();
	
}
