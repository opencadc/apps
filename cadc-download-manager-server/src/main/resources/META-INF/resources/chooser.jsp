<!--
************************************************************************
*******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
**************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
*
*  (c) 2009, 2020.                      (c) 2009, 2020.
*  Government of Canada                 Gouvernement du Canada
*  National Research Council            Conseil national de recherches
*  Ottawa, Canada, K1A 0R6              Ottawa, Canada, K1A 0R6
*  All rights reserved                  Tous droits réservés
*
*  NRC disclaims any warranties,        Le CNRC dénie toute garantie
*  expressed, implied, or               énoncée, implicite ou légale,
*  statutory, of any kind with          de quelque nature que ce
*  respect to the software,             soit, concernant le logiciel,
*  including without limitation         y compris sans restriction
*  any warranty of merchantability      toute garantie de valeur
*  or fitness for a particular          marchande ou de pertinence
*  purpose. NRC shall not be            pour un usage particulier.
*  liable in any event for any          Le CNRC ne pourra en aucun cas
*  damages, whether direct or           être tenu responsable de tout
*  indirect, special or general,        dommage, direct ou indirect,
*  consequential or incidental,         particulier ou général,
*  arising from the use of the          accessoire ou fortuit, résultant
*  software.  Neither the name          de l'utilisation du logiciel. Ni
*  of the National Research             le nom du Conseil National de
*  Council of Canada nor the            Recherches du Canada ni les noms
*  names of its contributors may        de ses  participants ne peuvent
*  be used to endorse or promote        être utilisés pour approuver ou
*  products derived from this           promouvoir les produits dérivés
*  software without specific prior      de ce logiciel sans autorisation
*  written permission.                  préalable et particulière
*                                       par écrit.
*
*  This file is part of the             Ce fichier fait partie du projet
*  OpenCADC project.                    OpenCADC.
*
*  OpenCADC is free software:           OpenCADC est un logiciel libre ;
*  you can redistribute it and/or       vous pouvez le redistribuer ou le
*  modify it under the terms of         modifier suivant les termes de
*  the GNU Affero General Public        la “GNU Affero General Public
*  License as published by the          License” telle que publiée
*  Free Software Foundation,            par la Free Software Foundation
*  either version 3 of the              : soit la version 3 de cette
*  License, or (at your option)         licence, soit (à votre gré)
*  any later version.                   toute version ultérieure.
*
*  OpenCADC is distributed in the       OpenCADC est distribué
*  hope that it will be useful,         dans l’espoir qu’il vous
*  but WITHOUT ANY WARRANTY;            sera utile, mais SANS AUCUNE
*  without even the implied             GARANTIE : sans même la garantie
*  warranty of MERCHANTABILITY          implicite de COMMERCIALISABILITÉ
*  or FITNESS FOR A PARTICULAR          ni d’ADÉQUATION À UN OBJECTIF
*  PURPOSE.  See the GNU Affero         PARTICULIER. Consultez la Licence
*  General Public License for           Générale Publique GNU Affero
*  more details.                        pour plus de détails.
*
*  You should have received             Vous devriez avoir reçu une
*  a copy of the GNU Affero             copie de la Licence Générale
*  General Public License along         Publique GNU Affero avec
*  with OpenCADC.  If not, see          OpenCADC ; si ce n’est
*  <http://www.gnu.org/licenses/>.      pas le cas, consultez :
*                                       <http://www.gnu.org/licenses/>.
*
*  $Revision: 4 $
*
************************************************************************
-->

<%@ page language="java" %>
<%@ taglib uri="WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="WEB-INF/fmt.tld" prefix="fmt" %>

<%@ page import="ca.nrc.cadc.config.ApplicationConfiguration" %>
<%@ page import="ca.nrc.cadc.dlm.server.DispatcherServlet" %>
<%@ page import="ca.nrc.cadc.dlm.server.SkinUtil" %>
<%@ page import="java.net.URI" %>
<%@ page import="java.util.List" %>
<%@ page import="ca.nrc.cadc.util.StringUtil" %>
<%@ page import="ca.nrc.cadc.dlm.DownloadTuple" %>

<%
    ApplicationConfiguration configuration = new ApplicationConfiguration(DispatcherServlet.DEFAULT_CONFIG_FILE_PATH);
    boolean enableWebstart = configuration.lookupBoolean("org.opencadc.dlm.webstart.enable", true);
    List<DownloadTuple> tupleList = (List<DownloadTuple>) request.getAttribute("tupleList");

    String params = (String) request.getAttribute("params");

    String requestHeaderLang = request.getHeader("Content-Language");
    if (requestHeaderLang == null) {
        requestHeaderLang = "en";
    }

    // If calling program has provided values they should be here
    String headerURL = SkinUtil.headerURL;
    String footerURL = SkinUtil.footerURL;
    String bodyHeaderURL = "";
    String skinURL = SkinUtil.skinURL;

    if (!StringUtil.hasLength(headerURL)) {
        if (!StringUtil.hasLength(skinURL)) {
            skinURL = "https://localhost/cadc/skin/";
        }

        if (!skinURL.endsWith("/")) {
            skinURL += "/";
        }

        if (!(skinURL.startsWith("http://") || skinURL.startsWith("https://"))) {
            if (!skinURL.startsWith("/")) {
                skinURL = "/" + skinURL;
            }
            skinURL = "https://localhost" + skinURL;
        }

        headerURL = skinURL + "htmlHead";
        bodyHeaderURL = skinURL + "bodyHeader";
        footerURL = skinURL + "bodyFooter";
    }
