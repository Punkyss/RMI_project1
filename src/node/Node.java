package node;

import common.NodeClientInterface;
import common.NodeServerInterface;

import java.io.*;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Node extends UnicastRemoteObject implements NodeServerInterface, NodeClientInterface {

    int ownPort;
    String ownData;
    HashMap<String,File> ownDataListRaw;
    ArrayList<FileNode> ownDataList = new ArrayList<>();

    String fatherIP;
    int fatherPort;
    boolean visited=false;

    //list of connected nodes (clients[0] -> is the father if it does have)
    List<NodeClientInterface> clients = new ArrayList<>();


    public Node() throws RemoteException {
    }

    // Initial LOAD DATA
    public void listFilesForFolder(final File folder) throws NoSuchAlgorithmException, IOException {
        HashGen hg = new HashGen();
        ownDataListRaw = new HashMap<>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                //Use SHA-1 algorithm
                MessageDigest shaDigest = MessageDigest.getInstance("SHA-256");
                //SHA-1 checksum
                String shaChecksum = hg.getFileChecksum(shaDigest, fileEntry);

                ownDataListRaw.put(shaChecksum,fileEntry);
                ownDataList.add(new FileNode(shaChecksum,fileEntry.getName()));

            }
        }
    }

    private void readFiles() throws IOException, NoSuchAlgorithmException {
        final File folder = new File(ownData);
        listFilesForFolder(folder);
    }


    // FUNCINALITIES
    private void listenInstruction() throws RemoteException,IOException {
        String instruction = "";

        System.out.println("What to do now:\n 'See OwnData'=1, 'Set files initial info'=2, 'Search'=3, 'Modify Own'=4, 'Delete Own'=5, 'Download'=6");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        instruction = reader.readLine();

        switch (instruction){
            case "1":
                for (FileNode f :ownDataList) {
                    System.out.println(f);
                }
                break;
            case "2":
                initialSetValues();
                break;
            case "3":
                search();
                break;
            case "4":
                modify();
                break;

            case "5":
                delete();
                break;

            case "6":
                download();
                break;

            case "7":
                System.out.println(clients);
                break;

        }
    }

    private List<FileNode> search() throws RemoteException ,IOException {

        System.out.println("for what you want to search: 'name'=1,'description'=2,'keyword'=3");
        BufferedReader readerx3 = new BufferedReader(new InputStreamReader(System.in));
        String i3 = readerx3.readLine();
        List<FileNode> result=new ArrayList<>();
        switch (i3){
            case "1":
                System.out.println("Set the Name of the file you want to search.");
                String i31=readerx3.readLine();
                String[] i31x= i31.split(",");
                result= searchAllNodes(result,i3,i31x);
                resetVisited();
                break;

            case "2":
                System.out.println("Set the Description of the file you want to search.");
                String i32 = readerx3.readLine();
                String[] i32x= i32.split(",");
                result = searchAllNodes(result,i3,i32x);
                resetVisited();
                break;

            case "3":
                System.out.println("Set the Keywords of the file you want to search. ( separated by ',')");
                String i33 = readerx3.readLine();
                String[] i33x= i33.split(",");
                result= searchAllNodes(result,i3,i33x);
                resetVisited();
                break;
        }
        int i=0;
        for (FileNode file: result) {
            i++;
            System.out.println(i+".-\n"+file);
        }
        return result;
    }

    private void initialSetValues() throws IOException, RemoteException  {
        System.out.println("Wich file to fill? ");
        for (FileNode f :ownDataList) {
            System.out.println(f.title+",");
        }
        System.out.println("\n");

        System.out.println("write: ['name','description','keyword1';'keyword2';'keyword3']");
        BufferedReader readerx2 = new BufferedReader(new InputStreamReader(System.in));
        String i2 = readerx2.readLine();
        String[] i22 = i2.split(",");
        for (FileNode f :ownDataList) {
            if(f.title.equals(i22[0])){
                f.description = i22[1];
                f.keywords = i22[2].split(";");
                ownDataList.set(ownDataList.indexOf(f),f);
            }
        }
    }

    private void modify() throws IOException ,RemoteException  {
        System.out.println("Wich file to fill? ");
        for (FileNode f :ownDataList) {
            System.out.println(f.title+"\n");
        }
        System.out.println("\n");

        System.out.println("write from the file wich want to be modified: ['name']");
        BufferedReader reader2 = new BufferedReader(new InputStreamReader(System.in));
        String i2 = reader2.readLine();
        for (FileNode f :ownDataList) {
            if(f.title.equals(i2)){
                System.out.println("write: ['name','description','keyword1'.'keyword2'.'keyword3']");
                String i22 = reader2.readLine();
                String[] i22x = i22.split(",");
                f.title=i22x[0];
                f.description=i22x[1];
                f.keywords = i22x[2].split(",");
                ownDataList.set(ownDataList.indexOf(f),f);

                ///falte modificar el fixer fisic amb el hash (com a molt cambiar el nom)
                // de la llista ownDataListRaw i del fixer de la carpeta del pc !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!FALTE TESTEJAR
                String newPath= ownDataListRaw.get(f.hash).getPath();
                int size=ownDataListRaw.get(f.hash).getName().length()-1;
                newPath.substring(0, newPath.length() - size);
                File fnew = new File(newPath);
                ownDataListRaw.get(f.hash).renameTo(fnew);
                ///
            }
        }
    }

    private void delete() throws IOException ,RemoteException  {
        System.out.println("Wich file to fill? ");
        for (FileNode f :ownDataList) {
            System.out.println(f.title+"\n");
        }
        System.out.println("\n");

        System.out.println("write: ['name']");
        BufferedReader readerx2 = new BufferedReader(new InputStreamReader(System.in));
        String i2 = readerx2.readLine();
        String[] i22 = i2.split(",");
        for (FileNode f :ownDataList) {
            if(f.title.equals(i22[0])){
                ownDataList.remove(f);
                ownDataListRaw.get(f.hash).delete();
                ownDataListRaw.remove(f.hash);
            }
        }

    }

    private void download() throws IOException {

        List<FileNode> res = search();
        resetVisited();

        if(!res.isEmpty()){
            System.out.println("What dumber do you want to download? ");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String number = reader.readLine();
            byte[] resultFile = searchAllNodesDownload( res.get( Integer.parseInt(number)-1 ) ,null); // E:\RMI_PRACTICA\DownloadsRMI



            //byte[] fileContent = Files.readAllBytes(resultFile.toPath());
            System.out.println("Introduce the path of the dowload");
            String path = reader.readLine();
            //new File(path).mkdir();
            String finalPath=path + '\\' +res.get( Integer.parseInt(number)-1 ).title;

            File fitxer = new File(finalPath);
            try (FileOutputStream fos = new FileOutputStream(finalPath)) {
                fos.write(resultFile);
                //fos.close(); There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
            }
        }else{
            System.out.println("Not fount item");
        }


    }


    /////////////////////////////////////SERVER

    private Registry startRegistry(Integer port) throws IOException, RemoteException  {
        if(port == null) {
            port=1000;

        }
        try {
            Registry registry = LocateRegistry.getRegistry(port);
            registry.list( );
            // The above call will throw an exception
            // if the registry does not already exist
            return registry;
        }
        catch (RemoteException ex) {
            // No valid registry at that port.
            System.out.println("RMI registry cannot be located ");
            Registry registry= LocateRegistry.createRegistry(port);
            System.out.println("RMI registry created at port ");
            return registry;
        }
    }

    @Override
    public void notifyStartServer() throws RemoteException{
        ArrayList<NodeClientInterface> error_clients = new ArrayList<>();
        for (NodeClientInterface c :clients) {
            try{
                c.notifyStartClient();
            }catch(RemoteException e){
                System.out.println("Client is not reachable");
                error_clients.add(c);
            }
        }
        for(NodeClientInterface c: error_clients){
            this.clients.remove(c);
        }

    }

    /////////////////////////////////////CLIENT

    @Override
    public boolean visiteNode() throws RemoteException {
        if (visited == false) {
            visited=true;
            return true;
        }
        return false;
    }

    @Override
    public void resetVisited() throws RemoteException{
        if(visited==true){
            visited =false;

            for (NodeClientInterface c: clients ) {
                c.resetVisited();
            }
        }
    }

    @Override
    public void notifyStartClient() throws RemoteException {
        System.out.println("- Client connected to own server");
    }

    @Override
    public void registerUserServer(NodeClientInterface client) throws RemoteException {
        clients.add(client);
    }

    @Override
    public List<FileNode> searchAllNodes(List<FileNode> result, String param, String[] value) throws RemoteException {
        if(visiteNode()){
            switch (param) {
                case "1":
                    for (FileNode f : ownDataList) {
                        if(f.title.equals(value[0])) result.add(f);
                    }
                    break;
                case "2":
                    for (FileNode f : ownDataList) {
                        if (f.description.equals(value[0])) result.add(f);
                    }
                    break;
                case "3":
                    int countSearch = value.length;
                    for (FileNode f : ownDataList) {
                        int i=0;
                        if(f.keywords!=null ){
                            for (String k: f.keywords) {
                                for (String s : value) {
                                    if(s.equals(k)){
                                        i++;
                                        if(i==countSearch){
                                            result.add(f);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;
            }
            for (NodeClientInterface c : clients) {
                result=c.searchAllNodes(result, param, value);
            }
        }
        return result;
    }

    @Override
    public byte[] searchAllNodesDownload(FileNode nodeFile, byte[]  fileByte) throws IOException {
        if(visiteNode()){
            for (FileNode f : ownDataList) {
                if(f.equals(nodeFile)) {
                    fileByte= Files.readAllBytes(ownDataListRaw.get(f.hash).toPath());;
                }
            }
            for (NodeClientInterface c : clients) {
                fileByte=c.searchAllNodesDownload(nodeFile,fileByte);
            }
        }
        return fileByte;
    }



    //////////////////////////////////////MAIN

    private void clientStart(Node node) throws IOException, RemoteException  {
        try {
            Registry registry = LocateRegistry.getRegistry(fatherIP,fatherPort);
            NodeClientInterface stub = (NodeClientInterface) registry.lookup("Hello");
            if(clients.isEmpty()){this.registerUserServer(stub);}

            stub.registerUserServer(node);
            System.out.println("Client registered");

        } catch (RemoteException e) {
            System.err.println("remote exception: " + e.toString()); e.printStackTrace();
        } catch (Exception e){
            System.err.println("client exception: " + e.toString()); e.printStackTrace();
        }
    }

    private void serverStart() throws RemoteException  {
        try {
            Registry registry = startRegistry(ownPort);
            registry.bind("Hello", (Node) this);
            notifyStartServer();

            while(true) {
                listenInstruction();
            }
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString()); e.printStackTrace();
        }
    }

    // E:\RMI_PRACTICA\CompartidaProj1,1000
    // E:\RMI_PRACTICA\CompartidaProj2,1001
    // 192.168.0.23,1000
    // 192.168.0.30,1000
    // ECRPListCrypt.pdf
    // E:\RMI_PRACTICA\DownloadsRMI
    // E:\RMI_PRACTICA\CompartidaProj3,1002
    // 192.168.0.23,1001
    // E:\RMI_PRACTICA\CompartidaProj4,1003
    // 192.168.0.23,1002
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, RemoteException  {
        Node node=new Node();
        System.out.println("Set:  'the initial data file', 'own Port'");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String strtxt = reader.readLine();
        String[] s = strtxt.split(",");
        node.ownData= s[0];
        node.ownPort= new Integer(s[1]);

        node.readFiles();


        System.out.println("Start as 1='server' 2='server+client'");
        BufferedReader reader2 = new BufferedReader(new InputStreamReader(System.in));

        if (reader2.readLine().equals("2")) {
            System.out.println("Set the IP and port to connect");
            BufferedReader reader3 = new BufferedReader(new InputStreamReader(System.in));
            String strtxt3 = reader3.readLine();
            String[] s3 = strtxt3.split(",");
            node.fatherIP= s3[0];
            node.fatherPort= new Integer(s3[1]);

            node.clientStart(node);

            node.serverStart();
        }else{
            System.out.println("Starting 1st Server");
            node.serverStart();
        }

    }


}
