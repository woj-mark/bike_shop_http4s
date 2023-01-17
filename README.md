# bike_shop_http4s
## Overview
I've always wanted to run a bikeshop and my current career aspitation is to build software with functional Scala.
To kill two birds with one stone, I decided to build a simple CRUD service for a bikeshop.
I built it using libraries from Scala Typelevel ecosystem, namely: 
- [http4s](https://http4s.org/) for the http library;
- [doobie](https://tpolecat.github.io/doobie/) for accessing a PostgreSQL database;
- [circe](https://circe.github.io/circe/) for JSON parsing;
- [cats-effect](https://typelevel.org/cats-effect/) to handle side-effects in the program with an IO monad.

## Endpoints
The endpoints provided by the app are as follows:
| METHOD | URL    | DESCRIPTION    |
| ----- | --- | --- |
| GET | /bikes   | Returns all bikes.   |
| GET | /bikes/{id}   | Returns a bike with the given id, 404 returned  when no todo present with the id provided.|
| POST | /bikes   | Creates a bike, returns a 201 with newly created bike as JSON. Id is autogenatated.  |
| PUT | /bikes/{id}   | Updates an existing bike, returns a 200 with the updated bike if a bike with the given id is present, 404 otherwise.   |
| DELETE | /bikes/{id}  | Deletes a bike with provided id, returns 404 if there's no bike with the provided id   |

### Testing the endpoints
Below I provide a few examples on how the endpoints can be hit with curl (or postman), assuming the app runs on port 8080:
- Get all bikes:   ```curl http://localhost:8080/bikes```
- Get a single bike with id (id = 2 used here):  ```curl http://localhost:8080/bikes/2```
- Create a bike: 
 ```curl -X POST http://localhost:8080/bikes -d "{\"model\":\"Viper\",\"brand\":\"Unibike\",\"year\":2021,\"price\":1200,\"size\":\"XXL\",\"id\":1}" ```
 - Update a bike (assuming id=1): ```curl -X PUT http://localhost:8080/bikes/1 -d "{\"model\":\"Roubaix\",\"brand\":\"Romet\",\"year\":1980,\"price\":350,\"size\":\"L\",\"id\":1}```
 - Delete a bike (assuming id =1) 

## How to run
Currently, the app can be run locally. In the near future (once the front-end is complete), I'm planning to deploy the application in the cloud.
It is assumed that the user has [sbt](https://www.scala-sbt.org/), [docker](https://docs.docker.com/get-docker/) and [postgresql](https://www.postgresql.org/download/) installed on their machines.

# Running locally
- Please open the docker directory in the project structure in cdm and start the docker container with : ```docker-compose up```
- You can run the microservice with ```sbt run```. It will listen to the port specifiec in the ```application.conf``` (8080 by default).

 
