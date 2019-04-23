package clockSynchronization.algorithms;

import java.util.ArrayList;
import java.util.Random;

import clockSynchronization.base.Client;
import clockSynchronization.base.ClockReader;
import clockSynchronization.base.FaultyClock;
import clockSynchronization.base.FieldMessage;
import clockSynchronization.base.NetworkLatency;
import clockSynchronization.base.Message;
import clockSynchronization.base.NetworkProxy;
import clockSynchronization.base.NetworkQueue;

public class Grid
{

    public static void main(String[] args)
    {
        NetworkQueue queue = new NetworkQueue();
        ClockReader cr = new ClockReader();
        
        int matSize = 3;
        int neighborCount = 0;
        ArrayList<GridClient> clients = new ArrayList<GridClient>();
        for (int i = 0; i < 3; i++) 
        {   
            for(int k = 0; k < 3; k++)
            {
                NetworkLatency latency = new Latency();
                FaultyClock bc = new FaultyClock(.0, 1e6);  
                NetworkProxy proxy = new NetworkProxy(queue, bc, latency);
                int [] neighbors = getNeighbors(i, k, matSize, matSize);

                GridClient gc = new GridClient(proxy, neighborCount++, neighbors);
                clients.add(gc);
                cr.addClock(bc);
            }
            
        }

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
            while(true)
            {
                try
                {
                    Thread.sleep(2000);
                }
                catch(InterruptedException ie)
                {
                }

                long startTime = proxy.getTime();
                for(int i = 0; i < neighbors.length; i++)
                {
                    proxy.sendMessage(new FieldMessage<Long>(0L), neighbors[i]);
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