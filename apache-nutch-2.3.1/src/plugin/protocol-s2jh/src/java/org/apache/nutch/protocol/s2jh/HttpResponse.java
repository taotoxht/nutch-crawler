/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nutch.protocol.s2jh;

// JDK imports

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.avro.util.Utf8;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.metadata.SpellCheckedMetadata;
import org.apache.nutch.net.protocols.HttpDateFormat;
import org.apache.nutch.net.protocols.Response;
import org.apache.nutch.parse.ParseFilter;
import org.apache.nutch.parse.s2jh.AbstractHtmlParseFilter;
import org.apache.nutch.parse.s2jh.HtmlParseFilterHelper;
import org.apache.nutch.plugin.Extension;
import org.apache.nutch.plugin.ExtensionPoint;
import org.apache.nutch.plugin.PluginDescriptor;
import org.apache.nutch.plugin.PluginRepository;
import org.apache.nutch.protocol.ProtocolException;
import org.apache.nutch.protocol.htmlunit.HttpWebClient;
import org.apache.nutch.protocol.htmlunit.ProxyIpPool;
import org.apache.nutch.protocol.http.api.HttpBase;
import org.apache.nutch.protocol.http.api.HttpException;
import org.apache.nutch.storage.WebPage;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/** 
 * An smart http fetcher.  Using HTTP, Htmlunit and Selenium WebDriver to fetch ajax page document.
 * Check the integrity of (AJAX) http response based on org.apache.nutch.parse.s2jh.isParseDataFetchLoaded callback method
 * First, try to use basic HTTP Socket to fetch page content, if we don't get expected content, continue,
 * Second, try to use Htmlunit to fetch page content, if we don't get expected content, continue,
 * Third, try to use Selenium WebDriver to fetch page content
 * 
 * @author EMAIL:s2jh-dev@hotmail.com , QQ:2414521719
 */
public class HttpResponse implements Response {

    private Configuration conf;
    private final HttpBase http;
    private final URL url;
    private byte[] content;
    private int code;
    private final Metadata headers = new SpellCheckedMetadata();
    //ip代理池
    private ProxyIpPool proxyIpPool;
    
    private String charset;
    
    private String webDriverPath;

    protected enum Scheme {
        HTTP, HTTPS,
    }

