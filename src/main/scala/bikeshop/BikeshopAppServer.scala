package bikeshop


//import cats._
import cats.effect._
import doobie.util.transactor.Transactor
import bikeshop.repository.BikeRepository
import bikeshop.service.BikeService
import org.http4s.blaze.server.BlazeServerBuilder
import config._
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.error.ConfigReaderFailures



//The IOApp application enables running a service which uses an IO monad effect
object BikeshopAppServer extends IOApp {


    //Transactor returning the IO effect
    val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
  "org.postgresql.Driver",
  "jdbc:postgresql:bikeshop",
  "docker",  // username
  "docker"   // password
)

   private def extractPort(config : Either[ConfigReaderFailures, ServiceConf]) : Int = {
    config.fold(
  _ => 8080,
  r => r.port.number)
   }

   
   private def extractHost(config : Either[ConfigReaderFailures, ServiceConf]) : String = {
    config.fold(
  _ => "localhost",
  r => r.host)
   }


  override def run(args: List[String]): IO[ExitCode] = {

  val repository = new BikeRepository(xa)

  val config : Either[ConfigReaderFailures, ServiceConf] = ConfigSource.default.load[ServiceConf]
  


  

   BlazeServerBuilder[IO]
      .bindHttp(extractPort(config), extractHost(config))
      .withHttpApp(new BikeService(repository).bikeRoutes.orNotFound)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
}
}











