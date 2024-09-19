package ca.nrc.cadc.dlm.server;

import ca.nrc.cadc.auth.AuthMethod;
import ca.nrc.cadc.auth.AuthenticationUtil;
import ca.nrc.cadc.config.ApplicationConfiguration;
import ca.nrc.cadc.dlm.DownloadRequest;
import ca.nrc.cadc.net.HttpPost;
import ca.nrc.cadc.reg.Standards;
import ca.nrc.cadc.reg.client.RegistryClient;
import ca.nrc.cadc.util.StringUtil;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.security.auth.Subject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public abstract class PackageServlet extends HttpServlet {
    private static final String PACKAGE_SERVICE_RESOURCE_ID_KEY = "org.opencadc.dlm.package-download.service.id";
    private static final String RESPONSE_FORMAT_PAYLOAD_KEY = "RESPONSEFORMAT";
    private static final String ID_PAYLOAD_KEY = "ID";
    private static final String RUN_ID_PAYLOAD_KEY = "runid";

    final ApplicationConfiguration configuration = new ApplicationConfiguration(DispatcherServlet.DEFAULT_CONFIG_FILE_PATH);


    /**
     * Handle POSTed download request from an external page.
     *
     * @param request  The HTTP Request
     * @param response The HTTP Response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        final URI packageServiceURI = this.configuration.lookupServiceURI(PackageServlet.PACKAGE_SERVICE_RESOURCE_ID_KEY, null);
        if (packageServiceURI == null) {
            throw new IllegalStateException("No service configured to perform Package Downloads");
        } else {
            doRedirect(request, response, packageServiceURI);
        }
    }

    /**
     * Handle creating a payload and obtaining a redirect URL from the main service.
     * @param request               The HTTP Request.
     * @param response              The HTTP Response.
     * @param packageServiceURI     The URI of the configured
     */
    void doRedirect(final HttpServletRequest request, final HttpServletResponse response, final URI packageServiceURI) {
        final Subject currentSubject = AuthenticationUtil.getCurrentSubject();
        final URL packageServiceURL = lookupServiceURL(packageServiceURI, currentSubject);

        Subject.doAs(currentSubject, (PrivilegedAction<Void>) () -> {
            final HttpPost httpPost = new HttpPost(packageServiceURL, getPayload(request), false);

            try {
                httpPost.prepare();
            } catch (Exception exception) {
                throw new RuntimeException(exception.getMessage(), exception);
            }

            final URL redirect = httpPost.getRedirectURL();
            if (redirect == null) {
                throw new IllegalArgumentException("Unable to complete download (no redirect).");
            } else {
                try {
                    response.sendRedirect(redirect.toExternalForm());
                } catch (IOException ioException) {
                    throw new IllegalStateException("Redirect is invalid: " + redirect);
                }
            }

            return null;
        });
    }

    private Map<String, Object> getPayload(final HttpServletRequest request) {
        final DownloadRequest downloadRequest = getDownloadRequest(request);

        final Map<String, Object> payload = new HashMap<>();

        final List<String> publisherIDs =
            downloadRequest.getTuples().stream().map(downloadTuple -> downloadTuple.getID().toString()).collect(Collectors.toList());

        if (publisherIDs.isEmpty()) {
            throw new IllegalArgumentException("Nothing specified to download.  Use tuple=<URI>.");
        }

        payload.put(PackageServlet.ID_PAYLOAD_KEY, publisherIDs);

        final String contentType = getContentType();
        if (!StringUtil.hasText(contentType)) {
            throw new IllegalStateException("Poorly configured package type (no content type specified)");
        }

        payload.put(PackageServlet.RESPONSE_FORMAT_PAYLOAD_KEY, contentType);

        if (StringUtil.hasText(downloadRequest.runID)) {
            payload.put(PackageServlet.RUN_ID_PAYLOAD_KEY, downloadRequest.runID);
        }

        return payload;
    }

    private URL lookupServiceURL(URI packageServiceURI, Subject currentSubject) {
        final RegistryClient registryClient = new RegistryClient();
        final AuthMethod authMethod = currentSubject == null ? AuthMethod.ANON : AuthenticationUtil.getAuthMethod(currentSubject);
        return registryClient.getServiceURL(packageServiceURI, Standards.PKG_10, authMethod);
    }

    private DownloadRequest getDownloadRequest(final HttpServletRequest request) {
        final DownloadRequest downloadReq = (DownloadRequest) request.getAttribute("downloadRequest");
        downloadReq.runID = (String) request.getAttribute("runid");

        return downloadReq;
    }

    /**
     * Pull the specific content type to set in the payload.
     * @return      String content-type, never null.
     */
    abstract String getContentType();
}
