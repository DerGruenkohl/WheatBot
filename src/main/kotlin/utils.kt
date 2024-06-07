import kong.unirest.json.JSONObject
import org.apache.commons.io.IOUtils
import java.io.IOException
import java.net.URL

@Throws(IOException::class)
fun getMinecraftUsername(uuid: String): String {
    val url = URL("https://sessionserver.mojang.com/session/minecraft/profile/$uuid")
    val input = url.openConnection().getInputStream()
    val jsonText = IOUtils.toString(input, "UTF-8")
    val json = JSONObject(jsonText)
    return json.getString("name")
}

@Throws(IOException::class)
fun getMinecraftUUID(name: String): String {
    val url = URL("https://api.mojang.com/users/profiles/minecraft/$name")
    val input = url.openConnection().getInputStream()
    val jsonText = IOUtils.toString(input, "UTF-8")
    val json = JSONObject(jsonText)
    return json.getString("id")
}