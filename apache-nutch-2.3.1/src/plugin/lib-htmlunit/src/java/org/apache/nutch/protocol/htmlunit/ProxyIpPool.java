package org.apache.nutch.protocol.htmlunit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;

/**
 * 从网上查找到n个 ip 代理 ，采用 随机轮训每次发起http请求从里面拿出一个
 * @author xuhaitao
 *
 */
public class ProxyIpPool
{
	
	private List<String[]> ipPool;
	private static ProxyIpPool sington;
	
	private ProxyIpPool(Configuration conf)
	{
		try
		{
			Reader reader=getProfileReader(conf);
			readToPool(reader);
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		
	}
	
	/**
	 * 初始化 ip pool
	 * @param reader
	 * @throws IOException 
	 */
	private void readToPool(Reader reader) throws IOException
	{
		ipPool=new ArrayList<String[]>();
		BufferedReader br=new BufferedReader(reader);
		String line=null;
		String[] ipAndPort=null;
		while((line=br.readLine() )!= null){
			if(line.length()==0||line.startsWith("#")){
				continue;
			}
			ipAndPort=line.split(" ");
			if(ipAndPort.length<2){
				continue;
			}
			ipPool.add(ipAndPort);
		}
	}
	
	/**
	 * 随机 获取一个代理 ip
	 * @return
	 */
	public String[] getProxy(){
		String[] proxy = null;
		Random r = new Random();
		return ipPool.get(r.nextInt(ipPool.size()));
	}

	/**
     * Rules specified as a config property will override rules specified
     * as a config file.
     */
    private Reader getProfileReader(Configuration conf) throws IOException {
        return conf.getConfResourceAsReader("ip-proxy-pool.txt");
    }

	public static ProxyIpPool getSingleton(Configuration conf) {
		if(sington == null){
			synchronized (ProxyIpPool.class) {
				if(sington == null){
					sington = new ProxyIpPool(conf);
				}
			}
		}
		return sington;
	}
	
}
