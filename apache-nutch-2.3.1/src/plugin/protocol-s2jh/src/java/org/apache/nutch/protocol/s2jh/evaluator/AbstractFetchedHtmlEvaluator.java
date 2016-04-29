package org.apache.nutch.protocol.s2jh.evaluator;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * 抓取数据 是否完整的 评估器
 * @author xuhaitao
 *
 */
public abstract class AbstractFetchedHtmlEvaluator {
	
	private Pattern filterPattern;
	
	public AbstractFetchedHtmlEvaluator() {
		String filterRegex = getUrlFilterRegex();
		if (StringUtils.isNotBlank(filterRegex)) {
			this.filterPattern = Pattern.compile(getUrlFilterRegex());
		}
	}
	/**
	 * 检测url获取页面内容是否已加载完毕，主要用于支持一些AJAX页面延迟等待加载
	 * 返回false则表示告知Fetcher处理程序继续AJAX执行短暂等待后再回调此方法直到返回true标识内容已加载完毕
	 * 
	 * @param fetchUrl
	 * @param html
	 *            页面HTML
	 * @return 默认返回true，子类根据需要定制判断逻辑
	 */
	public boolean isParseDataFetchLoaded(String url, String html) {
		if (filterPattern == null) {
			// 没有url控制规则，直接放行
			return true;
		}
		// 首先判断url是否匹配当前过滤器，如果是则继续调用内容判断逻辑
		if (filterPattern.matcher(url).find()) {
			if (StringUtils.isBlank(html)) {
				return false;
			}
			return isParseDataFetchLoadedInternal(url, html);
		}
		return true;
	}

	public abstract boolean isParseDataFetchLoadedInternal(String url, String html);
	
	/**
	 * 设置当前解析过滤器匹配的URL正则表达式 只有匹配的url才调用当前解析处理逻辑
	 * 
	 * @return
	 */
	protected abstract String getUrlFilterRegex();
}
