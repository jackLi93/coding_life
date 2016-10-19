package cn.migu.adp.dmp.tags;

import java.util.List;

/**
 * Created by lenvovo on 2016/10/19.
 */
public class UserRole {
    private String did;

    private String idtype;

    private List<MiguTag> tag ;

    public void setDid(String did){
        this.did = did;
    }
    public String getDid(){
        return this.did;
    }
    public void setIdtype(String idtype){
        this.idtype = idtype;
    }
    public String getIdtype(){
        return this.idtype;
    }
    public void setTag(List<MiguTag> tag){
        this.tag = tag;
    }
    public List<MiguTag> getTag(){
        return this.tag;
    }
}
