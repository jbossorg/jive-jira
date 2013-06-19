/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.community.sbs.plugin.jira;

import com.atlassian.jira.rpc.exception.RemoteAuthenticationException;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.exception.RemotePermissionException;
import com.atlassian.jira.rpc.soap.beans.*;
import com.jivesoftware.community.JiveGlobals;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.issues.rpc.soap.jirasoapservice_v2.JiraSoapService;
import org.jboss.issues.rpc.soap.jirasoapservice_v2.JiraSoapServiceServiceLocator;
import org.springframework.beans.factory.DisposableBean;

import javax.xml.rpc.ServiceException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * To update remote Jira Soap client classes run:<br>
 * <p/>
 * <pre>
 * java org.apache.axis.wsdl.WSDL2Java -n https://issues.jboss.org/rpc/soap/jirasoapservice-v2?wsdl
 * </pre>
 *
 * @author Libor Krzyzanek (lkrzyzan)
 */
public class RemoteJiraManagerImpl implements RemoteJiraManager, DisposableBean {

	private static final Logger log = LogManager.getLogger(RemoteJiraManagerImpl.class);

	public static final String CUSTOM_FIELD_FORUM_REFERENCE = "12310010";

	private JiraSoapService jiraSoapService = null;

	private String jiraToken = "";

	private JiraSoapService getJiraSoapService() throws ServiceException {
		if (jiraSoapService == null) {
			String portAddress = JiveGlobals.getJiveProperty("jboss.jira.baseURLInternal") + "/rpc/soap/jirasoapservice-v2";
			log.info("Create JIRA Soap Service on: " + portAddress);

			JiraSoapServiceServiceLocator jiraSoapServiceServiceLocator = new JiraSoapServiceServiceLocator();
			jiraSoapServiceServiceLocator.setJirasoapserviceV2EndpointAddress(portAddress);
			jiraSoapService = jiraSoapServiceServiceLocator.getJirasoapserviceV2();
		}
		return jiraSoapService;
	}

	private String doLogin() throws RemoteAuthenticationException, RemoteException, java.rmi.RemoteException,
			ServiceException {
		log.info("Trying to log in via SOAP WS to JIRA");
		final String username = JiveGlobals.getJiveProperty("jboss.jira.wsdlUsername");
		final String password = JiveGlobals.getJiveProperty("jboss.jira.wsdlPassword");
		return getJiraSoapService().login(username, password);
	}

	private void checkLogin() throws RemoteAuthenticationException, RemoteException, java.rmi.RemoteException,
			ServiceException {
		synchronized (jiraToken) {
			if (jiraToken.isEmpty()) {
				jiraToken = doLogin();
			}
		}
	}

	/**
	 * Do again login - useful in cases that SOAP session expired.
	 *
	 * @throws RemoteAuthenticationException
	 * @throws RemoteException
	 * @throws java.rmi.RemoteException
	 * @throws ServiceException
	 */
	private void reLogin() throws RemoteAuthenticationException, RemoteException, java.rmi.RemoteException,
			ServiceException {
		log.info("Re-login needed");
		synchronized (jiraToken) {
			jiraToken = doLogin();
		}
	}

	private void doLogout() throws java.rmi.RemoteException, ServiceException {
		log.info("Trying to log out from JIRA via SOAP WS");
		synchronized (jiraToken) {
			if (!jiraToken.isEmpty()) {
				getJiraSoapService().logout(jiraToken);
			}
		}
	}

	@Override
	public RemoteUser getUser(String username) throws RemoteAuthenticationException, RemoteException,
			java.rmi.RemoteException, ServiceException {
		checkLogin();
		RemoteUser user = null;

		if (log.isDebugEnabled()) {
			log.debug("Get JIRA user: " + username);
		}
		try {
			user = getJiraSoapService().getUser(jiraToken, username);
		} catch (Exception e) {
			// Jira SOAP returns always unknown exception in case of any problems
			// it's not possible to catch RemoteAuthenticationException
			reLogin();
			user = getJiraSoapService().getUser(jiraToken, username);
		}
		return user;
	}

