package jp.rough_diamond.tools.redmine;

import java.io.IOException;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManager.INCLUDE;
import com.taskadapter.redmineapi.bean.Issue;

/**
 * Redminに登録されているチケットにアクセスするためのリポジトリ
 * @author e-yamane
 */
public class IssueRepository {
//	private final static Logger log = LoggerFactory.getLogger(IssueRepository.class);
	
	final RedmineManager manager;
	final String projectKey;
	
	/**
	 * リポジトリを取得する
	 * @param host			RedminのトップURL
	 * @param projectKey	プロジェクト名
	 * @param accessKey		Redmineへアクセスするアクセスキー
	 * @return
	 */
	public static IssueRepository getRepository(String host, String projectKey, String accessKey) {
		return new IssueRepository(host, projectKey, accessKey);
	}

	IssueRepository(String host, String projectKey, String accessKey) {
		this(new RedmineManager(host, accessKey), projectKey);
	}
	
	//for testing
	IssueRepository(RedmineManager manager, String projectKey) {
		this.manager = manager;
		this.projectKey = projectKey;
	}
	
	public Issue byId(int id, INCLUDE... inclues) throws RedmineException, IOException {
		return manager.getIssueById(id, inclues);
	}
	
	public Iterable<Issue> all(INCLUDE... includes) throws RedmineException {
		return byQueryId(null, includes);
	}
	
	public Iterable<Issue> byQueryId(Integer queryId, final INCLUDE... includes) throws RedmineException {
		return byQueryId(queryId, Predicates.<Issue>alwaysTrue(), includes);
	}
	
	public Iterable<Issue> byQueryId(Integer queryId, Predicate<Issue> localFilter, final INCLUDE... includes) throws RedmineException {
		List<Issue> list = manager.getIssues(projectKey, queryId, includes);
		Iterable<Issue> issues = Iterables.filter(list, localFilter);
		return Iterables.transform(issues, new Function<Issue, Issue>() {
			@Override
			public Issue apply(Issue issue) {
				try {
					if(includes.length == 0) {
						return issue;
					}
					return byId(issue.getId(), includes);
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
	}
}
