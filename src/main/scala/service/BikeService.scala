package bikeshop.service

import bikeshop.repository.BikeRepository

import org.http4s._

import org.http4s.dsl.Http4sDsl
import cats.effect.IO
import bikeshop.domain.bike._


class BikeService(bikeRepo: BikeRepository) extends Http4sDsl[IO] {
      
      
  //Decoder for the incoming JSON to a bike
  //implicit val bikeDecoder: EntityDecoder[ F , Bike] = jsonOf[F , Bike]

  val bikeRoutes=    HttpRoutes.of[IO] {
      //Get all bikes
      case GET -> Root / "bikes" =>
        val allBikes : IO[List[Bike]] = bikeRepo.findAllBikes
        allBikes.flatMap(
            bikes => Ok(bikes)
        ) 
        }
}   



   // case GET -> Root / "bikes" :? BrandQueryParamMatcher(brand) +& YearQueryParamMatcher(optionalYear) => 
    //     val bikeByBrand = findBikesByBrand(brand)

    //     optionalYear match {
    //         case Some(yr) => 
    //             yr.fold(
    //             _ => BadRequest("The given year is not valid"),
    //             {year =>
    //                 val bikesByBrandAndYear = 
    //                     bikeByBrand.filter(_.year == year.getValue)
                    
    //                 Ok(bikesByBrandAndYear.head.asJson)}
    //             )
    //             case None => Ok(bikeByBrand.head.asJson)


    //     }

 

    
//       //Add a new bike
//       case req@POST -> Root / "bikes" => 
//         for{
//         bike <- req.as[Bike]
//         _ = bikes.put(bike.id,bike)
//         res <- Ok(bikes.asJson)
//         } yield res
//   }
//    case GET -> Root / "bikes" =>
//           val returnBikes: List[Bike]= bikes.values.toList

//           returnBikes match {
//             case Nil => NotFound("No bikes in the store")
//             case _ => Ok(returnBikes.asJson)
//           }