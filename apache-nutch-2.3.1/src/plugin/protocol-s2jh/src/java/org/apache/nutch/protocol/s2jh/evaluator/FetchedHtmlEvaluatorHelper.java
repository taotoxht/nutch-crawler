package org.apache.nutch.protocol.s2jh.evaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.nutch.protocol.s2jh.evaluator.ClassUtil.ClassFilter;

/**
 * 抓取评估器 工具类 读取该包内全部 AbstractFetchedHtmlEvaluator 的子类  得到评估器列表
 * @author root
 *
 */
public class FetchedHtmlEvaluatorHelper {

	private static AbstractFetchedHtmlEvaluator[] fetchedHtmlEvaluators;
	
	/*public static AbstractFetchedHtmlEvaluator[] getFetchedHtmlEvaluators(String packName) throws InstantiationException, IllegalAccessException{
		if(fetchedHtmlEvaluators == null){
			synchronized (FetchedHtmlEvaluatorHelper.class) {
				if(fetchedHtmlEvaluators == null){
					Set<Class<?>> set= ClassUtil.scanPackage(packName, new ClassFilter() {
						
						@Override
						public boolean accept(Class<?> clazz) {
							if(clazz == AbstractFetchedHtmlEvaluator.class){
								return false;
							}
							return AbstractFetchedHtmlEvaluator.class.isAssignableFrom(clazz);
						}
					});
					List<AbstractFetchedHtmlEvaluator> list = new ArrayList<AbstractFetchedHtmlEvaluator>(set.size());
					for(Class<?> clz:set){
						list.add((AbstractFetchedHtmlEvaluator)clz.newInstance());
					}
					fetchedHtmlEvaluators=new AbstractFetchedHtmlEvaluator[list.size()];
					list.toArray(fetchedHtmlEvaluators);
				}
			}
		}
		return fetchedHtmlEvaluators;
	}*/
	
	public static AbstractFetchedHtmlEvaluator[] getFetchedHtmlEvaluators(String packName) throws InstantiationException, IllegalAccessException{
		if(fetchedHtmlEvaluators == null){
			synchronized (FetchedHtmlEvaluatorHelper.class) {
				if(fetchedHtmlEvaluators == null){
					fetchedHtmlEvaluators = new AbstractFetchedHtmlEvaluator[1];
					fetchedHtmlEvaluators[0] = new ElongFetchedHtmlEvaluator();
				}
			}
		}
		return fetchedHtmlEvaluators;
	}
	
}
