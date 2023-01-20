package bikeshop.domain

object bike {
  case class Bike(
      model: String,
      brand: String,
      year: Int,
      price: Int,
      size: String,
      id: Int
  )

  import io.circe.{Encoder, Decoder}
  import io.circe.generic.semiauto._

  implicit val bikeEncoder: Encoder[Bike] = deriveEncoder[Bike]
  implicit val bikeDecoder: Decoder[Bike] = deriveDecoder[Bike]

}
