package jp.rough_diamond.tools.redmine;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManager.INCLUDE;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueStatus;
import com.taskadapter.redmineapi.bean.Tracker;

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

	/**
	 * チケットIDを指定して対象のチケットを取得する
	 * @param id
	 * @param inclues
	 * @return
	 * @throws RedmineException
	 * @throws IOException
	 */
	public Issue byId(int id, INCLUDE... inclues) throws RedmineException, IOException {
		return manager.getIssueById(id, inclues);
	}

	/**
	 * プロジェクトの全チケットを取得する
	 * @param includes
	 * @return
	 * @throws RedmineException
	 */
	public Iterable<Issue> all(INCLUDE... includes) throws RedmineException {
		return byQueryId(null, includes);
	}

	/**
	 * Redmine上のカスタムクエリーに対応するチケット群を取得する
	 * queryIdを省略した場合（=null）は、全チケットを取得する
	 * @param queryId
	 * @param includes
	 * @return
	 * @throws RedmineException
	 */
	public Iterable<Issue> byQueryId(Integer queryId, final INCLUDE... includes) throws RedmineException {
		return byQueryId(queryId, Predicates.<Issue>alwaysTrue(), includes);
	}
	
	/**
	 * Redmine上のカスタムクエリーに対応しかつローカルでフィルタリングされたチケット群を取得する
	 * Redmine上ではor条件で情報が取得できないので必要であればこちらのAPIを使用してください。
	 * @param queryId
	 * @param localFilter
	 * @param includes
	 * @return
	 * @throws RedmineException
	 */
	public Iterable<Issue> byQueryId(Integer queryId, Predicate<Issue> localFilter, final INCLUDE... includes) throws RedmineException {
		Map<String, String> param = new HashMap<>();
		param.put("project_id", projectKey);
		if(queryId == null) {
			param.put("status_id", "*");
		} else {
			param.put("query_id", queryId.toString());
		}
		return byParam(param, localFilter, includes);
	}

	/**
	 * RedmineのREST APIで許容しているパラメタに合致するチケット群を取得する
	 * @param param
	 * @param includes
	 * @return
	 * @throws RedmineException
	 */
	public Iterable<Issue> byParam(Map<String, String> param, final INCLUDE... includes) throws RedmineException {
		return byParam(param, Predicates.<Issue>alwaysTrue(), includes);
	}
	
	/**
	 * RedmineのREST APIで許容しているパラメタに合致するチケット群を取得する
	 * @param param
	 * @param includes
	 * @return
	 * @throws RedmineException
	 */
	public Iterable<Issue> byParam(Map<String, String> param, Predicate<Issue> localFilter, final INCLUDE... includes) throws RedmineException {
		//XXX paramにincludesを渡すべきなんだけど、今のところ渡しても意味ないみたいだし。。。
		List<Issue> list = manager.getIssues(param);
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
	
	/**
	 * チケットを登録する
	 * @param issue
	 * @return
	 * @throws RedmineException
	 */
	public Issue createIssue(Issue issue) throws RedmineException {
		return manager.createIssue(projectKey, issue);
	}

	/**
	 * チケットを更新する
	 * @param issue
	 * @throws RedmineException
	 */
	public void updateIssue(Issue issue) throws RedmineException {
		manager.update(issue);
	}

	/**
	 * RedmineManagerを返却する
	 * 全ての機能をWrapするつもりはないのでRedmineアクセス用のオブジェクトを返却する
	 * @return
	 */
	public RedmineManager getManager() {
		return manager;
	}
	
	/**
	 * 名前に該当するトラッカーを返却する
	 * @param name
	 * @return
	 * @throws RedmineException 
	 */
	public Tracker getTrackerByName(String name) throws RedmineException {
		return getTrackerMap().get(name);
	}
	
	private Map<String, Tracker> trackerMap;
	Map<String, Tracker> getTrackerMap() throws RedmineException {
		if(trackerMap == null) {
			makeTrackerMap();
		}
		return trackerMap;
	}

	synchronized private void makeTrackerMap() throws RedmineException {
		if(trackerMap != null) {
			return;
		}
		List<Tracker> trackers = manager.getTrackers();
		Map<String, Tracker> map = new HashMap<>();
		for(Tracker tracker : trackers) {
			map.put(tracker.getName(), tracker);
		}
		trackerMap = Collections.unmodifiableMap(map);
	}

	/**
	 * 名前に該当するステータスを返却する
	 * @param name
	 * @return
	 * @throws RedmineException 
	 */
	public IssueStatus getStatusByName(String name) throws RedmineException {
		return getStatusMap().get(name);
	}

	private Map<String, IssueStatus> statusMap;
	Map<String, IssueStatus> getStatusMap() throws RedmineException {
		if(statusMap == null) {
			makeStatusMap();
		}
		return statusMap;
	}

	synchronized private void makeStatusMap() throws RedmineException {
		if(statusMap != null) {
			return;
		}
		List<IssueStatus> statuses = manager.getStatuses();
		Map<String, IssueStatus> map = new HashMap<>();
		for(IssueStatus status : statuses) {
			map.put(status.getName(), status);
		}
		statusMap = Collections.unmodifiableMap(map);
	}
}
