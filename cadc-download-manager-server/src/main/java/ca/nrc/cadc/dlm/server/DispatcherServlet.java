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
import ca.nrc.cadc.dlm.DownloadRequest;
import ca.nrc.cadc.dlm.DownloadTuple;
import ca.nrc.cadc.log.ServletLogInfo;
import java.io.IOException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.List;
import java.util.Set;
import javax.security.auth.Subject;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;


/**
 * <p>Download pre-processor. This servlet accepts either direct POSTs from clients or
 * request scope attributes and passes the request on to the appropriate JSP page.
 * </p>
 * <p>
 * For direct POST, the client must use the multi-valued <em>uri</em> parameter to pass
 * in one or more URIs. These are flattened into a single comma-separated list and
 * passed along as attributes. The client may also set the single-valued <em>fragment</em>
 * parameter; the fragment is appended to each URI before SchemeHandler(s) are used to
 * convert them to URLs.
 * </p>
 * <p>
 * When forwarding from another servlet
 * </p>
 *
 * @author pdowler
 */
public class DispatcherServlet extends HttpServlet {
    private static final long serialVersionUID = 202008040800L;

    private static final Logger log = Logger.getLogger(DispatcherServlet.class);
    public static String INTERNAL_FORWARD_PARAMETER = "downloadRequest";
    public static String URLS = "URL List";
    public static String HTMLLIST = "HTML List";
    public static String WEBSTART = "Java Webstart";
    public static String SHELL_SCRIPT = "Shell Script";
    public static String TAR_PACKAGE = "TAR";
    public static String ZIP_PACKAGE = "ZIP";


    /// Used during JSP compilation
    public static String DEFAULT_CONFIG_FILE_PATH = System.getProperty("user.home") + "/config/org.opencadc.dlm-server.properties";

    /**
     * Checks cookie and request param for download method preference; tries to set a cookie
     * to save setting for future use.
     *
     * @param request  The HTTP Request.
     * @return name of page to forward to, null if caller should offer choices to user
     */
    public static String getDownloadMethod(HttpServletRequest request) {
        String method = request.getParameter(DLMInputHandler.PARAM_METHOD);
        final String target;

        if (method != null) {
            if (URLS.equals(method)) {
                target = UrlListServlet.FILE_LIST_TARGET;
            } else if (WEBSTART.equals(method)) {
                target = "/javaWebstart";
            } else if (HTMLLIST.equals(method)) {
                target = "/wget.jsp";
            } else if (SHELL_SCRIPT.equals(method)) {
                target = ShellScriptServlet.SCRIPT_TARGET;
            } else if (DispatcherServlet.TAR_PACKAGE.equals(method)) {
                target = TARPackageServlet.TAR_PACKAGE_TARGET;
            } else if (DispatcherServlet.ZIP_PACKAGE.equals(method)) {
                target = ZIPPackageServlet.ZIP_PACKAGE_TARGET;
            } else {
                return null;
            }
        } else {
            // invalid method, tell page we did not forward
            return null;
        }

        log.debug("Determined method: " + method);
        return target;
    }

    /**
     * Handle POSTed download request from an external page.
     *
     * @param request  The HTTP Request.
     * @param response The HTTP Response.
     * @throws java.io.IOException            For any I/O related errors.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        ServletLogInfo logInfo = new ServletLogInfo(request);
        log.info(logInfo.start());

        long start = System.currentTimeMillis();

        try {
            Subject subject = AuthenticationUtil.getSubject(request);
            logInfo.setSubject(subject);

            DownloadAction action = new DownloadAction(request, response);

            if (subject == null) {
                action.run();
            } else {
                try {
                    Subject.doAs(subject, action);
                } catch (PrivilegedActionException pex) {
                    if (pex.getCause() instanceof ServletException) {
                        throw (ServletException) pex.getCause();
                    } else if (pex.getCause() instanceof IOException) {
                        throw (IOException) pex.getCause();
                    } else if (pex.getCause() instanceof RuntimeException) {
                        throw (RuntimeException) pex.getCause();
                    } else {
                        throw new RuntimeException(pex.getCause());
                    }
                }
            }
        } catch (IOException ex) {
            log.debug("caught: " + ex);
            throw ex;
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
        } finally {
            Long dt = System.currentTimeMillis() - start;
            logInfo.setElapsedTime(dt);
            log.info(logInfo.end());
        }
    }


    private static class DownloadAction implements PrivilegedExceptionAction<Object> {
        HttpServletRequest request;
        HttpServletResponse response;

        DownloadAction(HttpServletRequest request, HttpServletResponse response) {
            this.request = request;
            this.response = response;
        }

        public Object run() throws Exception {
            // forward
            DownloadRequest downloadReq = (DownloadRequest) request.getAttribute(INTERNAL_FORWARD_PARAMETER);

            if (downloadReq == null) {
                // Set up input handling
                DLMInputHandler inputHandler = new DLMInputHandler(request);

                // external post
                inputHandler.parseInput();

                downloadReq = DLMInputHandler.getDownloadRequest();
                Set<DownloadTuple> tupleList = downloadReq.getTuples();
                List<Exception> validationErrList = downloadReq.getValidationErrors();

                if ((tupleList == null || tupleList.isEmpty()
                    && (validationErrList == null || validationErrList.isEmpty()))) {
                    request.getRequestDispatcher("/emptySubmit.jsp").forward(request, response);
                    return null;
                }
                // DownloadRequest will be set as the forwarded attribute going forward...
                request.setAttribute(INTERNAL_FORWARD_PARAMETER, downloadReq);
            }

            // check for preferred/selected download method
            String target = getDownloadMethod(request);
            log.debug("Target: " + target);
            if (target == null) {
                target = "/chooser.jsp";
            }

            RequestDispatcher disp = request.getRequestDispatcher(target);
            disp.forward(request, response);
            return null;
        }
    }
}
