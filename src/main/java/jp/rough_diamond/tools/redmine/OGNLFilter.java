package jp.rough_diamond.tools.redmine;

import ognl.Ognl;
import ognl.OgnlException;

import com.google.common.base.Predicate;

public class OGNLFilter<T> implements Predicate<T> {
	final String ognl;
	public OGNLFilter(String ognl) {
		this.ognl = ognl;
	}
	
	@Override
	public boolean apply(T obj) {
		try {
			Boolean ret = (Boolean) Ognl.getValue(ognl, obj);
			return ret;
		} catch (OgnlException e) {
			throw new RuntimeException(e);
		}
	}

}
