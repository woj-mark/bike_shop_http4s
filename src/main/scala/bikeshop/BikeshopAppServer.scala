package bikeshop

import bikeshop.domain.brand._
import bikeshop.domain.bike._
import bikeshop.domain.bikeNotFoundError._


//import cats._
import cats.effect._
import cats.implicits._
import org.http4s.circe._

import org.http4s._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.dsl._
import org.http4s.dsl.impl._

import java.time.Year
import scala.util.Try
import scala.collection.mutable
import cats.syntax.either._
import doobie.util.transactor.Transactor
import doobie.implicits._

//mport org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server._
//import org.http4s.blaze.server.BlazeServerBuilder


//The IOApp application enables running a service which uses an IO monad effect
object BikeshopAppServer extends IOApp {

  def bikeRoutesComplete[IO]: HttpApp[F] = {
  bikeRoutes.orNotFound
  }


  //override def run(args: List[String]): IO[ExitCode] = {

    //Transactor returning the IO effect
    val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
  "org.postgresql.Driver",
  "jdbc:postgresql:bikeshop",
  "docker",  // username
  "docker"   // password
)

 //Mounting the routes to the given path
    // def makeRouter(transactor: Transactor[IO]) : Kleisli[IO, Request[IO], Response[IO]] = 
    // Router[IO](
    //   "/api1" -> BikeService.bikeRoutes[IO],
    // ).orNotFound



  val repository = new BikeRepository(xa)


  import scala.concurrent.ExecutionContext.global

  override def run(args: List[String]): IO[ExitCode] = {



   BlazeServerBuilder[IO]
      .bindHttp(8085, "localhost")
      .withHttpApp(new BikeService(repository).bikeRoutes.orNotFound)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
}
}










