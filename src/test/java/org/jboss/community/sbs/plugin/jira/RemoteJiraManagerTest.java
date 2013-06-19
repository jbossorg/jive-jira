/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.community.sbs.plugin.jira;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RemoteJiraManagerTest {

	@Test
	public void testNormalizeJiraForumReference() {
		RemoteJiraManager manager = new RemoteJiraManagerImpl();
		assertEquals("http://seamframework.org/Community/AnotherInterceptorProblem",
				manager.normalizeJiraForumReference("http://seamframework.org/Community/AnotherInterceptorProblem"));

		assertEquals("https://community.jboss.org/message/577806",
				manager.normalizeJiraForumReference("https://community.jboss.org/message/577806"));

		assertEquals(
				"http://netty-forums-and-mailing-lists.685743.n2.nabble.com/Netty-3-2-3-Final-released-tp5669985.html",
				manager
						.normalizeJiraForumReference("http://netty-forums-and-mailing-lists.685743.n2.nabble.com/Netty-3-2-3-Final-released-tp5669985.html"));

		assertEquals("https://community.jboss.org/message/550657#550657",
				manager.normalizeJiraForumReference("https://community.jboss.org/message/550657#550657"));

		assertEquals("https://community.jboss.org/thread/39512?tstart=0",
				manager.normalizeJiraForumReference("http://www.jboss.org/index.html?module=bb&op=viewtopic&t=152864"));

		assertEquals("https://community.jboss.org/wiki/SecureTheJmxConsole",
				manager.normalizeJiraForumReference("http://www.jboss.org/community/docs/DOC-12190"));

	}

}
