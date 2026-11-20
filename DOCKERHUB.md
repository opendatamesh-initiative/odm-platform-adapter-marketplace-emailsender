# ODM Platform Marketplace Email Sender

The Email Sender is a marketplace executor adapter of [Open Data Mesh Platform](https://github.com/opendatamesh-initiative) V2. The [Marketplace Server](https://hub.docker.com/r/opendatamesh/odm-platform-pp-marketplace) forwards each access request to it; the Email Sender sends an email, rendered from a Mustache template, to the requester and reports the result back to the Marketplace: `GRANTED` for a subscribe, `REVOKED` for an unsubscribe, `ERROR` if the email can't be sent. It doesn't provision access or wait for an approval: use it where an email notification is all the fulfilment you need, or alongside an executor such as [Jira](https://hub.docker.com/r/opendatamesh/odm-platform-adapter-marketplace-executor-jira) or [ServiceNow](https://hub.docker.com/r/opendatamesh/odm-platform-adapter-marketplace-executor-servicenow). It is stateless (no database).

- **Source code:** [opendatamesh-initiative/odm-platform-adapter-marketplace-emailsender](https://github.com/opendatamesh-initiative/odm-platform-adapter-marketplace-emailsender)
- **Documentation:** [README](https://github.com/opendatamesh-initiative/odm-platform-adapter-marketplace-emailsender/blob/main/README.md)
- **Issues:** [GitHub issues](https://github.com/opendatamesh-initiative/odm-platform-adapter-marketplace-emailsender/issues)

## Tags

Each [GitHub release](https://github.com/opendatamesh-initiative/odm-platform-adapter-marketplace-emailsender/releases) publishes one image tagged with its version, without the `v` prefix:

```bash
docker pull opendatamesh/odm-platform-adapter-marketplace-emailsender:<version>
```

There is no `latest` tag: always pin a version. The same image is also published to `ghcr.io/opendatamesh-initiative/odm-platform-adapter-marketplace-emailsender`.

## Requirements

| Dependency | Required | Notes |
| --- | --- | --- |
| [Marketplace Server](https://hub.docker.com/r/opendatamesh/odm-platform-pp-marketplace) | Yes | Lists the Email Sender in `odm.utility-plane.marketplace-executors` and receives its results. |
| SMTP server | Yes | By default the Email Sender authenticates and uses STARTTLS. |

## Quick start

```bash
docker run --name odm-emailsender -p 9019:9019 \
  -e SPRING_MAIL_HOST=smtp.example.com \
  -e SPRING_MAIL_PORT=587 \
  -e SPRING_MAIL_USERNAME=odm@example.com \
  -e SPRING_MAIL_PASSWORD=change-me \
  -e ODM_EMAIL_FROM=odm@example.com \
  -e ODM_PRODUCT_PLANE_MARKETPLACE_SERVICE_ADDRESS=http://marketplace.example.com:8080 \
  opendatamesh/odm-platform-adapter-marketplace-emailsender:<version>
```

The application listens on port `9019`, even though the image declares `EXPOSE 8080`. Set `SPRING_MAIL_PORT` explicitly: the image defines it as empty, so the `587` default of the application doesn't apply.

When it's up, the API is documented at `http://localhost:9019/swagger-ui.html` (OpenAPI at `/api-docs`). The Marketplace Server calls `POST /api/v1/up/marketplace-executor/requests`; register the Email Sender there with its address, for example `{ "name": "email", "address": "http://emailsender.example.com:9019", "active": true }`.

The email goes to the requester's identifier (`request.requester.identifier`), which must be an email address. Its subject is `Access Granted: <data product FQN>` or `Unsubscription Confirmed: <data product FQN>`.

## Email templates

The image ships default plain-text templates, marked as defaults with a disclaimer. To use your own, mount them and point the template paths at them:

```bash
docker run --name odm-emailsender -p 9019:9019 \
  ...
  -v "$(pwd)/templates:/etc/odm/templates/email:ro" \
  -e ODM_EMAIL_SUBSCRIBE_TEMPLATE_PATH=/etc/odm/templates/email/subscribe.mustache \
  -e ODM_EMAIL_UNSUBSCRIBE_TEMPLATE_PATH=/etc/odm/templates/email/unsubscribe.mustache \
  -e ODM_EMAIL_USE_HTML=true \
  opendatamesh/odm-platform-adapter-marketplace-emailsender:<version>
```

Mounting the directory alone isn't enough: the paths default to the templates inside the image. A path is read from the file system first, then as a Spring resource (`classpath:...`). Templates read the request from the `data` object, for example `{{data.operation}}`, `{{data.request.provider.dataProductFqn}}`, `{{data.request.consumer.identifier}}` and `{{data.request.requester.identifier}}`. The variables are listed in the [README](https://github.com/opendatamesh-initiative/odm-platform-adapter-marketplace-emailsender/blob/main/README.md#template-variables), and the [default subscribe template](https://github.com/opendatamesh-initiative/odm-platform-adapter-marketplace-emailsender/blob/main/src/main/resources/templates/email/subscribe-template.mustache) is a starting point.

## Environment variables

### Image

| Variable | Description | Default |
| --- | --- | --- |
| `SPRING_MAIL_HOST` | SMTP host (`spring.mail.host`). | empty, required |
| `SPRING_MAIL_PORT` | SMTP port (`spring.mail.port`). | empty: set it |
| `SPRING_MAIL_USERNAME` | SMTP user (`spring.mail.username`). | empty |
| `SPRING_MAIL_PASSWORD` | SMTP password (`spring.mail.password`). | empty |
| `ODM_EMAIL_FROM` | Sender address of the emails (`odm.email.from`). | empty, required |
| `ODM_EMAIL_SUBSCRIBE_TEMPLATE_PATH` | Template of the subscribe email (`odm.email.subscribe-template-path`). | `classpath:templates/email/subscribe-template.mustache` |
| `ODM_EMAIL_UNSUBSCRIBE_TEMPLATE_PATH` | Template of the unsubscribe email (`odm.email.unsubscribe-template-path`). | `classpath:templates/email/unsubscribe-template.mustache` |
| `JAVA_OPTS` | Extra JVM options, for example `-Xmx512m`. | empty |
| `SPRING_PROPS` | Extra arguments added to the command line after `-jar`, passed to the application. Use Spring Boot `--` options, such as `--server.port=9000`: `-D` options and JSON are ignored here. Use `SPRING_APPLICATION_JSON` for nested configuration. | empty |

### Application

Every property can be set with `SPRING_APPLICATION_JSON` or as an environment variable, using Spring Boot relaxed binding (for example, `odm.product-plane.marketplace-service.address` becomes `ODM_PRODUCT_PLANE_MARKETPLACE_SERVICE_ADDRESS`).

| Property | Description | Default |
| --- | --- | --- |
| `server.port` | HTTP port. | `9019` |
| `odm.product-plane.marketplace-service.address` | Base URL of the Marketplace Server, where results are sent. A result that can't be delivered is logged and not retried. | `http://localhost:8085` |
| `odm.email.useHtml` (`ODM_EMAIL_USE_HTML`) | Send the rendered template as HTML (`true`) or plain text (`false`). | `false` |
| `spring.mail.properties.mail.smtp.auth` | Authenticate to the SMTP server. | `true` |
| `spring.mail.properties.mail.smtp.starttls.enable` | Use STARTTLS. | `true` |

The other Spring Boot 2.7 `spring.mail.*` properties also apply, and `spring.mail.properties.*` passes any JavaMail property to the SMTP session.

## Ports and volumes

- **Port:** `9019` (HTTP), or the value of `server.port`. The image's `EXPOSE 8080` doesn't match it.
- **Volumes:** none required. The image creates `/etc/odm/templates/email` for custom templates.

## Learn more

- [README](https://github.com/opendatamesh-initiative/odm-platform-adapter-marketplace-emailsender/blob/main/README.md)
- [Marketplace executor starter](https://github.com/opendatamesh-initiative/odm-platform-adapter-marketplace-executor-starter), to build your own executor

Licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).
