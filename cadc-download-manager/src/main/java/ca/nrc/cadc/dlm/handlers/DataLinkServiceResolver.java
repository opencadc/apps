/*
************************************************************************
*******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
**************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
*
*  (c) 2016.                            (c) 2016.
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
************************************************************************
*/

package ca.nrc.cadc.dlm.handlers;


import java.net.URI;
import java.net.URL;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 * Class to find the right DataLink service resourceIdentifier to use.
 * 
 * @author pdowler
 */
public class DataLinkServiceResolver 
{
    private static final Logger log = Logger.getLogger(DataLinkServiceResolver.class);

    public static final String DEFAULT_KEY = DataLinkServiceResolver.class.getName() + ".resourceID";
    
    private Properties props;
    
    public DataLinkServiceResolver()
    {
        String fname = DataLinkServiceResolver.class.getSimpleName() + ".properties";
        try
        {
            
            URL url = DataLinkServiceResolver.class.getClassLoader().getResource(fname);
            this.props = new Properties();
            props.load(url.openStream());
        }
        catch (Exception ex)
        {
            throw new RuntimeException("CONFIG: failed to read " + fname + " from classpath");
        }
        finally { }
    }
    
    /**
     * Find the DataLink service resourceIdentifier for the specified publisherID.
     * The current implementation ignores the publisherID and simply looks up a
     * configured the service in a configuration file.
     * 
     * @param publisherID
     * @return 
     */
    public URI getResourceID(URI publisherID)
    {
        String key = DEFAULT_KEY;
        
        if ("ivo".equals(publisherID.getScheme()))
        {
            key = publisherID.getPath();
            if (key.startsWith(("/")))
                key = key.substring(1);
        }
        else if ("caom".equals(publisherID.getScheme()))
        {
            String ssp = publisherID.getSchemeSpecificPart();
            String[] ss = ssp.split("/");
            key = ss[0];
        }
        
        String val = props.getProperty(key);
        log.debug("getResourceID: " + publisherID + " -> " + key + " -> " + val);
        
        if (val != null)
            return URI.create(val);
        
        log.debug("not found: " + key);
        return null;
    }
}
