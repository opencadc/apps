/*
 ************************************************************************
 *******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
 **************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
 *
 *  (c) 2011, 2020                       (c) 2011, 2020
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

package ca.nrc.cadc.dlm;

import ca.nrc.cadc.net.StorageResolver;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;


/**
 * @author pdowler
 */
public class TestStorageResolver implements DownloadGenerator, StorageResolver {
    private static final String SCHEME = "test";

    private String runID;

    /**
     * Returns the scheme for the storage resolver.
     *
     * @return a String representing the schema.
     */
    @Override
    public String getScheme() {
        return SCHEME;
    }

    @Override
    public void setRunID(String runid) {
        this.runID = runid;
    }

    public Iterator<DownloadDescriptor> downloadIterator(DownloadTuple dt) {
        if (!"test".equals(dt.getID().getScheme())) {
            throw new IllegalArgumentException("invalid scheme: " + dt.getID().getScheme());
        }

        URI uri = dt.getID();
        StringBuilder sb = new StringBuilder();
        sb.append("http://");
        sb.append(uri.getHost());
        sb.append(uri.getPath());

        // TODO: params used to be managed here in a way that would allow any iterator
        // to be handled in a test. Not certain that will still work.
        String surl = sb.toString();

        try {
            return new SingleDownloadIterator(dt, new URL(surl));
        } catch (MalformedURLException ex) {
            throw new RuntimeException("BUG: invalid url: " + surl, ex);
        }
    }

    /**
     * Convert the specified URI to one or more URL(s).
     *
     * @param uri the URI to convert
     * @return a URL to the identified resource
     * @throws IllegalArgumentException if the scheme is not equal to the value from getScheme()
     *                                  the uri is malformed such that a URL cannot be generated, or the uri is null
     */
    @Override
    public URL toURL(URI uri) throws IllegalArgumentException {
        if (!"test".equals(uri.getScheme())) {
            throw new IllegalArgumentException("invalid scheme: " + uri.getScheme());
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("http://");
        sb.append(uri.getHost());
        sb.append(uri.getPath());

        if (uri.getFragment() != null) {
            sb.append("?").append(uri.getFragment());
        }

        final String surl = sb.toString();

        try {
            return new URL(surl);
        } catch (MalformedURLException ex) {
            throw new RuntimeException("invalid URL: " + surl, ex);
        }
    }

}
