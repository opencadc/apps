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
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ScriptGeneratorTest {
    @Test
    public void testErrorGenerate() {
        try {
            new ScriptGenerator(null, null, null);
            Assert.fail("Should throw IllegalArgumentException.");
        } catch (IllegalArgumentException illegalArgumentException) {
            // Good
            Assert.assertEquals("Wrong error message.", "The DownloadDescriptor iterator is required.",
                                illegalArgumentException.getMessage());
        }

        try {
            new ScriptGenerator(Collections.emptyIterator(), "TOKEN", null);
            Assert.fail("Should throw IllegalArgumentException.");
        } catch (IllegalArgumentException illegalArgumentException) {
            // Good
            Assert.assertEquals("Wrong error message.", "The Expiry Date is required when the token is supplied.",
                                illegalArgumentException.getMessage());
        }

        try {
            new ScriptGenerator(Collections.emptyIterator(), "TOKEN", new Date());
            Assert.fail("Should throw IllegalArgumentException.");
        } catch (IllegalArgumentException illegalArgumentException) {
            // Good
            Assert.assertEquals("Wrong error message.", "The DownloadDescriptor iterator is required.",
                                illegalArgumentException.getMessage());
        }
    }

    @Test
    public void testAnonGenerate() throws Exception {
        final File expectedScript =
                FileUtil.getFileFromResource("cadc-download-expected-anon.sh", ScriptGeneratorTest.class);
        final String expectedScriptString = new String(FileUtil.readFile(expectedScript));

        final List<DownloadDescriptor> urls = new ArrayList<>();
        urls.add(new DownloadDescriptor("test:/file1", new URL("https://mysite.com/download/file1.fits")));
        urls.add(new DownloadDescriptor("test:/file2", new URL("https://mysite.com/download/file2.fits")));
        urls.add(new DownloadDescriptor("test:/broken", "No such file."));
        urls.add(new DownloadDescriptor("test:/file3", new URL("https://mysite.com/download/file3.fits")));
        urls.add(new DownloadDescriptor("test:/file4", new URL("https://mysite.com/download/file4.fits")));
        final ScriptGenerator scriptGenerator = new ScriptGenerator(urls.iterator());

        try (final Writer writer = new StringWriter()) {
            scriptGenerator.generate(writer);
            writer.flush();

            Assert.assertEquals("Output script does not match.", expectedScriptString, writer.toString());
        }
    }

    @Test
    public void testAuthGenerate() throws Exception {
        final File expectedScript =
                FileUtil.getFileFromResource("cadc-download-expected-auth.sh", ScriptGeneratorTest.class);
        final String expectedScriptString = new String(FileUtil.readFile(expectedScript));
        final Calendar calendar = Calendar.getInstance(DateUtil.UTC);
        calendar.set(1977, Calendar.NOVEMBER, 25, 1, 15, 0);

        final List<DownloadDescriptor> urls = new ArrayList<>();
        urls.add(new DownloadDescriptor(new URL("https://mysite.com/download/proprietary-1.fits")));
        urls.add(new DownloadDescriptor(new URL("https://mysite.com/download/proprietary-2.fits")));
        urls.add(new DownloadDescriptor(new URL("https://mysite.com/download/proprietary-3.fits")));
        urls.add(new DownloadDescriptor(new URL("https://mysite.com/download/proprietary-4.fits")));
        final ScriptGenerator scriptGenerator = new ScriptGenerator(urls.iterator(), "SUPERTOKEN",
                                                                    calendar.getTime());

        try (final Writer writer = new StringWriter()) {
            scriptGenerator.generate(writer);
            writer.flush();

            Assert.assertEquals("Output script does not match.", expectedScriptString, writer.toString());
        }
    }
}
