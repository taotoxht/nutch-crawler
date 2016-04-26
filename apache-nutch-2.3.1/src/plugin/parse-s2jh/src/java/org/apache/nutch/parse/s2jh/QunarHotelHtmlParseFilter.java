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
        	
        	try {
        		crawlDatas.add(new CrawlData(url, "name","酒店名称").setTextValue(getXPathValue(doc, "//DIV[@class='b_wrap']/DIV[@class='b-crumbs']/DIV/SPAN[2]/SPAN"),page));
            	System.out.println("name:" + getXPathValue(doc, "//DIV[@class='b_wrap']/DIV[@class='b-crumbs']/DIV/SPAN[2]/SPAN"));
			} catch (Exception e) {
				// TODO: handle exception
				crawlDatas.add(new CrawlData(url, "name","酒店名称").setTextValue("",page));
			}
        	
        	try {
        		crawlDatas.add(new CrawlData(url, "style","酒景类型").setTextValue(getXPathValue(doc, "//DIV[@class='b_wrap']/DIV[@class='b-crumbs']/DIV/SPAN[2]/A[2]").substring(2, 4),page));
            	System.out.println("style:" + getXPathValue(doc, "//DIV[@class='b_wrap']/DIV[@class='b-crumbs']/DIV/SPAN[2]/A[2]").substring(2, 4));
			} catch (Exception e) {
				// TODO: handle exception
				crawlDatas.add(new CrawlData(url, "style","酒景类型").setTextValue("",page));
			}
        	
        	try {
        		crawlDatas.add(new CrawlData(url, "address","酒店地址").setTextValue(getXPathValue(doc, 
            			"//DIV[@id='detail_pageHeader']/P[@class='adress']/SPAN").replaceAll(" ", ""),page));
            	System.out.println("address:" + getXPathValue(doc, 
            			"//DIV[@id='detail_pageHeader']/P[@class='adress']/SPAN").replaceAll(" ", ""));
				
			} catch (Exception e) {
				// TODO: handle exception
				crawlDatas.add(new CrawlData(url, "address","酒店地址").setTextValue("",page));
			}
        
        	try {
        		crawlDatas.add(new CrawlData(url, "level","酒店级别").setTextValue(getXPathValue(doc, "//DIV[@id='detail_pageHeader']/H2/EM"),page));
            	System.out.println("level:" + getXPathValue(doc, "//DIV[@id='detail_pageHeader']/H2/EM"));
			} catch (Exception e) {
				// TODO: handle exception
				crawlDatas.add(new CrawlData(url, "level","酒店级别").setTextValue("",page));
			}
        	
        	/*设施概况(1)*/
        	String[] hotelContent = new String[]{"联系方式","基本信息","酒店简介","网络设施","停车场","房间设施","酒店服务","酒店设施"};
        	NodeList nodelist1 = selectNodeList(doc, "//DIV[@id='descContent']/DIV[1]/DL");
        	for(int i=0; i<nodelist1.getLength(); i++){
        		try {
					NodeList no1 = ((Element)nodelist1.item(i)).getElementsByTagName("DT");
					NodeList no2 = ((Element)nodelist1.item(i)).getElementsByTagName("DD");
					try {
						String strKey = no1.item(0).getTextContent().replaceAll(" +", "").replaceAll("\n", "").replaceAll("	+", "");
						String strValue = no2.item(0).getTextContent().replaceAll(" +", "").replaceAll("\n", "").replaceAll("	+", "");
						if(strKey.equals(hotelContent[0])){
							if(strValue.length() > 24){
								crawlDatas.add(new CrawlData(url, "telephone","酒店电话").setTextValue(strValue.substring(2, 14),page));														
								crawlDatas.add(new CrawlData(url, "fax","酒店传真").setTextValue(strValue.substring(strValue.length()-12,strValue.length()),page));
							}else if(strValue.length()<24 && strValue.length() > 0){
								crawlDatas.add(new CrawlData(url, "telephone","酒店电话").setTextValue(strValue.substring(2, 14),page));	
							}							
						}else if(strKey.equals(hotelContent[1])){
								crawlDatas.add(new CrawlData(url, "baseInfo","基本信息").setTextValue(no2.item(0).getTextContent().replaceAll(" +", "")
										.replaceAll("\n", "").replaceAll("	+", ""),page));
						}else if(strKey.equals(hotelContent[2])){				
								crawlDatas.add(new CrawlData(url, "information","酒店介绍").setTextValue(no2.item(0).getTextContent().replaceAll(" +", "")
										.replaceAll("\n", "").replaceAll("	+", ""),page));
						}
					} catch (Exception e) {
						// TODO: handle exception
						continue;
					}
					
				} catch (Exception e) {
					// TODO: handle exception
					continue;
				}
        	}
        	
        	/*设施概况(2)*/
        	 NodeList nodelist2 =  selectNodeList(doc, "//DIV[@id='descContent']/DL");
        	 for(int i=0; i<nodelist2.getLength();i++){
        		 try {
        			NodeList no3 = ((Element)nodelist2.item(i)).getElementsByTagName("DT");
 					NodeList no4 = ((Element)nodelist2.item(i)).getElementsByTagName("DD");
 					try {
						String strKey = no3.item(0).getTextContent().replaceAll(" +", "").replaceAll("\n", "").replaceAll("	+", "");
						if(strKey.equals(hotelContent[3])){
							crawlDatas.add(new CrawlData(url, "internetfac","网络设施").setTextValue(no4.item(0).getTextContent().replaceAll(" +", "")
									.replaceAll("\n", "").replaceAll("	+", ""),page));
	 					}else if(strKey.equals(hotelContent[4])){
	 						crawlDatas.add(new CrawlData(url, "parking","停车场").setTextValue(no4.item(0).getTextContent().replaceAll(" +", "")
									.replaceAll("\n", "").replaceAll("	+", ""),page));
	 					}else if(strKey.equals(hotelContent[5])){
	 						NodeList housefacList = ((Element)no4.item(0)).getElementsByTagName("SPAN");
	 						  StringBuffer housefacsb = new StringBuffer();
	 				           if(housefacList.getLength() != 0){
	 				        	   for(int j=0; j<housefacList.getLength(); j++){
	 				        		   String str = "";	 				            	  			            	  
	 				            	   NodeList no = ((Element)housefacList.item(j)).getElementsByTagName("SPAN");
	 				            	   if(no.getLength() != 0){
	 				                  	str = no.item(0).getTextContent().replaceAll(" +", "").replace("\n", "") + ":" + ((Element)no.item(0)).getAttribute("title") + ";"; 	 				              
	 				            	   }
	 				            	   housefacsb.append(str);
	 				               }  
	 				           }
	 				          crawlDatas.add(new CrawlData(url, "housefac","房间设施").
	 				        		  setTextValue(housefacsb.toString().substring(0,housefacsb.toString().length()-1),page));
	 					}else if(strKey.equals(hotelContent[6])){
	 						 NodeList hSnodeList =  ((Element)no4.item(0)).getElementsByTagName("SPAN");	 						
	 						 StringBuffer hotelServicesb = new StringBuffer();
	 				           if(hSnodeList.getLength() != 0){
	 				        	   for(int j=0; j<hSnodeList.getLength(); j++){
	 				        		   String str = "";
	 				            	   NodeList no = ((Element)hSnodeList.item(j)).getElementsByTagName("SPAN");
	 				            	   if(no.getLength() != 0){
	 				                  	str = no.item(0).getTextContent().replaceAll(" +", "").replace("\n", "") + ":" + ((Element)no.item(0)).getAttribute("title") + ";";  				                  	
	 				            	   }
	 				            	   hotelServicesb.append(str);
	 				               }  
	 				           }
	 				     crawlDatas.add(new CrawlData(url, "hotelService","酒店服务").setTextValue(hotelServicesb.toString().substring(0,hotelServicesb.toString().length()-1),page));
	 					}else if(strKey.equals(hotelContent[7])){
	 					   NodeList hFnodeList =  ((Element)no4.item(0)).getElementsByTagName("SPAN");
	 			           StringBuffer hotelfacsb = new StringBuffer();
	 			           if(hFnodeList.getLength() != 0){
	 			        	   for(int j=0; j<hFnodeList.getLength(); j++){
	 			        		   String str = "";	            	  
	 			            	   NodeList no = ((Element)hFnodeList.item(j)).getElementsByTagName("SPAN");
	 			            	   if(no.getLength() != 0){
	 			                  	str = no.item(0).getTextContent().replaceAll(" +", "").replace("\n", "") + ":" + ((Element)no.item(0)).getAttribute("title") + ";"; 
	 			            	   }
	 			            	   hotelfacsb.append(str);
	 			               }  
	 			           }
	 			        crawlDatas.add(new CrawlData(url, "hotelfac","酒店设施").
	 			        		setTextValue(hotelfacsb.toString().substring(0,hotelfacsb.toString().length()-1),page)); 
	 					}
					} catch (Exception e) {
						// TODO: handle exception
						continue;
					} 					
				} catch (Exception e) {
					// TODO: handle exception
					continue;
				}
        		 	
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
