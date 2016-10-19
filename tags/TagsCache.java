package cn.migu.adp.dmp.tags;

import javax.swing.text.html.HTML;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jack on 2016/10/19.
 * this class is used to cache tags..
 */
public class TagsCache {

    public  static ConcurrentHashMap<String,String > TagCache = new ConcurrentHashMap<>();

    static {

        TagCache.put("50000","男性");
        TagCache.put("50002","女性");

        TagCache.put("51288","苹果");
        TagCache.put("51290","三星");
        TagCache.put("51392","国际漫游");
        TagCache.put("51394","国内漫游");
        TagCache.put("51396","爱好 警匪");
        TagCache.put("51398","爱好 动作");
        TagCache.put("53200","爱好足球");
        TagCache.put("53202","爱好篮球");

    }

}
