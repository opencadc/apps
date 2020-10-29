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

import ca.nrc.cadc.dali.DoubleInterval;
import ca.nrc.cadc.dali.Shape;
import ca.nrc.cadc.dali.util.DoubleIntervalFormat;
import ca.nrc.cadc.dali.util.ShapeFormat;
import ca.nrc.cadc.util.StringUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

public class DownloadTupleFormat {
    private static Logger log = Logger.getLogger(DownloadTupleFormat.class);
    // Use this to determine which type of cutout is passed in
    MultiDownloadGenerator multiDG = new MultiDownloadGenerator();

    /**
     * Parse DownloadTuple from internal format string.
     * @param tupleStr String representing a tuple
     * @return DownloadTuple populated with information from the input
     * @throws DownloadTupleParsingException if tuple is malformed or content is invalid
     */
    public DownloadTuple parse(String tupleStr) throws DownloadTupleParsingException {
        log.debug("tuple string input: " + tupleStr);
        // format is either:
        // uri (no braces)
        // uri{optional position string}{optional band string}{optional label string}

        // Do a rough match of number of '{' to '}'
        // This doesn't check for order or overall format
        int openBraceCount = getCount(tupleStr, "\\{");
        int closeBraceCount = getCount(tupleStr, "\\}");

        log.debug("found count of open & close braces : " + openBraceCount + ", " + closeBraceCount);

        if (openBraceCount != closeBraceCount) {
            throw new DownloadTupleParsingException("invalid tuple format: mismatched braces ': " + tupleStr);
        }

        // either all or nothing
        if (openBraceCount != 0 && openBraceCount != 3) {
            throw new DownloadTupleParsingException("invalid tuple format: " + tupleStr);
        }

        // Split out the tuple parts
        String[] tupleParts = tupleStr.split("\\{");
        // Get tuple URI - should at least have this.
        URI tmpURI = null;
        String uriStr = tupleParts[0];
        log.debug("uri given: " + uriStr);
        if (StringUtil.hasLength(uriStr)) {
            try {
                tmpURI = new URI(uriStr);
            } catch (URISyntaxException u) {
                throw new DownloadTupleParsingException("id parsing error: " + tupleStr, u);
            }
        } else {
            // invalid format - has to at least be a single URI passed in
            throw new DownloadTupleParsingException("zero length id found: " + tupleStr);
        }

        // Create new DownloadTuple
        DownloadTuple newTuple = new DownloadTuple(tmpURI);

        if (openBraceCount == 3) {
            String tmpPosStr = tupleParts[1];
            if (StringUtil.hasLength(tmpPosStr)) {
                // trim off trailing "}"
                String tmpShapeStr = tmpPosStr.substring(0, tmpPosStr.length() - 1);
                log.debug("cutout string: " + tmpShapeStr);
                if (StringUtil.hasLength(tmpShapeStr)) {
                    try {
                        ShapeFormat sf = new ShapeFormat();
                        newTuple.posCutout = sf.parse(tmpShapeStr);
                        log.debug("parsed cutout.");
                    } catch (IllegalArgumentException ill) {
                        log.debug("parsing error for cutout: " + tmpShapeStr);
                        throw new DownloadTupleParsingException("pos cutout parsing error: " + tmpPosStr, ill);
                    } catch (Exception e) {
                        log.debug("other error for parsing pos cutout:", e);
                        throw new DownloadTupleParsingException("BUG for pos cutout:" + e + ": " + tmpPosStr, e);
                    }
                }
            }

            String tmpBandStr = tupleParts[2];
            if (StringUtil.hasLength(tmpBandStr)) {
                // trim off trailing "}"
                String tmpShapeStr = tmpBandStr.substring(0, tmpBandStr.length() - 1);
                log.debug("cutout string: " + tmpShapeStr);
                if (StringUtil.hasLength(tmpShapeStr)) {
                    try {
                        DoubleIntervalFormat dif = new DoubleIntervalFormat();
                        newTuple.bandCutout = dif.parse(tmpShapeStr);
                        log.debug("parsed band cutout.");
                    } catch (IllegalArgumentException ill) {
                        log.debug("parsing error for band cutout: " + tmpBandStr, ill);
                        throw new DownloadTupleParsingException("band cutout parsing error: " + tmpBandStr, ill);
                    } catch (Exception e) {
                        log.debug("other error for parsing band cutout:", e);
                        throw new DownloadTupleParsingException("BUG for band cutout:" + tmpBandStr, e);
                    }
                }
            }

            // grab optional third [2] parameter as label
            String l = tupleParts[3];
            if (l.length() > 1) {
                if (newTuple.posCutout == null) {
                    throw new DownloadTupleParsingException("no cutout defined with label");
                }
                // trim off trailing "}".
                String tmpLabel = l.substring(0, l.length() - 1);
                if (StringUtil.hasLength(tmpLabel)) {
                    newTuple.label = convertLabelText(tmpLabel);
                }

            }
        }

        return newTuple;
    }

    private int getCount(final String input, final String regexp) {
        final Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(input);

        int count = 0;
        while (m.find()) {
            count++;
        }
        log.debug("found " + count + " instances of " + regexp + " in " + input);
        return count;
    }

    /**
     * Output DownloadTuple in internal format
     * @param tuple to format
     * @return string representation of tuple
     */
    public String format(DownloadTuple tuple) {
        StringBuilder sb = new StringBuilder();
        sb.append(tuple.getID().toString());

        sb.append("{");
        if (tuple.posCutout != null) {
            ShapeFormat sf = new ShapeFormat();
            sb.append(sf.format(tuple.posCutout));
        }
        sb.append("}{");
        if (tuple.bandCutout != null) {
            DoubleIntervalFormat dif = new DoubleIntervalFormat();
            sb.append(dif.format(tuple.bandCutout));
        }
        sb.append("}{");
        if (StringUtil.hasLength(tuple.label)) {
            sb.append(tuple.label);
        }
        sb.append("}");

        return sb.toString();
    }

