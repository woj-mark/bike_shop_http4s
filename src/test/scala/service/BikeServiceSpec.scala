package service

import cats.effect.IO
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import bikeshop.service.BikeService
import bikeshop.repository.BikeRepository
import bikeshop.domain.bike._
import org.http4s.circe._

import cats.effect.unsafe.IORuntime
import org.http4s.{Request, Response, Uri, Status}
import org.http4s.dsl.io._

import io.circe.literal._
import io.circe.Json



class BikeServiceSpec extends AnyWordSpec with MockFactory with Matchers {
  private val repository = stub[BikeRepository]

  private val service = new BikeService(repository).bikeRoutes

  private implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global

   def serve(request: Request[IO]): Response[IO] = {
    service.orNotFound(request).unsafeRunSync()
  }

    "BikeService" should {
    "create a bike" in {
     val id = 16
      val bike = new Bike("Viper","Unibike", 2001, 1040, "XXL",15)
      (repository.addBike _).when(bike).returns(IO.pure(Right(bike.copy(id = id))))
      val createJson = json"""
        {
          "model": ${bike.model},
          "brand": ${bike.brand},
          "year": ${bike.year},
          "price": ${bike.price},
          "size": ${bike.size},
          "id": ${bike.id}
        }"""
      val response = serve(Request[IO](POST, Uri.unsafeFromString("/bikes")).withEntity(createJson))
      response.status shouldBe Status.Created
    }

     "update a bike" in {
      val id = 1
      val bike = new Bike("Viper","B'twin", 2009, 930, "L",15)
      (repository.updateBike _).when(id, bike).returns(IO.pure(Right(bike.copy(id = id))))
      val updateJson = json"""
        {
          "model": ${bike.model},
          "brand": ${bike.brand},
          "year": ${bike.year},
          "price": ${bike.price},
          "size": ${bike.size},
          "id": ${bike.id}
        }"""

      val response = serve(Request[IO](PUT, Uri.unsafeFromString(s"/bikes/$id")).withEntity(updateJson))
      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync() shouldBe json"""
         {
          "model": ${bike.model},
          "brand": ${bike.brand},
          "year": ${bike.year},
          "price": ${bike.price},
          "size": ${bike.size},
          "id": ${id}
        }"""
    }
    
    }
    
}
