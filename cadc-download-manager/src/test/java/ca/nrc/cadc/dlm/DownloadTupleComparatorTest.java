
/*
 ************************************************************************
 *******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
 **************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
 *
 *  (c) 2020.                            (c) 2020.
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
import java.net.URI;
import java.util.Set;
import java.util.TreeSet;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DownloadTupleComparatorTest {
    private static Logger log = Logger.getLogger(DownloadTupleComparatorTest.class);
    private Set<DownloadTuple> testTreeSet;
    private ShapeFormat sf = new ShapeFormat();

    private String circleShape = "CIRCLE 1 2 3";
    private String circleShape2 = "CIRCLE 4 5 6";
    private String polygonShape = "POLYGON 1 2 3 4 5 6 ";

    private DownloadTuple d1;
    private DownloadTuple d2;
    private DownloadTuple d3;
    private DownloadTuple d4;
    private DownloadTuple d5;

    static {
        Log4jInit.setLevel("ca.nrc.cadc", Level.DEBUG);
    }

    @Before
    public void beforeTest() throws Exception {
        d1 = new DownloadTuple(new URI("test://uri/1"), sf.parse(circleShape), null, null, "circleLabel");
        d2 = new DownloadTuple(new URI("test://uri/2"), sf.parse(circleShape2),null, null,  "circleLabel2");
        d3 = new DownloadTuple(new URI("test://uri/3"), sf.parse(polygonShape),null, null,  "polygonLabel");

        d4 = new DownloadTuple(new URI("test://uri/1"));
        d5 = new DownloadTuple(new URI("test://uri/2"), sf.parse(circleShape2), null, null, null);
    }

    @Test
    public void testDuplicates() throws Exception {
        // Add a duplicate, size of TreeSet should be one less than number of tuples
        testTreeSet = new TreeSet(new DownloadTupleComparator());
        DownloadTuple dup = new DownloadTuple(new URI("test://uri/1"));
        dup.posCutout = sf.parse(circleShape);
        dup.label = "circleLabel";

        testTreeSet.add(d1);
        testTreeSet.add(d2);
        testTreeSet.add(dup);

        Assert.assertTrue("Duplicate was added", testTreeSet.size() == 2 );

        dup = new DownloadTuple(new URI("test://uri/1"));
        testTreeSet.add(d4);
        testTreeSet.add(dup);

        Assert.assertTrue("Duplicate was added", testTreeSet.size() == 3 );
        testTreeSet.add(d5);
        testTreeSet.add(dup);

        log.debug("testTreeSet size: " + testTreeSet.size());
        Assert.assertTrue("Duplicate was added", testTreeSet.size() == 4 );
    }

    @Test
    public void testNoDuplicates() throws Exception {
        // Add a duplicate, size of TreeSet should be one less than number of tuples
        testTreeSet = new TreeSet(new DownloadTupleComparator());

        testTreeSet.add(d1);
        Assert.assertTrue("Fully populated DownloadTuple not added", testTreeSet.size() == 1 );
        testTreeSet.add(d2);
        Assert.assertTrue("Fully populated DownloadTuple not added", testTreeSet.size() == 2 );
        testTreeSet.add(d3);
        Assert.assertTrue("Fully populated DownloadTuple not added", testTreeSet.size() == 3 );
        testTreeSet.add(d4);
        Assert.assertTrue("Null shape and label DownloadTuple not added", testTreeSet.size() == 4 );
        testTreeSet.add(d5);
        Assert.assertTrue("Null label DownloadTuple not added", testTreeSet.size() == 5 );
    }
}
