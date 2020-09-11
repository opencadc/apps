package ca.nrc.cadc.dlm;

import ca.nrc.cadc.util.StringUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidParameterException;
import javax.swing.plaf.synth.Region;
import org.apache.log4j.Logger;

public class DownloadTuple {
    private static Logger log = Logger.getLogger(DownloadTuple.class);

    public final URI tupleID;
    public final String shapeDescriptor;
    public final String label;

    // unsure we need Region or not - only interesting
    // in cadc-download-manager when DataLinkClient is putting
    // together cutouts
    private Region shape;

    /**
     * Convenience ctor to support historic use where only URI was provided.
     * @param tupleIDstr String representing URI
     */
    public DownloadTuple(String tupleIDstr) {
        this(tupleIDstr, null, null);
    }

    public DownloadTuple(String tupleIDstr, String shape, String label) {
        try {
            this.tupleID = new URI(tupleIDstr);
        } catch (URISyntaxException u ) {
            throw new InvalidParameterException("Invalid tupleID. " + tupleIDstr +": " + u.getReason());
        } catch (NullPointerException npe) {
            throw new InvalidParameterException("tupleID can not be null.");
        }
        this.shapeDescriptor = shape;
        this.label = label;
    }

    public DownloadTuple(URI tupleID, String shape, String label) {
        this.tupleID = tupleID;
        this.shapeDescriptor = shape;
        this.label = label;
    }

    
    public String toInternalFormat() {
        String tupleStr = tupleID.toString();

        // This function might be able to provide different formats
        // within the shapeDescriptor to substitute whitespace for a different character
        if (StringUtil.hasLength(shapeDescriptor)) {
            tupleStr += "{" + shapeDescriptor + "}";
        }

        if (StringUtil.hasLength(label)) {
            tupleStr += "{" + label + "}";
        }

        return tupleStr;
    }
}
