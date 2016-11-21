/*
 ************************************************************************
 ****  C A N A D I A N   A S T R O N O M Y   D A T A   C E N T R E  *****
 *
 * (c) 2011.                            (c) 2011.
 * National Research Council            Conseil national de recherches
 * Ottawa, Canada, K1A 0R6              Ottawa, Canada, K1A 0R6
 * All rights reserved                  Tous droits reserves
 *
 * NRC disclaims any warranties         Le CNRC denie toute garantie
 * expressed, implied, or statu-        enoncee, implicite ou legale,
 * tory, of any kind with respect       de quelque nature que se soit,
 * to the software, including           concernant le logiciel, y com-
 * without limitation any war-          pris sans restriction toute
 * ranty of merchantability or          garantie de valeur marchande
 * fitness for a particular pur-        ou de pertinence pour un usage
 * pose.  NRC shall not be liable       particulier.  Le CNRC ne
 * in any event for any damages,        pourra en aucun cas etre tenu
 * whether direct or indirect,          responsable de tout dommage,
 * special or general, consequen-       direct ou indirect, particul-
 * tial or incidental, arising          ier ou general, accessoire ou
 * from the use of the software.        fortuit, resultant de l'utili-
 *                                      sation du logiciel.
 *
 ****  C A N A D I A N   A S T R O N O M Y   D A T A   C E N T R E  *****
 ************************************************************************
 */

package ca.nrc.cadc.dlm.handlers;

import ca.nrc.cadc.auth.AuthMethod;
import ca.nrc.cadc.auth.AuthenticationUtil;
import ca.nrc.cadc.net.SchemeHandler;
import ca.nrc.cadc.reg.Standards;
import ca.nrc.cadc.reg.client.RegistryClient;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * SchemeHandler implementation for the Archive Directory (ad) system. 
 * This class can convert an AD URI into a URL. This is an alternate version
 * that uses the RegistryClient to find the data web service base URL.
 * 
 * @author pdowler
 */
public class AdSchemeHandler implements SchemeHandler
{
    private static final Logger log = Logger.getLogger(AdSchemeHandler.class);

    public static final String SCHEME = "ad";
    
    private static final String DATA_URI = "ivo://cadc.nrc.ca/data";
    
    private String baseURL;

    public AdSchemeHandler()
    {
        try
        {
            RegistryClient rc = new RegistryClient();
            AuthMethod am = AuthenticationUtil.getAuthMethod(AuthenticationUtil.getCurrentSubject());
            if (am == null)
            {
                am = AuthMethod.ANON;
            }
            URL serviceURL = rc.getServiceURL(URI.create(DATA_URI), Standards.DATA_10, am);
            this.baseURL = serviceURL.toExternalForm();
        }
        catch(Throwable t)
        {
            log.error("failed to find CADC data service URL", t);
            throw new RuntimeException("BUG: failed to find CADC data service URL", t);
        }
        log.debug("CADC data service URL: " + baseURL);
    }

    public URL getURL(URI uri)
    {
        if (!SCHEME.equals(uri.getScheme()))
            throw new IllegalArgumentException("invalid scheme in " + uri);

        try
        {
            StringBuilder sb = createURL(uri);
            URL url = new URL(sb.toString());
            log.debug(uri + " --> " + url);
            return url;
        }
        catch(MalformedURLException ex)
        {
            throw new RuntimeException("BUG", ex);
        }
    }
    
    /**
     * Convert a URI to a List of URL(s).
     *
     * @param uri a CADC storage system URI (ad scheme)
     * @throws IllegalArgumentException if the URI scheme is invalid
     * @return a list with a single URL to the identified resource
     */
    public List<URL> toURL(URI uri)
        throws IllegalArgumentException
    {
        if (!SCHEME.equals(uri.getScheme()))
            throw new IllegalArgumentException("invalid scheme in " + uri);
        
        URL url = getURL(uri);
        List<URL> ret = new ArrayList<URL>();
        ret.add(url);
        return ret;
    }


    private StringBuilder createURL(URI uri)
    {
        String[]  path = uri.getSchemeSpecificPart().split("/");
        if (path.length != 2)
            throw new IllegalArgumentException("malformed AD URI, expected 2 path componets, found " + path.length);
        String arc = path[0];
        String fid = path[1];

        StringBuilder sb = new StringBuilder();
        sb.append(baseURL);
        sb.append("/");
        sb.append(encodeString(arc));
        sb.append("/");
        sb.append(encodeString(fid));
        return sb;
    }
    
    private static String encodeString(String str)
    {
        try { return URLEncoder.encode(str, "UTF-8"); }
        catch(UnsupportedEncodingException ignore) { }
        return null;
    }
}
