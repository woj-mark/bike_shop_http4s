package bikeshop.repository

import cats.effect.IO
import doobie.util.transactor.Transactor
import bikeshop.domain.bike._
import doobie.implicits._
import bikeshop.domain.bikeNotFoundError._
//import bikeshop.service.bikeNotFoundError._



class BikeRepository(transactor: Transactor[IO]) {

  def findAllBikes : IO[List[Bike]] = {
    // As the number of the entries might be unknown it is more safe to use fs2 stream as an in-memory list might be too big.
    val allBikesStream : fs2.Stream[doobie.ConnectionIO, Bike] = sql"select model,brand,year, price,size, id from bikes".query[Bike].stream
    //Compiling the stream into an effect containing a List
    allBikesStream.compile.toList.transact(transactor)
 }

   def findBikeByID(id:Int) : IO[Either[BikeNotFoundError,Bike]] ={
    val query = sql"select model,brand,year, price,size, id from bikes where id=$id".query[Bike]
    //Option used as it's unknown whether the query would return a bike for given ID
      val action = query.option.transact(transactor).map{ 
        case Some(bike) => Right(bike)
        case None => Left(BikeNotFoundError(s"Bike with id=${id} was not found"))
  }
      action
 }

  def findAllBikesModels : IO[List[String]] = {
    val query = sql"select model from bikes".query[String]
    val action = query.to[List]
    action.transact(transactor)
 }

   def addBike(bike:Bike) : IO[Either[BikeNotFoundError,Bike]]= {
    //Returns the autogenerated id for the newly added bike
    val query = sql"insert into bikes (model,brand,year,price,size) values (${bike.model},${bike.brand},${bike.year},${bike.price},${bike.size})"
    query.update.withUniqueGeneratedKeys[Int]("id").transact(transactor).map{ id =>
      if (id > 0) {
      Right(new Bike(bike.model, bike.brand, bike.year,bike.price, bike.brand, id ))} 
      else{
      Left(BikeNotFoundError("Bike couldn't be added to the catalogue"))
      }
  }
  }

  def deleteBike(id: Int): IO[Either[BikeNotFoundError, String]] = {
    val query = sql"DELETE FROM bikes WHERE id = $id".update.run.transact(transactor)
    query.map { numberOfRows =>
      if (numberOfRows == 1) {
        Right((s"Bike (id=${id}) has been successfully deleted."))
      } else {
        Left(BikeNotFoundError(s"Bike with  id=${id} wasn't found- cannot be deleted."))
      }
    }
  }

  def updateBike(id: Int, bike: Bike): IO[Either[BikeNotFoundError, Bike]] ={
   val query = sql"UPDATE bikes SET model = ${bike.model}, brand = ${bike.brand}, year = ${bike.year}, price = ${bike.price}, size =${bike.size} WHERE id = $id"
   val affectedRows = query.update.run.transact(transactor)
   affectedRows.map{rows =>
     if (rows == 1) {
        Right(bike.copy(id = id))} 
     else {
        Left(BikeNotFoundError(s"Bike with  id=${id} wasn't found."))}
}
}
 
}
