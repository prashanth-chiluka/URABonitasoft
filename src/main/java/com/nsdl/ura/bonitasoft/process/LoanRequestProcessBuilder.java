package com.nsdl.ura.bonitasoft.process;

import java.util.Random;

import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.ExpressionBuilder;

public class LoanRequestProcessBuilder {

	public static final String ACTOR_REQUESTER = "Requester";

	public static final String ACTOR_VALIDATOR = "Validator";

	public static final String START_EVENT = "Start Request";

	public static final String REVIEW_REQUEST_TASK = "Review Request";

	public static final String DECISION_GATEWAY = "isAccepted";

	public static final String SIGN_CONTRACT_TASK = "Sign contract";

	public static final String NOTIFY_REJECTION_TASK = "Notify rejection";

	public static final String ACCEPTED_END_EVENT = "Accepted";

	public static final String REJECTED_END_EVENT = "Rejected";

	public static final String CONTRACT_AMOUNT = "amount";
	
	public DesignProcessDefinition buildExampleProcess() throws Exception {
	    ProcessDefinitionBuilder processBuilder = (new ProcessDefinitionBuilder()).createNewInstance("LoanRequest", "1.0");
	    processBuilder.addActor(ACTOR_REQUESTER, true);
	    processBuilder.addActor(ACTOR_VALIDATOR);
	    processBuilder.addUserTask(REVIEW_REQUEST_TASK, ACTOR_VALIDATOR);
	    processBuilder.addUserTask(SIGN_CONTRACT_TASK, ACTOR_REQUESTER);
	    processBuilder.addAutomaticTask(NOTIFY_REJECTION_TASK);
	    processBuilder.addStartEvent(START_EVENT);
	    processBuilder.addEndEvent(ACCEPTED_END_EVENT);
	    processBuilder.addEndEvent(REJECTED_END_EVENT);
	    processBuilder.addGateway(DECISION_GATEWAY, GatewayType.EXCLUSIVE);
	    processBuilder.addTransition(START_EVENT, REVIEW_REQUEST_TASK);
	    processBuilder.addTransition(REVIEW_REQUEST_TASK, DECISION_GATEWAY);
	    processBuilder.addTransition(DECISION_GATEWAY, SIGN_CONTRACT_TASK, (new ExpressionBuilder()).createConstantBooleanExpression(new Random(System.currentTimeMillis()).nextBoolean()));
	    processBuilder.addDefaultTransition(DECISION_GATEWAY, NOTIFY_REJECTION_TASK);
	    processBuilder.addTransition(SIGN_CONTRACT_TASK, ACCEPTED_END_EVENT);
	    processBuilder.addTransition(NOTIFY_REJECTION_TASK, REJECTED_END_EVENT);
	    //processBuilder.addContract().addInput(CONTRACT_AMOUNT, Type.INTEGER, "Amount of the loan requested");
	    return processBuilder.getProcess();
	  }
}
