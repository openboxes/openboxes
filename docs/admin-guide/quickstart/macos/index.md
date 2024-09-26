# Quickstart on Mac OS

## Instructions

### Step 1: Install Homebrew (if not already installed)
Homebrew is a package manager for macOS which makes installing software easier.

Open Terminal and run:
```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

### Step 2: Install Java 8
Java 8 is required for OpenBoxes.

```bash
brew tap adoptopenjdk/openjdk
brew install --cask adoptopenjdk8
```

### Step 3: Install Tomcat 9
Tomcat 9 will serve as the web server for OpenBoxes.

```bash
brew install tomcat@9
```

After installation, you can start Tomcat with:
```bash
brew services start tomcat@9
```

### Step 4: Install MySQL 8
MySQL is the database management system used by OpenBoxes.

```bash
brew install mysql@8.0
```

After installation, start MySQL with:
```bash
brew services start mysql@8.0
```

Secure your MySQL installation and set the root password:
```bash
mysql_secure_installation
```

### Step 5: Configure MySQL for OpenBoxes

1. Log into MySQL:
    ```bash
    mysql -u root -p
    ```

2. Create a database for OpenBoxes:
    ```sql
    CREATE DATABASE openboxes DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
    ```

3. Create a MySQL user for OpenBoxes and grant all privileges:
    ```sql
    CREATE USER 'openboxesuser'@'localhost' IDENTIFIED BY 'yourpassword';
    GRANT ALL PRIVILEGES ON openboxes.* TO 'openboxesuser'@'localhost';
    FLUSH PRIVILEGES;
    ```

### Step 6: Download OpenBoxes
Download the latest WAR file from the OpenBoxes GitHub releases page.

```bash
curl -L -o openboxes.war https://github.com/openboxes/openboxes/releases/latest/download/openboxes.war
```

### Step 7: Deploy OpenBoxes to Tomcat

1. Move the WAR file to the Tomcat webapps directory:
    ```bash
    mv openboxes.war /usr/local/Cellar/tomcat@9/*/libexec/webapps/
    ```

2. Tomcat will automatically deploy the WAR file. You can check the deployment by visiting:
    ``` 
    http://localhost:8080/openboxes
    ```

### Step 8: Configure OpenBoxes

Edit the configuration file for OpenBoxes:

1. Create a `openboxes-config.groovy` file in the Tomcat webapps directory:
    ```bash
    touch /usr/local/Cellar/tomcat@9/*/libexec/webapps/openboxes/WEB-INF/classes/openboxes-config.groovy
    ```

2. Add the following configuration settings to `openboxes-config.groovy`:

    ```groovy
    dataSource {
        dbCreate = "update" // one of 'create', 'create-drop', 'update'
        url = "jdbc:mysql://localhost:3306/openboxes?useUnicode=yes&characterEncoding=UTF-8&useLegacyDatetimeCode=false&serverTimezone=UTC"
        username = "openboxesuser"
        password = "yourpassword"
    }
    ```

### Step 9: Start OpenBoxes

Ensure Tomcat is running. Open a browser and navigate to:

```
http://localhost:8080/openboxes
```

Follow the setup instructions provided by OpenBoxes to complete the installation.

### Step 10: Verify Installation

After the setup, verify that OpenBoxes is properly installed and functioning by logging in with the default credentials provided during the setup process.

### Additional Configuration

You may want to configure Tomcat to start automatically on system boot and set environment variables for Java and MySQL.

1. To set Tomcat to start on boot:
    ```bash
    sudo brew services start tomcat@9
    ```

2. Add Java and MySQL to your shell profile (e.g., `.bash_profile`, `.zshrc`):
    ```bash
    export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)
    export PATH="/usr/local/opt/mysql@8.0/bin:$PATH"
    ```

This completes the installation process for OpenBoxes on macOS with Java 8, Tomcat 9, and MySQL 8. If you encounter any issues, refer to the OpenBoxes documentation or community forums for additional support.
