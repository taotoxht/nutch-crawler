package org.apache.nutch.protocol.htmlunit;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.util.NutchConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Htmlunit WebClient Helper
 * Use one WebClient instance per thread by ThreadLocal to support multiple threads execution
 * 
 * @author EMAIL:s2jh-dev@hotmail.com , QQ:2414521719
 */
public class HttpWebClient {

    private static final Logger LOG = LoggerFactory.getLogger(HttpWebClient.class);

    private static ThreadLocal<WebClient> threadWebClient = new ThreadLocal<WebClient>();

    private static String acceptLanguage;

    public static Page getPage(String url, Configuration conf,String proxyHost,int proxyPort) {
        synchronized (Thread.currentThread()) {
            try {
                WebRequest req = new WebRequest(new URL(url));
                req.setAdditionalHeader("Accept-Language", acceptLanguage);
                //req.setAdditionalHeader("Cookie", "");

                WebClient webClient = threadWebClient.get();
                if (webClient == null) {
                    LOG.info("Initing web client for thread: {}", Thread.currentThread().getId());
//                    webClient = new WebClient(BrowserVersion.FIREFOX_38);
                  //更改为 ip 代理形式
                    if(!StringUtils.isEmpty(proxyHost)){
                    	webClient = new WebClient(BrowserVersion.FIREFOX_38, proxyHost, proxyPort);
                    }else{
                    	 webClient = new WebClient(BrowserVersion.FIREFOX_38);
                    }
                    webClient.getOptions().setCssEnabled(false);
                    webClient.getOptions().setAppletEnabled(false);
                    webClient.getOptions().setThrowExceptionOnScriptError(false);
                    // AJAX support
                    webClient.setAjaxController(new NicelyResynchronizingAjaxController());
                    // Use extension version htmlunit cache process
                    webClient.setCache(new ExtHtmlunitCache());
                    // Enhanced WebConnection based on urlfilter
                    webClient.setWebConnection(new RegexHttpWebConnection(webClient, conf));
                    webClient.waitForBackgroundJavaScript(10000);
                    //设置足够高度以支持一些需要页面内容多需屏幕滚动显示的页面
//                    webClient.getCurrentWindow().setInnerHeight(6000);
                    webClient.getCurrentWindow().setInnerHeight(20000);
                    if (acceptLanguage == null && conf != null) {
                        acceptLanguage = conf.get("http.accept.language", " zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
                    }
                    threadWebClient.set(webClient);
                }
                Page page = webClient.getPage(req);
                return page;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static HtmlPage getHtmlPage(String url, Configuration conf,String proxyHost,int proxyPort) {
        return (HtmlPage) getPage(url, conf,proxyHost,proxyPort);
    }

    public static void main(String[] args) throws InterruptedException, IOException {
    	// 测试 代理可行
    	Configuration conf = NutchConfiguration.create();
        HtmlPage page = getHtmlPage("http://hotel.elong.com/baoding/", conf,"192.168.11.54",8888);
//        HtmlPage page = getHtmlPage("http://hotel.elong.com/guangzhou/32001026", conf,"192.168.11.54",8888);
//        HtmlPage page = getHtmlPage("http://192.168.9.21:8080/ycf-search/solr/vproduct/search?keyWord=%E5%B9%BF%E5%B7%9E", null,"192.168.11.54",8888);
        String html= page.asXml();
        FileUtils.write(new File("/data/sss"), html);
        if(html.indexOf("保定好时运美景酒店")>=0){
        	System.out.println("解析成功");
        }else{
        	System.out.println("解析失败");
        }
        TimeUnit.SECONDS.sleep(10);
        
        
//        if( html.indexOf("ht_pri_num") > 0 &&  html.indexOf("hmap_info_wrap") > 0){
//        	System.out.println("解析成功");
//        }else{
//        	System.out.println("解析失败");
//        }
       
    }
}
