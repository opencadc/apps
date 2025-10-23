/*
 ************************************************************************
 *******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
 **************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
 *
 *  (c) 2011.                            (c) 2011.
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
 *  $Revision: 5 $
 *
 ************************************************************************
 */

package ca.nrc.cadc.downloadManager.integration;


import ca.nrc.cadc.auth.AuthMethod;
import ca.nrc.cadc.auth.AuthenticationUtil;
import ca.nrc.cadc.auth.CookiePrincipal;
import ca.nrc.cadc.auth.PrincipalExtractor;
import ca.nrc.cadc.auth.RunnableAction;
import ca.nrc.cadc.auth.X509CertificateChain;
import ca.nrc.cadc.dlm.server.DispatcherServlet;
import ca.nrc.cadc.net.ContentType;
import ca.nrc.cadc.net.FileContent;
import ca.nrc.cadc.net.HttpPost;
import ca.nrc.cadc.net.NetUtil;
import ca.nrc.cadc.net.NetrcFile;
import ca.nrc.cadc.reg.Standards;
import ca.nrc.cadc.reg.client.LocalAuthority;
import ca.nrc.cadc.reg.client.RegistryClient;
import ca.nrc.cadc.util.Log4jInit;
import ca.nrc.cadc.util.StringUtil;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import javax.security.auth.Subject;
import java.io.ByteArrayOutputStream;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


/**
 * @author pdowler
 */
public class DownloadManagerIntTest {
    private static final Logger log = Logger.getLogger(DownloadManagerIntTest.class);

    static {
        Log4jInit.setLevel("ca.nrc.cadc.downloadManager", Level.DEBUG);
    }

    private final static URI DOWNLOAD_URI = URI.create("ivo://cadc.nrc.ca/download");

    private final static String PUBLIC_CAOM_URI = "ivo://cadc.nrc.ca/BLAST?BLASTabell31122006-12-21/REDUCED_250_2009-03-06";

    private final static String SI_JCMT_URI = "cadc:JCMT/a20210525_00032_01_0001.sdf";

    // proprietary until 2031-01-01 aka permanently
    // - if this test is failing, we may need to find another proprietary plane
    private final static String PROP_CAOM_URI = "ivo://cadc.nrc.ca/JCMT?scuba2_00024_20121023T034337/raw-850um";

    private final static String PUBLIC_SUBARU_URI = "ivo://cadc.nrc.ca/SUBARU?CIAE00184551/CIAA00184551";

    // For testing JSON payload to web service
    private static final String URI_STR = "test://mysite.ca/path/1";
    private static final String SHAPE_STR = "polygon 0 0 0 0";
    private static final String LABEL_STR = "label";

    private static final String TUPLE_JSON = "{\"tuple\":{\"tupleID\":{\"$\":\"" + URI_STR + "\"}," +
        "\"shape\":{\"$\":\"" + SHAPE_STR + "\"}," +
        "\"label\":{\"$\":\"" + LABEL_STR + "\"}}}";

    Subject anonSubject;
    Subject authSubject;
    RegistryClient reg;

    public DownloadManagerIntTest() {
        this.reg = new RegistryClient();
        anonSubject = AuthenticationUtil.getSubject(new PrincipalExtractor() {

            public Set<Principal> getPrincipals() {
                return new HashSet<>();
            }

            public X509CertificateChain getCertificateChain() {
                return null;
            }
        });

        authSubject = AuthenticationUtil.getSubject(new PrincipalExtractor() {
            final Principal cookie = getSSOCookie();

            public Set<Principal> getPrincipals() {
                Set<Principal> ret = new HashSet<>();
                ret.add(cookie);
                return ret;
            }

            public X509CertificateChain getCertificateChain() {
                return null;
            }

            // obsolete
            private CookiePrincipal getSSOCookie() {
                LocalAuthority localAuthority = new LocalAuthority();
                URI serviceURI = localAuthority.getResourceID(Standards.UMS_LOGIN_10);
                log.debug("login uri: " + serviceURI.toString());
                URL url = reg.getServiceURL(serviceURI, Standards.UMS_LOGIN_01, AuthMethod.ANON);
                log.debug("login url: " + url.toExternalForm());

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                HttpPost login = new HttpPost(url, loginParameters(url), bos);
                login.run();
                if (login.getThrowable() != null) {
                    throw new RuntimeException("login failed: " + login.getResponseCode(), login.getThrowable());
                }
                String token = bos.toString();
                return new CookiePrincipal("CADC_SSO", token);
            }
        });
    }

