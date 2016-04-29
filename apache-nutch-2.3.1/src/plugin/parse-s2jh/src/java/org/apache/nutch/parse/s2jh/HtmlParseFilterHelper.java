package org.apache.nutch.parse.s2jh;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 抓取评估器 工具类 读取该包内全部 AbstractHtmlParseFilter 的子类  (为了便于 控制 除了子类以外 还得加上一个注解限制)  得到评估器列表
 * @author root
 *
 */
public class HtmlParseFilterHelper {

	private static AbstractHtmlParseFilter[] parseFilters;
	
	/*public static AbstractHtmlParseFilter[] getParseFilters(String packageName) throws InstantiationException, IllegalAccessException{
		if(parseFilters == null){
			synchronized (HtmlParseFilterHelper.class) {
				if(parseFilters == null){
					Set<Class<?>> set= ClassUtil.scanPackage(packageName, new ClassFilter() {
						@Override
						public boolean accept(Class<?> clazz) {
							HtmlParseFilterOn h = clazz.getAnnotation(HtmlParseFilterOn.class);
							if( h== null || h.on()==false){
								return false;
							}
							if(clazz == AbstractHtmlParseFilter.class){
								return false;
							}
							return AbstractHtmlParseFilter.class.isAssignableFrom(clazz);
						}
					});
					List<AbstractHtmlParseFilter> list = new ArrayList<AbstractHtmlParseFilter>(set.size());
					for(Class<?> clz:set){
						list.add((AbstractHtmlParseFilter)clz.newInstance());
					}
					parseFilters=new AbstractHtmlParseFilter[list.size()];
					list.toArray(parseFilters);
				}
			}
		}
		return parseFilters;
	}*/
	
	public static AbstractHtmlParseFilter[] getParseFilters(String packageName) throws InstantiationException, IllegalAccessException{
		if(parseFilters == null){
			synchronized (HtmlParseFilterHelper.class) {
				if(parseFilters == null){
					parseFilters = new AbstractHtmlParseFilter[1];
					parseFilters[0]	= new ElongHotelHtmlParseFilter();
				}
			}
		}
		return parseFilters;
	}
	
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(value=TYPE)
	public static @interface  HtmlParseFilterOn{
		boolean on() default true;
	}
	
}
