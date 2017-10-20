/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.community.sbs.plugin.jira;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.community.sbs.plugin.jira.dao.IssueLinkDAO;
import org.jboss.community.sbs.plugin.jira.dao.RelatedIssueBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;

import com.fasterxml.jackson.databind.JsonNode;
import com.jivesoftware.base.event.v2.EventListener;
import com.jivesoftware.cache.Cache;
import com.jivesoftware.community.ForumMessage;
import com.jivesoftware.community.ForumThread;
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.JiveObject;
import com.jivesoftware.community.JiveObjectLoader;
import com.jivesoftware.community.NotFoundException;
import com.jivesoftware.community.lifecycle.ApplicationState;
import com.jivesoftware.community.lifecycle.ApplicationStateChangeEvent;
import com.jivesoftware.community.web.GlobalResourceResolver;

/**
 * DB Jira manager implementation
 *
 * @author Libor Krzyzanek (lkrzyzan)
 */
public class DbJiraManager implements JiraManager, EventListener<ApplicationStateChangeEvent> {

	private static final Logger log = LogManager.getLogger(DbJiraManager.class);

	private IssueLinkDAO issueLinkDAO;

	/**
	 * A cache for HF URL titles.<br>
	 * Key is {objectype}-{objectId}<br>
	 * Value is list of issue keys
	 */
	private Cache<String, List<String>> issueLinkCache;

	private JiveObjectLoader jiveObjectLoader;

	private RemoteJiraManager remoteJiraManager;

	private GlobalResourceResolver globalResourceResolver;

	private JiraForumReferenceCheckerThread jiraForumReferenceCheckerThread;

	private String sbsDomain;

	public static enum RESULT {
		OK, FORBIDDEN, ISSUE_NOT_FOUND, OBJECT_NOT_FOUND, ISSUE_CLOSED
	}

	@Override
	public void handle(ApplicationStateChangeEvent e) {
		if (e.getNewState().equals(ApplicationState.RUNNING)) {
			jiraForumReferenceCheckerThread = new JiraForumReferenceCheckerThread(this);
			jiraForumReferenceCheckerThread.setDaemon(true);
			jiraForumReferenceCheckerThread.start();
		}
		if (e.getNewState().equals(ApplicationState.SHUTDOWN)) {
			jiraForumReferenceCheckerThread.stopNextExecution();
		}
	}

	private String getCacheKey(int objectType, long objectId) {
		return objectType + "-" + objectId;
	}

	@Override
	public List<String> getRelatedIssues(int objectType, long objectId) {
		if (log.isInfoEnabled()) {
			log.info("Get Related Issues for objectType: " + objectType + " id: " + objectId);
		}
		final String cacheKey = getCacheKey(objectType, objectId);
		List<String> issues = issueLinkCache.get(cacheKey);
		if (issues == null) {
			try {
				issues = issueLinkDAO.getByJiveObject(objectType, objectId);
				issueLinkCache.put(cacheKey, issues);
			} catch (EmptyResultDataAccessException e) {
				return null;
			}
		}
		return issues;
	}

	@Override
	public String addLink(int objectType, long objectId, String issueID) throws IllegalArgumentException {
		// Jira uses upper case IDs
		issueID = issueID.toUpperCase();

		// 1. Check if jiveObject exists
		JiveObject jiveObject = getJiveObject(objectType, objectId);
		if (jiveObject == null) {
			throw new IllegalArgumentException("SBS object is not valid.");
		}

		// 2. Check if issue exists
		// and user has permission to this issue - this is not possible
		JsonNode issue;
		try {
			issue = remoteJiraManager.getIssue(issueID);
			if ("done".equalsIgnoreCase(issue.findValue("statusCategory").get("key").asText())) {
				return RESULT.ISSUE_CLOSED.toString();
			}

		} catch (Exception e) {
			log.error("Cannot get Issue from JIRA", e);
			return RESULT.ISSUE_NOT_FOUND.toString();
		}

		// 3. Check if link already exists.
		try {
			List<String> issues = issueLinkDAO.getByJiveObject(objectType, objectId);
			if (issues.contains(issueID)) {
				throw new DataIntegrityViolationException("Link already exists");
			}
		} catch (EmptyResultDataAccessException e) {
			// This is OK. Process can continue.
		}

		// 5. Update JIRA ticket
		final String url = globalResourceResolver.getURL(jiveObject, true);
		try {
			Set<String> references = remoteJiraManager.getForumReference(issue);
			if (references != null) {
				references.add(url);

				remoteJiraManager.updateIssueForumReference(issueID, references);
			} else {
				remoteJiraManager.updateIssueForumReference(issueID, new HashSet<>(Arrays.asList(url)));
			}
		} catch (Exception e) {
			log.error("Cannot update JIRA Issue", e);
			throw new RuntimeException("Cannot update JIRA Issue");
		}

		// 6. Add link
		createLinkInDB(objectType, objectId, issueID);

		return RESULT.OK.toString();
	}

	private void createLinkInDB(int objectType, long objectId, String issueID) {
		issueLinkDAO.createLink(objectType, objectId, issueID);

		String cacheKey = getCacheKey(objectType, objectId);

		List<String> issues = issueLinkDAO.getByJiveObject(objectType, objectId);
		issueLinkCache.put(cacheKey, issues);
	}

