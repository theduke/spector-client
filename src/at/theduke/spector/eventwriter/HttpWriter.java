package at.theduke.spector.eventwriter;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import at.theduke.spector.Event;

/**
 * @author theduke
 * See https://gist.github.com/jabbrwcky/1751986.
 *
 */
public class HttpWriter extends BaseWriter implements Writer {
	
	/**
	 * Unique writer identifier.
	 */
	static final String name = "http";
	
	String serverUrl;
	int port;
	boolean useSsl;
	
	HttpClient client;

	public HttpWriter(String serverUrl, int port, boolean useSsl, boolean doGzip) {
		this.serverUrl = serverUrl;
		this.port = port;
		this.useSsl = useSsl;
		this.doGzip = doGzip;
	}
	
	public void connect() {
		client = new DefaultHttpClient();
		SchemeRegistry sr = client.getConnectionManager().getSchemeRegistry();
		
		if (useSsl) {
			SSLSocketFactory sf = buildSSLSocketFactory();
			Scheme https = new Scheme("https", 443, sf);
			sr.register(https);
		}
		else {
			Scheme http = new Scheme("http", port, PlainSocketFactory.getSocketFactory());
			sr.register(http);
		}
		
		connected = true;
		
		logger.debug("Initialized httpPusher for url " + serverUrl);
	}
	
	public void close() {
		client = null;
		connected = false;
	}
	
	private SSLSocketFactory buildSSLSocketFactory() {
		TrustStrategy ts = new TrustStrategy() {
			@Override
			public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
				return true;
			}
		};
 
		SSLSocketFactory sf = null;
 
		try {
			/* build socket factory with hostname verification turned off. */
			sf = new SSLSocketFactory(ts, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Failed to initialize SSL handling.", e);
		} catch (KeyManagementException e) {
			logger.error("Failed to initialize SSL handling.", e);
		} catch (KeyStoreException e) {
			logger.error("Failed to initialize SSL handling.", e);
		} catch (UnrecoverableKeyException e) {
			logger.error("Failed to initialize SSL handling.", e);
		}
 
		return sf;
	}
	
	protected ArrayList<Event> executeFlush(ArrayList<Event> events) {
		try {

			doPostRequest(events);
		} catch (ClientProtocolException e) {
			logger.error("Could not push events to " + serverUrl, e);
			return events;
		} catch (IOException e) {
			logger.error("Could not push events to " + serverUrl, e);
			return events;
		}
		
		return null;
	}
	
	private void doPostRequest(ArrayList<Event> events) throws ClientProtocolException, IOException {
		logger.debug("HttpPusher flushing " + Integer.toString(events.size()) + " events");
		String data = eventsToString(events, doGzip);
		
		// Build post request.
		HttpPost post = new HttpPost(serverUrl);
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
		nvps.add(new BasicNameValuePair("events", data));
		nvps.add(new BasicNameValuePair("gzipped", doGzip ? "1" : "0"));
		
		post.setEntity(new UrlEncodedFormEntity(nvps));
		
		HttpResponse response2 = client.execute(post);
		try {
		    System.out.println(response2.getStatusLine());
		    HttpEntity entity2 = response2.getEntity();
		    EntityUtils.consume(entity2);
		    
		    System.out.print(entity2.getContent());
		} finally {
		    post.releaseConnection();
		}
	}
}