%>


<%-- Request scope variables so they can be seen in the imported JSPs --%>
<fmt:setLocale value="<%= requestHeaderLang %>" scope="request"/>
<fmt:setBundle basename="ca.nrc.cadc.downloadManager.downloadManagerBundle"
var="langBundle" scope="request"/>


<% if (StringUtil.hasLength(skinURL)) {
%>
<head>
<% }
%>
<c:catch><c:import url="<%= headerURL %>" /></c:catch>

<% if (StringUtil.hasLength(skinURL)) {
%>
</head>
<% }
%>

<body>
<%= headerURL %>
<%= footerURL %>

<% if (StringUtil.hasLength(bodyHeaderURL)) {
%>
<c:catch><c:import url="<%= bodyHeaderURL %>" /></c:catch>
<% }
%>

    <h1 id="wb-cont" class="wb-invisible"><fmt:message key="TITLE" bundle="${langBundle}"/></h1>

    <h2><fmt:message key="PAGE_HEADER" bundle="${langBundle}"/></h2>
    <br/>

    <form action="<fmt:message key="DOWNLOAD_LINK" bundle="${langBundle}"/>" method="POST">

<%      for (DownloadTuple tuple: tupleList) {
            String tupleStr = tuple.toInternalFormat();
%>
        <span><%= tupleStr %></span>
        <input type="hidden" name="tuple" value="<%= tupleStr %>" />
<%
        }
%>

        <input type="hidden" name="params" value="<%= params %>" />

        <div class="grid-12">
            <c:if test="<%=enableWebstart%>" >
                <div class="span-4">
                    <button class="button font-medium" name="method" value="<%= DispatcherServlet.WEBSTART %>" type="submit"><%= DispatcherServlet.WEBSTART %></button>
                </div>
                <div class="span-6">
                    <fmt:message key="JAVA_WEB_START_DESCRIPTION" bundle="${langBundle}"/>
                </div>
                <div class="span-2"></div>
                <div class="clear"></div>
                <br/>
            </c:if>
            <div class="span-4">
                <button class="button font-medium" name="method" value="<%= DispatcherServlet.URLS %>" type="submit"><fmt:message key="URL_LIST_BUTTON" bundle="${langBundle}"/></button>
            </div>
            <div class="span-6">
                <fmt:message key="URL_LIST_DESCRIPTION" bundle="${langBundle}"/>
            </div>
            <div class="span-2"></div>
            <div class="clear"></div>
            <br/>
            <div class="span-4">
                <button class="button font-medium" name="method" value="<%= DispatcherServlet.HTMLLIST %>" type="submit"><fmt:message key="HTML_LIST_BUTTON" bundle="${langBundle}"/></button>
            </div>
            <div class="span-6">
                <fmt:message key="HTML_LIST_DESCRIPTION" bundle="${langBundle}"/>
            </div>
            <div class="span-2"></div>
            <div class="clear"></div>
            <br/>

            <label class="form-checkbox" for="remember">
                <input type="checkbox" id="remember" name="remember" value="true"/>
                <fmt:message key="REMEMBER_CHECKBOX_LABEL" bundle="${langBundle}"/>
                <fmt:message key="REMEMBER_CHECKBOX_TEXT" bundle="${langBundle}"/>
            </label>

            <div class="span-1"></div>
            <div class="span-10">
                <p class="color-attention">
                    <fmt:message key="REMEMBER_TEXT" bundle="${langBundle}"/>
                </p>
            </div>
            <div class="span-1"></div>
            <div class="clear"></div>

        </div>
        <div class="clear"></div>
    </form>

    <h2><fmt:message key="HELP_HEADER" bundle="${langBundle}"/></h2>
    <h3>
    <i>wget </i><fmt:message key="HELP_WGET_NOT_WORKING_HEADER" bundle="${langBundle}"/>
    </h3>
    <fmt:message key="HELP_WGET_NOT_WORKING_TEXT" bundle="${langBundle}"/>
    <h3>
    <fmt:message key="HELP_WGET_OPTIONS_HEADER" bundle="${langBundle}"/><i> wget</i>
    </h3>
    <p></p>
    <fmt:message key="HELP_WGET_OPTIONS_TEXT" bundle="${langBundle}"/>

    <dl id="gcwu-date-mod" role="contentinfo">
        <dt><fmt:message key="DATE_MODIFIED_LABEL" bundle="${langBundle}"/>:</dt>
        <dd>
            <span>
                <time>$LastChangedDate$</time>
            </span>
        </dd>
    </dl>

    <c:catch><c:import url="<%= footerURL %>" /></c:catch>
</body>
</html>

