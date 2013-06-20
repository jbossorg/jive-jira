<#macro jiraWidget objectType objectID containerType=document>

<!-- BEGIN sidebar box 'JIRA' -->
<div id="jboss-jira-sidebar" class="jive-box jive-sidebar">

<@resource.dwr file="JiraManager" />
<@resource.javascript>
/**
 * jboss.JiraApp.JiraSource
 *
 * Model class that encapsulates the server interface for retrieving and
 * updating 'jira' results for a specified object.
 *
 * To use create an instance of Jira and call the refreshIssues method
 * with the object type and id of the piece of content you need related issues for
 */

/*extern jive $j */

jive.namespace("JiraApp");

/* Global variables for Jboss Jira */
var jbossJiraApp;
var jbossJiraView;
var jbossJiraSource;
var jiraJBossDWRtimeout = 20000; // 20 sec.
var fadeAnimationLength = 300;
var jiraBaseURL = "${JiveGlobals.getJiveProperty("jboss.jira.baseURL")}";
var addIssueBoxValue = "";


jive.JiraApp.JiraSource = function() {

    /**
     * refreshIssues(objectType, objectID)
     * - objectType (int): the content type of the object to get similar results for
     * - objectID (int): the id of the content object to get similar results for
     *
     * Retrieves similar results for the specified content object from the server and
     * passes the results to the callback.
     */
    function refreshIssues(objectType, objectID) {
        JiraManager.getRelatedIssues(objectType, objectID, {
            callback: function(issues) {
                if(issues != null && issues.length > 0) {
                  jbossJiraView.addIssuesIDs(issues);
                  jbossJiraSource.retrieveIssuesFromJIRA(issues);
                } else {
                  jbossJiraView.noRelatedIssues();
                }
            },
            errorHandler:function(errorString, exception) {
                jbossJiraView.displayError();
            },
            timeout: jiraJBossDWRtimeout
        });

        return this;
    }
    
    /**
     * Retrieve details about issues from JIRA
     * - issues - List or Issues
     */ 
    function retrieveIssuesFromJIRA(issues) {
        issues.forEach(function(issue) {
          $j.ajax({
            type: "GET",
            url: jiraBaseURL + "/rest/api/2/issue/" + issue + "?fields=summary,issuetype,status",
            dataType: "jsonp",
            jsonp: "jsonp-callback",
            jsonpCallback: "jbossJiraView.refreshIssuesData",
            crossDomain: "true"
          });
        });
    }

    this.refreshIssues = refreshIssues;
    this.retrieveIssuesFromJIRA = retrieveIssuesFromJIRA;
};

/**
 * jboss.JiraApp.JiraView
 *
 * Handles rendering of results from a 'jira' call into the page
 * by creating <li> elements in the <ul> container specified by the given
 * containerID
 *
 * To use create an instance of MoreLikeThisView and pass in the id of the
 * <ul> element in the page in which elements will be rendered.  Also, an
 * appropriate message for displaying when there are no results found should
 * be provided in the options as "noContentMessage"
 */

/*extern jive $j */

jive.namespace("JiraApp");

