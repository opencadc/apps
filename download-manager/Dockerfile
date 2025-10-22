FROM images.opencadc.org/library/cadc-tomcat:1

# Clear out the webapps directory.
RUN rm -rf webapps/*

# Allow JAR scanning due allow serving JSPs from classpath
RUN sed -i '/^tomcat.util.scan.StandardJarScanFilter.jarsToSkip/d' /usr/share/tomcat/conf/catalina.properties

COPY build/libs/*.war /usr/share/tomcat/webapps/
