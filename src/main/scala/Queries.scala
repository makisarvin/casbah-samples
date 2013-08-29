package com.arvanitis

import com.mongodb.casbah.Imports._


case class Trip(name: String, tags: List[String], country: String)


object Queries extends App {

  val mongoClient = MongoClient()
  // get the DB
  val mongoDB = mongoClient("test")
  // get the collection inside the DB where you will store the data
  val mongoColl = mongoDB("trips")

  val trip_to_greece = Trip("Visit Crete", List("sun", "food", "beach"), "Greece")
  val trip_to_france = Trip("Visit Paris", List("eiffel", "louvre"), "France")


  val tripDb1 = writer(trip_to_greece)

  mongoColl += tripDb1

  val tripDb2 = writer(trip_to_france)

  mongoColl += tripDb2

  //now build the query


 // val query = MongoDBObject("tags" -> "sun")
  val params: List[String] = List("sun", "food")
  //val query = ("tags" $in ("sun", "food"))
  val query = ("tags" $in params)

  println("results ")
  val trips = for (x <- mongoColl.find(query)) println(x)




  def writer(trip: Trip): DBObject = {
    val builder = MongoDBObject.newBuilder
    builder += "name" -> trip.name
    builder += "tags" -> trip.tags
    builder += "country" -> trip.country

    builder.result()
  }

  def reader(trip: mongoColl.T): Trip = {
    def parseTags(tagsJson: Option[MongoDBList]): List[String] = tagsJson match {
      case None => List()
      case Some(tags) => tags.map{ case t: String => t}.toList
    }

    Trip(
      trip.getAsOrElse[String]("name", ""),
      parseTags(trip.getAs[MongoDBList]("tags")),
      trip.getAsOrElse[String]("country", "")
    )
  }

}
