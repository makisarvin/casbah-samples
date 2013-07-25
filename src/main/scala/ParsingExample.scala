package com.arvanitis

import org.bson.{BSON, Transformer}
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.MongoConversionHelper

// The business classes for the example
case class Offer(name: String, product:Option[Product])
case class Product(name: String, description: String, price: Double)

/**
 * Create the deserializer. Extend the MongoConversionHelper class and
 * override the register and unregister methods.
 *
 * the addEnconding hook requires a encodeType and a transformer (org.bson.Transformer}
 * which in turn allows you to define your custom tranform method.
 */
trait ProductDeserializer extends MongoConversionHelper {
  private val encodeType = classOf[Product]

  private val transformer = new Transformer {

    def transform(o: AnyRef): AnyRef = o match {
      case p:MongoDBObject =>  Product(
          p.getAsOrElse[String]("name", ""),
          p.getAsOrElse[String]("description", ""),
          p.getAsOrElse[Double]("price", 0.0)
        )
      case _ => o
    }
  }

  override def register() = {
    BSON.addDecodingHook(encodeType, transformer)
    super.register()
  }

  override def unregister() = {
    BSON.removeDecodingHooks(encodeType)
    super.unregister()
  }
}

/**
 * The serializer works the same fashion as the deserializer but the transform function
 * does the opposite
 */
trait ProductSerializer extends MongoConversionHelper {
  private val encodeType = classOf[Product]

  private val transformer = new Transformer {

    def transform(o: AnyRef): AnyRef = o match {
      case p:Product =>  {
        val builder = MongoDBObject.newBuilder
        builder += "name" -> p.name
        builder += "description" -> p.description
        builder += "price" -> p.price

        builder.result()
      }
      case _ => o
    }
  }

  override def register() = {
    BSON.addDecodingHook(encodeType, transformer)
    super.register()
  }

  override def unregister() = {
    BSON.removeDecodingHooks(encodeType)
    super.unregister()
  }
}

// Utility objects to easily register the serializer and deserializer.
object RegisterProductDeserializer extends ProductDeserializer {
  def apply() = super.register()
}
object RegisterProductSerializer extends ProductSerializer {
  def apply() = super.register()
}


/**
 * Sample code
 */
object Product extends App {

  RegisterProductSerializer()
  RegisterProductDeserializer()

  val mongoClient = MongoClient()
  // get the DB
  val mongoDB = mongoClient("test")
  // get the collection inside the DB where you will store the data
  val mongoColl = mongoDB("products")

  val product  = Product("Milk", "Just Milk", 1.99)
  val offer = Offer("good offer", Some(product))

  val offerDB = writer(offer)

  mongoColl += offerDB

  val offerJson = mongoColl.findOneByID(offerDB.get("_id"))

  val offer2 = reader(offerJson.get)
  println(offer2)

  //See how we serialize the product now
  def writer(offer: Offer): DBObject = {
    val builder = MongoDBObject.newBuilder
    builder += "name" -> offer.name
    builder += "product" -> offer.product

    builder.result()
  }

  //See how we deserialize the product. the getAs[Product] works as expected
  def reader(offerDB: mongoColl.T): Offer = {
    Offer(
      offerDB.getAsOrElse[String]("name", ""),
      offerDB.getAs[Product]("product")
    )
  }
}