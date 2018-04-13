/*
************************************************************************
*******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
**************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
*
*  (c) 2011.                            (c) 2011.
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

package ca.nrc.cadc.dlm.handlers;

import ca.nrc.cadc.auth.AuthMethod;
import ca.nrc.cadc.auth.AuthenticationUtil;
import ca.nrc.cadc.dali.tables.votable.VOTableDocument;
import ca.nrc.cadc.dali.tables.votable.VOTableField;
import ca.nrc.cadc.dali.tables.votable.VOTableGroup;
import ca.nrc.cadc.dali.tables.votable.VOTableParam;
import ca.nrc.cadc.dali.tables.votable.VOTableReader;
import ca.nrc.cadc.dali.tables.votable.VOTableResource;
import ca.nrc.cadc.dali.tables.votable.VOTableTable;
import ca.nrc.cadc.dali.util.DoubleArrayFormat;
import ca.nrc.cadc.dlm.DownloadDescriptor;
import ca.nrc.cadc.dlm.DownloadGenerator;
import ca.nrc.cadc.dlm.FailIterator;
import ca.nrc.cadc.net.HttpDownload;
import ca.nrc.cadc.net.NetUtil;
import ca.nrc.cadc.reg.Standards;
import ca.nrc.cadc.reg.client.RegistryClient;
import ca.nrc.cadc.util.StringUtil;
import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.apache.log4j.Logger;

/**
 * Wrapper to interface a DataLink web service with DownloadManager. This implementation
 * is resolves URIs with the <em>caom</em> and <em>ivo</em> schemes to a single DataLink
 * service (by default, the CADC service <code>ivo://cadc.nrc.ca/caom2ops/datalink</code>).
 *
 * @author pdowler
 */
public class DataLinkClient implements DownloadGenerator {
    public static final String RESOURCE_ID_PROP = DataLinkClient.class.getName() + ".resourceID";
    public static final String CUTOUT = "#cutout";
    private static final Logger log = Logger.getLogger(DataLinkClient.class);
    private static final String DOWNLOAD_REQUEST = "downloads-only";

    private static final String COL_NAME_URI = "ID";
    private static final String COL_NAME_URL = "access_url";
    private static final String COL_NAME_SERVICE_DEF = "service_def";
    private static final String COL_NAME_ERR_MSG = "error_message";
    private static final String COL_NAME_SEMANTICS = "semantics";
    private final RegistryClient regClient;
    private final DataLinkServiceResolver resolver;
    // Package private for tests.
    String runID;
    String cutout;
    boolean downloadOnly = false;
    String requestFail;
    
    private final List<String> skipSemantics = new ArrayList<>();

    public DataLinkClient() {
        this.regClient = new RegistryClient();
        this.resolver = new DataLinkServiceResolver();
        skipSemantics.add("#preview");
        skipSemantics.add("http://www.openadc.org/caom2#thumbnail");
        skipSemantics.add("http://www.openadc.org/caom2#pkg");
    }

    @Override
    public void setParameters(Map<String, List<String>> params) {
        List<String> val;

        val = params.get("runid");
        if (val != null && !val.isEmpty()) {
            this.runID = val.get(0);
        }

        val = params.get("cutout");
        if (val != null && !val.isEmpty()) {
            // transform to SODA CIRCLE or POLYGON param
            String str = val.get(0); // first only

            // strip off old STC-S pre amble and just get the numeric values
            String[] tokens = str.split(" ");
            List<Double> dvals = new ArrayList<>();
            for (String t : tokens) {
                try {
                    Double d = new Double(t);
                    dvals.add(d);
                } catch (NumberFormatException ex) {
                    log.debug("ignoring token in cutout: " + t);
                }
            }
            DoubleArrayFormat daf = new DoubleArrayFormat();
            if (dvals.size() == 3) {
                this.cutout = "CIRCLE=" + NetUtil.encode(daf.format(dvals.iterator()));
            } else if (dvals.size() >= 6) {
                this.cutout = "POLYGON=" + NetUtil.encode(daf.format(dvals.iterator()));
            } else if ("spectralinterval".equalsIgnoreCase(tokens[0])) {
                this.cutout = "BAND=" + NetUtil.encode(daf.format(dvals.iterator()));
            } else {
                this.requestFail = "invalid parameter: cutout=" + str;
            }
        }


        if (cutout == null) {
            this.downloadOnly = true;
        }
    }

