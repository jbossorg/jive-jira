/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.issues.rpc.soap.jirasoapservice_v2;

import javax.xml.rpc.ServiceException;

import com.atlassian.jira.rpc.soap.beans.RemoteIssue;
import com.atlassian.jira.rpc.soap.beans.RemoteUser;

/**
 * Simple test class for debugging remote jira soap api
 *
 * @author Libor Krzyzanek
 */
public class JiraSoapServiceTest {

	private static JiraSoapService jiraSoapService = null;

	private static JiraSoapService getJiraSoapService() throws ServiceException {
		if (jiraSoapService == null) {
			String portAddress = "https://issues.stage.jboss.org/rpc/soap/jirasoapservice-v2";

			JiraSoapServiceServiceLocator jiraSoapServiceServiceLocator = new JiraSoapServiceServiceLocator();
			jiraSoapServiceServiceLocator.setJirasoapserviceV2EndpointAddress(portAddress);
			jiraSoapService = jiraSoapServiceServiceLocator.getJirasoapserviceV2();
		}
		return jiraSoapService;
	}

	public static void main(String[] args) throws Exception {
		String jiraToken = getJiraSoapService().login("sbs-jira-plugin", "");

		RemoteUser user = getJiraSoapService().getUser(jiraToken, "lkrzyzanek");
		System.out.println("User: " + user);

		RemoteIssue[] updatedIssues = getJiraSoapService().getIssuesFromJqlSearch(jiraToken, "updated >= \"-" + 1 + "m\"", 100);
		System.out.println("updated issues: " + updatedIssues);


		//		RemoteGroup remoteGroup = getJiraSoapService().getGroup(jiraToken, "JBoss Employee");

//		getJiraSoapService().removeUserFromGroup(jiraToken, remoteGroup, user);
	}
}
