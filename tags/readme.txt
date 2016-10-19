//一个查询ES服务器数据并解析的应用
小结：

1. 使用Es查询ES服务器的数据，如果服务器在内网需要使用跳转机
2.使用阿里巴巴的fastJason库将Jason数据转为对象
3.获取Client的时候，使用static    TransportClient client;//es客户端，能使效率更高，而不是每一个请求都new一个对象。

参考文档：

1. http://aoyouzi.iteye.com/blog/2125362：elasticsearch java API ------搜索
2.http://maclab.iteye.com/blog/1570860 ：fastjson 应用 string字符串转换成java对象或者对象数组
3.http://www.bejson.com/json2javapojo/  :JSON字符串转换成Java实体类(POJO) 小工具