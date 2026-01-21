package com.dergruenkohl.utils.database


import com.dergruenkohl.utils.getMinecraftUsername
import dev.freya02.botcommands.jda.ktx.messages.Embed
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.MessageEmbed
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.json.json


private val json = Json {
    encodeDefaults = true
}

data class Link (
    val uuid: String,
    val discordId: Long,
    val discordName: String? = null,
    val settings: Settings
){
    suspend fun toEmbed(): MessageEmbed{
        return Embed {
            title = "Link for ${getMinecraftUsername(uuid)}"
            description = """
                **Discord ID:** $discordId
                **Discord Name:** ${discordName ?: "unknown"}
                **Settings:**
                 - **Track**: ${if (settings.track) "Enabled" else "Disabled"}
                 - **Custom Image**: ${if (settings.customImage) "Enabled" else "Disabled"}
                 - **Text Color**: ${settings.textColor ?: "Default"}
                 - **Profile ID**: ${settings.profileID}
            """.trimIndent()
        }
    }
}

@Serializable
data class Settings(
    val uuid: String,
    val track: Boolean = false,
    val customImage: Boolean = false,
    val textColor: String? = null,
    val profileID: String
)
class LinkEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<LinkEntity>(LinkTable)

    var discordId by LinkTable.discordId
    var uuid by LinkTable.uuid
    var discordName by LinkTable.discordName
    var settings by LinkTable.setting

    // Convert DAO to Data class
    fun toLink(): Link = Link(
        discordId = discordId,
        uuid = uuid,
        discordName = discordName,
        settings = settings
    )
}


object LinkTable : LongIdTable("links") {
    val discordId = long("discord_id").uniqueIndex()
    val uuid = varchar("uuid", 36)
    val discordName: Column<String?> = varchar("discord_name", 255).nullable()
    val setting = json("settings", json, Settings.serializer())
}

object LinkRepo{
    init {
        transaction {
            SchemaUtils.create(LinkTable)
        }
    }
    fun getLink(discordId: Long): Link? = transaction {
        LinkEntity.find { LinkTable.discordId eq discordId }.firstOrNull()?.toLink()
    }

    fun createOrUpdateLink(link: Link){
        transaction {
            val existingLink = LinkEntity.find { LinkTable.discordId eq link.discordId }.firstOrNull()
            if (existingLink != null){
                updateLink(link.discordId, link)
            } else {
                createLink(link.discordId, link.uuid, link.discordName ?: "unknown", link.settings)
            }
        }
    }
    fun deleteLink(discordId: Long){
        transaction {
            LinkEntity.find { LinkTable.discordId eq discordId }.firstOrNull()?.delete()
        }
    }

    private fun updateLink(discordId: Long, newLink: Link){
        transaction {
            val link = LinkEntity.find { LinkTable.discordId eq discordId }.firstOrNull()
            link?.uuid = newLink.uuid
            link?.discordName = newLink.discordName
            link?.settings = newLink.settings
        }
    }


    private fun createLink(discordId: Long, uuid: String, discordName: String, settings: Settings){
        transaction {
            LinkEntity.new {
                this.discordId = discordId
                this.uuid = uuid
                this.discordName = discordName
                this.settings = settings
            }
        }
    }

    fun getAllTrack(): List<Link> = transaction {
        LinkEntity.all()
            .map { it.toLink() }
            .filter { it.settings.track }
    }
}
