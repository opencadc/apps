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


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
        "http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib uri="WEB-INF/c.tld" prefix="c"%>

<%@ page import="ca.nrc.cadc.dlm.DownloadTuple" %>
<%@ page import="ca.nrc.cadc.util.StringUtil" %>
<%@ page import="ca.nrc.cadc.dlm.server.SkinUtil" %>
<%@ page import="ca.nrc.cadc.dlm.DownloadRequest" %>
<%@ page import="ca.nrc.cadc.dlm.server.DispatcherServlet" %>
<%@ page import="java.util.Set" %>
<%@ page import="ca.nrc.cadc.dlm.DownloadTupleFormat" %>
<%
    DownloadRequest downloadReq = (DownloadRequest)request.getAttribute(DispatcherServlet.INTERNAL_FORWARD_PARAMETER);
    Set<DownloadTuple> tupleList = downloadReq.getTuples();
    DownloadTupleFormat df = new DownloadTupleFormat();
%>

<%
    // If calling program has provided values they should be here
    String headerURL = SkinUtil.headerURL;
    String footerURL = SkinUtil.footerURL;
    String bodyHeaderURL = "";
    String skinURL = SkinUtil.skinURL;

    if (!StringUtil.hasLength(headerURL)) {
        if (!StringUtil.hasLength(skinURL)) {
            skinURL = "http://localhost/cadc/skin/";
        }

        if (!skinURL.endsWith("/")) {
            skinURL += "/";
        }

        if (!(skinURL.startsWith("http://") || skinURL.startsWith("https://"))) {
            if (!skinURL.startsWith("/")) {
                skinURL = "/" + skinURL;
            }
            skinURL = "http://localhost" + skinURL;
        }

        headerURL = skinURL + "htmlHead";
        bodyHeaderURL = skinURL + "bodyHeader";
        footerURL = skinURL + "bodyFooter";
    }
%>

<html>
<head>
    <c:catch><c:import url="<%= headerURL %>" /></c:catch>
</head>

<body onResize="resize()">
    
    <c:catch><c:import url="<%= bodyHeaderURL %>" /></c:catch>
    
    <script language="JavaScript">
    function resize() {
        if (navigator.appName.indexOf("Microsoft") != -1) 
        {
            width = document.body.clientWidth;
            height = document.body.clientHeight;
        }
        else 
        {
            width = window.innerWidth;
            height = window.innerHeight;
        }
        hfHeight = 120;
        document.DownloadManager.width = width - 60;
        document.DownloadManager.height = height - 60 - hfHeight;
    }
    window.onResize = resize;
    </script>   
    
    <p>
        <center>
            <c:import url="appletTag.jsp" />
        </center>
    </p>
    
<div style="padding-left: 2em; padding-right: 2em">
    <form action="/downloadManager/download" method="POST">
        <%      for (DownloadTuple tuple: tupleList) {
            String tupleStr = df.format(tuple);
        %>
        <input type="hidden" name="tuple" value="${tupleStr}" />
        <%
            }
        %>
        <%     if ( downloadReq.runID != null ) {
        %>
        <input type="hidden" name="runid" value="<%= downloadReq.runID %>" />
        <%      }
        %>
        <%     if ( skinURL != null ) {
        %>
        <input type="hidden" name="skin" value="<%= skinURL %>" />
        <%      }
        %>

        <input type="submit" name="method" value="Chose one of the other download methods" />
    </form>
</div

<c:catch><c:import url="<%= footerURL%>" /></c:catch>
</body>
</html>