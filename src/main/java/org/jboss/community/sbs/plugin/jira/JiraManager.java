/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.community.sbs.plugin.jira;

import java.util.List;

/**
 * @author Libor Krzyzanek (lkrzyzan)
 */
public interface JiraManager {

	/**
	 * Get related issues to specified object
	 *
	 * @param objectType
	 * @param objectId
	 * @return List of Issues like ORG-500 or null
	 */
	public List<String> getRelatedIssues(int objectType, long objectId);

	/**
	 * Add link between specified object and Issue
	 *
	 * @param objectType
	 * @param objectId
	 * @param issueID
	 * @return Result. Can be: "OK", "ISSUE_NOT_FOUND"
	 * @throws IllegalArgumentException if some argument is not correct.
	 */
	public String addLink(int objectType, long objectId, String issueID) throws IllegalArgumentException;

	/**
	 * Remove link
	 *
	 * @param objectType
	 * @param objectId
	 * @param issueID
	 * @return Result. Can be: "OK"
	 * @throws IllegalArgumentException if some argument is not correct.
	 */
	public String removeLink(int objectType, long objectId, String issueID) throws IllegalArgumentException;

	/**
	 * Update Links
	 *
	 * @param period recent (in minutes) updated tickets in JIRA to be checked
	 */
	public void updateLinks(int period);


}
