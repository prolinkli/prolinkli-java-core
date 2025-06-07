# GitHub Wiki Setup Instructions

This folder contains markdown files that should be uploaded to your GitHub repository's wiki.

## 🚀 How to Set Up the Wiki

### Option 1: Manual Upload (Recommended)

1. **Navigate to your GitHub repository**
2. **Click the "Wiki" tab**
3. **Create new pages using the files in `docs/wiki/`**

### Option 2: Clone Wiki Repository

GitHub wikis are actually Git repositories that you can clone:

```bash
# Clone the wiki repository
git clone https://github.com/USERNAME/REPOSITORY.git.wiki.git

# Copy files from docs/wiki/ to the cloned wiki repo
cp docs/wiki/*.md /path/to/cloned/wiki/

# Push changes
cd /path/to/cloned/wiki/
git add .
git commit -m "Add comprehensive documentation"
git push origin master
```

## 📝 Wiki Pages to Create

Copy the content from these files to create the corresponding wiki pages:

| File | Wiki Page Name | Description |
|------|----------------|-------------|
| `Home.md` | Home | Main wiki landing page |
| `Getting-Started.md` | Getting-Started | Platform-specific setup guide |
| `DAO-Framework.md` | DAO-Framework | Complete DAO documentation |

## 🔧 Additional Wiki Pages to Create

You should also create these additional pages for a complete wiki:

1. **Windows-Setup** - Copy content from `Getting-Started.md` Windows section
2. **macOS-Setup** - Copy content from `Getting-Started.md` macOS section  
3. **Linux-Setup** - Copy content from `Getting-Started.md` Linux section
4. **Database-Management** - Create page for Liquibase & MyBatis
5. **Troubleshooting** - Create page for common issues
6. **Development-Guide** - Create page for dev workflows
7. **Configuration** - Create page for app configuration
8. **Contributing** - Create page for contribution guidelines

## 🎯 Wiki Navigation Setup

Once you create the pages, set up navigation in the Wiki sidebar by editing the `_Sidebar.md` file:

```markdown
### Getting Started
- [Home](Home)
- [Getting Started](Getting-Started)
- [Windows Setup](Windows-Setup)
- [macOS Setup](macOS-Setup)
- [Linux Setup](Linux-Setup)

### Framework
- [DAO Framework](DAO-Framework)
- [Database Management](Database-Management)
- [Configuration](Configuration)

### Development
- [Development Guide](Development-Guide)
- [Troubleshooting](Troubleshooting)
- [Contributing](Contributing)
```

## 📚 Content Templates

For pages not yet created, use this template:

```markdown
# Page Title

Brief description of what this page covers.

## Overview

Content overview here.

## Sections

### Section 1
Content here.

### Section 2
Content here.

## Related Pages

- [Related Page 1](Related-Page-1)
- [Related Page 2](Related-Page-2)

---

**Next**: Continue to [Next Page](Next-Page)
```

## 🔗 Link Updates

After creating the wiki, update these files to use the correct wiki URLs:

1. **README.md** - Update wiki links to point to your repository
2. **windows/README.md** - Update wiki link

Replace `../../wiki/` with `https://github.com/YOUR-USERNAME/YOUR-REPO/wiki/`

---

**📝 Note**: The wiki files in this folder are ready to copy-paste into your GitHub wiki pages. 