jive.JiraApp.JiraView = function(containerID, options) {
    var noContentMessage    = options.noContentMessage,
        errorMessage        = options.errorMessage,
        objectType  = options.objectType,
        objectID    = options.objectID,
        forbiddenMessage = options.forbiddenMessage,
        issueClosed = options.issueClosed,
        containerSelector   = "#" + containerID,
        issuesCount = 0;

    /**
     * refreshIssuesData(data)
     * - data (JSON list): list of JIRA issues
     */
    function refreshIssuesData(data) {
        var fields = data.fields;
        var issueTypeID = fields.issuetype.id;
        var issueTypeIconUrl = fields.issuetype.iconUrl;
		var statusID = fields.status.id;
		var statusIconUrl = fields.status.iconUrl;

        var link = "<a href=\"" + jiraBaseURL + "/browse/" + data.key + "\" title=\"" + fields.summary + "\" target=\"_blank\">";
        if (statusID == 6) {
          link += "<span class=\"jboss-jira-issue-closed\">" + data.key + "</span>";
        } else {
          link += data.key;
        }
        link += "</a>";
        
        var row = "<td width=\"20px\"><span class=\"jira-icon jira-issuetype-" + issueTypeID + "\" title=\"" + fields.issuetype.name + 
          "\" style=\"background-image: url('" + issueTypeIconUrl + "');\"></span></td><td>" + link +
          "</td><td><span class=\"jira-icon jira-status-" + statusID + "\" title=\"" + fields.status.name + "\" style=\"background-image: url('" + statusIconUrl + "');\"></span>&nbsp;" + fields.status.name +
          "</td>";
        <#if !user.anonymous>
        row += "<td align=\"right\" id=\"jboss-jira-remove-parent-" + data.key + "\"><a class=\"jboss-jira-remove-issue\" id=\"jboss-jira-remove-" + data.key + 
               "\" href=\"javascript:void(0)\" title=\"" + options.removeIssueDesc + "\"><span class=\"jive-icon-med jive-icon-delete\" id=\"jboss-jira-remove-icon\"></span></a></td>";
        $j("#issue-" + data.key).empty().append(row);
        if (statusID != 6) { 
          $j("#jboss-jira-remove-" + data.key).bind('click', function() {jbossJiraView.removeIssue(data.key)} );
        } else {
          $j("#jboss-jira-remove-" + data.key).bind('click', function() { alert(issueClosed); } );
        }
        <#else>
        $j("#issue-" + data.key).empty().append(row);
        </#if>

        $j("#issue-" + data.key).fadeIn(fadeAnimationLength);
    }

    function displayError() {
        $j(containerSelector).append("<tr><td>" + errorMessage + "</td></tr>");
    }

    /* 
     * issues - list of JIRA issues.
     * clearWidget - if true then widget is cleared before appending content. if null then it's desided if should be cleared or not.
     */
    function addIssuesIDs(issues) {
        var rows = "";
        if (issues != null) {
            issues.forEach(function(issue) {
                    rows += "<tr id=\"issue-" + issue + "\"><td colspan=\"4\">" + options.loadingJiraIssue + "</td></tr>";
            });
        }
        
        if (issuesCount == 0) {
          $j(containerSelector).empty().append(rows);
        } else {
          $j(containerSelector).append(rows);
        }
        issuesCount += issues.length;
    }
    
    function removeIssue(issue) {
      if (!confirm(options.removeIssueQuestion)) {
       return;
      } 
      $j("#jboss-jira-remove-parent-" + issue).empty();
      issuesCount--;
    
      JiraManager.removeLink(objectType, objectID, issue, {
        callback: function(result) {
          if (result == "OK") {
            $j("#issue-" + issue).fadeOut(fadeAnimationLength,function() { 
              $j(this).remove();
              jbossJiraView.checkIfNoIssues();
            });
          } if (result == "FORBIDDEN") {
            jbossJiraApp.showMessage(forbiddenMessage, "error"); 
          }
        },
        errorHandler:function(errorString, exception) {
            jbossJiraApp.showMessage(errorMessage, "error"); 
        },
        timeout: jiraJBossDWRtimeout
      });
    }
    
    function checkIfNoIssues() {
      if (issuesCount == 0) {
        noRelatedIssues();
      }
    }
    
    function noRelatedIssues() {
      var rows = "<tr><td colspan=\"5\">" + noContentMessage + "</td></tr>";
      $j(containerSelector).empty().append(rows);
    }
    
    function showPicker() {
      addIssueBoxValue = $j("#jboss-jira-add-input").val();
      $j.ajax({
          type: "GET",
          url: jiraBaseURL + "/rest/api/1.0/issues/picker.json?query=" + addIssueBoxValue + "&currentIssueKey=&showSubTasks=true&showSubTaskParent=true&currentProjectId=",
          dataType: "jsonp",
          jsonp: "jsonp-callback",
          jsonpCallback: "jbossJiraView.refreshPicker",
          crossDomain: "true"
      });
    }
    
    function refreshPicker(data) {
      var suggestions = "";
      var someData = false;
      data["sections"].forEach(function(section) {
        if (section["id"] == 'hs') {
          suggestions += "<h5>" + options.sectionHs;
        } else {
          suggestions += "<h5>" + section["label"];
        }
        if (typeof section["sub"] != 'undefined') {
          suggestions += "<span class=\"jboss-jira-add-suggestions-desc\">(" + section["sub"] + ")</span>";
        }
        suggestions += "</h5><ul id=\"" + section["id"] + "\">";

        if (typeof section["issues"] == 'undefined') {
          suggestions += "<li>" + section["msg"] + "</li>" + 
            "<li>" + options.loginToJiraMessage + "</li>";
        } else {
          someData = true;
          section["issues"].forEach(function(issue) {
            suggestions += "<li><a class=\"jboss-jira-suggestion-list-item-link\" id=\"" + issue["key"] + "\" href=\"javascript:void(0)\" style=\"background-image: url(" + jiraBaseURL + issue["img"] + "); text-overflow: ellipsis; overflow-x: hidden; overflow-y: hidden; \" title=\"" + issue["key"] + " - " + issue["summaryText"] + "\">" + issue["keyHtml"] + " - " + issue["summary"] + "</a></li>";
          });
        }

        suggestions += "</ul>";
      });
      
      $j("#jboss-jira-add-suggestions").empty().append(suggestions);

      if (someData) {
        links = $j("#jboss-jira-add-suggestions a");
        links.bind('click', function() { 
          $j("#jboss-jira-add-input").val($j(this).attr("id"));
          $j("#jboss-jira-add-suggestions-box").css("display", "none");
        } );
        links.hover(
          function() { $j(this).parent().addClass("active"); },
          function() { $j(this).parent().removeClass("active"); }
        );
      }
      showPickerBox();
    }
    
    function showPickerBox() {
      pos = $j("#jboss-jira-suggestion-position").offset();
      $j("#jboss-jira-add-suggestions-box").css({"left": pos.left + "px", "top": (pos.top + 5) + "px", "display": "block" });
    }
    
    this.refreshIssuesData = refreshIssuesData;
    this.displayError = displayError;
    this.addIssuesIDs = addIssuesIDs;
    this.removeIssue = removeIssue;
    this.noRelatedIssues = noRelatedIssues;
    this.checkIfNoIssues = checkIfNoIssues;
    this.showPicker = showPicker;
    this.refreshPicker = refreshPicker;
    this.showPickerBox = showPickerBox;
};

