# P3-Back

## Description

Ce projet est le backend de l'application de gestion de locations. Il est développé en Java avec le framework Spring Boot.

## Prérequis

- Java 17
- SpringBoot
- MySQL

## Installation

1. Clonez le dépôt :

```sh
git clone https://github.com/AllanLny/JavaangularP3BackEnd
```

2. Installez les dépendances Maven :

```sh
./mvnw clean install
```

3. Configurez la base de données MySQL :

```sh
Créez une base de données MySQL et mettez à jour les informations de connexion dans le fichier application.properties :
spring.datasource.url=jdbc:mysql://localhost:3306/votre_base_de_donnees
spring.datasource.username=votre_nom_utilisateur
spring.datasource.password=votre_mot_de_passe
```

## Démarrage

```sh
./mvnw spring-boot:run
```

L'application sera accessible à l'adresse http://localhost:3001.

## Swagger 

Le Swagger sera accessible à l'adresse http://localhost:3001/swagger-ui/index.html#. 


## Structure 

- src/main/java/com/openclassrooms: Contient le code source Java de l'application.
 
- src/main/resources: Contient les fichiers de configuration et les ressources statiques.
 
- application.properties: Fichier de configuration principal de l'application.
 
- src/main/resources/static: Contient les fichiers statiques.
 
- uploads: Répertoire pour les fichiers téléchargés.


## Dépendances principales 

- Spring Boot
- Spring Data JPA
- Spring Security
- Springdoc OpenAPI
- MySQL Connector

