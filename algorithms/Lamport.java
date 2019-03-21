package clockSynchronization.algorithms;

import clockSynchronization.base.Client;
import clockSynchronization.base.ClockReader;
import clockSynchronization.base.FaultyClock;
import clockSynchronization.base.IntegerMessage;
import clockSynchronization.base.LongMessage;
import clockSynchronization.base.NetworkLatency;
import clockSynchronization.base.NetworkProxy;
import clockSynchronization.base.NetworkQueue;

public class Lamport {
    /**
     * Lamport's logical clock algorithm. Clock counts will be passed as the message
     * itself.
     * 
     * Test
     */
    public static void main(String[] args) 
    {
        NetworkQueue queue = new NetworkQueue();
        FaultyClock goodClock = new FaultyClock(0, 0);
        FaultyClock badClock = new FaultyClock(.01, 1e6);

        NetworkLatency latency = new Latency();

        NetworkProxy proxy1 = new NetworkProxy(queue, goodClock, latency);
        NetworkProxy proxy2 = new NetworkProxy(queue, badClock, latency);

        LamportClient client1 = new LamportClient(proxy1, 1);
        LamportClient client2 = new LamportClient(proxy2, 2);

        new Thread(client1).start();
        new Thread(client2).start();

    }

    static class Latency implements NetworkLatency 
    {

        @Override
        public long getLatency(int source, int destination) 
        {
            return 500000000L;
        }

    }

    static class LamportClient extends Client 
    {
        private int logicalClockCount;
        private int id;

        public LamportClient(NetworkProxy proxy, int id) 
        {
            super(proxy);
            this.id = id;
            this.logicalClockCount = 0;
        }

        public void internalEvent()
        {
            if (Math.random() < 0.6) 
            {
                this.logicalClockCount += 1;
                System.out.printf("Internal event in %d.  Internal count inc to %d%n", 
                                    this.id, 
                                    this.logicalClockCount);
            }
        }

        public void updateClock(IntegerMessage im)
        {
            this.logicalClockCount = Math.max(im.i, this.logicalClockCount) + 1;
            System.out.printf("Process %d updated.  Internal count: %d%n", this.id, this.logicalClockCount);
        }
        @Override
        public void run() 
        {
            proxy.setID(this.id);
            if(this.id == 1)
            {
                this.logicalClockCount += 1;
                IntegerMessage sendmsg = new IntegerMessage(this.logicalClockCount);
                System.out.printf("Sending message from %d with count %d at time %d%n", 
                                    this.id, 
                                    this.logicalClockCount,
                                    System.nanoTime());
                proxy.sendMessage(sendmsg, (this.id % 2) + 1);
            }
            while (true) 
            {
                internalEvent();
                IntegerMessage recvmsg = (IntegerMessage) proxy.recvMessage((this.id % 2) + 1);
                this.logicalClockCount += 1;
                System.out.printf("Received msg at process %d.  Internal count: %d, Recv count: %d%n", this.id, this.logicalClockCount, recvmsg.i);
                updateClock(recvmsg);

                this.logicalClockCount += 1;
                IntegerMessage sendmsg = new IntegerMessage(this.logicalClockCount);
                System.out.printf("Sending message from %d with count %d at time %d%n", 
                                    this.id, 
                                    this.logicalClockCount,
                                    System.nanoTime());

                proxy.sendMessage(sendmsg, (this.id % 2) + 1);
                try 
                {
                    Thread.sleep(1000);
                } catch (InterruptedException e) 
                {

                }
            }
        }

    }

}