	@Override
	public String removeLink(int objectType, long objectId, String issueID) throws IllegalArgumentException {
		// 1. Check if jiveObject exists
		JiveObject jiveObject = getJiveObject(objectType, objectId);
		if (jiveObject == null) {
			throw new IllegalArgumentException("SBS object is not valid.");
		}

		final String url = globalResourceResolver.getURL(jiveObject, true);

		// 2. Update JIRA ticket
		try {
			JsonNode issue = remoteJiraManager.getIssue(issueID);
			Set<String> references = remoteJiraManager.getForumReference(issue);

			if (references != null) {
				references.remove(url);

				remoteJiraManager.updateIssueForumReference(issueID, references);
			} else {
				remoteJiraManager.updateIssueForumReference(issueID, null);
			}
		} catch (Exception e) {
			log.error("Cannot update JIRA Issue", e);
			throw new RuntimeException("Cannot update JIRA Issue");
		}

		issueLinkDAO.removeLink(objectType, objectId, issueID);

		final String cacheKey = getCacheKey(objectType, objectId);
		// just completely remove from cache and let retrieve new value in next call
		issueLinkCache.remove(cacheKey);

		return RESULT.OK.toString();
	}

	@Override
	public void updateLinks(int period) {
		log.debug("Get links from JIRA");

		Iterable<JsonNode> remoteIssues;
		try {
			remoteIssues = remoteJiraManager.getIssuesFromJqlSearch("updated >= \"-" + period + "m\"");
		} catch (Exception e) {
			log.error("Cannot get Issues from Remote JIRA", e);
			return;
		}

		log.debug("Check each issue if we have it in SBS database");
		for (JsonNode remoteIssue : remoteIssues) {
			try {
				updateLink(remoteIssue);
			} catch (Exception e) {
				log.error("Updating link of JIRA issue failed. Issue key: " + remoteIssue.get("key").asText(), e);
			}
		}
	}

	private void updateLink(JsonNode remoteIssue) {
		final String issueKey = remoteIssue.get("key").asText();

		Set<String> values = remoteJiraManager.getForumReference(remoteIssue);

		if (log.isInfoEnabled()) {
			log.info("Remove All Related Issue in SBS related to ticket: " + issueKey);
		}
		// Remove those related issues in all cases and insert them afterwards
		List<RelatedIssueBean> issuesInDB = issueLinkDAO.getByIssueID(issueKey);
		for (RelatedIssueBean relatedIssueBean : issuesInDB) {
			issueLinkCache.remove(getCacheKey(relatedIssueBean.getObjectType(), relatedIssueBean.getObjectID()));
		}
		issueLinkDAO.removeLinks(issueKey);

		if (values != null) {
			for (String value : values) {
				JiveObject relatedIssueObject = getRelatedIssueJiveObject(value);
				if (relatedIssueObject != null) {
					log.info("Create new link (based on JIRA value)");
					createLinkInDB(relatedIssueObject.getObjectType(), relatedIssueObject.getID(), issueKey);
				} else {
					if (log.isInfoEnabled()) {
						log.info("JBoss Forum Reference is not a Jive resource. URI: " + value);
					}
				}

			}
		}

	}

	private String getSbsDomain() {
		if (sbsDomain == null) {
			sbsDomain = JiveGlobals.getJiveProperty("jboss.jira.sbsDomainToCheck", "developer.jboss.org/");
		}
		return sbsDomain;
	}

	private JiveObject getRelatedIssueJiveObject(String jiraCustomFieldValueURL) {
		if (!jiraCustomFieldValueURL.contains(getSbsDomain())) {
			return null;
		}
		JiveObject o;
		try {
			o = globalResourceResolver.getObj(jiraCustomFieldValueURL);
		} catch (NotFoundException e) {
			return null;
		}
		// In forums root message is used
		if (o.getObjectType() == JiveConstants.THREAD) {
			o = ((ForumThread) o).getRootMessage();
		}
		if (o.getObjectType() == JiveConstants.MESSAGE) {
			o = ((ForumMessage) o).getForumThread().getRootMessage();
		}
		return o;
	}

	private JiveObject getJiveObject(int objectType, long objectId) {
		try {
			return jiveObjectLoader.getJiveObject(objectType, objectId);
		} catch (NotFoundException e) {
			return null;
		}
	}

	public void setIssueLinkDAO(IssueLinkDAO issueLinkDAO) {
		this.issueLinkDAO = issueLinkDAO;
	}

	public void setJiveObjectLoader(JiveObjectLoader jiveObjectLoader) {
		this.jiveObjectLoader = jiveObjectLoader;
	}

	public void setRemoteJiraManager(RemoteJiraManager remoteJiraManager) {
		this.remoteJiraManager = remoteJiraManager;
	}

	public void setIssueLinkCache(Cache<String, List<String>> issueLinkCache) {
		this.issueLinkCache = issueLinkCache;
	}

	public void setGlobalResourceResolver(GlobalResourceResolver globalResourceResolver) {
		this.globalResourceResolver = globalResourceResolver;
	}
}
