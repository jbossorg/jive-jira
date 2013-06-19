/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.community.sbs.plugin.jira;

import com.atlassian.jira.rpc.exception.RemoteAuthenticationException;
import com.atlassian.jira.rpc.exception.RemoteException;
import com.atlassian.jira.rpc.exception.RemotePermissionException;
import com.atlassian.jira.rpc.soap.beans.RemoteIssue;
import com.atlassian.jira.rpc.soap.beans.RemoteUser;

import javax.xml.rpc.ServiceException;

/**
 * Manager for accessing remote JIRA API
 *
 * @author Libor Krzyzanek (lkrzyzan)
 */
public interface RemoteJiraManager {

	/**
	 * Get user
	 *
	 * @param username
	 * @return user or null
	 * @throws RemoteAuthenticationException
	 * @throws RemoteException
	 * @throws java.rmi.RemoteException
	 * @throws ServiceException
	 */
	public RemoteUser getUser(String username) throws RemoteAuthenticationException, RemoteException,
			java.rmi.RemoteException, ServiceException;

	/**
	 * Removes user from defined groups
	 *
	 * @param username
	 * @param groups   list of group names
	 * @return array of groups from which user has been removed
	 * @throws RemoteAuthenticationException
	 * @throws RemoteException
	 * @throws java.rmi.RemoteException
	 * @throws ServiceException
	 */
	public String[] removeUserFromGroups(String username, String[] groups) throws RemoteAuthenticationException,
			RemoteException, java.rmi.RemoteException, ServiceException;

	/**
	 * Get Issue from JIRA
	 *
	 * @param issueId
	 * @return RemoteIssue or throw exception
	 * @throws RemotePermissionException
	 * @throws RemoteAuthenticationException
	 * @throws RemoteException
	 * @throws java.rmi.RemoteException
	 * @throws ServiceException
	 */
	public RemoteIssue getIssue(String issueId) throws RemotePermissionException, RemoteAuthenticationException,
			RemoteException, java.rmi.RemoteException, ServiceException;

	/**
	 * Get Forum reference custom field
	 *
	 * @return array of values or null if no such values are in JIRA
	 */
	public String[] getForumReference(RemoteIssue issue);

	/**
	 * Get Issues based on JQL (Jira Query Language)
	 *
	 * @param jql
	 * @return
	 * @throws ServiceException
	 * @throws RemoteAuthenticationException
	 * @throws RemoteException
	 * @throws java.rmi.RemoteException
	 */
	public RemoteIssue[] getIssuesFromJqlSearch(String jql) throws ServiceException, RemoteAuthenticationException,
			RemoteException, java.rmi.RemoteException;

	/**
	 * Updater Issue with specified forum reference
	 *
	 * @param issueId
	 * @param forumReferences
	 * @throws RemoteException
	 * @throws java.rmi.RemoteException
	 * @throws ServiceException
	 */
	public void updateIssueForumReference(String issueId, String[] forumReferences) throws RemoteException,
			java.rmi.RemoteException, ServiceException;

	/**
	 * Helper method for normalizing jira forum reference URL.<br>
	 * Method convert old URLs like www.jboss.org/index.html?module=bb and
	 * www.jboss.org/community
	 *
	 * @param uri
	 * @return Actual URL of resource
	 */
	public String normalizeJiraForumReference(String uri);

}
