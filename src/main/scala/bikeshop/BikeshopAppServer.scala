package bikeshop

import bikeshop.domain.brand._
import bikeshop.domain.bike._


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

//mport org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server._
import org.http4s.blaze.server.BlazeServerBuilder


//The IOApp application enables running a service which uses an IO monad effect
object BikeshopAppServer extends IOApp {

// Temporary in-memory data
  val surly: Bike = Bike(
   "Trucker",
    "Surly",
    2020,
    1100,
    "XL",
101495)

val bikes:mutable.Map[Int,Bike] = mutable.Map(surly.id -> surly)

//helper function
 private def findBikesByBrand(brand: String): List[Bike] =
    bikes.values.filter(_.brand == brand).toList

object BrandQueryParamMatcher extends QueryParamDecoderMatcher[String]("brand")

//Returns the result of the validation process with Validated[ParseFailure, Year]
object YearQueryParamMatcher extends OptionalValidatingQueryParamDecoderMatcher[Year]("year")

//Have to provide a custom decoder for the Year type (DSL provides decoders for basic types only)
implicit val yearQueryParamDecoder: QueryParamDecoder[Year] =
    //emap produces an Either[ParseFailure, U], where U is a value
  QueryParamDecoder[Int].emap { y =>
      Try(Year.of(y))
              .toEither
              .leftMap { tr =>
                //ParseFailure indicatesan error parsing an HTTP Message
                ParseFailure(tr.getMessage, tr.getMessage)
              }
    }

  
def bikeRoutes[F[_] : Concurrent]: HttpRoutes[F] = {
  val dsl = Http4sDsl[F]
  import dsl._

  //Decoder for the incoming JSON to a bike
  implicit val bikeDecoder: EntityDecoder[ F , Bike] = jsonOf[F , Bike]

  HttpRoutes.of[F] {
    case GET -> Root / "bikes" :? BrandQueryParamMatcher(brand) +& YearQueryParamMatcher(optionalYear) => 
        val bikeByBrand = findBikesByBrand(brand)

        optionalYear match {
            case Some(yr) => 
                yr.fold(
                _ => BadRequest("The given year is not valid"),
                {year =>
                    val bikesByBrandAndYear = 
                        bikeByBrand.filter(_.year == year.getValue)
                    
                    Ok(bikesByBrandAndYear.head.asJson)}
                )
                case None => Ok(bikeByBrand.head.asJson)


        }

      //Get all bikes
       case GET -> Root / "bikes" =>
          val returnBikes: List[Bike]= bikes.values.toList

          returnBikes match {
            case Nil => NotFound("No bikes in the store")
            case _ => Ok(returnBikes.asJson)
          }

    
      //Add a new bike
      case req@POST -> Root / "bikes" => 
        for{
        bike <- req.as[Bike]
        _ = bikes.put(bike.id,bike)
        res <- Ok(bikes.asJson)
        } yield res
  }
} 
      
        
    


        



val brands :  mutable.Map[String, Brand] = 
    mutable.Map("Surly" -> Brand("Surly"), "Giant" -> Brand("Giant"))

val brandsList : List[Brand] = brands.keys.toList.map(name => Brand(name))

def brandRoutes[F[_] : Concurrent]: HttpRoutes[F] = {
  val dsl = Http4sDsl[F]
  import dsl._

  //Decoder for the incoming JSON to a Brand
implicit val brandDecoder: EntityDecoder[F, Brand] = jsonOf[F, Brand]
//implicit val brandEncoder: EntityEncoder[F, Brand] = jsonEncoderOf[F, Brand]

  HttpRoutes.of[F] {
    //Get all brands
    case GET -> Root / "brands"  => 
        brandsList match {
            case Nil => NotFound("No brands available")
            case _ => Ok(brandsList.asJson)

        }

    //Add a new brand
    case req@POST -> Root / "brands" => 
        for{
        brand <- req.as[Brand]
        _ = brands.put(brand.toString,brand)
        res <- Ok(brands.asJson)
        } yield res
  }
}


//Combining the brand and bike routes (semigroups of Kleisli (effect)) into one semigroup
def allRoutes[F[_] : Concurrent]: HttpRoutes[F] = {
  import cats.syntax.semigroupk._
  // combineK (for Kleisli type) <+> operator returns a combined semigroup 
  bikeRoutes[F] <+> brandRoutes[F]
}


  def allRoutesComplete[F[_] : Concurrent]: HttpApp[F] = {
    allRoutes.orNotFound
  }


  override def run(args: List[String]): IO[ExitCode] = {


    //Mounting the routes to the given paths
    val apis = Router(
      "/api1" -> BikeshopAppServer.bikeRoutes[IO],
      "/api2" -> BikeshopAppServer.brandRoutes[IO]
    ).orNotFound


  //ExecutionContext needed to handle incoming requests concurrently
 //import scala.concurrent.ExecutionContext.global


    //The builder is bound to an en effects as the execution of the service
    // may lead to side effects. The effect is bound to an IO modad (cats-effect)
    BlazeServerBuilder[IO]
      .bindHttp(8083, "localhost")
      .withHttpApp(apis)
      .resource
      .use(_ => IO.never)
      //The last statement to map the result of the IO
      .as(ExitCode.Success)
  

}





}




