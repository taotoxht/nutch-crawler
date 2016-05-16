package org.apache.nutch.parse.s2jh;

import java.util.ArrayList;
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

import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;

public class CtripHotelHtmlParseFilter extends HotelAndScenicHtmlParseFilter  {
	public static final Logger LOG = LoggerFactory.getLogger(CtripHotelHtmlParseFilter.class);
	
	@Override
    public Parse filterInternal(String url, WebPage page, Parse parse, HTMLMetaTags metaTags, DocumentFragment doc) throws Exception {
		
		List<CrawlData> crawlDatas = Lists.newArrayList();
        crawlDatas.add(new CrawlData(url, "domain", "域名").setTextValue("ctrip.com", page));
        System.out.println("开始解析!");
        
//        <h2 class="cn_n" itemprop="nam
        
    	
    	
        
    	/*酒景名称*/
    	String  nameStr = getXPathValue(doc, "//H2[@itemprop='name']");
    	if(nameStr != null){
    		crawlDatas.add(new CrawlData(url, "name","酒景名称").setTextValue(nameStr,page));
    	}else{
    		crawlDatas.add(new CrawlData(url, "name","酒景名称").setTextValue("",page));
    	}  
    	
    	/*所属城市*/
    	String  cityStr = getXPathAttribute(doc, "//DIV[@id='searchForm']/INPUT[1]","value");
    	if(cityStr != null){
    		crawlDatas.add(new CrawlData(url, "city","所在城市").setTextValue(cityStr,page));
    	}else{
    		crawlDatas.add(new CrawlData(url, "city","所在城市").setTextValue("",page));
    	}  
    	/*酒景评级 level*/	
        
    	String  levelStr = getXPathAttribute(doc, "//DIV[@class='grade']/SPAN","title");
    	if(levelStr!= null){
    		crawlDatas.add(new CrawlData(url, "level","酒景评级").setTextValue(levelStr,page));
    	}else{
    		crawlDatas.add(new CrawlData(url, "level","酒景评级").setTextValue("",page));
    	}
    	
    	/*酒景地址 adress*/
    	String adressStr=getXPathValue(doc, "//DIV[@id='J_htl_info']/DIV[@class='adress']").replaceAll(" ", "");
    	if(adressStr!=null){
    		
    			crawlDatas.add(new CrawlData(url, "address","酒景地址").setTextValue(adressStr,page)); 
    		}
        else{
    		    crawlDatas.add(new CrawlData(url, "address","酒景地址").setTextValue("",page)); 
    	}
    	
    	
    /*酒景基本信息*/
	
	/*if(basicInfoStr != null){
		crawlDatas.add(new CrawlData(url, "basicInfo","酒景基本信息").setTextValue(basicInfoStr,page));
	}else{
		crawlDatas.add(new CrawlData(url, "basicInfo","酒景基本信息").setTextValue("",page));
	} */ 
	
	/*酒景主题标签*/
    StringBuffer hashtagTmp = new StringBuffer();
    NodeList  hashtag_nodes = selectNodeList(doc,"//DIV[@id='htltags']/SPAN");
    for(int i=0; i<hashtag_nodes.getLength(); i++){
    	if(((Element)hashtag_nodes.item(i)) != null){
    	    hashtagTmp.append(((Element)hashtag_nodes.item(i)).getTextContent()).append("##");
    	}	    	  
    }
	String  hashtagStr = hashtagTmp.substring(0,hashtagTmp.length()-2).replaceAll("\n", "").replaceAll(" ", "");
	if(hashtagStr != null){
		crawlDatas.add(new CrawlData(url, "hashtag","酒景主题标签").setTextValue(hashtagStr,page));
	}else{
		crawlDatas.add(new CrawlData(url, "hashtag","酒景主题标签").setTextValue("",page));
	}  
	
	/*总机电话*/
	String  telephoneStr = getXPathValue(doc, "//DIV[@id='htlDes']/P/SPAN/@*").substring(2,14);
	if(telephoneStr != null){
		crawlDatas.add(new CrawlData(url, "telephone","总机电话").setTextValue(telephoneStr,page));
	}else{
		crawlDatas.add(new CrawlData(url, "telephone","总机电话").setTextValue("",page));
	}  
	
	/*酒景简介*/
	String  introductionStr = getXPathValue(doc, "//SPAN[@itemprop='description']").replaceAll(" ", "");
	if(introductionStr != null){
		crawlDatas.add(new CrawlData(url, "introduction","酒景简介").setTextValue(introductionStr.trim(),page));
	}else{
		crawlDatas.add(new CrawlData(url, "introduction","酒景简介").setTextValue("",page));
	}  
	
	/*配套设施*/
     StringBuffer supportingFacilitTmp = new StringBuffer();
	 NodeList  supporting_nodes = selectNodeList(doc, "//DIV[@id='J_htl_facilities']/TABLE/TBODY/TR[1]/TD/UL/LI");
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
	
	/*活动设施*/
	StringBuffer activityFacilityTmp = new StringBuffer();
	    NodeList  activity_nodes = selectNodeList(doc, "//DIV[@id='J_htl_facilities']/TABLE/TBODY/TR[2]/TD/UL/LI");
	    for(int i=0; i<activity_nodes.getLength(); i++){
	       if(((Element)activity_nodes.item(i)) != null){
	    	 activityFacilityTmp.append(((Element)activity_nodes.item(i)).getTextContent()).append("##");
	        }	    	  
	    }
	    String activityFacilityStr=activityFacilityTmp.substring(0,activityFacilityTmp.length()-2).replaceAll("\n", "").replaceAll(" ", "");
	if(activityFacilityStr != null){
		crawlDatas.add(new CrawlData(url, "actFac","活动设施").setTextValue(activityFacilityStr,page));
	}else{
		crawlDatas.add(new CrawlData(url, "actFac","活动设施").setTextValue("",page));
	}  
	
	/*酒店服务*/
	StringBuffer hotelServiceTmp = new StringBuffer();
	    NodeList  hotel_nodes = selectNodeList(doc, "//DIV[@id='J_htl_facilities']/TABLE/TBODY/TR[3]/TD/UL/LI");
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
	
	/*客房设施*/
	StringBuffer roomFacilityTmp = new StringBuffer();
	    NodeList  roomFacility_nodes = selectNodeList(doc, "//DIV[@id='J_htl_facilities']/TABLE/TBODY/TR[4]/TD/UL/LI");
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
	
	/*酒景基本信息*/
	String  basicInfoStr = getXPathValue(doc, "//DIV[@id='htlDes']/P").substring(0,12);//酒景基本信息1
	StringBuffer informationsb = new StringBuffer();
	informationsb.append(basicInfoStr);
	    String informationStr=null;
	    NodeList  informationpos_nodes = selectNodeList(doc,  "//DIV[@class='htl_info_table']/TABLE[@class='detail_extracontent']/TBODY/TR/TH");
	    NodeList  informationdis_nodes = selectNodeList(doc, "//DIV[@class='htl_info_table']/TABLE[@class='detail_extracontent']/TBODY/TR/TD");
	    if(informationpos_nodes!=null&&informationdis_nodes!=null)
	    {
	       if(informationpos_nodes.getLength()==informationdis_nodes.getLength())
	       {
	    	   for(int i=0; i<informationpos_nodes.getLength(); i++){
	 	            if(((Element)informationpos_nodes.item(i) != null)&&((Element)informationdis_nodes.item(i) != null)){
	 	            	informationsb.append(((Element)informationpos_nodes.item(i)).getTextContent());
	 	            	informationsb.append(((Element)informationdis_nodes.item(i)).getTextContent());
	 	            }	    	  
	 	        }
	       }
	    } 
	    
	    if(informationsb.length()>=2){
	    	informationStr=informationsb.substring(0,informationsb.length()-2).replaceAll("\n", "").replaceAll(" ", "");//酒景基本信息2
	    }
	//String  informationStr = getXPathValue(doc, "//DIV[@class='htl_info_table']/TABLE[@class='detail_extracontent']/TBODY").replaceAll("\n", "").replaceAll(" ", "");
	if(informationStr != null){
		crawlDatas.add(new CrawlData(url, "basicInfo","酒景基本信息").setTextValue(informationStr,page));
	}else{
		crawlDatas.add(new CrawlData(url, "basicInfo","酒景基本信息").setTextValue("",page));
	}  
	
	
    /*周边交通 */ 	
//	    NodeList  trafficpos_nodes = selectNodeList(doc, "//DIV[@class='traffic_box']/DIV[position()<11]/P[@class='name']");
//	    NodeList  trafficdis_nodes = selectNodeList(doc, "//DIV[@class='traffic_box']/DIV[position()<11]/P[@class='distance']");
	    NodeList  trafficItems = selectNodeList(doc, "//DIV[@class='traffic_box']/DIV[@class='traffic_item']");
	   
	    if(trafficItems != null){
	    	  JSONArray trs = new JSONArray();
	    	for(int i=0; i<trafficItems.getLength(); i++){
	    		Node trafficItem = trafficItems.item(i);
	    		List<String> tr = new ArrayList<String>(2);
	    		tr.add(getXPathAttribute(trafficItems.item(i), "span", "class"));
	    		String trafficStr=null;
	    		StringBuffer trafficsb = new StringBuffer();
	    		NodeList  trafficpos_nodes = selectNodeList(trafficItem, "P[@class='name']");
	    	   NodeList  trafficdis_nodes = selectNodeList(trafficItem, "P[@class='distance']");
	    	   if(trafficpos_nodes.getLength()==trafficdis_nodes.getLength())
		       {
		    	   int j=1;
		    	   for(int k=0; k<trafficpos_nodes.getLength(); k++){
		 	            if(((Element)trafficpos_nodes.item(k) != null)&&((Element)trafficdis_nodes.item(k) != null)){
		 	            	trafficsb.append(j).append("##"); 	 	    	      
		 	            	trafficsb.append(((Element)trafficpos_nodes.item(k)).getTextContent()).append("##");
		 	    	        trafficsb.append(((Element)trafficdis_nodes.item(k)).getTextContent()).append("$;");
		 	    	        j++;
		 	            }	    	  
		 	        }
		    	   if(trafficsb.length()>=2){
		  	         trafficStr=trafficsb.substring(0,trafficsb.length()-2).replaceAll("\n", "").replaceAll(" ", "");
		    	   }
		    	   tr.add(trafficStr);
		    	   trs.add(tr);
		       }
	    	   
	    	}
	    	 crawlDatas.add(new CrawlData(url, "surTra","周边交通").setJsonValue(trs,page));
	    	 
	    }
	   
	    
//	    String trafficStr="";
       	

        
       saveCrawlData(url, crawlDatas, page);
       
     //添加数据 到 汇总表
     //mergeCrawlDataToMongo(url, crawlDatas);
        
	   return parse;
		

}

	
	
	@Override
    public String getUrlFilterRegex() {
		// http://hotels.ctrip.com/hotel/436187.html#ctm_ref=hod_sr_lst_dl_n_1_1
		// http://hotels.ctrip.com/hotel/485122.html#ctm_ref=hod_sr_lst_dl_n_3_1
		// http://hotels.ctrip.com/hotel/2083684.html#ctm_ref=hod_sr_lst_dl_n_1_1
		return "^http://hotels.ctrip.com/hotel/\\d+\\.html$";
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
