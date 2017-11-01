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
import ca.nrc.cadc.dlm.DownloadDescriptor;
import ca.nrc.cadc.dlm.DownloadGenerator;
import ca.nrc.cadc.dlm.FailIterator;
import ca.nrc.cadc.dlm.ManifestReader;
import ca.nrc.cadc.net.HttpDownload;
import ca.nrc.cadc.net.NetUtil;
import ca.nrc.cadc.reg.Standards;
import ca.nrc.cadc.reg.client.RegistryClient;
import ca.nrc.cadc.vos.VOSURI;
import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * Download generator for VOSpace identifiers. This implementation uses a custom
 * VOSpace view (view=manifest). This feature is implemented by the CANFAR VOSpace
 * code and allows clients to get both data nodes and container nodes in a simple fashion.
 *
 * @author pdowler
 */
public class VOSDownloadGenerator implements DownloadGenerator {
    private static final Logger log = Logger.getLogger(VOSDownloadGenerator.class);

    private RegistryClient regClient;
    private String runID;
    private String forceAuthMethod;

    public VOSDownloadGenerator() {
        this(new RegistryClient());
    }

    public VOSDownloadGenerator(RegistryClient rc) {
        this.regClient = rc;
    }

    public void setParameters(Map<String, List<String>> params) {
        List<String> val = params.get("runid");
        if (val != null && !val.isEmpty()) {
            this.runID = val.get(0);
        }
        val = params.get("auth");
        if (val != null && !val.isEmpty()) {
            this.forceAuthMethod = val.get(0);
        }
        log.debug("force auth method: " + forceAuthMethod);
    }

    public Iterator<DownloadDescriptor> downloadIterator(URI uri) {
        try {
            log.debug("downloadIterator: " + uri);
            VOSURI vos = new VOSURI(uri);

            StringBuilder sb = new StringBuilder();
            sb.append(vos.getPath());
            sb.append("?view=manifest");

            if (forceAuthMethod != null) {
                sb.append("&auth=").append(forceAuthMethod);
            }

            if (runID != null) {
                sb.append("&runid=").append(NetUtil.encode(runID));
            }

            // VOSPACE_NODES_20 doesn't support AuthMethod.COOKIE
            URI resourceID = vos.getServiceURI();
            URL serviceUrl = regClient.getServiceURL(
                resourceID, Standards.VOSPACE_NODES_20, AuthMethod.COOKIE);

            URL url = new URL(serviceUrl.toExternalForm() + sb.toString());
            log.debug("resolved URL: " + url + " auth: " + AuthMethod.COOKIE);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HttpDownload get = new HttpDownload(url, bos);
            get.run();

            Throwable t1 = get.getThrowable(); // report this fail if password fails
            if (get.getThrowable() != null) {
                if (get.getResponseCode() == 401 // no cookie or cookie invalid
                    || get.getResponseCode() == 403) { // no cookie and private node
                    // this will only actually work in the webstart application since
                    // it can prompt for a password
                    serviceUrl = regClient.getServiceURL(
                        resourceID, Standards.VOSPACE_NODES_20, AuthMethod.PASSWORD);
                    url = new URL(serviceUrl.toExternalForm() + sb.toString());
                    log.debug("resolved URL: " + url + " auth: " + AuthMethod.PASSWORD);
                    bos = new ByteArrayOutputStream();
                    get = new HttpDownload(url, bos);
                    get.run();
                    if (get.getThrowable() == null) {
                        t1 = null; // recovered from previous fail
                    }
                }
            }
            if (t1 != null) { // fail + possible retry fail: report first fail
                return new FailIterator(uri, "failed to resolve URI: " + t1.getMessage());
            }

            if (ManifestReader.CONTENT_TYPE.equals(get.getContentType())) { // view=manifest
                String responseContent = bos.toString("UTF-8");
                log.debug("response from " + url + "\n" + responseContent);
                ManifestReader r = new ManifestReader();
                return r.read(responseContent);
            }

            return new FailIterator(uri, "unable to download " + uri
                + " with view=manifest [" + get.getResponseCode() + ", " + get.getContentType() + "]");
        } catch (MalformedURLException bug) {
            log.error("failed to read DataLink result table", bug);
            throw new RuntimeException("BUG: failed to create DataLink query URL: " + bug);
        } catch (Exception ex) {
            log.debug("failed to read DataLink result table", ex);
            return new FailIterator(uri, "failed to read output from view=manifest: " + ex.toString());
        }
    }
}
