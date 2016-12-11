//class for running the webserver
public class TestCase{
	/**
	 * Main Method
	 */
	public static void main(String[] args) {
	        MiniWebServer webServer = new MiniWebServer();
	        try {
			webServer.runServer(8080);
		} catch (Exception e) {
			e.printStackTrace();
		}   
	}
}

