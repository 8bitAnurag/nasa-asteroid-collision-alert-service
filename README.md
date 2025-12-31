# ☄️ NASA Asteroid Collision Alert Service

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.0-green?logo=springboot&logoColor=white) ![Kafka](https://img.shields.io/badge/Apache_Kafka-Event_Streaming-red?logo=apachekafka) ![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker&logoColor=white)
![NASA](https://img.shields.io/badge/NASA-NeoWs_API-0B3D91?logo=nasa&logoColor=white) ![Gmail](https://img.shields.io/badge/Gmail-SMTP_Alerts-D14836?logo=gmail&logoColor=white)

A real-time microservices application that monitors Near-Earth Objects (NEOs) using NASA's NeoWs API. It identifies potentially hazardous asteroids and alerts registered users via email using an event-driven architecture.

---

## 📑 Table of Contents
1. [About the Project](#-about-the-project)
2. [Tech Stack](#-tech-stack)
3. [System Architecture & Communication](#-system-architecture--communication)
4. [Key Components & Class Structure](#-key-components--class-structure)
5. [How to Run Locally](#-how-to-run-locally)

---

## 🔭 About the Project

This project decouples the **fetching of data** from the **processing/alerting of data** using Apache Kafka.
* **The Problem:** Polling NASA's API and sending emails in a single monolith can be slow and unreliable.
* **The Solution:**
    1.  **AsteroidService** polls NASA and pushes "Hazardous" events to a Kafka topic.
    2.  **NotificationService** listens to that topic and handles the heavy lifting of looking up users and sending emails.

---

## 🛠 Tech Stack

* **Core:** Java 21, Spring Boot 4
* **Messaging:** Apache Kafka (Zookeeper & Broker)
* **Emailing:** SMTP External API
* **Database:** MySQL (for storing user subscriptions)
* **External API:** NASA NeoWs (Near Earth Object Web Service)
* **Containerization:** Docker & Docker Compose
* **Build Tool:** Maven

---

## 🧩 System Architecture & Communication

The two microservices communicate asynchronously via **Kafka Topics**.

### Communication Flow
1.  **Asteroid Service** fetches data from NASA.
2.  If an asteroid is `isPotentiallyHazardous`, it creates a JSON event.
3.  The event is pushed to the `asteroid-topic` in Kafka.
4.  **Notification Service** consumes this message.
5.  It fetches all subscribed emails from MySQL and triggers an SMTP email.

```mermaid
flowchart LR
    %% Styling Definitions
    classDef nasa fill:#4285F4,stroke:#fff,stroke-width:2px,color:white,rx:10,ry:10;
    classDef service fill:#34A853,stroke:#fff,stroke-width:2px,color:white,rx:5,ry:5;
    classDef kafka fill:#FABB05,stroke:#fff,stroke-width:4px,stroke-dasharray: 5 5,color:black,rx:50,ry:50;
    classDef database fill:#EA4335,stroke:#fff,stroke-width:2px,color:white;
    classDef user fill:#673AB7,stroke:#fff,stroke-width:2px,color:white,rx:10,ry:10;

    %% Nodes
    A[☁️ NASA API]:::nasa
    B[🛠️ Asteroid Service]:::service
    K((🔁 Kafka Topic)):::kafka
    C[📬 Notification Service]:::service
    D[(💾 MySQL DB)]:::database
    E>📩 User Email]:::user

    %% Connections
    A -- "1. JSON Data" --> B
    B -- "2. Publish Hazard Event" --> K
    K -- "3. Consume Event" --> C
    C -- "4. Query Users" --> D
    C -- "5. Send SMTP Alert" --> E

    %% Link Styles (Optional for GitHub, but good for other renderers)
    linkStyle 0 stroke:#4285F4,stroke-width:2px;
    linkStyle 1,2 stroke:#FABB05,stroke-width:3px;
    linkStyle 3 stroke:#EA4335,stroke-width:2px;
    linkStyle 4 stroke:#673AB7,stroke-width:2px;
