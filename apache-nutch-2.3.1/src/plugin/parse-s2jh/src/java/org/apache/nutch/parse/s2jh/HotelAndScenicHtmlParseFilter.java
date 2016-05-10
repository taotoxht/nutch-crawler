package org.apache.nutch.parse.s2jh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.nutch.storage.WebPage;

import com.ibm.icu.math.BigDecimal;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

/**
 * 酒店 景区信息 添加 共用类
 * 
 * @author root
 *
 */
public abstract class HotelAndScenicHtmlParseFilter extends
		AbstractHtmlParseFilter {

	/**
	 * 三个 网站 合并结果存储数据表
	 */
	private String mergeTableName = "crawl_hotel_scenic";
	private String defDataSrc = "ctrip";

	public boolean findByUrl(String url) {
		MongoClient mongoClient = null;
		try {
			mongoClient = new MongoClient(conf.get("mongodb.host"),
					Integer.valueOf(conf.get("mongodb.port")));
			DB db = mongoClient.getDB(conf.get("mongodb.db"));
			DBCollection coll = db.getCollection(mergeTableName);
			BasicDBObject bo = new BasicDBObject("url", url);
			bo = (BasicDBObject) coll.findOne(bo);
			if (bo != null) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mongoClient.close();
		}
		return false;
	}

	public void mergeCrawlDataToMongo(String url, List<CrawlData> crawlDatas) {
		MongoClient mongoClient = null;
		try {

			mongoClient = new MongoClient(conf.get("mongodb.host"),
					Integer.valueOf(conf.get("mongodb.port")));
			DB db = mongoClient.getDB(conf.get("mongodb.db"));

			// 改由子类定义具体的表名称
			// DBCollection coll = db.getCollection("crawl_data");
			DBCollection coll = db.getCollection(mergeTableName);
			BasicDBObject bo = new BasicDBObject("url", url);
			bo = (BasicDBObject) coll.findOne(bo);
			

			LOG.debug("Saving properties for url: {}", url);

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		} finally {
			mongoClient.close();
		}

	}

	/**
	 * 属性持久化处理，基于nutch-site.xml中parse.data.persist.mode定义值
	 * 
	 * @param url
	 * @param crawlDatas
	 * @param page
	 */
	protected void saveCrawlData(String url, List<CrawlData> crawlDatas,
			WebPage page) {

		String persistMode = conf.get("parse.data.persist.mode");
		if (StringUtils.isBlank(persistMode)
				|| "println".equalsIgnoreCase(persistMode)) {
			System.out.println("Parsed data properties:");
			for (CrawlData crawlData : crawlDatas) {
				System.out.println(" - " + crawlData.getCode() + " : "
						+ crawlData.getDisplayValue());
			}
			return;
		}
		// 添加版本
		MongoClient mongoClient =null;
		try {
			mongoClient = new MongoClient(conf.get("mongodb.host"),
					Integer.valueOf(conf.get("mongodb.port")));
			DB db = mongoClient.getDB(conf.get("mongodb.db"));

			// 改由子类定义具体的表名称
			// DBCollection coll = db.getCollection("crawl_data");
			DBCollection coll = db.getCollection(getTableName());
			BasicDBObject bo = new BasicDBObject("url", url).append(
					"crawlVersion", this.crawlVersion);
			LOG.debug("Saving properties for url: {}", url);
			// 先shanchu
			coll.remove(bo);

			bo.append("fetch_time", System.currentTimeMillis());

			for (CrawlData crawlData : crawlDatas) {
				if (!crawlData.getUrl().equals(url)) {
					LOG.error("Invalid crawlData not match url: {}", url);
					continue;
				}
				bo.append(crawlData.getKey(), crawlData.getValue());
			}
			coll.update(new BasicDBObject("url", url).append("crawlVersion", this.crawlVersion), bo, true, false);
			
			updateMergeTable(db,bo);
			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}finally{
			mongoClient.close();
		}

	}

	/**
	 * 更新 合并数据库 crawl_hotel_scenic
	 * @param mongoClient
	 * @param bo
	 */
	public void updateMergeTable(DB db, BasicDBObject bo) {
		DBCollection coll = db.getCollection(mergeTableName);
		String modifiedFields = hasModifiedToLastVersion(coll,bo);
		if(modifiedFields == null){
			return;
		}
		bo.append("modifiedFields", modifiedFields);
		if (defDataSrc.equals(getDataSrc())) {
			// 携程 的酒店 或景区
			coll.insert(bo);
		} else {
			//非携程的酒店 不存在才更新
			if(!existsInMergeDB(coll, bo.getString("dbResName"), bo.getString("ResAddress"),bo.getString("ResTelephone"))){
				coll.insert(bo);
			}
		}
	}

	/**
	 * 判断合并数据库是否存在该记录
	 * 
	 * @param DBCollection  表操作
	 * @param ResName 酒景名称
	 * @param ResAddress 地址
	 * @param ResTelephone 电话
	 *
	 * @return
	 */
	private boolean existsInMergeDB(DBCollection coll,String ResName, String ResAddress,
			String ResTelephone) {
		//{"$or":[{"ResName":"xxx", "ResAddress":"xxxx"}, {"ResName":"xxx","ResTelephone":"xxx"},{"ResAddress":"xxx","ResTelephone":"xxx"}]}
		Map<String,Object> map =new HashMap<String,Object>();
		map.put("ResName", ResName);
		map.put("ResAddress", ResAddress);
		map.put("ResTelephone", ResTelephone);
		
		List<String> list = new ArrayList<String>();
		list.add("ResName");
		list.add("ResAddress");
		list.add("ResTelephone");
		Map<String,Object> temp = null;
		List<Map<String,Object>> arr = new ArrayList<Map<String,Object>>();
		for(int i=0;i<list.size();i++){
			temp = new HashMap<String,Object>(map);
			temp.remove(list.get(i));
			arr.add(temp);
		}
		Map<String,Object> mapParam =new HashMap<String,Object>();
		mapParam.put("$or", arr);
		DBObject param = new BasicDBObject();
		param.putAll(mapParam);
		return coll.findOne(param) != null;
	}

	/**
	 * 新抓取的版本 和以前的对比 有变更 添加一个变更字段 标识出 改变的字段
	 * @param coll
	 * @param bo
	 * @return
	 */
	protected String hasModifiedToLastVersion(DBCollection coll, BasicDBObject bo) {
		StringBuilder result = new StringBuilder();
		String retVal = null;
		BigDecimal o = (BigDecimal) bo.remove("crawlVersion");
		BasicDBObject last = (BasicDBObject) coll.findOne(bo);
		if(last == null){
			//有变更
			last = (BasicDBObject) coll.findOne(new BasicDBObject("url", last.getString("url")));
			Map<String,Object> map = last.toMap();
			
			for(java.util.Map.Entry<String, Object> entry: map.entrySet()){
				if(!entry.getValue().equals(bo.get(entry.getKey()))){
					result.append(entry.getKey()).append("##");
				}
			}
			if(result.length()>0){
				retVal = result.substring(0, result.length()-2);
			}
			
		}
		bo.append("crawlVersion", o);
		return retVal;
	}

	public String getDataSrc() {
		return defDataSrc;
	}

}
