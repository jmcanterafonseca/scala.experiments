package fiware

import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils

object NgsiWriter {

  def write(endpoint:String,map:Map[String,Any]) = {
    val payload = Map("actionType" -> "append","entities" -> List(map))
    val json = JsonSerializer.serialize(payload)

    println(json)

    // create an HttpPost object
    val post = new HttpPost(endpoint + "/v2/op/update")

    // set the Content-type
    post.setHeader("Content-type", "application/json")

    // add the JSON as a StringEntity
    post.setEntity(new StringEntity(json))

    // send the post request
    val response = (new DefaultHttpClient).execute(post)

    // print the response headers
    println("--- HEADERS ---")
    response.getAllHeaders.foreach(arg => println(arg))

    println(response.getStatusLine.getStatusCode)
  }

}
