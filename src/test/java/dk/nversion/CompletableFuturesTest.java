package dk.nversion;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

public class CompletableFuturesTest {

    @Test
    public void test3StageGetNested() throws Exception {
        CompletableFuture<String> firstFuture = getFirst();
        firstFuture.whenComplete((firstStr, firstEx) -> {
           CompletableFuture<String> secondFuture = getSecondDependingOnFirst(firstStr);
            secondFuture.whenComplete((secondStr, secondEx) -> {
                CompletableFuture<String> thirdFuture = getThirdDependingOnFirstAndSecond(firstStr, secondStr);
                thirdFuture.whenComplete((thirdStr, ex) -> {
                    System.out.println("nested: " + thirdStr + (ex != null ? " : " +ex.getMessage() : ""));
                });
            });
        });
    }

    @Test
    public void test3StageGet() throws Exception {
        CompletableFuture<String> firstFuture = getFirst();
        CompletableFuture<String> secondFuture = firstFuture.thenCompose(this::getSecondDependingOnFirst);
        CompletableFuture<String> thirdFuture = secondFuture.thenCompose((secondStr) -> {
            // We can call .join here because we know that secondFuture won't complete before firstFuture
            return getThirdDependingOnFirstAndSecond(firstFuture.join(), secondStr);
        });

        thirdFuture.whenComplete((thirdStr, ex) -> {
            System.out.println("simple: " + thirdStr);
            String a = null;
            System.out.println(a.toString());

        }).exceptionally(e -> {
            System.out.println("Future exceptionally finished: " + e);
            return null;
        });
    }

    @Test
    public void test3StageGetShort() throws Exception {
        CompletableFuture<String> firstFuture = getFirst();
        firstFuture.thenCompose(this::getSecondDependingOnFirst).thenCompose((secondStr) -> {
            // We can call .join here because we know that secondFuture won't complete before firstFuture
            return getThirdDependingOnFirstAndSecond(firstFuture.join(), secondStr);
        }).whenComplete((thirdStr, ex) -> {
            System.out.println("short: " + thirdStr + (ex != null ? " : " +ex.getMessage() : ""));
        });
    }

    private CompletableFuture<String> getFirst() {
        CompletableFuture<String> future = new CompletableFuture<>();
        future.complete("first");
        //future.completeExceptionally(new Exception(""));
        return future;
    }

    private CompletableFuture<String> getSecondDependingOnFirst(String first) {
        CompletableFuture<String> future = new CompletableFuture<>();
        future.complete("second");
        return future;
    }

    private CompletableFuture<String> getThirdDependingOnFirstAndSecond(String first, String second) {
        CompletableFuture<String> future = new CompletableFuture<>();
        future.complete(first + "," + second + ",third");
        return future;
    }
}
