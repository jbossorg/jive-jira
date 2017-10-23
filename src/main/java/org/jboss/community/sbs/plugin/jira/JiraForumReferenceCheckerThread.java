/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.community.sbs.plugin.jira;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.jivesoftware.community.JiveGlobals;

/**
 * @author Libor Krzyzanek (lkrzyzan)
 */
public class JiraForumReferenceCheckerThread extends Thread {

	private static final Logger log = LogManager.getLogger(JiraForumReferenceCheckerThread.class);

	private JiraManager jiraManager;

	private static boolean run = true;

	public JiraForumReferenceCheckerThread(JiraManager jiraManager) {
		super("JiraForumReferenceCheckerThread");
		this.jiraManager = jiraManager;
	}

	@Override
	public void run() {
		log.info("Start JiraForumReferenceCheckerThread");
		int specialUpdateCountTo = getSpecialUpdateCountTo();
		while (run) {
			int intervalInMinutes = JiveGlobals.getJiveIntProperty("jboss.jira.updateJiraTicketsInterval", 10);
			try {
				Thread.sleep(intervalInMinutes * 60 * 1000);
			} catch (InterruptedException e) {
				log.error("Interupted. Quiting");
				run = false;
				break;
			}

			int periodInMinutes = JiveGlobals.getJiveIntProperty("jboss.jira.updateJiraTicketsPeriod", 15);

			if (specialUpdateCountTo <= 0) {
				periodInMinutes = JiveGlobals.getJiveIntProperty("jboss.jira.specialUpdateJiraTicketsPeriod", 360);
				specialUpdateCountTo = getSpecialUpdateCountTo();
				log.debug("Special Update");
			} else {
				specialUpdateCountTo--;
			}

			log.debug("Update links from JIRA");
			try {
				jiraManager.updateLinks(periodInMinutes);
			} catch (Exception e) {
				log.error("Execution of update links from JIRA to SBS failed.", e);
			}
		}
	}

	private int getSpecialUpdateCountTo() {
		return JiveGlobals.getJiveIntProperty("jboss.jira.specialUpdateCountTo", 36);
	}

	public void stopNextExecution() {
		run = false;
	}

}
