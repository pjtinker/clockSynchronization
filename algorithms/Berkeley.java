package clockSynchronization.algorithms;

import java.util.ArrayList;

import clockSynchronization.base.Client;
import clockSynchronization.base.ClockReader;
import clockSynchronization.base.FaultyClock;
import clockSynchronization.base.IntegerMessage;
import clockSynchronization.base.NetworkLatency;
import clockSynchronization.base.LongMessage;
import clockSynchronization.base.Message;
import clockSynchronization.base.NetworkProxy;
import clockSynchronization.base.NetworkQueue;

public class Berkeley {
    /**
     * Berkeley's clock synchronization algorithm. 
     * Node 0: master
     * 
     */
    public static void main(String[] args) 
    {
        NetworkQueue queue = new NetworkQueue();
        FaultyClock goodClock = new FaultyClock(0, 0);
        FaultyClock badClock1 = new FaultyClock(.01, 1e6);
        FaultyClock badClock2 = new FaultyClock(.03, 1e7);
        FaultyClock badClock3 = new FaultyClock(.08, 1e3);

        NetworkLatency latency = new Latency();

        NetworkProxy proxy1 = new NetworkProxy(queue, goodClock, latency);
        NetworkProxy proxy2 = new NetworkProxy(queue, badClock1, latency);
        NetworkProxy proxy3 = new NetworkProxy(queue, badClock2, latency);
        NetworkProxy proxy4 = new NetworkProxy(queue, badClock3, latency);

        BerkeleySlaveClient slave1 = new BerkeleySlaveClient(proxy2, 1);
        BerkeleySlaveClient slave2 = new BerkeleySlaveClient(proxy3, 2);
        BerkeleySlaveClient slave3 = new BerkeleySlaveClient(proxy4, 3);

        ArrayList<BerkeleySlaveClient> slaves = new ArrayList<BerkeleySlaveClient>();

        slaves.add(slave1);
        slaves.add(slave2);
        slaves.add(slave3);
        
        BerkeleyMasterClient master = new BerkeleyMasterClient(proxy1, 0, slaves);

        for(BerkeleySlaveClient slave : slaves)
        {
            new Thread(slave).start();
            System.out.printf("Slave %d running...%n", slave.getID());
        }

        new Thread(master).start();
        System.out.println("Master running...");
    }

    static class Latency implements NetworkLatency 
    {

        @Override
        public long getLatency(int source, int destination) 
        {
            return 500000000L;
        }

    }

    static class BerkeleyMasterClient extends Client 
    {

        private int id;
        private ArrayList<BerkeleySlaveClient> slaves;
        public BerkeleyMasterClient(NetworkProxy proxy, int id, ArrayList<BerkeleySlaveClient> slaves)
        {
            super(proxy);
            this.id = id;
            this.slaves = slaves;
        }

        public long calculateOffset(ArrayList<Long> times)
        {
            long totalDiffs = 0L;
            for(long l : times)
            {
                totalDiffs += l;
            }
            return totalDiffs / this.slaves.size();
        }

        @Override
        public void run()
        {
            proxy.setID(this.id);
            ArrayList<Long> diffs = new ArrayList<Long>();
            while(true)
            {
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                }

                for (BerkeleySlaveClient slave : this.slaves)
                {
                    int slave_id = slave.getID();
                    long startTime = proxy.getTime();
                    IntegerMessage sendmsg = new IntegerMessage(slave_id);
                    System.out.printf("Requesting clock from slave: %d%n", slave_id);
                    proxy.sendMessage(sendmsg, slave_id);

                    LongMessage recvmsg = (LongMessage) proxy.recvMessage(slave_id);
                    long diff = proxy.getTime() - startTime;
                    diffs.add(diff);
                    System.out.printf("Received msg from slave: %d%nMessage: %d%n", slave_id, recvmsg.l);
                }
                long offset = this.calculateOffset(diffs);
                System.out.printf("Calculated offset: %d%n", offset);
                for (BerkeleySlaveClient slave : this.slaves)
                {
                    int slave_id = slave.getID();
                    proxy.sendMessage(new LongMessage(offset), slave_id);

                }
            }

        }
    }

    static class BerkeleySlaveClient extends Client
    {
        /**
         * Passing IntegerMessage to slaves signals a request for time.
         * Passing a LongMessage indicates that message contains a clock update.
         */
        private int id;
        public BerkeleySlaveClient(NetworkProxy proxy, int id)
        {
            super(proxy);
            this.id = id;
        }

        public int getID() { return this.id; }

        @Override
        public void run()
        {
            proxy.setID(this.id);
            while(true)
            {
                Message recvmsg = proxy.recvMessage(0);
                // Check message type and handle accordingly
                if(recvmsg instanceof IntegerMessage){
                    System.out.printf("Time request message received at slave: %d%n", this.id);
                    proxy.sendMessage(new LongMessage(proxy.getTime()), 0);
                    
                }else {
                    recvmsg = (LongMessage)recvmsg;
                    System.out.printf("Time adjustment received at slave: %d%n", this.id);
                    proxy.setTime(proxy.getTime() + (long)recvmsg.getMsg());
                    System.out.printf("New time at slave %d -> %d%n", this.id, proxy.getTime());
                }

            }

        } 
    }

}