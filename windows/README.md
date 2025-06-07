# Windows Scripts

This folder contains Windows batch files (`.bat`) for easy development on Windows systems.

## 📁 Available Scripts

| Script | Description |
|--------|-------------|
| `setup-windows.bat` | **One-click setup** - Checks prerequisites and sets up the entire project |
| `start-db.bat` | Start the PostgreSQL database using Docker |
| `start-dev.bat` | Start the development server with debugging enabled |
| `run-mybatis.bat` | Generate MyBatis models, mappers, and XML files |
| `run-liquibase.bat` | Clear Liquibase checksums (fixes checksum mismatch errors) |

## 🚀 Quick Start for Windows

1. **Run the setup script**:
   ```cmd
   cd windows
   setup-windows.bat
   ```

2. **Start development**:
   ```cmd
   start-dev.bat
   ```

## 💡 Tips

- **Double-click** any `.bat` file to run it
- Scripts will **pause** on completion so you can see the output
- Scripts automatically detect if you have Maven installed or use the Maven Wrapper
- All scripts include **error handling** and colored output

## 🔧 Requirements

- **Java 17+** installed and in PATH
- **Docker Desktop** running
- **Git** for cloning the repository

---

For detailed Windows setup instructions, see our [Windows Setup Guide](../../../wiki/Windows-Setup). 