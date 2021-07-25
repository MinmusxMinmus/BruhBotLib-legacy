package shared

import com.mongodb.client.MongoClients
import org.bson.Document
import org.bson.types.ObjectId
import rmi.BBModule
import simpleCommands.SimpleCommandDeclaration

import com.mongodb.client.model.Filters.*

/*
The objective is to have a way for a module to store shit in database. A command must specify the module it belongs to
to store shit, that is so different modules store stuff in different collections/DBs, so there's no security issues there.

Regardless of what the stored documents hold, they will always be encapsulated by a bigger doc, created by the persistence
manager. These top-level objects will include a 'command' field (to indicate the command that stored this information),
a 'name' field as a descriptive way to tell what the object is, and a 'value' field containing the actual stored doc.
 */

object PersistenceManager: Logging {

    private val logger = logger()

    private var client = MongoClients.create("mongodb://localhost:27017")
    private var db = client.getDatabase("BruhBotRedux")

    fun storeDocument(module: BBModule, command: SimpleCommandDeclaration, document: Document, name: String) {
        logger.info("Storing document '$name' (Module: '${module.name()}', command: '${command.name}')")
        val collection = db.getCollection(module.name())
        val doc = Document(mapOf("command" to command.name, "name" to name, "value" to document))
        collection.insertOne(doc)
        logger.info("Document '$name' stored successfully")
    }

    fun getDocuments(module: BBModule, command: SimpleCommandDeclaration): Set<Pair<String, ObjectId>> {
        logger.info("Retrieving all documents from command '${command.name}' (module: '${module.name()}')")
        val collection = db.getCollection(module.name())
        return collection.find()
            .filter { it["command"] as String != command.name }
            .mapTo(mutableSetOf()) { it["name"] as String to it["_id"] as ObjectId }
            .toSet().also {
                logger.info("Documents from command '${command.name}' retrieved (module: '${module.name()}'). Total collection size: ${it.size}")
            }
    }

    fun getDocument(module: BBModule, command: SimpleCommandDeclaration, id: ObjectId): Document? {
        logger.info("Retrieving document with id: '${id.toHexString()}' (module: '${module.name()}', command: '${command.name}')")
        val collection = db.getCollection(module.name())
        return collection.find(and(eq("_id", id), eq("command", command.name)))
            .firstOrNull()?.let {
                logger.info("Document with id '$id' found in database (name: '${it["name"]}')")
                it["value"] as Document
            } ?: let {
            logger.warn("Document with id '$id' not found in database")
            null
        }
    }
}