
package ca.nrc.cadc.dlm;

import ca.nrc.cadc.util.Log4jInit;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author pdowler
 */
public class IteratorTest {
    private static Logger log = Logger.getLogger(IteratorTest.class);

    static {
        Log4jInit.setLevel("ca.nrc.cadc", Level.INFO);
    }

    @Test
    public void testIterateOK() {
        log.debug("testIterateOK");
//        String sb = "http://www.google.com" +
//            DownloadUtil.URI_SEPARATOR +
//            "test://www.example.com/test";
//        List<String> uris = DownloadUtil.decodeListURI(sb);
        try {
            List<DownloadTuple> dts = new ArrayList<>();
            dts.add(new DownloadTuple(new URI("http://www.google.com")));
            dts.add(new DownloadTuple(new URI("test://www.example.com/test")));

            Iterator<DownloadDescriptor> iter = DownloadUtil.iterateURLs(dts, null);
            long num = 0;
            while (iter.hasNext()) {
                DownloadDescriptor dd = iter.next();
                num++;
                log.debug("found: " + dd);
                Assert.assertEquals(DownloadDescriptor.OK, dd.status);
                Assert.assertEquals("http", dd.url.getProtocol());
            }
            Assert.assertEquals(dts.size(), num);
        } catch (Exception unexpected) {
            Assert.fail("unexpected error: " + unexpected.toString());
        }
    }

    @Test
    public void testIterateDuplicates() {
        log.debug("testIterateDuplicates");
//        String sb = "http://www.google.com" +
//            DownloadUtil.URI_SEPARATOR +
//            "http://www.google.com";
//        List<String> uris = DownloadUtil.decodeListURI(sb);

        try {
            List<DownloadTuple> dts = new ArrayList<>();
            dts.add(new DownloadTuple(new URI("http://www.google.com")));
            dts.add(new DownloadTuple(new URI("test://www.example.com/test")));

            Assert.assertEquals("tuple setup", 2, dts.size());

            Iterator<DownloadDescriptor> iter = DownloadUtil.iterateURLs(dts, null);
            long num = 0;
            while (iter.hasNext()) {
                DownloadDescriptor dd = iter.next();
                num++;
                log.debug("found: " + dd);
                Assert.assertEquals(DownloadDescriptor.OK, dd.status);
                Assert.assertEquals("http", dd.url.getProtocol());
            }
            Assert.assertEquals(2, num);
            Assert.assertFalse(iter.hasNext());

            // now test with removeDuplicates==true
            iter = DownloadUtil.iterateURLs(dts, null, true);
            DownloadDescriptor dd = iter.next();
            log.debug("found: " + dd);
            Assert.assertEquals(DownloadDescriptor.OK, dd.status);
            Assert.assertEquals("http", dd.url.getProtocol());

            dd = iter.next();
            log.debug("found: " + dd);
            Assert.assertEquals(DownloadDescriptor.ERROR, dd.status);
            Assert.assertTrue(dd.error.contains("NoSuchElementException"));

            Assert.assertFalse(iter.hasNext());

        } catch (Exception unexpected) {
            Assert.fail("unexpected error: " + unexpected.toString());
        }
    }

    @Test
    public void testIterateError() {
        log.debug("testIterateError");

//        String s = "foo:bar/baz";
//        List<String> uris = DownloadUtil.decodeListURI(s);

        try {
            List<DownloadTuple> dts = new ArrayList<>();
            dts.add(new DownloadTuple(new URI("fake:fake/baz")));

            Iterator<DownloadDescriptor> iter = DownloadUtil.iterateURLs(dts, null);
            long num = 0;
            while (iter.hasNext()) {
                DownloadDescriptor dd = iter.next();
                num++;
                log.debug("found: " + dd);
                Assert.assertEquals(DownloadDescriptor.ERROR, dd.status);
            }
            Assert.assertEquals(dts.size(), num);

        } catch (Exception unexpected) {
            Assert.fail("unexpected error: " + unexpected.toString());
        }
    }

    @Test
    public void testIterateParams() {
//        String s = "test://www.example.com/test";
//        List<String> uris = DownloadUtil.decodeListURI(s);
//
//        String s2 = "runid=123&cutout=[1]&cutout=[2]";
//        Map<String, List<String>> params = DownloadUtil.decodeParamMap(s2);

        try {
            List<DownloadTuple> dts = new ArrayList<>();
            dts.add(new DownloadTuple(new URI("test://www.example.com/test")));

            String s2 = "runid=123&cutout=[1]&cutout=[2]";
            Map<String, List<String>> params = DownloadUtil.decodeParamMap(s2);

            Iterator<DownloadDescriptor> iter = DownloadUtil.iterateURLs(dts, params);

            long num = 0;
            while (iter.hasNext()) {
                DownloadDescriptor dd = iter.next();
                num++;
                log.debug("found: " + dd);
                Assert.assertEquals(DownloadDescriptor.OK, dd.status);
                Assert.assertEquals("http", dd.url.getProtocol());
                URI.create(dd.uri);
                Assert.assertNotNull(dd.url);
                Assert.assertNotNull(dd.url.getQuery());
                Assert.assertTrue(dd.url.getQuery().length() >= s2.length());
                Assert.assertTrue(dd.url.getQuery().contains("runid=123"));
                Assert.assertTrue(dd.url.getQuery().contains("cutout="));
            }
            Assert.assertEquals(dts.size(), num);

        } catch (Exception unexpected) {
            Assert.fail("unexpected error: " + unexpected.toString());
        }
    }

}