/**
 * jboss.JiraApp.Main
 *
 * JavaScript code to handle Related Issues
 *
 * This is the main entry point of the JiraApp.  This class acts as a
 * controller.
 *
 */

/*extern jive $j jiveToggleTab */

jive.namespace("JiraApp");

jive.JiraApp.Main = function(options) {
    var objectType  = options.objectType,
        objectID    = options.objectID,
        containerID = options.containerID;

    function renderResults() {
        jbossJiraSource.refreshIssues(objectType, objectID);
    }
    
    function addLinkClicked() {
      hideMessage("error"); 
      
      $j("#jboss-jira-add-link-group").fadeOut(fadeAnimationLength / 2, function() {
        $j("#jboss-jira-add-form-group").fadeIn(fadeAnimationLength / 2);
        $j("#jboss-jira-add-input").focus();
      });
    }
    /** 
     * Show message.
     * - kind - can be "error", "info", "warning"
     */
    function showMessage(text, kind) {
      $j("#jboss-jira-message-" + kind).empty().append(text);
      $j("#jboss-jira-message-" + kind).fadeIn(fadeAnimationLength);
    }
    function hideMessage(kind, callback) {
      $j("#jboss-jira-message-" + kind).fadeOut(fadeAnimationLength, callback);
    }
    
    function addIssueFired() {
        var issue = $j("#jboss-jira-add-input").val();
        var errorText = '';
        if (issue == '') {
          errorText = options.noText;
        } else if (document.getElementById("issue-" + issue.toUpperCase()) != null) {
          errorText = options.issueAlreadyLinked;
        }

        if (errorText != '') {
          showMessage(errorText, "error");
          $j("#jboss-jira-add-input").focus();
          $j("#jboss-jira-add-suggestions-box").fadeOut(fadeAnimationLength / 2);
          return;
        }

        JiraManager.addLink(objectType, objectID, issue, {
            callback: function(result) {
              if (result == "OK") {
                showMessage("", "error");

                issue = $j("#jboss-jira-add-input").val().toUpperCase();
                issues = new Array(issue);
                jbossJiraView.addIssuesIDs(issues, null);
                jbossJiraSource.retrieveIssuesFromJIRA(issues);

                resetAddForm();
              } else {
                if (result == "ISSUE_NOT_FOUND") {
                  showMessage(options.issueNotFound, "error");
                }
                if (result == "ISSUE_CLOSED") {
                  showMessage(options.issueClosed, "error");
                }
                if (result == "FORBIDDEN") { 
                  showMessage(options.forbiddenMessage, "error");
                }
                $j("#jboss-jira-add-suggestions-box").fadeOut(fadeAnimationLength / 2);
              }
            },
            errorHandler:function(errorString, exception) {
              showMessage(options.errorMessage, "error"); 
            },
            timeout: jiraJBossDWRtimeout
        });
    }
    
    function resetAddForm() {
        hideMessage("error");
        addIssueBoxValue = "";
        $j("#jboss-jira-add-input").val(addIssueBoxValue);
        $j("#jboss-jira-add-form-group").fadeOut(fadeAnimationLength / 2, function() { $j("#jboss-jira-add-link-group").fadeIn(fadeAnimationLength / 2); } );
        $j("#jboss-jira-add-suggestions-box").fadeOut(fadeAnimationLength / 2);
    }
    
    /* *** Initialization *** */
    jbossJiraView = new jive.JiraApp.JiraView(containerID,
        { noContentMessage: options.noContentMessage, errorMessage: options.errorMessage, issueClosed: options.issueClosed,
          objectType: objectType, objectID: objectID, forbiddenMessage: options.forbiddenMessage, 
          loginToJiraMessage: options.loginToJiraMessage, removeIssueQuestion: options.removeIssueQuestion, 
          removeIssueDesc: options.removeIssueDesc, loadingJiraIssue: options.loadingJiraIssue,
          sectionHs: options.sectionHs });

    jbossJiraSource = new jive.JiraApp.JiraSource();

    $j(document).ready(function() {
        renderResults();
        <#if !user.anonymous>
        $j("#jboss-jira-add-link").bind('click', jbossJiraApp.addLinkClicked );
        $j("#jboss-jira-ok-link").bind('click', jbossJiraApp.addIssueFired );
        $j("#jboss-jira-add-input").keyup(function(e) {
          if(e.which == 13) {
            jbossJiraApp.addIssueFired();
          } else {
            if ($j("#jboss-jira-add-input").val() != addIssueBoxValue) {
              clearTimeout($j.data(this, "jbossJiraAddTimer"));
              var ms = 200; //milliseconds
              var wait = setTimeout(function() {
                jbossJiraView.showPicker();
              }, ms);
              $j.data(this, "jbossJiraAddTimer", wait);
            }
          }
        });
        $j("#jboss-jira-cancel-link").bind('click', jbossJiraApp.resetAddForm );

        suggestionBox = "<div id=\"jboss-jira-add-suggestions-box\" class=\"jboss-jira-add-suggestions-box" + options.containerType + "\" style=\"display: none; overflow-x: hidden; position: absolute; top: 0px; left: 0px; height: auto; width: auto; \">"
         + "<div id=\"jboss-jira-add-suggestions\" tabindex=\"-1\" style=\"white-space: nowrap; width: auto; display: block; \">"
         + "</div></div>";

        $j("body").append(suggestionBox);
        </#if>
    });
    this.addLinkClicked = addLinkClicked;
    this.addIssueFired = addIssueFired;
    this.resetAddForm = resetAddForm;
    this.showMessage = showMessage;
    this.hideMessage = hideMessage;
};

