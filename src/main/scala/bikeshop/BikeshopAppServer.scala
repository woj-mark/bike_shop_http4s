package bikeshop

import bikeshop.repository.BikeRepository
import bikeshop.service.BikeService
import cats.effect._
import config._
import doobie.util.transactor.Transactor
import org.http4s.server.middleware.Logger
import org.http4s.blaze.server.BlazeServerBuilder
import pureconfig._
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.auto._

//The IOApp application enables running a service which uses an IO monad effect
object BikeshopAppServer extends IOApp {

  // Transactor returning the IO effect
  val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql:bikeshop",
    "docker", // username
    "docker" // password
  )

//A helper function to extract the port from config
  private def extractPort(
      config: Either[ConfigReaderFailures, ServiceConf]
  ): Int = {
    config.fold(_ => 8080, r => r.port.number)
  }

  // A helper function to extract the host from config
  private def extractHost(
      config: Either[ConfigReaderFailures, ServiceConf]
  ): String = {
    config.fold(_ => "localhost", r => r.host)
  }

  override def run(args: List[String]): IO[ExitCode] = {

    val repository = new BikeRepository(xa)

    val config: Either[ConfigReaderFailures, ServiceConf] =
      ConfigSource.default.load[ServiceConf]

    val bikeServiceApp = new BikeService(repository).bikeRoutes.orNotFound

    val finalHttpApp =
      Logger.httpApp(logHeaders = true, logBody = true)(bikeServiceApp)

    BlazeServerBuilder[IO]
      .bindHttp(extractPort(config), extractHost(config))
      .withHttpApp(finalHttpApp)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}
