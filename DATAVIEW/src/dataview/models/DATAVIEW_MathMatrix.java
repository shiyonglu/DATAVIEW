package dataview.models;
/* 
 * This MathMatrix is implemented based on the assumption that a lot of calculations and updates will be performed on this matrix.
 * 
 * Shiyong Lu
 * July 10, 2018
 *
 **/

import java.util.*;

public class DATAVIEW_MathMatrix {
		private int n; // number of columns, the number of features
		private int m; // number of rows, the number of examples
		private double [][] elements; // vector's elements

		public DATAVIEW_MathMatrix(int m, int n)
		{
			this.m =  m;
			this.n =  n;
			this.elements = new double[m][n];
		}

		public DATAVIEW_MathMatrix(double[][] elements) // we copy the data to avoid update side-effect from the input
		{
			this.m = elements.length;
			this.n = elements[0].length;
			this.elements = new double[m][n];
			
			for(int i=0; i<m; i++) {
				for(int j=0; j<n; j++)
				this.elements[i][j] = elements[i][j];
			}
		}
		
		public DATAVIEW_MathMatrix(String lines[])
		{
			
			this.m = lines.length;
			String[] words = lines[0].split(",");
			this.n= words.length;
			this.elements = new double[m][n];
			

			for(int i=0; i<m; i++) {
				words = lines[i].split(",");
				for(int j=0; j<n; j++)
					this.elements[i][j] = Double.valueOf(words[j]);
			}			
		}
		
		public DATAVIEW_MathMatrix(List<String> lines)
		{
			
			this.m = lines.size();
			String[] words = lines.get(0).split(",");
			this.n= words.length;
			this.elements = new double[m][n];
			

			for(int i=0; i<m; i++) {
				words = lines.get(i).split(",");
				for(int j=0; j<n; j++)
					this.elements[i][j] = Double.valueOf(words[j]);
			}			
		}

		
		public int getNumOfRows()
		{
		  return m;
		}
		
		public int getNumOfColumns()
		{
		  return n;
		}
		
		public double get(int i, int j) 
		{
			return elements[i][j];
					
		}
		
		public DATAVIEW_MathVector getRow(int i)
		{
			return new DATAVIEW_MathVector(elements[i]);
		}
		
		
		public void set(int i, int j, double val)
		{
			elements[i][j] = val;
		}
		
	    public void add(DATAVIEW_MathMatrix newmx)
	    {
	       if(n != newmx.getNumOfColumns() || m != newmx.getNumOfRows()) {
	    	   throw new IllegalArgumentException("Dimensions are not equal for the plus operation.");
	       }
	       
	       for(int i=0; i<m; i++) {
				for(int j=0; j<n; j++)
					this.elements[i][j] += newmx.get(i, j);
	       }  
	    } // end 
		
		public void add(double newv)
		{
			  for(int i=0; i<m; i++) {
					for(int j=0; j<n; j++)
						this.elements[i][j] += newv;
		       }
		}
		
		/* add a value to a particular element. */
		public void add(int i, int j, double newv)
		{
					this.elements[i][j] += newv;
		}		
		
		public void addRow(int row, double newv)
		{
			
			for(int j=0; j<n; j++)
				this.elements[row][j] += newv;
		}
		
		public void addRow(int row, DATAVIEW_MathVector newv)
		{
			Dataview.debugger.logFalseCondition("The new vector should have the same dimenstion of a a row in the matrix, newv.length == row.length", n == newv.length());
			for(int j=0; j<n; j++)
				this.elements[row][j] += newv.get(j);
		   
		}

		public void addColumn(int col, double newv)
		{
			for(int i=0; i<m; i++)
				this.elements[i][col] += newv;
		}				

	    public void subtract(DATAVIEW_MathMatrix newmx)
	    {
	       if(n != newmx.getNumOfColumns() || m != newmx.getNumOfRows()) {
	    	   throw new IllegalArgumentException("Dimensions are not equal for the plus operation.");
	       }
	       
	       for(int i=0; i<m; i++) {
				for(int j=0; j<n; j++)
					this.elements[i][j] -= newmx.get(i, j);
	       }  
	    } // end 
		
