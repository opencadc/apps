<!--
************************************************************************
*******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
**************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
*
*  (c) 2009.                            (c) 2009.
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
<!DOCTYPE html>

<%@ taglib uri="WEB-INF/c.tld" prefix="c"%>

<%@ page import="ca.nrc.cadc.dlm.server.ServerUtil" %>
<%@ page import="ca.nrc.cadc.dlm.server.DispatcherServlet" %>
<%@ page import="ca.nrc.cadc.dlm.server.UrlListServlet" %>
<%
    String uris = (String) request.getAttribute("uris");
    String params = (String) request.getAttribute("params");
%>

<%

String skin = "http://localhost/en/";
String cadcHeader = skin + "_page_header.html";
String cadcFooter = skin + "_page_footer.html";
String cadcBanner = skin + "_cadc_banner.html";
String cadcSiteMenu = skin + "_cadc_site_menu.html";
%>

<c:catch><c:import url="<%= cadcHeader %>" /></c:catch>
<body>
<!-- Always include wb-body at the top, even though it's not ended until the included footer. -->
  <div id="wb-body">

    <!-- Accessibility to allow skipping to the content. -->
    <div id="wb-skip">
			<ul id="wb-tphp">
        <li id="wb-skip1"><a href="#wb-cont">Skip to main content</a></li>
        <li id="wb-skip2"><a href="#wb-nav">Skip to secondary menu</a></li>
      </ul>
    </div>
  
    <div id="wb-head">
      <div id="wb-head-in">
        <header>
          <!-- HeaderStart -->

          <c:catch><c:import url="<%= cadcBanner %>" /></c:catch>

          <nav role="navigation">
            <span lang="fr" class="lang-link-target">/fr</span>
            <c:catch><c:import url="<%= cadcSiteMenu %>" /></c:catch>

            <div id="gcwu-bc">
              <h2>Breadcrumb trail</h2>
              <div id="gcwu-bc-in">
                <ol>
                  <li><a href="/en">CADC Home</a></li>
				  				<li>DownloadManager</li>
                </ol>
              </div>
            </div>
          </nav>
          <!-- HeaderEnd -->
        </header>
      </div>
    </div>

    <div id="wb-core">
      <div id="wb-core-in" class="equalize">
        <div id="wb-main" role="main">
          <div id="wb-main-in">
            <!-- MainContentStart -->

            <!--MAIN CONTENT begins here-->
            
            <h1 id="wb-cont" class="wb-invisible">Download Manager</h1>
            <p><b>Choose a download method:</b></p>
            
            <form action="/downloadManager/download" method="POST">
            
            	<input type="hidden" name="uris" value="<%= uris %>" />
							<input type="hidden" name="params" value="<%= params %>" />
							<input type="hidden" name="skin" value="<%= skin %>" />
							
            	<div class="grid-12">
            		
								<div class="span-2">
									<input class="button" type="submit" name="method" value="<%= DispatcherServlet.WEBSTART %>" />
								</div>
								<div class="span-6">
									<jsp:include page='javaWebStartDescription.html' flush='true' />
								</div>
								<div class="span-4"></div>
								<div class="clear"></div>
								
								<div class="span-2">
									<input class="button" type="submit" name="method" value="<%= DispatcherServlet.URLS %>" />
								</div>
								<div class="span-6">
									<jsp:include page='urlListDescription.html' flush='true' />
								</div>
								<div class="span-4"></div>
								<div class="clear"></div>
								
								<div class="span-2">
									<input class="button" type="submit" name="method" value="<%= DispatcherServlet.HTMLLIST %>" />
								</div>
								<div class="span-6">
									<p>View the list of files in a Web page and click on individual files to download.</p>
								</div>
								<div class="span-4"></div>
								<div class="clear"></div>
								
								<label class="form-checkbox" for="remember">
									<input type="checkbox" id="remember" name="remember" value="true">
									Remember my choice of download method (cookies required)
								</label>
									
								<div class="span-1"></div>
								<div class="span-10">
									<p class="color-attention">
										Individual download pages have "Chose one of the other download methods" buttons which, if pressed, remove the remembered download choice and return to the multiple choice page.
									</p>
								</div>
								<div class="span-1"></div>
								<div class="clear"></div>
								
							</div>
            	<div class="clear"></div>								
						</form>
            
						<h2>Help</h2>
	
						<h3>I want to use the Java option but it didn't work. How can I fix it?</h3>
						<p>
						For general help on getting applets or webstart working, we 
						have a <a href="/JavaTest">Java Test Page</a> with instructions.
						</p>
						<h3><i>wget</i> is not working</h3>
						<p>
						The recommended usage above includes the <code>--content-disposition</code> option,
						which is available in <i>wget</i> version 1.12 or later. This option improves the
						likelhood that saved files will have the correct filenames when retrieved from services.
						</p>
						<p>
						Please note that there are many versions of <i>wget</i> with a variety of
						options and syntax.  Please consult your local help pages before contacting
						us.&nbsp;   <code>wget --help</code> should reveal the arguments supported by
						your version of <i>wget</i>.
						</p>
			
						<p> 
						The <i>wget</i> command should be available on most systems. If not, <i>wget</i>
						can be downloaded from <a href="http://www.gnu.org/software/wget/">gnu.org</a>.
						Alternately, you can try one of the several other web download utilities
						such as: curl, HTTrack, leech (mozilla add-on), pavuk, lftp, etc.
						</p>    
						<h3>common options used with <i>wget</i></h3>
						<p>
						For downloading large number of files with <i>wget</i>, the following options might come handy:
						</p>
						<ul>
							<li>-t,  --tries=NUMBER            set number of retries to NUMBER (5 recommended).</li>
							<li>--auth-no-challenge     send Basic HTTP authentication information
								 without first waiting for the server's challenge thus saving a roundtrip.</li>
							<li>--waitretry=SECONDS       wait 1..SECONDS between retries of a retrieval. By default, Wget will assume a value of 10 seconds.</li>
							<li>-N,  --timestamping  Turn on time-stamping and download only missing or updated files.</li>
						</ul>
		    		
		    		<dl id="gcwu-date-mod" role="contentinfo">
        			<dt>Date modified:</dt><dd><span><time>2013-08-01</time></span></dd>
      			</dl>
      			<!-- MainContentEnd -->
          </div>
        </div>
        <!-- noindex -->
      </div>
    </div>
	<c:catch><c:import url="<%= cadcFooter%>" /></c:catch>
