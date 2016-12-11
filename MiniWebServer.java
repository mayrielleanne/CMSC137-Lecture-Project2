//package miniwebserver;
//import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
/**
 * @author Bermas
 */
public class MiniWebServer {

    /**
     * @param args the command line arguments
     */
	 private ServerSocket s;
	 private String HTTPversion;
	 private String contentBeingFetched;
	 
	 private final int MAXSIZE = 1024;

	 private static LinkedList<String> requestAttributes; //contains request attributes
	 private static HashMap<String, String> requestAttributeValues; //mapping request attributes
	 
	 private static LinkedList<String> urlParameters; //contains url parameters
	 private static HashMap<String, String> urlParameterValues; //contains mapping of url parameters
	 
	 private static String request;
	 private String statCode;
	 
	 public MiniWebServer(){
			
	 }
	 	 
	 /**
	  * Creates and returns server socket.
	  * @param port Server port.
	  * @return created server socket
	  * @throws Exception Exception thrown, if socket cannot be created.
	  */
	protected ServerSocket getServerSocket(int port) throws Exception {
    	return new ServerSocket(port);
    }
 
    /**
     * Starts web server and handles web browser requests.
     * @param port Server port(ex. 80, 8080)
     * @throws Exception Exception thrown, if server fails to start.
     */
    public void runServer(int port) throws Exception {
        s = getServerSocket(port); //parang nainitilize lang si server socket s
       while (true) {
            try {
            	//initialize containers
            	requestAttributes = new LinkedList<String>();
            	requestAttributeValues = new HashMap<String, String>();
            	urlParameters = new LinkedList<String>();
            	urlParameterValues = new HashMap<String, String>();
            	
                Socket serverSocket = s.accept(); //listens for connection to be made
                handleRequest(serverSocket);
            } catch(IOException e) {
            	 System.out.println("Failed to start server: " + e.getMessage());
                System.exit(0);
                return;
            }
        }
    }
 
