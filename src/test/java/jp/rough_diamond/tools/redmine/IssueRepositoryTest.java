package jp.rough_diamond.tools.redmine;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManager.INCLUDE;
import com.taskadapter.redmineapi.bean.Issue;

public class IssueRepositoryTest {
	RedmineManager manager;
	IssueRepository repository;
	Issue byIdResponse;
	Issue issues1;
	Issue issues2;
	Issue issues3;
	
	@SuppressWarnings("rawtypes")
	@Before
	public void before() throws Exception {
		byIdResponse = makeTestingIssue(1);
		issues1 = makeTestingIssue(2);
		issues2 = makeTestingIssue(3);
		issues3 = makeTestingIssue(4);

		manager = mock(RedmineManager.class, new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				if(invocation.getMethod().getName().equals("getIssueById")) {
					return byIdResponse;
				} else if(invocation.getMethod().getName().equals("getIssues")) {
					return new ArrayList<>(Arrays.asList(issues1, issues2, issues3));
				}
				return RETURNS_DEFAULTS.answer(invocation);
			}
		});
		
		repository = new IssueRepository(manager, "testing");
	}

	private Issue makeTestingIssue(Integer id) {
		Issue ret = new Issue();
		ret.setId(id);
		return ret;
	}
	
	@Test
	public void byIdの呼び出しで引数が正しく渡されている事() throws Exception  {
		Issue issue = repository.byId(10, INCLUDE.attachments, INCLUDE.changesets);
		verify(manager).getIssueById(10, INCLUDE.attachments, INCLUDE.changesets);
		assertThat(issue.getId(), is(1));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void allの呼び出しで引数が正しく渡されている事() throws Exception {
		repository.all(INCLUDE.journals, INCLUDE.relations);
		ArgumentCaptor<Map> ac = ArgumentCaptor.forClass(Map.class);
		verify(manager).getIssues(ac.capture());
		assertThat(ac.getAllValues().size(), is(1));
		assertThat(ac.getValue().size(), is(2));
		assertThat(ac.getValue().get("project_id").toString(), is("testing"));
		assertThat(ac.getValue().get("status_id").toString(), is("*"));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void byQueryIdの呼び出しで引数が正しく渡されている事() throws Exception {
		repository.byQueryId(10, INCLUDE.attachments, INCLUDE.watchers);
		ArgumentCaptor<Map> ac = ArgumentCaptor.forClass(Map.class);
		verify(manager).getIssues(ac.capture());
		assertThat(ac.getAllValues().size(), is(1));
		assertThat(ac.getValue().size(), is(2));
		assertThat(ac.getValue().get("project_id").toString(), is("testing"));
		assertThat(ac.getValue().get("query_id").toString(), is("10"));
	}
	
	@Test
	public void byQueryIdの呼び出しの中からByIdの呼び出しが行われている事() throws Exception {
		Iterable<Issue> issues = repository.byQueryId(10, INCLUDE.attachments, INCLUDE.watchers);
		Iterables.all(issues, Predicates.<Issue>alwaysTrue());
		verify(manager).getIssueById(2, INCLUDE.attachments, INCLUDE.watchers);
		verify(manager).getIssueById(3, INCLUDE.attachments, INCLUDE.watchers);
		verify(manager).getIssueById(4, INCLUDE.attachments, INCLUDE.watchers);
	}
	
	@Test
	public void byQueryの呼び出しで返却されるオブジェクトは全てbyIdから返却された物である事() throws Exception {
		Iterable<Issue> issues = repository.byQueryId(10, INCLUDE.attachments, INCLUDE.watchers);
		for(Issue issue : issues) {
			assertThat(issue, is(byIdResponse));
		}
	}
	
	@Test
	public void byQuery内からのbyIdの呼び出しはlocalFilterにマッチした物だけである事() throws Exception {
		Predicate<Issue> filter = new Predicate<Issue>() {
			@Override
			public boolean apply(Issue issue) {
				return issue == issues1;
			}
		};
		Iterable<Issue> issues = repository.byQueryId(10, filter, INCLUDE.attachments, INCLUDE.watchers);
		Iterables.all(issues, Predicates.<Issue>alwaysTrue());
		verify(manager).getIssueById(2, INCLUDE.attachments, INCLUDE.watchers);
		verify(manager, never()).getIssueById(3, INCLUDE.attachments, INCLUDE.watchers);
		verify(manager, never()).getIssueById(4, INCLUDE.attachments, INCLUDE.watchers);
	}
}
