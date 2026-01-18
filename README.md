# Bilibili Microservices Project

This project is a microservices-based application inspired by Bilibili, built primarily with Spring Boot and Spring Cloud. It aims to provide a robust and scalable platform for video sharing and community interaction.

## Features

*   **User Management:** Registration, login, and user profile management.
*   **Authentication & Authorization:** JWT-based authentication with a global gateway filter.
*   **Video Services:** Upload, management, and streaming of video content.
*   **Comment System:** Functionality for users to comment on videos.
*   **Object Storage:** Integration with Minio and Aliyun OSS for efficient storage of media files.
*   **Message Queue:** Asynchronous communication using RabbitMQ for event-driven processes.
*   **Service Discovery & Configuration:** Centralized service registration and configuration management with Nacos.
*   **API Gateway:** Unified entry point for all microservices with Spring Cloud Gateway.

## Tech Stack

This project leverages a modern and powerful tech stack to deliver a high-performance and scalable solution.

### Core Frameworks & Languages

![Java](https://img.shields.io/badge/Java-8+-%23007396.svg?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-2.7.x-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-2021.x-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Spring Cloud Alibaba](https://img.shields.io/badge/Spring_Cloud_Alibaba-2021.x-%23FF6A00.svg?style=for-the-badge&logo=alibabacloud&logoColor=white)

### Infrastructure & Services

![Nacos](https://img.shields.io/badge/Nacos-2.x-%232080FF.svg?style=for-the-badge&logo=alibabacloud&logoColor=white)
![Spring Cloud Gateway](https://img.shields.io/badge/Spring_Cloud_Gateway-2.x-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-%234479A1.svg?style=for-the-badge&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-6.x-%23DC382D.svg?style=for-the-badge&logo=redis&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.x-%23FF6600.svg?style=for-the-badge&logo=rabbitmq&logoColor=white)
![Minio](https://img.shields.io/badge/Minio-8.x-%23FF6A00.svg?style=for-the-badge&logo=minio&logoColor=white)
![Aliyun OSS](https://img.shields.io/badge/Aliyun_OSS-SDK-%23FF6A00.svg?style=for-the-badge&logo=alibabacloud&logoColor=white)

### Libraries & Tools

![MyBatis Plus](https://img.shields.io/badge/MyBatis_Plus-3.x-%234479A1.svg?style=for-the-badge&logo=mybatis&logoColor=white)
![Lombok](https://img.shields.io/badge/Lombok-1.18.x-%23CC0000.svg?style=for-the-badge&logo=lombok&logoColor=white)
![Hutool](https://img.shields.io/badge/Hutool-5.x-%235CB85C.svg?style=for-the-badge&logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNCIgaGVpZ2h0PSIyNCIgdmlld0JveD0iMCAwIDI0IDI0Ij48cGF0aCBmaWxsPSIjNUNCNTg1IiBkPSJNMTAuNzUgMTcuMjVsLTQuNS00LjVsLTQuNS00LjVsNC41LTQuNWw0LjUgNC41bC00LjUgNC41bC00LjUgNC41em02LjUtMTMuNWwtNC41IDQuNWw0LjUgNC41bC00LjUgNC41bC00LjUtNC41bC00LjUtNC41em02LjUtMTMuNWwtNC41IDQuNWw0LjUgNC41bDUuMjUtNS4yNXoiLz48L3N2Zz4=&logoColor=white)
![Fastjson2](https://img.shields.io/badge/Fastjson2-2.x-%23007396.svg?style=for-the-badge&logo=json&logoColor=white)
![JJWT](https://img.shields.io/badge/JJWT-0.11.x-%23000000.svg?style=for-the-badge&logo=jwt&logoColor=white)
![OpenFeign](https://img.shields.io/badge/OpenFeign-11.x-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Spring Security Crypto](https://img.shields.io/badge/Spring_Security_Crypto-5.x-%236DB33F.svg?style=for-the-badge&logo=springsecurity&logoColor=white)
![Aliyun SMS](https://img.shields.io/badge/Aliyun_SMS-SDK-%23FF6A00.svg?style=for-the-badge&logo=alibabacloud&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.x-%23005F0F.svg?style=for-the-badge&logo=thymeleaf&logoColor=white)
![AspectJ](https://img.shields.io/badge/AspectJ-1.9.x-%23888888.svg?style=for-the-badge&logo=eclipse&logoColor=white)

## Getting Started

### Prerequisites

*   Java 8 or higher
*   Maven 3.6+
*   Docker (for running Nacos, MySQL, Redis, RabbitMQ, Minio)

### Installation

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/your-username/bilibili-main.git
    cd bilibili-main
    ```

2.  **Set up local services (Nacos, MySQL, Redis, RabbitMQ, Minio):**
    (Instructions for Docker Compose or manual setup would go here)

3.  **Build the project:**
    ```bash
    mvn clean install
    ```

4.  **Run individual microservices:**
    Each microservice can be run independently. For example:
    ```bash
    java -jar bilibili-gateway/target/bilibili-gateway-1.0-SNAPSHOT.jar
    ```
    Or use your IDE to run the main application class for each module.

## Project Structure

The project is organized into several modules, each representing a microservice or a shared component:

*   `bilibili-main`: Parent POM for managing common dependencies and build configurations.
*   `bilibili-common`: Shared utilities, common entities, and framework components.
*   `bilibili-gateway`: API Gateway for routing requests and handling cross-cutting concerns like authentication.
*   `bilibili-auth`: Authentication and authorization service.
*   `bilibili-user`: User management service.
*   `bilibili-context2inject`: Context injection utilities (e.g., for `X-User-Id`).
*   `bilibili-oss-service`: Object Storage Service for managing files.
*   `bilibili-comments`: Commenting service for videos.

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues.

## License

This project is licensed under the MIT License.