    public HttpResponse(HttpBase http, URL url, WebPage page) throws ProtocolException, IOException {
        conf = http.getConf();
        webDriverPath = conf.get("webdriver.binary.path");
        this.http = http;
        this.url = url;
        Scheme scheme = null;

        if ("http".equals(url.getProtocol())) {
            scheme = Scheme.HTTP;
        } else if ("https".equals(url.getProtocol())) {
            scheme = Scheme.HTTPS;
        } else {
            throw new HttpException("Unknown scheme (not http/https) for url:" + url);
        }

        if (Http.LOG.isTraceEnabled()) {
            Http.LOG.trace("fetching " + url);
        }

        String path = "".equals(url.getFile()) ? "/" : url.getFile();

        // some servers will redirect a request with a host line like
        // "Host: <hostname>:80" to "http://<hpstname>/<orig_path>"- they
        // don't want the :80...

        String host = url.getHost();
        int port;
        String portString;
        if (url.getPort() == -1) {
            if (scheme == Scheme.HTTP) {
                port = 80;
            } else {
                port = 443;
            }
            portString = "";
        } else {
            port = url.getPort();
            portString = ":" + port;
        }
        Socket socket = null;

        try {
            socket = new Socket(); // create the socket
            socket.setSoTimeout(http.getTimeout());

            // connect
            String proxyHost =null;
            int proxyPort = 0;
            if(http.isUseProxyPool()){
       		 proxyIpPool = ProxyIpPool.getSingleton(conf);
       		  String[] proxy = proxyIpPool.getProxy();
       		  if(proxy!=null){
       			  proxyHost = proxy[0];
       			  proxyPort = Integer.parseInt(proxy[1]);
       		  }
            }
            String sockHost =proxyHost!=null?proxyHost:host;
            int sockPort = proxyHost!=null?proxyPort:port;
            
//            String sockHost = http.useProxy() ? http.getProxyHost() : host;
//            int sockPort = http.useProxy() ? http.getProxyPort() : port;
            InetSocketAddress sockAddr = new InetSocketAddress(sockHost, sockPort);
            socket.connect(sockAddr, http.getTimeout());

            if (scheme == Scheme.HTTPS) {
                SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                SSLSocket sslsocket = (SSLSocket) factory.createSocket(socket, sockHost, sockPort, true);
                sslsocket.setUseClientMode(true);

                // Get the protocols and ciphers supported by this JVM
                Set<String> protocols = new HashSet<String>(Arrays.asList(sslsocket.getSupportedProtocols()));
                Set<String> ciphers = new HashSet<String>(Arrays.asList(sslsocket.getSupportedCipherSuites()));

                // Intersect with preferred protocols and ciphers
                protocols.retainAll(http.getTlsPreferredProtocols());
                ciphers.retainAll(http.getTlsPreferredCipherSuites());

                sslsocket.setEnabledProtocols(protocols.toArray(new String[protocols.size()]));
                sslsocket.setEnabledCipherSuites(ciphers.toArray(new String[ciphers.size()]));

                sslsocket.startHandshake();
                socket = sslsocket;
            }

            if (sockAddr != null && conf.getBoolean("store.ip.address", false) == true) {
                String ipString = sockAddr.getAddress().getHostAddress(); // get the ip
                                                                          // address
                page.getMetadata().put(new Utf8("_ip_"), ByteBuffer.wrap(ipString.getBytes()));
            }

            Http.LOG.debug("HTTP fetching: " + url);
            // make request
            OutputStream req = socket.getOutputStream();

            StringBuffer reqStr = new StringBuffer("GET ");
            if (proxyHost != null) {
                reqStr.append(url.getProtocol() + "://" + host + portString + path);
            } else {
                reqStr.append(path);
            }

            reqStr.append(" HTTP/1.0\r\n");

            reqStr.append("Host: ");
            reqStr.append(host);
            reqStr.append(portString);
            reqStr.append("\r\n");

            reqStr.append("Accept-Encoding: x-gzip, gzip\r\n");

            reqStr.append("Accept: ");
            reqStr.append(this.http.getAccept());
            reqStr.append("\r\n");

            String userAgent = http.getUserAgent();
            if ((userAgent == null) || (userAgent.length() == 0)) {
                if (Http.LOG.isErrorEnabled()) {
                    Http.LOG.error("User-agent is not set!");
                }
            } else {
                reqStr.append("User-Agent: ");
                reqStr.append(userAgent);
                reqStr.append("\r\n");
            }

            // if (page.isReadable(WebPage.Field.MODIFIED_TIME.getIndex())) {
            reqStr.append("If-Modified-Since: " + HttpDateFormat.toString(page.getModifiedTime()));
            reqStr.append("\r\n");
            // }
            reqStr.append("\r\n");

            byte[] reqBytes = reqStr.toString().getBytes();

            req.write(reqBytes);
            req.flush();

            PushbackInputStream in = // process response
            new PushbackInputStream(new BufferedInputStream(socket.getInputStream(), Http.BUFFER_SIZE), Http.BUFFER_SIZE);

            StringBuffer line = new StringBuffer();

            boolean haveSeenNonContinueStatus = false;
            while (!haveSeenNonContinueStatus) {
                // parse status code line
                this.code = parseStatusLine(in, line);
                // parse headers
                parseHeaders(in, line);
                haveSeenNonContinueStatus = code != 100; // 100 is "Continue"
            }

            if (!url.toString().endsWith("robots.txt")) {
                if (readPlainContent(url.toString(), in)) {
                } else if (readPlainContentByHtmlunit(url,proxyHost,proxyPort)) {
                } else {
                    readPlainContentByWebDriver(url,proxyHost,proxyPort,webDriverPath);
                }
            }

            if (content != null && content.length > 0) {
                String html = charset == null ? new String(content) : new String(content, charset);
                //System.out.println("URL: " + url + ", CharsetName: " + charset + " , Page HTML=\n" + html);
                Http.LOG_HTML.trace("URL: " + url + ", CharsetName: " + charset + " , Page HTML=\n" + html);
            }

            // add headers in metadata to row
            if (page.getHeaders() != null) {
                page.getHeaders().clear();
            }
            for (String key : headers.names()) {
                page.getHeaders().put(new Utf8(key), new Utf8(headers.get(key)));
            }

        } catch (Exception e) {
            Http.LOG.error(e.getMessage(), e);
        } finally {
            if (socket != null)
                socket.close();
        }

    }

