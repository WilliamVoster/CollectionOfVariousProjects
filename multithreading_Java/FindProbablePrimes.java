package lab2;

import java.lang.reflect.GenericArrayType;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;

public class FindProbablePrimes {

    public static Map<BigInteger, BigInteger> sequential(List<BigInteger> arguments){

        Map<BigInteger, BigInteger> returnMap = new HashMap<>();

        Iterator<BigInteger> it = arguments.iterator();
        while(it.hasNext()){
            BigInteger x = it.next();
            BigInteger y = x.nextProbablePrime();
            returnMap.put(x, y);
        }

        return returnMap;

    }

    public static Map<BigInteger, BigInteger> parallel(List<BigInteger> arguments){

        Map<BigInteger, BigInteger> returnMap = new HashMap<>();

        List<CalcNextPrime> runnables = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();

        for (BigInteger x : arguments){

            CalcNextPrime r = new CalcNextPrime(x);
            Thread t = new Thread(r);
            t.start();
            threads.add(t);
            runnables.add(r);
        }

        Iterator<CalcNextPrime> itR = runnables.iterator();
        Iterator<Thread> itT = threads.iterator();
        while(itR.hasNext() && itT.hasNext()){

            Thread t = itT.next();
            CalcNextPrime r = itR.next();

            try {

                t.join();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            BigInteger x = r.getX();
            BigInteger y = r.getY();

            returnMap.put(x, y);
        }

        return returnMap;
    }

    public static <T, R> Map<T, R> genericParallelCompute(List<T> data, Function<T, R> function){

        class GenericRunnable implements Runnable{

            T input;
            R output;
            GenericRunnable(T input){this.input = input;}

            public T getInput(){return input;}
            public R getOutput(){return output;}

            @Override
            public void run() {
                this.output = function.apply(input);
            }
        }

        Map<T, R> returnMap = new HashMap<>();
        List<Thread> threads = new ArrayList<>();
        List<GenericRunnable> runnables = new ArrayList<>();

        for (T element : data){

            GenericRunnable r = new GenericRunnable(element);
            Thread t = new Thread();
            t.start();
            runnables.add(r);
            threads.add(t);
        }

        Iterator<GenericRunnable> itR = runnables.iterator();
        Iterator<Thread> itT = threads.iterator();
        while(itR.hasNext() && itT.hasNext()){

            GenericRunnable r = itR.next();
            Thread t = itT.next();

            try{
                t.join();
            }catch (InterruptedException e){
                e.printStackTrace();
            }

            returnMap.put(r.getInput(), r.getOutput());
        }

        return returnMap;
    }


    public static void main(String[] args) {

        Random random = new Random();

        FindProbablePrimes findPrimes = new FindProbablePrimes();

        List<BigInteger> arguments = new ArrayList<>();

        int N = 16;

        for (int i = 0; i < N; i++) {

            BigInteger a = new BigInteger(2000, random);

            arguments.add(a);
        }

        Calendar calendar = Calendar.getInstance();
        long pre = calendar.getTimeInMillis();

        Map<BigInteger, BigInteger> nextPrime = findPrimes.sequential(arguments);

        calendar = Calendar.getInstance();
        long post = calendar.getTimeInMillis();
        long diffSequential = post - pre;


        calendar = Calendar.getInstance();
        pre = calendar.getTimeInMillis();

        Map<BigInteger, Integer> returnMap = genericParallelCompute(arguments, x->Integer.parseInt(String.valueOf(x.toString().charAt(0))));

        calendar = Calendar.getInstance();
        post = calendar.getTimeInMillis();
        long diffParallel = post - pre;


        System.out.println("time taken (sequential): " + diffSequential + " - " + N + " times");
        System.out.println("time taken (parallel): " + diffParallel + " - " + N + " times");
        System.out.println("seq / n " + diffSequential / (float)N);
        System.out.println("par / n " + diffParallel / (float)N);
        System.out.println("factor (seq/par): " + (float)diffSequential / (float)diffParallel);

        System.out.println(returnMap);



    }

}


class CalcNextPrime implements Runnable{

    BigInteger x, y;

    CalcNextPrime(BigInteger x){
        this.x = x;
    }

    public BigInteger getY(){return this.y;}
    public BigInteger getX(){return this.x;}

    @Override
    public void run() {
        y = x.nextProbablePrime();
    }
}