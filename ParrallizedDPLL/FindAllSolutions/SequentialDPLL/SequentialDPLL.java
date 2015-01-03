
import java.util.ArrayList;
import java.util.List;

public class SequentialDPLL{
	
	// This value indicates when all solutions have been found
	// The reason this 2x the total number of solutions is discussed
	// in the paper
	int DOUBLE_TOTAL_SOLUTIONS = 8;
	int global_cnt = 0;
	int N; 
	int totalVars;

	// Constructor
	SequentialDPLL(int N)
	{
		//get number of queens
		this.N = N;
		this.totalVars = N * N;

	}
	// Create a list of variables
	public void CreateVarList(List<Integer>vars)
	{
		for(int i = 0; i < totalVars; i++)
		{
			vars.add(i+1);
		}
	}

	public void CreateFormula(ArrayList<List<Integer>> cnf)
	{
		// Produce Clauses where all values in row may exist
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
		// Produce Clauses where only one queen exists on that one row 
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
		// Produce Clauses where only one queen exists on that one column
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

		// Produce Clauses where only one queen exists per right diagonals
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

		// Produce Clauses where only one queen exists per left diagonals
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
	// DPLL SAT SOLVER
	public boolean DPLL_OPTO(List<Integer>vars, ArrayList<List<Integer>> cnf, int guess, List<Integer>guesses)
	{
		int choice;
		int position1;
		int position2;
		List<Integer> testEmpty = new ArrayList<Integer>();
		List<Integer> temp_guesses = new ArrayList<Integer>(copyList(guesses));
		List<Integer> tempVars = new ArrayList<Integer>(copyList(vars));
		boolean containsEmpty = cnf.contains(testEmpty);

		ArrayList<List<Integer>> temp_cnf = new ArrayList<List<Integer>>();

		// If the cnf formula is empty, satisfiability has been achieved
		if(cnf.isEmpty())
		{
			if( global_cnt % 2 == 0)
			{
				//Print Contents of guesses that contains history of guesses from recusive calls
				for(int i = 0; i < temp_guesses.size();i++)
				{
					System.out.println(temp_guesses.get(i));
				}
			}

			// Keep count of # of times a solution has been found
			// When they have all been found, return true
			global_cnt++;
			if(global_cnt == DOUBLE_TOTAL_SOLUTIONS)
				return true;
			else
				return false;
		}
		else if(containsEmpty)
		{
			// Return false if an empty clause is found
			return false;
		}
		else
		{
			copy(cnf,temp_cnf);
	
			// Store the guess for the current variable
			temp_guesses.add(guess);

			// Extract the current variable
			choice = tempVars.get(0);

			// Remove variable from list of variables
			tempVars.remove(0);

			// Constantly check if an empty clause occurs
			if(guess == 1 && !containsEmpty)
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
						temp_cnf.get(i).remove(position1);
						containsEmpty = temp_cnf.get(i).isEmpty();
					}	
					if(position2 >= 0)
					{
						temp_cnf.get(i).clear();
						temp_cnf.get(i).add(0);
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
						temp_cnf.get(i).clear();
						temp_cnf.get(i).add(0);
					}	
					if(position2 >= 0)
					{
						temp_cnf.get(i).remove(position2);
						containsEmpty = temp_cnf.get(i).isEmpty();
					}

				}
			}
			// Delete lazly removed clauses
			PhysicalDelete(temp_cnf);
			// BCP
			BCP(tempVars,temp_cnf);          
			return (DPLL_OPTO(tempVars,temp_cnf,1,temp_guesses) || DPLL_OPTO(tempVars,temp_cnf,0,temp_guesses));
		}
	}

	public void PhysicalDelete(ArrayList<List<Integer>> temp)
	{
		int tempSize = temp.size();
		ArrayList<List<Integer>> cpy = new ArrayList<List<Integer>>();
		// Traverse cnf formula and physically remove clauses with [0]
		for(int i = 0; i < tempSize; i++)
		{
			if(!temp.get(i).contains(0))
			{
				cpy.add(temp.get(i));
			}
		}
		temp.retainAll(cpy);
	}
	// Take a Deep copy of cnf formula
	public void copy(ArrayList<List<Integer>>ref,ArrayList<List<Integer>>copy)
	{		
		for(int i = 0; i < ref.size(); i++)
		{
			copy.add(copyList(ref.get(i)));
		}
	}
	// Take a deep copy of a clause
	List<Integer> copyList(List<Integer> list)
	{
		List<Integer> deepCopy = new ArrayList<Integer>();
		for(Integer obj : list)
		{
			deepCopy.add(new Integer(obj));
		}
		return deepCopy;
	}

}

