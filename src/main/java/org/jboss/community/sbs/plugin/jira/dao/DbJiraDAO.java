/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.community.sbs.plugin.jira.dao;

import com.atlassian.jira.rpc.soap.beans.RemoteCustomFieldValue;
import com.atlassian.jira.rpc.soap.beans.RemoteIssue;
import com.jivesoftware.base.database.dao.JiveJdbcDaoSupport;
import org.jboss.community.sbs.plugin.jira.RemoteJiraManagerImpl;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Libor Krzyzanek (lkrzyzan)
 */
public class DbJiraDAO extends JiveJdbcDaoSupport implements JiraDAO {

	@Override
	public List<RemoteIssue> getIssuesWithForumReference() {
		return this.getSimpleJdbcTemplate()
				.query(
						"select i.pkey, cfv.stringvalue"
								+ " from jiradb.customfieldvalue cfv join jiradb.jiraissue i on i.id = cfv.issue"
								+ " where cfv.customfield = ?", new RemoteIssueMapper(),
						RemoteJiraManagerImpl.CUSTOM_FIELD_FORUM_REFERENCE);
	}

	class RemoteIssueMapper implements ParameterizedRowMapper<RemoteIssue> {

		@Override
		public RemoteIssue mapRow(ResultSet rs, int rowNum) throws SQLException {
			RemoteIssue issue = new RemoteIssue();
			issue.setKey(rs.getString("pkey"));
			RemoteCustomFieldValue cf = new RemoteCustomFieldValue();
			cf.setCustomfieldId("customfield_" + RemoteJiraManagerImpl.CUSTOM_FIELD_FORUM_REFERENCE);
			cf.setValues(new String[]{rs.getString("stringvalue")});

			issue.setCustomFieldValues(new RemoteCustomFieldValue[]{cf});
			return issue;
		}

	}

}
