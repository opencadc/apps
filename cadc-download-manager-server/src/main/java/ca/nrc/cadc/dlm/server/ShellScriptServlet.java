/*
 ************************************************************************
 *******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
 **************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
 *
 *  (c) 2023.                            (c) 2023.
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

import ca.nrc.cadc.ac.server.oidc.OIDCUtil;
import ca.nrc.cadc.auth.AuthMethod;
import ca.nrc.cadc.auth.AuthenticationUtil;
import ca.nrc.cadc.cred.client.CredUtil;
import ca.nrc.cadc.date.DateUtil;
import ca.nrc.cadc.dlm.DownloadDescriptor;
import ca.nrc.cadc.dlm.DownloadRequest;
import ca.nrc.cadc.dlm.DownloadUtil;
import ca.nrc.cadc.net.HttpGet;
import ca.nrc.cadc.reg.Standards;
import ca.nrc.cadc.reg.client.LocalAuthority;
import ca.nrc.cadc.reg.client.RegistryClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.security.AccessControlException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import javax.security.auth.Subject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Servlet to handle requests for a Shell Script.
 *
 * @author jenkinsd
 */
public class ShellScriptServlet extends HttpServlet {
    private static final long serialVersionUID = 202303151015L;
    public static final String SCRIPT_TARGET = "/shellScript";

    private static final DateFormat DATE_FORMAT = DateUtil.getDateFormat("yyyyMMddHHmmss", DateUtil.UTC);

    /**
     * Handle POSTed download request from an external page.
     *
     * @param request  The HTTP Request
     * @param response The HTTP Response
     * @throws IOException if stream processing fails
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        final String filename = String.format("cadc-download-%s.sh", DATE_FORMAT.format(new Date()));
        response.setContentType("text/x-shellscript");
        response.setHeader("content-disposition", "attachment;filename=\"" + filename + "\"");

        final DownloadRequest downloadReq = (DownloadRequest) request.getAttribute("downloadRequest");
        downloadReq.runID = (String) request.getAttribute("runid");

        final Iterator<DownloadDescriptor> downloadDescriptorIterator = DownloadUtil.iterateURLs(downloadReq);
        final Subject currentSubject = AuthenticationUtil.getSubject(request, false);
        final ScriptGenerator scriptGenerator;

        try {
            if (CredUtil.checkCredentials(currentSubject)) {
                final String authToken = getToken(currentSubject);
                final Calendar calendar = Calendar.getInstance(DateUtil.UTC);

                // Expiry is current time plus the set expiry minutes.
                calendar.add(Calendar.MINUTE, OIDCUtil.ID_TOKEN_EXPIRY_MINUTES);
                scriptGenerator = new ScriptGenerator(downloadDescriptorIterator, authToken, calendar.getTime());
            } else {
                scriptGenerator = new ScriptGenerator(downloadDescriptorIterator);
            }

            final Writer writer = response.getWriter();
            scriptGenerator.generate(writer);
            writer.flush();
        } catch (CertificateExpiredException | CertificateNotYetValidException certificateException) {
            throw new AccessControlException(certificateException.getMessage());
        } catch (IOException ioException) {
            throw ioException;
        } catch (Exception exception) {
            throw new ServletException(exception.getMessage(), exception);
        }
    }

    private String getToken(final Subject subject) throws Exception {
        final LocalAuthority localAuthority = new LocalAuthority();
        final URI oAuthServiceURI = localAuthority.getServiceURI(Standards.SECURITY_METHOD_OAUTH.toString());
        final RegistryClient registryClient = new RegistryClient();
        final URL baseOAuthServiceURL = registryClient.getServiceURL(oAuthServiceURI, Standards.SECURITY_METHOD_OAUTH,
                                                                     AuthMethod.COOKIE);
        final URL oAuthServiceURL = new URL(baseOAuthServiceURL.toString() + "?response_type=token");

        try {
            return Subject.doAs(subject, (PrivilegedExceptionAction<String>) () -> {
                final HttpGet get = new HttpGet(oAuthServiceURL, true);
                get.run();

                final BufferedReader reader = new BufferedReader(new InputStreamReader(get.getInputStream()));
                return reader.readLine();
            });
        } catch (PrivilegedActionException privilegedActionException) {
            throw privilegedActionException.getException();
        }
    }
}
