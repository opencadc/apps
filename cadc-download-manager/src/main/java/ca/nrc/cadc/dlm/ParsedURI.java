package ca.nrc.cadc.dlm;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.log4j.Logger;

public class ParsedURI {
    private static final Logger log = Logger.getLogger(ParsedURI.class);
    public String str;
    public URI uri;
    public Throwable error;

    /** Use if URI is a string coming from another source
     *
     * @param uriStr String holding URI to be parsed
     */
    public ParsedURI(String uriStr) {
        this.str = uriStr;
        try {
            this.uri = new URI(uriStr);
        } catch (URISyntaxException uriErr) {
            log.error("error parsing URI: " + uriStr);
            this.error = uriErr;
        }
    }

    /** Use if URI is already defined
     *
     * @param uri URI to store in this instance
     */
    public ParsedURI(URI uri) {
        this.uri = uri;
        this.str = uri.toString();
        this.error = null;
    }
}
