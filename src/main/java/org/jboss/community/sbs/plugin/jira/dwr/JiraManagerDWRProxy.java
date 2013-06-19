/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.community.sbs.plugin.jira.dwr;

import com.jivesoftware.base.aaa.AuthenticationProvider;
import com.jivesoftware.community.aaa.AnonymousUser;
import com.jivesoftware.community.aaa.MethodNotSupportedException;
import org.apache.commons.lang.StringUtils;
import org.jboss.community.sbs.plugin.jira.DbJiraManager.RESULT;
import org.jboss.community.sbs.plugin.jira.JiraManager;

import java.util.List;

/**
 * @author Libor Krzyzanek (lkrzyzan)
 */
public class JiraManagerDWRProxy implements JiraManager {

	private JiraManager jiraManager;

	private AuthenticationProvider authenticationProvider;

	public void setJiraManager(JiraManager jiraManager) {
		this.jiraManager = jiraManager;
	}

	protected boolean isAnonymous() {
		return AnonymousUser.ANONYMOUS_ID == authenticationProvider.getJiveUser().getID();
	}

	@Override
	public List<String> getRelatedIssues(int objectType, long objectId) {
		return jiraManager.getRelatedIssues(objectType, objectId);
	}

	protected void validateIssueID(String issueID) throws IllegalArgumentException {
		if (StringUtils.isBlank(issueID)) {
			throw new IllegalArgumentException("Issue ID must be specified");
		}
	}

	@Override
	public String addLink(int objectType, long objectId, String issueID) throws IllegalArgumentException {
		if (isAnonymous()) {
			return RESULT.FORBIDDEN.toString();
		}
		validateIssueID(issueID);

		// TODO: Check if IssueID already exists for specififed object. Rather check it in business logic and throw AlreadyExistsException

		return jiraManager.addLink(objectType, objectId, issueID);
	}

	@Override
	public String removeLink(int objectType, long objectId, String issueID) throws IllegalArgumentException {
		if (isAnonymous()) {
			return RESULT.FORBIDDEN.toString();
		}
		validateIssueID(issueID);
		return jiraManager.removeLink(objectType, objectId, issueID);
	}

	public void setAuthenticationProvider(AuthenticationProvider authenticationProvider) {
		this.authenticationProvider = authenticationProvider;
	}

	@Override
	public void updateLinks(int period) {
		throw new MethodNotSupportedException("Not supported via DWR");
	}

	@Override
	public int syncJira2SBS() {
		throw new MethodNotSupportedException("Not supported via DWR");
	}

}
