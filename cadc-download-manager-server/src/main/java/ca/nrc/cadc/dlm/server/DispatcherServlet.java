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

import ca.nrc.cadc.auth.AuthMethod;
import ca.nrc.cadc.auth.AuthenticationUtil;
import ca.nrc.cadc.auth.SSOCookieCredential;
import ca.nrc.cadc.dlm.DownloadUtil;
import ca.nrc.cadc.log.ServletLogInfo;
import java.io.IOException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
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
    private static final long serialVersionUID = 201712011100L;

    private static final Logger log = Logger.getLogger(DispatcherServlet.class);

    public static String URLS = "URL List";
    public static String HTMLLIST = "HTML List";
    public static String WEBSTART = "Java Webstart";

    private static int ONE_YEAR = 365 * 24 * 3600;

    public static String DEFAULT_CONFIG_FILE_PATH = System.getProperty("user.home") + "/config/org.opencadc.dlm-server.properties";

    public void mkParamsCompatible(HttpServletRequest request) {
        // Nothing to be in done in base class
    }

    /**
     * Checks cookie and request param for download method preference; tries to set a cookie
     * to save setting for future use.
     *
     * @return name of page to forward to, null if caller should offer choices to user
     */
    public static String getDownloadMethod(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        String method = request.getParameter(ServerUtil.PARAM_METHOD);
        Cookie ck = null;

        // get cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("DownloadMethod")) {
                    ck = cookie;
                }
            }
        }

        String target = null;
        if ((method == null) && (ck != null) && (ck.getValue() != null)) {
            method = ck.getValue();
            if ((URLS.equals(method) || WEBSTART.equals(method))
                && (request.getParameter("execute") == null)) {
                target = "/clearChoice.jsp";
                if (URLS.equals(method)) {
                    request.setAttribute("Description",
                        "urlListDescription.html");
                } else {
                    request.setAttribute("Description",
                        "javaWebStartDescription.html");
                }
            } else if (HTMLLIST.equals(method)) {
                target = "/wget.jsp";
            }
        }

        if (target == null) {
            if (method != null) {
                if (URLS.equals(method)) {
                    target = UrlListServlet.FILE_LIST_TARGET;
                } else if (WEBSTART.equals(method)) {
                    target = "/javaWebstart";
                } else if (HTMLLIST.equals(method)) {
                    target = "/wget.jsp";
                } else {
                    return null;
                }
            } else {
                // invalid method, tell page we did not forward
                if (ck != null) {
                    // delete cookie on client
                    ck.setValue(null);
                    ck.setMaxAge(0); // delete
                    response.addCookie(ck);
                }
                return null;
            }
        }
        log.debug("Determined method: " + method);


        if (request.getParameter("remember") != null) {
            // set/edit cookie
            if (ck == null) { // new
                ck = new Cookie("DownloadMethod", method);
                ck.setPath(request.getContextPath());
                ck.setMaxAge(ONE_YEAR);
                response.addCookie(ck);
            } else if (!method.equals(ck.getValue())) { // changed
                ck.setValue(method);
                ck.setPath(request.getContextPath());
                ck.setMaxAge(ONE_YEAR);
                response.addCookie(ck);
            }
        } else {
            if ((request.getParameter("clearCookie") != null)
                && (ck != null)) {
                // remove cookie
                log.debug("Delete cookie!!!");
                ck.setPath(request.getContextPath());
                ck.setMaxAge(0);
                response.addCookie(ck);
            }
        }
        return target;
    }

    /**
     * Handle POSTed download request from an external page.
     *
     * @param request  The HTTP Request.
     * @param response The HTTP Response.
     * @throws javax.servlet.ServletException For general Servlet exceptions
     * @throws java.io.IOException            For any I/O related errors.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        ServletLogInfo logInfo = new ServletLogInfo(request);
        log.info(logInfo.start());

        long start = System.currentTimeMillis();

        try {
            // TODO: is this step necessary?
            Subject subject = AuthenticationUtil.getSubject(request);
            logInfo.setSubject(subject);

            handleDeprecatedAPI(request);

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
                RuntimeException rex = (RuntimeException) e;
                throw rex;
            }
        } finally {
            Long dt = System.currentTimeMillis() - start;
            logInfo.setElapsedTime(dt);
            log.info(logInfo.end());
        }
    }


    private class DownloadAction implements PrivilegedExceptionAction<Object> {
        HttpServletRequest request;
        HttpServletResponse response;

        DownloadAction(HttpServletRequest request, HttpServletResponse response) {
            this.request = request;
            this.response = response;
        }

        public Object run()
            throws Exception {
            // forward
            String uris = (String) request.getAttribute("uris");
            String params = (String) request.getAttribute("params");

            if (uris == null) {
                // external post
                List<String> uriList = ServerUtil.getURIs(request);
                if (uriList == null || uriList.isEmpty()) {
                    request.getRequestDispatcher("/emptySubmit.jsp").forward(request, response);
                    return null;
                }
                uris = DownloadUtil.encodeListURI(uriList);
                request.setAttribute("uris", uris);
            }

            if (params == null) {
                Map<String, List<String>> paramMap = ServerUtil.getParameters(request);
                if (paramMap != null && !paramMap.isEmpty()) {
                    params = DownloadUtil.encodeParamMap(paramMap);
                    request.setAttribute("params", params);
                }
            }

            log.debug("uris: " + uris);
            log.debug("params: " + params);

            // check for preferred/selected download method
            String target = getDownloadMethod(request, response);
            log.debug("Target: " + target);
            if (target == null) {
                target = "/chooser.jsp";
            }

            RequestDispatcher disp = request.getRequestDispatcher(target);
            disp.forward(request, response);
            return null;
        }
    }


    private void handleDeprecatedAPI(HttpServletRequest request) {

        String uris = (String) request.getAttribute("uris");
        // If no 'uris' parameter is passed in, check for deprecated parameters and
        // convert to 'uris' (URIs)
        if (uris == null)
        {
            List<String> uriList = new ArrayList<String>();
            String[] sa;

            // fileClass -> dynamic params = AD scheme-specific part
            String[] fileClasses = request.getParameterValues("fileClass");
            if (fileClasses != null)
            {
                // fileClass is a list of parameters giving other URIs
                log.debug("fileClass param(s): " + fileClasses.length);
                for (String fileClass : fileClasses)
                {
                    log.debug("fileClass: " + fileClass);
                    sa = request.getParameterValues(fileClass);
                    if (sa != null)
                    {
                        for (String aSa : sa)
                        {
                            String u = processURI(aSa);
                            if (u != null)
                            {
                                u = toAd(u);
                                log.debug("\turi: " + u);
                                uriList.add(u);
                            }
                        }
                    }
                }
            }

            sa = request.getParameterValues("fileId");
            if (sa != null)
            {
                log.debug("fileId param(s): " + sa.length);
                for (String aSa : sa)
                {
                    String u = processURI(aSa);
                    if (u != null)
                    {
                        u = toAd(u);
                        uriList.add(u);
                    }
                }
            }

            // Check to see if passed via parameter instead of attribute
            List<String> stdUriList = ServerUtil.getURIs(request);
            uriList.addAll(stdUriList);
            if ( !uriList.isEmpty() )
            {
                uris = DownloadUtil.encodeListURI(uriList);
                request.setAttribute("uris", uris);
            }
        }

        String params = (String) request.getAttribute("params");
        if (params == null) {
            Map<String,List<String>> paramMap = ServerUtil.getParameters(request);

            List<String> frag = paramMap.get("fragment");
            if (frag != null)
            {
                for (String f : frag)
                {
                    String[] parts = f.split("&");
                    for (String p : parts)
                    {
                        String[] kv = p.split("=");
                        if (kv.length == 2)
                        {
                            List<String> values = paramMap.get(kv[0]);
                            if (values == null)
                            {
                                values = new ArrayList<String>();
                                paramMap.put(kv[0], values);
                            }
                            values.add(kv[1]);
                        }
                    }
                }
                paramMap.remove("fragment");
            }

            // things to strip out
            paramMap.remove("fileId");
            List<String> fcs = paramMap.get("fileClass");
            if (fcs != null)
            {
                for (String fc : fcs)
                    paramMap.remove(fc);
                paramMap.remove("fileClass");
            }

            if (!paramMap.isEmpty())
            {
                params = DownloadUtil.encodeParamMap(paramMap);
                request.setAttribute("params", params);
            }
        }
    }

    /**
     * Validate content by trimming and checking length.
     *
     * @param uri
     * @return valid (trimmed non-zero-length) string or null
     */
    protected String processURI(String uri) {
        String ret = null;
        if (uri != null) {
            ret = uri.trim();
            if (ret.length() == 0)
                ret = null;
        }
        return ret;
    }

    // CADC-specific backwards compat: prepend ad scheme if there is none
    private String toAd(String s) {
        String[] parts = s.split(","); // comma-sep list
        if (parts.length == 1) {
            if (s.indexOf(':') > 0) // already has a scheme
                return s;
            log.debug("adding ad scheme to " + s);
            return "ad:"+s;
        }
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            s = toAd(part);
            if (s != null)
            {
                sb.append(s).append(",");
            }
        }
        if (sb.length() > 0)
            return sb.substring(0, sb.length() - 1); // trailing comma
        return null;
    }


}
