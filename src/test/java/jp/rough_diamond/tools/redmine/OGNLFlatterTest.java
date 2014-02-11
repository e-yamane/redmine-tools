package jp.rough_diamond.tools.redmine;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class OGNLFlatterTest {

	@Test
	public void ヘッダーなしの状態でオブジェクトのValueが取得できる事() throws Exception {
		Hoge hoge = new Hoge();
		hoge.foo = "abc";
		hoge.bar = "xyz";
		OGNLFlatter<Hoge> flatter = new OGNLFlatter<>("{foo, bar}");
		Object[] ret = flatter.apply(hoge);
		assertThat(ret.length, is(2));
		assertThat((String)ret[0], is("abc"));
		assertThat((String)ret[1], is("xyz"));
	}

	@Test
	public void ヘッダーありの状態でオブジェクトのValueが取得できる事() throws Exception {
		Hoge hoge = new Hoge();
		hoge.foo = "abc";
		hoge.bar = "xyz";
		OGNLFlatter<Hoge> flatter = new OGNLFlatter<>("#{'ヘッダー1':foo, 'ヘッダー2':bar}");
		Object[] ret = flatter.apply(hoge);
		assertThat(ret.length, is(2));
		assertThat((String)ret[0], is("abc"));
		assertThat((String)ret[1], is("xyz"));
	}

	@Test
	public void ヘッダーの取得ができること() throws Exception {
		OGNLFlatter<Hoge> flatter = new OGNLFlatter<>("{foo, bar}");
		String[] header = flatter.getHeader();
		assertThat(header.length, is(0));
	}
	
	@Test
	public void Mapじゃない場合はヘッダーは空配列であること() throws Exception {
		OGNLFlatter<Hoge> flatter = new OGNLFlatter<>("#{'ヘッダー1':foo, 'ヘッダー2':bar}");
		String[] header = flatter.getHeader();
		assertThat(header.length, is(2));
		assertThat((String)header[0], is("ヘッダー1"));
		assertThat((String)header[1], is("ヘッダー2"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void 組み込み関数_reverseのテスト() throws Exception {
		Hoge hoge = new Hoge();
		hoge.foo = "abcd";
		hoge.bar = "xyz";
		hoge.list = new ArrayList<>(Arrays.asList("abc", "def"));
		hoge.array = new String[]{"123", "456"};
		OGNLFlatter<Hoge> flatter = new OGNLFlatter<>("{#util.reverse(list), #util.reverse(array)}");
		Object[] ret = flatter.apply(hoge);
		assertThat(ret.length, is(2));
		List<String> list = (List<String>)ret[0];
		String[] array = (String[])ret[1];
		assertThat(list.get(0), is("def"));
		assertThat(list.get(1), is("abc"));
		assertThat(array[0], is("456"));
		assertThat(array[1], is("123"));
	}

	public static class Hoge {
		private String foo;
		private String bar;
		private List<String> list;
		private String[] array;
		public String getFoo() {
			return foo;
		}
		public String getBar() {
			return bar;
		}
		
		public List<String> getList() {
			return list;
		}
		
		public String[] getArray() {
			return array;
		}
	}
}
