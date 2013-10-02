package gridfs

import java.io.{File, FileInputStream}
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.gridfs.Imports._

object GridFSSample {


  def main(args: Array[String]) {

    val mongoClient = MongoClient()

    // get the DB
    val mongoDB = mongoClient("test")

    // Pass the connection to the GridFS class
    val gridfs = GridFS(mongoDB)

    // Save a file to GridFS
    val logo = new FileInputStream("images/new_york.jpg")
    val id = gridfs(logo) { f =>
      f.filename = "feature_new_york_logo.jpg"
      f.contentType = "image/jpg"
    }

    // Find a file in GridFS by its ObjectId
    val myFile = gridfs.findOne(id.get.asInstanceOf[ObjectId])

    val newFileName = "output.jpg";

    myFile.get.writeTo(newFileName)

    // Or find a file in GridFS by its filename
    val myFile1 = gridfs.findOne("feature_new_york_logo.jpg")

    println(myFile1.map(_.size))

    // Print all filenames stored in GridFS
    for (f <- gridfs) println(f.filename)

  }

}
