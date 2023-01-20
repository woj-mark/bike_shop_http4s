package bikeshop.service

import bikeshop.repository.BikeRepository
import bikeshop.domain.bikeNotFoundError._

import org.http4s._

import org.http4s.dsl.Http4sDsl
import cats.effect.IO
import bikeshop.domain.bike._
import io.circe.syntax._
import org.http4s.circe._

class BikeService(bikeRepo: BikeRepository) extends Http4sDsl[IO] {

  val bikeRoutes = HttpRoutes.of[IO] {

    // Get all bikes
    case GET -> Root / "bikes" =>
      for {
        allBikesResponse <- Ok(bikeRepo.findAllBikes.map(_.asJson))
      } yield allBikesResponse

    // Get bike by id
    case GET -> Root / "bikes" / IntVar(id) =>
      for {
        bikeWithId <- bikeRepo.findBikeByID(id)
        bikeWithIdResponse <- bikeResult(bikeWithId)
      } yield bikeWithIdResponse

    // Add a new bike
    case req @ POST -> Root / "bikes" =>
      for {
        bike <- req.decodeJson[Bike]
        createdBike <- bikeRepo.addBike(bike)
        createdBikeResponse <- bikeAddedResult(createdBike)
      } yield createdBikeResponse

    // Update an existing bike
    case req @ PUT -> Root / "bikes" / IntVar(id) =>
      for {
        bike <- req.decodeJson[Bike]
        updatedBike <- bikeRepo.updateBike(id, bike)
        updatedBikeResponse <- bikeResult(updatedBike)
      } yield updatedBikeResponse

    case DELETE -> Root / "bikes" / IntVar(id) =>
      bikeRepo.deleteBike(id).flatMap {
        case Left(error) => NotFound(error.message)
        case Right(_)    => NoContent()
      }
  }

  // A helper method to extract the response values from Either for Not
  private def bikeResult(data: Either[BikeNotFoundError, Bike]) = {
    data match {
      case Left(error)    => NotFound(error.message)
      case Right(correct) => Ok(correct.asJson)
    }
  }

  // A helper method to extract the response values from Either for Not
  private def bikeAddedResult(data: Either[BikeNotFoundError, Bike]) = {
    data match {
      case Left(error)    => NotFound(error.message)
      case Right(correct) => Created(correct.asJson)
    }
  }
}
