import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;


public class quicksort extends Thread{
	
	private static int THREADS_AVAILABLE = 1;
	
	public List<Integer> sort(List<Integer> array) {
		// create the lists for elements greater and less than the pivot
		List<Integer> less = new ArrayList<Integer>(), more = new ArrayList<Integer>();
		
		// save size of the array in local variable
		int size = array.size();
		
		// if array is of length 0 or 1, nothing to sort
		if (size<=1) return array;
		
		// choose pivot from the middle of the array and remove it
		int pivot =  array.get(size/2);
		array.remove(Integer.valueOf(pivot));
		
		// divide list in elements greater and smaller than pivot
		for(Integer x : array) {
			if(x<=pivot) less.add(x);
			else more.add(x);
		}
		
		// begin the recursion
		if (THREADS_AVAILABLE > 1) {
	        THREADS_AVAILABLE--;
			return concat_r(less,pivot,more);
		}
		else
			return concat(sort(less),pivot,sort(more));
	}
	
	
	// function created to concatenate the merged lists of lesser and greater numbers
	public List<Integer> concat(List<Integer> less, int pivot, List<Integer> more) {
		// create the list that will hold the sorted array
		List<Integer> sorted = new ArrayList<Integer>();
		
		// fill the list with the numbers in the right order
		sorted.addAll(less);
		sorted.add(pivot);
		sorted.addAll(more);
		
		// return the sorted list
		return sorted;
	}
	
	// function that sorts in parallel and then concatenates the resulting lists
	public List<Integer> concat_r(final List<Integer> array1, int pivot, final List<Integer> array2) {
		final List<Integer> left = new ArrayList<Integer>(), right = new ArrayList<Integer>();
		
		//
		ExecutorService executor1 = java.util.concurrent.Executors.newSingleThreadExecutor();
        executor1.execute(new Runnable() { public void run() {
    	   left.addAll(sort(array1));
        }});
        executor1.shutdown();
        
        ExecutorService executor2 = java.util.concurrent.Executors.newSingleThreadExecutor();
        executor2.execute(new Runnable() { public void run() {
    	   right.addAll(sort(array2));
        }});
        executor2.shutdown();
        
        try {
        executor1.awaitTermination(10000, TimeUnit.MILLISECONDS);
        executor2.awaitTermination(10000, TimeUnit.MILLISECONDS);
        } catch (Exception e) { e.printStackTrace(); }
        
        THREADS_AVAILABLE++;
		return concat(left,pivot,right);
	}
	
	// this parses the input and outputs the list of numbers to be sorted
	public static List<Integer> parse(String[] args) {
		// flags used to parse the arguments
		boolean file = false, numbers = false, thread = false;
		
		// create list to hold the unsorted numbers
		List<Integer> unsorted = new ArrayList<Integer>();
		
		if (args.length == 0) printHelp(0);
		// parse the arguments
		for (String x : args) {
			
			if (x.equals("-h") || x.equals("--help"))
				printHelp(0);
			
			else if (x.equals("-f") || x.equals("--file"))
				file = true;
			
			else if (x.equals("-n") || x.equals("--numbers"))
				numbers = true;
			
			else if (x.equals("-t") || x.equals("--threads"))
				thread = true;
			
			else if (x.startsWith("-"))		// unsupported option
				printHelp(4);
			
			else if(file) try {
				file = false;
				// read the numbers from a file and place them in the list
				BufferedReader br = new BufferedReader(new FileReader(x));
				String line = "";
				while((line = br.readLine()) !=null) {
					for (String s : line.split(" "))
						unsorted.add(Integer.valueOf(s));
				}
			} catch (IOException e) {
				printHelp(1);
			}
			
			else if(numbers) try {
				unsorted.add(Integer.parseInt(x));
			} catch (NumberFormatException e) {
				printHelp(2);
			}
			
			else if(thread) try {
				thread = false;
				THREADS_AVAILABLE = Integer.parseInt(x);
			} catch (NumberFormatException e) {
				printHelp(2);
			}
		}
		return unsorted;
	}
		
		
	public static void printHelp(int err) {	
		System.out.println();
		
		switch(err) {
		case 1: System.out.println("Error reading file");
				break;
		case 2: System.out.println("Error parsing numbers");
				break;
		case 3: System.out.println("Error parsing threads");
				break;
		case 4: System.out.println("Error unknown parameter");
				break;
		}
		System.out.println("java -jar quicksort.jar [-t threads] [-f file] [-n num1 num2 num3...]");
		System.out.println();
		System.out.println("Usage:");
		System.out.println("-t or --threads:	must be followed by number of threads(processes) to be used");
		System.out.println("-f or --file:		must be followed by file containing list of numbers");
		System.out.println("-n or --numbers:	must be followed by list of numbers separated by spaces");
		System.out.println("-h or --help:		displays this help");
		System.exit(0);

	}
	public static void main(String[] inargs) {
		// get the list of unsorted numbers from input
		List<Integer> unsorted = parse(inargs);
		
		long time = System.currentTimeMillis();		// begin timer
		
		// sort numbers
		quicksort q = new quicksort();
		List<Integer> sorted = q.sort(unsorted);
		
		time = System.currentTimeMillis() - time;	// stop timer
		
		// print results
//		System.out.println("Unsorted numbers: " + unsorted);
		System.out.println("Sorted numbers: " + sorted);
		System.out.println("Time elapsed: " + time);
		System.out.println("Elements sorted: " + sorted.size());
	}
}
