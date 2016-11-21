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

package ca.nrc.cadc.integration.caom2;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import ca.nrc.cadc.caom2.DataLinkClient;
import ca.nrc.cadc.dlm.DownloadDescriptor;
import ca.nrc.cadc.net.NetUtil;
import ca.nrc.cadc.util.CaseInsensitiveStringComparator;
import ca.nrc.cadc.util.Log4jInit;

/**
 *
 * @author pdowler
 */
public class DataLinkClientIntTest
{
     private static Logger log = Logger.getLogger(DataLinkClientIntTest.class);

    static
    {
        Log4jInit.setLevel("ca.nrc.cadc.caom2", Level.INFO);
        Log4jInit.setLevel("ca.nrc.cadc.integration.caom2", Level.INFO);
    }

    String CAOM_URI = "caom:IRIS/f232h000/IRAS-25um";
    String MULTI_PT_URI = "caom:BLAST/BLASTabell31122006-12-21/REDUCED_250_2009-03-06";

    public DataLinkClientIntTest()
    {

    }

    @Test
    public void testPlaneURI()
    {
        try
        {
            DataLinkClient dlc = new DataLinkClient();
            Iterator<DownloadDescriptor> iter = dlc.downloadIterator(new URI(CAOM_URI));

            // expect: one download + one cutout + 2 soda cutouts
            
            Assert.assertTrue(iter.hasNext());
            DownloadDescriptor dd = iter.next();
            Assert.assertEquals(CAOM_URI, dd.uri);
            
            Assert.assertTrue(iter.hasNext());
            dd = iter.next();
            Assert.assertEquals(CAOM_URI, dd.uri);
            
            Assert.assertTrue(iter.hasNext());
            dd = iter.next();
            Assert.assertEquals(CAOM_URI, dd.uri);
            
            Assert.assertTrue(iter.hasNext());
            dd = iter.next();
            Assert.assertEquals(CAOM_URI, dd.uri);

            Assert.assertFalse(iter.hasNext());

        }
        catch (Exception e)
        {
            log.error("unexpected exception", e);
            Assert.fail("unexpected exception: " + e);
        }
    }

    @Test
    public void testWithRUNID()
    {
        try
        {
            Map<String,List<String>> params = new TreeMap<String,List<String>>(new CaseInsensitiveStringComparator());
            List<String> ss = new ArrayList<String>();
            ss.add("TEST");
            params.put("RUNID", ss);

            DataLinkClient dlc = new DataLinkClient();
            dlc.setParameters(params);
            Iterator<DownloadDescriptor> iter = dlc.downloadIterator(new URI(CAOM_URI));

            Assert.assertTrue(iter.hasNext());
            while ( iter.hasNext() )
            {
                DownloadDescriptor dd = iter.next();
                Assert.assertEquals(CAOM_URI, dd.uri);
                if (dd.url != null) // skip cutout link
                {
                    String query = dd.url.getQuery();
                    Assert.assertNotNull("null query string", query);
                    query = query.toLowerCase();
                    Assert.assertTrue("runid in query string", query.contains("runid=test"));
                }
            }
        }
        catch (Exception e)
        {
            log.error("unexpected exception", e);
            Assert.fail("unexpected exception: " + e);
        }
    }

    // filtering is deprecated and not used via AdvancedSearch
    //@Test
    public void testWithFilter()
    {
        try
        {
            Map<String,List<String>> params = new TreeMap<String,List<String>>(new CaseInsensitiveStringComparator());
            List<String> ss = new ArrayList<String>();
            ss.add("science");
            ss.add("foo");
            params.put("productType", ss);

            DataLinkClient dlc = new DataLinkClient();
            dlc.setParameters(params);
            Iterator<DownloadDescriptor> iter = dlc.downloadIterator(new URI(CAOM_URI));

            Assert.assertTrue(iter.hasNext());
            DownloadDescriptor dd = iter.next();
            Assert.assertEquals(CAOM_URI, dd.uri);

            Assert.assertFalse("found one science already", iter.hasNext());

            params = new HashMap<String,List<String>>();
            ss = new ArrayList<String>();
            ss.add("foo");
            params.put("productType", ss);

            dlc.setParameters(params);
            iter = dlc.downloadIterator(new URI(CAOM_URI));

            // no results with productType = foo
            Assert.assertFalse("found no foo", iter.hasNext());
        }
        catch (Exception e)
        {
            log.error("unexpected exception", e);
            Assert.fail("unexpected exception: " + e);
        }
    }

