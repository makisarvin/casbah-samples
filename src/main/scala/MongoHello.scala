package mongo.samples

import com.mongodb.casbah.Imports._


object MongoHello {

  val mongoClient = MongoClient()

  // get the DB
  val mongoDB = mongoClient("test")

  // get the collection inside the DB where you will store the data 
  val mongoColl = mongoDB("test_data")


  // create an object 
  val newObj = MongoDBObject("a" -> "1")

  // store the object in the collection 
  // db.collection.save(newObj)
  mongoColl += newObj


}