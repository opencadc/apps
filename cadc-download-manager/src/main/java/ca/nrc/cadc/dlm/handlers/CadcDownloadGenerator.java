/*
 ************************************************************************
 *******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
 **************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
 *
 *  (c) 2023.                            (c) 2023.
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

package ca.nrc.cadc.dlm.handlers;

import ca.nrc.cadc.caom2.artifact.resolvers.CadcResolver;
import ca.nrc.cadc.dali.Circle;
import ca.nrc.cadc.dali.Point;
import ca.nrc.cadc.dali.Polygon;
import ca.nrc.cadc.dali.Range;
import ca.nrc.cadc.dali.Shape;
import ca.nrc.cadc.dali.util.CircleFormat;
import ca.nrc.cadc.dali.util.DoubleIntervalFormat;
import ca.nrc.cadc.dali.util.PointFormat;
import ca.nrc.cadc.dali.util.PolygonFormat;
import ca.nrc.cadc.dali.util.RangeFormat;
import ca.nrc.cadc.dlm.DownloadDescriptor;
import ca.nrc.cadc.dlm.DownloadGenerator;
import ca.nrc.cadc.dlm.DownloadTuple;
import ca.nrc.cadc.dlm.FailIterator;
import ca.nrc.cadc.dlm.SingleDownloadIterator;
import ca.nrc.cadc.net.NetUtil;
import ca.nrc.cadc.util.StringUtil;

import java.net.URL;
import java.util.Iterator;

/**
 * Generator implementation for <code>cadc:</code> URLs that will be resolved to Storage Inventory.
 */
public class CadcDownloadGenerator implements DownloadGenerator {
    protected String runID;

    @Override
    public void setRunID(String runID) {
        this.runID = runID;
    }

    /**
     * Produce an iterator over DownloadDescriptor instances associated with the given DownloadTuple information.
     * This will look up Storage Inventory URLs, and preserve cutouts in the SODA format.
     *
     * @param dt the DownloadTuple to resolve into one or more downloads
     * @return  An Iterator over DownloadDescriptor instances, or a FileIterator in the case of an Exception.
     */
    @Override
    public Iterator<DownloadDescriptor> downloadIterator(DownloadTuple dt) {
        final CadcResolver resolver = new CadcResolver();
        try {
            final URL resolvedURL = resolver.toURL(dt.getID());
            final StringBuilder urlStringBuilder = new StringBuilder(resolvedURL.toString());
            boolean isAppendQueryParameter = false;

            if (StringUtil.hasText(this.runID)) {
                urlStringBuilder.append("?runid=");
                urlStringBuilder.append(this.runID);
                isAppendQueryParameter = true;
            }

            if (StringUtil.hasText(dt.pixelCutout)) {
                urlStringBuilder.append(isAppendQueryParameter ? "&" : "?");
                urlStringBuilder.append("SUB=").append(NetUtil.encode(dt.pixelCutout));
                isAppendQueryParameter = true;
            }

            if (dt.posCutout != null) {
                urlStringBuilder.append(isAppendQueryParameter ? "&" : "?");
                urlStringBuilder.append(dt.posCutout.getClass().getSimpleName().toUpperCase());
                urlStringBuilder.append("=");
                urlStringBuilder.append(NetUtil.encode(format(dt.posCutout)));
                isAppendQueryParameter = true;
            }

            if (dt.bandCutout != null) {
                final DoubleIntervalFormat doubleIntervalFormat = new DoubleIntervalFormat();
                urlStringBuilder.append(isAppendQueryParameter ? "&" : "?");
                urlStringBuilder.append("BAND=");
                urlStringBuilder.append(NetUtil.encode(doubleIntervalFormat.format(dt.bandCutout)));
            }

            return new SingleDownloadIterator(new DownloadTuple(dt.getID()), new URL(urlStringBuilder.toString()));
        } catch (Exception ex) {
            return new FailIterator(dt, "failed to resolve URI: " + ex.getMessage());
        }
    }

    private String format(final Shape shape) {
        final String formattedShape;
        if (shape instanceof Point) {
            final PointFormat fmt = new PointFormat();
            formattedShape = fmt.format((Point) shape);
        } else if (shape instanceof Circle) {
            final CircleFormat fmt = new CircleFormat();
            formattedShape = fmt.format((Circle) shape);
        } else if (shape instanceof Range) {
            final RangeFormat fmt = new RangeFormat();
            formattedShape = fmt.format((Range) shape);
        } else {
            if (!(shape instanceof Polygon)) {
                throw new IllegalArgumentException("Unsupported shape: " + shape.getClass().getName());
            }

            final PolygonFormat fmt = new PolygonFormat();
            formattedShape = fmt.format((Polygon) shape);
        }

        return formattedShape;
    }
}
