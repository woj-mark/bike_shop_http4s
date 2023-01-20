package bikeshop.domain

/*business domain
The company should have a unique ID and name (cannot be replicated)
 */

object brand {

  case class Brand(name: String, id: Int) {
    override def toString: String = s"$name"
  }
}
