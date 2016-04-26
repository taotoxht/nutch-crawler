package org.apache.nutch.parse.s2jh;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;

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
        	
        	try {
        		crawlDatas.add(new CrawlData(url, "address","酒店地址").setTextValue(getXPathValue(doc, 
            			"//TITLE").substring(0,getXPathValue(doc, "//TITLE").length()-8),page));           	
			} catch (Exception e) {
				// TODO: handle exception
				crawlDatas.add(new CrawlData(url, "address","酒店地址").setTextValue("",page));  
			}
        
        	try {
        		crawlDatas.add(new CrawlData(url, "style","酒景类型").setTextValue(getXPathValue(doc, "//DIV[@class='cont_box response_wrap3']/DIV[@class='mt10 mb10']"
            			+ "/DIV[@class='link555 t12']/A[2]"),page));
			} catch (Exception e) {
				// TODO: handle exception
				crawlDatas.add(new CrawlData(url, "style","酒景类型").setTextValue("",page));
			}
        	
        try {
        	crawlDatas.add(new CrawlData(url, "name","酒店名称").setTextValue(getXPathValue(doc, "//DIV[@class='cont_box response_wrap3']/DIV[@class='mt10 mb10']"
        			+ "/DIV[@class='link555 t12']/H1"),page));
		} catch (Exception e) {
			// TODO: handle exception
			crawlDatas.add(new CrawlData(url, "name","酒店名称").setTextValue("",page));
		}
        
        	
            
        	/*配套设施 supFac*/
	    	  NodeList sFnodeList =  selectNodeList(doc,"//DIV[@id='hotelContent']/UL/LI" );
	           StringBuffer supFacsb = new StringBuffer();
	           if(sFnodeList.getLength() != 0){
	        	   for(int i=0; i<sFnodeList.getLength(); i++){
	        		   String str1 = null;
	        		   String str2 = null;
	            	   Element e2 = (Element)sFnodeList.item(i);
	            	   str1 = e2.getTextContent().replaceAll(" +", "").replace("\n", "");
	            	   str2 = (e2.getAttribute("class")==""?"Yes":"No");
	            	   supFacsb.append(str1+":"+str2+";");
	               }  
	           }
	           crawlDatas.add(new CrawlData(url, "supFac","配套设施").setTextValue(supFacsb.toString().substring(0,supFacsb.toString().length()-1),page));
	           System.out.println("supFac:" + supFacsb.toString().substring(0,supFacsb.toString().length()-1));
	           
	       	    /*酒店级别 level*/	
	        	try{
	        		NodeList nodelist1 = selectNodeList(doc, "//DIV[@class='hdetail_rela_wrap']/DIV[1]/DIV[1]/DIV[1]/DIV/");
	        		NodeList no = ((Element)nodelist1.item(0)).getElementsByTagName("B");
	            	Element e1 = (Element)no.item(0);
	            	crawlDatas.add(new CrawlData(url, "level","酒店级别").setTextValue(e1.getAttribute("title"),page));
	                //System.out.println("level:" + e1.getAttribute("title"));
	        	}catch(NullPointerException e){
	        		//e.printStackTrace();
	        		crawlDatas.add(new CrawlData(url, "level","酒店级别").setTextValue("",page));
	        	}
        	
	        /*酒店详情页*/
	        String[] hotelContent = new String[]{"酒店电话","开业时间","酒店设施","酒店服务","酒店简介"};
	        NodeList nodelist = selectNodeList(doc, "//DIV[@id='hotelContent']/DIV[@class='dview_info']/DL");
	       for(int i=0; i<nodelist.getLength()-1;i++){
	    	   try{	    		   
	    		   NodeList   no1 = ((Element)nodelist.item(i)).getElementsByTagName("DT");
	    		   NodeList   no2 =  ((Element)nodelist.item(i)).getElementsByTagName("DD");	
		    	   if(no1!= null && no2 != null){
		    		   String strKey = no1.item(0).getTextContent().replaceAll(" +", "").replace("\n", "").replaceAll("	+", "");
		    		   String strValue = no2.item(0).getTextContent().replaceAll(" +", "").replaceAll("\n", "").replaceAll("	+", "");
		    		   System.out.println(no1.item(0).getTextContent().replaceAll(" +", "").replaceAll("\n", "") + no2.item(0).getTextContent().replaceAll(" +", "").replaceAll("\n", ""));
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
	    	   }catch(Exception e){
	    		  // e.printStackTrace();
	    		   continue;
	    	   }	    	   	    
	       }
               	       	      	
	    	/*周边酒店 surhotel*/
	       StringBuffer sb = new StringBuffer();
	       NodeList surhotelNode = selectNodeList(doc, "//DIV[@id='surroundingHotelContainer']/DIV[1]/UL/LI");
	       for(int i=0; i<surhotelNode.getLength(); i++){
	    	   try {
	    		   NodeList subno1 = ((Element)surhotelNode.item(i)).getElementsByTagName("div");
	    		   try {
	    			   sb.append(((Element)(((Element)subno1.item(0)).getElementsByTagName("a").item(0))).getAttribute("title"));
				} catch (Exception e) {
					// TODO: handle exception
					continue;
				}
	    		   sb.append(":");
	    		   try {
					sb.append(((Element)(((Element)subno1.item(1)).getElementsByTagName("p").item(0))).getTextContent());
				} catch (Exception e) {
					// TODO: handle exception
					continue;
				}
	    		sb.append(";");   
			} catch (Exception e) {
				// TODO: handle exception
				continue;
			}	    	 
	       }
	       crawlDatas.add(new CrawlData(url, "surhotel","周边酒店").setTextValue(sb.toString().substring(0, sb.toString().length()-1),page));
	    	/*周边交通*/
	    	/*周边景点*/
	        
        
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
        //http://item.jumei.com/sh150311p619680.html
        //http://www.jumeiglobal.com/deal/ht150122p933681t1.html
        /*return "http://item.jumei.com/.*.html|http://www.jumeiglobal.com/deal/.*.html";*/
    	return "http://hotel.elong.com/guangzhou/.*|http://hotel.elong.com/.*";
    }

    @Override
    protected boolean isParseDataFetchLoadedInternal(String url, String html) {	
    	if(html.indexOf("ht_pri_num") > 0 && html.indexOf("hmap_tra_list")>0 ){	    
    		byte[] byteContent = html.toString().getBytes();
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
    		} 
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
