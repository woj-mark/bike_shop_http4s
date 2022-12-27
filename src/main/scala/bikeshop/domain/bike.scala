package bikeshop.domain

object bike{
   case class Bike(
    model: String,
    brand : String,
    year: Int,
    price : Int,
    size: String,
    id: Int)

    import io.circe.Encoder
  import io.circe.generic.semiauto.deriveEncoder

  implicit val bikeEncoder: Encoder[Bike] = deriveEncoder[Bike]
}


