///A Simple Web Server (WebServer.java)

package http.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.System.exit;

public class WebServer {

    protected void start(int port) {
        ServerSocket s;

        System.out.println("Webserver starting up on port " + port);
        System.out.println("(press ctrl-c to exit)");
        try {
            // create the main server socket
            s = new ServerSocket(port);
        } catch (Exception e) {
            System.out.println("Error: " + e);
            return;
        }

        System.out.println("Waiting for connection");
        while (true) {
            try {
                Socket remote = s.accept();

                System.out.println("Connection from " + remote.getInetAddress().getHostAddress());
                BufferedReader in = new BufferedReader(new InputStreamReader(remote.getInputStream()));
                PrintWriter out = new PrintWriter(remote.getOutputStream());


                String str = in.readLine();
                System.out.println(str);

                String[] request = str.split(" "); // GET /index.html HTTP/1.1
                String method = request[0];
                String resource = request[1];
                String httpVersion = request[2];
                while (str != null && !str.equals("")) { // Reading the headers. They are ignored.
                    str = in.readLine();
                }

                switch(method){
                    case "GET" :
                        handleGETRequest(remote, out, resource, httpVersion);
                        break;
                    case "DELETE" :
                        handleDELETERequest(remote, out, resource, httpVersion);
                        break;
                    case "PUT" :
                        handlePUTRequest(remote, out, resource, httpVersion);
                        break;
                    default:
                        System.out.println("default");
                }

                out.flush();
                remote.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleGETRequest(Socket remote, PrintWriter out, String resource, String httpVersion) throws IOException {
        if (resource.equals("/")) resource = "index.html";

        if (resource.charAt(0) == '/') resource = resource.substring(1);

        String filetype;
        filetype = Files.probeContentType(Path.of(resource)); // text/html or image/jpg or mp3
        // It seems like Files.probeContentType(path) does not recognize Javascript files correctly
        if (filetype == null && resource.split("\\.")[1].equals("js")) {
            filetype = "text/javascript";
        }
        try {
            File file = new File(resource);
            String filecategory = filetype.split("/")[0];
            if (filecategory.equals("image") || filecategory.equals("audio") || filecategory.equals("video")) { // If file is an image or a song
                Files.copy(file.toPath(), remote.getOutputStream()); // Send the bytes directly
            } else { // Else, it's a text file
                FileReader fileReader = new FileReader(file); // If file is not found, FileNotFoundException is thrown and caught
                out.println(httpVersion + " 200 OK");
                out.println("Content-Type: " + filetype);
                out.println("Server: Pierre&Nico's Handmade Web Server");
                out.println("");

                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) { // Reading the file line per line and sending each line to the client
                    out.println(line);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            out.println(httpVersion + " 404 NOT FOUND");
            out.println("");
            //TODO implement more
        }
    }


    private void handleDELETERequest(Socket remote, PrintWriter out, String resource, String httpVersion){
        if (resource.equals("/")) resource = "index.html";

        if (resource.charAt(0) == '/') resource = resource.substring(1);

        try {
            if(Files.deleteIfExists(Path.of(resource))){
                out.println(httpVersion + " 204 No Content");
                out.println("Server: Pierre&Nico's Handmade Web Server");
                out.println("");
            } else {
                out.println(httpVersion + " 404 Not Found");
                out.println("Server: Pierre&Nico's Handmade Web Server");
                out.println("");
                out.println("<h1>File not found</h1>");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
  
    private void handlePUTRequest(Socket remote, PrintWriter out, String resource, String httpVersion) throws IOException {
        if (resource.equals("/")) resource = "index.html";
        if (resource.charAt(0) == '/') resource = resource.substring(1);

        String filetype;
        filetype = Files.probeContentType(Path.of(resource)); // text/html or image/jpg or mp3
        // It seems like Files.probeContentType(path) does not recognize Javascript files correctly
        if (filetype == null && resource.split("\\.")[1].equals("js")) {
            filetype = "text/javascript";
        }

        if (filetype.split("/")[0].equals("text")) {
            // text
            out.println(httpVersion + " 200 OK");
            out.println("Content-Type: " + filetype);
            out.println("Server: Pierre&Nico's Handmade Web Server");
            out.println("");

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(remote.getInputStream()));

            FileWriter fileWriter = new FileWriter(resource);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            String line;
            while ((line = bufferedReader.readLine()) != null) { // Reading the file line per line and sending each line to the client
                bufferedWriter.write(line);
            }
        } else {
            // other cases : image, video, ...
        }
    }

    /**
     * Start the application.
     *
     * @param args Command line parameters are not used.
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage : WebServer <port>");
            exit(1);
        }
        WebServer ws = new WebServer();
        ws.start(Integer.parseInt(args[0]));
    }
}
