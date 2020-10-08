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
import ca.nrc.cadc.dali.util.ShapeFormat;
import ca.nrc.cadc.util.StringUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

public class DownloadTupleFormat {
    private static Logger log = Logger.getLogger(DownloadTupleFormat.class);


    /**
     * Parse DownloadTuple from internal format string.
     * @param tupleStr String representing a tuple
     */
    public DownloadTuple parse(String tupleStr) throws DownloadTupleParsingException {
        log.debug("tuple string input: " + tupleStr);
        // Do a rough match of number of '{' to '}'
        // This doesn't check for order or overall format
        int openBraceCount = getCount(tupleStr, "\\{");
        int closeBraceCount = getCount(tupleStr, "\\}");

        log.debug("found count of open & close braces : " + openBraceCount + ", " + closeBraceCount);

        if (openBraceCount != closeBraceCount) {
            throw new DownloadTupleParsingException("invalid tuple format: mismatched braces ': " + tupleStr);
        }

        if (openBraceCount > 2) {
            throw new DownloadTupleParsingException("invalid tuple format: too many braces ': " + tupleStr);
        }

        // Split out the tuple parts
        String [] tupleParts = tupleStr.split("\\{");
        String tmpTupleID;
        String tmpLabel;

        if (tupleParts.length > 3) {
            throw new DownloadTupleParsingException("tuple has too many parts. expected max 3 and found "
                + tupleParts.length + ": " + tupleStr);
        }
        log.debug("tuple parts count: " + tupleParts.length);

        // Get any label that might be there
        if (tupleParts.length == 3) {
            // grab optional third [2] parameter as label
            String l = tupleParts[2];
            if (l.length() > 1) {
                // trim off trailing "}".
                // guaranteed to be there due to
                // check for equal occurrences of { and } above.
                tmpLabel = l.substring(0, l.length() - 1);
            } else {
                // invalid format
                throw new DownloadTupleParsingException("invalid label format: " + tupleStr);
            }
        } else {
            tmpLabel = null;
        }

        Shape tmpShape = null;
        // Get any cutout that might be there
        if (tupleParts.length > 1) {
            String sd = tupleParts[1];
            if (sd.length() > 1) {
                // trim off trailing "}"
                // guaranteed to be there due to
                // check for equal occurrences of { and } above.
                String tmpShapeStr = sd.substring(0, sd.length() - 1);
                log.debug("cutout string: " + tmpShapeStr);
                if (StringUtil.hasLength(tmpShapeStr)) {
                    try {
                        ShapeFormat sf = new ShapeFormat();
                        tmpShape = sf.parse(tmpShapeStr);
                        log.debug("parsed cutout.");
                    } catch (IllegalArgumentException ill) {
                        log.debug("parsing error for cutout: " + tmpShapeStr);
                        throw new DownloadTupleParsingException("cutout parsing error: " + ill + ": " + tupleStr);
                    } catch (Exception e) {
                        log.debug("other error for parsing cutout:" + e);
                        throw new DownloadTupleParsingException("BUG for cutout:" + e + ": " + tupleStr);
                    }
                }
            } else {
                // invalid format
                throw new DownloadTupleParsingException("invalid cutout: " + tupleStr);
            }
        } else {
            tmpShape = null;
        }

        // Get tuple URI - should at least have this.
        URI tmpURI = null;
        String uriStr = tupleParts[0];
        log.debug("uri given: " + uriStr);
        if (StringUtil.hasLength(uriStr)) {
            try {
                tmpURI = new URI(uriStr);
            } catch (URISyntaxException u) {
                throw new DownloadTupleParsingException("id parsing error: " + u + ": " + tupleStr);
            }
        } else {
            // invalid format - has to at least be a single URI passed in
            throw new DownloadTupleParsingException("zero length id found: " + tupleStr);
        }

        return new DownloadTuple(tmpURI, tmpShape, tmpLabel);
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
     * @param tuple
     * @return
     */
    public String format(DownloadTuple tuple) {
        String tupleStr = tuple.getID().toString();

        if (tuple.cutout != null) {
            ShapeFormat sf = new ShapeFormat();
            tupleStr += "{" + sf.format(tuple.cutout) + "}";
        }

        if (StringUtil.hasLength(tuple.label)) {
            tupleStr += "{" + tuple.label + "}";
        }

        return tupleStr;
    }

    /**
     * Put a set of strings into the internalformat before parsing into DownloadTuple.
     * Note: this code can be used at the tail end of parsing JSON input, or other blob-type data
     * that is provided in String format. Use this function to leverage the validation code found
     * in parse(internal_format_string).
     * @param part1
     * @param part2
     * @param part3
     * @return
     * @throws DownloadTupleParsingException
     */
    public DownloadTuple parseUsingInternalFormat(String part1, String part2, String part3) throws DownloadTupleParsingException {
        String tupleStr = part1;
        if (StringUtil.hasLength(part2)) {
            tupleStr += "{" + part2 + "}";
        }
        if (StringUtil.hasLength(part3)) {
            tupleStr += "{" + part3 + "}";
        }
        return parse(tupleStr);
    }

}
