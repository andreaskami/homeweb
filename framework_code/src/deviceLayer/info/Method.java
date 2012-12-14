package deviceLayer.info;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.restlet.data.MediaType;

public class Method implements XMLRepresentable{
	
	/** Name of the Method.*/
	private String methodName;
	
	/** Descriptions of the specific Method.*/
	private List<String>  methodDescriptions = 
		Collections.synchronizedList(new LinkedList<String>());

	/** the media types (mime) that are used for this resource. */
	private MediaType[] mediaTypes;
	
	/** parameters necessary for the Service to be properly invoked.*/
	private List<String>  paramNames = 
		Collections.synchronizedList(new LinkedList<String>());
	
	/** parameter types of the service parameters. */
	private List<String>  paramTypes 
		= Collections.synchronizedList(new LinkedList<String>());
	
	/** Descriptions of the parameters.*/
	private List<String>  paramDescriptions = 
		Collections.synchronizedList(new LinkedList<String>());
	
	/** Parameters' possible values. */
	Map<String, List<String>>  parameterOptionValues = 
		new ConcurrentHashMap<String, List<String>>();;
	
	// XML Cache
	/** flags whether cache is ok or not. */
	protected boolean xmlCacheDirty = true;
	
	/** the cache string. */
	protected String xmlCacheString = null;
	
	public Method(
			String		 methodName,
			List<String> methodDescriptions,
			List<String> paramNames,
			List<String> paramDescriptions,
			List<String> paramTypes,
			Map<String, List<String>>  parameterValues,
			MediaType[]  mediaTypes
			) {
		this.methodName = methodName;
		if(methodDescriptions != null)
			this.methodDescriptions.addAll(methodDescriptions);
		if(paramNames != null)
			this.paramNames.addAll(paramNames);
		if(paramDescriptions != null)
			this.paramDescriptions.addAll(paramDescriptions);
		if(paramTypes != null)
			this.paramTypes.addAll(paramTypes);	
		if(parameterValues != null){
			this.parameterOptionValues.putAll(parameterValues);	
		}
		if(mediaTypes != null){
			this.mediaTypes = new MediaType[mediaTypes.length];
			for (int m=0; m < mediaTypes.length; m++) {
				this.mediaTypes[m] = mediaTypes[m];
			}
		}
		setXMLCacheDirty(true);
	}
	
	/**
	 * @return the media types (mime) that are used for this resource.
	 */
	public String getMethodName() {
		return methodName;
	}
	
	/**
	 * @return the media types (mime) that are used for this resource.
	 */
	public MediaType[] getMediaType() {
		return mediaTypes;
	}
	
	/**
	 * @return the parameters that need to be passed to the resource.
	 */
	public List<String> getParameterNames(){
		return this.paramNames;
	}
	
	/**
	 * sets the parameters that are needed to be passed to the resource.
	 * @param parameters the parameters.
	 */
	public void setParameterNames(List<String> parameters){
		this.paramNames.clear(); 
		this.paramNames.addAll(parameters);
	}
	
	/**
	 * @return the parameters types that need to be passed to the resource.
	 */
	public List<String> getParameterTypes(){
		return this.paramTypes;
	}
	
	/**
	 * sets the parameters types that need to be passed to the resource.
	 * @param parameterTypes the parameters types.
	 */
	public void setParameterTypes(List<String> parameterTypes){
		this.paramTypes.clear(); 

		this.paramTypes.addAll(parameterTypes);
	}
	
	public String asXML(){
		String str = "";
		if (isXmlCacheOk()) {
			return xmlCacheString;
		}
		
		str += "<method>"; 
		str += String.format("<name>%s</name>", getMethodName());
		for (int d=0; d < methodDescriptions.size(); d++)
			str += String.format("<description>%s</description>", methodDescriptions.get(d));
		str += "<mimetypes>";
		for (MediaType mediaType : mediaTypes) {
			str += "<mimetype>" + mediaType.getName() + "</mimetype>";
		}
		str += "</mimetypes>";
		if (paramNames.size() > 0) {
			str += "<parameters>";
			for (int i=0; i< paramNames.size(); i++) {
				str += "<parameter>";
					str += String.format("<name>%s</name>", paramNames.get(i));
					str += String.format("<description>%s</description>", paramDescriptions.get(i));
					str += String.format("<type>%s</type>", paramTypes.get(i));
					if(parameterOptionValues.size() > 0){
						List<String> options = parameterOptionValues.get(paramNames.get(i));
						if(options != null){
							for(int o=0; o < options.size(); o++)
								str += String.format("<option>%s</option>", options.get(o));
						}
					}
				str += "</parameter>";
			}
			str += "</parameters>";
		} else {
			str += "<parameters/>";
		}
		str +="</method>";
		xmlCacheString = str;
		setXMLCacheDirty(false);
		return str;
	}
	
	public void setXMLCacheDirty(boolean dirty) {
		this.xmlCacheDirty = dirty;
	}
	
	public boolean isXmlCacheOk() {
		return (false == xmlCacheDirty);
	}
}


