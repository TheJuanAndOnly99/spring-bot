package org.finos.symphony.toolkit.workflow.response;

import java.util.Map;

import org.finos.symphony.toolkit.workflow.content.Addressable;

/**
 * A Response that contains some JSON data to be included in the message.
 * 
 * @author rob@kite9.com
 *
 */
public class DataResponse implements Response {

	private final Map<String, Object> data;
	private final String templateName;
	private final Addressable to;

	public DataResponse(Addressable to, Map<String, Object> data, String templateName) {
		super();
		this.to = to;
		this.data = data;
		this.templateName = templateName;
	}

	public Map<String, Object> getData() {
		return data;
	}
	

	@Override
	public String toString() {
		return "DataResponse [data=" + data + ", template=" + templateName + "]";
	}

	@Override
	public String getTemplateName() {
		return templateName;
	}

	@Override
	public Addressable getAddress() {
		return to;
	}

}