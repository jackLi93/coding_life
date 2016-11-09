##ElasticSerach批量查询

![ElasticSerach](http://openskill.cn/uploads/article/20161015/0f32aeff3ccf8e5dc3d196ee8fd45d56.png)
有时候，我们不仅需要存储和查询数据，还需要对大量数据进行批量查询，这样就能节省网络请求所需的开销，降低查询时间提高查询效率。

Elasticsearch 允许通过一次查询请求执行多个文档的检索，这样**可以避免多次请求造成的单独网络开销，相对于一个一个的检索，这样的效率更高。**

在我的实践中，将单个查询改为批量查询后，1650条查询的数据优化结果直接由3分多钟到不足一秒钟

**Es中的两种批量操作API**

-  检索多个文档 -- mget API
-  批量增删改查 -- bulk API

在我的实践中，一开始采用的是bulk API进行批量查询，但由于无法解析查询的结果，后改为mget ApI 的批量查询，最终实现了由单条数据查询到批量查询的优化，大大缩短了查询的时间。

-  bulk java 样例代码如下：
<pre>
<code>
BulkRequestBuilder bulkRequest = client.prepareBulk();
// either use client#prepare, or use Requests# to directly build index/delete requests
bulkRequest.add(client.prepareIndex("twitter", "tweet", "1")
.setSource(jsonBuilder()
.startObject()
.field("user", "kimchy")
.field("postDate", new Date())
.field("message", "trying out Elasticsearch")
.endObject()
)
);
bulkRequest.add(client.prepareIndex("twitter", "tweet", "2")
.setSource(jsonBuilder()
.startObject()
.field("user", "kimchy")
.field("postDate", new Date())
.field("message", "another post")
.endObject()
)
);
BulkResponse bulkResponse = bulkRequest.execute().actionGet();
if (bulkResponse.hasFailures()) {
// process failures by iterating through each bulk response item
}
</code>
</pre>

-  mget API java 样例代码如下：
<pre>
<code>
	
MultiGetResponse multiGetItemResponses = client.prepareMultiGet()
    .add("twitter", "tweet", "1")           
    .add("twitter", "tweet", "2", "3", "4") 
    .add("another", "type", "foo")          
    .get();

for (MultiGetItemResponse itemResponse : multiGetItemResponses) { 
    GetResponse response = itemResponse.getResponse();
    if (response.isExists()) {                      
        String json = response.getSourceAsString(); 
    }
}
</code>
</pre>

--- 

**最佳实践**

-  使用mget API实现批量查询的代码如下，经过验证

	<pre>
<code>
    public  static  List<UserRole>  multiGet(List<String> ids){
        TransportClient client = getTransportClient();
        MultiGetRequestBuilder  multiGetRequestBuilder =client.prepareMultiGet();
        List<UserRole> userRole_list = new ArrayList<>();
        int size = ids.size();
        for(int i=0;i<size;i++){
            multiGetRequestBuilder= multiGetRequestBuilder.add("com","jack",ids.get(i));
        }
        MultiGetResponse multiGetItemResponses =  multiGetRequestBuilder.get();
        for (MultiGetItemResponse itemResponse : multiGetItemResponses) {
            GetResponse response = itemResponse.getResponse();
            if (response.isExists()) {
                String json = response.getSourceAsString();
                log.info(json);
                userRole_list.add(parseJason(json));
            }
        }
        return userRole_list;
	}

</code>
</pre>
  

----

**参考文档**

-  [ElasticSearch入门-Bulk,Search操作](http://donlianli.iteye.com/blog/1902840)
-  《es guide bulk API》
-  [Multi Get API](https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/java-docs-multi-get.html#java-docs-multi-get)
-  [Elasticsearch 批量增删改查](http://techlog.cn/article/list/10182845#b)

---

**关注我学习更多技术哦**

![学习更多技术](http://upload-images.jianshu.io/upload_images/1307647-2e61e10298750352.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)