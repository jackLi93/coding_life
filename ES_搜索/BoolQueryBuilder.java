package com.code_life.look_alike;

/**
 * Created by jacklee on 2016/10/27. 重写了ES中BoolQueryBuilder的方法，添加了三个方法，可以实现多个参数插入进去
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.iflytek.gnome.share.GLog;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.ToXContent.Params;
import org.elasticsearch.index.query.BaseQueryBuilder;
import org.elasticsearch.index.query.BoostableQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

public class BoolQueryBuilder extends BaseQueryBuilder implements BoostableQueryBuilder<BoolQueryBuilder> {
   private  final static GLog log = new GLog(BoolQueryBuilder.class);
    private ArrayList<QueryBuilder> mustClauses = new ArrayList();
    private ArrayList<QueryBuilder> mustNotClauses = new ArrayList();
    private ArrayList<QueryBuilder> shouldClauses = new ArrayList();
    private float boost = -1.0F;
    private Boolean disableCoord;
    private String minimumShouldMatch;
    private Boolean adjustPureNegative;
    private String queryName;

    public BoolQueryBuilder() {
    }

    public BoolQueryBuilder must(QueryBuilder queryBuilder) {
        this.mustClauses.add(queryBuilder);
        return this;
    }

    /**
     * 输入一个 list的Idtag 返回查询
     * @param tagIdList
     * @return
     */
    public BoolQueryBuilder must(List<String > tagIdList){
        for(int i=0;i<tagIdList.size();i++){
            this.mustClauses.add(QueryBuilders.termQuery("id", tagIdList.get(i)));
        }
        return this;
    }
    public BoolQueryBuilder mustNot(QueryBuilder queryBuilder) {
        this.mustNotClauses.add(queryBuilder);
        return this;
    }

    /**
     * 输入taglist 返回must Not
     * @param tagIdList
     * @return
     */

    public BoolQueryBuilder mustNot(List<String > tagIdList){
        for(int i=0;i<tagIdList.size();i++){
            this.mustNotClauses.add(QueryBuilders.termQuery("id", tagIdList.get(i)));
        }
        return this;
    }
    public BoolQueryBuilder should(QueryBuilder queryBuilder) {
        this.shouldClauses.add(queryBuilder);
        return this;
    }

    /**
     * 返回should
     * @param tagIdList
     * @return
     */
    public BoolQueryBuilder should(List<String > tagIdList){
        for(int i=0;i<tagIdList.size();i++){
            this.shouldClauses.add(QueryBuilders.termQuery("id", tagIdList.get(i)));
        }
        return this;
    }

    public BoolQueryBuilder boost(float boost) {
        this.boost = boost;
        return this;
    }

    public BoolQueryBuilder disableCoord(boolean disableCoord) {
        this.disableCoord = Boolean.valueOf(disableCoord);
        return this;
    }

    public BoolQueryBuilder minimumNumberShouldMatch(int minimumNumberShouldMatch) {
        this.minimumShouldMatch = Integer.toString(minimumNumberShouldMatch);
        return this;
    }

    public BoolQueryBuilder minimumShouldMatch(String minimumShouldMatch) {
        this.minimumShouldMatch = minimumShouldMatch;
        return this;
    }

    public boolean hasClauses() {
        return !this.mustClauses.isEmpty() || !this.shouldClauses.isEmpty() || !this.mustNotClauses.isEmpty();
    }

    public BoolQueryBuilder adjustPureNegative(boolean adjustPureNegative) {
        this.adjustPureNegative = Boolean.valueOf(adjustPureNegative);
        return this;
    }

    public BoolQueryBuilder queryName(String queryName) {
        this.queryName = queryName;
        return this;
    }

    protected void doXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject("bool");
        this.doXArrayContent("must", this.mustClauses, builder, params);
        this.doXArrayContent("must_not", this.mustNotClauses, builder, params);
        this.doXArrayContent("should", this.shouldClauses, builder, params);
        if(this.boost != -1.0F) {
            builder.field("boost", this.boost);
        }

        if(this.disableCoord != null) {
            builder.field("disable_coord", this.disableCoord);
        }

        if(this.minimumShouldMatch != null) {
            builder.field("minimum_should_match", this.minimumShouldMatch);
        }

        if(this.adjustPureNegative != null) {
            builder.field("adjust_pure_negative", this.adjustPureNegative);
        }

        if(this.queryName != null) {
            builder.field("_name", this.queryName);
        }

        builder.endObject();
    }

    private void doXArrayContent(String field, List<QueryBuilder> clauses, XContentBuilder builder, Params params) throws IOException {
        if(!clauses.isEmpty()) {
            if(clauses.size() == 1) {
                builder.field(field);
                ((QueryBuilder)clauses.get(0)).toXContent(builder, params);
            } else {
                builder.startArray(field);
                Iterator i$ = clauses.iterator();

                while(i$.hasNext()) {
                    QueryBuilder clause = (QueryBuilder)i$.next();
                    clause.toXContent(builder, params);
                }

                builder.endArray();
            }

        }
    }

    public  static  void  main(String []args){

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        List<String> tagList = new ArrayList<>();
        tagList.add("5114");
        tagList.add("5188");
        BoolQueryBuilder test = boolQueryBuilder.must(tagList).mustNot(tagList).should(tagList);
        log.info(test.toString());
    }
}

//以下是输出
/*[BoolQueryBuilder.java172] main:{
        "bool" : {
        "must" : [ {
        "term" : {
        "id" : "5114"
        }
        }, {
        "term" : {
        "id" : "5188"
        }
        } ],
        "must_not" : [ {
        "term" : {
        "id" : "5114"
        }
        }, {
        "term" : {
        "id" : "5188"
        }
        } ],
        "should" : [ {
        "term" : {
        "id" : "5114"
        }
        }, {
        "term" : {
        "id" : "5188"
        }
        } ]
        }
        }*/

