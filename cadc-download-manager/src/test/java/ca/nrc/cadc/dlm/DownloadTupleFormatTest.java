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

import ca.nrc.cadc.dali.Shape;
import ca.nrc.cadc.util.Log4jInit;
import java.net.URI;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DownloadTupleFormatTest extends DownloadTupleTestBase {
    private static Logger log = Logger.getLogger(DownloadTupleFormatTest.class);
    private DownloadTupleFormat df = new DownloadTupleFormat();
    private DownloadTuple fullTestTuple;

    // test://mysite.ca/path/1{polygon 0 0 0 0 0 0}
    private static String TUPLE_INTERNAL_SHAPE = URI_STR + "{" + SHAPE_STR + "}";

    // test://mysite.ca/path/1{polygon 0 0 0 0 0 0}{label}
    private static String TUPLE_INTERNAL_FULL = TUPLE_INTERNAL_SHAPE + "{" + LABEL_STR + "}";


    static {
        Log4jInit.setLevel("ca.nrc.cadc", Level.INFO);
    }

    @Before
    public void testSetup() {
        try {
            // TODO: need CUTOUT tests as well as POS tests
            expectedURI = new URI(URI_STR);
            expectedCutout = sf.parse(SHAPE_STR);
            expectedLabel = LABEL_STR;
            fullTestTuple = new DownloadTuple(new URI(URI_STR), sf.parse(SHAPE_STR), LABEL_STR);
        } catch (Exception unexpectedSetupError) {
            log.error("DownalodTupleTest setup failed: " + unexpectedSetupError);
            Assert.fail("test setup failed.");
        }
    }

    // DownloadTuple to internalt format tests
    @Test
    public void testFormatURIOnly() throws Exception {
        DownloadTuple dt = new DownloadTuple(new URI(URI_STR));
        Assert.assertEquals("ctor didn't work for id", dt.getID(), expectedURI);
        Assert.assertEquals("ctor didn't work for cutout", null, dt.posCutout);
        Assert.assertEquals("ctor didn't work for label", null, dt.label);

        String internalFormat = df.format(dt);
        log.debug("internal format, uri only: " + df.format(dt));
        Assert.assertEquals("invalid internal tuple format", internalFormat, URI_STR);
    }

    @Test
    public void testFormatFullTuple() throws Exception {
        String internalFormat = df.format(fullTestTuple);
        log.debug("internal format, full: " + internalFormat);
        Assert.assertEquals("invalid internal tuple format", internalFormat, TUPLE_INTERNAL_FULL);
    }

    @Test
    public void testFormatNoLabel() throws Exception {
        DownloadTuple dt = new DownloadTuple(new URI(URI_STR), sf.parse(SHAPE_STR), null);
        Assert.assertEquals("ctor didn't work for id", dt.getID(), expectedURI);
        Assert.assertEquals("ctor didn't work for cutout", expectedCutout, dt.posCutout);
        Assert.assertEquals("ctor didn't work for label", null, dt.label);

        String internalFormat = df.format(dt);
        log.debug("internal format, no label: " + internalFormat);
        Assert.assertEquals("invalid internal tuple format", internalFormat, TUPLE_INTERNAL_SHAPE);
    }

    // Internal format string to DownloadTuple tests
    @Test
    public void testParseFullTuple() throws Exception {
        DownloadTuple dt = df.parse(TUPLE_INTERNAL_FULL);
        Assert.assertEquals("ctor didn't work for id", dt.getID(), expectedURI);
        Assert.assertEquals("ctor didn't work for cutout", expectedCutout, dt.posCutout);
        Assert.assertEquals("ctor didn't work for label", expectedLabel, dt.label);
    }

    // Internal format string to DownloadTuple tests
    @Test
    public void testParseFullTupleLabelConverted() throws Exception {
        DownloadTuple dt = df.parse("test://mysite.ca/path/1{circle 8.0 9.0 0.5}{02:24:07.5 +03:18:00 0.5}");
        String exConvertedLabel = "02_24_07.5_p03_18_00_0.5";
        Shape exCutout = sf.parse("circle 8.0 9.0 0.5");
        URI exID = new URI("test://mysite.ca/path/1");
        Assert.assertEquals("ctor didn't work for id", exID, dt.getID());
        Assert.assertEquals("ctor didn't work for cutout", exCutout, dt.posCutout);
        Assert.assertEquals("ctor didn't work for label", exConvertedLabel, dt.label);
    }

    @Test
    public void testParseFullTupleFromStrings() throws Exception {
        // This function can be used in InputHandler classes to leverage validation
        // provided in df.parse
        DownloadTuple dt = df.parseUsingInternalFormat(URI_STR, SHAPE_STR, LABEL_STR);
        Assert.assertEquals("ctor didn't work for id", dt.getID(), expectedURI);
        Assert.assertEquals("ctor didn't work for cutout", expectedCutout, dt.posCutout);
        Assert.assertEquals("ctor didn't work for label", expectedLabel, dt.label);
    }

    @Test
    public void testParseNoLabel() throws Exception {
        DownloadTuple dt = df.parse(TUPLE_INTERNAL_SHAPE);
        Assert.assertEquals("ctor didn't work for id", dt.getID(), expectedURI);
        Assert.assertEquals("ctor didn't work for cutout", expectedCutout, dt.posCutout);
        Assert.assertEquals("ctor didn't work for cutout", null, dt.pixelCutout);
        Assert.assertEquals("ctor didn't work for label", null, dt.label);
    }

    @Test
    public void testParseIDOnlyl() throws Exception {
        DownloadTuple dt = df.parse(URI_STR);
        Assert.assertEquals("ctor didn't work for id", dt.getID(), expectedURI);
        Assert.assertEquals("ctor didn't work for cutout", dt.posCutout, null);
        Assert.assertEquals("ctor didn't work for cutout", dt.pixelCutout, null);
        Assert.assertEquals("ctor didn't work for label", dt.label, null);
    }


    @Test
    public void testParseInvalidURI() {
        try {
            DownloadTuple dt = df.parse("bad uri1{circle 0.0 0.0 0.0}{label}");
        } catch (DownloadTupleParsingException parseError) {
            log.info("expected parsing error: " + parseError);
        } catch (Exception unexpected) {
            Assert.fail("unexpected error: " + unexpected);
        }

        try {
            DownloadTuple dt = df.parse("bad uri1");
        } catch (DownloadTupleParsingException parseError) {
            log.info("expected parsing error: " + parseError);
        } catch (Exception unexpected) {
            Assert.fail("unexpected error: " + unexpected);
        }
    }

    @Test
    public void testParseBadShapeCutout() {
        try {
            DownloadTuple dt = df.parse("test://mysite.ca/path/1{bad_polygon}{label}");
        } catch (DownloadTupleParsingException parseError) {
            log.info("expected parsing error: " + parseError);
        } catch (Exception unexpected) {
            Assert.fail("unexpected error: " + unexpected);
        }

        try {
            DownloadTuple dt = df.parse("test://mysite.ca/path/1{oblong 11.0 10.0 0.5}");
        } catch (DownloadTupleParsingException parseError) {
            log.info("expected parsing error: " + parseError);
        } catch (Exception unexpected) {
            Assert.fail("unexpected error: " + unexpected);
        }

        try {
            DownloadTuple dt = df.parse("test://mysite.ca/path/1{}");
        } catch (DownloadTupleParsingException parseError) {
            log.info("expected parsing error: " + parseError);
        } catch (Exception unexpected) {
            Assert.fail("unexpected error: " + unexpected);
        }
    }

    @Test
    public void testParseBadFormatBraceMismatch() {
        try {
            DownloadTuple dt = df.parse("test://mysite.ca/path/1 circle 0.0 0.0 0.0}{label}");
        } catch (DownloadTupleParsingException parseError) {
            log.info("expected parsing error: " + parseError);
        } catch (Exception unexpected) {
            Assert.fail("unexpected error: " + unexpected);
        }

        try {
            DownloadTuple dt = df.parse("test://mysite.ca/path/1{circle 0.0 0.0 0.0}label}");
        } catch (DownloadTupleParsingException parseError) {
            log.info("expected parsing error: " + parseError);
        } catch (Exception unexpected) {
            Assert.fail("unexpected error: " + unexpected);
        }

        try {
            DownloadTuple dt = df.parse("test://mysite.ca/path/1{circle 0.0 0.0 0.0}{label");
        } catch (DownloadTupleParsingException parseError) {
            log.info("expected parsing error: " + parseError);
        } catch (Exception unexpected) {
            Assert.fail("unexpected error: " + unexpected);
        }

    }

    @Test
    public void testParseMissingBracePair() {
        try {
            DownloadTuple dt = df.parse("test://mysite.ca/path/1{polygon 0 0 0 0 0 0 label}");
        } catch (DownloadTupleParsingException parseError) {
            log.info("expected parsing error: " + parseError);
        } catch (Exception unexpected) {
            Assert.fail("unexpected error: " + unexpected);
        }

        try {
            DownloadTuple dt = df.parse("test://mysite.ca/path/1 polygon 0 0 0 0 0 0}{label");
        } catch (DownloadTupleParsingException parseError) {
            log.info("expected parsing error: " + parseError);
        } catch (Exception unexpected) {
            Assert.fail("unexpected error: " + unexpected);
        }
    }

    @Test
    public void testParseTooManyBraces() {
        try {
            DownloadTuple dt = df.parse("test://mysite.ca/path/1{polygon 0 0 0 0 0 0}{label}{extraLabel}");
        } catch (DownloadTupleParsingException parseError) {
            log.info("expected parsing error: " + parseError);
        } catch (Exception unexpected) {
            Assert.fail("unexpected error: " + unexpected);
        }
    }

    @Test
    public void testParsePixelCutoutl() throws Exception {
        try {
            String pixelString = "[1]";
            URI expectURI = new URI("ivo://mysite.ca/path/1");
            DownloadTuple test = df.parsePixelStringTuple(URI_STR, pixelString);
            Assert.assertEquals("ctor didn't work for id", expectURI, test.getID());
            Assert.assertEquals("ctor didn't work for cutout", null, test.posCutout);
            Assert.assertEquals("ctor didn't work for cutout", pixelString, test.pixelCutout);
            Assert.assertEquals("ctor didn't work for label", null, test.label);

        } catch (DownloadTupleParsingException parseError) {
            log.info("expected parsing error: " + parseError);
        } catch (Exception unexpected) {
            Assert.fail("unexpected error: " + unexpected);
        }
    }

    @Test
    public void testValidLabelRadiusConversion() {
        // Note: convertLabelText() doesn't throw errors, so there's nothing to test for
        // invalid labels.

        //        M101 30' --> M101_30arcmin
        String label = "M101 30'";
        String expectedConvertedLabel = "M101_30arcmin";
        String actualConvertedLabel = "";
        actualConvertedLabel = df.convertLabelText(label);
        Assert.assertEquals("conversion failed", expectedConvertedLabel, actualConvertedLabel);

        //        M101 45" --> M101_45arcsec
        label = "M101 45\"";
        expectedConvertedLabel = "M101_45arcsec";
        actualConvertedLabel = df.convertLabelText(label);
        Assert.assertEquals("conversion failed", expectedConvertedLabel, actualConvertedLabel);

        //        HD79158 0.05 --> HD79158_0.05
        label = "HD79158 0.05";
        expectedConvertedLabel = "HD79158_0.05";
        actualConvertedLabel = df.convertLabelText(label);
        Assert.assertEquals("conversion failed", expectedConvertedLabel, actualConvertedLabel);

        //        HD79158/2 0.05 --> HD79158_2_0.05
        label = "HD79158/2 0.05";
        expectedConvertedLabel = "HD79158_2_0.05";
        actualConvertedLabel = df.convertLabelText(label);
        Assert.assertEquals("conversion failed", expectedConvertedLabel, actualConvertedLabel);
    }

    @Test
    public void testValidLabelTargetNameConversion() {
        // Note: convertLabelText() doesn't throw errors, so there's no way to test for
        // invalid labels.

        //        02:24:07.5 +03:18:00 0.5 --> 02:24:07.5_03:18:00_0.5
        String label = "02:24:07.5 +03:18:00 0.5";
        String expectedConvertedLabel = "02_24_07.5_p03_18_00_0.5";
        String actualConvertedLabel = df.convertLabelText(label);
        Assert.assertEquals("conversion failed", expectedConvertedLabel, actualConvertedLabel);
    }

}
