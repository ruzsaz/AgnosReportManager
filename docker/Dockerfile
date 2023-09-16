FROM quay.io/wildfly/wildfly:26.1.3.Final-jdk17

WORKDIR /
USER root
RUN mkdir /opt/agnos /opt/agnos/Reports /opt/agnos/Cubes /opt/agnos/WEB-INF
ENV AGNOS_REPORTS_DIR=/opt/agnos/Reports
ENV AGNOS_CUBES_DIR=/opt/agnos/Cubes
COPY ${src} /opt/jboss/wildfly/standalone/deployments/
COPY standalone.xml /opt/jboss/wildfly/standalone/configuration/
COPY ./envsubst /usr/bin/envsubst
RUN chmod +x /usr/bin/envsubst
COPY oidc.json.template /opt/agnos/oidc.json.template

# CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]

ENTRYPOINT /usr/bin/envsubst < /opt/agnos/oidc.json.template > /opt/agnos/WEB-INF/oidc.json \
    && cd /opt/agnos \
    && jar uf /opt/jboss/wildfly/standalone/deployments/AgnosReportManager.war WEB-INF/oidc.json \
    && /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0 -bmanagement 0.0.0.0