package com.xuecheng.search;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SearchTest {

    //高级客户端
    @Autowired
    RestHighLevelClient client;

    @Autowired
    RestClient restClient;

    //删除索引
    @Test
    public void delteTest() throws IOException {
        //创建索引删除对象
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("xc_course");
        //删除索引
        AcknowledgedResponse delete1 = client.indices().delete(deleteIndexRequest);
        boolean acknowledged = delete1.isAcknowledged();
        System.out.println(acknowledged);

    }
    //创建索引
    @Test
    public void  createIndex() throws IOException {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest("xc_course");
        //设置索引参数
        createIndexRequest.settings(Settings.builder().put("number_of_shards",1)
                        .put("number_of_replicas",0));
        createIndexRequest.mapping("doc","{\n" +
                "\"properties\": {\n" +
                "\"name\": {\n" +
                "\"type\": \"text\",\n" +
                "\"analyzer\":\"ik_max_word\",\n" +
                "\"search_analyzer\":\"ik_smart\"\n" +
                "},\n" +
                "\"description\": {\n" +
                "\"type\": \"text\",\n" +
                "\"analyzer\":\"ik_max_word\",\n" +
                "\"search_analyzer\":\"ik_smart\"\n" +
                "},\n" +
                "\"studymodel\": {\n" +
                "\"type\": \"keyword\"\n" +
                "},\n" +
                "\"price\": {\n" +
                "\"type\": \"float\"\n" +
                "},\n" +
                "\"timestamp\": {\n" +
                "\"type\": \"date\",\n" +
                "\"format\": \"yyyy‐MM‐dd HH:mm:ss||yyyy‐MM‐dd||epoch_millis\"\n" +
                "}\n" +
                "}\n" +
                "}",XContentType.JSON);
        CreateIndexResponse createIndexResponse = client.indices().create(createIndexRequest);
        boolean acknowledged = createIndexResponse.isAcknowledged();
        System.out.println(acknowledged);
    }

    @Test
    public void  testAddDoc() throws IOException {
        //准备数据
        //准备json数据
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("name", "spring cloud实战");
        jsonMap.put("description", "本课程主要从四个章节进行讲解： 1.微服务架构入门 2.spring cloud基础入门 3.实战Spring Boot 4.注册中心eureka。");
        jsonMap.put("studymodel", "201001");
        SimpleDateFormat dateFormat =new SimpleDateFormat("yyyy‐MM‐dd HH:mm:ss");
        jsonMap.put("timestamp", dateFormat.format(new Date()));
        jsonMap.put("price", 5.6f);
        //索引请求对象
        IndexRequest indexRequest = new IndexRequest("xc_course","doc");
        //指定索引文档内容
        indexRequest.source(jsonMap);
        //索引响应对象
        IndexResponse index = client.index(indexRequest);
        DocWriteResponse.Result result = index.getResult();
        System.out.println(result);
    }
    //Api查询
    @Test
    public void getDoc() throws IOException {
        GetRequest getRequest = new GetRequest(
                "xc_course",
                "doc",
                "D5AUMmkB-_Ys0uccGZi5");

        GetResponse documentFields = client.get(getRequest);
        boolean exists = documentFields.isExists();
        Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();
        System.out.println(sourceAsMap);
    }
    //更新文档
    @Test
    public void updateDoc() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("xc_course", "doc",
                "D5AUMmkB-_Ys0uccGZi5");
        Map<String, String> map = new HashMap<>();
        map.put("name", "spring cloud实战");
        updateRequest.doc(map);
        UpdateResponse update = client.update(updateRequest);
        RestStatus status = update.status();
        System.out.println(status);
    }
    //根据id删除文档
    @Test
    public void testDelDoc() throws IOException {
//删除文档id
        String id = "eqP_amQBKsGOdwJ4fHiC";
//删除索引请求对象
        DeleteRequest deleteRequest = new DeleteRequest("xc_course","doc",id);
//响应对象
        DeleteResponse deleteResponse = client.delete(deleteRequest);
//获取响应结果
        DocWriteResponse.Result result = deleteResponse.getResult();
        System.out.println(result);
    }
    //搜索type下的全部记录
    @Test
    public void testSearchAll() throws IOException {
        //设置搜索请求索引
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query();
        //source源字段过滤
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel"}, new String[]{});
        searchRequest.source(searchSourceBuilder);
        SearchResponse search = client.search(searchRequest);
        SearchHits hits = search.getHits();
        SearchHit[] sourceHits = hits.getHits();
        for (SearchHit hit : sourceHits) {
            String index = hit.getIndex();
            String type = hit.getType();
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);
        }
    }

    //分页查询
    @Test
    public void testPageLimmite() throws IOException {
        //设置搜索请求索引
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        //设置分页下标 从0开始
        searchSourceBuilder.from(0);
        //每页显示个数
        searchSourceBuilder.size(10);
        //source源字段过虑
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel"}, new String[]{});
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHit[] hits = searchResponse.getHits().getHits();
        System.out.println(hits.length);

    }
    //Term Query
    @Test
    public void testTermQuery() throws IOException {
        //设置搜索请求索引
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        /*
        *   根据id多个精确匹配
        *   String[] split = new String[]{"1","2"};
            List<String> idList = Arrays.asList(split);
            searchSourceBuilder.query(QueryBuilders.termsQuery("_id", idList));
        * */
        searchSourceBuilder.query(QueryBuilders.termQuery("name","spring"));
        //source源字段过虑
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel"}, new String[]{});
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHit[] hits = searchResponse.getHits().getHits();
        System.out.println(hits.length);
    }

    //match Query
    @Test
    public void testMatchQuery() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //source源字段过虑
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel"}, new String[]{});
        //匹配关键字
        //还可以占比匹配  .minimumShouldMatch("80%");//设置匹配占比

        searchSourceBuilder.query(QueryBuilders.matchQuery("description", "spring开").operator(Operator.OR));
         searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String index = hit.getIndex();
            String type = hit.getType();
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);
        }

    }
    //multi Query 多项匹配去 多项field中匹配
    @Test
    public void testMultiQuery() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //source源字段过虑
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel"}, new String[]{});

        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("spring框架","name","description")
                .minimumShouldMatch("50%");
        multiMatchQueryBuilder.field("name",10);//提升boost


        searchSourceBuilder.query(multiMatchQueryBuilder);
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String index = hit.getIndex();
            String type = hit.getType();
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);
        }
    }

    //BoolQuery
    @Test
    public void testBoolQuery() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //source源字段过虑
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel"}, new String[]{});
        //multiQuery
        String keyword = "spring开发框架";
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("spring框架",
                "name", "description")
                .minimumShouldMatch("50%");
        multiMatchQueryBuilder.field("name",10);
        //TermQuery
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("studymodel", "201001");

        //布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(multiMatchQueryBuilder);
        boolQueryBuilder.must(termQueryBuilder);
        //设置布尔查询对象
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);//设置搜索源配置
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for(SearchHit hit:searchHits){
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            System.out.println(sourceAsMap);
        }
    }
    //布尔查询使用过虑器
    @Test
    public void testFilter() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //source源字段过虑
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel"}, new String[]{});

        searchRequest.source(searchSourceBuilder);


        //匹配关键字
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("spring框架", "name", "description");
        //设置匹配占比
        multiMatchQueryBuilder.minimumShouldMatch("50%");
        //提升另个字段的Boost值
        multiMatchQueryBuilder.field("name",10);
        searchSourceBuilder.query(multiMatchQueryBuilder);

        //布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(searchSourceBuilder.query());

        //过虑
        boolQueryBuilder.filter(QueryBuilders.termQuery("studymodel", "201001"));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(60).lte(100));
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for(SearchHit hit:searchHits){
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            System.out.println(sourceAsMap);
        }
    }
    //sort
    public void testSort() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //source源字段过虑
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel"}, new String[]{});
        searchRequest.source(searchSourceBuilder);

        //布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //过虑
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0).lte(100));

        //排序
        searchSourceBuilder.sort(new FieldSortBuilder("studymodel").order(SortOrder.DESC));
        searchSourceBuilder.sort(new FieldSortBuilder("price").order(SortOrder.ASC));

        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String index = hit.getIndex();
            String type = hit.getType();
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);
        }
    }
    //highLight

    @Test
    public void testHightLight() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //source源字段过虑
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","description"},
                new String[]{});
        searchRequest.source(searchSourceBuilder);
        //匹配关键字
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("开发",
                "name", "description");
        searchSourceBuilder.query(multiMatchQueryBuilder);
        //布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(searchSourceBuilder.query());

        //过虑
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0).lte(100));
        //排序
        searchSourceBuilder.sort(new FieldSortBuilder("studymodel").order(SortOrder.DESC));
        searchSourceBuilder.sort(new FieldSortBuilder("price").order(SortOrder.ASC));
        //高亮设置
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<tag>");//设置前缀
        highlightBuilder.postTags("</tag>");//设置后缀
        // 设置高亮字段
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        // highlightBuilder.fields().add(new HighlightBuilder.Field("description"));
        searchSourceBuilder.highlighter(highlightBuilder);
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            //名称
            String name = (String) sourceAsMap.get("name");
            //取出高亮字段内容
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if(highlightFields!=null){
                HighlightField nameField = highlightFields.get("name");
                if(nameField!=null){
                    Text[] fragments = nameField.getFragments();
                    StringBuffer stringBuffer = new StringBuffer();
                    for (Text str : fragments) {
                        stringBuffer.append(str.string());
                    } name = stringBuffer.toString();
                }
            }
            String index = hit.getIndex();
            String type = hit.getType();
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            String studymodel = (String) sourceAsMap.get("studymodel");
            String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);
        }
    }
}