	@Override
	public String[] removeUserFromGroups(String username, String[] groups) throws RemoteAuthenticationException,
			RemoteException, java.rmi.RemoteException, ServiceException {
		RemoteUser user = getUser(username);

		if (user == null) {
			if (log.isDebugEnabled()) {
				log.debug("User " + username + "not found in Jira");
			}
			return new String[0];
		}

		List<String> removedGroups = new ArrayList<String>();
		for (String g : groups) {
			if (log.isTraceEnabled()) {
				log.trace("Removing from group: " + g);
			}
			try {
				RemoteGroup remoteGroup = getJiraSoapService().getGroup(jiraToken, g.trim());
				if (log.isTraceEnabled()) {
					log.trace("Remote group to remove: " + remoteGroup);
				}
				try {
					getJiraSoapService().removeUserFromGroup(jiraToken, remoteGroup, user);
					removedGroups.add(remoteGroup.getName());
				} catch (Exception e) {
					log.info("Cannot remove user " + username + " from group " + remoteGroup.getName()
							+ ". User is probably not a member of this group");
				}
			} catch (RemoteException e) {
				log.error("Cannot remove user from group " + g, e);
			}
		}
		return removedGroups.toArray(new String[0]);
	}

	@Override
	public RemoteIssue getIssue(String id) throws RemotePermissionException, RemoteAuthenticationException,
			RemoteException, java.rmi.RemoteException, ServiceException {
		log.info("Get Issue from JIRA via SOAP WS");
		checkLogin();
		RemoteIssue issue;
		try {
			issue = getJiraSoapService().getIssue(jiraToken, id);
		} catch (Exception e) {
			// Jira SOAP returns always unknown exception in case of any problems
			// it's not possible to catch RemoteAuthenticationException
			reLogin();
			issue = getJiraSoapService().getIssue(jiraToken, id);
		}
		return issue;
	}

	@Override
	public String[] getForumReference(RemoteIssue issue) {
		final String CF_ID = "customfield_" + CUSTOM_FIELD_FORUM_REFERENCE;

		RemoteCustomFieldValue[] cfValues = issue.getCustomFieldValues();
		for (RemoteCustomFieldValue remoteCustomFieldValue : cfValues) {
			if (CF_ID.equals(remoteCustomFieldValue.getCustomfieldId())) {
				return remoteCustomFieldValue.getValues();
			}
		}
		return null;
	}

	@Override
	public RemoteIssue[] getIssuesFromJqlSearch(String jql) throws ServiceException, RemoteAuthenticationException,
			RemoteException, java.rmi.RemoteException {
		checkLogin();

		if (log.isInfoEnabled()) {
			log.info("Get Issues from JQL Search. Query: " + jql);
		}

		// TODO change maxNumResults
		try {
			return getJiraSoapService().getIssuesFromJqlSearch(jiraToken, jql, 100);
		} catch (Exception e) {
			// Jira SOAP returns always unknown exception in case of any problems
			// it's not possible to catch RemoteAuthenticationException
			reLogin();
			return getJiraSoapService().getIssuesFromJqlSearch(jiraToken, jql, 100);
		}
	}

	@Override
	public void updateIssueForumReference(String issueId, String[] forumReferences) throws RemoteException,
			java.rmi.RemoteException, ServiceException {
		checkLogin();
		RemoteFieldValue forumReferenceField = new RemoteFieldValue();
		forumReferenceField.setId("customfield_" + CUSTOM_FIELD_FORUM_REFERENCE);
		forumReferenceField.setValues(forumReferences);

		if (log.isInfoEnabled()) {
			log.info("updateIssueForumReference(" + issueId + ")");
			log.info(forumReferences == null ? "null" : Arrays.asList(forumReferences));
		}

		try {
			getJiraSoapService().updateIssue(jiraToken, issueId, new RemoteFieldValue[]{forumReferenceField});
		} catch (Exception e) {
			// Jira SOAP returns always unknown exception in case of any problems
			// it's not possible to catch RemoteAuthenticationException
			reLogin();
			getJiraSoapService().updateIssue(jiraToken, issueId, new RemoteFieldValue[]{forumReferenceField});
		}
	}

	@Override
	public String normalizeJiraForumReference(String uri) {
		if (uri.contains("index.html?module=bb") || uri.contains("jboss.org/community")) {
			// Get actual URL from old URL (based on real redirects)
			HttpClient httpClient = new HttpClient();
			GetMethod getMethod = new GetMethod(uri);
			getMethod.setFollowRedirects(true);
			try {
				httpClient.executeMethod(getMethod);
				final String actualURL = getMethod.getURI().toString();
				getMethod.releaseConnection();

				return actualURL;
			} catch (Exception e) {
				log.error("Cannot get Actual URL. Old URL: " + uri, e);
				return uri;
			}
		}
		return uri;
	}

	@Override
	public void destroy() throws Exception {
		doLogout();
	}

}
