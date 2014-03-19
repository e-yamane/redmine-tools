package jp.rough_diamond.tools.redmine;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

import com.google.common.base.Predicate;

//XXX 別パッケージに移動するかも。。。
/**
 * OGNL形式で記述した文字列に応じたFilter
 * @author e-yamane
 *
 * @param <T>
 */
public class OGNLFilter<T> implements Predicate<T> {
	final String ognl;
	final OgnlContext context;
	private final static Map<String, Object> NULL_MAP = Collections.unmodifiableMap(new HashMap<String, Object>());
	
	public OGNLFilter(String ognl) {
		this(ognl, NULL_MAP);
	}
	
	public OGNLFilter(String ognl, Map<String, Object> functions) {
		this.ognl = ognl;
		context = new OgnlContext();
		context.put("util", new OnglUtils());
		for(Entry<String, Object> entry : functions.entrySet()) {
			context.put(entry.getKey(), entry.getValue());
		}
	}
	
	@Override
	public boolean apply(T obj) {
		try {
			Boolean ret = (Boolean) Ognl.getValue(ognl, context, obj);
			return ret;
		} catch (OgnlException e) {
			throw new RuntimeException(e);
		}
	}

}
