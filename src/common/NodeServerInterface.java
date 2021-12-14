package common;

import node.Node;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.HashMap;

public interface NodeServerInterface extends Remote {


    public void notifyStartServer() throws  RemoteException;

}
