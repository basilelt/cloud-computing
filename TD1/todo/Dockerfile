# First stage, build the custom JRE
FROM azul/zulu-openjdk-alpine:17 AS jre-builder

# Install binutils, required by jlink
RUN apk add --no-cache binutils && \
    rm -rf /var/cache/apk/*

# Build small JRE image
RUN $JAVA_HOME/bin/jlink \
    --verbose \
    --add-modules java.base,java.management,java.naming,java.sql,java.desktop,java.security.jgss,java.instrument,jdk.unsupported \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=2 \
    --output /optimized-jdk-17


# Second stage, Use the custom JRE and build the app image with Tomcat
FROM alpine:3

# Set Env
ENV JAVA_HOME=/opt/jdk/jdk-17
ENV PATH="${JAVA_HOME}/bin:${PATH}"
ENV CATALINA_HOME=/opt/tomcat
ENV PATH="${CATALINA_HOME}/bin:${PATH}"
ENV TOMCAT_VERSION=9.0.115

ARG APPLICATION_USER=tomcat

# Copy JRE from the base image
COPY --from=jre-builder /optimized-jdk-17 $JAVA_HOME

# Install upgrade, add curl, create user, install Tomcat, set ownership — all in one layer to avoid duplication
RUN apk update && \
    apk upgrade && \
    apk add --no-cache curl && \
    rm -rf /var/cache/apk/* && \
    apk --purge del apk-tools && \
    rm -rf /usr/lib/libapk.so* \
       /sbin/apk \
       /lib/apk \
       /etc/apk \
       /var/cache/apk && \
    \
    addgroup -S $APPLICATION_USER && \
    adduser -S -G $APPLICATION_USER $APPLICATION_USER && \
    \
    curl -SL https://downloads.apache.org/tomcat/tomcat-9/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.tar.gz | tar -xzC /opt && \
    mv /opt/apache-tomcat-$TOMCAT_VERSION $CATALINA_HOME && \
    \
    rm -rf $CATALINA_HOME/webapps/* \
        $CATALINA_HOME/bin/*.bat \
        $CATALINA_HOME/bin/*.tar.gz \
        $CATALINA_HOME/bin/catalina-tasks.xml \
        $CATALINA_HOME/BUILDING.txt \
        $CATALINA_HOME/CONTRIBUTING.md \
        $CATALINA_HOME/README.md \
        $CATALINA_HOME/RELEASE-NOTES \
        $CATALINA_HOME/RUNNING.txt && \
    \
    chmod +x $CATALINA_HOME/bin/*.sh && \
    chown -R $APPLICATION_USER:$APPLICATION_USER $CATALINA_HOME

# Copy the WAR file to Tomcat's webapps directory as ROOT.war
COPY --chown=$APPLICATION_USER:$APPLICATION_USER todo-app.war $CATALINA_HOME/webapps/ROOT.war

WORKDIR $CATALINA_HOME

USER $APPLICATION_USER

EXPOSE 8080

# Start Tomcat
ENTRYPOINT ["catalina.sh", "run"]
