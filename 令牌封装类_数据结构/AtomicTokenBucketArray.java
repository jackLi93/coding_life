package cn.migu.adp.dmp.query_service.bean;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * Created by Smart on 2016/9/13.
 */
public class AtomicTokenBucketArray {
    private static float loadFactor = 0.75f;

    private AtomicLongArray atomicLongArray;//用于存储用户的令牌数
    private ConcurrentHashMap<String, Integer> arrayIndex; //将key和atomicLong数组的索引对应起来
    //流程：key--->value(value又是数组的索引)--->获取数组中的值

    public AtomicTokenBucketArray(int size){
        this.atomicLongArray = new AtomicLongArray(size);
        this.arrayIndex = new ConcurrentHashMap<String, Integer>(size, this.loadFactor);
    }

    public AtomicTokenBucketArray(int size, float loadFactor){
        this.loadFactor = loadFactor;
        this.atomicLongArray = new AtomicLongArray(size);
        this.arrayIndex = new ConcurrentHashMap<String, Integer>(size, loadFactor);
    }

    private int getIndex(String indexName) {
        if(arrayIndex.containsKey(indexName)){
            return arrayIndex.get(indexName);
        } else {
            return -1;
        }
    }

    public void set(String indexName, long value) { //z只能对已有的进行set
        int index = getIndex(indexName);
        if(index != -1){
            atomicLongArray.set(index, value);
        }
    }

    public void put(String indexName, long value){  //对已有和新增的
        int index = getIndex(indexName);
        if(index != -1){
            atomicLongArray.set(index, value);
        } else {
            int currentArraySize = arrayIndex.size();
            if(currentArraySize >= atomicLongArray.length() * loadFactor){
                //需要扩容 atomicLongArray
                synchronized(this) {
                    AtomicLongArray oldArray = this.atomicLongArray;
                    this.atomicLongArray = new AtomicLongArray(currentArraySize * 2 + 1);
                    for (int i = 0; i < currentArraySize; i++) {
                        this.atomicLongArray.set(i, oldArray.get(i));
                    }
                }

            }
            arrayIndex.put(indexName, currentArraySize);
            atomicLongArray.set(currentArraySize, value);
        }
    }

    public long decrementAndGet(String indexName) {
        int index;
        long value = -1;
        if(arrayIndex.containsKey(indexName)){
            index = arrayIndex.get(indexName);
            value = atomicLongArray.decrementAndGet(index);
        }
        return value;
    }

//    public static void main(String[] args) {
//        AtomicTokenBucketArray at = new AtomicTokenBucketArray(10);
//        for(int i = 0; i<100; i++){
//            at.put(i+"", 1000L);
//        }
//        at.decrementAndGet("1");
//        for(int i = 0; i<at.atomicLongArray.length(); i++){
//            System.out.println("index:" + i + " key:" + at.getIndex(i+"")+ " value:" + at.atomicLongArray.get(i));
//        }
//
//        System.out.println("atomicLongArray:\t" + at.atomicLongArray.length());
//        System.out.println("arrayIndex:\t" + at.arrayIndex.size());
//    }
}