    /**
     * Put a set of strings into the internal format before parsing into DownloadTuple.
     * Note: this code can be used at the tail end of parsing JSON input, or other blob-type data
     * that is provided in String format. Use this function to leverage the validation code found
     * in parse(internal_format_string).
     * @param uriStr string representation of URI for download
     * @param posCutoutStr (Optional) DALI string representation of cutout
     * @param bandCutoutStr (Optional) label to add to download request
     * @param labelStr (Optional) label to add to download request
     * @return DownloadTuple populated with tuple information provided.
     * @throws DownloadTupleParsingException if tuple is malformed or content is invalid
     */
    public DownloadTuple parseUsingInternalFormat(String uriStr, String posCutoutStr,
                                                  String bandCutoutStr, String labelStr)
        throws DownloadTupleParsingException {
        // build up a full tuple of format:
        // URI{pos DALI string}{band DALI string}{SODA filename label}
        // where only the URI is required
        // ie: uri{}{}{} is a possible output
        StringBuilder sb = new StringBuilder();
        sb.append(uriStr);
        sb.append("{");

        if (StringUtil.hasLength(posCutoutStr)) {
            sb.append(posCutoutStr);
        }
        sb.append("}{");
        if (StringUtil.hasLength(bandCutoutStr)) {
            sb.append(bandCutoutStr);
        }
        sb.append("}{");
        if (StringUtil.hasLength(labelStr)) {
            sb.append(labelStr);
        }
        sb.append("}");
        return parse(sb.toString());
    }

    /**
     * Create a DownloadTuple using a URI string and a cutout string.
     * @param uriStr URI for download
     * @param pixelCutoutStr pixel cutout to apply to download
     * @return DownloadTuple populated with URI and cutout.
     * @throws DownloadTupleParsingException if URI is invalid
     */
    public DownloadTuple parsePixelStringTuple(String uriStr, String pixelCutoutStr) throws DownloadTupleParsingException  {
        try {
            URI tmpURI = new URI(uriStr);
            DownloadTuple newTuple = new DownloadTuple(tmpURI);
            newTuple.pixelCutout = pixelCutoutStr;
            return newTuple;
        } catch (URISyntaxException uriEx)  {
            throw new DownloadTupleParsingException("invalid id for tuple:", uriEx);
        }
    }

    /**
     * Validate a band (interval) cutout string. To be used in setting DownloadTuple bandCutout.
     * @param bandCutoutStr DALI DoubleInterval string
     * @return DoubleInterval populated with doubles from input string
     * @throws DownloadTupleParsingException if DoubleInterval is invalid
     */
    public DoubleInterval parseBandCutout(String bandCutoutStr) throws DownloadTupleParsingException  {
        if (StringUtil.hasLength(bandCutoutStr)) {
            try {
                DoubleIntervalFormat dif = new DoubleIntervalFormat();
                DoubleInterval bandCutout = dif.parse(bandCutoutStr);
                return bandCutout;
            } catch (IllegalArgumentException argEx) {
                throw new DownloadTupleParsingException("invalid band cutout:" + bandCutoutStr, argEx);
            }
        }
        return null;
    }

    /**
     * Validate a position cutout string. To be used in setting DownloadTuple posCutout.
     * @param posCutoutStr DALI Shape string
     * @return Shape populated with values from input string
     * @throws DownloadTupleParsingException if Shape is invalid
     */
    public Shape parsePosCutout(String posCutoutStr) throws DownloadTupleParsingException  {
        if (StringUtil.hasLength(posCutoutStr)) {
            try {
                ShapeFormat sf = new ShapeFormat();
                Shape posCutout = sf.parse(posCutoutStr);
                return posCutout;
            } catch (IllegalArgumentException argEx)  {
                throw new DownloadTupleParsingException("invalid pos cutout:" + posCutoutStr, argEx);
            }
        }
        return null;
    }

    /**
     * Convert a string to a format that can be used for subsequent web service calls.
     * @param labelText String of label to be converted
     * @return label converted to format acceptable for web service calls
     */
    public static String convertLabelText(String labelText) {
        // Note: this works primarily
        String convertedLabel = labelText;

        if (StringUtil.hasLength(labelText)) {
            log.debug("label to convert: " + convertedLabel);

            // Rules from user story CADC-1245, subtask CADC 8244
            // '/' added because SODA service (caom2ops) does not accept it.
            // 1) ' -> arcmin
            // 2) " -> arcsec
            // 3) '+' -> 'p'
            // 4) ':' and '/' -> '_'
            // 5) all whitespaces replaced by underscores.

            convertedLabel = convertedLabel.replaceAll("'", "arcmin");
            log.debug("after arcmin substitution: " + convertedLabel);

            convertedLabel = convertedLabel.replaceAll("\"", "arcsec");
            log.debug("after arcsec substitution: " + convertedLabel);

            // This section may get expanded as failures are discovered
            // in the broader context of how this label is used.
            convertedLabel = convertedLabel.replaceAll("\\:|\\/", "_");
            convertedLabel = convertedLabel.replaceAll("\\+", "p");
            log.debug("after : and + substitution: " + convertedLabel);

            // put '_' in place of whitespace last
            convertedLabel = convertedLabel.replaceAll("\\s+", "_");
            log.debug("after whitespace substitution: " + convertedLabel);
        }

        return convertedLabel;
    }

}