    /*
     * ------------------------- * <implementation:Response> *
     * -------------------------
     */

    public URL getUrl() {
        return url;
    }

    public int getCode() {
        return code;
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public Metadata getHeaders() {
        return headers;
    }

    public byte[] getContent() {
        return content;
    }

    private static final int MAX_AJAX_WAIT_SECONDS = 3;

    private boolean readPlainContent(String urlStr, InputStream in) throws Exception {
        boolean forceAjax = conf.getBoolean("fetch.force.ajax.support", true);
        if (forceAjax) {
            return false;
        }
        if (urlStr.indexOf("detail.tmall.com") > -1) {
            return false;
        }
        int contentLength = Integer.MAX_VALUE; // get content length
        String contentLengthString = headers.get(Response.CONTENT_LENGTH);
        if (contentLengthString != null) {
            contentLengthString = contentLengthString.trim();
            try {
                if (!contentLengthString.isEmpty())
                    contentLength = Integer.parseInt(contentLengthString);
            } catch (NumberFormatException e) {
                throw new HttpException("bad content length: " + contentLengthString);
            }
        }
        if (http.getMaxContent() >= 0 && contentLength > http.getMaxContent()) // limit
                                                                               // download
                                                                               // size
            contentLength = http.getMaxContent();

        ByteArrayOutputStream out = new ByteArrayOutputStream(Http.BUFFER_SIZE);
        byte[] bytes = new byte[Http.BUFFER_SIZE];
        int length = 0;
        // read content
        int i = in.read(bytes);
        while (i != -1) {
            out.write(bytes, 0, i);
            length += i;
            if (length >= contentLength) {
                break;
            }
            if ((length + Http.BUFFER_SIZE) > contentLength) {
                // reading next chunk may hit contentLength,
                // must limit number of bytes read
                i = in.read(bytes, 0, (contentLength - length));
            } else {
                i = in.read(bytes);
            }
        }

        boolean ok = isParseDataFetchLoaded(urlStr, new String(out.toByteArray()));
        if (ok) {
            content = out.toByteArray();

            String contentEncoding = getHeader(Response.CONTENT_ENCODING);
            if ("gzip".equals(contentEncoding) || "x-gzip".equals(contentEncoding)) {
                content = http.processGzipEncoded(content, url);
            } else {
                if (Http.LOG.isTraceEnabled()) {
                    Http.LOG.trace("fetched " + content.length + " bytes from " + url);
                }
            }
        }
        return ok;
    }

    private boolean readPlainContentByHtmlunit(URL url,String proxyHost,int proxyPort) throws Exception {
    	
    	long inst = System.currentTimeMillis();
    	
        String urlStr = url.toString();
        if (urlStr.indexOf("detail.tmall.com") > -1) {
            return false;
        }
        Http.LOG.debug("Htmlunit fetching: " + url);
        HtmlPage page = (HtmlPage) HttpWebClient.getHtmlPage(urlStr, conf,proxyHost,proxyPort);
        charset = page.getPageEncoding();
        String html = null;
        boolean ok = true;

        int i = 0;
        while (i++ < MAX_AJAX_WAIT_SECONDS) {
            html = page.asXml();
            ok = isParseDataFetchLoaded(urlStr, html);
            if (ok) {
                break;
            }
            Http.LOG.info("Sleep " + i + " seconds to wait Htmlunit execution...");
            Thread.sleep(1000);
        }
        if (ok == true) {
            this.code = 200;
            Http.LOG.debug("Success parse page by Htmlunit  for: {}", url);
            html = StringUtils.substringAfter(html, "?>").trim();
        }

        if (ok) {
            this.code = 200;
            content = html.getBytes(charset);
        } else {
            Http.LOG.warn("Failure Htmlunit parse page for: {}", url);
            Http.LOG.warn("Htmlunit Fetch Failure URL: " + url + ", CharsetName: " + charset + " , Page HTML=\n" + html);
        }
        long ined = System.currentTimeMillis();
        Http.LOG.warn("readPlainContentByHtmlunit time elapsed : {}", (ined-inst)/1000);
        return ok;
    }

    /**
     * Most page can be fetched and parsed efficiently by Htmlunit, 
     * But some sites (such as ) using some special javascript tech that can't be processed by Htmlunit correctly,
     * We use Selenium WebDriver even lower process speed.
     * @param url
     * @throws Exception
     */
    private void readPlainContentByWebDriver(URL url,String proxyHost,int proxyPort,String webDriverPath) throws Exception {
    	long inst = System.currentTimeMillis();
        String urlStr = url.toString();
        Http.LOG.debug("WebDriver fetching: " + url);
        String html = null;
        boolean ok = true;

        WebDriver driver = null;
        int i = 0;
        try {
//        	driver = createFirefoxDriver(proxyHost,proxyPort,webDriverPath);
        	driver = createPhantomjsDriver(proxyHost,proxyPort,webDriverPath);
        	
            driver.get(url.toString());
            //Trigger scroll event to get ajax content                
            ((JavascriptExecutor) driver).executeScript("scroll(0," +  6000 + ");");
           
            while (i++ < MAX_AJAX_WAIT_SECONDS) {
                html = driver.getPageSource().trim();
                ok = isParseDataFetchLoaded(urlStr, html);
                if (ok) {
                    break;
                }
                //Trigger scroll event to get ajax content                
                ((JavascriptExecutor) driver).executeScript("scroll(0," + ((i+1) * 6000) + ");");
                Http.LOG.info("Sleep " + i + " seconds to wait WebDriver execution...");
                Thread.sleep(1000);
            }
        }catch(Exception e){
        	e.printStackTrace();
        } finally {
            //Ensure driver quit
            if (driver != null) {
                driver.quit();
            }
        }
        
        if (ok) {
            Http.LOG.debug("Success parse page by WebDriver  for: {}", url);
            this.code = 200;
            content = html.getBytes();
        } else {
        	// 重试次数超过限制 不管成功与否 直接保存结果 避免 数据丢失
        	if(i >= MAX_AJAX_WAIT_SECONDS){
        		Http.LOG.warn("Failure WebDriver parse page for: {},but inject the content!!!", url);
        		 this.code = 200;
                 content = html.getBytes();
        	}
            Http.LOG.warn("Failure WebDriver parse page for: {}", url);
            Http.LOG.warn("WebDriver Fetch Failure URL: " + url + ", CharsetName: " + charset + " , Page HTML=\n" + html);
        }
        long ined = System.currentTimeMillis();
        Http.LOG.warn("readPlainContentByWebDriver time elapsed : {}", (ined-inst)/1000);
        
    }

    private WebDriver createPhantomjsDriver(String proxyHost, int proxyPort,
			String webDriverPath) {
    	WebDriver driver = null;
    	DesiredCapabilities profile = new DesiredCapabilities();
    	profile.setCapability("phantomjs.binary.path",webDriverPath);
    	profile.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 UBrowser/5.6.12150.8 Safari/537.36");
    	profile.setJavascriptEnabled(true);
    	// Disable "web-security", enable all possible "ssl-protocols" and "ignore-ssl-errors" for PhantomJSDriver
    	//and proxy
    	List<String> argList=new ArrayList<>();
    	argList.add("--web-security=false");
    	argList.add("--ssl-protocol=any");
    	argList.add("--ignore-ssl-errors=true");
    	argList.add("--webdriver-loglevel=INFO");
    	if(StringUtils.isNotEmpty(proxyHost)){
    		
    		argList.add("--proxy="+proxyHost+":"+proxyPort);
    		argList.add("--proxy-type=http");
//    		argList.add("--proxy=http://"+"127.0.0.1"+":"+proxyPort);
//    		argList.add("--proxy-type=http");
    	}
    	profile.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS,argList);
    	profile.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX+"loadImages ", false);
    	profile.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX+"resourceTimeout  ", 1000*2);
    	driver = new PhantomJSDriver(profile);
		return driver;
	}

	private WebDriver createFirefoxDriver(String proxyHost, int proxyPort,
			String webDriverPath2) {
    	WebDriver driver = null;
    	//指定firefox的安装路径
    	System.setProperty("webdriver.firefox.bin",webDriverPath);
    	if(!StringUtils.isEmpty(proxyHost)){
    		FirefoxProfile profile = new FirefoxProfile();
            profile.setPreference("network.proxy.type", 1);
            profile.setPreference("network.proxy.http", proxyHost);
            profile.setPreference("network.proxy.http_port", proxyPort);
            profile.setPreference("network.proxy.ssl", proxyHost);
            profile.setPreference("network.proxy.ssl_port", proxyPort);
            profile.setPreference("network.proxy.ftp", proxyHost);
            profile.setPreference("network.proxy.ftp_port", proxyPort);
            profile.setPreference("network.proxy.socks", proxyHost);
            profile.setPreference("network.proxy.socks_port", proxyPort);
            driver = new FirefoxDriver(profile);
    	}else{
    		driver = new FirefoxDriver();
    	}
		return driver;
	}
	
	/**
     * Check excepted data has loaded within the current AJAX page content
     * If not we need sleep sometime to wait ajax execution to get more content  
     * @param url 
     * @param html 
     * @return
     * @throws IllegalAccessException 
     * @throws InstantiationException 
	 * @throws ClassNotFoundException 
     */
