/*
 ************************************************************************
 ****  C A N A D I A N   A S T R O N O M Y   D A T A   C E N T R E  *****
 *
 * (c) 2020.                            (c) 2020.
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
import ca.nrc.cadc.caom2.artifact.resolvers.CadcResolver;
import ca.nrc.cadc.dali.Circle;
import ca.nrc.cadc.dali.DoubleInterval;
import ca.nrc.cadc.dali.Interval;
import ca.nrc.cadc.dali.Point;
import ca.nrc.cadc.dali.Polygon;
import ca.nrc.cadc.dlm.DownloadDescriptor;
import ca.nrc.cadc.dlm.DownloadTuple;
import ca.nrc.cadc.reg.Standards;
import ca.nrc.cadc.reg.client.RegistryClient;
import ca.nrc.cadc.util.Log4jInit;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * @author jenkinsd
 */
public class CadcDownloadGeneratorTest {
    private static final Logger log = Logger.getLogger(CadcDownloadGeneratorTest.class);
    static {
        Log4jInit.setLevel("ca.nrc.cadc.dlm.handlers", Level.DEBUG);
    }

    @Before
    public void setUp() {
        System.setProperty(RegistryClient.class.getName() + ".host", "ws.cadc-ccda.hia-iha.nrc-cnrc.gc.ca");
    }

    @After
    public void tearDown() {
        System.clearProperty(RegistryClient.class.getName() + ".host");
    }

    private static URL getBaseURL() {
        final RegistryClient rc = new RegistryClient();
        return rc.getServiceURL(CadcResolver.STORAGE_INVENTORY_URI, Standards.SI_FILES, AuthMethod.ANON);
    }

