package com.love.qsort;

import java.util.*;

public class MyQsort {

	/**
	 * @param args
	 */

	private static int curr = 0;
	private static int prev = -1;
	private static Integer[] array;
	private static String steps = "";
	private static String indices = "";
	
	
	public static <E extends Comparable<? super E>>
	void qsort(E[] A, int i, int j, int minimum) {      // Quicksort
		
		int pivotindex = findpivot(A, i, j); // Pick a pivot
		DSutil.swap(A, pivotindex, j);       // Stick pivot at end
		// k will be the first position in the right subarray
		int k = partition(A, i-1, j, A[j]);
		
		steps = steps + "\n" +  Integer.toString(i) + " "+ Integer.toString(j) + " ";

		indices += Integer.toString(i) + Integer.toString(j);
		curr++;
		
		
		DSutil.swap(A, k, j);
		
		
//		for(int index = 0; index < 8 ; index++)
//		{
//			steps += A[index].toString();
//		}
//		steps += "\n";
	//	if(j > minimum || k > minimum) return;
		if ((k-i) > 1) qsort(A, i, k-1,minimum);
		if ((j-k) > 1) qsort(A, k+1, j,minimum);

	}

	private static void printArray() {
		// TODO Auto-generated method stub
		for(int i = 0; i < 8 ; i++)
		{
			System.out.print(array[i]);
		}
		System.out.println("   ");
		
	}

	static <E extends Comparable<? super E>>
	int partition(E[] A, int l, int r, E pivot) {
		do {                 // Move bounds inward until they meet
			while (A[++l].compareTo(pivot)<0);
			while ((r!=0) && (A[--r].compareTo(pivot)>0));

			DSutil.swap(A, l, r);
		} while (l < r);
		DSutil.swap(A, l, r);
		// Swap out-of-place values
		// Stop when they cross
		// Reverse last, wasted swap
		return l;
	}


	static <E extends Comparable<? super E>>
	int findpivot(E[] A, int i, int j)
	{ return (i+j)/2; }




	public static ArrayList<Integer> sortArrayList(ArrayList<Integer> list)
	{
		Integer[] test = new Integer[list.size()];
		list.toArray(test);
		MyQsort.qsort(test, 0, list.size() - 1, 0);
		list = new ArrayList<Integer>(Arrays.asList(test));
		return list;
	}



	private static void reset() {
		// TODO Auto-generated method stub
		prev = -1;
		curr = 0;
		steps = "";
		indices = "";
		
	}

	private static void setArray(Integer[] tempArray) {
		// TODO Auto-generated method stub
		array = tempArray;
		
	}
	
}


