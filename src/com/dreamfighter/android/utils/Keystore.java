package com.dreamfighter.android.utils;

import java.io.Serializable;
import javax.crypto.SecretKey;

public class Keystore implements Serializable{
	private static final long serialVersionUID = 8133130026821481242L;
	private SecretKey key;
	private Integer id;
	private String name;
	
	public Keystore(SecretKey key){
		this.key = key;
	}
	
	public SecretKey getKey() {
		//byte[] encoded = key.getEncoded();
		//String data = new BigInteger(1, encoded).toString(16);
		return key;
	}
	public void setKey(SecretKey key) {
		this.key = key;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
