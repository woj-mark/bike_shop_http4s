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
    mutable.Map("Surly" -> Brand("Surly",1), "Giant" -> Brand("Giant",2))

val brandsList : List[Brand] = brands.keys.toList.zipWithIndex.map{case (name,index) => Brand(name, index)}

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


  //override def run(args: List[String]): IO[ExitCode] = {

    //Transactor returning the IO effect
    val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
  "org.postgresql.Driver",
  "jdbc:postgresql:bikeshop",
  "docker",  // username
  "docker"   // password
)

//
  def finAllBikeModels : IO[List[String]] = {
  val query = sql"select model from bikes".query[String]
  val action = query.to[List]
  action.transact(xa)
 }

// val allBikesStream: fs2.Stream[doobie.ConnectionIO, String] = 
//   sql"select model,brand,year, price,size, id from bikes".query[Bike].stream


   def findAllBikes : IO[List[Bike]] = {
  val allBikesStream : fs2.Stream[doobie.ConnectionIO, Bike] = sql"select model,brand,year, price,size, id from bikes".query[Bike].stream
  allBikesStream.compile.toList.transact(xa)

 }
 def findBikeByID(id:Int) : IO[Either[BikeNotFoundError,Bike]] ={
  val query = sql"select model,brand,year, price,size, id from bikes where id=$id".query[Bike]
  val action = query.option.transact(xa).map{ //Not sure whether the query would return a bike for given ID
      case Some(bike) => Right(bike)
      case None => Left(BikeNotFoundError("Not found"))
  }
  action
 }

   def findAllBikesModels : IO[List[String]] = {
  val query = sql"select model from bikes".query[String]
  val action = query.to[List]
  action.transact(xa)
 }
 

 implicit class Debugger[A](io: IO[A]){
  def debug: IO[A] = io.map{
    a => println(s"[${Thread.currentThread.getName}] $a")
    a
  }
  }

  //As doobie uses Typelevel ecosystm, and we might not know how big is the number of entries
  // it is more safe to use fs2 as list might be too big.
  //Stream being the definition of all values that are query. Then the effect is compiled to a in-memory list.
  //Stream would allow processing a lot of data without bringing it to
val bikeNameStream = sql"select model from bikes".query[String].stream.transact(xa)

def addBike(bike:Bike) : IO[Either[BikeNotFoundError,Int]]= {
  //Returns the autogenerated id for the newly added bike
  val query = sql"insert into bikes (model,brand,year,price,size) values (${bike.model},${bike.brand},${bike.year},${bike.price},${bike.size})"
  query.update.withUniqueGeneratedKeys[Int]("id").transact(xa).map{ id =>
    if (id < 20) {
      Right(id)} 
    else{
      Left(BikeNotFoundError("Not found."))
    }
        
  }
  }

def deleteBike(id: Int): IO[Either[BikeNotFoundError, String]] = {
    val query = sql"DELETE FROM bikes WHERE id = $id".update.run.transact(xa)
    query.map { numberOfRows =>
      if (numberOfRows == 1) {
        Right((s"Bike (id=${id}) has been successfully deleted."))
      } else {
        Left(BikeNotFoundError("Bike not found- cannot be deleted."))
      }
    }
  }

def updateBike(id: Int, bike: Bike): IO[Either[BikeNotFoundError, Bike]] ={
val query = sql"UPDATE bikes SET model = ${bike.model}, brand = ${bike.brand}, year = ${bike.year}, price = ${bike.price}, size =${bike.size} WHERE id = $id"
val affectedRows = query.update.run.transact(xa)
affectedRows.map{rows =>
  if (rows == 1) {Right(bike.copy(id = id))} 
  else {
    Left(BikeNotFoundError(s"No bike with given id=${id} found."))}
}
}

val bike5 = Bike(
   "Swift",
    "Bianchi",
    1996,
    450,
    "L",
6)

 override def run(args: List[String]) : IO[ExitCode] = 
updateBike(4,bike5).debug.as(ExitCode.Success)

    //Mounting the routes to the given paths
    val apis = Router(
      "/api1" -> BikeshopAppServer.bikeRoutes[IO],
      "/api2" -> BikeshopAppServer.brandRoutes[IO]
    ).orNotFound


  //ExecutionContext needed to handle incoming requests concurrently
 //import scala.concurrent.ExecutionContext.global


    // //The builder is bound to an en effects as the execution of the service
    // // may lead to side effects. The effect is bound to an IO modad (cats-effect)
    // BlazeServerBuilder[IO]
    //   .bindHttp(8084, "localhost")
    //   .withHttpApp(apis)
    //   .resource
    //   .use(_ => IO.never)
    //   //The last statement to map the result of the IO
    //   .as(ExitCode.Success)
  

}










