
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

import ca.nrc.cadc.dali.util.ShapeFormat;
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
    private ShapeFormat sf = new ShapeFormat();
    private DownloadTupleFormat df = new DownloadTupleFormat();

    static {
        Log4jInit.setLevel("ca.nrc.cadc", Level.INFO);
    }

    @Test
    public void iterateURLsRemoveDuplicates() throws Exception {
        final DownloadRequest downloadReq = mkDownloadRequest(true);

        final List<DownloadDescriptor> expected = new ArrayList<DownloadDescriptor>();
        expected.add(new DownloadDescriptor("test://mysite.ca/path/1", new URL("http://mysite.ca/path/1")));
        expected.add(new DownloadDescriptor("test://mysite.ca/path/2", new URL("http://mysite.ca/path/2")));
        expected.add(new DownloadDescriptor("test://mysite.ca/path/3", new URL("http://mysite.ca/path/3")));
        expected.add(new DownloadDescriptor("test://mysite.ca/path/4", new URL("http://mysite.ca/path/4")));
        expected.add(new DownloadDescriptor("test://mysite.ca/path/5", new URL("http://mysite.ca/path/5")));
        expected.add(new DownloadDescriptor("test://mysite.ca/path/6", new URL("http://mysite.ca/path/6")));

        // Dump test results into a list for easy validation.
        final List<DownloadDescriptor> downloadDescriptorList = new ArrayList<>();

        final Iterator<DownloadDescriptor> iterator = DownloadUtil.iterateURLs(downloadReq);
        while (iterator.hasNext()) {
            downloadDescriptorList.add(iterator.next());
        }

        assertEquals("Should have 6 items due to a duplicate.", expected, downloadDescriptorList);
    }

    @Test
    public void iterateURLsWithDuplicates() throws Exception {
        final DownloadRequest downloadReq = mkDownloadRequest(true);

        final List<DownloadDescriptor> expected = new ArrayList<DownloadDescriptor>();
        expected.add(new DownloadDescriptor("test://mysite.ca/path/1", new URL("http://mysite.ca/path/1")));
        expected.add(new DownloadDescriptor("test://mysite.ca/path/2", new URL("http://mysite.ca/path/2")));
        expected.add(new DownloadDescriptor("test://mysite.ca/path/3", new URL("http://mysite.ca/path/3")));
        expected.add(new DownloadDescriptor("test://mysite.ca/path/4", new URL("http://mysite.ca/path/4")));
        expected.add(new DownloadDescriptor("test://mysite.ca/path/5", new URL("http://mysite.ca/path/5")));
        expected.add(new DownloadDescriptor("test://mysite.ca/path/6", new URL("http://mysite.ca/path/6")));

        // Dump test results into a list for easy validation.
        final List<DownloadDescriptor> downloadDescriptorList = new ArrayList<>();

        for (final Iterator<DownloadDescriptor> iterator =
             DownloadUtil.iterateURLs(downloadReq); iterator.hasNext(); ) {
            downloadDescriptorList.add(iterator.next());
        }

        assertEquals("Should have 7 items.", expected, downloadDescriptorList);
    }

    private DownloadRequest mkDownloadRequest(boolean addDuplicate) throws Exception {
        DownloadRequest dr = new DownloadRequest();
        dr.runID = "testRunID";

        dr.getTuples().add(df.parse("test://mysite.ca/path/1"));
        dr.getTuples().add(df.parse("test://mysite.ca/path/2"));
        dr.getTuples().add(df.parse("test://mysite.ca/path/3"));
        dr.getTuples().add(df.parse("test://mysite.ca/path/4"));
        if (addDuplicate == true) {
            dr.getTuples().add(df.parse("test://mysite.ca/path/2"));
        }
        dr.getTuples().add(df.parse("test://mysite.ca/path/5"));
        dr.getTuples().add(df.parse("test://mysite.ca/path/6"));

        return dr;
    }

    @Test
    public void iterateSingle() throws Exception {
        DownloadTuple testtuple = df.parse("test://cadc.nrc.ca/JCMT/scuba2_00047_20180426T160429/raw-450um");
        DownloadRequest dr = new DownloadRequest();
        dr.getTuples().add(testtuple);
        dr.runID = "testrunID";

        final List<DownloadDescriptor> expected = new ArrayList<>();
        expected.add(new DownloadDescriptor(testtuple.getID().toString(),
            new URL("http://cadc.nrc.ca/JCMT/scuba2_00047_20180426T160429/raw-450um")));

        // Dump test results into a list for easy validation.
        final List<DownloadDescriptor> downloadDescriptorList = new ArrayList<>();
        final Map<String, List<String>> params = Collections.emptyMap();

        for (final Iterator<DownloadDescriptor> iterator =
             DownloadUtil.iterateURLs(dr); iterator.hasNext(); ) {
            downloadDescriptorList.add(iterator.next());
        }

        assertEquals("Should have 1 item.", expected, downloadDescriptorList);
    }

}