//    private boolean isParseDataFetchLoaded(String url, String html) {
//        boolean ok = true;
//      
//        AbstractHtmlParseFilter[] parseFilters = AbstractHtmlParseFilter.getParseFilters(conf);
//        Http.LOG.error("Invoke isParseDataFetchLoaded , parseFilters length {} , ", parseFilters.length);
//        if (parseFilters != null) {
//            for (AbstractHtmlParseFilter htmlParseFilter : parseFilters) {
//                Boolean ret = htmlParseFilter.isParseDataFetchLoaded(url, html);
//                Http.LOG.debug("Invoke isParseDataFetchLoaded of {} , return : {}", htmlParseFilter.getClass(), ret);
//                //Any one return NOT loaded, break and return flase
//                if (ret == false) {
//                    ok = false;
//                    break;
//                }
//            }
//        }
//        return ok;
//    }
    private boolean isParseDataFetchLoaded(String url, String html) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
	    boolean ok = true;
	    PluginRepository pluginRepository = PluginRepository.get(conf);
	    PluginDescriptor pd = pluginRepository.getPluginDescriptor("parse-s2jh");
	    ClassLoader oldClazzLoader = Thread.currentThread().getContextClassLoader();
	    Http.LOG.error("taotoxht oldClazzLoader , ",oldClazzLoader.toString());
	    Thread.currentThread().setContextClassLoader(pd.getClassLoader());
	    Http.LOG.error("taotoxht newClazzLoader , ",pd.getClassLoader());
	    AbstractHtmlParseFilter[] parseFilters = AbstractHtmlParseFilter.getParseFilters(conf);
	    Http.LOG.error("taotoxht AbstractHtmlParseFilter.class loaded by  , ",AbstractHtmlParseFilter.class.getClassLoader());
	    Thread.currentThread().setContextClassLoader(oldClazzLoader);
