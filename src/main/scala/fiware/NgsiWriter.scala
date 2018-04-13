package fiware

import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient


object NgsiWriter {

  def write(endpoint:String, map:Map[String,Any], tenant:String) = {
    val payload = Map("actionType" -> "append","entities" -> List(map))
    val json = JsonSerializer.serialize(payload)

    // create an HttpPost object
    val post = new HttpPost(endpoint + "/v2/op/update")

    post.setHeader("Content-type", "application/json")

    post.setHeader("Fiware-Service", tenant)


    // add the JSON as a StringEntity
    post.setEntity(new StringEntity(json))

    // send the post request
    val response = (new DefaultHttpClient).execute(post)

    println(response.getStatusLine.getStatusCode)
  }

}
