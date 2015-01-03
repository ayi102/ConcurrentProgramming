import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;


public class OldConcurrentQueueDPLL {

	private int N;
	private int totalVars;

	OldConcurrentQueueDPLL(int N, int totalVars)
	{
		//get number of queens
		this.N = N;
		this.totalVars = totalVars;

	}

	public boolean DPLL_OPTO(List<Integer>vars, ArrayList<List<Integer>> cnf, ConcurrentLinkedQueue<ArrayList<Integer>> tasks, ArrayList<Integer> past)
	{
		int choice;
		int guess;
		int position1;
		int position2;
		ArrayList<Integer> testEmpty = new ArrayList<Integer>();
		boolean containsEmpty = cnf.contains(testEmpty);
		ArrayList<Integer> initTasks = new ArrayList<Integer>();

		// If the cnf formula is empty, satisfiability has been achieved
		if(cnf.isEmpty())
		{
			//Print contents of guesses that contains history of guesses from recusive calls
			for(int i = 0; i < past.size();i++)
				System.out.println(past.get(i));
			return true;
		}
		else if(containsEmpty)
		{
			// Return false if an empty clause is found
			return false;
		}
		else
		{
			// If the past(task) this thread carried in is empty, then it must be the first thread
			// Attempting to do the kickstart session
			if(past.isEmpty())
			{
				// Add 0 to its queue of work
				initTasks.add(0);
				tasks.add(initTasks);
			}
			else
			{
				// Add 0 to threads past guesses and add to task queue
				// Then remove the 0 to maintain the guesses of this current thread
				past.add(0);
				ArrayList<Integer> addTask = new ArrayList<Integer>(copyList(past));
				tasks.add(addTask);
				past.remove(past.size() - 1);
			}
			// Extract the current variable
			choice = vars.get(0);

			// Remove variable from list of variables
			vars.remove(0);

			// By default, guess 1 for that variable
			guess = 1;

			// Add this guess to the history of guesses for this thread
			past.add(1);

			// Constantly check if an empty clause occurs
			if(!containsEmpty)
			{
				// Traverse through clauses and look for "choice"(variable from the list)
				// Lazyly remove clause if it contains "choice"
				// Lazyly remove remove "-choice" from clause if found
				for(int i = 0; i < cnf.size() && !containsEmpty; i++)
				{
					position1 = cnf.get(i).indexOf(-choice);
					position2 = cnf.get(i).indexOf(choice);
					if(position1 >= 0)
					{
						cnf.get(i).remove(position1);
						containsEmpty = cnf.get(i).isEmpty();
					}    
					if(position2 >= 0)
					{
						cnf.get(i).clear();
						cnf.get(i).add(0);
					}
				}
			}

			if(!containsEmpty)
			{
				//Physically delete logical deletes
				PhysicalDelete(cnf);
				BCP(vars,cnf);
			}
			return DPLL_OPTO(vars,cnf,tasks,past);
		}
	}
	// Boolean Constraint Propogation
	public void BCP(List<Integer>vars,ArrayList<List<Integer>> cnf)
	{
		List<Integer> temp_list1 = new ArrayList<Integer>();
		List<Integer> temp_list2 = new ArrayList<Integer>();
		List<Integer> zero = new ArrayList<Integer>();
		zero.add(0);

		int index;
		int index2;
		for(int i = 0; i < cnf.size(); i++)
		{
			// Extract first clause
			temp_list1 = cnf.get(i);
			// Ensure the clause is a literal and is not a lazy deleted clause
			if(temp_list1.size() == 1 && !temp_list1.equals(zero))
			{
				// Compare literal to every clause in the cnf
				// Eliminate clause if it contains the literal
				// Eliminate -literal if contained in clause
				for(int j = 0; j < cnf.size(); j++)
				{
					if(j != i)
					{
						temp_list2 = cnf.get(j);

						index = temp_list2.indexOf(-temp_list1.get(0));
						index2 = temp_list2.indexOf(temp_list1.get(0));
						if(index != -1)
						{
							cnf.get(cnf.indexOf(temp_list2)).remove(temp_list2.indexOf(-temp_list1.get(0)));						
						}
						if(index2 != -1)
						{
							cnf.set(cnf.indexOf(temp_list2),zero);
						}
					}
				}
			}
		}
		//Physically delete logical deletes
		PhysicalDelete(cnf);
	}
	// Evaluate Boolean formula based on a give past/task
	public int EvalFormula(ArrayList<List<Integer>> cnf,List<Integer> temp,List<Integer>vars)
	{
		int position1;
		int position2;
		boolean containsEmpty = false;

		for(int j = 0; j < temp.size() && !containsEmpty; j++)
		{
			// Extract the current variable
			int choice = vars.get(0);

			// Remove variable from list of variables
			vars.remove(0);

			// Constantly check if an empty clause occurs
			if(temp.get(j) == 1 && !containsEmpty)
			{
				// Traverse through clauses and look for "choice"(variable from the list)
				// Lazyly remove clause if it contains "choice"
				// Lazyly remove remove "-choice" from clause if found
				for(int i = 0; i < cnf.size() && !containsEmpty; i++)
				{
					position1 = cnf.get(i).indexOf(-choice);
					position2 = cnf.get(i).indexOf(choice);
					if(position1 >= 0)
					{
						cnf.get(i).remove(position1);
						containsEmpty = cnf.get(i).isEmpty();
					}

					if(position2 >= 0)
					{
						cnf.get(i).clear();
						cnf.get(i).add(0);
					}
				}
			}
			else if(!containsEmpty)
			{
				// Traverse through clauses and look for "-choice"(variable from the list)
				// Lazly remove clause if it contains "-choice"
				// Lazly remove "choice" from clause if found
				for(int i = 0; i < cnf.size() && !containsEmpty; i++)
				{
					position1 = cnf.get(i).indexOf(-choice);
					position2 = cnf.get(i).indexOf(choice);
					if(position1 >= 0)
					{
						cnf.get(i).clear();
						cnf.get(i).add(0);
					}	
					if(position2 >= 0)
					{
						cnf.get(i).remove(position2);
						containsEmpty = cnf.get(i).isEmpty();
					}
				}
			}
		}
		// Delete lazly removed clauses
		PhysicalDelete(cnf);

		// If the cnf has no clauses, then the formula is satisfiable, print the sequence
		// If the cnf contains an empty clause, then the formula is not satisfiable with the current sequence
		if(cnf.isEmpty())
		{
			for(int i = 0; i < temp.size();i++)
			{
				System.out.println(temp.get(i));
			}
			return 1;
		}
		else if(containsEmpty)
		{
			return -1;
		}
		return 0;
	}
	// Deep copy of ArrayList of Lists
	public void copy(ArrayList<List<Integer>>ref,ArrayList<List<Integer>>copy)
	{		
		for(int i = 0; i < ref.size(); i++)
		{
			copy.add(copyList(ref.get(i)));
		}
	}
	// Deep copy of an ArrayList
	List<Integer> copyList(List<Integer> list)
	{
		List<Integer> deepCopy = new ArrayList<Integer>();
		for(Integer obj : list)
		{
			deepCopy.add(new Integer(obj));
		}
		return deepCopy;
	}
	// Physical Delete of lazy deleted nodes
	public void PhysicalDelete(ArrayList<List<Integer>> temp)
	{
		int tempSize = temp.size();
		ArrayList<List<Integer>> cpy = new ArrayList<List<Integer>>();
		for(int i = 0; i < tempSize; i++)
		{
			if(!temp.get(i).contains(0))
			{
				cpy.add(temp.get(i));
			}
		}

		temp.retainAll(cpy);
	}

