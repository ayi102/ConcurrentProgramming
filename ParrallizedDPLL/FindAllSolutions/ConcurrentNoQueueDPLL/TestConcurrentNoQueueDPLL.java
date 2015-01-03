import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class TestConcurrentNoQueueDPLL {

	final private ThreadLocal<Integer> THREAD_ID = new ThreadLocal<Integer>(){
		final private AtomicInteger id = new AtomicInteger(0);

		protected Integer initialValue(){
			return id.getAndIncrement();
		}
	};
	// set # of THREADS and QUEENS
	final static int THREADS = 2;
	int NumOfQueens = 6;
	Thread[] thread = new Thread[THREADS];
	ConcurrentNoQueueDPLL test = new ConcurrentNoQueueDPLL(NumOfQueens);

	// Run Multiple Threads
	public void testParallel() throws Exception {

		for (int i = 0; i < THREADS; i++) {
			thread[i] = new MyThread();
		}
		for (int i = 0; i < THREADS; i++) {
			thread[i].start();
		}
		for (int i = 0; i < THREADS; i++) {
			thread[i].join();
		}
	}
	class MyThread extends Thread {
		public void run() 
		{
			// Start the timer
			long startTime = System.currentTimeMillis();
			// Grab Thread ID
			int i = THREAD_ID.get();

			// Generate lists to store cnf, list of variables and list of guesses per variable
			ArrayList<List<Integer>> cnf = new ArrayList<List<Integer>>();
			ArrayList<Integer> vars = new ArrayList<Integer>();
			List<Integer> guesses = new ArrayList<Integer>();
			boolean answer;

			test.CreateFormula(cnf);
			test.CreateVarList(vars);
			test.BCP(vars, cnf);

			// Assign Thread 0 to solve every guess sequence that begins with 1
			// Assign Thread 1 to solve every guess sequence that begines with 0
			if(i == 0)
				answer = (test.DPLL_OPTO(vars, cnf, 1,guesses));
			else
				answer = (test.DPLL_OPTO(vars, cnf, 0,guesses));
			// Answer has been found and printed, quit timer
			long endTime = System.currentTimeMillis();

			if(answer)
			{
				long totalTime = (endTime - startTime);  
				System.out.println("Finished in : " + totalTime);
			}	
		}
	}

	public static void main(String[] args)
	{	
		TestConcurrentNoQueueDPLL mpt = new TestConcurrentNoQueueDPLL();

		try {
			mpt.testParallel();
		}
		catch (Exception e) {}
	}

}
