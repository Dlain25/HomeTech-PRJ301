import util.*;
import java.math.BigDecimal;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkflowChecks {
    static void check(boolean v){if(!v)throw new AssertionError();}
    static void rejects(Runnable task){try{task.run();}catch(IllegalArgumentException e){return;}throw new AssertionError("Expected rejection");}
    public static void main(String[] args) throws Exception {
        check(OrderRules.next("approve","PENDING","COD","PENDING").equals("CONFIRMED"));
        check(OrderRules.next("approve","PENDING","BANK_TRANSFER","PAID").equals("CONFIRMED"));
        rejects(()->OrderRules.next("approve","PENDING","BANK_TRANSFER","PENDING"));
        rejects(()->OrderRules.next("approve","CANCELLED","BANK_TRANSFER","PAID"));
        rejects(()->OrderRules.next("deliver","PENDING","COD","PENDING"));
        check(OrderRules.next("ship","CONFIRMED","BANK_TRANSFER","PAID").equals("SHIPPING"));
        check(OrderRules.next("deliver","SHIPPING","COD","PENDING").equals("DELIVERED"));
        rejects(()->OrderRules.next("ship","CONFIRMED","BANK_TRANSFER","FAILED"));
        rejects(()->OrderRules.next("cancelUser","PENDING","BANK_TRANSFER","PAID"));
        rejects(()->OrderRules.next("cancel","PENDING","BANK_TRANSFER","REFUNDED"));
        rejects(()->OrderRules.next("cancel","CANCELLED","COD","FAILED"));
        check(OrderRules.cancellable("PENDING","PENDING"));
        check(!OrderRules.cancellable("SHIPPING","PENDING"));
        check(!OrderRules.cancellable("PENDING",null));
        OrderRules.sameAmount(new BigDecimal("10000.00"),new BigDecimal("10000"));
        rejects(()->OrderRules.sameAmount(new BigDecimal("10000"),new BigDecimal("10001")));
        rejects(()->OrderRules.sameAmount(BigDecimal.ZERO,BigDecimal.ZERO));
        RateLimiter limiter=new RateLimiter();
        ExecutorService workers=Executors.newFixedThreadPool(8);AtomicInteger accepted=new AtomicInteger();
        for(int i=0;i<100;i++)workers.submit(()->{if(limiter.allow("same-ip",5,60000))accepted.incrementAndGet();});
        workers.shutdown();check(workers.awaitTermination(5,TimeUnit.SECONDS));check(accepted.get()==5);
        check(limiter.allow("other-ip",5,60000));
        System.out.println("WorkflowChecks passed: state transitions, money checks, concurrent rate limiter.");
    }
}
