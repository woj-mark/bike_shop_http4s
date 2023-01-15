package config

// import pureconfig._
// import pureconfig.generic.auto._



case class Port(number: Int) extends AnyVal



case class ServiceConf(host: String, port: Port);
  


 