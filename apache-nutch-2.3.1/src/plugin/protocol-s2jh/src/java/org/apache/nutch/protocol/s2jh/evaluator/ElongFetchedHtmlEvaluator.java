package org.apache.nutch.protocol.s2jh.evaluator;

public class ElongFetchedHtmlEvaluator  extends AbstractFetchedHtmlEvaluator{

	
	
	@Override
	public boolean isParseDataFetchLoadedInternal(String url, String html) {
		 //ht_pri_num 判断套餐价格  	hmap_tra_list 
	   	if(html.indexOf("ht_pri_num") > 0 && html.indexOf("hmap_table_spo")>0 ){	
	    		return true;
	    	}else{
	    		return false;
	    	}
	}

	@Override
	protected String getUrlFilterRegex() {
		return "^http://hotel.elong.com/.+/\\d+/?.*$";
	}

}