    private Map<String, Object> loginParameters(final URL loginURL) {
        final NetrcFile netrcFile = new NetrcFile();
        final PasswordAuthentication passwordAuthentication = netrcFile.getCredentials(loginURL.getHost(), false);

        if (passwordAuthentication == null) {
            throw new RuntimeException("No credentials found for " + loginURL.getHost() + " in .netrc.");
        }

        final Map<String, Object> params = new TreeMap<>();
        params.put("username", passwordAuthentication.getUserName());
        params.put("password", new String(passwordAuthentication.getPassword()));

        return params;
    }

    private URL getTargetURL() throws Exception {
        final String configuredURL = System.getenv("DOWNLOAD_MANAGER_TEST_URL");
        final URL serviceURL;
        if (StringUtil.hasText(configuredURL)) {
            serviceURL = URI.create(configuredURL).toURL();
        } else {
            final URL downloadManagerBase =
                    reg.getAccessURL(RegistryClient.Query.APPLICATIONS, DownloadManagerIntTest.DOWNLOAD_URI);
            serviceURL = URI.create(downloadManagerBase.getProtocol() + "://" + downloadManagerBase.getHost()
                    + "/downloadManager/download").toURL();
        }

        log.debug("target URL: " + serviceURL.toExternalForm());
        return serviceURL;
    }

