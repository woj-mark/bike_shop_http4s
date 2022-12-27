package bikeshop

import bikeshop.domain.brand._
import bikeshop.domain.bike._


import cats._
import cats.effect._
import cats.implicits._
import org.http4s.circe._

import org.http4s._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.dsl._
import org.http4s.dsl.impl._
import org.http4s.headers._

import java.time.Year
import scala.util.Try
import scala.collection.mutable
import cats.syntax.either._



object BikeshopAppServer extends IOApp {



   
// Temporary in-memory data
  val surly: Bike = Bike(
   "Trucker",
    "Surly",
    2020,
    1100,
    "XL",
101495)

  

val bikes:Map[Int,Bike] = Map(surly.id -> surly)

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

  
def bikeRoutes[F[_] : Monad]: HttpRoutes[F] = {
  val dsl = Http4sDsl[F]
  import dsl._



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


        }}
}


val brands :  mutable.Map[String, Brand] = 
    mutable.Map("Surly" -> Brand("Surly"), "Giant" -> Brand("Giant"))


def brandRoutes[F[_] : Concurrent]: HttpRoutes[F] = {
  val dsl = Http4sDsl[F]
  import dsl._

  //Decoder for the incoming JSON to a Brand
implicit val brandDecoder: EntityDecoder[F, Brand] = jsonOf[F, Brand]

  HttpRoutes.of[F] {
    //Get all brands
    case GET -> Root / "brands"  => 
        brands match {
            case _ => Ok(brands.asJson)
            case Nil => NotFound("No brands available")
        }

    //Add a new brand
    case req@POST -> Root / "brands" => 
        for{
        brand <- req.as[Brand]
        _ = brands.put(brand.toString,brand)
        res <- Ok.headers(`Content-Encoding`(ContentCoding.gzip))
                .map(_.addCookie(ResponseCookie("My-Cookie", "value")))
        } yield res
  }
}

//Combining the brand and bike routes (semigroups of Kleisli (effect)) into one semigroup
def allRoutes[F[_] : Concurrent]: HttpRoutes[F] = {
  import cats.syntax.semigroupk._
  // combineK (for Kleisli type) <+> operator returns a combined semigroup 
  bikeRoutes[F] <+> brandRoutes[F]
}

}