</@resource.javascript>

    <div class="j-box" id="jboss-jive-jira-widget">
		<header>
			<h4><@s.text name="plugin.jira.widget.name" /></h4>
		</header>
		<div id="jboss-sidebar-body-jira" class="j-box-body">
			<table id="jboss-jira-${objectType?c}-${objectID?c}" id="jboss-jira-result-table" cellpadding="1px" border="0" width="100%">
				<tr><td><@s.text name="plugin.jira.widget.retrieve_data.text" /></td></tr>
			</table>
			<div id="jboss-jira-messages">
			  <div id="jboss-jira-message-error" style="display: none;"></div>
			  <div id="jboss-jira-message-info" style="display: none;"></div>
			  <div id="jboss-jira-message-warning" style="display: none;"></div>
			</div>
			<#if !user.anonymous>
			<div id="jboss-jira-add-link-row">
			  <div id="jboss-jira-add-link-group">
			  <a id="jboss-jira-add-link" href="javascript:void(0)"><@s.text name="plugin.jira.widget.add_issue" /></a>
			  </div>

			  <div id="jboss-jira-add-form-group" style="display: none;">
			  <input type="text" id="jboss-jira-add-input"/>
			  <a id="jboss-jira-ok-link" href="javascript:void(0)"><@s.text name="plugin.jira.widget.ok" /></a>&nbsp;
			  <a id="jboss-jira-cancel-link" href="javascript:void(0)"><@s.text name="plugin.jira.widget.cancel" /></a>
			  </div>
			</div>
			<div id="jboss-jira-suggestion-position"></div>
			</#if>
		</div>
	</div>