    @Test
    public void testAnonHTML() {
        try {
            URL url = getTargetURL();

            Map<String, Object> params = new TreeMap<>();
            params.put("uri", PUBLIC_CAOM_URI);
            params.put("method", DispatcherServlet.HTMLLIST);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HttpPost post = new HttpPost(url, params, bos);
            post.setFollowRedirects(false);
            post.prepare();

            final String contentType = post.getResponseHeader("content-type");
            Assert.assertNotNull(contentType);
            ContentType ct = new ContentType(contentType);
            Assert.assertEquals("text/html", ct.getBaseType());

            String html = bos.toString();
            log.debug("testAnonHTML:\n" + html);

            // TODO: parse and verify URLs are present? other format tests check to different levels

        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }

    @Test
    public void testAnonStorageInventory() {
        try {
            URL url = getTargetURL();

            Map<String, Object> params = new TreeMap<>();
            params.put("uri", SI_JCMT_URI);
            params.put("method", DispatcherServlet.SHELL_SCRIPT);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HttpPost post = new HttpPost(url, params, bos);
            post.setFollowRedirects(false);
            Subject.doAs(anonSubject, new RunnableAction(post));

            Assert.assertNull(post.getThrowable());
            Assert.assertEquals("response code", 200, post.getResponseCode());

            final String contentType = post.getResponseHeader("content-type");
            Assert.assertNotNull(contentType);
            ContentType ct = new ContentType(contentType);
            Assert.assertEquals("text/x-shellscript", ct.getBaseType());

            String shellScript = bos.toString();
            log.debug("testAnonScript:\n" + shellScript);

            // TODO: parse and verify URLs are present? other format tests check to different levels

        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }

    @Test
    public void testAuthHTML() {
        try {
            URL url = reg.getAccessURL(RegistryClient.Query.APPLICATIONS, DownloadManagerIntTest.DOWNLOAD_URI);
            url = new URL(url.getProtocol() + "://" + url.getHost() + "/downloadManager/download");

            Map<String, Object> params = new TreeMap<String, Object>();
            params.put("uri", PROP_CAOM_URI);
            params.put("method", DispatcherServlet.HTMLLIST);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HttpPost post = new HttpPost(url, params, bos);
            post.setFollowRedirects(false);
            Subject.doAs(authSubject, new RunnableAction(post));

            Assert.assertNull(post.getThrowable());
            Assert.assertEquals("response code", 200, post.getResponseCode());

            final String contentType = post.getResponseHeader("content-type");
            Assert.assertNotNull(contentType);
            ContentType ct = new ContentType(contentType);
            Assert.assertEquals("text/html", ct.getBaseType());

            String html = bos.toString();
            log.debug("testAuthHTML:\n" + html);

            // TODO: parse and verify URLs are present? other format tests check to different levels

        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }

    @Test
    public void testAnonTextPlain() {
        try {
            URL url = reg.getAccessURL(RegistryClient.Query.APPLICATIONS, DownloadManagerIntTest.DOWNLOAD_URI);
            url = new URL(url.getProtocol() + "://" + url.getHost() + "/downloadManager/download");

            Map<String, Object> params = new TreeMap<>();
            params.put("uri", PUBLIC_CAOM_URI);
            params.put("method", DispatcherServlet.URLS);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HttpPost post = new HttpPost(url, params, bos);
            post.setFollowRedirects(false);
            Subject.doAs(anonSubject, new RunnableAction(post));

            Assert.assertNull(post.getThrowable());
            Assert.assertEquals("response code", 200, post.getResponseCode());

            final String contentType = post.getResponseHeader("content-type");
            Assert.assertNotNull(contentType);
            ContentType ct = new ContentType(contentType);
            Assert.assertEquals("text/plain", ct.getBaseType());

            String doc = bos.toString();
            log.debug("testAnonTextPlain:\n" + doc);

            LineNumberReader r = new LineNumberReader(new StringReader(doc));
            String line = r.readLine();
            while (line != null) {
                line = line.trim();
                String[] tokens = line.split("[\\s]");
                Assert.assertEquals("tokens on line [" + line + "]", 1, tokens.length);
                Assert.assertNotSame("ERROR", tokens[0]);
                try {
                    URL dl = new URL(tokens[0]);
                } catch (MalformedURLException bad) {
                    Assert.fail("invalid URL: " + tokens[0] + " " + bad);
                }

                line = r.readLine();
            }

        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }

    @Test
    public void testAuthTextPlain() {
        try {
            URL url = reg.getAccessURL(RegistryClient.Query.APPLICATIONS, DownloadManagerIntTest.DOWNLOAD_URI);
            url = new URL(url.getProtocol() + "://" + url.getHost() + "/downloadManager/download");

            Map<String, Object> params = new TreeMap<String, Object>();
            params.put("uri", PROP_CAOM_URI);
            params.put("method", DispatcherServlet.URLS);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HttpPost post = new HttpPost(url, params, bos);
            post.setFollowRedirects(false);
            Subject.doAs(authSubject, new RunnableAction(post));

            Assert.assertNull(post.getThrowable());
            Assert.assertEquals("response code", 200, post.getResponseCode());

            final String contentType = post.getResponseHeader("content-type");
            Assert.assertNotNull(contentType);
            ContentType ct = new ContentType(contentType);
            Assert.assertEquals("text/plain", ct.getBaseType());

            String doc = bos.toString();
            log.debug("testAuthTextPlain:\n" + doc);

            LineNumberReader r = new LineNumberReader(new StringReader(doc));
            String line = r.readLine();
            while (line != null) {
                line = line.trim();
                // authSubject can't see proprietary metadata so: ERROR until we
                // have content to support auth test
                assertNotFoundError(line, PROP_CAOM_URI);
                //assertValidURL(line, PROP_CAOM_URI);
                line = r.readLine();
            }
        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }

    private void assertNotFoundError(String line, String uri) {
        String[] tokens = line.split("[\\s]");
        Assert.assertEquals("tokens on line", 4, tokens.length);
        Assert.assertEquals("ERROR", tokens[0]);
        Assert.assertEquals(uri, tokens[1]);
        // 2-3 is the error message from the datalink service
        Assert.assertEquals("NotFoundFault:", tokens[2]);
        Assert.assertEquals(uri, tokens[3]);
    }

    @Test
    public void testAnonProprietary() throws Exception {
        // this test will fail if PROP_CAOM_URI ever becomes public or disappears
        try {
            URL url = reg.getAccessURL(RegistryClient.Query.APPLICATIONS, DownloadManagerIntTest.DOWNLOAD_URI);
            url = new URL(url.getProtocol() + "://" + url.getHost() + "/downloadManager/download");

            Map<String, Object> params = new TreeMap<String, Object>();
            params.put("uri", PROP_CAOM_URI);
            params.put("method", DispatcherServlet.URLS);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HttpPost post = new HttpPost(url, params, bos);
            post.setFollowRedirects(false);
            Subject.doAs(anonSubject, new RunnableAction(post));

            Assert.assertNull(post.getThrowable());
            Assert.assertEquals("response code", 200, post.getResponseCode());

            final String contentType = post.getResponseHeader("content-type");
            Assert.assertNotNull(contentType);
            ContentType ct = new ContentType(contentType);
            Assert.assertEquals("text/plain", ct.getBaseType());

            String doc = bos.toString();
            log.debug("testAnonProprietary:\n" + doc);

            LineNumberReader r = new LineNumberReader(new StringReader(doc));
            String line = r.readLine();
            while (line != null) {
                line = line.trim();
                assertNotFoundError(line, PROP_CAOM_URI);
                line = r.readLine();
            }

        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            throw unexpected;
        }
    }

    @Test
    public void testResolveSUBARU() throws Exception {
        final URL url = getTargetURL();

        Map<String, Object> params = new TreeMap<>();
        params.put("uri", PUBLIC_SUBARU_URI);
        params.put("method", DispatcherServlet.URLS);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        HttpPost post = new HttpPost(url, params, bos);
        post.setFollowRedirects(false);
        Subject.doAs(anonSubject, new RunnableAction(post));

        Assert.assertNull(post.getThrowable());
        Assert.assertEquals("response code", 200, post.getResponseCode());

        final String contentType = post.getResponseHeader("content-type");
        Assert.assertNotNull(contentType);
        ContentType ct = new ContentType(contentType);
        Assert.assertEquals("text/plain", ct.getBaseType());

        String doc = bos.toString();
        log.debug("testAnonTextPlain:\n" + doc);

        LineNumberReader r = new LineNumberReader(new StringReader(doc));
        String line = r.readLine();

        if (line == null) {
            Assert.fail("Should have content for SUBARU URI: " + PUBLIC_SUBARU_URI);
        } else {
            while (line != null) {
                line = line.trim();
                Assert.assertEquals("tokens on line [" + line + "]", 1, line.split("[\\s]").length);
                line = r.readLine();
            }
        }
    }


    @Test
    public void testLoadChooserURIs() {
        try {
            URL url = getTargetURL();

            Map<String, Object> params = new TreeMap<>();
            params.put("uri", PUBLIC_CAOM_URI);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HttpPost post = new HttpPost(url, params, bos);
            post.setFollowRedirects(false);
            Subject.doAs(authSubject, new RunnableAction(post));

            Assert.assertNull(post.getThrowable());
            Assert.assertEquals("response code", 200, post.getResponseCode());

            // This tests that the page loads, that's about it.
        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }

    @Test
    public void testLoadChooserURIsPlusPOSCutout() {
        try {
            URL url = getTargetURL();

            Map<String, Object> params = new TreeMap<>();
            params.put("uri", PUBLIC_CAOM_URI);
            params.put("pos", NetUtil.encode("circle 9.0 8.0 0.5"));

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HttpPost post = new HttpPost(url, params, bos);
            post.setFollowRedirects(false);
            Subject.doAs(authSubject, new RunnableAction(post));

            Assert.assertNull(post.getThrowable());
            Assert.assertEquals("response code", 200, post.getResponseCode());

            // This tests that the page loads, that's about it.
        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }

    @Test
    public void testLoadChooserURIsPlusPixelCutout() {
        try {
            URL url = getTargetURL();

            Map<String, Object> params = new TreeMap<>();
            params.put("uri", PUBLIC_CAOM_URI);
            params.put("cutout", NetUtil.encode("[2]"));

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HttpPost post = new HttpPost(url, params, bos);
            post.setFollowRedirects(false);
            Subject.doAs(authSubject, new RunnableAction(post));

            Assert.assertNull(post.getThrowable());
            Assert.assertEquals("response code", 200, post.getResponseCode());

            // This tests that the page loads, that's about it.
        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }


    @Test
    public void testJSONTupleInput() throws Exception {
        URL url = getTargetURL();

        // pass in just tuples, this can/should be supported as well as multi-part data by the
        // underlying SyncInput class
        log.debug("testJSONTupleInput");
        // Build an array of one
        String jsonTuples = "{\"tupleList\":{\"$\":[" + TUPLE_JSON + "]}}";
        log.debug("jsonTuples string:" + jsonTuples);

        HttpPost post = new HttpPost(url, new FileContent(jsonTuples, "application/json", StandardCharsets.UTF_8), false);
        Subject.doAs(anonSubject, new RunnableAction(post));

        Assert.assertNull(post.getThrowable());
        Assert.assertEquals("response code", 200, post.getResponseCode());
    }


    @Test
    public void testAnonTextPlainJSON() {
        try {
            URL url = getTargetURL();

            Map<String, Object> params = new TreeMap<>();
            params.put("method", DispatcherServlet.URLS);
            params.put("runid", "testRunID");

            log.debug("testJSONTupleInput");
            // Build an array of one
            String jsonTuples = "{\"tupleList\":{\"$\":[" + TUPLE_JSON + "]}}";
            log.debug("jsonTuples string:" + jsonTuples);
            params.put("jsonTuples", new FileContent(jsonTuples, "application/json", StandardCharsets.UTF_8));

            HttpPost post = new HttpPost(url, params, false);
            Subject.doAs(anonSubject, new RunnableAction(post));

            Assert.assertNull(post.getThrowable());
            Assert.assertEquals("response code", 200, post.getResponseCode());

        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }
}
