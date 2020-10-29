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

package ca.nrc.cadc.dlm.client;

import static org.junit.Assert.assertEquals;

import ca.nrc.cadc.dali.DoubleInterval;
import ca.nrc.cadc.dali.Shape;
import ca.nrc.cadc.dali.util.DoubleIntervalFormat;
import ca.nrc.cadc.dali.util.ShapeFormat;
import ca.nrc.cadc.dlm.DownloadRequest;
import ca.nrc.cadc.dlm.DownloadTuple;
import ca.nrc.cadc.util.ArgumentMap;
import ca.nrc.cadc.util.Log4jInit;
import java.net.URI;
import java.util.Set;
import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Test;

public class MainArgParsingTest {
    private ShapeFormat sf = new ShapeFormat();
    private DoubleIntervalFormat dif = new DoubleIntervalFormat();

    static {
        Log4jInit.setLevel("ca.nrc.cadc", Level.DEBUG);
    }

    // This test suite is testing the parsing code in Main, focusing on parsing tuples from a
    // command line, deteting '{}' segments if they appear, with single or multiple tuples
    // provided in different states of completion (URI only, URI + shape, or URI + shape + label.)
    // It does not exhaustively test the quality of the tuple data provided.
    // Those code paths are tested fully in DownloadTupleFormatTest.

    @Test
    public void testArgParsingSingleTuple() throws Exception {
        String testArgStr = "test://cadc.nrc.ca/TEST/testDevice1{circle 10.0 11.0 0.166667}{2.0 3.0}{testLabel_1011} ";
        String[] args =   testArgStr.split(" ");
        Shape expectedCutout = sf.parse("circle 10.0 11.0 0.166667");
        DoubleInterval expectedInterval = dif.parse("2.0 3.0");

        ArgumentMap am = new ArgumentMap(args);

        try {
            URI testURI = new URI("test://cadc.nrc.ca/TEST/testDevice1");
            DownloadRequest dr = Main.getDownloadRequest(am);
            Set<DownloadTuple> tupleList = dr.getTuples();

            assertEquals("Should be 1 valid tuple found. (found " + tupleList.size() + ")", 1, tupleList.size());
            assertEquals("Should be no invalid tuples found. (found " + dr.getValidationErrors().size() + ")", 0, dr.getValidationErrors().size());

            for (DownloadTuple dt: tupleList) {
                assertEquals("id didn't parse correctly", testURI, dt.getID());
                assertEquals("pos cutout didn't parse correctly", expectedCutout, dt.posCutout);
                assertEquals("band cutout didn't parse correctly", expectedInterval, dt.bandCutout);
                assertEquals("label didn't parse correctly", "testLabel_1011", dt.label);
            }

        } catch (Exception e) {
            System.out.println("Unexpected exception");
        }
    }

    @Test
    public void testArgParsingBandCutout() throws Exception {
        String testArgStr = "test://cadc.nrc.ca/TEST/testDevice1{}{2.0 3.0}{} ";
        String[] args = testArgStr.split(" ");
        DoubleInterval expectedInterval = dif.parse("2.0 3.0");

        ArgumentMap am = new ArgumentMap(args);

        try {
            URI testURI = new URI("test://cadc.nrc.ca/TEST/testDevice1");
            DownloadRequest dr = Main.getDownloadRequest(am);
            Set<DownloadTuple> tupleList = dr.getTuples();

            assertEquals("Should be 1 valid tuple found. (found " + tupleList.size() + ")", 1, tupleList.size());
            assertEquals("Should be no invalid tuples found. (found " + dr.getValidationErrors().size() + ")", 0, dr.getValidationErrors().size());

            for (DownloadTuple dt: tupleList) {
                assertEquals("id didn't parse correctly", testURI, dt.getID());
                assertEquals("band cutout didn't parse correctly", expectedInterval, dt.bandCutout);
                assertEquals("pos cutout didn't parse correctly", null, dt.posCutout);
                assertEquals("label didn't parse correctly", null, dt.label);
            }

        } catch (Exception e) {
            System.out.println("Unexpected exception");
        }
    }