    @Test
    public void testURL() {
        try {
            final CadcDownloadGenerator gen = new CadcDownloadGenerator();
            final URI uri = URI.create("cadc:archiveName/file_1.fits");
            final DownloadTuple dt = new DownloadTuple(uri);
            final Iterator<DownloadDescriptor> descriptorIterator = gen.downloadIterator(dt);

            Assert.assertNotNull(descriptorIterator);
            Assert.assertTrue(descriptorIterator.hasNext());

            final DownloadDescriptor dd = descriptorIterator.next();
            final URL baseURL = CadcDownloadGeneratorTest.getBaseURL();

            Assert.assertEquals("uri", uri.toASCIIString(), dd.uri);
            Assert.assertEquals("protocol", baseURL.getProtocol(), dd.url.getProtocol());
            Assert.assertEquals("hostname", baseURL.getHost(), dd.url.getHost());
            Assert.assertEquals("path", baseURL.getPath() + "/cadc:archiveName/file_1.fits",
                                dd.url.getPath());

            Assert.assertFalse(descriptorIterator.hasNext());
        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }

    @Test
    public void testURLPlusSUBCutout() {
        try {
            final CadcDownloadGenerator gen = new CadcDownloadGenerator();
            gen.setRunID("testRunID");

            final URI uri = URI.create("cadc:archiveName/file_1.fits");
            final DownloadTuple dt = new DownloadTuple(uri);
            dt.pixelCutout = "[1]";
            final Iterator<DownloadDescriptor> descriptorIterator = gen.downloadIterator(dt);

            Assert.assertNotNull(descriptorIterator);
            Assert.assertTrue(descriptorIterator.hasNext());

            DownloadDescriptor dd = descriptorIterator.next();
            log.debug(dd.status);
            Assert.assertEquals(DownloadDescriptor.OK, dd.status);

            final URL baseURL = CadcDownloadGeneratorTest.getBaseURL();
            Assert.assertEquals("uri", uri.toASCIIString(), dd.uri);
            Assert.assertEquals("protocol", baseURL.getProtocol(), dd.url.getProtocol());
            Assert.assertEquals("hostname", baseURL.getHost(), dd.url.getHost());
            Assert.assertEquals("path", baseURL.getPath() + "/cadc:archiveName/file_1.fits",
                                dd.url.getPath());

            Assert.assertEquals("query", "runid=testRunID&SUB=" + encodeString(dt.pixelCutout),
                                dd.url.getQuery());

        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }

    @Test
    public void testURLPlusCIRCLECutout() {
        try {
            final CadcDownloadGenerator gen = new CadcDownloadGenerator();
            final URI uri = URI.create("cadc:archiveName/file_1.fits");
            final DownloadTuple dt = new DownloadTuple(uri);
            dt.posCutout = new Circle(new Point(88.0D, 9.0D), 0.5D);
            final Iterator<DownloadDescriptor> descriptorIterator = gen.downloadIterator(dt);

            Assert.assertNotNull(descriptorIterator);
            Assert.assertTrue(descriptorIterator.hasNext());

            DownloadDescriptor dd = descriptorIterator.next();
            log.debug(dd.status);
            Assert.assertEquals(DownloadDescriptor.OK, dd.status);

            final URL baseURL = CadcDownloadGeneratorTest.getBaseURL();

            Assert.assertEquals("uri", uri.toASCIIString(), dd.uri);
            Assert.assertEquals("protocol", baseURL.getProtocol(), dd.url.getProtocol());
            Assert.assertEquals("hostname", baseURL.getHost(), dd.url.getHost());
            Assert.assertEquals("path", baseURL.getPath() + "/cadc:archiveName/file_1.fits",
                                dd.url.getPath());

            Assert.assertEquals("query", "CIRCLE=" + encodeString("88.0 9.0 0.5"), dd.url.getQuery());

        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }

    @Test
    public void testURLPlusBANDAndPOLYGONCutout() {
        try {
            final CadcDownloadGenerator gen = new CadcDownloadGenerator();
            final URI uri = URI.create("cadc:archiveName/file_1.fits");

            final DownloadTuple dt = new DownloadTuple(uri);
            dt.bandCutout = new Interval<>(14.6D, 64.1D);

            final Polygon polygon = new Polygon();
            polygon.getVertices().add(new Point(10.0D, 10.0D));
            polygon.getVertices().add(new Point(10.2D, 10.0D));
            polygon.getVertices().add(new Point(10.2D, 10.2D));
            polygon.getVertices().add(new Point(10.0D, 10.2D));
            dt.posCutout = polygon;

            final Iterator<DownloadDescriptor> descriptorIterator = gen.downloadIterator(dt);

            Assert.assertNotNull(descriptorIterator);
            Assert.assertTrue(descriptorIterator.hasNext());

            DownloadDescriptor dd = descriptorIterator.next();
            log.debug(dd.status);
            Assert.assertEquals(DownloadDescriptor.OK, dd.status);

            final URL baseURL = CadcDownloadGeneratorTest.getBaseURL();

            Assert.assertEquals("uri", uri.toASCIIString(), dd.uri);
            Assert.assertEquals("protocol", baseURL.getProtocol(), dd.url.getProtocol());
            Assert.assertEquals("hostname", baseURL.getHost(), dd.url.getHost());
            Assert.assertEquals("path", baseURL.getPath() + "/cadc:archiveName/file_1.fits",
                                dd.url.getPath());

            Assert.assertEquals("query", "POLYGON=" + encodeString("10.0 10.0 10.2 10.0 10.2 10.2 10.0 10.2")
                                         + "&BAND=" + encodeString("14.6 64.1"), dd.url.getQuery());

        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }

    @Test
    public void testIllegalScheme() {
        try {
            final CadcDownloadGenerator gen = new CadcDownloadGenerator();
            final URI uri = URI.create("ad:archiveName/archiveFile.fits");
            final DownloadTuple downloadTuple = new DownloadTuple(uri);
            final Iterator<DownloadDescriptor> descriptorIterator = gen.downloadIterator(downloadTuple);

            Assert.assertNotNull(descriptorIterator);
            Assert.assertTrue(descriptorIterator.hasNext());

            final DownloadDescriptor dd = descriptorIterator.next();

            Assert.assertEquals(DownloadDescriptor.ERROR, dd.status);
            Assert.assertEquals(uri.toASCIIString(), dd.uri);
            Assert.assertNull(dd.url);
            Assert.assertNotNull(dd.error);
        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }

    private static String encodeString(String str) {
        return URLEncoder.encode(str, StandardCharsets.UTF_8);
    }
}
