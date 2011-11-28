package com.mobvcasting.pageasplace;

import com.wowza.wms.application.*;
import com.wowza.wms.amf.*;
import com.wowza.wms.client.*;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.module.*;
import com.wowza.wms.request.*;
import com.wowza.wms.stream.*;
import com.wowza.wms.rtp.model.*;
import com.wowza.wms.httpstreamer.model.*;
import com.wowza.wms.httpstreamer.cupertinostreaming.httpstreamer.*;
import com.wowza.wms.httpstreamer.smoothstreaming.httpstreamer.*;
import java.net.*;
import java.io.*;

public class PageAsPlace extends ModuleBase {
	
	int currentClientPosition = 0;
	PAPClient[] connectedClients = new PAPClient[20];
	
	public void doSomething(IClient client, RequestFunction function, AMFDataList params) {
		getLogger().info("doSomething");
		sendResult(client, params, "Hello Wowza");
	}

	public void streamSelect(IClient client, RequestFunction function, AMFDataList params) {
		getLogger().info("streamSelect");
		
		String iam = null;
		String theyare = "";
		
		// Let's find out who we are and who they are
		for (int i = 0; i < connectedClients.length; i++) {
			if (connectedClients[i] != null && connectedClients[i].client.equals(client)) {
				iam = "stream" + i;
			} else if (connectedClients[i] != null) {
				theyare += ":stream" + i; 
			}
		}

		String resultString = iam + theyare;

		getLogger().info(resultString);

		sendResult(client, params, resultString);
	}
	
	public void getUrls(IClient client, RequestFunction function, AMFDataList params) {
		getLogger().info("getUrls");
		
		StringBuilder resultStringBuilder = new StringBuilder();
		
		resultStringBuilder.append("http://www.google.com/\n");
		resultStringBuilder.append("http://itp.nyu.edu/\n");
		
		for (int i = 0; i < connectedClients.length; i++) {
			if (connectedClients[i] != null && !connectedClients[i].client.equals(client) && connectedClients[i].currentURL != null) {
				resultStringBuilder.append(connectedClients[i] + "\n");
			}
		}
		
		sendResult(client, params, resultStringBuilder.toString());
	}
	
	// EXAMPLE: returning an object to the client
	/*
	public void c2sReturnObject(IClient client, RequestFunction function, AMFDataList params)
	{
		String res = "";
		WMSLogger log = getLogger();

		AMFDataObj retObj = new AMFDataObj();

		retObj.put("val1", new AMFDataItem("Hello Wowza 1"));
		retObj.put("val2", new AMFDataItem(3456));
		retObj.put("val3", new AMFDataItem(true));

		res = retObj.toString();
		log.info(res);

		sendResult(client, params, retObj);
	}	
	*/
	
	public void newUrl(IClient client, RequestFunction function, AMFDataList params) {
		getLogger().info("newUrl");
		getLogger().info("AMFDataList: " + params.toString());
		
		String passedUrl = getParamString(params,PARAM1);
		getLogger().info("newUrl: " + passedUrl);
		
		// Make HTTP Request here!
		/*
		StringBuilder htmlSource = new StringBuilder();
		try {
			URL theUrl = new URL(passedUrl);
			URLConnection urlConn = theUrl.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				htmlSource.append(inputLine);
			}
			in.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
		
		/*
		XmlHttpProxy xhp = new XmlHttpProxy();

		InputStream xslInputStream = null;
		String urlString = 
			"http://api.local.yahoo.com/MapsService/V1/geocode" +
				"?appid=jmaki-key&location=sunnyvale";
		String xslURLString = 
			"http://localhost:8080/jmaki/resources/xsl/yahoo-geocoder.xsl";
		
		Map paramsMap = new HashMap();
		// add parameters if any here
		
		URL xslURL = new URL(xslURLString);
		if (xslURL != null) {
		    xslInputStream  = xslURL.openStream();
		} else {
		   System.err.println(	"Error: Unable to locate XSL at URL " + xslURLString);
		}	    
		xhp.doGet(urlString, System.out, xslInputStream, paramsMap);
		 */
		
		String previousUrl = "";
		for (int i = 0; i < connectedClients.length; i++) {
			if (connectedClients[i] != null && connectedClients[i].client.equals(client)) {
				previousUrl = connectedClients[i].currentURL;
				// Set currentURL
				connectedClients[i].currentURL = passedUrl;
			}
		}
		getLogger().info("Previous URL: " + previousUrl);
		
		for (int j = 0; j < connectedClients.length; j++) {
			//if (connectedClients[j] != null && !connectedClients[j].client.equals(client) && connectedClients[j].currentURL.equals(previousUrl)) {
			if (connectedClients[j] != null && connectedClients[j].currentURL.equals(previousUrl)) {
			//if (connectedClients[j] != null) {
				getLogger().info("Have a client with the right previous URL");

				// Need to set currentURL
				connectedClients[j].currentURL = passedUrl;

				// Tell client to navigate
				// This is a dummy class to get anything back from the client
				class s2cParamsSimpleResult extends ModuleBase implements IModuleCallResult
				{
					public void onResult(IClient client, RequestFunction function, AMFDataList params)
					{
						WMSLogger log = getLogger();
						String param1 = getParamString(params, PARAM1);
						log.info(param1);
					}
				}
				
				// Call the method on the client (assuming we have HTML)
				//connectedClients[j].client.call("newUrl", new s2cParamsSimpleResult(), passedUrl, htmlSource.toString());

				// Cal the method on the client with no HTML
				connectedClients[j].client.call("newUrl", new s2cParamsSimpleResult(), passedUrl);
			}
			else if (connectedClients[j] != null) {
				getLogger().info("Wrong previous URL");
			}
		}
	}
	
	public void onAppStart(IApplicationInstance appInstance) {
		String fullname = appInstance.getApplication().getName() + "/"
				+ appInstance.getName();
		getLogger().info("onAppStart: " + fullname);
	}

	public void onAppStop(IApplicationInstance appInstance) {
		String fullname = appInstance.getApplication().getName() + "/"
				+ appInstance.getName();
		getLogger().info("onAppStop: " + fullname);
	}

	public void onConnect(IClient client, RequestFunction function, AMFDataList params) {
		getLogger().info("onConnect: " + client.getClientId());
		
		String passedUrl = getParamString(params,PARAM1);
		getLogger().info("onConnect URL: " + passedUrl);

		client.setStreamVideoSampleAccess(IClient.VIDEOSAMPLE_ACCESS_ALL);

		boolean done = false;
		for (int i = 0; i < connectedClients.length && !done; i++) {
			if (connectedClients[i] == null) {
				connectedClients[i] = new PAPClient(client);
				connectedClients[i].currentURL = passedUrl;
				client.acceptConnection();
				done = true;
				
				getLogger().info("New Client Accepted");
			}
		}
		
		if (!done) {
			client.rejectConnection();
		}
	}

	public void onConnectAccept(IClient client) {
		getLogger().info("onConnectAccept: " + client.getClientId());
	}

	public void onConnectReject(IClient client) {
		getLogger().info("onConnectReject: " + client.getClientId());
	}

	public void onDisconnect(IClient client) {
		getLogger().info("onDisconnect: " + client.getClientId());
		
		for (int i = 0; i < connectedClients.length; i++) {
			if (connectedClients[i] != null && connectedClients[i].client.equals(client)) {
				connectedClients[i] = null;
			}
		}
	}
	
	class PAPClient {
		IClient client;
		IClient following;
		String currentURL;
		String streamName;
		
		PAPClient (IClient client) {
			this.client = client;
		}		
	}

}