		public void subtract(double newv)
		{
			  for(int i=0; i<m; i++) {
					for(int j=0; j<n; j++)
						this.elements[i][j] -= newv;
		       }
		}
		
		/* subtract a value from a particular element. */
		public void subtract(int i, int j, double newv)
		{
					this.elements[i][j] -= newv;
		}		
		
		public void subtractRow(int row, double newv)
		{
			for(int j=0; j<n; j++)
				this.elements[row][j] -= newv;
		}

		public void subtractColumn(int col, double newv)
		{
			for(int i=0; i<m; i++)
				this.elements[i][col] -= newv;
		}				

	    public void multiply(DATAVIEW_MathMatrix newmx)
	    {
	       if(n != newmx.getNumOfColumns() || m != newmx.getNumOfRows()) {
	    	   throw new IllegalArgumentException("Dimensions are not equal for the plus operation.");
	       }
	       
	       for(int i=0; i<m; i++) {
				for(int j=0; j<n; j++)
					this.elements[i][j] *= newmx.get(i, j);
	       }  
	    } // end 
		
		public void multiply(double newv)
		{
			  for(int i=0; i<m; i++) {
					for(int j=0; j<n; j++)
						this.elements[i][j] *= newv;
		       }
		}
		
		/* multiply a value to a particular element. */
		public void multiply(int i, int j, double newv)
		{
					this.elements[i][j] *= newv;
		}		
		
		public void multiplyRow(int row, double newv)
		{
			for(int j=0; j<n; j++)
				this.elements[row][j] *= newv;
		}

		public void multiplyColumn(int col, double newv)
		{
			for(int i=0; i<m; i++)
				this.elements[i][col] *= newv;
		}				

	    public void div(DATAVIEW_MathMatrix newmx)
	    {
	       if(n != newmx.getNumOfColumns() || m != newmx.getNumOfRows()) {
	    	   throw new IllegalArgumentException("Dimensions are not equal for the plus operation.");
	       }
	       
	       for(int i=0; i<m; i++) {
				for(int j=0; j<n; j++)
					this.elements[i][j] /= newmx.get(i, j);
	       }  
	    } // end 
		
		public void div(double newv)
		{
			  for(int i=0; i<m; i++) {
					for(int j=0; j<n; j++)
						this.elements[i][j] /= newv;
		       }
		}
		
		/* divide a value from a particular element. */
		public void div(int i, int j, double newv)
		{
					this.elements[i][j] /= newv;
		}		
		
		public void divRow(int row, double newv)
		{
			for(int j=0; j<n; j++)
				this.elements[row][j] /= newv;
		}

		
		
		public void divColumn(int col, double newv)
		{
			for(int i=0; i<m; i++)
				this.elements[i][col] /= newv;
		}

		/* Given an input vector, which row vector is closest to the input vector? This method will return the 
		 * index of such a row.
		 */
		public int getClosestRow(DATAVIEW_MathVector p)
		{
			double mind = 0.0;
			int minrow = 0;
			
			// set minrow = 0; 
			for(int j=0; j<n; j++) {
				mind += (p.get(j)-elements[0][j])*(p.get(j)-elements[0][j]);
			}
			
			// compare to row 1 up to row m-1
			for(int i=1; i<m; i++) {
				double d = 0.0;
				for(int j=0; j<n; j++) {
					d += (p.get(j)-elements[i][j])*(p.get(j)-elements[i][j]);
				}
				if(d < mind ) { // found a closer row
					minrow = i;
					mind = d;
				}
			}
			
			// return the row index that has the minimum distance to the input vector
			return minrow;			
		}
		
		
		
	    @Override
	    public String toString() 
		{
			String str = "";
			
			for(int i = 0; i < m; i++) {
				str = str + elements[i][0];
				for(int j=1; j<n; j++)
					str += "," + elements[i][j];
				str = str + "\n";
			}
			
			return str;
		}		
}