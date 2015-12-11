package models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.omg.CORBA_2_3.portable.OutputStream;

public class HttpConnection {

	private String responseBody;

	public void connect(String url, String method, String property, String parameter) {

		HttpURLConnection connection = null;
		try {
			URL connectUrl = new URL(url);
			connection = (HttpURLConnection)connectUrl.openConnection();
			connection.setRequestMethod(method);
			if(property != null) {
				connection.setRequestProperty("Authorization", property);
			}
			if(parameter != null) {
				connection.setDoOutput(true);
				String queryParam = new String("screen_name=" + parameter);
				PrintStream ps = new PrintStream(connection.getOutputStream());
				ps.print(queryParam);
				ps.close();
			}

			System.out.println("code : " + connection.getResponseCode() + "\nmes : " + connection.getResponseMessage());
			System.out.println("header :" + connection.getHeaderFields());

			if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				String responseBody = "";

				try(InputStreamReader isr = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
					BufferedReader reader = new BufferedReader(isr)) {

					String line;

					while((line = reader.readLine()) != null) {
						responseBody = responseBody + line;
					}
				}

//				System.out.println("responseBody : " + responseBody);
				setReponseBody(responseBody);
			}
			else {
				System.out.println("ログイン失敗");
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		finally {
			if(connection != null) {
				connection.disconnect();
			}
		}

	}

	public HashMap<String, String> getResponseBody() {
		String data[] = responseBody.split("&");
		HashMap<String, String> body = new HashMap<String, String>();

		for(String string : data) {
			String[] p = string.split("=");
			if(p.length == 2){
				body.put(p[0], p[1]);
			}
		}
		return body;
	}

	protected void setReponseBody(String body) {
		responseBody = body;
	}
}
