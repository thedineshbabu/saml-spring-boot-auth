# SAML Spring Boot Authentication Application

A modern Spring Boot application that provides SAML 2.0-based Single Sign-On (SSO) functionality, migrated from a servlet-based implementation to leverage Spring Security's built-in SAML support.

## 🚀 Features

- **SAML 2.0 Authentication**: Full SAML 2.0 support with Spring Security
- **Multiple IdP Support**: Dynamic configuration for multiple Identity Providers
- **Database-Driven Configuration**: IdP configurations stored in PostgreSQL
- **Email Domain Mapping**: Automatic IdP selection based on user email domain
- **Modern UI**: Clean, responsive interface using Thymeleaf templates
- **Spring Boot 3.2**: Latest Spring Boot with Java 17 support
- **Comprehensive Logging**: Structured logging with SLF4J and Logback
- **Caching**: Redis-compatible caching for performance
- **Health Checks**: Built-in health monitoring endpoints

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- Docker (optional, for containerized deployment)

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Security**: Spring Security SAML2
- **Database**: PostgreSQL with Spring Data JPA
- **Templating**: Thymeleaf
- **Build Tool**: Maven
- **Logging**: SLF4J + Logback
- **Container**: Docker (optional)

## 📦 Installation & Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd saml-spring-boot-auth
```

### 2. Database Setup

Create a PostgreSQL database and run the schema:

```sql
CREATE DATABASE saml_app_db;
CREATE USER opal_user WITH PASSWORD 'opal_password';
GRANT ALL PRIVILEGES ON DATABASE saml_app_db TO opal_user;
```

### 3. Configuration

Update `src/main/resources/application.yml` with your database and SAML settings:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/saml_app_db
    username: your_username
    password: your_password
```

### 4. Build and Run

```bash
# Build the application
mvn clean package

# Run the application
java -jar target/saml-spring-boot-auth-1.0.0.jar
```

Or use Maven:

```bash
mvn spring-boot:run
```

### 5. Access the Application

- **Application**: http://localhost:8080
- **Login Page**: http://localhost:8080/login
- **SAML Metadata**: http://localhost:8080/saml2/service-provider-metadata/{registrationId}
- **Health Check**: http://localhost:8080/actuator/health

## 🔧 Configuration

### SAML Configuration

The application supports dynamic SAML configuration through the database. Key configuration areas:

1. **IdP Configuration**: Store Identity Provider metadata in the database
2. **Email Domain Mapping**: Map email domains to specific IdPs
3. **SAML Properties**: Configure SAML-specific properties per IdP

### Database Schema

The application uses three main tables:

- `idp_configurations`: Core IdP metadata
- `idp_properties`: SAML-specific properties
- `idp_email_domains`: Email domain to IdP mapping

### Environment Variables

Key environment variables for configuration:

```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=saml_app_db
DB_USER=opal_user
DB_PASSWORD=opal_password

# Application
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev
```

## 🔄 Migration from Servlet-Based Application

This Spring Boot application is a complete rewrite of the original servlet-based SAML application. Here are the key differences:

### Architecture Changes

| Aspect | Servlet-Based | Spring Boot |
|--------|---------------|-------------|
| **Framework** | Java Servlets + JSP | Spring Boot + Thymeleaf |
| **Security** | Custom SAML implementation | Spring Security SAML2 |
| **Database** | JDBC with custom DAO | Spring Data JPA |
| **Configuration** | Properties files | YAML + Environment variables |
| **Templating** | JSP | Thymeleaf |
| **Build** | Maven (WAR) | Maven (JAR) |
| **Deployment** | Tomcat container | Embedded Tomcat |

### Key Improvements

1. **Modern Framework**: Spring Boot 3.2 with Java 17
2. **Better Security**: Spring Security's built-in SAML support
3. **Type Safety**: Strong typing with JPA entities
4. **Caching**: Built-in caching support
5. **Monitoring**: Health checks and metrics
6. **Testing**: Better testability with Spring Boot Test
7. **Documentation**: Comprehensive API documentation
8. **Error Handling**: Centralized exception handling

### Migration Steps

1. **Database Migration**: Use existing database schema (compatible)
2. **Configuration Migration**: Convert properties to YAML format
3. **Certificate Migration**: Move certificates to `src/main/resources/credentials/`
4. **IdP Configuration**: Existing IdP configurations remain valid

