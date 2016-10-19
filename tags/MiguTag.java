package cn.migu.adp.dmp.tags;
import java.util.List;

/**
 * Created by lenvovo on 2016/10/19.
 */
public class MiguTag {
    private List<String> classify ;

    private String id;

    private List<String> parent ;

    public void setClassify(List<String> classify){
        this.classify = classify;
    }
    public List<String> getClassify(){
        return this.classify;
    }
    public void setId(String id){
        this.id = id;
    }
    public String getId(){
        return this.id;
    }
    public void setParent(List<String> parent){
        this.parent = parent;
    }
    public List<String> getParent(){
        return this.parent;
    }
}
