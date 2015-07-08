package com.dreamfighter.android.entity;

public class ProxyConfiguration extends BaseEntity {
	private String proxyAddress;
	private Integer proxyPort;
	private Boolean useProxyAuth;
	private String proxyUserName;
	private String proxyPassword;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getProxyAddress() {
		return proxyAddress;
	}
	public void setProxyAddress(String proxyAddress) {
		this.proxyAddress = proxyAddress;
	}
	public Integer getProxyPort() {
		return proxyPort;
	}
	public void setProxyPort(Integer proxyPort) {
		this.proxyPort = proxyPort;
	}
	public Boolean getUseProxyAuth() {
		return useProxyAuth;
	}
	public void setUseProxyAuth(Boolean useProxyAuth) {
		this.useProxyAuth = useProxyAuth;
	}
	public String getProxyUserName() {
		return proxyUserName;
	}
	public void setProxyUserName(String proxyUserName) {
		this.proxyUserName = proxyUserName;
	}
	public String getProxyPassword() {
		return proxyPassword;
	}
	public void setProxyPassword(String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}
}
