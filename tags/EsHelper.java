package cn.migu.adp.dmp.tags;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.iflytek.gnome.share.GLog;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.get.GetField;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.util.List;

/**
 * Created by jack on 2016/10/18.
 * this class is used to get tags from es....
 */
public class EsHelper {
    private  final  static GLog log = new GLog(EsHelper.class);

    public static  String getTagsFromEs(String id, String type, String index) {
        if (null == id || null == type || null == index)
            return null;
        log.info("index:"+index+"type"+type+"id"+id);
        String tags = queryData(index, type, id);
        //插入redis上传列表中
        //EsearchPutRedis.putRedisMap.put(id, tags);//也可以做一个内存缓存提高速度===>那么查询就变成：先检查内存中是否有标签 有直接获取--》无查询es
        return tags;
    }
    public static String queryData(String index, String type, String id) {
        if (null == id || null == type || null == index)
            return null;
        String tags = null;
        //1.判断输入合法性--->2.获取Client--->3.判断Client合法性--->4.获取key对应的值
        //修改--->直接getTransportClient--->进行查询
        //GetResponse response = EsearchMain.sysMgrModule().
        GetResponse response = getTransportClient().prepareGet()
                .setIndex(index)
                .setType(type)
                .setId(id)
                .setFields("taglist")
                .get();
        log.info("response+"+response.getSourceAsString());
        SearchResponse response2 = getTransportClient().prepareSearch("dmp")
                .setTypes("phone").setQuery(QueryBuilders.idsQuery().addIds(id)).execute().actionGet();
        if (null != response && response.isExists()&& !response.isSourceEmpty() && response.isExists()) {
            GetField getfiled = response.getField("taglist");
            tags = getfiled.getValues().toString();///---->输出tags看是何种形式的数据
            log.debug("输出查询到的标签字符：------->"+tags);
        }
        String test_tags=response2.toString();
        //  String source_tags = response2.getHits().toString();
        SearchHits hits = response2.getHits();
        if(hits.getHits().length==0){
            return null;
        }
        SearchHit hitFields =hits.getAt(0);
        log.info(hitFields.getSourceAsString()+"ooooo");
        String  tagJason=hitFields.getSourceAsString();
        UserRole userRole = JSON.parseObject(tagJason, new TypeReference<UserRole>(){});
        log.info(userRole.getDid()+'\t'+userRole.getIdtype()+'\t'+userRole.getTag());
        for(SearchHit hit :hits){
            String jasonStr = hit.getSourceAsString();
            System.out.println("hhhah:"+jasonStr);
        }
        //log.info(source_tags);
        log.info("===================");
        log.info("test_tags:"+test_tags );
        return  test_tags;
        //return tags;
    }

    public static UserRole getUserRole(String index, String type, String id) {
        if (null == id || null == type || null == index)
            return null;
        String tags = null;
        //1.判断输入合法性--->2.获取Client--->3.判断Client合法性--->4.获取key对应的值
        //修改--->直接getTransportClient--->进行查询
        //GetResponse response = EsearchMain.sysMgrModule().
/*        GetResponse response = getTransportClient().prepareGet()
                .setIndex(index)
                .setType(type)
                .setId(id)
                .setFields("taglist")
                .get();
        log.info("response+"+response.getSourceAsString());*/
        SearchResponse response2 = getTransportClient().prepareSearch("dmp")
                .setTypes("phone").setQuery(QueryBuilders.idsQuery().addIds(id)).execute().actionGet();
/*        if (null != response && response.isExists()&& !response.isSourceEmpty() && response.isExists()) {
            GetField getfiled = response.getField("taglist");
            tags = getfiled.getValues().toString();///---->输出tags看是何种形式的数据
            log.debug("输出查询到的标签字符：------->"+tags);
        }*/
/*        if(response2.isContextEmpty()){
            return  null;
        };*/
        String test_tags=response2.toString();
        //  String source_tags = response2.getHits().toString();
        SearchHits hits = response2.getHits();
        if(hits.getHits().length==0){
            return null;
        }
        SearchHit hitFields =hits.getAt(0);
        log.info(hitFields.getSourceAsString()+"ooooo");
        String  tagJason=hitFields.getSourceAsString();
        UserRole userRole = JSON.parseObject(tagJason, new TypeReference<UserRole>(){});
        log.info(userRole.getDid()+'\t'+userRole.getIdtype()+'\t'+userRole.getTag());
/*
        for(SearchHit hit :hits){
            String jasonStr = hit.getSourceAsString();
            System.out.println("hhhah:"+jasonStr);
        }
*/

        return  userRole;
    }

    /**
     *
     es_ip = 60.166.12.158
     es_port = 9300
     es_cluster = migudmp
     * @return
     */
    static    TransportClient client;//es客户端=====>放在外面西欧阿里很高
    private static TransportClient getTransportClient(){
        if(client!=null)
            return  client;
        String cluster ="jackdmp";
        String esIp="127.0.0.1";
        int esPort=19999;
        //初始化ES查询
        Settings esSetting = ImmutableSettings.settingsBuilder()
                .put("cluster.name", cluster)
                .build(); //--->es集群的名字
        client = new TransportClient(esSetting);
        client.addTransportAddress(new InetSocketTransportAddress(esIp, esPort)); //集群的ip 端口

        return  client;

    }

    public  static  String getUserDesc(UserRole userRole){
        if(userRole==null){
            log.info("没查到该用户。。。");
            return  null;
        }
        StringBuilder sb = new StringBuilder("用户画像描述,该用户拥有如下标签:  ");
        List<MiguTag> usertags = userRole.getTag();
        if(usertags.size()>0){
            for (int i=0;i<usertags.size();i++){
                MiguTag tag = usertags.get(i);
                String tagId = tag.getId();
                if(TagsCache.TagCache.containsKey(tagId)){
                    sb.append(TagsCache.TagCache.get(tagId)+'\t');
                }
            }
        }
        return  sb.toString();
    }

    public static void main(String []args){
        log.info("测试开始。。。。");
        String testTags = getTagsFromEs("18817843978","phone","dmp");

        log.info(testTags);

        String test2Tags = getTagsFromEs("13601999545","phone","dmp");

        String test3Tags = getTagsFromEs("13511068382","phone","dmp");

        String test4Tags = getTagsFromEs("18217074505","phone","dmp");
        UserRole userRole = getUserRole("dmp","phone","13524356198");
        String userDesc = getUserDesc(userRole);
        System.out.print(userDesc);

    }
}
