package test.precomputations;

import java.util.concurrent.*;

public class ThreadTest {


    private class TestTask implements Callable<String> {
        @Override
        public String call() {
            System.out.println("This was written by this thread");

            try {
                Thread.sleep(5000);
                System.out.println("This was written after five seconds of sleep");
            } catch (InterruptedException e) {
//                e.printStackTrace();
            }

            return "success";
        }
    }


    public void testThread() {
        // to start a new thread with a timeout
        ExecutorService executor = Executors.newSingleThreadExecutor();
        // result of the thread
        Future<String> future = null;

        future = executor.submit(new TestTask());
        try {
            String result = future.get(2000, TimeUnit.MILLISECONDS);
            System.out.println(result);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            System.out.println("the execution was terminated after 5 s");
            boolean cancelled = future.cancel(true);
            System.out.println(cancelled);


        }

        try {
            System.out.println("attempt to shutdown executor");
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            System.err.println("tasks interrupted");
        }
        finally {
            if (!executor.isTerminated()) {
                System.err.println("cancel non-finished tasks");
            }
            executor.shutdownNow();
            System.out.println("shutdown finished");
        }

    }

    public static void main(String[] args) {
        ThreadTest test = new ThreadTest();
        test.testThread();
        System.out.println("Done");
    }
}
