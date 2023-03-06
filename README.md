<img src = "resources/logo.png" title = "logo" width = "50%"/>

# InterSystems SAP Service

An InterSystems SAP Business Service to receive from a SAP System.

---

-   [Overview](#overview)
-   [Dependencies](#dependencies)
-   [Installation](#installation)
-   [Settings](#settings)
    -   [SAP Service Settings](#sap-service-settings)
    -   [SAP Server Connection](#sap-server-connection)
    -   [SAP Client Connection](#sap-client-connection)
    -   [Logging](#logging)
-   [Bugs](#bugs)
-   [Release Notes](#release-notes)

---

## Overview

![SAPService_Details](resources/SAPService_Details.jpg)

---

## Dependencies

-   [sapco.jar](https://support.sap.com/en/product/connectors/jco.html) _3.0.11 or higher_
-   intersystems-jdbc.jar _3.3.1 or higher_
-   intersystems-utils.jar _3.0.0 or higher_

> **Note:** The _intersystems-jdbc.jar_ and _intersystems-utils.jar_ are included in the InterSystems IRIS installation. You can find them in the `~/dev/java` folder.

---

## Installation

-   1 Download the jar file from the [latest release]()
-   2 Create an [external java language server](https://docs.intersystems.com/irisforhealthlatest/csp/docbook/DocBook.UI.Page.cls?KEY=BEXTSERV_managing)
    -   2.1 Open the Management Portal
    -   2.2 Go to `System Administration` > `Configuration` > `Connectivity` > `External Language Servers`
    -   2.3 You can create a new server by clicking on the `Create External Language Server` button or use the default `%Java Server`. (When you want to create a new server, make sure to select the `Java` language and use java version 1.8 or 11)
    -   2.4 Click on `Start`
-   3 [Register the pex component](https://docs.intersystems.com/irislatest/csp/docbook/DocBook.UI.Page.cls?KEY=EPEX_register)
    -   3.1 Open the Management Portal
    -   3.2 Go to `Interoperability` > `Configure` > `Production EXtensions Components`
    -   3.3 Click on `Register New Component`
    -   3.4 In the field `Remote Classname` enter `ASPB.sap.XMLService` or `ASPB.sap.JSONService`
    -   3.5 In the field `Proxy Name` the same name as in the `Remote Classname` field will be used by default (you can change it if you want)
    -   3.6 Select the java language server from step 2 in the field `External Language Server`
    -   3.7 In the field `Gateway Extra CLASSPATH` add the jar file from step 1
    -   3.8 Click on `Register`
    -   3.9 Click on `Refresh`
-   4 [Create a new service](https://docs.intersystems.com/irislatest/csp/docbook/DocBook.UI.Page.cls?KEY=AFL_productions#AFL_productions_explore_simple)
    -   4.1 Open the Management Portal
    -   4.2 Go to `Interoperability` > `Configure` > `Production`
    -   4.3 Create a new production or open an existing one
    -   4.4 Click on the `+` button next to the _Services_
    -   4.5 In the field `Service Class` select the class from step 3.5
    -   4.6 Add a Service Name
    -   4.7 Click on `OK`
    -   4.8 Configure the [settings](#settings)

> **Note:** Step 3 is namespace specific. If you want to use the service in another namespace, you have to repeat step 3 in this namespace.

---

## Settings

### SAP Service Settings

-   `BusinessPartner` _string, required_ - The Business Partner to send the data to in the InterSystems Production
-   `BurstSize` _integer_ - The number of messages to send in one burst if there are enough elements in the buffer. A Burst will be sent after ever `Basic Settings: Call Interval` seconds. You can set the Burst size to 0 and all elements from the buffer will be sent in one Burst. _(default: 10)_
-   `MaxBufferSize` _integer_ - The maximum number of messages to store in the buffer. You can set the Buffer size to 0 for an unlimited Buffer. _(default: 100)_

### SAP Server Connection

-   `GatewayHost` _string, required_ - The host address of the SAP Gateway
-   `GatewayService` _string, required_ - The service name of the SAP Gateway. Usually `sapgwNN` whereas NN is the instance number.
-   `ProgrammID` _string, required_ - The Program ID of this service to identify the connection.
-   `ConnectionCount` _integer, required_ - The number of connections to the SAP Gateway.
-   `Repository` _string, required_ - The name of the SAP Repository.

### SAP Client Connection

-   `HostAddress` _string, required_ - The host address of the SAP System.
-   `ClientID` _string, required_ - The Client ID of your mandant.
-   `SystemNumber` _string, required_ - The System Number of the SAP System.
-   `Username` _string, required_ - The username to connect to the SAP System.
-   `Password` _string, required_ - The password of the user.
-   `Language` _string, required_ - The language of the SAP System.

### Logging

-   `LogFilePath` _string_ - An absolute Path to a file where the service will log the messages. The file must be already created and writeable, the service will **not** create the file. If no or an invalid path is set, the service will not log the messages.
-   `ClearLogOnRestart` _boolean_ - If set to true, the log file will be cleared when the service is restarted. _(default: false)_

---

## Bugs

-   _no known bugs_

---

## [Release Notes](https://github.com/phil1436/intersystems-sap-service/blob/master/CHANGELOG.md)

### [v0.0.1](https://github.com/phil1436/intersystems-sap-service/tree/0.0.1)

-   _Initial release_

---

by Philipp Bonin
