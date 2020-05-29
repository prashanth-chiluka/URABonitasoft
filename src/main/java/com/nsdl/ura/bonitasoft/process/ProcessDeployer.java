package com.nsdl.ura.bonitasoft.process;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.actorMapping.Actor;
import org.bonitasoft.engine.bpm.bar.actorMapping.ActorMapping;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.User;

import com.nsdl.ura.bonitasoft.BonitasoftApplication;
//import org.bonitasoft.loanrequest.LoanRequestApplicationKt;

public class ProcessDeployer {

	public ProcessDefinition deployAndEnableProcessWithActor(DesignProcessDefinition designProcessDefinition, String requesterActor, User requesterUser, String validatorActor, User validatorUser) throws BonitaException {

		Actor actor1 = new Actor(requesterActor);
		actor1.addUser(requesterUser.getUserName());
		Actor actor2 = new Actor(validatorActor);
		actor2.addUser(validatorUser.getUserName());

		ActorMapping actorMapping = new ActorMapping();
		actorMapping.addActor(actor1);
		actorMapping.addActor(actor2);

		BusinessArchive businessArchive = (new BusinessArchiveBuilder()).createNewBusinessArchive()
				.setProcessDefinition(designProcessDefinition)
				.setActorMapping(actorMapping)
				.done();

		ProcessAPI processAPI = BonitasoftApplication.getApiClient().getProcessAPI();
		ProcessDefinition processDefinition = processAPI.deploy(businessArchive);
		processAPI.enableProcess(processDefinition.getId());
		return processDefinition;

	}
}
