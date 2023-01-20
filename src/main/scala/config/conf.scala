package config

case class Port(number: Int) extends AnyVal

case class ServiceConf(host: String, port: Port);
