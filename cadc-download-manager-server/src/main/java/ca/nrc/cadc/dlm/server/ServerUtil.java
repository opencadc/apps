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

import ca.nrc.cadc.dlm.DownloadUtil;
import ca.nrc.cadc.util.StringUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;

/**
 * TODO.
 *
 * @author pdowler
 */
public class ServerUtil {
    // public API for DownloadManager is to accept and interpret these two params
    static final String PARAM_URI = "uri";
    static final String PARAM_PARAMLIST = "params";
    static final String PARAM_METHOD = "method";
    static final List<String> INTERNAL_PARAMS = new ArrayList<String>();
    private static final Logger log = Logger.getLogger(ServerUtil.class);

    static {
        INTERNAL_PARAMS.add(PARAM_URI);
        INTERNAL_PARAMS.add(PARAM_PARAMLIST);
        INTERNAL_PARAMS.add(PARAM_METHOD);
    }

    private ServerUtil() {
    }

    public static String getCodebase(HttpServletRequest request) {
        try {
            URL req = new URL(request.getRequestURL().toString());
            String ret = req.getProtocol() + "://" + req.getHost();
            ret += request.getContextPath();
            return ret;
        } catch (Throwable oops) {
            log.error("failed to generate codebase URL", oops);
        }
        return null;
    }

    /**
     * Extract all download content related parameters from the request.
     *
     * @param request
     * @return
     */
    public static Map<String, List<String>> getParameters(HttpServletRequest request) {
        // internal repost
        String params = request.getParameter("params");
        if (params != null) {
            return DownloadUtil.decodeParamMap(params);
        }

        // original post
        Map<String, List<String>> paramMap = new TreeMap<>();
        Enumeration e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            if (!INTERNAL_PARAMS.contains(key)) {
                String[] values = request.getParameterValues(key);
                if (values != null && values.length > 0) {
                    paramMap.put(key, Arrays.asList(values));
                }
            }
        }

        // not considered deprecated yet. parameters in 'fragment' will be
        // parsed into the paramMap
        List<String> frag = paramMap.get("fragment");
        if (frag != null) {
            for (String f : frag) {
                String[] parts = f.split("&");
                for (String p : parts) {
                    String[] kv = p.split("=");
                    if (kv.length == 2) {
                        List<String> values = paramMap.get(kv[0]);
                        if (values == null) {
                            values = new ArrayList<String>();
                            paramMap.put(kv[0], values);
                        }
                        values.add(kv[1]);
                    }
                }
            }
            paramMap.remove("fragment");
        }

        // Deprecated values need to be stripped out
        // Should only be 1 entry
        paramMap.remove("fileId");

        // can be more than one
        List<String> fcs = paramMap.get("fileClass");
        if (fcs != null) {
            for (String fc : fcs) {
                paramMap.remove(fc);
            }
            paramMap.remove("fileClass");
        }

        return paramMap;
    }

    /**
     * Extract all download content related parameters from the request.
     *
     * @param request
     * @return
     */
    public static List<URI> getURIs(HttpServletRequest request) throws URISyntaxException {

        List<URI> ret = new ArrayList<>();
        String[] uriList;
        uriList = request.getParameterValues(PARAM_URI);

        // process into a List<URI>
        if (uriList != null) {
            for (String u : uriList) {
                if (StringUtil.hasText(u)) {
                    ret.add(new URI(u));
                }
            }
        }

        // In case nothing is passed in as 'uri' or 'uris,' check for
        // deprecated parameters
        if (ret.isEmpty()) {
            ret = handleDeprecatedAPI(request);
        }

        return ret;
    }

    /**
     * Check for deprecated parameters in the request. Convert to the most current for Download Manager's API.
     * Kept for backward compatibility.
     * (last rev: Aug 7, 2020, s2739, HJ)
     * @param request
     */
    public static List<URI> handleDeprecatedAPI(HttpServletRequest request) {
        // Check to see if URIs have already been parsed from the request parameters.
        // If so, this is an internal dispatch/forward being processed
        List<URI> uriList = new ArrayList<>();
//        if (uriList == null) {
            try {
                String referer = request. getHeader("referer");
                String[] sa;

                // fileClass -> dynamic params = AD scheme-specific part
                String[] fileClasses = request.getParameterValues("fileClass");
                if (fileClasses != null) {
                    // fileClass is a list of parameters giving other URIs
                    log.debug("fileClass param(s): " + fileClasses.length);
                    log.warn("deprecated param 'fileClass' used by " + referer);
                    for (String fileClass : fileClasses) {
                        log.debug("fileClass: " + fileClass);
                        sa = request.getParameterValues(fileClass);
                        if (sa != null) {
                            for (String curSa : sa) {
                                String u = processURI(curSa);
                                if (u != null) {
                                    u = toAd(u);
                                    log.debug("\turi: " + u);
                                    uriList.add(new URI(u));
                                }
                            }
                        }
                    }
                }

                sa = request.getParameterValues("fileId");
                if (sa != null) {
                    log.debug("fileId param(s): " + sa.length);
                    log.warn("deprecated param 'fileId' used by " + referer);
                    for (String curSa : sa) {
                        String u = processURI(curSa);
                        if (u != null) {
                            u = toAd(u);
                            uriList.add(new URI(u));
                        }
                    }
                }

            } catch (URISyntaxException ure) {
                log.error("error parsing URI from deprecated input parameter");
            }

            return uriList;

    }


    /**
     * Validate content by trimming and checking length.
     *
     * @param uri
     * @return valid (trimmed non-zero-length) string or null
     */
    private static String processURI(String uri) {
        String ret = null;
        if (uri != null) {
            ret = uri.trim();
            if (ret.length() == 0) {
                ret = null;
            }
        }
        return ret;
    }

    // CADC-specific backwards compat: prepend ad scheme if there is none
    private static String toAd(String s) {
        String[] parts = s.split(","); // comma-sep list
        if (parts.length == 1) {
            if (s.indexOf(':') > 0) { // already has a scheme
                return s;
            }
            log.debug("adding ad scheme to " + s);
            return "ad:" + s;
        }
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            s = toAd(part);
            if (s != null) {
                sb.append(s).append(",");
            }
        }
        if (sb.length() > 0) {
            return sb.substring(0, sb.length() - 1); // trailing comma
        }
        return null;
    }

}
