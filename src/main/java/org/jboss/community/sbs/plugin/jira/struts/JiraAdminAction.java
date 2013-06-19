/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.community.sbs.plugin.jira.struts;

import com.jivesoftware.community.action.JiveActionSupport;
import org.jboss.community.sbs.plugin.jira.JiraManager;

/**
 * Action for JIRA - SBS synchronization
 *
 * @author Libor Krzyzanek (lkrzyzan)
 */
public class JiraAdminAction extends JiveActionSupport {

	private static final long serialVersionUID = 4296712704583049729L;

	private JiraManager jiraManager;

	public String fullReindex() {
		int count = jiraManager.syncJira2SBS();

		addActionMessage(getText("plugin.jira.admin.reindex.text.success", new String[]{"" + count}));

		return SUCCESS;
	}

	public void setJiraManager(JiraManager jiraManager) {
		this.jiraManager = jiraManager;
	}

}