<@resource.javascript>
jbossJiraApp = new jive.JiraApp.Main({
            objectType: ${objectType?c},
            objectID: ${objectID?c},
            containerID: "jboss-jira-${objectType?c}-${objectID?c}",
            containerType: "${containerType}",
            noContentMessage: "<@s.text name="plugin.jira.widget.no_content.text" />",
            loadingJiraIssue: "<@s.text name="plugin.jira.widget.retrieve_issue.text" />",
            errorMessage: "<@s.text name="plugin.jira.widget.error" />",
            forbiddenMessage: "<@s.text name="plugin.jira.widget.forbidden.text" />",
            noText: "<@s.text name="plugin.jira.widget.notext.text" />",
            sectionHs: "<@s.text name="plugin.jira.widget.suggestions.section.hs" />",
            issueAlreadyLinked: "<@s.text name="plugin.jira.widget.issueAlreadyLinked.text" />",
            issueNotFound: "<@s.text name="plugin.jira.widget.issueNotFound.text" />",
            issueClosed: "<@s.text name="plugin.jira.widget.issueClosed.text" />",
            loginToJiraMessage: '<@s.text name="plugin.jira.widget.loginToJira.text" />',
            removeIssueQuestion: '<@s.text name="plugin.jira.widget.removeIssueConfirmation.text" />',
            removeIssueDesc: '<@s.text name="plugin.jira.widget.removeIssueDescription.text" />'
        });
</@resource.javascript>
</div>
<!-- END sidebar box 'JIRA' -->

</#macro>