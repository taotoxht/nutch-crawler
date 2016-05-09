package org.apache.nutch.parse.s2jh;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.nutch.storage.WebPage;

import ch.epfl.lamp.fjbg.JConstantPool.Entry;

import com.ibm.icu.math.BigDecimal;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
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
		try {
			MongoClient mongoClient = new MongoClient(conf.get("mongodb.host"),
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
			coll.update(new BasicDBObject("url", url), bo, true, false);
			
			updateMergeTable(db,bo);
			
			mongoClient.close();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
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
			
		}
		
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
