/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.community.sbs.plugin.jira.dao;

import com.atlassian.jira.rpc.soap.beans.RemoteIssue;
import org.jboss.community.sbs.plugin.jira.RemoteJiraManager;

import java.util.List;

/**
 * DAO for JIRA. This is very low level API and should be used very carefully.
 * Consider using standard JIRA API - {@link RemoteJiraManager}
 *
 * @author Libor Krzyzanek (lkrzyzan)
 */
public interface JiraDAO {

	/**
	 * Get Issues with filled JBoss Forum Reference
	 *
	 * @return List of remote issues. Only issueKey and customField is filled.
	 */
	public List<RemoteIssue> getIssuesWithForumReference();

}
