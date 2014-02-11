package jp.rough_diamond.tools.redmine;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Test;

public class OGNLSorterTest {

	@Test
	public void conditionはLinkedHashMapで作成されている事() throws Exception {
		OGNLSorter<Object> sorter = new OGNLSorter<Object>("{'id':'asc'}");
		System.out.println(sorter.conditions.getClass().getName());
		assertThat(sorter.conditions.getClass().getName(), is(LinkedHashMap.class.getName()));
	}
	
	@Test
	public void ascの動作確認() throws Exception {
		Hoge hoge1 = new Hoge();
		hoge1.i = 10;
		hoge1.s = "abc";
		Hoge hoge2 = new Hoge();
		hoge2.i = 5;
		hoge2.s = "xyz";
		
		OGNLSorter<Hoge> sorter = new OGNLSorter<Hoge>("{\"i\":'asc'}");
		List<Hoge> list = new ArrayList<>(Arrays.asList(hoge1, hoge2));
		Collections.sort(list, sorter);
		assertThat(list.get(0), is(hoge2));
	}
	
	@Test
	public void descの動作確認() throws Exception {
		Hoge hoge1 = new Hoge();
		hoge1.i = 10;
		hoge1.s = "abc";
		Hoge hoge2 = new Hoge();
		hoge2.i = 5;
		hoge2.s = "xyz";
		
		OGNLSorter<Hoge> sorter = new OGNLSorter<Hoge>("{\"i\":'desc'}");
		List<Hoge> list = new ArrayList<>(Arrays.asList(hoge2, hoge1));
		Collections.sort(list, sorter);
		assertThat(list.get(0), is(hoge1));
	}

	@Test
	public void 複合ソートキーの動作確認() throws Exception {
		Hoge hoge1 = new Hoge();
		hoge1.i = 10;
		hoge1.s = "abc";
		Hoge hoge2 = new Hoge();
		hoge2.i = 10;
		hoge2.s = "xyz";
		
		OGNLSorter<Hoge> sorter = new OGNLSorter<Hoge>("{\"i\":'desc', \"s\":'asc'}");
		List<Hoge> list = new ArrayList<>(Arrays.asList(hoge2, hoge1));
		Collections.sort(list, sorter);
		assertThat(list.get(0), is(hoge1));
	}

	public static class Hoge {
		int i;
		String s;
		public int getI() {
			return i;
		}
		public String getS() {
			return s;
		}
		
	}
}
