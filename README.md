<div style="text-align: center;">
  <a href="https://www.openboxes.com">
    <img src="/docs/assets/img/logo-background.png" alt="OpenBoxes logo" width="110">
  </a>
  <h1 style="margin-top:0; padding-top:0">OpenBoxes</h1>
  <h4>A lightweight, open-source inventory and supply chain management system.</h4>

  [![status of develop branch](https://github.com/openboxes/openboxes/actions/workflows/on-change.yml/badge.svg)](https://github.com/openboxes/openboxes/actions/workflows/on-change.yml)
  [![status of documentation build](https://readthedocs.org/projects/openboxes/badge/?version=develop)](https://readthedocs.org/projects/openboxes/?badge=develop)
  [![status of dbdocs build](https://github.com/openboxes/openboxes/actions/workflows/dbdocs.yml/badge.svg)](https://github.com/openboxes/openboxes/actions/workflows/dbdocs.yml)
  [![status of docker build](https://github.com/openboxes/openboxes/actions/workflows/docker-image.yml/badge.svg)](https://github.com/openboxes/openboxes/actions/workflows/docker-image.yml)

  [![User Guide](https://img.shields.io/badge/user%20guide-blue?style=for-the-badge&logo=helpscout&logoColor=white)](https://help.openboxes.com/)
  [![Install Docs](https://img.shields.io/badge/install%20docs-limegreen?style=for-the-badge&logo=materialformkdocs&logoColor=white)](https://docs.openboxes.com/en/latest/)
  [![Contributor Guide](https://img.shields.io/badge/contributor%20guide-grey?style=for-the-badge&logo=gitbook&logoColor=white)](https://openboxes.gitbook.io/contributor-guide/)

  [![Slack](https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=slack&logoColor=white)](http://slack-signup.openboxes.com/)
  [![Forums](https://img.shields.io/badge/support%20forums-orange?style=for-the-badge&logo=discourse&logoColor=white)](https://community.openboxes.com/)

  <br>
  <hr>

  <p>
    <a href="#bulb-about">About</a> •
    <a href="#question-support">Get Support</a> •
    <a href="#handshake-contributing">Contribute</a> •
    <a href="#rocket-quickstart-guide">Quickstart Guide</a> •
    <a href="#page_with_curl-license">License</a>
  </p>
</div>

## 💡 About
OpenBoxes was initially created to assist with inventory management and to digitize the stock movements of emergency medical supplies to healthcare facilities in Haiti in the aftermath of the 2010 earthquake. As such, the application was designed to be able to run smoothly and reliably in low-resource environments, and for it to be quick and intuitive to get up and running.

The application has grown significantly since its conception, and is now used to help run warehouses and healthcare facilities at all scales, all over the world, including in Sierra Leone, Lesotho, Rwanda, the United States, and more!

See [our website](https://openboxes.com/features/) for a list of some of the primary features of the application or check out our [demo site](https://demo.openboxes.com/openboxes/auth/signup) to experiment with the application yourself!

## ❓ Support
Do you need assistance running or using the app? Have you encountered an issue/bug? See our [support guide](/SUPPORT.md) for more information.

## 🤝 Contributing
Are you looking to help out the project? Please see our [contributor guide](https://openboxes.gitbook.io/contributor-guide/).

[![Contributor Guide](https://img.shields.io/badge/contributor%20guide-grey?style=for-the-badge&logo=gitbook&logoColor=white)](https://openboxes.gitbook.io/contributor-guide/)

This project exists thanks to all the generous people who contribute:

<a href="https://github.com/openboxes/openboxes/graphs/contributors"><img src="https://opencollective.com/openboxes/contributors.svg?width=890&button=true" /></a>

You can also [sponsor OpenBoxes with a financial contribution](https://opencollective.com/openboxes/contribute).

We are incredibly grateful for all our financial contributors:

<a href="https://opencollective.com/openboxes/organization/0/website"><img src="https://opencollective.com/openboxes/organization/0/avatar.svg"></a>
<a href="https://opencollective.com/openboxes/organization/1/website"><img src="https://opencollective.com/openboxes/organization/1/avatar.svg"></a>
<a href="https://opencollective.com/openboxes/organization/2/website"><img src="https://opencollective.com/openboxes/organization/2/avatar.svg"></a>
<a href="https://opencollective.com/openboxes/organization/3/website"><img src="https://opencollective.com/openboxes/organization/3/avatar.svg"></a>
<a href="https://opencollective.com/openboxes/organization/4/website"><img src="https://opencollective.com/openboxes/organization/4/avatar.svg"></a>
<a href="https://opencollective.com/openboxes/organization/5/website"><img src="https://opencollective.com/openboxes/organization/5/avatar.svg"></a>
<a href="https://opencollective.com/openboxes/organization/6/website"><img src="https://opencollective.com/openboxes/organization/6/avatar.svg"></a>
<a href="https://opencollective.com/openboxes/organization/7/website"><img src="https://opencollective.com/openboxes/organization/7/avatar.svg"></a>
<a href="https://opencollective.com/openboxes/organization/8/website"><img src="https://opencollective.com/openboxes/organization/8/avatar.svg"></a>
<a href="https://opencollective.com/openboxes/organization/9/website"><img src="https://opencollective.com/openboxes/organization/9/avatar.svg"></a>

## 🚀 Quickstart Guide
The following instructions describe the minimum steps required to build and run the application on a Linux machine. For Mac users, or anyone looking to contribute to the project, please see our [in-depth setup instructions](https://openboxes.gitbook.io/contributor-guide/).

If you're system administrator looking to deploy the application to a production server, you'll want to read through the [installation documentation](http://docs.openboxes.com/en/latest/installation/) instead.

[![Install Docs](https://img.shields.io/badge/install%20docs-limegreen?style=for-the-badge&logo=materialformkdocs&logoColor=white)](https://docs.openboxes.com/en/latest/)

### 1. Install Dependencies
| Dependency                                                                                | Version                                                                                           |
|:------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------|
| [MariaDB](https://mariadb.com/kb/en/mariadb-10-11-4-release-notes/)                       | 10.11.4 ([MySQL 8.0](https://downloads.mysql.com/archives/community/) is a supported alternative) |
| NPM                                                                                       | 6.14.6                                                                                            |
| [Node.js](https://nodejs.org/en/download)                                                 | 14+                                                                                               |
| [Java](https://www.oracle.com/pl/java/technologies/javase/javase8-archive-downloads.html) | 8.0                                                                                               |
| [Grails](https://grails.org/download.html)                                                | 3.3.18                                                                                            |

We recommend using [SDK Man](https://sdkman.io/install) to easily install and manage your SDK versions. With it, you can install Java and Grails using:
```
$ sdk install java 8.0.452-zulu
$ sdk install grails 3.3.18
```

### 2. Configure the database instance
Create the openboxes database:
```
mysql -u root -p -e 'create database openboxes default charset utf8;'
```

Create the "openboxes" user:
```
mysql -u root -p -e 'CREATE USER "openboxes"@"localhost" IDENTIFIED BY "openboxes";'
mysql -u root -p -e 'GRANT ALL ON openboxes.* TO "openboxes"@"localhost";'
```

### 3. Create Openboxes configuration file
If using MySQL, move to the next step. No custom config is required.

If using MariaDB, create the `~/.grails/openboxes.yml` file with the following contents:
```
dataSource.url=jdbc:mariadb://localhost:3306/openboxes?serverTimezone=UTC&useSSL=false
dataSource.driverClassName: org.mariadb.jdbc.Driver
```

### 4. Install NPM dependencies
If you haven't already, [clone the repository](), then cd to the main folder of the project and run: 
```    
npm config set engine-strict true
npm install
```

### 5. Start the application
```
grails run-app
```
This will start the application in development mode using an internal Tomcat instance. This will also build all the remaining dependencies for the project. You may need to run the `grails compile` command beforehand, or run `grails run-app` several times in order to download all dependencies.

### 6. Log into OpenBoxes
Navigate to `http://localhost:8080/openboxes` and log in as an admin using the default credentials (username: `admin` and password: `password`). From there, you can create further accounts as needed.

## 📃 License
This project is licensed under the [Eclipse Public License 1.0](https://opensource.org/license/epl-1-0). See the [LICENSE.md](/LICENSE.md) file for a copy of the full license.

By using or contributing to this software in any fashion, you are agreeing to be bound by the terms of this license.

You must not remove this licensing notice or any other notices from this software.

<div style="text-align: center;">
  <br>
  <hr>
  <a href="#">Back To Top</a>
</div>