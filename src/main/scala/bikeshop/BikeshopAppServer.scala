package bikeshop


//import cats._
import cats.effect._
import doobie.util.transactor.Transactor
import bikeshop.repository.BikeRepository
import bikeshop.service.BikeService
import org.http4s.blaze.server.BlazeServerBuilder


//The IOApp application enables running a service which uses an IO monad effect
object BikeshopAppServer extends IOApp {


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


  //import scala.concurrent.ExecutionContext.global

  override def run(args: List[String]): IO[ExitCode] = {



   BlazeServerBuilder[IO]
      .bindHttp(8085, "localhost")
      .withHttpApp(new BikeService(repository).bikeRoutes.orNotFound)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
}
}










