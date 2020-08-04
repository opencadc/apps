
/*
 ************************************************************************
 *******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
 **************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
 *
 *  (c) 2018.                            (c) 2018.
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
 *
 ************************************************************************
 */

package ca.nrc.cadc.dlm;

import ca.nrc.cadc.util.Log4jInit;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DownloadUtilTest {
    private static Logger log = Logger.getLogger(DownloadUtilTest.class);

    static {
        Log4jInit.setLevel("ca.nrc.cadc", Level.INFO);
    }

    @Test
    public void iterateURLsRemoveDuplicates() throws Exception {
        final List<DownloadTuple> tupleList = mkTupleList(true);
        final List<DownloadDescriptor> expected = new ArrayList<DownloadDescriptor>();
        expected.add(new DownloadDescriptor(tupleList.get(0).tupleID, new URL("http://mysite.ca/path/1")));
        expected.add(new DownloadDescriptor(tupleList.get(1).tupleID, new URL("http://mysite.ca/path/2")));
        expected.add(new DownloadDescriptor(tupleList.get(2).tupleID, new URL("http://mysite.ca/path/3")));
        expected.add(new DownloadDescriptor(tupleList.get(3).tupleID, new URL("http://mysite.ca/path/4")));
        expected.add(new DownloadDescriptor(tupleList.get(5).tupleID, new URL("http://mysite.ca/path/5")));
        expected.add(new DownloadDescriptor(tupleList.get(6).tupleID, new URL("http://mysite.ca/path/6")));

        // Dump test results into a list for easy validation.
        final List<DownloadDescriptor> downloadDescriptorList = new ArrayList<>();
        final Map<String, List<String>> params = Collections.emptyMap();

        final Iterator<DownloadDescriptor> iterator = DownloadUtil.iterateURLs(tupleList, params, true);
        while (iterator.hasNext()) {
            downloadDescriptorList.add(iterator.next());
        }

        assertEquals("Should have 6 items due to a duplicate.", expected, downloadDescriptorList);
    }

    @Test
    public void iterateURLsWithDuplicates() throws Exception {
        final ArrayList<DownloadTuple> tupleList = mkTupleList(true);

        final List<DownloadDescriptor> expected = new ArrayList<DownloadDescriptor>();
        expected.add(new DownloadDescriptor(tupleList.get(0).tupleID, new URL("http://mysite.ca/path/1")));
        expected.add(new DownloadDescriptor(tupleList.get(1).tupleID, new URL("http://mysite.ca/path/2")));
        expected.add(new DownloadDescriptor(tupleList.get(2).tupleID, new URL("http://mysite.ca/path/3")));
        expected.add(new DownloadDescriptor(tupleList.get(3).tupleID, new URL("http://mysite.ca/path/4")));
        expected.add(new DownloadDescriptor(tupleList.get(4).tupleID, new URL("http://mysite.ca/path/2")));
        expected.add(new DownloadDescriptor(tupleList.get(5).tupleID, new URL("http://mysite.ca/path/5")));
        expected.add(new DownloadDescriptor(tupleList.get(6).tupleID, new URL("http://mysite.ca/path/6")));

        // Dump test results into a list for easy validation.
        final List<DownloadDescriptor> downloadDescriptorList = new ArrayList<>();
        final Map<String, List<String>> params = Collections.emptyMap();

        for (final Iterator<DownloadDescriptor> iterator =
             DownloadUtil.iterateURLs(tupleList, params, false); iterator.hasNext(); ) {
            downloadDescriptorList.add(iterator.next());
        }

        assertEquals("Should have 7 items.", expected, downloadDescriptorList);
    }

    private ArrayList<DownloadTuple> mkTupleList(boolean addDuplicate) throws Exception {
        ArrayList<DownloadTuple> tupleList = new ArrayList<DownloadTuple>();

        tupleList.add(new DownloadTuple("test://mysite.ca/path/1"));
        tupleList.add(new DownloadTuple("test://mysite.ca/path/2"));
        tupleList.add(new DownloadTuple("test://mysite.ca/path/3"));
        tupleList.add(new DownloadTuple("test://mysite.ca/path/4"));
        if (addDuplicate == true) {
            tupleList.add(new DownloadTuple("test://mysite.ca/path/2"));
        }
        tupleList.add(new DownloadTuple("test://mysite.ca/path/5"));
        tupleList.add(new DownloadTuple("test://mysite.ca/path/6"));

        return tupleList;
    }

    @Test
    public void iterateSingle() throws Exception {
        final List<DownloadTuple> tupleList = new ArrayList<>();

        tupleList.add(new DownloadTuple("test://cadc.nrc.ca/JCMT/scuba2_00047_20180426T160429/raw-450um"));

        final List<DownloadDescriptor> expected = new ArrayList<>();
        expected.add(new DownloadDescriptor(tupleList.get(0).tupleID,
            new URL("http://cadc.nrc.ca/JCMT/scuba2_00047_20180426T160429/raw-450um")));

        // Dump test results into a list for easy validation.
        final List<DownloadDescriptor> downloadDescriptorList = new ArrayList<>();
        final Map<String, List<String>> params = Collections.emptyMap();

        for (final Iterator<DownloadDescriptor> iterator =
             DownloadUtil.iterateURLs(tupleList, params, false); iterator.hasNext(); ) {
            downloadDescriptorList.add(iterator.next());
        }

        assertEquals("Should have 1 item.", expected, downloadDescriptorList);
    }

    @Test
    public void testArgParsing() throws Exception {
        String[] args =  {"-verbose", "test://cadc.nrc.ca/JCMT/scuba2_00047_20180426T160429/raw-450um{shape_descriptor}{label}"};

        try {
            List<DownloadTuple> tupleList = DownloadUtil.parseTuplesFromArgs(args);

            for (DownloadTuple dt: tupleList) {
                assertEquals("tupleID didn't parse correctly", "test://cadc.nrc.ca/JCMT/scuba2_00047_20180426T160429/raw-450um", dt.tupleID);
                assertEquals("shapeDescriptor didn't parse correctly", "shape_descriptor", dt.shapeDescriptor);
                assertEquals("tupleID didn't parse correctly", "label", dt.label);
            }

        } catch (Exception e) {
            System.out.println("Unexpected exception");
        }
    }


    @Test
    public void testArgParsingSpaces() throws Exception {
        String[] args =  {"-verbose", "test://cadc.nrc.ca/JCMT/scuba2_00047_20180426T160429/raw-450um{shape", "descriptor}{label}"};

        try {
            List<DownloadTuple> tupleList = DownloadUtil.parseTuplesFromArgs(args);

            for (DownloadTuple dt: tupleList) {
                assertEquals("tupleID didn't parse correctly", "test://cadc.nrc.ca/JCMT/scuba2_00047_20180426T160429/raw-450um", dt.tupleID);
                assertEquals("shapeDescriptor didn't parse correctly", "shape descriptor", dt.shapeDescriptor);
                assertEquals("tupleID didn't parse correctly", "label", dt.label);
            }

        } catch (Exception e) {
            System.out.println("Unexpected exception");
        }
    }

    @Test
    public void testArgParsingSingleString() throws Exception {
        String[] args =  {"test://cadc.nrc.ca/JCMT/scuba2_00047_20180426T160429/raw-450um{shape descriptor}{label}"};

        try {
            List<DownloadTuple> tupleList = DownloadUtil.parseTuplesFromArgs(args);

            for (DownloadTuple dt: tupleList) {
                assertEquals("tupleID didn't parse correctly", "test://cadc.nrc.ca/JCMT/scuba2_00047_20180426T160429/raw-450um", dt.tupleID);
                assertEquals("shapeDescriptor didn't parse correctly", "shape descriptor", dt.shapeDescriptor);
                assertEquals("tupleID didn't parse correctly", "label", dt.label);
            }

        } catch (Exception e) {
            System.out.println("Unexpected exception");
        }
    }

}
