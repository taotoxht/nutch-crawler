package org.apache.nutch.parse.s2jh;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.prefs.NodeChangeListener;

import org.apache.commons.lang.StringUtils;
import org.apache.nutch.parse.HTMLMetaTags;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.storage.WebPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.common.collect.Lists;
import com.sun.javafx.collections.MappingChange.Map;
import com.thoughtworks.selenium.webdriven.commands.GetAttribute;

/**
 * 
 * @author EMAIL:s2jh-dev@hotmail.com , QQ:2414521719
 *
 */
public class ElongHotelHtmlParseFilter extends AbstractHtmlParseFilter {

    public static final Logger LOG = LoggerFactory.getLogger(ElongHotelHtmlParseFilter.class);

    @Override
    public Parse filterInternal(String url, WebPage page, Parse parse, HTMLMetaTags metaTags, DocumentFragment doc) throws Exception {
  	
    	List<CrawlData> crawlDatas = Lists.newArrayList();
        crawlDatas.add(new CrawlData(url, "domain", "域名").setTextValue("elong.com", page));

        if (url.startsWith("http://hotel.elong.com/")) {
        	
        	
        	String adStr = getXPathValue(doc, 
        			"//TITLE");
        	if(adStr!=null){
        		crawlDatas.add(new CrawlData(url, "address","酒店地址").setTextValue(adStr.substring(0,adStr.length()-8),page)); 
        	}else{
        		crawlDatas.add(new CrawlData(url, "address","酒店地址").setTextValue("",page)); 
        	}
        	
        	String styleStr = getXPathValue(doc, "//DIV[@class='cont_box response_wrap3']/DIV[@class='mt10 mb10']"
        			+ "/DIV[@class='link555 t12']/A[2]");
        	if(styleStr!=null){
        		crawlDatas.add(new CrawlData(url, "style","酒景类型").setTextValue(styleStr,page));
        	}else{
        		crawlDatas.add(new CrawlData(url, "style","酒景类型").setTextValue("",page));
        	}

        	String  nameStr = getXPathValue(doc, "//DIV[@class='cont_box response_wrap3']/DIV[@class='mt10 mb10']"
        			+ "/DIV[@class='link555 t12']/H1");
        	if(nameStr != null){
        		crawlDatas.add(new CrawlData(url, "name","酒店名称").setTextValue(nameStr,page));
        	}else{
        		crawlDatas.add(new CrawlData(url, "name","酒店名称").setTextValue("",page));
        	}         	
                       
	       	    /*酒店级别 level*/	        	
	        		NodeList nodelist1 = selectNodeList(doc, "//DIV[@class='hdetail_rela_wrap']/DIV[1]/DIV[1]/DIV[1]/DIV/");
	        		NodeList no = ((Element)nodelist1.item(0)).getElementsByTagName("B");
	            	Element e1 = (Element)no.item(0);
	            	if(e1 != null){
	            		String levelStr = e1.getAttribute("title").replaceAll(" +", "").replaceAll("\n", "");
	            		crawlDatas.add(new CrawlData(url, "level","酒店级别").setTextValue(levelStr,page));
	            	}else{
	            		crawlDatas.add(new CrawlData(url, "level","酒店级别").setTextValue("",page));
	            	}

        	
	        /*酒店详情页*/
	        String[] hotelContent = new String[]{"酒店电话","开业时间","酒店设施","酒店服务","酒店简介"};
	        NodeList nodelist = selectNodeList(doc, "//DIV[@id='hotelContent']/DIV[@class='dview_info']/DL");
	        String strKey = "";
	        String strValue = "";
	       for(int i=0; i<nodelist.getLength()-1;i++){  		   
	    		   NodeList   no1 = ((Element)nodelist.item(i)).getElementsByTagName("DT");
	    		   NodeList   no2 =  ((Element)nodelist.item(i)).getElementsByTagName("DD");	
		    	   if(no1!= null && no2 != null){
		    		   if(no1.item(0) != null){
		    			strKey = no1.item(0).getTextContent().replaceAll(" +", "").replace("\n", "").replaceAll("	+", "");
		    		   }
		    		   if(no2.item(0) != null){
		    			  strValue = no2.item(0).getTextContent().replaceAll(" +", "").replaceAll("\n", "").replaceAll("	+", "");   
		    		   }
		    		  		   
		    		   if(strKey.equals(hotelContent[0])){
		    			   crawlDatas.add(new CrawlData(url, "telephone","酒店电话").setTextValue(strValue.substring(0, 12),page));
		    		   }else if(strKey.equals(hotelContent[1])){
		    			   crawlDatas.add(new CrawlData(url, "baseInfo","基本信息").setTextValue(strValue,page));
		    		   }else if(strKey.equals(hotelContent[2])){		    		    
		    				 crawlDatas.add(new CrawlData(url, "hotelfac","酒店设施").setTextValue(strValue,page));
		    			 }else if(strKey.equals(hotelContent[3])){
		    				 crawlDatas.add(new CrawlData(url, "hotelService","酒店服务").setTextValue(strValue,page));
		    			 }else if(strKey.equals(hotelContent[4])) {
		    				 crawlDatas.add(new CrawlData(url, "information","酒店信息").setTextValue(strValue,page));
		    			 }else{
		    				continue;
		    			 }  			    	   
		    	   }
    	   	    
	       }
               	       	      	
	    	/*周边酒店 surhotel*/
	       StringBuffer sb = new StringBuffer();
	       NodeList surhotelNode = selectNodeList(doc, "//DIV[@id='surroundingHotelContainer']/DIV[1]/UL/LI");
	       String titleValue = "";
	       String distance = "";
	       String price = "";	       
	       for(int i=0; i<surhotelNode.getLength(); i++){
	    		   NodeList subno1 = ((Element)surhotelNode.item(i)).getElementsByTagName("div");
	    		   Element subno1_e = (Element)subno1.item(0);//第一个div
	    		   Element subno2_e = (Element)subno1.item(1);//第二个div
	    		   if(subno1_e != null ){
	    			   NodeList subno1_e_node1 = subno1_e.getElementsByTagName("a");	    		
	    			   if(subno1_e_node1 != null){
	    				   Element subno1_e_node_e = (Element)subno1_e_node1.item(0);
	    				   if(subno1_e_node_e != null){
	    					   titleValue = subno1_e_node_e.getAttribute("title");
	    				 }
	    			   }
	    		   }
	    		   if(subno2_e != null){
    				   NodeList subno2_e_node1 = subno2_e.getElementsByTagName("p");
    				   NodeList subno2_e_node2 = subno2_e.getElementsByTagName("h3");
    				   if(subno2_e_node1.getLength() > 0){
    					   distance  = ((Element)subno2_e_node1.item(0)).getTextContent();
    				   }
    				   if(subno2_e_node2.getLength() > 0){
    					   
    					  price  = ((Element)subno2_e_node2.item(0)).getTextContent();
    					  price = price.substring(0, price.length() - titleValue.length());
    				   }
    			   }
	    		   sb.append(titleValue + ":" + price + ":" + distance + ";");
	    		   titleValue = "";
	    	       distance = "";
	    	       price = "";	
	       }
	       
	       crawlDatas.add(new CrawlData(url, "surhotel","周边酒店").setTextValue(sb.toString().replaceAll(" +", "").replaceAll("\n", ""),page));
	       
	    	/*周边景点  surscenic*/
	       	   StringBuffer surscenicsb = new StringBuffer();
	    	   NodeList surscenicNode = selectNodeList(doc, "//DIV[@data-name='jd']/DIV[2]/TABLE/TBODY/TR");
		       for(int i=0; i<surscenicNode.getLength(); i++){		    	   
		    	   Element e = (Element)surscenicNode.item(i);
		    	   if(e != null){
		    		   surscenicsb.append(e.getTextContent());
			    	   surscenicsb.append(";");  
		    	   }   
		       }
		   crawlDatas.add(new CrawlData(url, "surscenic","周边景点").setTextValue(surscenicsb.toString(),page)); 

	       
	       /*周边交通 traffic*/
	       StringBuffer trafficsb = new StringBuffer();
	       NodeList  trafficNode = selectNodeList(doc, "//DIV[@data-name='jt']/DIV[2]/DL");
	       for(int i=0; i<trafficNode.getLength(); i++){
	    	   if(((Element)trafficNode.item(i)) != null){
	    		   trafficsb.append(((Element)trafficNode.item(i)).getTextContent());
	    	   }	    	  
	       }
	       String[] trStr = trafficsb.toString().split("m");
	       trafficsb = new StringBuffer();
	       int j=0;
	       while(j < trStr.length){
	    	   trafficsb.append(trStr[j++]);
	    	   trafficsb.append("m;");
	       }
	       crawlDatas.add(new CrawlData(url, "traffic","周边交通").setTextValue(trafficsb.toString().substring(0, trafficsb.toString().length()-2),page)); 
	   }
        /**
        SELECT url ,fetch_time, 
        GROUP_CONCAT(CASE WHEN  code = 'domain'  THEN  text_value ELSE  null  END)   AS  `domain`,
        GROUP_CONCAT(CASE WHEN  code = 'name'  THEN  text_value ELSE  null  END)   AS  `name`,
        GROUP_CONCAT(CASE WHEN  code = 'brand'  THEN  text_value ELSE  null  END)   AS  `brand`,
        GROUP_CONCAT(CASE WHEN  code = 'category'  THEN  text_value ELSE  null  END)   AS  `category`,
        GROUP_CONCAT(CASE WHEN  code = 'purpose'  THEN  text_value ELSE  null  END)   AS  `purpose`,
        GROUP_CONCAT(CASE WHEN  code = 'price'  THEN  num_value ELSE  null  END)   AS  `price`,
        GROUP_CONCAT(CASE WHEN  code = 'refPrice'  THEN  num_value ELSE  null  END)   AS  `refPrice`,
        GROUP_CONCAT(CASE WHEN  code = 'primaryImage'  THEN  text_value ELSE  null  END)   AS  `primaryImage`
        FROM crawl_data GROUP BY url,fetch_time
         */
        saveCrawlData(url, crawlDatas, page);
        return parse;
    }

    @Override
    public String getUrlFilterRegex() {
    	return "^http://hotel.elong.com/.+/\\d+/?.*$";
    }

    @Override
    protected boolean isParseDataFetchLoadedInternal(String url, String html) {	
   	if(html.indexOf("ht_pri_num") > 0 && html.indexOf("hmap_tra_list")>0 ){	    
   	 /*	byte[] byteContent = html.toString().getBytes();
        	OutputStream os;
    		try {
    			os = new FileOutputStream(new File("D:/Program Files/nutch-ajax/nutch-ajax/apache-nutch-2.3/urls/ElongCLJD.html"));
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
    		} */
    		return true;
    	}else{
    		return false;
    	}
    	//return true;
    }

    @Override
    protected boolean isContentMatchedForParse(String url, String html) {
        return true;
    }
}
