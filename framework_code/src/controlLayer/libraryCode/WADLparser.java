package controlLayer.libraryCode;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class WADLparser {
	
	List<String> 		 	   resName;
	List<String> 			   resDescription;
	Map<String, List<String>>  resMethods;
	Map<String, List<String>>  resMethodDescription;
	Map<String, List<String>>  resReqParamName;
	Map<String, List<String>>  resReqParamType;
	Map<String, List<String>>  resReqParamDescription;
	Map<String, List<String>>  resReqParamValues;
	Map<String, List<String>>  resRespReturnMediaType;
	Map<String, List<String>>  resRespReturnType;
	Map<String, List<String>>  resRespReturnDesc;
	
	public WADLparser(){
		this.resName       			= Collections.synchronizedList(new LinkedList<String>());
		this.resDescription   		= Collections.synchronizedList(new LinkedList<String>());
		this.resMethods     		= new ConcurrentHashMap<String, List<String>>();
		this.resMethodDescription 	= new ConcurrentHashMap<String, List<String>>();
		this.resReqParamName     	= new ConcurrentHashMap<String, List<String>>();
		this.resReqParamType     	= new ConcurrentHashMap<String, List<String>>();
		this.resReqParamDescription = new ConcurrentHashMap<String, List<String>>();
		this.resReqParamValues		= new ConcurrentHashMap<String, List<String>>();
		this.resRespReturnMediaType	= new ConcurrentHashMap<String, List<String>>();
		this.resRespReturnType  	= new ConcurrentHashMap<String, List<String>>();
		this.resRespReturnDesc 		= new ConcurrentHashMap<String, List<String>>();
	}
	
    public boolean parseWADL(String xml) throws ParserConfigurationException, SAXException{
    	
    	try {
    		  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    		  DocumentBuilder db = dbf.newDocumentBuilder();
    		  InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
    		  Document doc = db.parse(is);
    		  doc.getDocumentElement().normalize();
    		  //System.out.println("Root element " + doc.getDocumentElement().getNodeName());
    		  NodeList resources = doc.getElementsByTagName(Constants.WADL_RESOURCE);
    		  System.out.println("Parsing WADL file for service description data...");
    	
    		  // List of Resources
    		  for (int s = 0; s < resources.getLength(); s++) {
    			  
    		    Node resource = resources.item(s);
    		    
    		    if (resource.getNodeType() == Node.ELEMENT_NODE) {
    		    	//Resource URI
    		    	NamedNodeMap nmap = resource.getAttributes();
    		    	resName.add(nmap.getNamedItem("path").getNodeValue());
    		    	String resourceName = nmap.getNamedItem("path").getNodeValue();
    		    	Element res = (Element) resource;
    		    	
    		    	//Resource Description
    		    	NodeList docu = res.getElementsByTagName(Constants.WADL_DESCRIPTION);
    		    	Node resInfo = docu.item(0);
	    			NamedNodeMap resDesc = resInfo.getAttributes();
	    			resDescription.add(resDesc.getNamedItem("title").getNodeValue());
    		    	
    		    	
    		    	NodeList methods = res.getElementsByTagName(Constants.WADL_METHOD);
    		    	// List of Methods
    	    		for (int m = 0; m < methods.getLength(); m++) {
    	    			Node method = methods.item(m);
    	    			NamedNodeMap verb = method.getAttributes();
    	    			List<String> lm = resMethods.get(resourceName);
    	    			if(lm == null){
    	    				lm = Collections.synchronizedList(new LinkedList<String>());
    	    			}
    	    			lm.add(verb.getNamedItem("name").getNodeValue());
    	    			resMethods.put(resourceName,lm);
        		    	
        		    	//Method Description
        		    	Element meth = (Element) method;
        		    	NodeList methDesc = meth.getElementsByTagName(Constants.WADL_DESCRIPTION);
        		    	Node methodDesc   = methDesc.item(0);
        		    	NamedNodeMap methInfo = methodDesc.getAttributes();
        		    	String currentMethod = verb.getNamedItem("name").getNodeValue();
        		    	String key = resourceName + currentMethod;
        		    	
    	    			List<String> lmd = resMethodDescription.get(key);
    	    			if(lmd == null){
    	    				lmd = Collections.synchronizedList(new LinkedList<String>());
    	    			}
    	    			lmd.add(methInfo.getNamedItem("title").getNodeValue());
    	    			resMethodDescription.put(key,lmd);
        		    	
        		    	
        		    	// Request Parameters
        		    	NodeList req = meth.getElementsByTagName(Constants.WADL_REQUEST);
        		    	Node request = req.item(0);
        		    	Element requ = (Element) request;
        		    	NodeList reqParams = requ.getElementsByTagName(Constants.WADL_PARAMETER);
        		    	
        		    	//List of Parameters
        		    	for (int p = 0; p < reqParams.getLength(); p++) {
        		    		Node param = reqParams.item(p);
        		    		NamedNodeMap paramInfo = param.getAttributes();
        		    		String parameterName = paramInfo.getNamedItem("name").getNodeValue();

        	    			List<String> lreqpn = resReqParamName.get(key);
        	    			if(lreqpn == null){
        	    				lreqpn = Collections.synchronizedList(new LinkedList<String>());
        	    			}
        	    			lreqpn.add(paramInfo.getNamedItem("name").getNodeValue());
        		    		resReqParamName.put(key, lreqpn);
        		    		
        	    			List<String> lreqpt = resReqParamType.get(key);
        	    			if(lreqpt == null){
        	    				lreqpt = Collections.synchronizedList(new LinkedList<String>());
        	    			}
        	    			lreqpt.add(paramInfo.getNamedItem("type").getNodeValue());
        	    			resReqParamType.put(key, lreqpt);
        		    		
            		    	Element paramDesc = (Element) param;
            		    	// Parameter Description
        		    		NodeList para = paramDesc.getElementsByTagName(Constants.WADL_DESCRIPTION);
            		    	Node parDesc   = para.item(0);
            		    	NamedNodeMap parInfo = parDesc.getAttributes();
            		    	
        	    			List<String> lreqpd = resReqParamDescription.get(key);
        	    			if(lreqpd == null){
        	    				lreqpd = Collections.synchronizedList(new LinkedList<String>());
        	    			}
        	    			lreqpd.add(parInfo.getNamedItem("title").getNodeValue());
        	    			resReqParamDescription.put(key, lreqpd);
            		    	
            		    	// Parameter Option Values
        		    		NodeList options = paramDesc.getElementsByTagName(Constants.WADL_PARAMETER_OPTION);
        		    		String pkey = resourceName + currentMethod + parameterName;
        		    		//List of Options
            		    	for (int o = 0; o < options.getLength(); o++) {
            		    		Node option   = options.item(o);
            		    		NamedNodeMap oName = option.getAttributes();
            		    		
            	    			List<String> lreqpo = resReqParamValues.get(pkey);
            	    			if(lreqpo == null){
            	    				lreqpo = Collections.synchronizedList(new LinkedList<String>());
            	    			}
            	    			lreqpo.add(oName.getNamedItem("value").getNodeValue());
            	    			resReqParamValues.put(pkey, lreqpo);
            		    	}
        		    	}
        		    	
        		    	// Response Parameters
        		    	NodeList respo = meth.getElementsByTagName(Constants.WADL_RESPONSE);
        		    	Node response = respo.item(0);
        		    	Element resp = (Element) response;
        		    	NodeList representations = resp.getElementsByTagName(Constants.WADL_REPRESENTATION);
        		    	
        		    	//List of Representations
        		    	for (int r = 0; r < representations.getLength(); r++) {
        		    		Node rep = representations.item(r);
        		    		NamedNodeMap representationInfo = rep.getAttributes();
        		    		key = resourceName + currentMethod;
        		    		
        	    			List<String> lresmt = resRespReturnMediaType.get(key);
        	    			if(lresmt == null){
        	    				lresmt = Collections.synchronizedList(new LinkedList<String>());
        	    			}
        	    			lresmt.add(representationInfo.getNamedItem("mediaType").getNodeValue());  		    		
        		    		resRespReturnMediaType.put(key, lresmt);
        		    		
        	    			List<String> lresrt = resRespReturnType.get(key);
        	    			if(lresrt == null){
        	    				lresrt = Collections.synchronizedList(new LinkedList<String>());
        	    			}
        	    			lresrt.add(representationInfo.getNamedItem("type").getNodeValue());  		    		
        	    			resRespReturnType.put(key, lresrt);
        		    		
            		    	Element repDesc = (Element) rep;
            		    	// Representation Description
        		    		NodeList repInf = repDesc.getElementsByTagName(Constants.WADL_DESCRIPTION);
            		    	Node represDesc   = repInf.item(0);
            		    	NamedNodeMap repInfo = represDesc.getAttributes();
            		    	
        	    			List<String> lresrd = resRespReturnDesc.get(key);
        	    			if(lresrd == null){
        	    				lresrd = Collections.synchronizedList(new LinkedList<String>());
        	    			}
        	    			lresrd.add(repInfo.getNamedItem("title").getNodeValue());  		    		
        	    			resRespReturnDesc.put(key, lresrd);
        		    	}	
    	    		}
    		    }
    		  }
    		  	System.out.println("Parsed WADL file successfully!");
  		    	return true;
    	} catch (SAXParseException e) {
    		System.err.println("Not a valid WADL file.");
    		return false;
		    //e.printStackTrace();
    	} catch (IOException ex) {
    		System.err.println("URL not valid.");
    		return false;
    		//ex.printStackTrace();
    	}
    }
    
	public List<String> getResourceNames(){
		return resName;
	}
	
	public List<String> getResourceDescriptions(){
		return resDescription;
	}
	
	public List<String> getResourceMethods(String resource){
		
			return resMethods.get(resource);
	}
	
	public List<String> getMethodDescriptions(String resource, String method){
		String key = resource + method;
		
		return resMethodDescription.get(key);
	}
	
	public List<String> getParameterNames(String resource, String method){
		String key = resource + method;
		
		return resReqParamName.get(key);
	}

	public List<String> getParameterTypes(String resource, String method){
		String key = resource + method;
		
		return resReqParamType.get(key);
	}
	
	public List<String> getParameterDescriptions(String resource, String method){
		String key = resource + method;
		
		return resReqParamDescription.get(key);
	}

	public List<String> getParameterValues(String resource, String method, String parameter){
		String key = resource + method + parameter;
		
		return resReqParamValues.get(key);
	}

	public List<String> getReturnMediaTypes(String resource, String method){
		String key = resource + method;
		
		return resRespReturnMediaType.get(key);
	}
	
	public List<String> getReturnTypes(String resource, String method){
		String key = resource + method;
		
		return resRespReturnType.get(key);
	}
	
	public List<String> getReturnValuesDescriptions(String resource, String method){
		String key = resource + method;
		
		return resRespReturnDesc.get(key);
	}

}