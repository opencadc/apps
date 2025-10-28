# Download Manager

## Running it

### WAR Deployment

For backwards compatibility with existing scripts, the download manager app should be run with a [WAR rename configuration](https://github.com/opencadc/docker-base/tree/main/cadc-tomcat#war-renameconf):

`war-rename.conf`:
```txt
mv download-manager.war downloadManager.war
```

### Configuration

Download Manager requires a file called `org.opencadc.dlm-server.properties` to be placed in `$CATALINA_BASE/config/` with the following properties set:

`org.opencadc.dlm-server.properties`:
```properties
# Whether to enable the Java Webstart option for bulk downloading.  Setting to true will enable that button, and false will hide it.
org.opencadc.dlm.webstart.enable = false

# The URI in the Registry of the package download service.  In CADC, this is caom2ops, but on a developer machine it could be anything.
org.opencadc.dlm.package-download.service.id = ivo://cadc.nrc.ca/caom2ops
```

`cadc-log.properties` (See the [cadc-log](https://github.com/opencadc/core/tree/main/cadc-log#cadc-logproperties-optional) module):
```properties
group = <IVOA URI>
username = <CADC username>
```

Sample `cadc-registry.properties` (See the [cadc-registry](https://github.com/opencadc/reg/tree/main/cadc-registry#cadc-registryproperties) module):
```properties
#  RegistryClient.baseURL replaces RegistryClient.host and is more useful
#
ca.nrc.cadc.reg.client.RegistryClient.baseURL=https://ws.cadc-ccda.hia-iha.nrc-cnrc.gc.ca/reg

# The following authority lines are only required for services.
# this is moved from LocalAuthority.properties.
#
ivo://ivoa.net/std/GMS#search-1.0 = ivo://cadc.nrc.ca/gms
ivo://ivoa.net/std/CDP#delegate-1.0 = ivo://cadc.nrc.ca/cred
ivo://ivoa.net/std/CDP#proxy-1.0 = ivo://cadc.nrc.ca/cred

ivo://ivoa.net/sso#OAuth = ivo://cadc.nrc.ca/gms
ivo://ivoa.net/sso#OpenID = ivo://cadc.nrc.ca/gms
ivo://ivoa.net/sso#tls-with-password = ivo://cadc.nrc.ca/gms

ivo://ivoa.net/std/GMS#groups-1.0 = ivo://cadc.nrc.ca/gms
```

Sample `catalina.properties`:
```properties
tomcat.connector.scheme=https
tomcat.connector.proxyName=<proxy hostname>
tomcat.connector.proxyPort=443

# auth needs to know what's validating certs and cookies
ca.nrc.cadc.auth.IdentityManager=ca.nrc.cadc.ac.ACIdentityManager
```

A file that contains the public key to verify Cookie tokens called `RsaSignaturePub.key` is also required. This file should be placed in `$CATALINA_BASE/config/`.  See the existing configuration.

## Testing it

Running integration tests requires:
- valid test certificates (in $A/test-certificates)
- valid servops.pem
- valid ca.crt file

