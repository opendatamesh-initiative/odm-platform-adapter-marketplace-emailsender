FROM amazoncorretto:11-alpine-jdk

# Create directory for external templates
RUN mkdir -p /etc/odm/templates/email

COPY target/odm-platform-adapter-marketplace-emailsender-*.jar ./application.jar

ENV JAVA_OPTS=""
ENV SPRING_PROPS=""

# Default environment variables for email configuration
ENV SPRING_MAIL_HOST= \
    SPRING_MAIL_PORT= \
    SPRING_MAIL_USERNAME= \
    SPRING_MAIL_PASSWORD= \
    ODM_EMAIL_FROM= \
    ODM_EMAIL_SUBSCRIBE_TEMPLATE_PATH=classpath:templates/email/subscribe-template.mustache \
    ODM_EMAIL_UNSUBSCRIBE_TEMPLATE_PATH=classpath:templates/email/unsubscribe-template.mustache

EXPOSE 8080

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS  -jar ./application.jar $SPRING_PROPS" ] 