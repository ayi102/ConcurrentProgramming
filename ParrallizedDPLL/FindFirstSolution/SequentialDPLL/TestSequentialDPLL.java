import java.util.ArrayList;
import java.util.List;

public class TestSequentialDPLL {

	public static void main(String[] args)
	{
		int QUEENS = 4;
		boolean answer;

		// Start Timer
		long startTime = System.currentTimeMillis();

		// Generate lists to store cnf, list of variables and list of guesses per variable
		SequentialDPLL test = new SequentialDPLL(QUEENS);
		ArrayList<List<Integer>> cnf = new ArrayList<List<Integer>>();
		ArrayList<Integer> vars = new ArrayList<Integer>();
		List<Integer> guesses = new ArrayList<Integer>();


		test.CreateFormula(cnf);
		test.CreateVarList(vars);
		test.BCP(vars, cnf);
		answer = (test.DPLL_OPTO(vars, cnf, 1,guesses) || test.DPLL_OPTO(vars, cnf, 0,guesses));

		// Stop Timer
		long endTime = System.currentTimeMillis();

		// Print Time
		System.out.println("Finished in: " + (endTime - startTime));
	}

}
