package bikeshop.service

import bikeshop.repository.BikeRepository

import org.http4s._

import org.http4s.dsl.Http4sDsl
import cats.effect.IO
import bikeshop.domain.bike._
import io.circe.syntax._
import org.http4s.circe._

class BikeService(bikeRepo: BikeRepository) extends Http4sDsl[IO] {
      
      
//To encode a Scala value of type A into an entity, we need an EntityEncoder[A] in scope
//https://http4s.org/v0.16/json/

  val bikeRoutes=    HttpRoutes.of[IO] {
      //Get all bikes
      case GET -> Root / "bikes" =>
        val allBikes : IO[List[Bike]] = bikeRepo.findAllBikes
        allBikes.flatMap(
            bikes => Ok(bikes.asJson)
        ) 
        }
}   