    @Test
    public void testArgParsingMultipleTuples() {
        String testArgStr = "test://cadc.nrc.ca/TEST/testDevice1{circle 10.0 11.0 0.5}{}{testLabel_1011} "
            + "test://cadc.nrc.ca/TEST/testDevice2{circle 9.0 8.0 0.5}{}{testLabel_98}";
        Shape expectedCutout1 = sf.parse("circle 10.0 11.0 0.5");
        Shape expectedCutout2 = sf.parse("circle 9.0 8.0 0.5");

        String[] args =   testArgStr.split(" ");
        ArgumentMap am = new ArgumentMap(args);

        try {
            DownloadRequest dr = Main.getDownloadRequest(am);
            Set<DownloadTuple> tupleList = dr.getTuples();
            assertEquals("Should be 2 valid tuples found. (found " + tupleList.size() + ")", 2, tupleList.size());
            assertEquals("Should be no invalid tuples found. (found " + dr.getValidationErrors().size() + ")", 0, dr.getValidationErrors().size());

            for (DownloadTuple dt: tupleList) {
                if (dt.getID().toString().contains("testDevice2")) {
                    assertEquals("shapeDescriptor didn't parse correctly", expectedCutout2, dt.posCutout);
                    assertEquals("tupleID didn't parse correctly", "testLabel_98", dt.label);
                } else if (dt.getID().toString().contains("testDevice1")) {
                    assertEquals("shapeDescriptor didn't parse correctly", expectedCutout1, dt.posCutout);
                    assertEquals("tupleID didn't parse correctly", "testLabel_1011", dt.label);
                } else {
                    Assert.fail("IDs did not parse correctly");
                }
            }

        } catch (Exception e) {
            System.out.println("Unexpected exception");
        }
    }

    @Test
    public void testArgParsingInvalidSpacingMultipleTuples() {
        // 'Invalid' refers to no space between the tuples, so there's no distinct
        // end to the tuples in the input
        String testArgStr = "test://cadc.nrc.ca/TEST/testDevice1{circle 10.0 11.0 0.5}{}{testLabel_1011}"
            + "test://cadc.nrc.ca/TEST/testDevice2{circle 9.0 8.0 0.5}{}{testLabel_98}";

        String[] args =   testArgStr.split(" ");
        ArgumentMap am = new ArgumentMap(args);

        try {
            DownloadRequest dr = Main.getDownloadRequest(am);
            Set<DownloadTuple> tupleList = dr.getTuples();
            assertEquals("Should be no valid tuples found. (found " + tupleList.size() + ")", 0, tupleList.size());
            assertEquals("Should be no 1 tuple found. (found " + dr.getValidationErrors().size() + ")", 1, dr.getValidationErrors().size());

        } catch (Exception e) {
            System.out.println("Unexpected exception");
        }

        testArgStr = "test://cadc.nrc.ca/TEST/testDevice1   {circle 10.0 11.0 0.5}{}{testLabel_1011}"
            + " test://cadc.nrc.ca/TEST/testDevice2{circle 9.0 8.0 0.5}{}{testLabel_98}";

        args =   testArgStr.split(" ");
        am = new ArgumentMap(args);

        try {
            DownloadRequest dr = Main.getDownloadRequest(am);
            Set<DownloadTuple> tupleList = dr.getTuples();
            assertEquals("Should be 2 valid tuples found. (found " + tupleList.size() + ")", 2, tupleList.size());
            assertEquals("Should be 1 invalid found. (found " + dr.getValidationErrors().size() + ")", 1, dr.getValidationErrors().size());

        } catch (Exception e) {
            System.out.println("Unexpected exception");
        }

    }

