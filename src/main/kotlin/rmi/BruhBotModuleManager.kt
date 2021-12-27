package rmi

import shared.Logging
import shared.logger
import java.rmi.registry.LocateRegistry
import java.rmi.server.UnicastRemoteObject

object BruhBotModuleManager: Logging {
    private val logger = logger()
    private const val PORT_STUB = 0
    private const val PORT_REGISTRY = 1099

    fun register(module: BBModule) {
        try {
            logger.info("Registering module '${module.name()}'")
            val stub = UnicastRemoteObject.exportObject(module, PORT_STUB) as BBModule
            logger.debug("Stub exported to port $PORT_STUB")
            val registry = LocateRegistry.getRegistry(PORT_REGISTRY)
            logger.debug("Registry located at port $PORT_REGISTRY")
            registry.rebind(module.name(), stub)
            logger.info("Successfully registered module '${module.name()}'")
        } catch (e: Exception) {
            logger.error("Unable to export module '${module.name()}'")
            logger.error("Trace: ${e.stackTraceToString()}")
        }
    }
}