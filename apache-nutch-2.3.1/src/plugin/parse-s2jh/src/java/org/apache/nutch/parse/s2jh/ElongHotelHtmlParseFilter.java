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

        if (url.startsWith("http://hotel.elong.com/")){	
        	/*酒景名称*/
        	String  nameStr = getXPathValue(doc, "//H1[@id='lastbread']");
        	if(nameStr != null){
        		crawlDatas.add(new CrawlData(url, "name","酒景名称").setTextValue(nameStr,page));
        	}else{
        		crawlDatas.add(new CrawlData(url, "name","酒景名称").setTextValue("",page));
        	}  
        	
        	   /*酒景评级 level*/	    
		    NodeList level_nodes_node = null;
		    Element e = null;
    		NodeList level_nodes = selectNodeList(doc, "//DIV[@class='hdetail_rela_wrap']/DIV[1]/DIV[1]/DIV[1]/DIV/");
    		if((Element)level_nodes.item(0) != null){
    			level_nodes_node = ((Element)level_nodes.item(0)).getElementsByTagName("B");
    		}  		
    		if(level_nodes_node!= null ){
    			 e = (Element)level_nodes_node.item(0);
    		}            
        	if(e != null){
        		String levelStr = e.getAttribute("title").replaceAll(" +", "").replaceAll("\n", "");
        		crawlDatas.add(new CrawlData(url, "level","酒景评级").setTextValue(levelStr,page));
        	}else{
        		crawlDatas.add(new CrawlData(url, "level","酒景评级").setTextValue("",page));
        	}
        	/*酒景地址 adress*/
        	String adressStr = getXPathValue(doc, "//HEAD/TITLE");
        	if(adressStr!=null){
        		if(adressStr.length() > 8){
        			crawlDatas.add(new CrawlData(url, "address","酒景地址").setTextValue(adressStr.substring(0,adressStr.length()-8),page)); 
        		}
	
        	}else{
        		crawlDatas.add(new CrawlData(url, "address","酒景地址").setTextValue("",page)); 
        	}
        	/*酒景类型 type*/
        	crawlDatas.add(new CrawlData(url, "type","酒景类型").setTextValue("酒店",page));
        	
	        /*酒店详情页*/
	        String[] hotelContent = new String[]{"酒店电话","开业时间","酒店设施","酒店服务","酒店简介"};
	        NodeList hotelContent_nodes = selectNodeList(doc, "//DIV[@id='hotelContent']/DIV[@class='dview_info']/DL");
	        String strKey = "";
	        String strValue = "";
	       for(int i=0; i<hotelContent_nodes.getLength()-1;i++){
	    		   NodeList   hC_no1 = ((Element)hotelContent_nodes.item(i)).getElementsByTagName("DT");
	    		   NodeList   hC_no2 =  ((Element)hotelContent_nodes.item(i)).getElementsByTagName("DD");	
		    	   if(hC_no1!= null && hC_no2 != null){
		    		   if(hC_no1.item(0) != null){
		    			strKey = hC_no1.item(0).getTextContent().replaceAll(" +", "").replace("\n", "").replaceAll("	+", "");
		    		   }
		    		   if(hC_no2.item(0) != null){
		    			  strValue = hC_no2.item(0).getTextContent().replaceAll(" +", "").replaceAll("\n", "").replaceAll("	+", "");   
		    		   }
		    		  		   
		    		   if(strKey.equals(hotelContent[0])){
		    			   crawlDatas.add(new CrawlData(url, "telephone","总机电话").setTextValue(strValue.substring(0, 12),page));
		    		   }else if(strKey.equals(hotelContent[1])){
		    			   crawlDatas.add(new CrawlData(url, "basicInfo","酒景基本信息").setTextValue(strValue,page));
		    		   }else if(strKey.equals(hotelContent[2])){		    		    
		    				 crawlDatas.add(new CrawlData(url, "supFac","配套设施").setTextValue(strValue,page));
		    			 }else if(strKey.equals(hotelContent[3])){
		    				 crawlDatas.add(new CrawlData(url, "hotelService","酒店服务").setTextValue(strValue,page));
		    			 }else if(strKey.equals(hotelContent[4])) {
		    				 crawlDatas.add(new CrawlData(url, "introduction","酒景简介").setTextValue(strValue,page));
		    			 }else{
		    				continue;
		    			 }  			    	   
		    	   }
    	   	    
	       }
               	       	      	
	    	/*周边酒店 surHotel*/
	       StringBuffer sb = new StringBuffer();
	       NodeList surhotel_nodes = selectNodeList(doc, "//DIV[@id='surroundingHotelContainer']/DIV[1]/UL/LI");
	       String titleValue = "";
	       String distance = "";
	       String price = "";	       
	       for(int i=0; i<surhotel_nodes.getLength(); i++){
	    		   NodeList subno1 = ((Element)surhotel_nodes.item(i)).getElementsByTagName("div");
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
	       
	       crawlDatas.add(new CrawlData(url, "surHotel","周边酒店").setTextValue(sb.toString().replaceAll(" +", "").replaceAll("\n", ""),page));
	       
	    	/*周边景点  surScenic*/
	       	   StringBuffer surscenicsb = new StringBuffer();
	    	   NodeList surscenic_nodes = selectNodeList(doc, "//DIV[@data-name='jd']/DIV[2]/TABLE/TBODY/TR");
		       for(int i=0; i<surscenic_nodes.getLength(); i++){		    	   
		    	   Element sube = (Element)surscenic_nodes.item(i);
		    	   if(sube != null){
		    		   surscenicsb.append(sube.getTextContent());
			    	   surscenicsb.append(";");  
		    	   }   
		       }
		   crawlDatas.add(new CrawlData(url, "surScenic","周边景点").setTextValue(surscenicsb.toString(),page)); 

	       
	       /*周边交通 surTra*/
	       StringBuffer trafficsb = new StringBuffer();
	       NodeList  traffic_nodes = selectNodeList(doc, "//DIV[@data-name='jt']/DIV[2]/DL");
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
    	return true;	
//  	if(html.indexOf("ht_pri_num") > 0 &&  html.indexOf("hmap_info_wrap") > 0){	
//    		return true;
//    	}else{
//    		return false;
//    	}
    }

    @Override
    protected boolean isContentMatchedForParse(String url, String html) {
        return true;
    }
    
    @Override
    public String getTableName() {
    	return "crawl_data_elong";
    }
}
