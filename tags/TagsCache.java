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
		// 动态加载资源的方法，资源文件批量写入
	    static {
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(new File(Thread.currentThread().getContextClassLoader().getResource("tag_set.txt").getPath())));//tag_set放在工程的资源文件下，动态加载资源
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] tmp = line.split("\t");
                if (tmp.length == 11) {
                    TagCache.put(tmp[1], tmp[4]);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
