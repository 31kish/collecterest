package models;

import static org.hamcrest.CoreMatchers.nullValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;

import javax.swing.text.AbstractDocument.Content;

import org.hibernate.validator.util.privilegedactions.GetConstructor;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;
import org.opengraph.OpenGraph;

import com.mchange.v2.cfg.PropertiesConfigSource.Parse;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

public class HtmlParser {

	public static Article parse(Article article) throws IOException, Exception {

		OpenGraph openGraph = new OpenGraph(article.url,true);
		article.title = openGraph.getContent("title");
		article.description = openGraph.getContent("description");
		article.imageUrl = openGraph.getContent("image");

		if (article.title==null || article.description==null || article.imageUrl==null) {
//			System.out.println("ogp非対応");
			nonOpenGraph(article);
			return article;
		}
		else {
			return article;
		}
	}

	public static void nonOpenGraph(Article article) throws IOException{

		URL url = new URL(article.url);
		InputStream inStream = url.openStream();
		String title = new String();
		String image = new String();
		String desc = new String();

		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inStream));
			HtmlCleaner cleaner = new HtmlCleaner();
			CleanerProperties props = cleaner.getProperties();
			try {
				TagNode node = cleaner.clean(bufferedReader);
				PrettyXmlSerializer serializer = new PrettyXmlSerializer(props);
				StringWriter writer = new StringWriter();
				serializer.write(node, writer, "utf-8");

				String split[] = writer.toString().split("\n");
				for (String string : split) {
					if (string.matches(".*"+"<title>"+".*") && title.isEmpty()) {
						title = string;
						title = title.replace("<title>", "");
						title = title.replace("</title>", "");
//						System.out.println("見つけた"+title);
					}
					else if (string.matches(".*"+"description"+".*") && desc.isEmpty()) {
						desc = string;
						desc = desc.replaceAll("\"", "");
						desc = desc.replace("<meta ", "");
						desc = desc.replace("name=description", "");
						desc = desc.replace("content=", "");
						desc = desc.replace("/>", "");
//						System.out.println("見つけた:"+desc);
					}
					else if (string.matches("<img src") && image.isEmpty()) {
//						System.out.println(string);
					}
				}
				writer.close();
			} catch (IOException e) {

			}

		} finally {
			inStream.close();
		}

		article.title = title;
		article.description = desc;
		article.imageUrl = image;
	}

}
