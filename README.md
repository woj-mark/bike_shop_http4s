# bike_shop_http4s
## Overview
I've always wanted to run a bikeshop and my current career aspitation is to build software with functional Scala.
To kill two birds with one stone, I've decided to build a simple CRUD service for a bikeshop.
I built it using libraries from Scala Typelevel ecosystem, namely: 
- http4s for the http library
- doobie for accessing a PostgreSQL database 
- circe for JSON parsing.

## Endpoints

| METHOD | URL    | DESCRIPTION    |
| ----- | --- | --- |
| GET | /bikes   | Returns all bikes.   |
| GET | /bikes/{id}   | Returns a bike with the given id, 404 returned  when no todo present with the id provided.|
| POST | /bikes   | Creates a bike, returns a 201 with newly created bike as JSON. Id is autogenatated.  |
| PUT | /bikes/{id}   | Updates an existing bike, returns a 200 with the updated bike if a bike with the given id is present, 404 otherwise.   |
| DELETE | /bikes/{id}  | Deletes a bike with provided id, returns 404 if there's no bike with the provided id   |
