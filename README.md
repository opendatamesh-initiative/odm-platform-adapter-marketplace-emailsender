# ODM Platform Adapter Marketplace Email Sender

This service is part of the Open Data Mesh (ODM) Platform and handles email notifications for marketplace subscription and unsubscription events. It provides customizable email templates and configurable SMTP settings to notify users about their data product access status.

## Features

- Sends email notifications for subscription and unsubscription events
- Customizable email templates using Mustache templating
- Configurable SMTP settings
- Docker support for easy deployment

## Requirements

### Java Version

- **Build**: Java 11 or later
- **Runtime**: Java 11 or later
- **Docker Image**: Uses Amazon Corretto 11 (Alpine-based)

The service is built and tested with:
- Java 11 (Amazon Corretto)
- Java 21 (OpenJDK)

While the service should work with any Java 11+ distribution, we recommend using:
- Amazon Corretto 11
- OpenJDK 11/21

## Configuration

### Email Settings

The service can be configured through environment variables:

```yaml
MAIL_HOST=your-smtp-server
MAIL_PORT=587
MAIL_USERNAME=your-username
MAIL_PASSWORD=your-password
MAIL_FROM=your-sender-email
```

### Email Templates

The service supports two types of email templates:
1. Subscription notification
2. Unsubscription notification

#### Template Locations

Templates can be loaded from:
1. Classpath (default)
2. External file system (configurable)

Default template paths:
```yaml
SUBSCRIBE_TEMPLATE_PATH=classpath:templates/email/subscribe-template.mustache
UNSUBSCRIBE_TEMPLATE_PATH=classpath:templates/email/unsubscribe-template.mustache
```

#### Template Variables

The following variables are available in the templates through the `data` object:

Request Details:
- `{{data.request.name}}`: Name of the request
- `{{data.request.identifier}}`: Unique identifier of the request
- `{{data.operation}}`: Type of operation (MARKETPLACE_SUBSCRIBE or MARKETPLACE_UNSUBSCRIBE)
- `{{data.v}}`: Version of the request

Data Product Information:
- `{{data.request.provider.dataProductFqn}}`: Fully qualified name of the data product
- `{{#data.request.provider.dataProductPortsFqn}}{{.}}{{/data.request.provider.dataProductPortsFqn}}`: List of data product ports

Access Details:
- `{{data.request.consumer.type}}`: Type of consumer (dataproduct, user, team)
- `{{data.request.consumer.identifier}}`: Identifier of the consumer
- `{{data.request.startDate}}`: Start date of the access period
- `{{data.request.endDate}}`: End date of the access period

Requester Information:
- `{{data.request.requester.type}}`: Type of requester
- `{{data.request.requester.identifier}}`: Identifier of the requester

Additional Properties:
- `{{#data.request.properties}}{{@key}}: {{.}}{{/data.request.properties}}`: Any additional properties provided in the request

#### Creating Custom Templates

1. Create a new `.mustache` file with your template content
2. Use the available variables in your template
3. Configure the template path in your environment:
   ```bash
   SUBSCRIBE_TEMPLATE_PATH=/path/to/your/subscribe-template.mustache
   UNSUBSCRIBE_TEMPLATE_PATH=/path/to/your/unsubscribe-template.mustache
   ```

## Building the Project

### Prerequisites

- Java 11 or later (see Requirements section for details)
- Maven (optional, project includes Maven wrapper)

### Using Maven Wrapper

```bash
# Build the project
./mvnw clean install

# Run tests
./mvnw test
```

### Using Maven

```bash
# Build the project
mvn clean install

# Run tests
mvn test
```

## Docker Support

### Building the Docker Image

```bash
# Build the image
docker build -t odm-marketplace-email-sender .
```

### Running the Container

```bash
docker run -d \
  -p 8080:8080 \
  -e MAIL_HOST=your-smtp-server \
  -e MAIL_USERNAME=your-username \
  -e MAIL_PASSWORD=your-password \
  -e MAIL_FROM=your-sender-email \
  -v /path/to/your/templates:/etc/odm/templates/email \
  odm-marketplace-email-sender
```

### Environment Variables

The following environment variables can be configured when running the container:

- `MAIL_HOST`: SMTP server host
- `MAIL_PORT`: SMTP server port (default: 587)
- `MAIL_USERNAME`: SMTP username
- `MAIL_PASSWORD`: SMTP password
- `MAIL_FROM`: Sender email address
- `SUBSCRIBE_TEMPLATE_PATH`: Path to subscription template
- `UNSUBSCRIBE_TEMPLATE_PATH`: Path to unsubscription template

### Volume Mounting

To use custom email templates, mount a volume containing your template files:

```bash
-v /host/path/to/templates:/etc/odm/templates/email
```

## Default Templates

The service includes default templates that are used when no custom templates are provided. These templates are located in:

- `src/main/resources/templates/email/subscribe-template.mustache`
- `src/main/resources/templates/email/unsubscribe-template.mustache`

These templates include a disclaimer indicating they are default templates and should be replaced with custom ones in production.

## Contributing

Please read our contributing guidelines before submitting pull requests.

## License

This project is licensed under the terms of the license included in the repository.