    @Test
    public void testInvalidArgNoURI() {
        String testArgStr = "test://cadc.nrc.ca/TEST/testDevice1{circle 10.0 11.0 0.5}{}{testLabel_1011} "
            + "{circle 9.0 8.0 0.5}{}{testLabel_98}";

        String[] args =   testArgStr.split(" ");
        ArgumentMap am = new ArgumentMap(args);
        Shape expectedCutout1 = sf.parse("circle 10.0 11.0 0.5");

        try {
            URI testURI = new URI("test://cadc.nrc.ca/TEST/testDevice1");

            DownloadRequest dr = Main.getDownloadRequest(am);
            Set<DownloadTuple> tupleList = dr.getTuples();
            assertEquals("Should be 1 valid tuple found. (found " + tupleList.size() + ")", 1, tupleList.size());
            assertEquals("Should be 1 invalid tuple found. (found " + dr.getValidationErrors().size() + ")", 1, dr.getValidationErrors().size());

            for (DownloadTuple dt: tupleList) {
                assertEquals("URI didn't parse correctly", testURI, dt.getID());
                assertEquals("shapeDescriptor didn't parse correctly", expectedCutout1, dt.posCutout);
                assertEquals("tupleID didn't parse correctly", "testLabel_1011", dt.label);
            }

        } catch (Exception e) {
            System.out.println("Unexpected exception");
        }
    }

    @Test
    public void testArgParsingURIOnly() {
        String testArgStr = "test://cadc.nrc.ca/TEST/testDevice1";
        String[] args =   testArgStr.split(" ");
        ArgumentMap am = new ArgumentMap(args);

        try {
            URI testURI = new URI("test://cadc.nrc.ca/TEST/testDevice1");

            DownloadRequest dr = Main.getDownloadRequest(am);
            Set<DownloadTuple> tupleList = dr.getTuples();
            assertEquals("Should be 1 valid tuple found. (found " + tupleList.size() + ")", 1, tupleList.size());
            assertEquals("Should be no invalid tuples found. (found " + dr.getValidationErrors().size() + ")", 0, dr.getValidationErrors().size());

            for (DownloadTuple dt: tupleList) {
                assertEquals("URI didn't parse correctly", testURI, dt.getID());
                assertEquals("shapeDescriptor should be null", null, dt.posCutout);
                assertEquals("tupleID didn't parse correctly", null, dt.label);
            }

        } catch (Exception e) {
            System.out.println("Unexpected exception");
        }
    }

    @Test
    public void testArgParsingMultipleURIOnly() {
        String testArgStr = "test://cadc.nrc.ca/TEST/testDevice1 test://cadc.nrc.ca/TEST/testDevice2";

        String[] args =   testArgStr.split(" ");
        ArgumentMap am = new ArgumentMap(args);

        try {
            DownloadRequest dr = Main.getDownloadRequest(am);
            Set<DownloadTuple> tupleList = dr.getTuples();
            assertEquals("Should be 2 valid tuples found. (found " + tupleList.size() + ")", 2, tupleList.size());
            assertEquals("Should be no invalid tuples found. (found " + dr.getValidationErrors().size() + ")", 0, dr.getValidationErrors().size());

            for (DownloadTuple dt: tupleList) {
                if (dt.getID().toString().contains("testDevice1") ||
                    dt.getID().toString().contains("testDevice2")) {
                    assertEquals("shapeDescriptor should be null", null, dt.posCutout);
                    assertEquals("tupleID didn't parse correctly", null, dt.label);
                }
            }

        } catch (Exception e) {
            System.out.println("Unexpected exception");
        }
    }

