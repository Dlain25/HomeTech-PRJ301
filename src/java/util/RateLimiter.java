package util;
import java.util.*;

/** Bounded, synchronized single-Tomcat limiter. Use a shared store if deployed on multiple nodes. */
public final class RateLimiter {
    private final Map<String,Bucket> buckets=new HashMap<>();
    private static final class Bucket {long expires;int hits;Bucket(long e){expires=e;}}
    public synchronized boolean allow(String key,int limit,long windowMs) {
        long now=System.currentTimeMillis();
        buckets.entrySet().removeIf(e->e.getValue().expires<=now);
        Bucket b=buckets.get(key);
        if(b==null){if(buckets.size()>=5000)return false;b=new Bucket(now+windowMs);buckets.put(key,b);}
        if(b.hits>=limit)return false;b.hits++;return true;
    }
}
