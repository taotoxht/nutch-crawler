package org.apache.nutch.parse.s2jh;

import java.util.List;

import org.apache.nutch.parse.HTMLMetaTags;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.storage.WebPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;

public class CtripHotelHtmlParseFilter extends AbstractHtmlParseFilter  {
	public static final Logger LOG = LoggerFactory.getLogger(CtripHotelHtmlParseFilter.class);
	
	@Override
    public Parse filterInternal(String url, WebPage page, Parse parse, HTMLMetaTags metaTags, DocumentFragment doc) throws Exception {
		
		List<CrawlData> crawlDatas = Lists.newArrayList();
        crawlDatas.add(new CrawlData(url, "domain", "域名").setTextValue("ctrip.com", page));
        System.out.println("开始解析!");
        
        if (url.startsWith("http://hotel.ctrip.com/")){	
        	
       
        	/*酒景名称*/
        	String  nameStr = getXPathValue(doc, "//H2[@itemprop='name']");
        	if(nameStr != null){
        		crawlDatas.add(new CrawlData(url, "name","酒景名称").setTextValue(nameStr,page));
        	}else{
        		crawlDatas.add(new CrawlData(url, "name","酒景名称").setTextValue("",page));
        	}  
        	
        	/*酒景评级 level*/	    
        	String  levelStr = getXPathValue(doc, "//DIV[@class='grade']/SPAN");
        	if(levelStr!= null){
        		crawlDatas.add(new CrawlData(url, "level","酒景评级").setTextValue(levelStr,page));
        	}else{
        		crawlDatas.add(new CrawlData(url, "level","酒景评级").setTextValue("",page));
        	}
            
        	/*酒景地址 adress*/
        	String adressStr = getXPathValue(doc, "//DIV[@itemscope itemtype='http://schema.org/PostalAddress']").replaceAll(" ", "");
        	if(adressStr!=null){
        		if(adressStr.length() > 8){
        			crawlDatas.add(new CrawlData(url, "address","酒景地址").setTextValue(adressStr,page)); 
        		}
               else{
        		    crawlDatas.add(new CrawlData(url, "address","酒景地址").setTextValue("",page)); 
        	    }
        	}
        	
           /*周边交通 surTra*/
 	       StringBuffer trafficsb = new StringBuffer();
 	       NodeList  traffic_nodes = selectNodeList(doc, "//DIV[@class='traffic_box']/DIV[@class='traffic_item']");
 	       for(int i=0; i<traffic_nodes.getLength(); i++){
 	    	   if(((Element)traffic_nodes.item(i)) != null){
 	    		   trafficsb.append(((Element)traffic_nodes.item(i)).getTextContent());
 	    	   }	    	  
 	       }
 	       
 	      
 	       String[] trStr = trafficsb.toString().split("m");
 	       trafficsb = new StringBuffer();
 	       int j=0;
 	       while(j < trStr.length){
 	    	   trafficsb.append(trStr[j++]);
 	    	   trafficsb.append("m;");
 	       }
 	       crawlDatas.add(new CrawlData(url, "surTra","周边交通").setTextValue(trafficsb.toString().substring(0, trafficsb.toString().length()-2),page)); 
        	
        	
        	
        	
	}
		return parse;
		

}

	@Override
	protected String getUrlFilterRegex() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean isParseDataFetchLoadedInternal(String url, String html) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean isContentMatchedForParse(String url, String html) {
		// TODO Auto-generated method stub
		return false;
	}
}
