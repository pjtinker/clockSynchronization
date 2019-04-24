package clockSynchronization.algorithms;

import java.util.ArrayList;
import java.util.Random;

import clockSynchronization.base.Client;
import clockSynchronization.base.ClockReader;
import clockSynchronization.base.FaultyClock;
import clockSynchronization.base.GridMessage;
import clockSynchronization.base.NetworkLatency;
import clockSynchronization.base.Message;
import clockSynchronization.base.NetworkProxy;
import clockSynchronization.base.NetworkQueue;

public class Grid
{
    /**
     * Grid algorithm.  Hard coded for a 3x3 matrix
     * @param args
     */
    public static void main(String[] args)
    {
        NetworkQueue queue = new NetworkQueue();
        ClockReader cr = new ClockReader();
        Random r = new Random();
        double min = 0.0001;
        double max = 0.5;
        int matSize = 3;
        int neighborCount = 0;
        ArrayList<GridClient> clients = new ArrayList<GridClient>();
        for (int i = 0; i < 3; i++) 
        {   
            for(int k = 0; k < 3; k++)
            {
                NetworkLatency latency = new Latency();
                FaultyClock bc;
                if(neighborCount == 0)
                {
                    bc = new FaultyClock(.0, 0);
                }
                else
                {
                    double v = (neighborCount % 2 == 0) ? 1e5 : 1e7;
                    bc = new FaultyClock(.001, v);  
                }

                NetworkProxy proxy = new NetworkProxy(queue, bc, latency);
                int [] neighbors = getNeighbors(i, k, matSize, matSize);

                GridClient gc = new GridClient(proxy, neighborCount++, neighbors);
                clients.add(gc);
                cr.addClock(bc);
                new Thread(gc).start();
            }
            
        }
        cr.printClocks(50);
    }
    static class Latency implements NetworkLatency
    {
        Random rand;

        public Latency()
        {
            rand = new Random();
        }

        @Override
        public long getLatency(int source, int destination)
        {
            return (long) (500000000L + rand.nextGaussian() * 10000000);
        }
    }

    static class GridClient extends Client 
    {
        private int id;
        private int[] neighbors;

        public GridClient(NetworkProxy proxy, int id, int[] neighbors)
        {
            super(proxy);
            this.id = id;
            this.neighbors = neighbors;
        }

        @Override
        public void run()
        {
            proxy.setID(this.id);
            long [] timeDiffs = new long[neighbors.length];
            int[] sourceId = new int[1];
            boolean proceed = false;
            if(this.id == 0)
            {
                proceed = true;
            }
            while(true)
            {
                try
                {
                    Thread.sleep(100);
                }
                catch(InterruptedException ie)
                {
                }
                if(proceed)
                {
                    long startTime = proxy.getTime();
                    for(int i = 0; i < this.neighbors.length; i++)
                    {
                        proxy.sendMessage(new GridMessage<Long>(0L, false), this.neighbors[i]);
                        GridMessage<Long> msg = (GridMessage<Long>) proxy.recvMessage(this.neighbors[i]);
                        long diff = startTime - msg.getMsg() + 
                                    (proxy.getTime() - startTime) / 2;
                        timeDiffs[i] = diff;
                    }
                    long avgDiff = 0;

                    for(int k = 0; k < timeDiffs.length; k++)
                    {
                        avgDiff += timeDiffs[k];
                    }
                    avgDiff += proxy.getTime() - startTime;
                    avgDiff /= this.neighbors.length + 1;
                    proxy.setTime(proxy.getTime() - avgDiff);

                    int nextUp = 0;
                    if(this.id != 8) nextUp = this.id + 1;
                    proxy.sendMessage(new GridMessage<Long>(0L, true), nextUp);
                    proceed = false;
                }
                else
                {
                    GridMessage<Long> recv = (GridMessage<Long>) proxy.recvAnyMessage(sourceId);
                    if(recv.getBool() == true)
                    {
                        proceed = true;
                    }
                    else
                    {
                        proxy.sendMessage(new GridMessage<Long>(proxy.getTime(), false), sourceId[0]);
                    }
                }

            }
        }
    }
    private static int addr(int x, int y, int m, int n)
    {
        int colValue = (y%n);
        if(colValue < 0) colValue += n;
        int rowValue = (x%m);
        if(rowValue < 0) rowValue += m;
        int val = colValue * m + rowValue;
        return val;
    }

    private static int[] getNeighbors(int row, int col, int numRows, int numCols) 
    {
        int idx = 0;
        int[] neighbors = new int[4];
        for (int k = -1; k <= 1; k++)
        {
            for(int j = -1; j <= 1; j++)
            {
                if(k != 0 || j != 0)
                {
                    if(Math.abs(k) != Math.abs(j))
                    {
                        neighbors[idx] = addr(row+k, col+j, numRows, numCols);
                        idx++;

                    }
                }
            }
        }
        return neighbors;
    }
}