    @Override
    public Iterator<DownloadDescriptor> downloadIterator(URI uri) {
        if (requestFail != null) {
            return new FailIterator(uri, requestFail);
        }

        try { // query datalink with uri and (for now) filters
            URI resourceID = resolver.getResourceID(uri);
            AuthMethod am = AuthenticationUtil.getAuthMethod(AuthenticationUtil.getCurrentSubject());
            if (am == null) {
                am = AuthMethod.ANON;
            }
            URL serviceURL = regClient.getServiceURL(resourceID, Standards.DATALINK_LINKS_10, am);
            if (serviceURL == null) {
                return new FailIterator(uri, "failed to resolve URI: cannot find DataLink service " + resourceID);
            }

            StringBuilder sb = new StringBuilder(serviceURL.toExternalForm());
            sb.append("?id=");
            sb.append(NetUtil.encode(uri.toASCIIString()));

            if (downloadOnly) {
                sb.append("&request=").append(DOWNLOAD_REQUEST); // custom
            }

            if (runID != null) {
                sb.append("&runid=").append(NetUtil.encode(runID));
            }

            URL url = new URL(sb.toString());
            log.debug("datalink: " + url);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HttpDownload get = new HttpDownload(url, bos);
            get.run();
            if (get.getThrowable() != null) {
                return new FailIterator(uri, "failed to resolve URI: " + get.getThrowable().getMessage());
            }

            try {
                String responseContent = bos.toString("UTF-8");
                log.debug("response from " + url + "\n" + responseContent);

                VOTableReader r = new VOTableReader();
                VOTableDocument doc = r.read(responseContent);
                Iterator<DownloadDescriptor> ret = new DownloadIterator(doc);
                return ret;
            } catch (Exception ex) {
                log.debug("failed to read DataLink result table", ex);
                throw new RuntimeException("failed to read DataLink result table: " + ex.getMessage());
            }
        } catch (MalformedURLException bug) {
            log.error("failed to read DataLink result table", bug);
            throw new RuntimeException("BUG: failed to create DataLink query URL: " + bug);
        }
    }

