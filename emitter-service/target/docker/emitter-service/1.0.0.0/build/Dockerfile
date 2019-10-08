FROM registry.access.redhat.com/redhat-openjdk-18/openjdk18-openshift
ENV JAVA_APP_DIR=/deployments JAVA_OPTIONS="-Dvertx.cacheDirBase=/tmp -Dvertx.disableDnsResolver=true"
LABEL org.label-schema.description="Event Emitter Service" org.label-schema.version=1.0.0.0 org.label-schema.schema-version=1.0 org.label-schema.build-date=2019-10-07T21:22:22.837 org.label-schema.name="Event Emitter Service" org.label-schema.vcs-ref=e8997fbaa2fe285612a028cd4e1753ef545b1039 org.label-schema.vcs-url=https://github.com/snandakumar87/emitter-service.git
EXPOSE 8080 8778 9779
COPY maven /deployments/
