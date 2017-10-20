/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.community.sbs.plugin.jira;

import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Manager for accessing remote JIRA API
 *
 * @author Libor Krzyzanek (lkrzyzan)
 */
public interface RemoteJiraManager {

	/**
	 * Get Issue from JIRA
	 *
	 * @param issueId
	 * @return RemoteIssue or throw exception
	 */
	public JsonNode getIssue(String issueId);

	/**
	 * Get Forum reference custom field
	 *
	 * @return array of values or null if no such values are in JIRA
	 */
	public Set<String> getForumReference(JsonNode issue);

	/**
	 * Get Issues based on JQL (Jira Query Language)
	 *
	 * @param jql
	 * @return
	 */
	public Iterable<JsonNode> getIssuesFromJqlSearch(String jql);

	/**
	 * Updater Issue with specified forum reference
	 *
	 * @param issueId
	 * @param forumReferences
	 */
	public void updateIssueForumReference(String issueId, Set<String> forumReferences);

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
