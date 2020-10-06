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

package ca.nrc.cadc.dlm.client;

import ca.nrc.cadc.appkit.ui.Application;
import ca.nrc.cadc.appkit.ui.ApplicationFrame;
import ca.nrc.cadc.auth.AuthMethod;
import ca.nrc.cadc.auth.SSOCookieCredential;
import ca.nrc.cadc.dlm.DownloadRequest;
import ca.nrc.cadc.dlm.DownloadTuple;
import ca.nrc.cadc.dlm.DownloadTupleFormat;
import ca.nrc.cadc.dlm.DownloadTupleParsingException;
import ca.nrc.cadc.dlm.DownloadUtil;
import ca.nrc.cadc.thread.ConditionVar;
import ca.nrc.cadc.util.ArgumentMap;
import ca.nrc.cadc.util.StringUtil;
import java.awt.Component;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.security.auth.Subject;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * TODO
 *
 * @author pdowler
 * @version $Version$
 */
public class Main {
    private static Logger log = Logger.getLogger(Main.class);
    private static UserInterface ui;

    public static void main(final String[] args) {
        try {
            final ArgumentMap am = new ArgumentMap(args);
            if (am.isSet("h") || am.isSet("help")) {
                usage();
                System.exit(0);
            }
            Level level = Level.WARN;
            if (am.isSet("d") || am.isSet("debug")) {
                level = Level.DEBUG;
            } else if (am.isSet("v") || am.isSet("verbose")) {
                level = Level.INFO;
            } else if (am.isSet("q") || am.isSet("quiet")) {
                level = Level.OFF;
            }
            final Level logLevel = level;

            Subject subject = new Subject();

            // Cookie based authentication
            boolean cookieCreds = false;
            String ssoCookieStr = fixNull(am.getValue("ssocookie"));
            if (ssoCookieStr != null) {
                String ssoCookieDomain =
                    fixNull(am.getValue("ssocookiedomain"));
                if (ssoCookieDomain == null) {
                    System.out.println("Missing ssocookiedomain argument...");
                    Main.usage();
                    System.exit(-1);
                }
                final String[] domains = ssoCookieDomain.split(",");
                if (domains.length < 1) {
                    System.out.println("Invalid ssocookiedomain argument: " + ssoCookieDomain);
                    Main.usage();
                    System.exit(-1);
                }
                for (String domain : domains) {
                    SSOCookieCredential cred = new SSOCookieCredential(ssoCookieStr, domain.trim());
                    subject.getPublicCredentials().add(cred);
                    cookieCreds = true;
                }
            }

            final boolean headless = am.isSet("headless");

            String str = null;
            if (!headless && !cookieCreds) {
                str = AuthMethod.PASSWORD.getValue();
            }
            final String forceAuthMethod = str;

            final ConditionVar downloadCompleteCond = new ConditionVar();

            boolean result = Subject.doAs(subject, new PrivilegedAction<Boolean>() {
                public Boolean run() {
                    // TODO: Q: support 'uris' as input to this, when DownloadManager.jsp
                    // hasn't been using it for a while?
                    //String uriStr = fixNull(am.getValue("uris"));

                    DownloadRequest downloadRequest = getDownloadRequest(am);
                    String runIDStr = fixNull(am.getValue("runid"));
                    downloadRequest.runID = runIDStr;

                    // TODO: 'auth' needs to be handled in the new paradigm soon...
                    //                    if (forceAuthMethod != null) {
                    //                        List<String> am = new ArrayList<>();
                    //                        am.add(forceAuthMethod);
                    //                        params.put("auth", am);
                    //                    }
                    if (headless) {
                        boolean decompress = am.isSet("decompress");
                        boolean overwrite = am.isSet("overwrite");
                        String dest = am.getValue("dest");
                        String thStr = am.getValue("threads");
                        boolean retry = am.isSet("retry");
                        Integer threads = null;
                        if (thStr != null) {
                            try {
                                threads = new Integer(thStr);
                            } catch (NumberFormatException ex) {
                                throw new IllegalArgumentException("failed to parse '" + thStr + "' as an integer");
                            }
                        }
                        downloadCompleteCond.set(false);
                        ui = new ConsoleUI(logLevel, threads, retry, dest, decompress, overwrite, downloadCompleteCond);
                    } else {
                        ui = new GraphicUI(logLevel);
                        ApplicationFrame frame = new ApplicationFrame(Constants.name, (Application) ui);
                        frame.getContentPane().add((Component) ui);
                        frame.setVisible(true);
                    }


                    // Input validation messages will be printed in this section
                    ui.add(downloadRequest);
                    if (downloadRequest.getTuples().isEmpty()) {
                        log.info("no tuples to work on, quitting...");
                    } else {
                        ui.start();
                    }
                    return true;
                }
            });

            if (!result) {
                System.err.println("Error occurred during execution..");
            }

            // if running headless, don't exit
            // until the downloads have been completed.
            if (headless) {
                downloadCompleteCond.waitForTrue();
            }
        } catch (IllegalArgumentException ex) {
            System.err.println("fatal error during startup");
            ex.printStackTrace();
            usage();
            System.exit(1);
        } catch (Throwable oops) {
            System.err.println("fatal error during startup");
            oops.printStackTrace();
            System.exit(2);
        }
    }

