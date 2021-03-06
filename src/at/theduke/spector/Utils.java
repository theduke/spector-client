package at.theduke.spector;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.zip.GZIPOutputStream;

import javax.xml.bind.DatatypeConverter;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

public class Utils {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}
	
	public static String getCommandOutput(String cmd) throws InterruptedException, IOException {
		Runtime run = Runtime.getRuntime() ;
		
		Process pr;
		pr = run.exec(cmd) ;
		pr.waitFor();
		
		BufferedReader buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) ) ;
		String line;
		
		StringBuilder builder = new StringBuilder();
		
		while ((line = buf.readLine()) != null) {
			builder.append(line);
			builder.append("\n");
		}
		
		return builder.toString();
	}
	
	/**
	 * Retrieve the current time as an ISO8601 string.
	 */
	public static String getIso8601Time(Date date) {	    
	    GregorianCalendar calendar = new GregorianCalendar();
	    calendar.setTime(date);
	    String nowAsISO = DatatypeConverter.printDateTime(calendar);
	    
	    return nowAsISO;
	}
	
	public static String doGzip(String content) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzipper = new GZIPOutputStream(out);
        gzipper.write(content.getBytes());
        gzipper.close();
        String outStr = out.toString("UTF-8");
        
        return outStr;
	}
	
	public static Client getElasticSearchConnection(String clusterName, String host, int port) {
		// Set log level to WARN for ES.
		final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("org.elasticsearch");		
		if ((logger instanceof ch.qos.logback.classic.Logger)) {
			ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) logger;
			logbackLogger.setLevel(ch.qos.logback.classic.Level.WARN);
		}
		
		Settings settings = ImmutableSettings.settingsBuilder()
		        .put("cluster.name", clusterName).build();
		
		Client esClient = new TransportClient(settings)
        	.addTransportAddress(new InetSocketTransportAddress(host, port));
		
		return esClient;
	}
}
