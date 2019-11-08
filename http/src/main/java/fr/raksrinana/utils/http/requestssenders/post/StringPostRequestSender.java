package fr.raksrinana.utils.http.requestssenders.post;

import kong.unirest.HttpResponse;
import kong.unirest.UnirestException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

public class StringPostRequestSender extends PostRequestSender<String>{
	public StringPostRequestSender(@Nonnull String url) throws URISyntaxException, MalformedURLException{
		super(url);
	}
	
	public StringPostRequestSender(@Nonnull URL url) throws URISyntaxException{
		super(url);
	}
	
	public StringPostRequestSender(@Nonnull URL url, @Nullable Map<String, String> headers) throws URISyntaxException{
		super(url, headers);
	}
	
	public StringPostRequestSender(@Nonnull URL url, @Nullable Map<String, String> headers, @Nullable Map<String, String> params) throws URISyntaxException{
		super(url, headers, params);
	}
	
	public StringPostRequestSender(@Nonnull URL url, @Nullable Map<String, String> headers, @Nullable Map<String, String> params, @Nonnull String body) throws URISyntaxException{
		super(url, headers, params, body);
	}
	
	@Override
	@Nonnull
	public HttpResponse<String> getRequestResult() throws UnirestException{
		return this.getRequest().asString();
	}
}