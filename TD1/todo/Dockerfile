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

RUN apk update && \
    apk upgrade && \
    apk add --no-cache curl && \
    rm -rf /var/cache/apk/*

# Set Java home and path
ENV JAVA_HOME=/opt/jdk/jdk-17
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Copy JRE from the base image
COPY --from=jre-builder /optimized-jdk-17 $JAVA_HOME

# Install Tomcat
ENV CATALINA_HOME=/opt/tomcat
ENV PATH="${CATALINA_HOME}/bin:${PATH}"
ENV TOMCAT_VERSION=9.0.115

# Download and install Tomcat 9
RUN curl -SL https://downloads.apache.org/tomcat/tomcat-9/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.tar.gz | tar -xzC /opt && \
    mv /opt/apache-tomcat-$TOMCAT_VERSION $CATALINA_HOME && \
    rm -rf $CATALINA_HOME/webapps/* && \
    chown -R root:root $CATALINA_HOME && \
    chmod +x $CATALINA_HOME/bin/*.sh

# Add app user
ARG APPLICATION_USER=tomcat

# Create a user to run the application, don't run as root
RUN addgroup -S $APPLICATION_USER && \
    adduser -S -G $APPLICATION_USER $APPLICATION_USER

# Create required Tomcat directories and set permissions
RUN mkdir -p $CATALINA_HOME/conf $CATALINA_HOME/logs $CATALINA_HOME/temp $CATALINA_HOME/work && \
    chown -R $APPLICATION_USER:$APPLICATION_USER $CATALINA_HOME/bin $CATALINA_HOME/conf $CATALINA_HOME/lib $CATALINA_HOME/logs $CATALINA_HOME/temp $CATALINA_HOME/webapps $CATALINA_HOME/work

# Copy the WAR file to Tomcat's webapps directory as ROOT.war
COPY --chown=$APPLICATION_USER:$APPLICATION_USER todo-app.war $CATALINA_HOME/webapps/ROOT.war

WORKDIR $CATALINA_HOME

USER $APPLICATION_USER

EXPOSE 8080

# Start Tomcat
ENTRYPOINT ["catalina.sh", "run"]
