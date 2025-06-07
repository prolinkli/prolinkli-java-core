# Getting Started

Welcome to the ProLinkLi Java Core Framework! This guide will help you set up and run the framework on your development machine.

## 📋 Prerequisites

### Required Software

| Software | Version | Purpose | Download Link |
|----------|---------|---------|---------------|
| **Java JDK** | 17+ | Runtime and compilation | [Adoptium](https://adoptium.net/) |
| **Docker Desktop** | Latest | Database containers | [Docker](https://www.docker.com/products/docker-desktop/) |
| **Git** | Latest | Version control | [Git SCM](https://git-scm.com/) |

### Optional Software

| Software | Purpose | Download Link |
|----------|---------|---------------|
| **Maven** | Build tool (wrapper included) | [Maven](https://maven.apache.org/) |
| **IDE** | Development environment | [IntelliJ IDEA](https://www.jetbrains.com/idea/), [VS Code](https://code.visualstudio.com/), [Eclipse](https://www.eclipse.org/) |

## 🚀 Quick Setup

### Universal Steps (All Platforms)

1. **Clone the Repository**
   ```bash
   git clone https://github.com/prolinkli/prolinkli-java-core.git
   cd prolinkli-java-core
   ```

2. **Start the Database**
   ```bash
   # Linux/macOS
   ./start-db.sh
   
   # Windows (use one of these)
   windows\start-db.bat
   # OR in Git Bash: ./start-db.sh
   # OR direct: docker compose up -d
   ```

3. **Build the Project**
   ```bash
   # Linux/macOS
   ./mvnw clean install
   
   # Windows
   mvnw.cmd clean install
   ```

4. **Start Development Server**
   ```bash
   # Linux/macOS
   ./start-dev.sh
   
   # Windows
   windows\start-dev.bat
   # OR in Git Bash: ./start-dev.sh
   ```

5. **Verify Installation**
   - Application: http://localhost:8080/v1/api
   - Build Info: http://localhost:8080/v1/api/buildinfo

## 💻 Platform-Specific Setup

### 🍎 macOS Setup

<details>
<summary>Click to expand macOS instructions</summary>

#### Install Prerequisites

1. **Install Java 17+ using Homebrew**
   ```bash
   # Install Homebrew if you don't have it
   /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
   
   # Install Java 17
   brew install openjdk@17
   
   # Add to shell profile (.zshrc or .bash_profile)
   echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 17)' >> ~/.zshrc
   echo 'export PATH="$JAVA_HOME/bin:$PATH"' >> ~/.zshrc
   
   # Reload shell configuration
   source ~/.zshrc
   
   # Verify installation
   java -version
   ```

2. **Install Docker Desktop**
   - Download from [Docker Desktop for Mac](https://www.docker.com/products/docker-desktop/)
   - Install and start Docker Desktop
   - Verify with: `docker --version`

3. **Clone and Setup Project**
   ```bash
   git clone https://github.com/prolinkli/prolinkli-java-core.git
   cd prolinkli-java-core
   
   # Make scripts executable
   chmod +x *.sh
   chmod +x bash/*.sh
   
   # Start database
   ./start-db.sh
   
   # Build and run
   ./mvnw clean install
   ./start-dev.sh
   ```

#### macOS Troubleshooting

- **Port conflicts**: Use `lsof -i :8080` to find conflicting processes
- **Permission issues**: Ensure scripts are executable with `chmod +x`
- **Java not found**: Check `JAVA_HOME` environment variable

</details>

### 🐧 Linux Setup

<details>
<summary>Click to expand Linux instructions</summary>

#### Ubuntu/Debian

1. **Install Java 17+**
   ```bash
   # Update package list
   sudo apt update
   
   # Install Java 17
   sudo apt install openjdk-17-jdk
   
   # Verify installation
   java -version
   javac -version
   ```

2. **Install Docker**
   ```bash
   # Install Docker
   sudo apt install docker.io docker-compose
   
   # Add user to docker group (requires logout/login)
   sudo usermod -aG docker $USER
   
   # Start Docker service
   sudo systemctl start docker
   sudo systemctl enable docker
   
   # Verify installation (after logout/login)
   docker --version
   ```

3. **Setup Project**
   ```bash
   git clone https://github.com/prolinkli/prolinkli-java-core.git
   cd prolinkli-java-core
   
   # Make scripts executable
   chmod +x *.sh
   chmod +x bash/*.sh
   
   # Start database
   ./start-db.sh
   
   # Build and run
   ./mvnw clean install
   ./start-dev.sh
   ```

#### CentOS/RHEL/Fedora

1. **Install Java 17+**
   ```bash
   # RHEL/CentOS
   sudo yum install java-17-openjdk-devel
   
   # Fedora
   sudo dnf install java-17-openjdk-devel
   
   # Verify
   java -version
   ```

2. **Install Docker**
   ```bash
   # RHEL/CentOS
   sudo yum install docker docker-compose
   
   # Fedora
   sudo dnf install docker docker-compose
   
   # Start and enable Docker
   sudo systemctl start docker
   sudo systemctl enable docker
   sudo usermod -aG docker $USER
   ```

#### Linux Troubleshooting

- **Permission denied on Docker**: Ensure user is in docker group and logout/login
- **Scripts not executable**: Run `chmod +x *.sh bash/*.sh`
- **Port 8080 in use**: `sudo netstat -tlnp | grep :8080` to find conflicting process

</details>

### 🪟 Windows Setup

<details>
<summary>Click to expand Windows instructions</summary>

Windows users have several options for running the framework:

#### Option 1: Windows Batch Scripts (Recommended)

1. **Install Prerequisites**
   - Download and install [Java 17+](https://adoptium.net/)
   - Download and install [Docker Desktop](https://www.docker.com/products/docker-desktop/)
   - Download and install [Git for Windows](https://git-scm.com/)

2. **Setup Project**
   ```cmd
   git clone https://github.com/prolinkli/prolinkli-java-core.git
   cd prolinkli-java-core
   
   # Run automated setup
   cd windows
   setup-windows.bat
   
   # Start development
   start-dev.bat
   ```

#### Option 2: WSL2 (Windows Subsystem for Linux)

1. **Install WSL2**
   ```powershell
   # Run as Administrator
   wsl --install
   
   # Restart computer when prompted
   # Install Ubuntu from Microsoft Store
   ```

2. **Setup in WSL2**
   ```bash
   # In WSL2 terminal, follow Linux instructions above
   sudo apt update
   sudo apt install openjdk-17-jdk docker.io docker-compose
   
   # Clone and setup project
   git clone https://github.com/prolinkli/prolinkli-java-core.git
   cd prolinkli-java-core
   ./start-db.sh
   ./mvnw clean install
   ./start-dev.sh
   ```

#### Option 3: Git Bash

1. **Install Git for Windows** (includes Git Bash)
2. **Run Linux commands in Git Bash**
   ```bash
   git clone https://github.com/prolinkli/prolinkli-java-core.git
   cd prolinkli-java-core
   ./start-db.sh
   ./mvnw clean install
   ./start-dev.sh
   ```

#### Option 4: PowerShell/CMD Direct

```powershell
git clone https://github.com/prolinkli/prolinkli-java-core.git
cd prolinkli-java-core

# Start database
docker compose up -d

# Build and run
mvnw.cmd clean install
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Windows Troubleshooting

- **Java not recognized**: Ensure Java is in PATH environment variable
- **Docker not starting**: Check if Docker Desktop is running
- **Port conflicts**: Use `netstat -ano | findstr :8080` to find conflicting processes
- **Permission issues**: Run command prompt as Administrator if needed

</details>

## 🗄️ Database Configuration

The framework uses PostgreSQL running in Docker with these default settings:

| Setting | Value |
|---------|-------|
| **Host** | localhost |
| **Port** | 6543 |
| **Database** | postgres |
| **Username** | postgres |
| **Password** | docker |

### Database Commands

```bash
# Start database
./start-db.sh                    # Linux/macOS
windows\start-db.bat            # Windows

# Check database status
docker compose ps

# View database logs
docker compose logs postgres

# Stop database
docker compose down

# Stop and remove data (⚠️ destructive)
docker compose down -v
```

## 🔧 Development Workflow

### Essential Commands

```bash
# Database operations
./start-db.sh                   # Start PostgreSQL
./run-liquibase.sh             # Clear Liquibase checksums
./run-mybatis.sh               # Generate MyBatis code

# Development
./start-dev.sh                 # Start with debugging
./mvnw test                    # Run tests
./mvnw package                 # Build JAR

# Docker operations
docker compose logs -f         # View all logs
docker compose restart         # Restart services
```

### IDE Setup

#### IntelliJ IDEA

1. **Import Project**
   - File → Open → Select `pom.xml`
   - Import as Maven project

2. **Configure JDK**
   - File → Project Structure → Project → SDK: Java 17

3. **Enable Debug**
   - Run → Edit Configurations
   - Add Remote JVM Debug
   - Host: localhost, Port: 5005

#### VS Code

1. **Install Extensions**
   - Extension Pack for Java
   - Spring Boot Extension Pack

2. **Open Project**
   - File → Open Folder → Select project directory

3. **Configure Debug**
   - Create `.vscode/launch.json`:
   ```json
   {
     "type": "java",
     "name": "Debug (Attach)",
     "request": "attach",
     "hostName": "localhost",
     "port": 5005
   }
   ```

## 🧪 Verify Your Setup

Run these commands to ensure everything is working:

```bash
# 1. Check Java version
java -version
# Expected: openjdk version "17.x.x" or higher

# 2. Check Docker
docker --version
# Expected: Docker version 20.x.x or higher

# 3. Check database connectivity
docker compose ps
# Expected: postgres container running

# 4. Check application
curl http://localhost:8080/v1/api/buildinfo
# Expected: JSON response with build information

# 5. Check debug port
telnet localhost 5005
# Expected: Connection successful (Ctrl+C to exit)
```

## 🚨 Common Issues

### Database Connection Issues

```bash
# Check if database is running
docker compose ps

# Restart database
docker compose restart postgres

# Check database logs
docker compose logs postgres

# Test connection
docker exec -it postgres_container psql -U postgres -d postgres
```

### Build Issues

```bash
# Clean and rebuild
./mvnw clean compile

# Clear Maven cache
rm -rf ~/.m2/repository/com/prolinkli

# Check Java version
java -version
javac -version
```

### Port Conflicts

```bash
# Linux/macOS - Find process using port 8080
lsof -i :8080
kill -9 <PID>

# Windows - Find and kill process
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

## 📚 Next Steps

Once you have the framework running:

1. **[DAO Framework](DAO-Framework)** - Learn the data access patterns
2. **[Database Management](Database-Management)** - Understand Liquibase and MyBatis
3. **[Development Guide](Development-Guide)** - Explore development workflows
4. **[Configuration](Configuration)** - Customize application settings

## 🆘 Getting Help

- **Documentation**: Browse this wiki for detailed guides
- **Issues**: [Create an issue](https://github.com/prolinkli/prolinkli-java-core/issues) for bugs or questions
- **Discussions**: [GitHub Discussions](https://github.com/prolinkli/prolinkli-java-core/discussions) for general questions

---

**🎉 Congratulations!** You now have the ProLinkLi Java Core Framework running. Start building with our [DAO Framework](DAO-Framework) guide. 