## 🏗️ Project Structure

```
saml-spring-boot-auth/
├── src/
│   ├── main/
│   │   ├── java/com/example/
│   │   │   ├── SamlApplication.java          # Main application class
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java       # Spring Security configuration
│   │   │   ├── controller/
│   │   │   │   └── AuthController.java       # Authentication controller
│   │   │   ├── entity/
│   │   │   │   ├── IdpConfiguration.java     # IdP configuration entity
│   │   │   │   ├── IdpProperty.java          # IdP properties entity
│   │   │   │   └── IdpEmailDomain.java       # Email domain entity
│   │   │   ├── repository/
│   │   │   │   └── IdpConfigurationRepository.java # Data access layer
│   │   │   └── service/
│   │   │       └── IdpConfigurationService.java    # Business logic
│   │   └── resources/
│   │       ├── application.yml               # Application configuration
│   │       ├── templates/
│   │       │   ├── login.html                # Login page template
│   │       │   └── dashboard.html            # Dashboard template
│   │       └── credentials/                  # SAML certificates (create this)
│   └── test/                                 # Test classes
├── pom.xml                                   # Maven configuration
└── README.md                                 # This file
```

## 🔐 SAML Integration

### Setting up Azure AD

1. **Azure AD App Registration**:
   - Create a new app registration in Azure AD
   - Configure SAML authentication
   - Set the identifier (Entity ID) to your application's entity ID
   - Configure reply URL to your ACS endpoint

2. **Application Configuration**:
   - Add Azure AD IdP configuration to the database
   - Configure email domain mapping
   - Set up SAML properties

### SAML Flow

1. **User Access**: User visits the application
2. **Email Input**: User enters email address
3. **IdP Selection**: Application determines appropriate IdP
4. **SAML Request**: Redirect to IdP for authentication
5. **User Authentication**: User authenticates with IdP
6. **SAML Response**: IdP sends SAML response to application
7. **Session Creation**: Application creates user session
8. **Dashboard Access**: User redirected to dashboard

## 🧪 Testing

### Unit Tests

```bash
mvn test
```

### Integration Tests

```bash
mvn verify
```

### Manual Testing

1. Start the application
2. Access http://localhost:8080
3. Enter an email address
4. Complete SAML authentication
5. Verify dashboard access

## 📊 Monitoring

### Health Checks

- **Application Health**: `/actuator/health`
- **Database Health**: `/actuator/health/db`
- **SAML Health**: `/actuator/health/saml`

### Logging

Logs are written to:
- Console (development)
- `logs/saml-spring-boot-auth.log` (production)

### Metrics

Application metrics available at `/actuator/metrics`

## 🚀 Deployment

### Docker Deployment

```bash
# Build Docker image
docker build -t saml-spring-boot-auth .

# Run container
docker run -p 8080:8080 saml-spring-boot-auth
```

### Production Deployment

1. **Environment Setup**:
   ```bash
   export SPRING_PROFILES_ACTIVE=prod
   export DB_HOST=your-db-host
   export DB_PASSWORD=your-secure-password
   ```

2. **Run Application**:
   ```bash
   java -jar saml-spring-boot-auth-1.0.0.jar
   ```

3. **Reverse Proxy**: Use Nginx or Apache for SSL termination

## 🔧 Troubleshooting

### Common Issues

1. **Database Connection**:
   - Verify database is running
   - Check connection credentials
   - Ensure database schema exists

2. **SAML Configuration**:
   - Verify IdP metadata is correct
   - Check certificate validity
   - Ensure entity IDs match

3. **Port Conflicts**:
   - Change port in `application.yml`
   - Use `SERVER_PORT` environment variable

### Debug Mode

Enable debug logging:

```yaml
logging:
  level:
    com.example: DEBUG
    org.springframework.security: DEBUG
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For support and questions:

1. Check the troubleshooting section
2. Review the logs
3. Create an issue in the repository
4. Contact the development team

## 🔄 Version History

- **v1.0.0**: Initial Spring Boot migration
  - Complete rewrite from servlet-based application
  - Spring Security SAML2 integration
  - Modern UI with Thymeleaf
  - Database-driven configuration
  - Comprehensive logging and monitoring

---

**Note**: This application is a production-ready migration from the original servlet-based SAML application. It maintains compatibility with existing IdP configurations while providing modern Spring Boot features and improved security. 