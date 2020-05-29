package com.nsdl.ura.bonitasoft;

import java.util.HashMap;
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
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.nsdl.ura.bonitasoft.process.LoanRequestProcessBuilder;
import com.nsdl.ura.bonitasoft.process.ProcessDeployer;

@SpringBootApplication
//@PropertySources(value = {
//		@PropertySource(value = "classpath:ura-bonitasoft-application.properties", ignoreResourceNotFound = false),
//		@PropertySource(value = "file:${external.config}", ignoreResourceNotFound = false)
//})
public class BonitasoftApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(BonitasoftApplication.class);

	public static final String TENANT_ADMIN_NAME = "install";

	public static final String TENANT_ADMIN_PASSWORD = "install";

	private static final APIClient apiClient = new APIClient();

	public static final APIClient getApiClient() {
		return apiClient;
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(BonitasoftApplication.class, args);
		loginAsTenantAdministrator();
		try {
			User requester = createNewUser("requester", "bpm", "Requester", "LoanRequester");
			User validator = createNewUser("validator", "bpm", "Validator", "LoanValidator");
			switchToOtherUser(requester);
			createAndExecuteProcess(requester, validator);
		} finally {
			apiClient.logout();
		} 
	}

	public static final void loginAsTenantAdministrator() throws BonitaException {
		LOGGER.trace("Starting loginAsTenantAdministrator() from LoanRequestService");
		apiClient.logout();
		apiClient.login(TENANT_ADMIN_NAME, TENANT_ADMIN_PASSWORD);
		LOGGER.trace("Exiting loginAsTenantAdministrator() from LoanRequestService");
	}

	public static final void switchToOtherUser(User newUser) throws BonitaException {
		LOGGER.trace("Starting switchToOtherUser() from LoanRequestService with parameters ::" + newUser);
		apiClient.logout();
		apiClient.login(newUser.getUserName(), "bpm");
		LOGGER.trace("Exiting switchToOtherUser() from LoanRequestService with parameters ::" + newUser);
	}


	public static final User createNewUser(String userName, String password, String firstName, String lastName) throws BonitaException {
		LOGGER.trace("Starting createNewUser() from LoanRequestService :: with parameters :: Username: "+ userName +" Password: "+ password +" Firstname: "+ firstName +" lastname: "+ lastName);
		return apiClient.getIdentityAPI().createUser(userName, password, firstName, lastName);
	}

	public static final void createAndExecuteProcess(User initiator, User validator) throws Exception {
		LOGGER.trace("Starting createAndExecuteProcess() from LoanRequestService with parameters :: Initiator:" + initiator+" Validator: "+ validator);
		DesignProcessDefinition designProcessDefinition = (new LoanRequestProcessBuilder()).buildExampleProcess();
		ProcessDefinition processDefinition = (new ProcessDeployer()).deployAndEnableProcessWithActor(designProcessDefinition, "Requester", initiator, "Validator", validator);
		//ProcessInstance processInstance = apiClient.getProcessAPI().startProcessWithInputs(processDefinition.getId(), (Map<String, Serializable>) new HashMap<>().put("amount", Double.valueOf(12000.00)));
		ProcessInstance processInstance = apiClient.getProcessAPI().startProcess(processDefinition.getId());
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

	public static final HumanTaskInstance waitForUserTask(User user, long processInstanceId, String userTaskName) {
		LOGGER.trace("Starting waitForUserTask() from LoanRequestService with parameters :: User: " + user +" ProcessInstanceId: "+processInstanceId+" UserTaskName: "+userTaskName);
		Awaitility.await("User task should not last so long to be ready :-(").atMost(Duration.TEN_SECONDS).pollInterval(Duration.FIVE_HUNDRED_MILLISECONDS).until(() -> apiClient.getProcessAPI().getNumberOfPendingHumanTaskInstances(user.getId()));
		return (HumanTaskInstance)apiClient.getProcessAPI().getHumanTaskInstances(processInstanceId, userTaskName, 0, 1).get(0);
	}

	public static final void removeUser(User newUser) throws BonitaException {
		LOGGER.trace("Starting removeUser() from LoanRequestService with parameter :: newUser" + newUser);
		apiClient.getIdentityAPI().deleteUser(newUser.getId());
	}
	public static final void waitForProcessToFinish() {
		LOGGER.trace("Starting waitForProcessToFinish() from LoanRequestService");
		Callable<Boolean> waitForProcessToFinish = null;
		Awaitility.await("Process instance lasts long to complete").atMost(Duration.TEN_SECONDS).pollInterval(Duration.FIVE_HUNDRED_MILLISECONDS).until((Runnable)waitForProcessToFinish);
		LOGGER.trace("Exiting waitForProcessToFinish() from LoanRequestService");
	}
}