    private int getColumnByName(String name, VOTableTable vot) {
        List<VOTableField> cols = vot.getFields();
        for (int i = 0; i < cols.size(); i++) {
            VOTableField tf = cols.get(i);
            if (tf.getName() != null && tf.getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private class DownloadIterator implements Iterator<DownloadDescriptor> {
        private Iterator<List<Object>> rowIter;
        private int uriIndex;
        private int urlIndex;
        private int sdIndex;
        private int errIndex;
        private int semIndex;
        private int ptIndex;

        private List<Object> curRow;
        private String curParams;

        private Map<String, VOTableResource> serviceDefinitions;
        private Map<String, Integer> inputColumns;

        public DownloadIterator(VOTableDocument doc) {
            VOTableResource res = doc.getResourceByType("results");
            VOTableTable links = res.getTable();

            this.uriIndex = getColumnByName(COL_NAME_URI, links);
            this.urlIndex = getColumnByName(COL_NAME_URL, links);
            this.sdIndex = getColumnByName(COL_NAME_SERVICE_DEF, links);
            this.errIndex = getColumnByName(COL_NAME_ERR_MSG, links);
            this.semIndex = getColumnByName(COL_NAME_SEMANTICS, links);

            if (uriIndex == -1) {
                throw new RuntimeException("cannot find " + COL_NAME_URI + " column in votable");
            }
            if (urlIndex == -1) {
                throw new RuntimeException("cannot find " + COL_NAME_URL + " column in votable");
            }
            if (sdIndex == -1) {
                throw new RuntimeException("cannot find " + COL_NAME_SERVICE_DEF + " column in votable");
            }
            if (semIndex == -1) {
                throw new RuntimeException("cannot find " + COL_NAME_SEMANTICS + " column in votable");
            }

            // pre-process the meta resources so services can be
            // resolved while iterating the table links
            serviceDefinitions = new HashMap<String, VOTableResource>();
            inputColumns = new HashMap<String, Integer>();

            Iterator<VOTableResource> resourceIt = doc.getResources().iterator();
            VOTableResource nextResource = null;
            while (resourceIt.hasNext()) {
                nextResource = resourceIt.next();
                if ("meta".equals(nextResource.getType())
                    && "adhoc:service".equals(nextResource.utype)
                    && nextResource.id != null) {
                    serviceDefinitions.put(nextResource.id, nextResource);
                    log.debug("Added service definition mapping for '" + nextResource.id + "'");
                }
            }

            // next, map any columns that may have an id reference
            for (int i = 0; i < links.getFields().size(); i++) {
                if (links.getFields().get(i).id != null) {
                    if (!inputColumns.containsKey(links.getFields().get(i).id)) {
                        inputColumns.put(links.getFields().get(i).id, Integer.valueOf(i));
                        log.debug("Added input column mapping: "
                            + links.getFields().get(i).id + " -> " + i + "," + links.getFields().get(i));
                    }
                }
            }

            this.rowIter = links.getTableData().iterator();

            advance();
        }

        @Override
        public boolean hasNext() {
            return (curRow != null);
        }

        @Override
        public DownloadDescriptor next() {
            if (curRow == null) {
                throw new NoSuchElementException();
            }

            String uri = (String) curRow.get(uriIndex);
            String serviceDef = (String) curRow.get(sdIndex);
            String errMsg = (String) curRow.get(errIndex);

            try {
                String url = (String) curRow.get(urlIndex);
                if (url == null && serviceDef != null) {
                    // consult the service description in the meta data resources
                    VOTableResource metaResource = serviceDefinitions.get(serviceDef);
                    if (metaResource == null) {
                        return new DownloadDescriptor(uri, "invalid link: service " + serviceDef + " is not described");
                    }

                    url = getServiceProperty(serviceDef, "accessURL");
                    if (url == null) {
                        return new DownloadDescriptor(uri, "invalid link: service " + serviceDef + " has no accessURL parameter");
                    }
                    log.debug("accessURL: " + url);

                    // find any additional service input parameters
                    List<VOTableGroup> groups = metaResource.getGroups();
                    VOTableGroup inputGroup = null;
                    for (VOTableGroup nextGroup : groups) {
                        if (nextGroup.getName().equals("inputParams")) {
                            if (inputGroup != null) {
                                throw new RuntimeException("BUG: more than one inputParams group in resource " + serviceDef);
                            }
                            inputGroup = nextGroup;
                        }
                    }

                    if (inputGroup != null) {
                        List<VOTableParam> params = inputGroup.getParams();
                        for (VOTableParam nextParam : params) {
                            // see if the parameter value is supplied as a column in the
                            // votable results
                            if (nextParam.ref != null) {
                                if (inputColumns.containsKey(nextParam.ref)) {
                                    Integer paramColumn = inputColumns.get(nextParam.ref);
                                    Object paramValue = curRow.get(paramColumn.intValue());
                                    url = appendParam(url, nextParam.getName(), paramValue);
                                }
                            } else if (StringUtil.hasText(nextParam.getValue())) { // value supplied
                                url = appendParam(url, nextParam.getName(), nextParam.getValue());
                            }
                            //else
                            //{
                            // otherwise ensure we have provided the parameter
                            //    if (curParams == null || !curParams.contains(nextParam.getName() + "="))
                            //    {
                            //        return new DownloadDescriptor(uri, "missing required parameter " + nextParam.getName());
                            //    }
                            //}
                        }
                    }
                }

                if (errMsg != null) {
                    return new DownloadDescriptor(uri, errMsg);
                } else if (url == null) {
                    return new DownloadDescriptor(uri, "failed to generate URL");
                } else {
                    try {
                        if (!url.toLowerCase().contains("runid=")) {
                            url = appendParam(url, "runid", runID);
                        }
                        url = appendParams(url, curParams);
                        return new DownloadDescriptor(uri, new URL(url));
                    } catch (MalformedURLException ex) {
                        return new DownloadDescriptor(uri, "invalid URL: " + url);
                    }
                }
            } finally {
                if (curRow != null) {
                    advance();
                }
            }
        }

        private String getServiceProperty(String serviceDef, String paramName) {
            // consult the service description in the meta data resources
            VOTableResource metaResource = serviceDefinitions.get(serviceDef);

            if (metaResource == null) {
                return null;
            }

            String resourceIdentifier = null;
            for (VOTableParam param : metaResource.getParams()) {
                if (param.getName().equals(paramName)) {
                    resourceIdentifier = param.getValue();
                }
            }
            return resourceIdentifier;
        }

        private void advance() {
            curRow = null;
            curParams = null;
            while (curRow == null && rowIter.hasNext()) {
                curRow = rowIter.next();
                String url = (String) curRow.get(urlIndex);
                String serviceDef = (String) curRow.get(sdIndex);
                if (downloadOnly && serviceDef != null) {
                    curRow = null;
                    log.debug("skip: downloadOnly");
                }
                
                // semantics filtering
                if (curRow != null && semIndex >= 0) {
                    String sem = (String) curRow.get(semIndex);
                    String sdef = (String) curRow.get(sdIndex);

                    if (skipSemantics.contains(sem)) {
                        curRow = null;
                        log.debug("skip: " + url + " semantics: " + sem);
                    } else if (CUTOUT.equals(sem)) {
                        String standardID = getServiceProperty(sdef, "standardID");
                        if (Standards.SODA_SYNC_10.toString().equals(standardID)) {
                            curParams = cutout;
                            log.debug("pass: " + url + " semantics: " + sem + " cutout: " + cutout);
                        } else {
                            curRow = null;
                            log.debug("skip: " + url + " standardID: " + standardID + " cutout: " + cutout);
                        }
                    } else if (cutout != null) {
                        curRow = null;
                        log.debug("skip: " + url + " semantics: " + sem + " cutout: " + cutout);
                    } else {
                        log.debug("pass: " + url);
                    }
                }
            }
        }

        private String appendParam(String url, String name, Object value) {
            // TODO: Check the object type to do the appropriate cast or conversion
            //       This currently assumes the value is a string
            if (name == null || value == null) {
                return url;
            }
            if (!(value instanceof String)) {
                return url;
            }
            String param = name + "=" + NetUtil.encode((String) value);
            return appendParams(url, param);
        }

        private String appendParams(String url, String params) {
            if (params == null) {
                return url;
            }

            // HACK: this is what comes from having an opaque service url that might have params already
            if (url.indexOf('?') > 0) { // has query string
                if (!url.endsWith("&")) {
                    url += "&";
                }
            } else {
                url += "?";
            }
            url += params;
            return url;
        }

        private boolean containsAny(List<String> productTypes, String[] values) {
            for (String v : values) {
                if (productTypes.contains(v)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void remove() {
        }
    }

}
