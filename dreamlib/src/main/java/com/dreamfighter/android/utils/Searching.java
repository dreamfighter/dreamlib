package com.dreamfighter.android.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.dreamfighter.android.log.Logger;

public class Searching {
	 
	 public static List<Integer> binarySearchElemnt(int[] arr, int searchValue) {
       int result = binarySearch(arr, searchValue, 0, arr.length-1);
       Logger.log("binary result=>"+result);
	   List<Integer> resultList = new ArrayList<Integer>();
       if(result>-1){
    	   for(int i = result;i<arr.length;i++){
    	       Logger.log("binary arr[i]=>"+arr[i]);
    		   if(searchValue!=arr[i]){
    			   break;
    		   }
    		   resultList.add(i);
    	   }
    	   for(int i = result-1;i>=0;i--){
    	       Logger.log("binary arr[i]=>"+arr[i]);
    		   if(searchValue!=arr[i]){
    			   break;
    		   }
    		   resultList.add(0,i);
    	   }
       }
       return resultList;
	 }
	 
	 public static boolean binarySearch(int[] arr, int searchValue) {
        int result = binarySearch(arr, searchValue, 0, arr.length-1);
        Logger.log("binarySearch=>"+result);
        if(result!=-1){
        	return true;
        }
        return false;
	 }
	 
	 public static int binarySearch(int[] arr, int searchValue, int left, int right) {
         /*if (right < left) {
                 return -1;
         }
          
         int mid = mid = (left + right) / 2;
         There is a bug in the above line;
         Joshua Bloch suggests the following replacement:
         
         int mid = (left + right) >>> 1;
         if (searchValue > arr[mid]) {
                 return binarySearch(arr, searchValue, mid, right);
         } else if (searchValue < arr[mid]) {
                 return binarySearch(arr, searchValue, left, mid);
         } else {
                 return mid;
         }*/
		 return Arrays.binarySearch(arr, searchValue);
	 }
}
