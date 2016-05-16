package org.apache.nutch.parse.s2jh;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.nutch.parse.HTMLMetaTags;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.storage.WebPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;
/**
 * 
 * @author EMAIL:s2jh-dev@hotmail.com , QQ:2414521719
 *
 */
public class QunarHotelHtmlParseFilter extends AbstractHtmlParseFilter {

    public static final Logger LOG = LoggerFactory.getLogger(QunarHotelHtmlParseFilter.class);

    @Override
    public Parse filterInternal(String url, WebPage page, Parse parse, HTMLMetaTags metaTags, DocumentFragment doc) throws Exception {
    	
    	List<CrawlData> crawlDatas = Lists.newArrayList();
        crawlDatas.add(new CrawlData(url, "domain", "域名").setTextValue("qunar.com", page));
        System.out.println("开始解析!");

        if (url.startsWith("http://hotel.qunar.com/")) {
        	
        	
        	/*酒景名称*/
        	String  nameStr = getXPathValue(doc, "//DIV[@id='detail_pageHeader']/H2/SPAN");
        	if(nameStr != null){
        		crawlDatas.add(new CrawlData(url, "name","酒景名称").setTextValue(nameStr,page));
        	}else{
        		crawlDatas.add(new CrawlData(url, "name","酒景名称").setTextValue("",page));
        	}  
        	
        	/*所属城市*/
        	String  cityStr = getXPathAttribute(doc, "//SCRIPT[@type='text/javascript']","var cityName");
        	if(cityStr != null){
        		crawlDatas.add(new CrawlData(url, "city","所在城市").setTextValue(cityStr,page));
        	}else{
        		crawlDatas.add(new CrawlData(url, "city","所在城市").setTextValue("",page));
        	}  
        	
        	/*酒景评级 level*/	
        	String  levelStr = getXPathValue(doc, "//DIV[@id='detail_pageHeader']/H2/EM");
        	if(levelStr!= null){
        		crawlDatas.add(new CrawlData(url, "level","酒景评级").setTextValue(levelStr,page));
        	}else{
        		crawlDatas.add(new CrawlData(url, "level","酒景评级").setTextValue("",page));
        	}
        	
        	/*酒景地址 adress*/
        	String adressStr=getXPathValue(doc, "//DIV[@id='detail_pageHeader']/P[@class='adress']/SPAN").replaceAll(" ", "");
        	if(adressStr!=null){	
        			crawlDatas.add(new CrawlData(url, "address","酒景地址").setTextValue(adressStr,page)); 
        		}
            else{
        		    crawlDatas.add(new CrawlData(url, "address","酒景地址").setTextValue("",page)); 
        	}
        	
        	/*总机电话*/
        	String  telephoneStr = getXPathValue(doc, "//DIV[@class='base-info bordertop clrfix']/DL[1]/DD/CITE[1]");
        	if(telephoneStr != null){
        		crawlDatas.add(new CrawlData(url, "telephone","总机电话").setTextValue(telephoneStr,page));
        	}else{
        		crawlDatas.add(new CrawlData(url, "telephone","总机电话").setTextValue("",page));
        	}  
        	
        	/*传真号码*/
        	String  faxStr = getXPathValue(doc, "//DIV[@class='base-info bordertop clrfix']/DL[1]/DD/CITE[2]");
        	if(faxStr != null){
        		crawlDatas.add(new CrawlData(url, "fax","传真号码").setTextValue(telephoneStr,page));
        	}else{
        		crawlDatas.add(new CrawlData(url, "fax","传真号码").setTextValue("",page));
        	}  
        	
        	/*酒景基本信息*/
        	String  basicInfoStr =null;
        	StringBuffer informationsb = new StringBuffer();
            NodeList  information_nodes = selectNodeList(doc,  "//DIV[@class='base-info bordertop clrfix']/DL[2]/DD");
            for(int i=0; i<information_nodes.getLength(); i++){
        	 	if((Element)information_nodes.item(i) != null){
        	 	        informationsb.append(((Element)information_nodes.item(i)).getTextContent()).append("##");
        	 	}	    	  
        	}        
            if(informationsb.length()>=2){
        	    basicInfoStr=informationsb.substring(0,informationsb.length()-2).replaceAll("\n", "").replaceAll(" ", "");
            }
        	if(basicInfoStr != null){
        		crawlDatas.add(new CrawlData(url, "basicInfo","酒景基本信息").setTextValue(basicInfoStr,page));
        	}else{
        		crawlDatas.add(new CrawlData(url, "basicInfo","酒景基本信息").setTextValue("",page));
            }  
        	
        	/*酒景简介*/
        	String  introductionStr = getXPathValue(doc, "//DL[@class='inform-list clrfix']/DD/DIV[@class='introduce']").replaceAll(" ", "");
        	if(introductionStr != null){
        		crawlDatas.add(new CrawlData(url, "introduction","酒景简介").setTextValue(introductionStr.trim(),page));
        	}else{
        		crawlDatas.add(new CrawlData(url, "introduction","酒景简介").setTextValue("",page));
        	}  
        	/*配套设施*/
    	    StringBuffer supportingFacilitTmp = new StringBuffer();
    	    supportingFacilitTmp.append(getXPathValue(doc,"//SPAN[@class='each-facility inter-width']")).append("&&");
    	    supportingFacilitTmp.append(getXPathValue(doc,"//DIV[@class='hotel-introduce']/DL[2]/DD/SPAN")).append("&&");
    	    NodeList  supporting_nodes = selectNodeList(doc, "//DIV[@class='hotel-introduce']/DL[5]/DD/SPAN");
    	    for(int i=0; i<supporting_nodes.getLength(); i++){
    	       if(((Element)supporting_nodes.item(i)) != null){
    	    	  supportingFacilitTmp.append(((Element)supporting_nodes.item(i)).getTextContent()).append("##");
    	        }	    	  
    	    }
    	    String supportingFacilityStr=supportingFacilitTmp.substring(0,supportingFacilitTmp.length()-2).replaceAll("\n", "").replaceAll(" ", "");
    	    if(supportingFacilityStr != null){
    		    crawlDatas.add(new CrawlData(url, "supFac","配套设施").setTextValue(supportingFacilityStr,page));
    	    }else{
    		    crawlDatas.add(new CrawlData(url, "supFac","配套设施").setTextValue("",page));
    	    }  
    	
    		/*客房设施*/
    		StringBuffer roomFacilityTmp = new StringBuffer();
    	    NodeList  roomFacility_nodes = selectNodeList(doc, "//DIV[@class='hotel-introduce']/DL[3]/DD/SPAN");
    		for(int i=0; i<roomFacility_nodes.getLength(); i++){
    		    if(((Element)roomFacility_nodes.item(i)) != null){
    		       roomFacilityTmp.append(((Element)roomFacility_nodes.item(i)).getTextContent()).append("##");
    		    }	    	  
    		}
    		String roomFacilityStr=roomFacilityTmp.substring(0,roomFacilityTmp.length()-2).replaceAll("\n", "").replaceAll(" ", "");
    		if(roomFacilityStr != null){
    			crawlDatas.add(new CrawlData(url, "roomFac","客房设施").setTextValue(roomFacilityStr,page));
    		}else{
    			crawlDatas.add(new CrawlData(url, "roomFac","客房设施").setTextValue("",page));
    		}  
    		
    		/*酒店服务*/
    		StringBuffer hotelServiceTmp = new StringBuffer();
    		NodeList  hotel_nodes = selectNodeList(doc, "//DIV[@class='hotel-introduce']/DL[4]/DD/SPAN");
    	    for(int i=0; i<hotel_nodes.getLength(); i++){
    		   if(((Element)hotel_nodes.item(i)) != null){
    		    	 hotelServiceTmp.append(((Element)hotel_nodes.item(i)).getTextContent()).append("##");
    		   }	    	  
    		}
    		String hotelServiceStr=hotelServiceTmp.substring(0,hotelServiceTmp.length()-2).replaceAll("\n", "").replaceAll(" ", "");
    		if(hotelServiceStr != null){
    			crawlDatas.add(new CrawlData(url, "hotelSer","酒店服务").setTextValue(hotelServiceStr,page));
    		}else{
    			crawlDatas.add(new CrawlData(url, "hotelSer","酒店服务").setTextValue("",page));
    		} 
    		
    		
        	
        	
    	//周边酒店
    		
    		
    	//周边交通
    	//周边景点      
        
        }
        saveCrawlData(url, crawlDatas, page);
        return parse;
    }

    @Override
    public String getUrlFilterRegex() {
        //http://item.jumei.com/sh150311p619680.html
        //http://www.jumeiglobal.com/deal/ht150122p933681t1.html
        /*return "http://item.jumei.com/.*.html|http://www.jumeiglobal.com/deal/.*.html";*/
    	return "http://hotel.qunar.com/city/guangzhou/.*|http://hotel.qunar.com/.*";
    }

    @Override
    protected boolean isParseDataFetchLoadedInternal(String url, String html) {
    	//System.out.println(html.toString());
    	byte[] byteContent = html.toString().getBytes();
    	OutputStream os;
		try {
			os = new FileOutputStream(new File("D:/Program Files/nutch-ajax/nutch-ajax/apache-nutch-2.3/urls/QunarCLJD.html"));
			try {
				os.write(byteContent);
				os.flush();  
		        os.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    	if(html.indexOf("hotel-quote-list") > 0){	       	
    		return true;
    	}else{
    		return false;
    	}
//    	return true;
    }

    @Override
    protected boolean isContentMatchedForParse(String url, String html) {
        return true;
    }
}
