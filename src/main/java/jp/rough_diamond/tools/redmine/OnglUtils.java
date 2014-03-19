package jp.rough_diamond.tools.redmine;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class OnglUtils {
	public Object tmp = "";

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

	public String date(String format) {
		return date(tmp, format);
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