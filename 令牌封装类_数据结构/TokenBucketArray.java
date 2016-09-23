package cn.migu.adp.dmp;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * Created by jacklee on 2016/9/23.
 */
public class TokenBucketArray {
    /**
     * 成员
     * 构造函数
     * set||get等方法
     */
    private  int size;
    private  float loadFactor = 0.75f;
    private AtomicLongArray token_array;
    private ConcurrentHashMap<String ,Integer> key2array_index ;

    public TokenBucketArray(int size){
        this.size=size;
        token_array = new AtomicLongArray(size);
        key2array_index = new ConcurrentHashMap<String ,Integer>(size,this.loadFactor);
    }

    public TokenBucketArray(int size,float loadFactor){
        this.loadFactor =loadFactor;
        this.size=size;
        token_array = new AtomicLongArray(size);
        key2array_index = new ConcurrentHashMap<String,Integer>(size,loadFactor);
    }

    public int getIndex(String key){
        if(key2array_index.containsKey(key)){
            return key2array_index.get(key);
        }
        return -1;
    }

    /**
     * 用于更新tokenbucketArray中的值
     * @param key
     * @param tokenBuckets
     */
    public void setToken(String key,long tokenBuckets){
        if(getIndex(key)!=-1){
            int index = getIndex(key);
            token_array.set(index,tokenBuckets);
        }

    }

    public void  putToken(String key,long tokenBuckets){
        if(getIndex(key)!=-1){
            //如果已存在，直接更新即可
            setToken(key,tokenBuckets);
        }else{
            //存入数值，但是存入之前需要进行判断，以免溢出
            int currentSize = key2array_index.size();
            if(currentSize<this.size*this.loadFactor){
                key2array_index.put(key,currentSize);
                token_array.set(currentSize,tokenBuckets);
            }else{
                synchronized (this){
                    int  new_size = this.size*2+1;
                    this.size=new_size;
                    ConcurrentHashMap<String,Integer> new_key2array_index = new ConcurrentHashMap<>(new_size,this.loadFactor);
                    AtomicLongArray new_token_array = new AtomicLongArray(new_size);
                    for(ConcurrentHashMap.Entry<String,Integer> key_index:key2array_index.entrySet()){
                        new_key2array_index.put(key_index.getKey(),key_index.getValue());
                        new_token_array.set(key_index.getValue(),token_array.get(key_index.getValue()));
                    }
                    key2array_index=new_key2array_index;
                    token_array=new_token_array;
                    key2array_index.put(key,currentSize);
                    token_array.set(currentSize,tokenBuckets);//?会自动增长么,测试证明不能自增，需要人为控制。
                    //注意：此处代码可以优化，因为ConcurrentHashMap会自动进行扩容。
                }

            }

        }

    }

    public long decrementAndGet(String key){
        if(getIndex(key)!=-1){
            int index = getIndex(key);
            return token_array.decrementAndGet(index);
        }
        return -1;
    }

    public int getSize(){
        return  this.key2array_index.size();
    }
    public static  void main(String []args){
        TokenBucketArray tokenBucketArray = new TokenBucketArray(60*1000);
        for(int i=0;i<80*1000;i++){
            tokenBucketArray.putToken("key"+i,i*1000l);
        }
        for (int i =0;i<tokenBucketArray.getSize();i++){
            System.out.println(tokenBucketArray.decrementAndGet("key"+i)+1);
        }

        //以下代码是测试，AtomicLongArray是否会自增，程序报错，测试结果表明：不能自增，Array初始化的size为10,只能放10个值，
        //否则数组会越界

/*        AtomicLongArray test_array = new AtomicLongArray(10);
        for(int i=0;i<100;i++){
            test_array.set(i,i*1000l);
        }
        for(int i =0;i<test_array.length();i++){
            System.out.println(test_array.get(i));
        }*/

        //以下代码测试，ConcurrentHashMap<String,String>能否自动扩容
        ConcurrentHashMap<String,String> test_chashMap= new ConcurrentHashMap<>(80);
        for(int i=0;i<800;i++){
            test_chashMap.put("key"+i,"value"+i);
        }
        for(int i=0;i<test_chashMap.size();i++){
            System.out.println(test_chashMap.get("key"+i));
        }
        //测试结果，ConcurrentHashMap<String,String>会自动扩容。
    }
}
