/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.community.sbs.plugin.jira.dao;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;

/**
 * DAO for IssueLink
 *
 * @author Libor Krzyzanek (lkrzyzan)
 */
public interface IssueLinkDAO {

	/**
	 * Get list of issues
	 *
	 * @param objectType
	 * @param objectID
	 * @return
	 * @throws EmptyResultDataAccessException
	 */
	public List<String> getByJiveObject(long objectType, long objectID) throws EmptyResultDataAccessException;

	/**
	 * Get list of related issues based on issueID.
	 *
	 * @param issueID
	 * @return list of RelatedIssueBean
	 */
	public List<RelatedIssueBean> getByIssueID(String issueID);

	/**
	 * Create link between Jive object and issue
	 *
	 * @param objectType
	 * @param objectID
	 * @param issueID
	 * @throws DataAccessException
	 */
	public void createLink(long objectType, long objectID, String issueID) throws DataAccessException;

	/**
	 * Remove link
	 *
	 * @param objectType
	 * @param objectID
	 * @param issueID
	 * @throws DataAccessException
	 */
	public void removeLink(long objectType, long objectID, String issueID) throws DataAccessException;

	/**
	 * Remove links related to specific Issue
	 *
	 * @param issueID
	 * @throws DataAccessException
	 */
	public void removeLinks(String issueID) throws DataAccessException;

	/**
	 * Remove all links
	 *
	 * @throws DataAccessException
	 */
	public void removeAllLinks() throws DataAccessException;

}
