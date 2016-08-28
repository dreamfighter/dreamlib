package com.dreamfighter.android.enums;

import java.io.Serializable;

public class GenericResponse implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = -5560351027550549940L;
	private Object data;
	private String status;
	private String code;
    
	public GenericResponse(){}
	
    public GenericResponse(Object data, String status,String code){
        this.data = data;
        this.status = status;
        this.code = code;
    }

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