    //@Test
    public void testWithFilterMultiple()
    {
        try
        {
            Map<String,List<String>> params = new TreeMap<String,List<String>>(new CaseInsensitiveStringComparator());
            List<String> ss = new ArrayList<String>();
            ss.add("science");
            ss.add("foo");
            params.put("productType", ss);

            DataLinkClient dlc = new DataLinkClient();
            dlc.setParameters(params);
            Iterator<DownloadDescriptor> iter = dlc.downloadIterator(new URI(MULTI_PT_URI));

            Assert.assertTrue(iter.hasNext());
            DownloadDescriptor dd = iter.next();
            Assert.assertEquals(MULTI_PT_URI, dd.uri);

            // only one science
            Assert.assertFalse("found one science already", iter.hasNext());

            params = new HashMap<String,List<String>>();
            ss = new ArrayList<String>();
            ss.add("auxiliary");
            params.put("productType", ss);

            dlc.setParameters(params);
            iter = dlc.downloadIterator(new URI(MULTI_PT_URI));

            Assert.assertTrue(iter.hasNext());
            dd = iter.next();
            Assert.assertEquals(MULTI_PT_URI, dd.uri);

            Assert.assertTrue(iter.hasNext());
            dd = iter.next();
            Assert.assertEquals(MULTI_PT_URI, dd.uri);

            Assert.assertTrue(iter.hasNext());
            dd = iter.next();
            Assert.assertEquals(MULTI_PT_URI, dd.uri);

            Assert.assertFalse("found 3 aux already", iter.hasNext());
        }
        catch (Exception e)
        {
            log.error("unexpected exception", e);
            Assert.fail("unexpected exception: " + e);
        }
    }

    @Test
    public void testWithCutout()
    {
        try
        {
            Map<String,List<String>> params = new TreeMap<String,List<String>>(new CaseInsensitiveStringComparator());
            List<String> ss = new ArrayList<String>();
            ss.add("CIRCLE ICRS 12 34 1");
            params.put("cutout", ss);

            String cutoutStr = "cutout=" + NetUtil.encode(ss.get(0));

            ss = new ArrayList<String>();
            ss.add("TEST");
            params.put("RUNID", ss);

            DataLinkClient dlc = new DataLinkClient();
            dlc.setParameters(params);
            Iterator<DownloadDescriptor> iter = dlc.downloadIterator(new URI(CAOM_URI));

            Assert.assertTrue(iter.hasNext());
            DownloadDescriptor dd = iter.next();
            Assert.assertEquals(CAOM_URI, dd.uri);

            Assert.assertFalse("found one url already", iter.hasNext());

            log.debug("cutout url: " + dd.url);

            Assert.assertNotNull("has URL", dd.url);

            String query = dd.url.getQuery();
            Assert.assertNotNull("query", query);
            Assert.assertTrue("query contains cutout", query.contains(cutoutStr));

            Assert.assertTrue("query contains runid", query.contains("runid=TEST"));
        }
        catch (Exception e)
        {
            log.error("unexpected exception", e);
            Assert.fail("unexpected exception: " + e);
        }
    }

//    @Test
    public void testWithFilterCutout()
    {
        try
        {
            Map<String,List<String>> params = new TreeMap<String,List<String>>(new CaseInsensitiveStringComparator());
            List<String> ss = new ArrayList<String>();
            ss.add("science");
            params.put("productType", ss);

            ss = new ArrayList<String>();
            ss.add("CIRCLE ICRS 12 34 1");
            params.put("cutout", ss);

            String cutoutStr = "cutout=" + NetUtil.encode(ss.get(0));

            DataLinkClient dlc = new DataLinkClient();
            dlc.setParameters(params);
            Iterator<DownloadDescriptor> iter = dlc.downloadIterator(new URI(MULTI_PT_URI));

            Assert.assertTrue(iter.hasNext());
            DownloadDescriptor dd = iter.next();
            Assert.assertEquals(MULTI_PT_URI, dd.uri);

            // only one science
            Assert.assertFalse("found one science already", iter.hasNext());

            log.debug("cutout url: " + dd.url);

            Assert.assertNotNull("has URL", dd.url);

            String query = dd.url.getQuery();
            Assert.assertNotNull("query", query);
            Assert.assertTrue("query contains cutout", query.contains(cutoutStr));

        }
        catch (Exception e)
        {
            log.error("unexpected exception", e);
            Assert.fail("unexpected exception: " + e);
        }
    }
}
