FROM nginxdemos/hello:0.4

LABEL maintainer=basile.le-thiec@uha.fr

RUN apk update &&\
    apk upgrade &&\
    rm -rf /var/cache/apk/*

RUN sed -i "s;src=\"[^\"]*;src=\"https://svn.ensisa.uha.fr/bd/ensisa.svg\";" /usr/share/nginx/html/index.html

EXPOSE 80
HEALTHCHECK --interval=5s --timeout=2s --retries=3 CMD /usr/bin/wget -O - http://localhost 2>/dev/null | grep -q html || exit 1
