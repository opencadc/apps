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
import java.security.InvalidParameterException;
import org.apache.log4j.Logger;

public class DownloadTuple {
    private static Logger log = Logger.getLogger(DownloadTuple.class);

    private final URI tupleID;
    public Shape cutout;
    public String label;

    /**
     * ctor
     * @param tupleID
     */
    public DownloadTuple(URI tupleID) {
        this.tupleID = tupleID;
        this.cutout = null;
        this.label = null;
    }

    /**
     * ctor
     * @param tupleID
     * @param cutout
     */
    public DownloadTuple(URI tupleID, Shape cutout) {
        this.tupleID = tupleID;
        this.cutout = cutout;
        this.label = null;
    }

    /**
     * ctor
     * @param tupleID
     * @param cutout
     * @param label
     */
    public DownloadTuple(URI tupleID, Shape cutout, String label) {
        this.tupleID = tupleID;
        this.cutout = cutout;
        this.label = label;
    }

    /**
     * Convenience ctor to parse from internal format string. Will also
     * parse URI strings only.
     * @param tupleStr String representing a tuple
     */
    public DownloadTuple (String tupleStr) {
        log.info("tuple string input: " + tupleStr);

        String [] tupleParts = tupleStr.split("\\{");
        String tmpTupleID;
        String tmpLabel;
        URI tmpURI = null;
        Shape tmpShape = null;

        if (tupleParts.length > 3) {
            throw new InvalidParameterException("tuple has too many parts '{..}': " + tupleStr);
        }

        // Get any label that might be there
        if (tupleParts.length == 3) {
            // grab optional third [2] parameter as label
            String l = tupleParts[2];
            if (l.length() > 1) {
                // trim off trailing "}"
                tmpLabel = l.substring(0, l.length() - 1);
            } else {
                // invalid format
                throw new InvalidParameterException("Invalid label format: " + tupleStr);
            }
        } else {
            tmpLabel = null;
        }

        // Get any shape that might be there
        if (tupleParts.length > 1) {
            String sd = tupleParts[1];
            if (sd.length() > 1) {
                // trim off trailing "}"
                String tmpShapeStr = sd.substring(0, sd.length() - 1);
                if (StringUtil.hasLength(tmpShapeStr)) {
                    try {
                        ShapeFormat sf = new ShapeFormat();
                        tmpShape = sf.parse(tmpShapeStr);
                    } catch (IllegalArgumentException ill) {
                        log.debug("parsing error for shape: " + tmpShapeStr);
                        // TODO: throw this error?
//                        validationErrors.add(ill);
                    }
                }
            } else {
                // invalid format
                throw new InvalidParameterException("invalid shape descriptor: " + tupleStr);
            }
        } else {
            tmpShape = null;
        }

        // Get tuple URI - should at least have this.
        String uriStr = tupleParts[0];
        if (StringUtil.hasLength(uriStr)) {
            tmpTupleID = uriStr;
        } else {
            // invalid format - has to at least be a single URI passed in
            throw new InvalidParameterException("missing tupleID: " + tupleStr);
        }

        if (!StringUtil.hasLength(tmpTupleID)) {
            throw new InvalidParameterException("missing tupleID: " + tupleStr);
        } else {
            try {
                tmpURI = new URI(tmpTupleID);
            } catch (URISyntaxException u) {
                // TODO: throw this error instead?
//                validationErrors.add(u);
            }
            this.label = label;
        }

        // assign values
        this.tupleID = tmpURI;
        this.cutout = tmpShape;
        this.label = tmpLabel;
    }

    public String toInternalFormat() {
        String tupleStr = tupleID.toString();

        if (cutout != null) {
            ShapeFormat sf = new ShapeFormat();
            tupleStr += "{" + sf.format(this.cutout) + "}";
        }

        if (StringUtil.hasLength(label)) {
            tupleStr += "{" + label + "}";
        }

        return tupleStr;
    }

    public URI getTupleID() {
        return tupleID;
    }
}
