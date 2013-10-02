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

  //now build the queries

  //1st query. Search for a single value in tags
  val query_simple = MongoDBObject("tags" -> "sun")

  println("search for a single value in tags")
  for (x <- mongoColl.find(query_simple)) println(x)

  //2nd query. Search for multiple values in tags. This will search any value in the list
  val params: List[String] = List("sun", "food")
  val query_mult_1 = ("tags" $in params)
  val query_mult_2 = ("tags" $in ("sun", "eiffel"))

  val params_empty = List[String]()
  val query_mult_3 = ("tags" $in params_empty)

  println("Search for multiple values in tags ")

  println("mult_1. Should return Crete")
  for (x <- mongoColl.find(query_mult_1)) println(x)

  println("mult_2. Should return both")
  for (x <- mongoColl.find(query_mult_2)) println(x)

  println("mult_3. Should be empty")
  for (x <- mongoColl.find(query_mult_3)) println(x)

  //3rd query. Search for tags and country

  val query_complex_1 = $and { "country" -> "Greece" :: ("tags" $in ("sun", "eiffel")) }
  println("complex_1. Should be Crete")
  for (x <- mongoColl.find(query_complex_1)) println(x)

  //4th query. Geolocation
  val query_geo_1 = $and { ("country_loc" $near (50,50)) :: ("tags" $in ("sun", "eiffel")) }
  println("complex_1. Should be both")
  //for (x <- mongoColl.find(query_geo_1)) println(x)

  val params_geo = MongoDBObject( "country_loc" ->
    MongoDBObject("$near" ->
      MongoDBObject("$geometry" ->
        MongoDBObject("type" -> "Point",
          "coordinates" -> GeoCoords(50.12, 50.12)),
        "$maxDistance" -> 100)))

  val query_geo_2 = ("country_loc" $nearSphere  (50, 50))
  for (x <- mongoColl.find(query_geo_2)) println(x)
  println("----------TEST------------------")
  for (x <- mongoColl.find(params_geo)) println(x)

  for( x <- mongoColl.find($and { params_geo :: ("tags" $in ("sun", "eiffel")) })) println(x)


  def writer(trip: Trip): DBObject = {

    val location =  MongoDBObject.newBuilder
    location += "type" -> "Point"
    location += "coordinates" -> GeoCoords(50.12, 50.12)

    val builder = MongoDBObject.newBuilder
    builder += "name" -> trip.name
    builder += "tags" -> trip.tags
    builder += "country" -> trip.country
    builder += "country_loc" -> location.result

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
