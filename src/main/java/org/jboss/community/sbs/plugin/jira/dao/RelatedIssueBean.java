/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.community.sbs.plugin.jira.dao;

import java.io.Serializable;

/**
 * @author Libor Krzyzanek (lkrzyzan)
 */
public class RelatedIssueBean implements Serializable {

	private static final long serialVersionUID = 4986852287660748776L;

	/**
	 * ID - primary key
	 */
	private long id;

	/**
	 * SBS's object ID
	 */
	private long objectID;

	/**
	 * SBS's Object Type
	 */
	private int objectType;

	/**
	 * JIRA's related Issue ID (key). i.e. ORG-500
	 */
	private String issueID;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getObjectID() {
		return objectID;
	}

	public void setObjectID(long objectID) {
		this.objectID = objectID;
	}

	public int getObjectType() {
		return objectType;
	}

	public void setObjectType(int objectType) {
		this.objectType = objectType;
	}

	public String getIssueID() {
		return issueID;
	}

	public void setIssueID(String issueID) {
		this.issueID = issueID;
	}

	@Override
	public String toString() {
		return "RelatedIssueBean [id=" + id + ", objectID=" + objectID + ", objectType=" + objectType + ", issueID="
				+ issueID + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((issueID == null) ? 0 : issueID.hashCode());
		result = prime * result + (int) (objectID ^ (objectID >>> 32));
		result = prime * result + objectType;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof RelatedIssueBean)) {
			return false;
		}
		RelatedIssueBean other = (RelatedIssueBean) obj;
		if (id != other.id) {
			return false;
		}
		if (issueID == null) {
			if (other.issueID != null) {
				return false;
			}
		} else if (!issueID.equals(other.issueID)) {
			return false;
		}
		if (objectID != other.objectID) {
			return false;
		}
		if (objectType != other.objectType) {
			return false;
		}
		return true;
	}

}
