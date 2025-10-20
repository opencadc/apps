FROM images.opencadc.org/library/cadc-tomcat:1.4

# Clear out the webapps directory.
RUN rm -rf webapps/*

COPY build/libs/*.war /usr/share/tomcat/webapps/
