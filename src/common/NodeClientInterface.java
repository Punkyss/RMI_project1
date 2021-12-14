package common;

import node.FileNode;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

public interface NodeClientInterface extends Remote{

    public void resetVisited() throws RemoteException;
    public boolean visiteNode() throws RemoteException;
    public void notifyStartClient() throws RemoteException;
    public void registerUserServer(NodeClientInterface client) throws RemoteException;
    public List<FileNode> searchAllNodes(List<FileNode> result, String param, String[] value) throws RemoteException;
    public byte[] searchAllNodesDownload(FileNode nodeFile, byte[]  fileByte) throws IOException;
}