    @Test
    public void testArgParsingNoLabel() {
        String testArgStr = "test://cadc.nrc.ca/TEST/testDevice1{circle 10.0 11.0 0.5}{}{}";
        String[] args =   testArgStr.split(" ");
        ArgumentMap am = new ArgumentMap(args);

        try {
            URI testURI = new URI("test://cadc.nrc.ca/TEST/testDevice1");
            Shape expectedCutout1 = sf.parse("circle 10.0 11.0 0.5");
            DownloadRequest dr = Main.getDownloadRequest(am);
            Set<DownloadTuple> tupleList = dr.getTuples();
            assertEquals("Should be 1 valid tuple found. (found " + tupleList.size() + ")", 1, tupleList.size());
            assertEquals("Should be no invalid tuples found. (found " + dr.getValidationErrors().size() + ")", 0, dr.getValidationErrors().size());

            for (DownloadTuple dt: tupleList) {
                assertEquals("URI didn't parse correctly", testURI, dt.getID());
                assertEquals("shapeDescriptor didn't parse correctly", expectedCutout1, dt.posCutout);
                assertEquals("tupleID should be null", null, dt.label);
            }

        } catch (Exception e) {
            System.out.println("Unexpected exception");
        }
    }

    @Test
    public void testArgParsingMultipleNoLabel() {
        String testArgStr = "test://cadc.nrc.ca/TEST/testDevice1{circle 10.0 11.0 0.5}{}{testLabel_1011} "
            + "test://cadc.nrc.ca/TEST/testDevice2{circle 9.0 8.0 0.5}{}{}";
        Shape expectedCutout1 = sf.parse("circle 10.0 11.0 0.5");
        Shape expectedCutout2 = sf.parse("circle 9.0 8.0 0.5");

        String[] args =   testArgStr.split(" ");
        ArgumentMap am = new ArgumentMap(args);

        try {
            DownloadRequest dr = Main.getDownloadRequest(am);
            Set<DownloadTuple> tupleList = dr.getTuples();
            assertEquals("Should be 2 valid tuples found. (found " + tupleList.size() + ")", 2, tupleList.size());
            assertEquals("Should be no invalid tuples found. (found " + dr.getValidationErrors().size() + ")", 0, dr.getValidationErrors().size());

            for (DownloadTuple dt: tupleList) {
                if (dt.getID().toString().contains("testDevice2")) {
                    assertEquals("shapeDescriptor didn't parse correctly", expectedCutout2, dt.posCutout);
                    assertEquals("tupleID didn't parse correctly", null, dt.label);
                } else if (dt.getID().toString().contains("testDevice1")) {
                    assertEquals("shapeDescriptor didn't parse correctly", expectedCutout1, dt.posCutout);
                    assertEquals("tupleID didn't parse correctly", "testLabel_1011", dt.label);
                } else {
                    Assert.fail("IDs did not parse correctly");
                }
            }

        } catch (Exception e) {
            System.out.println("Unexpected exception");
        }
    }

    @Test
    public void testArgParsingMultipleNoLabelMissingShape() {
        String testArgStr = "test://cadc.nrc.ca/TEST/testDevice1{}{}{testLabel_1011} "
            + "test://cadc.nrc.ca/TEST/testDevice2{circle 9.0 8.0 0.5}{}{testLabel_98}";

        String[] args =   testArgStr.split(" ");
        ArgumentMap am = new ArgumentMap(args);

        try {
            URI testURI = new URI("test://cadc.nrc.ca/TEST/testDevice2");
            Shape expectedCutout1 = sf.parse("circle 9.0 8.0 0.5");

            DownloadRequest dr = Main.getDownloadRequest(am);
            Set<DownloadTuple> tupleList = dr.getTuples();
            assertEquals("Should be 1 valid tuple found. (found " + tupleList.size() + ")", 1, tupleList.size());
            assertEquals("Should be 1 invalid tuple found. (found " + dr.getValidationErrors().size() + ")", 1, dr.getValidationErrors().size());

            for (DownloadTuple dt: tupleList) {
                assertEquals("URI didn't parse correctly", testURI, dt.getID());
                assertEquals("shapeDescriptor didn't parse correctly", expectedCutout1, dt.posCutout);
                assertEquals("tupleID didn't parse correctly", "testLabel_98", dt.label);
            }

        } catch (Exception e) {
            System.out.println("Unexpected exception");
        }
    }

}
