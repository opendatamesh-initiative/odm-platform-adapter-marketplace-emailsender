FROM amazoncorretto:11-alpine-jdk

# Create directory for external templates
RUN mkdir -p /etc/odm/templates/email

COPY target/odm-platform-adapter-marketplace-emailsender-*.jar ./application.jar

ENV JAVA_OPTS=""
ENV SPRING_PROPS=""

# Default environment variables for email configuration
ENV MAIL_HOST= \
    MAIL_PORT= \
    MAIL_USERNAME= \
    MAIL_PASSWORD= \
    MAIL_FROM= \
    SUBSCRIBE_TEMPLATE_PATH=classpath:templates/email/subscribe-template.mustache \
    UNSUBSCRIBE_TEMPLATE_PATH=classpath:templates/email/unsubscribe-template.mustache

EXPOSE 8080

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS  -jar ./application.jar $SPRING_PROPS" ] 