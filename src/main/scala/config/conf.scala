package config



import cats.effect.{IO, Resource};
import com.typesafe.config.ConfigFactory
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._


object Conf{
    case class ServerConfig(host: String ,port: Int);

    case class Conf(server: ServerConfig)

    
    def load(configFile: String = "bikeApp.conf"): Resource[IO, Conf] = {
      Resource.eval(ConfigSource.fromConfig(ConfigFactory.load(configFile)).loadF[IO, Conf]())
  }

}
