# ProLinkLi Java Core Framework

A comprehensive Java core framework built with **Spring Boot**, **Maven**, **MyBatis**, and **Liquibase** for rapid enterprise application development.

## 🚀 Quick Start

### Prerequisites
- **Vault** - [Download here](https://developer.hashicorp.com/vault/downloads)
- **Java 17+** - [Download here](https://adoptium.net/)
- **Docker Desktop** - [Download here](https://www.docker.com/products/docker-desktop/)

### Setup & Run

> ⚠️ **IMPORTANT**: Before running this application, you must:
> 
> 1. **Download Vault**: Install HashiCorp Vault from [https://developer.hashicorp.com/vault/downloads](https://developer.hashicorp.com/vault/downloads)
> 2. **Contact @IdentityofSine**: Reach out to [@IdentityofSine](https://github.com/IdentityofSine) to request a Vault account and credentials
> 
> The application requires Vault authentication to access secrets and configuration. Without proper Vault setup, the application will not start correctly.


```bash
# 1. Clone repository
git clone https://github.com/prolinkli/prolinkli-java-core.git
cd prolinkli-java-core

# 2. Start database
./start-db.sh

# 3. Build & run
./mvnw clean install
./start-dev.sh
```

**🪟 Windows Users**: Use scripts in the `windows/` folder or see our [Windows Setup Guide](../../wiki/Windows-Setup).

### Access Points
- **Application**: [http://localhost:8080/v1/api](http://localhost:8080/v1/api)
- **Build Info**: [http://localhost:8080/v1/api/buildinfo](http://localhost:8080/v1/api/buildinfo)

## 📚 Documentation

For comprehensive documentation, tutorials, and guides, visit our **[GitHub Wiki](../../wiki)**:

- **[Getting Started Guide](../../wiki/Getting-Started)** - Detailed setup for all platforms
- **[DAO Framework](../../wiki/DAO-Framework)** - Using the Generic DAO pattern
- **[Database Management](../../wiki/Database-Management)** - Liquibase & MyBatis
- **[Development Guide](../../wiki/Development-Guide)** - Commands & workflows
- **[Troubleshooting](../../wiki/Troubleshooting)** - Common issues & solutions

## 🛠️ Essential Commands

```bash
./start-db.sh         # Start PostgreSQL database
./start-dev.sh        # Start development server
./run-mybatis.sh      # Generate MyBatis code
./run-liquibase.sh    # Clear Liquibase checksums
./mvnw test           # Run tests
```

## 🤝 Contributing

Please read our [Contributing Guide](../../wiki/Contributing) for development setup and guidelines.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**📖 Full documentation available in our [Wiki](../../wiki)**


