/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.community.sbs.plugin.jira;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.jivesoftware.community.JiveGlobals;

/**
 * To update remote Jira Soap client classes run:<br>
 * <p/>
 * <pre>
 * java org.apache.axis.wsdl.WSDL2Java -n https://issues.jboss.org/rpc/soap/jirasoapservice-v2?wsdl
 * </pre>
 *
 * @author Libor Krzyzanek (lkrzyzan)
 */
public class RemoteJiraManagerImpl implements RemoteJiraManager {

    private static final Logger log = LogManager.getLogger(RemoteJiraManagerImpl.class);

    public static final String CUSTOM_FIELD_FORUM_REFERENCE = "12310010";

    protected String getJiraUrl() {
        return JiveGlobals.getJiveProperty("jboss.jira.baseURLInternal");
    }

    protected BasicAuthRestTemplate getRestTemplate() {
        return new BasicAuthRestTemplate(JiveGlobals.getJiveProperty("jboss.jira.wsdlUsername"), JiveGlobals.getJiveProperty("jboss.jira.wsdlPassword"));
    }

    @Override
    public JsonNode getIssue(String id) {
        log.info("Get Issue from JIRA via REST API");
        return getRestTemplate().getForEntity(getJiraUrl()
                + "/rest/api/2/issue/" + id
                + "?fields=customfield_12310010&fields=status",
                JsonNode.class).getBody();
    }

    @Override
    public Set<String> getForumReference(JsonNode issue) {
        final String CF_ID = "customfield_" + CUSTOM_FIELD_FORUM_REFERENCE;

        JsonNode values = issue.findValue(CF_ID);
        if (values != null && values.isArray()) {
            Set<String> result = new HashSet<>(values.size());
            for (final JsonNode objNode : values) {
                result.add(objNode.textValue());
            }
            return result;
        }
        return null;
    }

    @Override
    public Iterable<JsonNode> getIssuesFromJqlSearch(String jql) {
        if (log.isInfoEnabled()) {
            log.info("Get Issues from JQL Search. Query: " + jql);
        }
        ResponseEntity<JsonNode> response = getRestTemplate().getForEntity(
                getJiraUrl() + "/rest/api/2/search?jql=" + jql
                        + "&fields=customfield_12310010&fields=status",
                JsonNode.class);
        return response.getBody().get("issues");
    }

    @Override
    public void updateIssueForumReference(String issueId, Set<String> forumReferences) {
        if (log.isInfoEnabled()) {
            log.info("updateIssueForumReference(" + issueId + ")");
            log.info(forumReferences == null ? "null" : Arrays.asList(forumReferences));
        }

        Map<String, Object> fields = new HashMap<>();
        fields.put("customfield_" + CUSTOM_FIELD_FORUM_REFERENCE, StringUtils.join(forumReferences, '\n'));
        Map<String, Object> params = new HashMap<>();
        params.put("fields", fields);

        getRestTemplate().put(getJiraUrl() + "/rest/api/2/issue/" + issueId, params);
    }

    @Override
    public String normalizeJiraForumReference(String uri) {
        if (uri.contains("index.html?module=bb") || uri.contains("jboss.org/community")) {
            // Get actual URL from old URL (based on real redirects)
            HttpClient httpClient = new HttpClient();
            GetMethod getMethod = new GetMethod(uri);
            getMethod.setFollowRedirects(true);
            try {
                httpClient.executeMethod(getMethod);
                final String actualURL = getMethod.getURI().toString();
                getMethod.releaseConnection();

                return actualURL;
            } catch (Exception e) {
                log.error("Cannot get Actual URL. Old URL: " + uri, e);
                return uri;
            }
        }
        return uri;
    }

}
