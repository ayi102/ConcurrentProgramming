import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;


public class TestOldConcurrentQueueDPLL {

	final private ThreadLocal<Integer> THREAD_ID = new ThreadLocal<Integer>(){
		final private AtomicInteger id = new AtomicInteger(0);

		protected Integer initialValue(){
			return id.getAndIncrement();
		}
	};
	// Set # of THREADS and QUEENS
	final static int THREADS = 3;
	int NumOfQueens = 7;
	Thread[] thread = new Thread[THREADS];

	// Personal queue for each thread
	static 	ArrayList<ConcurrentLinkedQueue<ArrayList<Integer>>> tasks = new ArrayList<ConcurrentLinkedQueue<ArrayList<Integer>>>(THREADS);

	// cnf, variable list, and totalt number of variables
	ArrayList<List<Integer>> cnf = new ArrayList<List<Integer>>();
	List<Integer> vars = new ArrayList<Integer>();
	int NumVars = NumOfQueens * NumOfQueens;

	// "answer" indicates when an answer is found
	// "firstIter" indicates when the first thread has finished the kickstart session
	volatile boolean answer = false;
	volatile boolean firstIter = false;

	// Construct DPLL
	OldConcurrentQueueDPLL test = new OldConcurrentQueueDPLL(NumOfQueens, NumVars);
	static long startTime;

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
			long endTime, totalTime;
			// Grab Thread ID
			int i = THREAD_ID.get();

			int evalResult = 0;
			boolean gotTask = false;

			// Threads must wait for first iteration
			while(firstIter == false && i != 0);

			// First iteration procedure for Thread 0
			if(i == 0)
			{
				ArrayList<Integer> past = new ArrayList<Integer>();
				ArrayList<List<Integer>> temp_cnf = new ArrayList<List<Integer>>();

				// Create formula, take a copy, create variable list, take a copy
				test.CreateFormula(cnf);  
				test.copy(cnf, temp_cnf); 
				test.CreateVarList(vars); 
				List<Integer> temp_vars = new ArrayList<Integer>(test.copyList(vars));
				// BCP, DPLL
				test.BCP(temp_vars, temp_cnf);
				if(test.DPLL_OPTO(temp_vars, temp_cnf, tasks.get(i), past)) // run first iteration
					answer = true;
				firstIter = true;

			}
			while(!answer)
			{
				// Create new copy of formula and variable list
				ArrayList<List<Integer>> temp_cnf = new ArrayList<List<Integer>>();
				test.copy(cnf,temp_cnf);
				List<Integer> temp_vars = new ArrayList<Integer>(test.copyList(vars));
				ArrayList<Integer> past = new ArrayList<Integer>();

				// Grab a task(past of a previous thread) from your queue, otherwise, grab a task from another threads queue
				past = tasks.get(i).poll();
				if(past == null && !answer)
				{
					while(!gotTask && !answer)
					{
						for(int k = 0; k < THREADS && !gotTask && !answer; k++)
						{
							if(k != i)
							{
								past = tasks.get(k).poll();
								if(past != null)
									gotTask = true;
							}
						}
					}
					gotTask = false;
				}

				//  Evaluate the task to obtain a reduced formula
				if(!answer)
					evalResult = test.EvalFormula(temp_cnf, past, temp_vars);
				// evalResult = 0: Satisfiability has not been determine, thread must complete the work
				// evalResult = 1: The evaluated result proves formula is satisfiable, print time
				// evalResult = x: The evaluated result proved to be unsatisfiable, grab more work from the task queues
				if( evalResult == 0 && !answer)
				{
					// BCP, DPLL
					test.BCP(temp_vars,temp_cnf);
					if(test.DPLL_OPTO(temp_vars,temp_cnf,tasks.get(i),past))
					{
						answer = true;
						endTime = System.currentTimeMillis();
						totalTime = (endTime - startTime);
						System.out.println("Finished in : " + totalTime);	
					}
				}
				else if(evalResult == 1)
				{
					answer = true;
					endTime = System.currentTimeMillis();
					totalTime = (endTime - startTime);
					System.out.println("Finished in : " + totalTime);
				}
			}
		}
	}
	// Main
	public static void main(String[] args)
	{	
		startTime = System.currentTimeMillis();
		TestOldConcurrentQueueDPLL mpt = new TestOldConcurrentQueueDPLL();

		// Initialize the List of Concurrent Queues
		for(int j = 0; j < THREADS ; j++)
		{
			ConcurrentLinkedQueue<ArrayList<Integer>> init = new ConcurrentLinkedQueue<ArrayList<Integer>>();
			tasks.add(init);
		}
		try {
			mpt.testParallel();
		}
		catch (Exception e) {}
	}


}
