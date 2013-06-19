/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.community.sbs.plugin.jira.dao;

import com.jivesoftware.base.database.dao.JiveJdbcDaoSupport;
import com.jivesoftware.base.database.sequence.SequenceManager;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Libor Krzyzanek (lkrzyzan)
 */
public class DbIssueLinkDAOImpl extends JiveJdbcDaoSupport implements IssueLinkDAO {

	public static final int ISSUE_LINK_SEQ = 5010;

	@Override
	public List<String> getByJiveObject(long objectType, long objectID) throws EmptyResultDataAccessException {
		return this.getSimpleJdbcTemplate().query(
				"SELECT issueID FROM jbossJiraIssueLink WHERE objectType = ? AND objectID = ?", new StringMapper(), objectType,
				objectID);
	}

	@Override
	public List<RelatedIssueBean> getByIssueID(String issueID) {
		return this.getSimpleJdbcTemplate().query("SELECT * FROM jbossJiraIssueLink WHERE issueID = ?",
				new RelatedIssueBeanMapper(), issueID);
	}

	@Override
	public void createLink(long objectType, long objectID, String issueID) throws DataAccessException {
		long id = SequenceManager.nextID(ISSUE_LINK_SEQ);

		getSimpleJdbcTemplate().update(
				"INSERT INTO jbossJiraIssueLink (id, objectType, objectID, issueID) VALUES (?, ?, ?, ?)", id, objectType,
				objectID, issueID);
	}

	@Override
	public void removeLink(long objectType, long objectID, String issueID) throws DataAccessException {
		getSimpleJdbcTemplate().update(
				"DELETE FROM jbossJiraIssueLink WHERE objectType = ? AND objectID = ? AND issueID = ?", objectType, objectID,
				issueID);
	}

	@Override
	public void removeLinks(String issueID) throws DataAccessException {
		getSimpleJdbcTemplate().update("DELETE FROM jbossJiraIssueLink WHERE issueID = ?", issueID);
	}

	@Override
	public void removeAllLinks() throws DataAccessException {
		getSimpleJdbcTemplate().update("DELETE FROM jbossJiraIssueLink");
	}

	private static class StringMapper implements ParameterizedRowMapper<String> {
		public String mapRow(ResultSet rs, int rowNum) throws SQLException {
			return rs.getString(1);
		}
	}

	private static class RelatedIssueBeanMapper implements ParameterizedRowMapper<RelatedIssueBean> {
		public RelatedIssueBean mapRow(ResultSet rs, int rowNum) throws SQLException {
			RelatedIssueBean o = new RelatedIssueBean();
			o.setId(rs.getLong("id"));
			o.setObjectType(rs.getInt("objectType"));
			o.setObjectID(rs.getLong("objectID"));
			o.setIssueID(rs.getString("issueID"));
			return o;
		}
	}
}
