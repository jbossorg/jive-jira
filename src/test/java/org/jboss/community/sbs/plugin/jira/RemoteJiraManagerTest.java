/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.community.sbs.plugin.jira;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RemoteJiraManagerTest {

    public static void main(String[] args) throws URISyntaxException, IOException {
        String jiraBase = "https://issues.stage.jboss.org";

        //		System.out.println("build number:" + client.getMetadataClient().getServerInfo().claim().getBuildNumber());

        // /rest/api/2/issue/ORG-2779

        BasicAuthRestTemplate restTemplate = new BasicAuthRestTemplate("lkrzyzanek", "PASSWORD");
        //
        //		ResponseEntity<JsonNode> issue = restTemplate.getForEntity(jiraBase + "/rest/api/2/issue/ORG-2779?fields=customfield_12310010", JsonNode.class);
        //
        //		JsonNode values = issue.getBody().findValue("customfield_12310010");
        //		if (values != null && values.isArray() && values.size() > 0) {
        //
        //			System.out.println(values.get(0).textValue());
        //		}

        Map<String, Object> fields = new HashMap<>();
        fields.put("customfield_12310010", "http://localhost:8080/docs/DOC-1001\nhttp://localhost:8080/docs/DOC-1011");
        Map<String, Object> params = new HashMap<>();
        params.put("fields", fields);

        restTemplate.put(jiraBase + "/rest/api/2/issue/ORG-2779?notifyUsers=false",
                params);
        //        ResponseEntity<JsonNode> resonse = restTemplate.getForEntity(
        //                jiraBase + "/rest/api/2/search?jql=updated >= -30m"
        //                        + "&fields=customfield_12310010&fields=status",
        //                JsonNode.class);
        //
        //        System.out.println(resonse.getBody().get("issues").findValue("statusCategory").get("key").asText());


//        HttpResponse<JsonNode> response = Unirest.get(jiraBase + "/rest/api/2/issue/ORG-2779")
//				.queryString("fields", "summary")
//				.basicAuth("lkrzyzanek", "fuyJL71vp1")
//				.asJson();
//		System.out.println(response);

        System.out.println("DONE");

    }

    @Test
    public void testNormalizeJiraForumReference() {
        RemoteJiraManager manager = new RemoteJiraManagerImpl();
        assertEquals("http://seamframework.org/Community/AnotherInterceptorProblem",
                manager.normalizeJiraForumReference("http://seamframework.org/Community/AnotherInterceptorProblem"));

        assertEquals("https://developer.jboss.org/message/577806",
                manager.normalizeJiraForumReference("https://developer.jboss.org/message/577806"));

        assertEquals(
                "http://netty-forums-and-mailing-lists.685743.n2.nabble.com/Netty-3-2-3-Final-released-tp5669985.html",
                manager
                        .normalizeJiraForumReference("http://netty-forums-and-mailing-lists.685743.n2.nabble.com/Netty-3-2-3-Final-released-tp5669985.html"));

        assertEquals("https://developer.jboss.org/message/550657#550657",
                manager.normalizeJiraForumReference("https://developer.jboss.org/message/550657#550657"));
/* No more supported URL from nukes forums
        assertEquals("https://community.jboss.org/thread/39512?tstart=0",
				manager.normalizeJiraForumReference("http://www.jboss.org/index.html?module=bb&op=viewtopic&t=152864"));
*/

        assertEquals("https://developer.jboss.org/wiki/SecureTheJmxConsole",
                manager.normalizeJiraForumReference("http://www.jboss.org/community/docs/DOC-12190"));

    }

}