    /**
     * Handles web browser requests and returns a static web page to browser.
     * @param s socket connection between server and web browser.
     */
    public void handleRequest(Socket s) {
        //is variable reads request header from the client
        BufferedReader is;     // inputStream from web browser
        PrintWriter os;        // outputStream to web browser
        String request;        // Request from web browser
 
        try {
	            String webServerAddress = s.getInetAddress().toString(); //returns local address and store as string
	            System.out.println("Accepted connection from " + webServerAddress);
	            is = new BufferedReader(new InputStreamReader(s.getInputStream()));
	 
	            request = is.readLine();
	            //String[] token = request.split(":");
	            System.out.println("Server received request from client: " + request);          	            
	            String line;
	            do{
	            	line = is.readLine();
	            	if(request != null && !request.isEmpty()){
	            		
	            		String attribs[] = line.split(":",2);
	            		if(attribs.length > 1){
	            			requestAttributes.add(attribs[0]);
	            			requestAttributeValues.put(attribs[0], attribs[1]);
	            		}
	            		
	            	}else{
	            		break;
	            	}
	            	
	            }while(line.length() != 0);
	            
	        if(request != null && !request.isEmpty()){ //check if request is not not to proceed
	        	String [] requestInfo = parseRequest(request); //requestInfo contains the Operation Type, Page to Fetch, and the HTTP
	        	os = new PrintWriter(s.getOutputStream(), true);
	            //index 0 - request type; index 1 - file to open; index 2 - http protocol
	        	
	        	//line for the GET route
	        	contentBeingFetched = loadFile(requestInfo[1]); //get page content
	        	this.HTTPversion = requestInfo[2]; //get HTTP version
	        	
	        	if(requestInfo[0].equals("POST")){ //HTTP method
	        		try{
	        			char[] params = new char[MAXSIZE]; //initialize character array
	        			is.read(params);	//reads from input stream and stores in character array, params
	        			line = new String(params);
	        			//further parsing for toReturn[1]
	        			String[] req2 = line.split("&");
	        			for(int i=0; i<req2.length; i++){
	        				String[] req3 = req2[i].split("=");
	        				urlParameters.add(req3[0]);				//add to linked list
	        				urlParameterValues.put(req3[0], req3[1]); //add to hash map
	        			}
	        			
	        		}catch(Exception e){
	        			e.printStackTrace();
	        		}
	        	}
	        	
	        	String table = generateTable();
	    		contentBeingFetched = contentBeingFetched.replaceAll("<body>", "<body>\r\n"+table);     //merges the content with the table generated
	    		
	        	String header = "";
	        	
	        	//generate the response header based on the content read
	        	switch(contentBeingFetched){
		        	case "":
		        		contentBeingFetched = loadFile("404.html");
		        		contentBeingFetched = contentBeingFetched.replaceAll("<body>", "<body>\r\n"+table);     //merges the content with the table generated
		        		header = returnResponseHeader("404");
		        		break;
		        	default:
		        		contentBeingFetched = loadFile("sample.html");
		        		header = returnResponseHeader("200");
		        		break;
	        	
	        	}
	        	
	        	//Header + table + page content
	        	String totalGeneratedPage = header + "\r\n\r\n" + contentBeingFetched;
	        	
	        	os = new PrintWriter(s.getOutputStream(),true);
	        	os.println(totalGeneratedPage);
	        	System.out.println(totalGeneratedPage);
	            os.flush();
	            os.close();
	            
	        }    
        
            s.close();
        } catch (IOException e) {
            System.out.println("Failed to send response to client: " + e.getMessage());
        } finally {
        	if(s != null) {
        		try {
					s.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }
        return;
    }
   
    //generates table that appears before the actual page to be loaded
    public static String generateTable(){
    	String toReturn = "";
    	toReturn += "<table>\r\n";
    	toReturn += "<tr>";
    		toReturn += "<td>";
    			toReturn += "<h3>Request Value</h3>";
    		toReturn += "</td>";
    	toReturn += "</tr>";

    	toReturn += "<tr>";
			toReturn += "<td>";
				toReturn += "<h4>"+ request +"</h4>";
			toReturn += "</td>";
		toReturn += "</tr>";
		
		//table headers
		toReturn += "<tr>";
			toReturn += "<td>";
				toReturn += "<h4>Attribute</h4>";
			toReturn += "</td>";

			toReturn += "<td>";
				toReturn += "<h4>Value</h4>";
			toReturn += "</td>";
		toReturn += "</tr>";		
		
		
		//add request data to table 
		for(int i=0; i<requestAttributes.size(); i++){
			String property = requestAttributes.get(i);
	    	toReturn += "<tr>";
				toReturn += "<td>";
					toReturn += property; //add request property
				toReturn += "</td>";
				
				toReturn += "<td>";
					toReturn += requestAttributeValues.get(property); //add value of request property
				toReturn += "</td>";
			toReturn += "</tr>";
		}

		
		if(urlParameters.size() > 0){
	    	toReturn += "<tr>";
	    		toReturn += "<td>";
	    			toReturn += "<h3>Parameters</h3>";
	    		toReturn += "</td>";
    		toReturn += "</tr>";
			
    		//table header
	    	toReturn += "<tr>";
	    		toReturn += "<td>";
	    			toReturn += "<h4>Name</h4>";
	    		toReturn += "</td>";

	    		toReturn += "<td>";
	    			toReturn += "<h4>Value</h4>";
	    		toReturn += "</td>";
    		toReturn += "</tr>";    		
			
    		//add data to table
    		for(int i=0; i<urlParameters.size(); i++){
    			String value = urlParameters.get(i);
    	    	toReturn += "<tr>";
		    		toReturn += "<td>";
		    			toReturn += value;
		    		toReturn += "</td>";
	
		    		toReturn += "<td>";
		    			toReturn += urlParameterValues.get(value);
		    		toReturn += "</td>";
	    		toReturn += "</tr>";    		
    			
    		}
		}
		
		toReturn += "</table>\r\n";
		
		return toReturn;
    }
 
    //returns the read string from the text file specified, (String parameter : file extension to be read)
    public String loadFile(String fileName){
    	if(fileName.equals("/")){ //finding the index
    		String toReturn = "";
    		FileReader reader;
    		try{
    			reader = new FileReader("404.html"); //or you can use sample.html
    			char[] chars = new char[(int) toReturn.length()];
    			
    			reader.read(chars);
    			reader.close();
    			toReturn += new String(chars); //append the content
    		}catch(Exception e){
    			return "";
    		}
    		
    		return toReturn;
    	}else{
    		fileName = fileName.replaceAll("/", "");
    	}
    	
    	String content = "";
    	File file = new File(fileName);
    	try{
    		FileReader reader = new FileReader(file);
    		char[] chars = new char[(int) file.length()];
    		reader.read(chars);
    		reader.close();
    		content += new String(chars); //append the content
    		return content;
    	}catch(Exception e){
    		//return this 404 error not found code to the browser
    		return "";
    	}
    }
    
    //returns a String array with ff assignments
    //index 0 - request type; index 1 - file to open; index 2 - http protocol
    public static String[] parseRequest(String request){
    	int parseSize = 3; 
    	StringTokenizer strtok = new StringTokenizer(request, " ");
    	String[] parsedRequest = new String[parseSize];
    	
    	for(int i=0; i<parseSize; i++){
    		parsedRequest[i] = strtok.nextToken();
    	}
    	
    	//further parsing for toReturn[1]
    	String[] req = parsedRequest[1].split("[?]");
    	parsedRequest[1] = req[0];
    	if(req.length > 1){ //parameters were appended
    		String[] req2 = req[1].split("&");
    		for(int i=0; i<req2.length; i++){
    			String[] req3 = req2[i].split("=");
    			urlParameters.add(req3[0]);				//add to linked list
    			urlParameterValues.put(req3[0],req3[1]); //add to HashMap
    		}
    	}    	
    	return parsedRequest;    	
    }
    
    //returns responseHeader
    //Http version, status code and message
    public String returnResponseHeader(String statusCode){
    	String responseHeader = "";
    	switch(statusCode){
    		case "404":
    			responseHeader = this.HTTPversion + " 404 Not Found\r\n";
    			break;
    		case "200":
    			responseHeader = this.HTTPversion + " 200 OK\r\n";
    			responseHeader += "Content: " + getTypeOfContent() + "\r\n";
    			break;
    	}
    	responseHeader += "Content-Length: " +getContentLength() + "\r\n";
    	responseHeader += "Date: " + getDay() + ", " + getDate() + "\r\n";
    	responseHeader += "Server: Http\r\n";
    	
    	responseHeader += "Connection: close\r\n";
    	return responseHeader;
    }
    
 	public int getContentLength(){
   		byte[] converted = contentBeingFetched.getBytes();
   		return contentBeingFetched.length();
   	}
 	
   	//returns type content of the file being requested
   	public String getTypeOfContent(){
   		String[] req = parseRequest(request);
   		String file = req[1].replaceAll("/","");
   		if(file.endsWith("html"))
   			return "text/html";
   		if(file.endsWith("css"))
   			return "text/css";
   		if(file.endsWith("js"))
   			return "application/javascript"; //text/javascript is obsolete
   		return null; // if file type cannot be found, just return null first.
   	}    
    
 	public String getDay(){
   		Calendar calendar = Calendar.getInstance();
   		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
   		switch(dayOfWeek){
   			case Calendar.SUNDAY: return "Sun";
   			case Calendar.MONDAY: return "Mon";
   			case Calendar.TUESDAY: return "Tues";
   			case Calendar.WEDNESDAY: return "Wed";
   			case Calendar.THURSDAY: return "Thurs";
   			case Calendar.FRIDAY: return "Fri";
   			case Calendar.SATURDAY: return "Sat";
   		}
   		
   		return null; //fail-safe mechanism
   	}
   	
   	public String getDate(){
   		Date date = new Date();
   		SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM YYYY HH:mm:ss z");
  	   //get current date time with Date()
   	   dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
  	   String stringedDate = dateFormat.format(date);
  	   return stringedDate;
   	}
   	
}	
