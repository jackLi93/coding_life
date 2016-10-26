package cn.com.axin.elasticsearch.qwzn.share;
import java.net.UnknownHostException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import cn.com.axin.elasticsearch.util.ConnectionUtil;
import cn.com.axin.elasticsearch.util.Log;

/**
 * @Title
 *
 * @author 
 * @date 2016-8-11
 * es测试类完整Java代码
 */
public class Search {

    public static void main(String[] args) throws Exception {
//        searchAll();
//        execQuery(termSearch());
//        execQuery(termsSearch());
//        execQuery(rangeSearch());
//        execQuery(existsSearch());
//        execQuery(matchSearch());
        execQuery(boolSearch());
//        highlightedSearch();
//        scorll();
//        
    }

    /**
     * @return
     */
    private static QueryBuilder boolSearch() {
        // age > 30 or last_name is Smith
        QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .should(QueryBuilders.rangeQuery("age").gt("30"))
                .should(QueryBuilders.matchQuery("last_name", "Smith"));

        // 挺高查询权重
//        QueryBuilders.matchQuery("title", "Dog").boost(3);
//        QueryBuilders.boolQuery().must(null);
//        QueryBuilders.boolQuery().mustNot(null);

        return queryBuilder;
    }
    private static void scorll() {

        Client client = null;

        try {
            client = ConnectionUtil.getLocalClient(); // 获取Client连接对象
            SearchRequestBuilder requestBuilder = client.prepareSearch("my_index").setTypes("my_type")
//                    .setQuery(QueryBuilders.termQuery("age", "20"))
                    .setScroll(new TimeValue(20000))    // 设置scroll有效时间
                    .setSize(2);
            System.out.println(requestBuilder);

            SearchResponse scrollResp = requestBuilder.get();
            System.out.println("totalHits:" + scrollResp.getHits().getTotalHits());

            while (true) {

                String scrollId = scrollResp.getScrollId();
                System.out.println("scrollId:" + scrollId);
                SearchHits searchHits = scrollResp.getHits();

                for (SearchHit hit : searchHits.getHits()) {
                    System.out.println(hit.getId() + "~" + hit.getSourceAsString());
                }
                System.out.println("=================");

                // 3. 通过scrollId获取后续数据
                scrollResp = client.prepareSearchScroll(scrollId)
                        .setScroll(new TimeValue(20000)).execute().actionGet();
                if (scrollResp.getHits().getHits().length == 0) {
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != client) {
                client.close();
            }
        }
    }

    /**
     * @return
     */
    private static void highlightedSearch() {
        QueryBuilder builder = QueryBuilders.termsQuery("age", "18");

        Client client = null;
        try {
            client = ConnectionUtil.getLocalClient();
            SearchRequestBuilder requestBuilder = 
                    client.prepareSearch("my_index").setTypes("my_type")
                    .setFrom(0).setSize(10)
                    .addHighlightedField("age");
//                    .addSort("age", SortOrder.DESC);
            Log.debug(requestBuilder);

            SearchResponse response = requestBuilder.get();
            Log.debug(response);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } finally {
            if (null != client) {
                client.close();
            }
        }
    }
    /**
     * @return
     */
    private static QueryBuilder matchSearch() {

        QueryBuilder builder = QueryBuilders.matchAllQuery();

        builder = QueryBuilders.matchQuery("title", "Jumps");

        /*
         type: boolean  分析后进行查询
         phrase: 确切的匹配若干个单词或短语,
         phrase_prefix: The match_phrase_prefix is the same as match_phrase, 
             except that it allows for prefix matches on the last term in the text
         */
        builder = QueryBuilders.matchQuery("title", "BROWN DOG!").operator(MatchQueryBuilder.Operator.OR).type(MatchQueryBuilder.Type.BOOLEAN);
        builder = QueryBuilders.multiMatchQuery("title", "dog", "jump");

        return builder;
    }
    /**
     * @return
     */
    private static QueryBuilder existsSearch() {

        // exits
        QueryBuilder builder = QueryBuilders.existsQuery("title");

        // missing
        builder = QueryBuilders.missingQuery("title");
        // instead of missing
        builder = QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("title"));

        return builder;
    }
    /**
     * 
     */
    private static QueryBuilder rangeSearch() {

        // age >= 18 && age < 20
        return QueryBuilders.rangeQuery("age").gte(18).lt(20);
    }

    private static QueryBuilder termSearch(){
        QueryBuilder builder = QueryBuilders.termsQuery("title", "brown");
        return builder;
    }
    private static QueryBuilder termsSearch(){
        QueryBuilder builder = QueryBuilders.termsQuery("title", "dog", "jumps");
        // 与termsQuery等效
        builder = QueryBuilders.boolQuery().should(QueryBuilders.termQuery("title", "dog")).should(QueryBuilders.termQuery("title", "jumps"));
        return builder;
    }

    private static void searchAll() {

        Client client = null;
        try {
            client = ConnectionUtil.getLocalClient();
            SearchRequestBuilder requestBuilder = 
                    client.prepareSearch("my_index").setTypes("my_type")
                    .setFrom(0).setSize(10)
                    .addSort("age", SortOrder.DESC);
            Log.debug(requestBuilder);

            SearchResponse response = requestBuilder.get();
            Log.debug(response);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } finally {
            if (null != client) {
                client.close();
            }
        }
    }

    /**
     * @param builder
     * @throws UnknownHostException
     */
    private static void execQuery(QueryBuilder builder)
            throws UnknownHostException {
        Client client = ConnectionUtil.getLocalClient();

        SearchRequestBuilder requestBuilder = 
                client.prepareSearch("my_index").setTypes("my_type")
                .setExplain(true)
                .setQuery(builder);
        Log.debug(requestBuilder);

        SearchResponse response = requestBuilder.get();
        Log.debug(response);
    }

}
获取连接对象的代码

/**
     * 获取本地的连接对象(127.0.0.1:9300)
     * @return
     * @throws UnknownHostException
     */
    public static Client getLocalClient() throws UnknownHostException {
        return getClient("127.0.0.1", 9300, "es-stu");
    }

    /**
     * 获取连接对象
     * @param host 主机IP
     * @param port 端口
     * @param clusterName TODO
     * @return 
     * @throws UnknownHostException
     */
    private static Client getClient(String host, int port, String clusterName) throws UnknownHostException {
        // 参数设置
        Builder builder = Settings.settingsBuilder();
        // 启用嗅探功能 sniff
        builder.put("client.transport.sniff", true);
        // 集群名
        builder.put("cluster.name", clusterName);

        Settings settings = builder.build();

        TransportClient transportClient = TransportClient.builder().settings(settings).build();
        Client client = transportClient.addTransportAddress(
                new InetSocketTransportAddress(InetAddress.getByName(host), port));
        // 连接多个地址
        // transportClient.addTransportAddresses(transportAddress);

        return client;
    }  