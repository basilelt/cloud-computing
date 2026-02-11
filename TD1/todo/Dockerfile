FROM tomcat:9-jre17-temurin

LABEL maintainer=basile.le-thiec@uha.fr

RUN apt-get update &&\
    apt-get upgrade -y &&\
    apt-get install -y --no-install-recommends \
        curl &&\
    apt-get autoremove -y &&\
    apt-get clean &&\
    rm -rf /var/lib/apt/lists/*

RUN rm -rf ${CATALINA_HOME}/webapps/

COPY todo-app.war ${CATALINA_HOME}/webapps/ROOT.war

EXPOSE 8080
HEALTHCHECK --interval=5s --timeout=2s --retries=3 CMD /usr/bin/curl -f http://localhost:8080/ || exit 1