	// Create list of variables
	public void CreateVarList(List<Integer>vars)
	{
		for(int i = 0; i < totalVars; i++)
		{
			vars.add(i+1);
		}
	}

	// Create Boolean Formula for N-Queen Problem
	public void CreateFormula(List<List<Integer>> cnf)
	{
		//Produce Clauses where all values in row may exist
		int cnt = 0;
		for(int i = 0; i < N; i++)
		{
			List<Integer> vals = new ArrayList<Integer>();
			for(int j = 1; j <= N; j++)
			{
				vals.add(j+cnt);
			}
			cnf.add(vals);
			cnt+=N;
		}
		//Produce Clauses where only one queen exists on that one row 
		int tempSize = N;
		int rowCnt1 = 0;
		int rowCnt2 = 0;
		int tempVal1;
		int tempVal2;
		for(int i = 0; i < N; i++)
		{
			for(int j = 0; j < N - 1; j++)
			{

				tempVal1 = (cnf.get(i).get(j));
				rowCnt2 = rowCnt1 + 1;
				while(rowCnt2 < tempSize)
				{
					List<Integer> vals = new ArrayList<Integer>();
					tempVal2 = (cnf.get(i).get(rowCnt2));
					vals.add(-tempVal1);
					vals.add(-tempVal2);
					rowCnt2++;
					cnf.add(vals);

				}
				rowCnt1++;
			}
			rowCnt1 = 0;
			rowCnt2 = 0;
		}
		//Produce Clauses where only one queen exists on that one column
		int rowCnt3 = 1;
		for(int i = 0; i < N; i++)
		{
			for(int j = 0; j < N - 1; j++)
			{

				tempVal1 = (cnf.get(j).get(i));
				rowCnt2 = rowCnt1 + 1;
				while(rowCnt2 < tempSize)
				{
					List<Integer> vals = new ArrayList<Integer>();
					tempVal2 = (cnf.get(j + rowCnt3).get(i));
					vals.add(-tempVal1);
					vals.add(-tempVal2);
					rowCnt2++;
					cnf.add(vals);
					rowCnt3++;

				}
				rowCnt1++;
				rowCnt3 = 1;
			}
			rowCnt1 = 0;
			rowCnt2 = 0;
		}

		//Produce Clauses where only one queen exists per right diagonals
		rowCnt3 = 1;
		for(int i = 0; i < N - 1; i++)
		{
			for(int j = 0; j < N - 1; j++)
			{
				tempVal1 = (cnf.get(i).get(j));
				while(j + rowCnt3 <= (tempSize - 1) && rowCnt3 + i <= (tempSize - 1))
				{
					List<Integer> vals = new ArrayList<Integer>();
					tempVal2 = (cnf.get(rowCnt3 + i).get(j + rowCnt3));
					vals.add(-tempVal1);
					vals.add(-tempVal2);
					cnf.add(vals);
					rowCnt3++;

				}
				rowCnt3 = 1;
			}
		}

		//Produce Clauses where only one queen exists per right diagonals
		rowCnt3 = 1;
		for(int i = 0; i < N - 1; i++)
		{
			for(int j = 1; j < N; j++)
			{
				tempVal1 = (cnf.get(i).get(j));
				while(j - rowCnt3 >= 0 && rowCnt3 + i <= (tempSize - 1))
				{
					List<Integer> vals = new ArrayList<Integer>();
					tempVal2 = (cnf.get(rowCnt3 + i).get(j - rowCnt3));
					vals.add(-tempVal1);
					vals.add(-tempVal2);
					cnf.add(vals);
					rowCnt3++;

				}
				rowCnt3 = 1;
			}
		}

	}
}
