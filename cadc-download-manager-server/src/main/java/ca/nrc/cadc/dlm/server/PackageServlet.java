package ca.nrc.cadc.dlm.server;

import ca.nrc.cadc.auth.AuthMethod;
import ca.nrc.cadc.auth.AuthenticationUtil;
import ca.nrc.cadc.config.ApplicationConfiguration;
import ca.nrc.cadc.net.HttpPost;
import ca.nrc.cadc.reg.Standards;
import ca.nrc.cadc.reg.client.RegistryClient;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.security.auth.Subject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class PackageServlet extends HttpServlet {
    private static final String PACKAGE_SERVICE_RESOURCE_ID_KEY = "org.opencadc.dlm.package-download.service.id";
    private static final String RESPONSE_FORMAT_PAYLOAD_KEY = "RESPONSEFORMAT";
    private static final String ID_PAYLOAD_KEY = "ID";
    private static final String REQUEST_PARAM_URI_KEY = "tuple";
    private static final String REQUEST_PARAM_METHOD_KEY = "method";
    private static final Map<String, String> METHOD_TO_CONTENT_TYPE_MAP = new HashMap<>();

    private final ApplicationConfiguration configuration = new ApplicationConfiguration(DispatcherServlet.DEFAULT_CONFIG_FILE_PATH);

    static {
        PackageServlet.METHOD_TO_CONTENT_TYPE_MAP.put(DispatcherServlet.TAR_PACKAGE, "application/x-tar");
        PackageServlet.METHOD_TO_CONTENT_TYPE_MAP.put(DispatcherServlet.ZIP_PACKAGE, "application/zip");
    }


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
        final Map<String, Object> payload = new HashMap<>();

        final String[] publisherIDs = request.getParameterValues(PackageServlet.REQUEST_PARAM_URI_KEY);
        if (publisherIDs == null || publisherIDs.length == 0) {
            throw new IllegalArgumentException("Nothing specified to download.  Use tuple=<URI>.");
        }

        payload.put(PackageServlet.ID_PAYLOAD_KEY, Arrays.asList(publisherIDs));

        final String requestedDeliveryMethod = request.getParameter(PackageServlet.REQUEST_PARAM_METHOD_KEY);

        if (requestedDeliveryMethod == null) {
            throw new IllegalArgumentException("Delivery method is mandatory.  Use method=<TAR,ZIP>");
        } else if (!PackageServlet.METHOD_TO_CONTENT_TYPE_MAP.containsKey(requestedDeliveryMethod.toUpperCase())) {
            throw new IllegalArgumentException("Unknown method " + requestedDeliveryMethod + ". Use "
                                               + Arrays.toString(PackageServlet.METHOD_TO_CONTENT_TYPE_MAP.keySet().toArray(new String[0])));
        }

        payload.put(PackageServlet.RESPONSE_FORMAT_PAYLOAD_KEY, PackageServlet.METHOD_TO_CONTENT_TYPE_MAP.get(requestedDeliveryMethod.toUpperCase()));

        return payload;
    }

    private URL lookupServiceURL(URI packageServiceURI, Subject currentSubject) {
        final RegistryClient registryClient = new RegistryClient();
        final AuthMethod authMethod = currentSubject == null ? AuthMethod.ANON : AuthenticationUtil.getAuthMethod(currentSubject);
        return registryClient.getServiceURL(packageServiceURI, Standards.PKG_10, authMethod);
    }
}
