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

package ca.nrc.cadc.dlm.server;

import ca.nrc.cadc.date.DateUtil;
import ca.nrc.cadc.dlm.DownloadDescriptor;
import ca.nrc.cadc.util.FileUtil;
import ca.nrc.cadc.util.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * Generate a Bash script with the Download URLs.
 */
class ScriptGenerator {
    private static final String NEWLINE = "\n";
    private static final String AUTH_SCRIPT_TEMPLATE_FILENAME = "cadc-download-auth-template.sh";
    private static final String ANON_SCRIPT_TEMPLATE_FILENAME = "cadc-download-anon-template.sh";
    private static final String VARIABLE_REPLACE = "%%%";
    private static final String VARIABLE_EXPIRY_REPLACE =
            ScriptGenerator.VARIABLE_REPLACE + "EXPIRY" + ScriptGenerator.VARIABLE_REPLACE;
    private static final String VARIABLE_TOKEN_REPLACE =
            ScriptGenerator.VARIABLE_REPLACE + "TOKEN" + ScriptGenerator.VARIABLE_REPLACE;
    private static final String VARIABLE_URLS_REPLACE =
            ScriptGenerator.VARIABLE_REPLACE + "URLS" + ScriptGenerator.VARIABLE_REPLACE;

    private static final String VARIABLE_ERROR_URIS_REPLACE =
            ScriptGenerator.VARIABLE_REPLACE + "ERRORURIS" + ScriptGenerator.VARIABLE_REPLACE;
    private static final String ERROR_URI_MESSAGE_DELIMINATOR = "|||";

    private final Iterator<DownloadDescriptor> downloadDescriptors;
    private final String authToken;
    private final Date expiryDate;


    ScriptGenerator(final Iterator<DownloadDescriptor> downloadDescriptors) {
        this(downloadDescriptors, null, null);
    }

    ScriptGenerator(final Iterator<DownloadDescriptor> downloadDescriptors, final String authToken,
                    final Date expiryDate) {
        this.downloadDescriptors = downloadDescriptors;
        this.authToken = authToken;
        this.expiryDate = expiryDate;

        // If the auth token was provided, the expiry date should be provided as well.
        if (this.expiryDate == null && StringUtil.hasLength(this.authToken)) {
            throw new IllegalArgumentException("The Expiry Date is required when the token is supplied.");
        } else if (this.downloadDescriptors == null || !this.downloadDescriptors.hasNext()) {
            throw new IllegalArgumentException("The DownloadDescriptor iterator is required.");
        }
    }

    /**
     * Determine the appropriate template script file, and write out the string template variable values.
     * @param writer        The Writer to send data to.
     * @throws IOException  If any I/O problems occur.
     */
    void generate(final Writer writer) throws IOException {
        final String templateFile = StringUtil.hasLength(this.authToken)
                                    ? ScriptGenerator.AUTH_SCRIPT_TEMPLATE_FILENAME
                                    : ScriptGenerator.ANON_SCRIPT_TEMPLATE_FILENAME;
        final URL templateFileURL = FileUtil.getURLFromResource(templateFile, ScriptGenerator.class);
        try (final InputStream inputStream = templateFileURL.openStream()) {
            final Reader inputStreamReader = new InputStreamReader(inputStream);
            final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            final List<DownloadDescriptor> errorDownloadDescriptors = new ArrayList<>();
            final List<DownloadDescriptor> successDownloadDescriptors = new ArrayList<>();

            // Separate the successful Downloads from the errors.
            this.downloadDescriptors.forEachRemaining(downloadDescriptor -> {
                if (downloadDescriptor.url == null) {
                    errorDownloadDescriptors.add(downloadDescriptor);
                } else {
                    successDownloadDescriptors.add(downloadDescriptor);
                }
            });

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains(ScriptGenerator.VARIABLE_REPLACE)) {
                    if (line.contains(ScriptGenerator.VARIABLE_EXPIRY_REPLACE)) {
                        writer.write(line.replace(ScriptGenerator.VARIABLE_EXPIRY_REPLACE,
                                                  DateUtil.getDateFormat(DateUtil.ISO8601_DATE_FORMAT_LOCAL, DateUtil.UTC)
                                                          .format(this.expiryDate)));
                    } else if (line.contains(ScriptGenerator.VARIABLE_TOKEN_REPLACE)) {
                        writer.write(line.replace(ScriptGenerator.VARIABLE_TOKEN_REPLACE, this.authToken));
                    } else if (line.contains(ScriptGenerator.VARIABLE_URLS_REPLACE)) {
                        for (final Iterator<DownloadDescriptor> downloadDescriptorIterator
                             = successDownloadDescriptors.iterator(); downloadDescriptorIterator.hasNext(); ) {
                            final DownloadDescriptor downloadDescriptor = downloadDescriptorIterator.next();
                            writer.write("\"" + downloadDescriptor.url + "\"");
                            if (downloadDescriptorIterator.hasNext()) {
                                writer.write(ScriptGenerator.NEWLINE);
                            }
                        }
                    } else if (line.contains(ScriptGenerator.VARIABLE_ERROR_URIS_REPLACE)) {
                        // Error URIs are written as the URI|||Error Message.
                        for (final Iterator<DownloadDescriptor> downloadDescriptorIterator
                             = errorDownloadDescriptors.iterator(); downloadDescriptorIterator.hasNext(); ) {
                            final DownloadDescriptor downloadDescriptor = downloadDescriptorIterator.next();
                            writer.write("\"" + downloadDescriptor.uri + ScriptGenerator.ERROR_URI_MESSAGE_DELIMINATOR
                                         + downloadDescriptor.error + "\"");
                            if (downloadDescriptorIterator.hasNext()) {
                                writer.write(ScriptGenerator.NEWLINE);
                            }
                        }
                    }
                    writer.write(ScriptGenerator.NEWLINE);
                } else {
                    writer.write(line);
                    writer.write(ScriptGenerator.NEWLINE);
                }
            }
        }
    }
}
