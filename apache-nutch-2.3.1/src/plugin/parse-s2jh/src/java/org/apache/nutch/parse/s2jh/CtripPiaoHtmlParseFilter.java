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
public class CtripPiaoHtmlParseFilter extends HotelAndScenicHtmlParseFilter  {
	public static final Logger LOG = LoggerFactory.getLogger(CtripHotelHtmlParseFilter.class);
	
	@Override
    public Parse filterInternal(String url, WebPage page, Parse parse, HTMLMetaTags metaTags, DocumentFragment doc) throws Exception {
		
		List<CrawlData> crawlDatas = Lists.newArrayList();
        crawlDatas.add(new CrawlData(url, "domain", "域名").setTextValue("ctrip.com", page));
        System.out.println("开始解析!");
        
//        <h2 class="cn_n" itemprop="nam
        
        if (url.startsWith("http://piao.ctrip.com/")){	
        	
       
        	/*酒景名称*/
        	String  nameStr = getXPathValue(doc, "//DIV[@class='media-right']/H2[@class='media-title']");
        	if(nameStr != null){
        		crawlDatas.add(new CrawlData(url, "name","酒景名称").setTextValue(nameStr,page));
        	}else{
        		crawlDatas.add(new CrawlData(url, "name","酒景名称").setTextValue("",page));
        	}  
        
        	/*酒景评级 level*/	
        	String  levelStr = getXPathValue(doc, "//DIV[@class='media-right']/SPAN[@class='media-grade']").replaceAll(" ", "");
        	if(levelStr!= null){
        		crawlDatas.add(new CrawlData(url, "level","酒景评级").setTextValue(levelStr,page));
        	}else{
        		crawlDatas.add(new CrawlData(url, "level","酒景评级").setTextValue("",page));
        	}
        	
        	/*酒景地址 adress*/
        	String adressStr=(getXPathValue(doc, "//DIV[@class='media-right']/UL/LI[1]/EM")+"##"+getXPathValue(doc, "//DIV[@class='media-right']/UL/LI[1]/SPAN")).replaceAll(" ", "");
        	if(adressStr!=null){
        		
        			crawlDatas.add(new CrawlData(url, "address","酒景地址").setTextValue(adressStr,page)); 
        		}
            else{
        		    crawlDatas.add(new CrawlData(url, "address","酒景地址").setTextValue("",page)); 
        	}
        	
        	
        /*酒景基本信息*/
        StringBuffer basicInfoBuf=new StringBuffer();
        basicInfoBuf.append(getXPathValue(doc, "//DIV[@class='media-right']/UL/LI[2]")).append("&&");
        basicInfoBuf.append(getXPathValue(doc, "//DIV[@id='content-wrapper']/DIV[@class='content-left']/DIV[@id='J-content']/DIV[@class='c-wrapper no-border-top layoutfix']/DL[@class='c-wrapper-info']/DD[1]")).append("&&");
        basicInfoBuf.append(getXPathValue(doc, "//DIV[@id='content-wrapper']/DIV[@class='content-left']/DIV[@id='J-content']/DIV[@class='c-wrapper no-border-top layoutfix']/DL[@class='c-wrapper-info']/DD[3]"));
        String basicInfoStr=basicInfoBuf.toString().replaceAll(" ", "");
    	if(basicInfoStr != null){
    		crawlDatas.add(new CrawlData(url, "basicInfo","酒景基本信息").setTextValue(basicInfoStr,page));
    	}else{
    		crawlDatas.add(new CrawlData(url, "basicInfo","酒景基本信息").setTextValue("",page));
    	}
    	
    	/*酒景简介*/
    	StringBuffer introductionsb = new StringBuffer();
 	    String introductionStr=null;
 	    NodeList  introindex_nodes = selectNodeList(doc, "//DIV[@id='J-Jdjj']/DIV[@class='feature-wrapper']/UL/LI/SPAN");
 	    NodeList  introcontext_nodes = selectNodeList(doc, "//DIV[@id='J-Jdjj']/DIV[@class='feature-wrapper']/UL/LI/P");
 	    if(introindex_nodes!=null&&introcontext_nodes!=null)
 	    {
 	       if(introindex_nodes.getLength()==introcontext_nodes.getLength())
 	       {
 	    	   for(int i=0; i<introindex_nodes.getLength(); i++){
 	 	            if(((Element)introindex_nodes.item(i) != null)&&((Element)introcontext_nodes.item(i) != null)){	 	    	      
 	 	            	introductionsb.append(((Element)introindex_nodes.item(i)).getTextContent()).append("##");
 	 	            	introductionsb.append(((Element)introcontext_nodes.item(i)).getTextContent()).append("$;");
 	 	            }	    	  
 	 	        }
 	       }
 	    } 
    	introductionStr = introductionsb.append(getXPathValue(doc, "//DIV[@id='J-Jdjj']/DIV[@class='feature-wrapper']/DIV[@class='feature-content']")).toString().replaceAll("\n", "").replaceAll(" ", "");
    	if(introductionStr != null){
    		crawlDatas.add(new CrawlData(url, "introduction","酒景简介").setTextValue(introductionStr,page));
    	}else{
    		crawlDatas.add(new CrawlData(url, "introduction","酒景简介").setTextValue("",page));
    	}  
    	
        /*周边交通 */ 	
    	String  trafficStr = getXPathValue(doc, "//DIV[@id='J-Jtzn']/DIV[@class='feature-traffic']").replaceAll(" ", "");
 	    if(trafficStr != null){
 	       crawlDatas.add(new CrawlData(url, "surTra","周边交通").setTextValue(trafficStr,page));
 	    }else{
 	    	crawlDatas.add(new CrawlData(url, "surTra","周边交通").setTextValue("",page));
 	    }  
        
 	    /*周边酒店*/
 	   StringBuffer hotelsb = new StringBuffer();
	    String hotelStr=null;
	    NodeList  hotelname_nodes = selectNodeList(doc, "//DIV[@id='J-NearHotel']/UL[@class='hotel-list layoutfix']/LI/DIV[@class='hotel-intro']/A");
	    NodeList  hoteldis_nodes = selectNodeList(doc, "//DIV[@id='J-NearHotel']/UL[@class='hotel-list layoutfix']/LI/DIV[@class='hotel-intro']/SPAN[2]");
	    if(hotelname_nodes!=null&&hoteldis_nodes!=null)
	    {
	       if(hotelname_nodes.getLength()==hoteldis_nodes.getLength())
	       {
	    	   int j=1;
	    	   for(int i=0; i<hotelname_nodes.getLength(); i++){
	 	            if(((Element)hotelname_nodes.item(i) != null)&&((Element)hoteldis_nodes.item(i) != null)){
	 	            	hotelsb.append(j); 	 	    	      
	 	            	hotelsb.append(((Element)hotelname_nodes.item(i)).getTextContent()).append("##");
	 	    	        hotelsb.append(((Element)hoteldis_nodes.item(i)).getTextContent()).append("$;");
	 	    	        j++;
	 	            }	    	  
	 	        }
	       }
	    } 
	    
	    if(hotelsb.length()>=2){
	         hotelStr=hotelsb.substring(0,hotelsb.length()-2).replaceAll("\n", "").replaceAll(" ", "");
	    }
	    if(hotelStr != null){
	       crawlDatas.add(new CrawlData(url, "surHotel","周边酒店").setTextValue(hotelStr,page));
	    }else{
	    	crawlDatas.add(new CrawlData(url, "surHotel","周边酒店").setTextValue("",page));
	    }  
	}
        
       saveCrawlData(url, crawlDatas, page);
       
     //添加数据 到 汇总表
     //mergeCrawlDataToMongo(url, crawlDatas);
        
	   return parse;
		

}

	
	
	@Override
    public String getUrlFilterRegex() {
		//http://piao.ctrip.com/dest/.*
    	return "^http://piao.ctrip.com/dest/.*";
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
    	return "crawl_data_ctrip";
    }
}

