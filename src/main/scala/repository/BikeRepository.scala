// package bikeshop.repository

// import cats.effect.IO
// import doobie.util.transactor.Transactor
// import bikeshop.domain.bike._
// import doobie._
// import doobie.implicits._


// class BikeRepository(transactor: Transactor[IO]) {

// def findAllBikes : IO[List[Bike]] = {
//   val query = sql"select model,brand,year, price,size, id from bikes".query[Bike]
//   val action = query.to[List]
//   action.transact(xa)
//  }

// }