//	    AbstractHtmlParseFilter[] parseFilters = HtmlParseFilterHelper.getParseFilters("org.apache.nutch.parse.s2jh");
	    Http.LOG.error("Invoke isParseDataFetchLoaded , parseFilters length {} , ", parseFilters.length);
	    if (parseFilters != null) {
	        for (AbstractHtmlParseFilter htmlParseFilter : parseFilters) {
	            Boolean ret = htmlParseFilter.isParseDataFetchLoaded(url, html);
	            System.out.println(html);
	            Http.LOG.debug("Invoke isParseDataFetchLoaded of {} , return : {}", htmlParseFilter.getClass(), ret);
	            //Any one return NOT loaded, break and return flase
	            if (ret == false) {
	                ok = false;
	                break;
	            }
	        }
	    }
	    return ok;
	}
   /* private boolean isParseDataFetchLoaded(String url, String html) throws InstantiationException, IllegalAccessException {
        boolean ok = true;
      
        AbstractFetchedHtmlEvaluator[] parseFilters = FetchedHtmlEvaluatorHelper.getFetchedHtmlEvaluators("org.apache.nutch.protocol.s2jh.evaluator");
        Http.LOG.error("Invoke isParseDataFetchLoaded , parseFilters length {} , ", parseFilters.length);
        if (parseFilters != null) {
            for (AbstractFetchedHtmlEvaluator htmlParseFilter : parseFilters) {
                Boolean ret = htmlParseFilter.isParseDataFetchLoaded(url, html);
                Http.LOG.debug("Invoke isParseDataFetchLoaded of {} , return : {}", htmlParseFilter.getClass(), ret);
                //Any one return NOT loaded, break and return flase
                if (ret == false) {
                    ok = false;
                    break;
                }
            }
        }
        return ok;
    }
    */

    /**
     * 
     * @param in
     * @param line
     * @throws HttpException
     * @throws IOException
     */
    @SuppressWarnings("unused")
    private void readChunkedContent(PushbackInputStream in, StringBuffer line) throws HttpException, IOException {
        boolean doneChunks = false;
        int contentBytesRead = 0;
        byte[] bytes = new byte[Http.BUFFER_SIZE];
        ByteArrayOutputStream out = new ByteArrayOutputStream(Http.BUFFER_SIZE);

        while (!doneChunks) {
            if (Http.LOG.isTraceEnabled()) {
                Http.LOG.trace("Http: starting chunk");
            }

            readLine(in, line, false);

            String chunkLenStr;
            // if (LOG.isTraceEnabled()) { LOG.trace("chunk-header: '" + line + "'");
            // }

            int pos = line.indexOf(";");
            if (pos < 0) {
                chunkLenStr = line.toString();
            } else {
                chunkLenStr = line.substring(0, pos);
                // if (LOG.isTraceEnabled()) { LOG.trace("got chunk-ext: " +
                // line.substring(pos+1)); }
            }
            chunkLenStr = chunkLenStr.trim();
            int chunkLen;
            try {
                chunkLen = Integer.parseInt(chunkLenStr, 16);
            } catch (NumberFormatException e) {
                throw new HttpException("bad chunk length: " + line.toString());
            }

            if (chunkLen == 0) {
                doneChunks = true;
                break;
            }

            if (http.getMaxContent() >= 0 && (contentBytesRead + chunkLen) > http.getMaxContent())
                chunkLen = http.getMaxContent() - contentBytesRead;

            // read one chunk
            int chunkBytesRead = 0;
            while (chunkBytesRead < chunkLen) {

                int toRead = (chunkLen - chunkBytesRead) < Http.BUFFER_SIZE ? (chunkLen - chunkBytesRead) : Http.BUFFER_SIZE;
                int len = in.read(bytes, 0, toRead);

                if (len == -1)
                    throw new HttpException("chunk eof after " + contentBytesRead + " bytes in successful chunks" + " and " + chunkBytesRead
                            + " in current chunk");

                // DANGER!!! Will printed GZIPed stuff right to your
                // terminal!
                // if (LOG.isTraceEnabled()) { LOG.trace("read: " + new String(bytes, 0,
                // len)); }

                out.write(bytes, 0, len);
                chunkBytesRead += len;
            }

            readLine(in, line, false);
        }

        if (!doneChunks) {
            if (contentBytesRead != http.getMaxContent())
                throw new HttpException("chunk eof: !doneChunk && didn't max out");
            return;
        }

        content = out.toByteArray();
        parseHeaders(in, line);

    }

    private int parseStatusLine(PushbackInputStream in, StringBuffer line) throws IOException, HttpException {
        readLine(in, line, false);

        int codeStart = line.indexOf(" ");
        int codeEnd = line.indexOf(" ", codeStart + 1);

        // handle lines with no plaintext result code, ie:
        // "HTTP/1.1 200" vs "HTTP/1.1 200 OK"
        if (codeEnd == -1)
            codeEnd = line.length();

        int code;
        try {
            code = Integer.parseInt(line.substring(codeStart + 1, codeEnd));
        } catch (NumberFormatException e) {
            throw new HttpException("bad status line '" + line + "': " + e.getMessage(), e);
        }

        return code;
    }

    private void processHeaderLine(StringBuffer line) throws IOException, HttpException {

        int colonIndex = line.indexOf(":"); // key is up to colon
        if (colonIndex == -1) {
            int i;
            for (i = 0; i < line.length(); i++)
                if (!Character.isWhitespace(line.charAt(i)))
                    break;
            if (i == line.length())
                return;
            throw new HttpException("No colon in header:" + line);
        }
        String key = line.substring(0, colonIndex);

        int valueStart = colonIndex + 1; // skip whitespace
        while (valueStart < line.length()) {
            int c = line.charAt(valueStart);
            if (c != ' ' && c != '\t')
                break;
            valueStart++;
        }
        String value = line.substring(valueStart);
        headers.set(key, value);
    }

    // Adds headers to our headers Metadata
    private void parseHeaders(PushbackInputStream in, StringBuffer line) throws IOException, HttpException {

        while (readLine(in, line, true) != 0) {

            // handle HTTP responses with missing blank line after headers
            int pos;
            if (((pos = line.indexOf("<!DOCTYPE")) != -1) || ((pos = line.indexOf("<HTML")) != -1) || ((pos = line.indexOf("<html")) != -1)) {

                in.unread(line.substring(pos).getBytes("UTF-8"));
                line.setLength(pos);

                try {
                    // TODO: (CM) We don't know the header names here
                    // since we're just handling them generically. It would
                    // be nice to provide some sort of mapping function here
                    // for the returned header names to the standard metadata
                    // names in the ParseData class
                    processHeaderLine(line);
                } catch (Exception e) {
                    // fixme:
                    Http.LOG.error("Failed with the following exception: ", e);
                }
                return;
            }

            processHeaderLine(line);
        }
    }

    private static int readLine(PushbackInputStream in, StringBuffer line, boolean allowContinuedLine) throws IOException {
        line.setLength(0);
        for (int c = in.read(); c != -1; c = in.read()) {
            switch (c) {
            case '\r':
                if (peek(in) == '\n') {
                    in.read();
                }
            case '\n':
                if (line.length() > 0) {
                    // at EOL -- check for continued line if the current
                    // (possibly continued) line wasn't blank
                    if (allowContinuedLine)
                        switch (peek(in)) {
                        case ' ':
                        case '\t': // line is continued
                            in.read();
                            continue;
                        }
                }
                return line.length(); // else complete
            default:
                line.append((char) c);
            }
        }
        throw new EOFException();
    }

    private static int peek(PushbackInputStream in) throws IOException {
        int value = in.read();
        in.unread(value);
        return value;
    }
    
    public static void main(String[] args) {
    	WebDriver driver = null;
    	DesiredCapabilities profile = new DesiredCapabilities();
    	profile.setCapability("phantomjs.binary.path","/opt/phantomjs/phantomjs-2.1.1-linux-x86_64/phantomjs-2.1.1-linux-x86_64/bin/phantomjs");
    	profile.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 UBrowser/5.6.12150.8 Safari/537.36");
    	profile.setJavascriptEnabled(true);
    	// Disable "web-security", enable all possible "ssl-protocols" and "ignore-ssl-errors" for PhantomJSDriver
    	//and proxy
    	List<String> argList=new ArrayList<>();
    	argList.add("--web-security=false");
    	argList.add("--ssl-protocol=any");
    	argList.add("--ignore-ssl-errors=true");
    	argList.add("--webdriver-loglevel=INFO");
    	if(StringUtils.isNotEmpty("127.0.0.1")){
//    		argList.add("--proxy="+"192.168.11.54"+":"+8888);
//    		argList.add("--proxy-type=http");
//    		argList.add("--proxy=http://"+"127.0.0.1"+":"+proxyPort);
//    		argList.add("--proxy-type=http");
    	}
    	profile.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS,argList);
    	driver = new PhantomJSDriver(profile);
        driver.get("http://192.168.9.21:8080/ycf-search/solr/vproduct/search");
//        driver.get("http://hotel.elong.com/guangzhou/32001026/#review");
        String html = driver.getPageSource().trim();
        System.out.println(html);
    }

}
