package jp.rough_diamond.tools.redmine;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

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
		context.put("util", new OnglUtils());
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
}
