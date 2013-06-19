<html>
    <head>
        <#assign pageTitle=action.getText('plugin.jira.admin.name') />
        <title>${pageTitle}</title>
        <content tag="pagetitle">${pageTitle}</content>
        <content tag="pageID">system-jira-admin</content>
    </head>
    <body>
    <#include "/template/global/include/form-message.ftl" />

    <@s.form theme="simple" action="jira-integration-fullreindex">
      <table>
        <tr>
          <td><@s.text name="plugin.jira.admin.reindex.name"/>:</td>
          <td><@s.submit value="${action.getText('plugin.jira.admin.reindex.submit')}"/></td>
        </tr>
      </table>
    </@s.form>
    </body>
</html>