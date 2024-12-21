# kitchensink

## Tech Stack

**Server:** Java 21, Spring Boot 3.4, Docker

Clone the project

```bash
  git clone https://github.com/myselfrizali/kitchensink.git
```

Go to the project directory

```bash
  cd my-project
```

Install dependencies

```bash
  mvn clean install
```

Start mongodb container
```bash
  docker run -it --rm -p 27017:27017 -e MONGO_INITDB_ROOT_USERNAME=mongoadmin -e MONGO_INITDB_ROOT_PASSWORD=secret mongo
```

Start the application

```bash
  cd target
  java -jar kitchensink-0.0.1-SNAPSHOT.jar
```

# Or via docker compose

#### Note: No need to run above mentioned mongodb container
```bash
  docker-compose up --build
```