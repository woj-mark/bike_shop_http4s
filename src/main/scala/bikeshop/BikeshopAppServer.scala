package bikeshop


//import cats._
import cats.effect._
import doobie.util.transactor.Transactor
import bikeshop.repository.BikeRepository
import bikeshop.service.BikeService
import org.http4s.blaze.server.BlazeServerBuilder


//The IOApp application enables running a service which uses an IO monad effect
object BikeshopAppServer extends IOApp {


    //Transactor returning the IO effect
    val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
  "org.postgresql.Driver",
  "jdbc:postgresql:bikeshop",
  "docker",  // username
  "docker"   // password
)

  override def run(args: List[String]): IO[ExitCode] = {

  val repository = new BikeRepository(xa)

   BlazeServerBuilder[IO]
      .bindHttp(8098, "localhost")
      .withHttpApp(new BikeService(repository).bikeRoutes.orNotFound)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
}
}










