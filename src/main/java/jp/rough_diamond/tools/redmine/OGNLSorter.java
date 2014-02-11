package jp.rough_diamond.tools.redmine;

import java.util.Comparator;
import java.util.Map;

import ognl.Ognl;
import ognl.OgnlException;

//XXX この子も別パッケージに移動するかも。。。
public class OGNLSorter<T> implements Comparator<T> {
	private final String ognl;
	final Map<Object, String> conditions;
	
	@SuppressWarnings("unchecked")
	public OGNLSorter(String ognl) throws OgnlException {
		this.ognl = ognl.startsWith("#") ? ognl : "#" + ognl;
		conditions = (Map<Object, String>) Ognl.getValue(Ognl.parseExpression(this.ognl), null);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public int compare(T i1, T i2) {
//		System.out.println(i1.getClass().getName());
//		System.out.println(i2.getClass().getName());
		try {
			for(Map.Entry<Object, String> entry : conditions.entrySet()) {
//				System.out.println(entry.getKey());
//				System.out.println(entry.getKey().getClass());
				Comparable<Object> c1 = (Comparable<Object>)Ognl.getValue(entry.getKey().toString(), i1);
				Comparable<Object> c2 = (Comparable<Object>)Ognl.getValue(entry.getKey().toString(), i2);
				int comp = c1.compareTo(c2) * orders.valueOf(entry.getValue().toLowerCase()).bias;
				if(comp != 0) {
					return comp;
				}
			}
			return 0;
		} catch (OgnlException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static enum orders {
		asc(1),
		desc(-1);
		
		public final int bias;
		private orders(int bias) {
			this.bias = bias;
		}
	}
}
