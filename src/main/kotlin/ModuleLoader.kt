package remote

import java.rmi.registry.LocateRegistry
import java.rmi.server.UnicastRemoteObject


object ModuleLoader {
    fun register(module: BBModule) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(SecurityManager())
        }
        try {
            val stub = UnicastRemoteObject.exportObject(module, 0) as BBModule
            val registry = LocateRegistry.getRegistry()
            registry.rebind(module.name(), stub)
        } catch (e: Exception) {
            System.err.println("Module exception:")
            e.printStackTrace()
        }
    }
}