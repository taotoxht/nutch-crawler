package org.apache.nutch.parse.s2jh;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;


import org.apache.nutch.parse.HTMLMetaTags;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.storage.WebPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;

import com.google.common.collect.Lists;

/**
 * 
 * @author EMAIL:s2jh-dev@hotmail.com , QQ:2414521719
 *
 */
public class QunarScenicHtmlParseFilter extends AbstractHtmlParseFilter {

    public static final Logger LOG = LoggerFactory.getLogger(QunarScenicHtmlParseFilter.class);

    @Override
    public Parse filterInternal(String url, WebPage page, Parse parse, HTMLMetaTags metaTags, DocumentFragment doc) throws Exception {
    	
    	List<CrawlData> crawlDatas = Lists.newArrayList();
        crawlDatas.add(new CrawlData(url, "domain", "域名").setTextValue("qunar.com", page));
        System.out.println("开始解析!");

        if (url.startsWith("http://piao.qunar.com/")) {
        	
        	try {
        		crawlDatas.add(new CrawlData(url, "name","酒景名称").setTextValue(getXPathValue(doc, "//DIV[@class='mp-description-view']/SPAN[1]"),page));
			} catch (Exception e) {
				// TODO: handle exception
				crawlDatas.add(new CrawlData(url, "name","酒景名称").setTextValue("",page));
			}
        	try {
        		crawlDatas.add(new CrawlData(url, "level","酒景级别").setTextValue(getXPathValue(doc, "//DIV[@class='mp-description-view']/SPAN[2]"),page));
			} catch (Exception e) {
				// TODO: handle exception
				crawlDatas.add(new CrawlData(url, "level","酒景级别").setTextValue("",page));
			}
        		crawlDatas.add(new CrawlData(url, "style","酒景类型").setTextValue("景区",page));
	
        	try {
        		crawlDatas.add(new CrawlData(url, "address","酒景地址").setTextValue(getXPathValue(doc, 
            			"//DIV[@class='mp-description-detail']/DIV[3]/SPAN[3]"),page));
			} catch (Exception e) {
				// TODO: handle exception
				crawlDatas.add(new CrawlData(url, "address","酒景地址").setTextValue("",page));
			}
        	
        	/*酒景简介 wineScene*/
        	String wineScene = "";
        	try {
        		wineScene = getXPathValue(doc, "//DIV[@id='mp-charact']/DIV[1]/DIV[1]/DIV[1]/P").replaceAll(" +", "").replaceAll("\n", "");
        		//crawlDatas.add(new CrawlData(url, "information","酒景介绍").setTextValue(getXPathValue(doc, "//DIV[@id='mp-charact']/DIV[1]/DIV[1]/DIV/P"),page));
			} catch (Exception e) {
				// TODO: handle exception
				wineScene = "";
			}
        	try {
        		wineScene = wineScene + getXPathValue(doc, "//DIV[@id='mp-charact']/DIV[1]/DIV[2]/DIV").replaceAll(" +", "").replaceAll("\n", "");
			} catch (Exception e) {
				// TODO: handle exception
				wineScene = wineScene + "";
			}
        	crawlDatas.add(new CrawlData(url, "wineScene","酒景简介").setTextValue(wineScene,page));
        	
        	/*酒景基本信息 baseInfo*/        
        	String baseInfo = "";
        	try {
        		baseInfo = getXPathValue(doc, "//DIV[@id='mp-charact']/DIV[3]").replaceAll(" +", "").replaceAll("\n", "");
        		//crawlDatas.add(new CrawlData(url, "information","酒景介绍").setTextValue(getXPathValue(doc, "//DIV[@id='mp-charact']/DIV[1]/DIV[1]/DIV/P"),page));
			} catch (Exception e) {
				// TODO: handle exception
				baseInfo = "";
			}
        	crawlDatas.add(new CrawlData(url, "baseInfo","酒景简介").setTextValue(baseInfo,page));
        	
        	//周边交通，traffic mp-traffic
        	String traffic = "";
        	try {
        		traffic = getXPathValue(doc, "//DIV[@id='mp-traffic']/DIV[4]/DIV[1]").replaceAll(" +", "").replaceAll("\n", "");
        		
			} catch (Exception e) {
				// TODO: handle exception
				traffic = "";
			}
        	try {
        		traffic = traffic + ":" + getXPathValue(doc, "//DIV[@id='mp-traffic']/DIV[4]/DIV[2]").replaceAll(" +", "").replaceAll("\n", "");
        		
			} catch (Exception e) {
				// TODO: handle exception
			}
        	crawlDatas.add(new CrawlData(url, "tarffic","周边交通").setTextValue(traffic,page));
    	//周边景点 ,nearsight,
        	String nearsight="";
        	try {
        		nearsight = nearsight + ":" + getXPathValue(doc, "//DIV[@id='mp-nearsight']/DIV[1]").replaceAll(" +", "").replaceAll("\n", "").replaceAll("	+", "");
			} catch (Exception e) {
				// TODO: handle exception
				nearsight = "";
			}
        	crawlDatas.add(new CrawlData(url, "nearsight","周边景点").setTextValue(nearsight,page));
        
        }
        saveCrawlData(url, crawlDatas, page);
        return parse;
    }

    @Override
    public String getUrlFilterRegex() {
        //http://item.jumei.com/sh150311p619680.html
        //http://www.jumeiglobal.com/deal/ht150122p933681t1.html
        /*return "http://item.jumei.com/.*.html|http://www.jumeiglobal.com/deal/.*.html";*/
    	return "http://piao.qunar.com/ticket/.*|http://piao.qunar.com/.*";
    }

    @Override
    protected boolean isParseDataFetchLoadedInternal(String url, String html) {
    	System.out.println(html.toString());    	
    	if(html.indexOf("mp-groupbody-price") > 0){	
    		byte[] byteContent = html.toString().getBytes();
        	OutputStream os;
    		try {
    			os = new FileOutputStream(new File("D:/Program Files/nutch-ajax/nutch-ajax/apache-nutch-2.3/urls/QunarCLDJQ.html"));
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
