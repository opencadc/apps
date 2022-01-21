/*
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
*/


package ca.nrc.cadc.dlm.server;

import ca.nrc.cadc.auth.AuthenticationUtil;
import ca.nrc.cadc.auth.SSOCookieCredential;
import ca.nrc.cadc.dlm.DownloadRequest;
import ca.nrc.cadc.net.NetUtil;
import ca.nrc.cadc.reg.client.RegistryClient;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import javax.security.auth.Subject;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Download pre-processor for Java WebStart download method.
 *
 * @author adriand
 */
public class JavaWebStartServlet extends HttpServlet {
    private static final long serialVersionUID = 202007201400L;

    private static final Logger log = Logger.getLogger(JavaWebStartServlet.class);


    /**
     * @param config - webstart configuration
     * @throws javax.servlet.ServletException - if parent init fails
     */
    public void init(ServletConfig config)
        throws ServletException {
        super.init(config);
        log.setLevel(Level.DEBUG);
    }

    /**
     * Handle POSTed download request directed to the Java WebStart download method.
     *
     * @param request  The HTTP Request.
     * @param response The HTTP Response.
     * @throws javax.servlet.ServletException For general Servlet exceptions
     * @throws java.io.IOException            For any I/O related errors.
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        // encode & in the param list for safe use in XML
        //String params = (String) request.getAttribute("params");
        //if (params != null) {
        //    params = params.replaceAll("&", "&amp;");
        //    request.setAttribute("params", params);
        //}

        Subject subject = AuthenticationUtil.getCurrentSubject();
        // Nothing added in if user is anon
        if (subject != null) {
            Set<SSOCookieCredential> creds = subject.getPublicCredentials(SSOCookieCredential.class);
            if (!creds.isEmpty()) {
                // if it is empty, there's possibly a different issue?

                Iterator<SSOCookieCredential> credIter = creds.iterator();

                SSOCookieCredential cred = credIter.next();
                // these are only really needed by the webstart servlet/jsp since server-side
                // will use the credential from the subject directly
                String ck = cred.getSsoCookieValue();
                ck = ck.replace("&", "&amp;");
                request.setAttribute("ssocookie", ck);
                log.debug("ssocookie attribute: " + ck);

                String ssodomains = cred.getDomain();
                // build comma-delimited ssocookie domain list
                while (credIter.hasNext()) {
                    cred = credIter.next();
                    ssodomains = ssodomains + "," + cred.getDomain();
                }

                request.setAttribute("ssocookiedomain", ssodomains);
                log.debug("ssocookiedomain attribute: " + ssodomains);
            }

        }

        // codebase for applet and webstart deployments
        String codebase = ServerUtil.getCodebase(request);
        request.setAttribute("codebase", codebase);
        log.debug("codebase attribute: " + codebase);

        setRegistryClientProps(request);

        RequestDispatcher disp = request.getRequestDispatcher("DownloadManager.jsp");
        disp.forward(request, response);
    }

    private void setRegistryClientProps(HttpServletRequest request) {
        String local = System.getProperty(RegistryClient.class.getName() + ".local");
        String host = System.getProperty(RegistryClient.class.getName() + ".host");
        if ("true".equals(local)) {
            host = NetUtil.getServerName(JavaWebStartServlet.class);
        }
        if (host != null) {
            request.setAttribute("targetHost", host);
        }
    }


}
