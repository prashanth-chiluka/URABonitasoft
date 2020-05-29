package com.nsdl.ura.bonitasoft.service.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.bonitasoft.engine.api.APIClient;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.nsdl.ura.bonitasoft.BonitasoftApplication;
import com.nsdl.ura.bonitasoft.process.LoanRequestProcessBuilder;
import com.nsdl.ura.bonitasoft.process.ProcessDeployer;
import com.nsdl.ura.bonitasoft.service.ILoanRequestService;

@Service
public class LoanRequestService implements ILoanRequestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BonitasoftApplication.class);

	public static final String TENANT_ADMIN_NAME = "install";

	public static final String TENANT_ADMIN_PASSWORD = "install";

	private static final APIClient apiClient = new APIClient();

	@Override
	public APIClient getApiClient() {
		return apiClient;
	}

	@Override
	public void bonitaAppLauncher() throws Exception {
		LOGGER.trace("Starting bonitaAppLauncher() from LoanRequestService");
		loginAsTenantAdministrator();
		try {
			User requester = createNewUser("requester", "bpm", "Requester", "LoanRequester");
			User validator = createNewUser("validator", "bpm", "Validator", "LoanValidator");
			switchToOtherUser(requester);
			createAndExecuteProcess(requester, validator);
		} finally {
			apiClient.logout();
		} 
		LOGGER.trace("Exiting bonitaAppLauncher() from LoanRequestService");
	}

	@Override
	public void loginAsTenantAdministrator() throws BonitaException {
		LOGGER.trace("Starting loginAsTenantAdministrator() from LoanRequestService");
		apiClient.logout();
		apiClient.login(TENANT_ADMIN_NAME, TENANT_ADMIN_PASSWORD);
		LOGGER.trace("Exiting loginAsTenantAdministrator() from LoanRequestService");
	}

	@Override
	public void switchToOtherUser(User newUser) throws BonitaException {
		LOGGER.trace("Starting switchToOtherUser() from LoanRequestService with parameters ::" + newUser);
		apiClient.logout();
		apiClient.login(newUser.getUserName(), "bpm");
		LOGGER.trace("Exiting switchToOtherUser() from LoanRequestService with parameters ::" + newUser);
	}

	@Override
	public User createNewUser(String userName, String password, String firstName, String lastName) throws BonitaException {
		LOGGER.trace("Starting createNewUser() from LoanRequestService :: with parameters :: Username: "+ userName +" Password: "+ password +" Firstname: "+ firstName +" lastname: "+ lastName);
		return apiClient.getIdentityAPI().createUser(userName, password, firstName, lastName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void createAndExecuteProcess(User initiator, User validator) throws Exception {
		LOGGER.trace("Starting createAndExecuteProcess() from LoanRequestService with parameters :: Initiator:" + initiator+" Validator: "+ validator);
		DesignProcessDefinition designProcessDefinition = (new LoanRequestProcessBuilder()).buildExampleProcess();
		ProcessDefinition processDefinition = (new ProcessDeployer()).deployAndEnableProcessWithActor(designProcessDefinition, "Requester", initiator, "Validator", validator);
		ProcessInstance processInstance = apiClient.getProcessAPI().startProcessWithInputs(processDefinition.getId(), (Map<String, Serializable>) new HashMap<>().put("amount", Double.valueOf(12000.0D)));
		long processInstanceId = processInstance.getId();
		switchToOtherUser(validator);
		HumanTaskInstance reviewRequestTask = waitForUserTask(validator, processInstanceId, "Review Request");
		apiClient.getProcessAPI().assignAndExecuteUserTask(validator.getId(), reviewRequestTask.getId(), new HashMap<>());
		HumanTaskInstance signContractTask = waitForUserTask(initiator, processInstanceId, "Sign contract");
		apiClient.getProcessAPI().assignAndExecuteUserTask(initiator.getId(), signContractTask.getId(), new HashMap<>());
		waitForProcessToFinish();
		System.out.println("Instance of Process LoanRequest(1.0) with id " + processInstanceId + " has finished executing.");
		LOGGER.trace("Exiting createAndExecuteProcess() from LoanRequestService");
	}

	@Override
	public HumanTaskInstance waitForUserTask(User user, long processInstanceId, String userTaskName) {
		LOGGER.trace("Starting waitForUserTask() from LoanRequestService with parameters :: User: " + user +" ProcessInstanceId: "+processInstanceId+" UserTaskName: "+userTaskName);
		Callable<User> supplier = (Callable<User>) user;
		Awaitility.await("User task should not last so long to be ready :-(").atMost(Duration.TEN_SECONDS).pollInterval(Duration.FIVE_HUNDRED_MILLISECONDS).until((Runnable) supplier);
		return (HumanTaskInstance)apiClient.getProcessAPI().getHumanTaskInstances(processInstanceId, userTaskName, 0, 1).get(0);
	}

	@Override
	public void removeUser(User newUser) throws BonitaException {
		LOGGER.trace("Starting removeUser() from LoanRequestService with parameter :: newUser" + newUser);
		apiClient.getIdentityAPI().deleteUser(newUser.getId());
	}

	@Override
	public void waitForProcessToFinish() {
		LOGGER.trace("Starting waitForProcessToFinish() from LoanRequestService");
		Callable<Boolean> waitForProcessToFinish = null;
		Awaitility.await("Process instance lasts long to complete").atMost(Duration.TEN_SECONDS).pollInterval(Duration.FIVE_HUNDRED_MILLISECONDS).until((Callable)waitForProcessToFinish);
		LOGGER.trace("Exiting waitForProcessToFinish() from LoanRequestService");
	}

}