    // convert string 'null' and empty string to a null, trim() and return
    private static String fixNull(String s) {
        if (s == null || "null".equals(s)) {
            return null;
        }
        s = s.trim();
        if (s.length() == 0) {
            return null;
        }
        return s;
    }
    
    public static DownloadRequest getDownloadRequest(ArgumentMap argMap) {
        DownloadRequest downloadRequest = new DownloadRequest();
        String curTupleStr = "";

        // Iterate through the positional arguments and attempt to construct tuples
        // URI{DALI position string}{label}

        List<String> positionalArgs = argMap.getPositionalArgs();
        for (String segment : positionalArgs) {
            log.debug("segment: " + segment);

            boolean endOfTuple = false;

            if (segment.endsWith("}")) {
                endOfTuple = true;
            } else if (segment.contains("{")) {
                // pass
                endOfTuple = false;
            } else {
                if (StringUtil.hasLength(curTupleStr)) {
                    endOfTuple = false;
                } else {
                    endOfTuple = true;
                }
            }

            // concatenate a into curTupleStr & continue
            if (StringUtil.hasLength(curTupleStr)) {
                curTupleStr += " ";
            }
            curTupleStr += segment;
            if (endOfTuple == true) {
                try {
                    DownloadTupleFormat df = new DownloadTupleFormat();
                    DownloadTuple dt = df.parse(curTupleStr);
                    downloadRequest.getTuples().add(dt);
                } catch (Exception e) {
                    // df.parse will throw validation errors. Record
                    // them and continue
                    downloadRequest.getValidationErrors().add(e);
                }
                curTupleStr = "";
                endOfTuple = false;
            }
        }

        return downloadRequest;
    }

    private static void usage() {
        System.out.println("cadc-download-manager -h || --help");
        System.out.println("cadc-download-manager [-v|--verbose | -d|--debug | -q|--quiet ] [options] <space separated list of URIs>");
        System.out.println("         [ --runid=<runid> ]");
        System.out.println("         [ --ssocookie=<cookie value to use in sso authentication> ]");
        System.out.println("         [ --ssocookiedomain=<domain cookie is valid in (required with ssocookie arg)> ]");
        System.out.println("         [ --tuple=<URI{DALI position string}{label}> ]");
        System.out.println("         [ --headless ] : run in non-interactive (no GUI) mode");
        System.out.println();
        System.out.println("optional arguments to use with --headless:");
        System.out.println("        --dest=<directory> : directory must exist and be writable by the user");
        System.out.println("        --decompress : decompress files after download (gzip,zip supported)");
        System.out.println("        --overwrite : overwrite existing files with the same name");
        System.out.println("        --threads=<number of threads> : allowed range is [1,11]");
        System.out.println("        --retry : retry (loop) when server reports it is too busy");
    }
}
