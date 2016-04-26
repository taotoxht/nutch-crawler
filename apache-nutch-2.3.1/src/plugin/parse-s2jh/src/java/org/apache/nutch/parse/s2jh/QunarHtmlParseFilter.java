package org.apache.nutch.parse.s2jh;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.nutch.parse.HTMLMetaTags;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.storage.WebPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;

import com.google.common.collect.Lists;

/**
 * 去哪儿网站定制爬虫
 * 
 * @author xuhaitao
 *
 */
public class QunarHtmlParseFilter extends AbstractHtmlParseFilter {

    public static final Logger LOG = LoggerFactory.getLogger(JumeiHtmlParseFilter.class);

    @Override
    public Parse filterInternal(String url, WebPage page, Parse parse, HTMLMetaTags metaTags, DocumentFragment doc) throws Exception {
        List<CrawlData> crawlDatas = Lists.newArrayList();
        
        
        crawlDatas.add(new CrawlData(url, "domain", "域名").setTextValue("qunar.com", page));

        crawlDatas.add(new CrawlData(url, "pubtime", "酒店名").setTextValue(getXPathValue(doc, "//DIV[@id='detail_pageHeader']"), page));

        saveCrawlData(url, crawlDatas, page);

        // 用于网页内容索引的页面内容，一般是去头去尾处理后的有效信息内容
        String txt = getXPathValue(doc, "//DIV[@class='conText']") + getXPathValue(doc, "//DIV[@id='text']");
        if (StringUtils.isNotBlank(txt)) {
            parse.setText(txt);
        } else {
            LOG.warn("NO data parased");
        }

        return parse;
    }

    @Override
    public String getUrlFilterRegex() {
        return "^http://hotel.qunar.com/city/.+/.+/.*$";
    }

    @Override
    protected boolean isParseDataFetchLoadedInternal(String url, String html) {
    	try
		{
			FileUtils.write(new File("E:\\tmp\\"+new Random().nextInt()), html);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	// <div id="pricePanel" class="d-wt980 b-room-tools">
    	if(-1!=html.indexOf("detail_pageHeader")){
    		return true;
    	}
        return false;
    }

    @Override
    protected boolean isContentMatchedForParse(String url, String html) {
        return true;
    }
}