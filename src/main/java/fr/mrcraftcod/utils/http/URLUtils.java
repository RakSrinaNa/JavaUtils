package fr.mrcraftcod.utils.http;

import com.mashape.unirest.http.exceptions.UnirestException;
import fr.mrcraftcod.utils.FileUtils;
import fr.mrcraftcod.utils.Log;
import org.apache.commons.lang3.StringEscapeUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class URLUtils
{
	public static List<URL> convertStringToURL(List<String> strings)
	{
		LinkedList<URL> urls = new LinkedList<>();
		for(String urlString : strings)
			try
			{
				urls.add(new URL(urlString));
			}
			catch(MalformedURLException e)
			{
				e.printStackTrace();
			}
		return urls;
	}

	public static List<String> pullLinks(URL url) throws Exception
	{
		return pullLinks(URLHandler.getJsoup(url).html());
	}

	public static List<String> pullLinks(String text)
	{
		LinkedList<String> links = new LinkedList<>();
		Matcher matcher = Pattern.compile("\\(?\\b(http://|https://|www[.])[-A-Za-z0-9+&@#/\\\\%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]").matcher(text);
		while(matcher.find())
		{
			String urlString = matcher.group();
			if(urlString.startsWith("(") && urlString.endsWith(")"))
				urlString = urlString.substring(1, urlString.length() - 1);
			links.add(StringEscapeUtils.unescapeHtml4(urlString));
		}
		HashSet<String> hs = new HashSet<>();
		hs.addAll(links);
		links.clear();
		links.addAll(hs);
		return links;
	}

	public static boolean saveAsFile(URL url, File file)
	{
		FileUtils.createDirectories(file);
		try(InputStream is = URLHandler.getAsBinary(url); FileOutputStream fos = new FileOutputStream(file))
		{
			int i = 0;
			while((i = is.read()) != -1)
				fos.write(i);
		}
		catch(IOException | UnirestException | URISyntaxException e)
		{
			Log.warning("Couldn't download file " + url.toString() + " to " + file.toString(), e);
			return false;
		}
		return true;
	}
}
