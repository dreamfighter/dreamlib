package com.dreamfighter.android.manager;

import java.util.List;

import android.content.Context;

import com.dreamfighter.android.entity.ProxyConfiguration;

public class ProxyConfigurationManager extends Sqlite2Manager{

	public ProxyConfigurationManager(Context context) {
		super(context, "proxy_configuration", 1, ProxyConfiguration.class);
	}
	
	public ProxyConfiguration getProxyConfigurationByAddressAndPort(String proxyAddress, int proxyPort){
		List<ProxyConfiguration> list = queryHelper(allColums(), "proxyAddress='"+proxyAddress+"' and proxyPort='"+proxyPort+"'", null);
		if(!list.isEmpty() && list.size()==1){
			return list.get(0); 
		}
		return null;
	}

}
