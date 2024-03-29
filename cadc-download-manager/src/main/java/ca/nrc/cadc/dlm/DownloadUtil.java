/*
 ************************************************************************
 *******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
 **************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
 *
 *  (c) 2009, 2020.                      (c) 2009, 2020.
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
 *  $Revision: 4 $
 *
 ************************************************************************
 */

package ca.nrc.cadc.dlm;

import ca.nrc.cadc.util.CaseInsensitiveStringComparator;
import ca.nrc.cadc.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

/**
 * Miscellaneous methods for use in JSP pages.
 *
 * @author pdowler
 */
public class DownloadUtil {
    public static final String URI_SEPARATOR = " ";
    public static final String PARAM_SEPARATOR = "&";
    private static Logger log = Logger.getLogger(DownloadUtil.class);

    private DownloadUtil() {
    }

    // static classes for return values so we can put list operations in here 
    // and keep fine-grained error handling in app

    public static String encodeListURI(List<String> uriList) {
        StringBuilder uris = new StringBuilder();
        for (String u : uriList) {
            if (uris.length() > 0) {
                uris.append(URI_SEPARATOR);
            }
            uris.append(u);
        }
        return uris.toString();
    }

    public static List<String> decodeListURI(String s) {
        if (!StringUtil.hasText(s)) {
            return new ArrayList<>();
        }
        String[] uris = s.split(URI_SEPARATOR);
        return Arrays.asList(uris);
    }

    public static String encodeParamMap(Map<String, List<String>> paramMap) {
        // separated list if key=value pairs
        StringBuilder params = new StringBuilder();
        for (Map.Entry<String, List<String>> me : paramMap.entrySet()) {
            for (String value : me.getValue()) {
                if (params.length() > 0) {
                    params.append(PARAM_SEPARATOR);
                }
                params.append(me.getKey());
                params.append("=");
                params.append(value);
            }
        }
        return params.toString();
    }

    public static Map<String, List<String>> decodeParamMap(String s) {
        String[] parts = null;
        if (s != null) {
            parts = s.split(PARAM_SEPARATOR);
        }
        return toParamMap(parts);
    }

    private static Map<String, List<String>> toParamMap(String[] params) {
        Map<String, List<String>> paramSet = new TreeMap<>(new CaseInsensitiveStringComparator());
        if (params != null) {
            for (String p : params) {
                log.debug("toParamMap: " + p);
                String[] par = p.split("=");
                if (par.length == 2) {
                    String key = par[0];
                    String val = par[1];
                    List<String> cur = paramSet.get(key);
                    if (cur == null) {
                        cur = new ArrayList<>();
                        paramSet.put(key, cur);
                    }
                    cur.add(val);
                }
            }
        }
        return paramSet;
    }

    /**
     * Generate an Iterator of DownloadDescriptors to be reported to caller. URLs and
     * errors found while generating URLs are included in the DownloadDescriptors.
     * @param downloadRequest includes tuples with URIs to be translated to URLs
     * @return Iterator with DownloadDescriptors
     */
    public static Iterator<DownloadDescriptor> iterateURLs(DownloadRequest downloadRequest) {

        final MultiDownloadGenerator gen = new MultiDownloadGenerator();

        final Set<DownloadTuple> dt = downloadRequest.getTuples();
        gen.setRunID(downloadRequest.runID);

        return new Iterator<DownloadDescriptor>() {
            final Iterator<DownloadTuple> outer = dt.iterator();
            Iterator<DownloadDescriptor> inner = null;

            public boolean hasNext() {
                return (inner != null || outer.hasNext());
            }

            public DownloadDescriptor next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                if (inner != null) {
                    DownloadDescriptor dd = inner.next();
                    if (!inner.hasNext()) {
                        inner = null;
                    }
                    return dd;
                }

                DownloadTuple cur = outer.next();
                // TODO: parsing errors will need to be handled. This is commented out
                // while code rework for input validation is being done
                // this might take in a download tuple instead of a uri?
                //if (cur.parsingError != null) { // string -> URI fail
                //    return new DownloadDescriptor(cur.tupleIDstr, (cur.parsingError.toString()));
                //}
                try {
                    inner = gen.downloadIterator(cur);
                    if (inner.hasNext()) {
                        return this.next(); // recursive
                    }
                    // inner was empty
                    inner = null;
                    return new DownloadDescriptor(cur.getID().toString(), "no matching files");
                } catch (Throwable t) {
                    return new DownloadDescriptor(cur.getID().toString(), t.toString());
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
