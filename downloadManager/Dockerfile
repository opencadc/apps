FROM images.opencadc.org/library/cadc-tomcat:1.4

# Clear out the webapps directory.
RUN rm -rf webapps/*

# Allow JAR scanning
RUN sed -i '/^tomcat.util.scan.StandardJarScanFilter.jarsToSkip/d' /usr/share/tomcat/conf/catalina.properties

COPY build/libs/*.war /usr/share/tomcat/webapps/
