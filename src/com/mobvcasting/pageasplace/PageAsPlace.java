package com.mobvcasting.pageasplace;

import com.wowza.wms.application.*;
import com.wowza.wms.amf.*;
import com.wowza.wms.client.*;
import com.wowza.wms.module.*;
import com.wowza.wms.request.*;
import com.wowza.wms.stream.*;
import com.wowza.wms.rtp.model.*;
import com.wowza.wms.httpstreamer.model.*;
import com.wowza.wms.httpstreamer.cupertinostreaming.httpstreamer.*;
import com.wowza.wms.httpstreamer.smoothstreaming.httpstreamer.*;

public class PageAsPlace extends ModuleBase {

	int currentClientPosition = 0;
	IClient[] connectedClients = new IClient[4];
	
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
			if (connectedClients[i] != null && connectedClients[i].equals(client)) {
				iam = "stream" + i;
			} else if (connectedClients[i] != null) {
				theyare += ":stream" + i; 
			}
		}

		String resultString = iam + theyare;

		getLogger().info(resultString);

		sendResult(client, params, resultString);
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

	public void onConnect(IClient client, RequestFunction function,
			AMFDataList params) {
		getLogger().info("onConnect: " + client.getClientId());
		
		client.setStreamVideoSampleAccess(IClient.VIDEOSAMPLE_ACCESS_ALL);

		boolean done = false;
		for (int i = 0; i < connectedClients.length && !done; i++) {
			if (connectedClients[i] == null) {
				connectedClients[i] = client;
				client.acceptConnection();
				done = true;
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
			if (connectedClients != null && connectedClients[i].equals(client)) {
				connectedClients[i] = null;
			}
		}
	}

}