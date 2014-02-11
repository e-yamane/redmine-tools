package jp.rough_diamond.tools.redmine;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

//XXX 別パッケージに移動するかも。。。
/**
 * OGNL形式で指定した形式にオブジェクトから情報を抽出するクラス
 * @author e-yamane
 *
 * @param <F>
 */
public class OGNLFlatter<F> implements Function<F, Object[]> {
	final String ognl;
	final OgnlContext context;
	public OGNLFlatter(String ognl) throws OgnlException {
		Ognl.parseExpression(ognl);	//Fail Fast
		this.ognl = ognl;
		context = new OgnlContext();
		context.put("util", new FlatterUtils());
	}
	
	@SuppressWarnings({ "unchecked", "serial" })
	public String[] getHeader() {
		try {
			//ヘッダー値が文字列であるという前提でvalueはまぁMapにしとくきゃぬるぽでないからこーしてみる
			Object ret = Ognl.getValue(ognl, context, new HashMap<Object, Object>(){
				@Override
				public Object get(Object key) {
					return new HashMap<>();
				}
			});
			if(ret instanceof Map) {
				Set<String> keys = ((Map<String, Object>)ret).keySet(); 
				return keys.toArray(new String[keys.size()]);
			} else {
				return new String[0];
			}
		} catch (OgnlException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings({ "unchecked"})
	@Override
	public Object[] apply(F obj) {
		try {
			Object ret = Ognl.getValue(ognl, context, obj);
			if(ret instanceof List) {
				return toArray((List<Object>)ret);
			} else if(ret instanceof Map) {
				return toArray((Map<String, Object>)ret);
			} else {
				return new Object[]{ret};
			}
		} catch (OgnlException e) {
			throw new RuntimeException(e);
		}
	}

	private Object[] toArray(Map<String, Object> map) {
		return toArray(map.values());
	}

	private Object[] toArray(Collection<Object> collection) {
		return Iterables.toArray(collection, Object.class);
	}
	
	public static class FlatterUtils {
		@SuppressWarnings("unchecked")
		public Object reverse(Object obj) {
			if(obj == null) return null;
			if(obj.getClass().isArray()) return reverseArray((Object[]) obj);
			if(obj instanceof List) return reverseList((List<Object>)obj);
			return obj;
		}
		
		<T> List<T> reverseList(List<T> list) {
			if(list == null) return null;
			return Lists.reverse(list);
		}
		
		@SuppressWarnings("unchecked")
		<T> T[] reverseArray(T[] array) {
			if(array == null) return null;
			 List<T> list = Lists.reverse(Lists.newArrayList(array));
			return Iterables.toArray(list, (Class<T>)array.getClass().getComponentType());
		}

		public String date(Object d, String format) {
			if(d == null) return "";
			if(!(d instanceof Date)) return d.toString();
			DateFormat df = new SimpleDateFormat(format);
			return df.format(d);
		}
		
		@SuppressWarnings("rawtypes")
		public String join(Object obj, String separator) {
			if(!(obj instanceof List)) return obj.toString();
			List list = (List)obj;
			return Joiner.on(separator).join(list);
		}
	}
}
