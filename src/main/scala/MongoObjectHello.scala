import com.mongodb.casbah.Imports._

/**
 * Created by arvange on 10/02/14.
 */

case class Person(id:Option[String], name:String, email: Option[String])

object MongoObjectHello extends App {

  val mongoClient = MongoClient()

  // get the DB
  val mongoDB = mongoClient("test")

  // get the collection inside the DB where you will store the data
  val mongoColl = mongoDB("test_data")

  def parse(person: mongoColl.T): Person = {
    Person(
      id = Some(person("_id").toString),
      person.getAsOrElse[String]("name", "no name"),
      person.getAs[String]("email")
    )
  }

  def transform(person: Person): DBObject = {
    val builder = MongoDBObject.newBuilder
    builder += "name" -> person.name
    builder += "email" -> person.email
    builder.result()
  }

  //arbirtary object
  val newObj = MongoDBObject("a" -> "1")
  val p = Person(None, "Bob", Some("email@gmail.com"))
  val p2 = Person(None, "John", None)

  mongoColl += newObj
  mongoColl += transform(p)
  mongoColl += transform(p2)

  for( x <- mongoColl) println(parse(x))

}
