# Download Manager

## Running it

For backwards compatibility with existing scripts, the download manager app should be run with a [WAR rename configuration](https://github.com/opencadc/docker-base/tree/main/cadc-tomcat#war-renameconf):

`war-rename.conf`:
```txt
mv download-manager.war downloadManager.war
```

## Testing it

Running integration tests requires:
- valid test certificates (in $A/test-certificates)
- valid servops.pem
- valid ca.crt file

