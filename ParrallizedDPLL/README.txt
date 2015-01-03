HOW TO USE ALGORITHMS
--------------------------------------------------------------------------------------------------------------------------------------
All that you have to do is adjust the number of threads and number of queens to solve. The algorithms do not consider less than 4 queens
since they have no solution.

MUST READ NOTES!!!!!!!!!!!!
---------------------------------------------------------------------------------------------------------------------------------------

NOTE: If there are multiple threads running, they may print their "Finished:" time while another thread is printing a solution. Just remove
the printed finishing time and the solutions should remain intact. This is not a concern since only the first occurence of a solution is relevant.
You could always rerun it and it will resettle itself.

NOTE: It is possible that an exception would be thrown if there are too many threads and not enough work for the threads to do.
This only happens after a solution is found, so I let this be to save time.

NOTE: All the FINDALLSOLUTIONS algorithms find all 4 solutions for 6 queens. You can adjust this if you like, but the solutions will be
printed vertically. Reference the example runs, I have displayed nicely for you.