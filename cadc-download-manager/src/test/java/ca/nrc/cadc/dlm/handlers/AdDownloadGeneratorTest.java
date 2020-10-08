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
import ca.nrc.cadc.dlm.DownloadDescriptor;
import ca.nrc.cadc.dlm.DownloadTuple;
import ca.nrc.cadc.dlm.DownloadTupleFormat;
import ca.nrc.cadc.reg.Standards;
import ca.nrc.cadc.reg.client.RegistryClient;
import ca.nrc.cadc.util.Log4jInit;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

/**
 *
 * @author pdowler
 */
public class AdDownloadGeneratorTest
{
    private static final Logger log = Logger.getLogger(AdDownloadGeneratorTest.class);
    private DownloadTupleFormat df = new DownloadTupleFormat();
    static
    {
        Log4jInit.setLevel("ca.nrc.cadc.dlm.handlers", Level.DEBUG);
    }

    URL baseURL;
    
    public AdDownloadGeneratorTest()
    {
        try
        {
            RegistryClient rc = new RegistryClient();
            URL serviceURL = rc.getServiceURL(
                URI.create("ivo://cadc.nrc.ca/data"), Standards.DATA_10, AuthMethod.ANON);
            this.baseURL = serviceURL;
        }
        catch(Throwable t)
        {

        }
    }

    @Test
    public void testURL()
    {
        try
        {
            AdDownloadGenerator gen = new AdDownloadGenerator();
            
            URI uri = new URI("ad", "SomeArchive/SomeFileID", null);
            DownloadTuple dt = new DownloadTuple(uri);
            Iterator<DownloadDescriptor> iter = gen.downloadIterator(dt);

            Assert.assertNotNull(iter);
            Assert.assertTrue(iter.hasNext());

            DownloadDescriptor dd = iter.next();

            Assert.assertEquals("uri", uri.toASCIIString(), dd.uri);

            Assert.assertEquals("protocol", baseURL.getProtocol(), dd.url.getProtocol());
            Assert.assertEquals("hostname", baseURL.getHost(), dd.url.getHost());
            Assert.assertEquals("path", baseURL.getPath() + "/SomeArchive/SomeFileID", dd.url.getPath());

            Assert.assertFalse(iter.hasNext());
        }
        catch(Exception unexpected)
        {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }
    
    @Test
    public void testURLPlusCutout()
    {
        try
        {
            AdDownloadGenerator gen = new AdDownloadGenerator();
            gen.setRunID("testRunID");

            URI uri = new URI("ad", "SomeArchive/SomeFileID", null);
            DownloadTuple dt = new DownloadTuple(uri, "[1]");
            Iterator<DownloadDescriptor> iter = gen.downloadIterator(dt);

            Assert.assertNotNull(iter);
            Assert.assertTrue(iter.hasNext());

            DownloadDescriptor dd = iter.next();
            log.debug(dd.status);
            Assert.assertEquals(DownloadDescriptor.OK, dd.status);

            Assert.assertEquals("uri", uri.toASCIIString(), dd.uri);

            Assert.assertEquals("protocol", baseURL.getProtocol(), dd.url.getProtocol());
            Assert.assertEquals("hostname", baseURL.getHost(), dd.url.getHost());
            Assert.assertEquals("path", baseURL.getPath() + "/SomeArchive/SomeFileID", dd.url.getPath());

            Assert.assertNotNull("query", dd.url.getQuery());
            Assert.assertTrue("runID", dd.url.getQuery().contains("runid=testRunID"));
            Assert.assertTrue("cutout", dd.url.getQuery().contains("cutout="+encodeString("[1]") ));
            // TODO: should this support multiple cutout parameters?
            //            Assert.assertTrue("cutout1", dd.url.getQuery().contains( "cutout="+encodeString("[1]") ));
            //            Assert.assertTrue("cutout2", dd.url.getQuery().contains( "cutout="+encodeString("[2]") ));
            
        }
        catch(Exception unexpected)
        {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }

    @Test
    public void testIllegalScheme()
    {
        try
        {
            AdDownloadGenerator gen = new AdDownloadGenerator();
            URI uri = new URI("foo", "SomeArchive/SomeFileID", null);
            DownloadTuple dt = new DownloadTuple(uri);
            Iterator<DownloadDescriptor> iter = gen.downloadIterator(dt);
            Assert.assertNotNull(iter);
            Assert.assertTrue(iter.hasNext());

            DownloadDescriptor dd = iter.next();
            Assert.assertEquals(DownloadDescriptor.ERROR, dd.status);
            Assert.assertEquals(uri.toASCIIString(), dd.uri);
            Assert.assertNull(dd.url);
            Assert.assertNotNull(dd.error);
        }
        catch(Exception unexpected)
        {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }

//    @Test
    public void testLogKeyValue()
    {
        // This test is commented out until logkey/logvalue are determined
        // to still be needed as input parameters - Sept, 2020
        // Currently are pruned out during move to using DownloadRequest
        // & DownloadTuple as input
        try
        {
//            Map<String,List<String>> params = new HashMap<String,List<String>>();
//            List<String> ss = new ArrayList<String>();
//            ss.add("FOO");
//            params.put("logkey", ss);
//            ss = new ArrayList<String>();
//            ss.add("BAR");
//            params.put("logvalue", ss);

            AdDownloadGenerator gen = new AdDownloadGenerator();

            URI uri = new URI("ad", "SomeArchive/SomeFileID", null);
            DownloadTuple dt = new DownloadTuple(uri);
            Iterator<DownloadDescriptor> iter = gen.downloadIterator(dt);

            Assert.assertNotNull(iter);
            Assert.assertTrue(iter.hasNext());

            DownloadDescriptor dd = iter.next();
            Assert.assertEquals(DownloadDescriptor.OK, dd.status);

            Assert.assertEquals("uri", uri.toASCIIString(), dd.uri);

            Assert.assertEquals("protocol", baseURL.getProtocol(), dd.url.getProtocol());
            Assert.assertEquals("hostname", baseURL.getHost(), dd.url.getHost());
            Assert.assertEquals("path", baseURL.getPath() + "/SomeArchive/SomeFileID", dd.url.getPath());

            Assert.assertNotNull("query", dd.url.getQuery());
            Assert.assertTrue("logKey", dd.url.getQuery().contains("logkey=FOO"));
            Assert.assertTrue("logValue", dd.url.getQuery().contains("logvalue=BAR"));

        }
        catch(Exception unexpected)
        {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }

    private static String encodeString(String str)
    {
        try { return URLEncoder.encode(str, "UTF-8"); }
        catch(UnsupportedEncodingException ignore) { }
        return null;
    }
}
