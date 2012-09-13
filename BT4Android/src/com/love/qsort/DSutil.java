package com.love.qsort;

import java.util.*;
import java.math.*;

class DSutil<E> {

	public static <E> void swap(E[] A, int p1, int p2) {
		E temp = A[p1];
		A[p1] = A[p2];
		A[p2] = temp;
	}

	/** Randomly permute the Objects in an array.
  @param A The array
	 */

	//int version
	//Randomly permute the values of array "A"
	static void permute(int[] A) {
		for (int i = A.length; i > 0; i--) // for each i
			swap(A, i-1, DSutil.random(i));  //   swap A[i-1] with
	}                                    //   a random element

	public static void swap(int[] A, int p1, int p2) {
		int temp = A[p1];
		A[p1] = A[p2];
		A[p2] = temp;
	}

	static <E> void permute(E[] A) {
		for (int i = A.length; i > 0; i--) // for each i
			swap(A, i-1, DSutil.random(i));  //   swap A[i-1] with
	}                                    //   a random element

	static void MinMax(int A[], int l, int r, int Out[]) {
		if (l == r) {        // n=1
			Out[0] = A[r];
			Out[1] = A[r];
		}
		else if (l+1 == r) { // n=2
			Out[0] = Math.min(A[l], A[r]);
			Out[1] = Math.max(A[l], A[r]);
		}
		else {               // n>2
			int[] Out1 = new int[2];
			int[] Out2 = new int[2];
			int mid = (l + r)/2;
			MinMax(A, l, mid, Out1);
			MinMax(A, mid+1, r, Out2);
			Out[0] = Math.min(Out1[0], Out2[0]);
			Out[1] = Math.max(Out1[1], Out2[1]);
		}
	}

	static private Random value = new Random(); // Hold the Random class object

	static int random(int n) {
		return Math.abs(value.nextInt()) % n;
	}

}