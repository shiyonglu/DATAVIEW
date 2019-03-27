package dataview.models;
import java.util.Vector;


/* This MathVector is implemented based on the assumption that a lot of calculations and updates will be performed on this vector.
 */

public class DATAVIEW_MathVector {
	private final int n;    // number of dimensions of the vector
	private double [] elements; // vector's elements

	public DATAVIEW_MathVector(int n)
	{
		this.n= n;
		this.elements = new double[n];
	}

	public DATAVIEW_MathVector(double[] elements) // we copy the data to avoid update side-effect from the intput
	{
		this.n= elements.length;
		this.elements = new double[this.n];
	
		for(int i=0; i<n; i++) {
			this.elements[i] = elements[i];
		}
	}
	
	public DATAVIEW_MathVector(String vecstr){
		
		String[] words = vecstr.split(",");
		this.n= words.length;
		this.elements = new double[this.n];

		for(int i=0; i<n; i++) {
			this.elements[i] = Double.valueOf(words[i]);
		}	
	}
			
	
	
	public int length()
	{
		return n;
	}
	
	public double get(int i)
	{
		return elements[i];
	}
	
	public void set(int i, double v)
	{
		elements[i] = v;
	}
	
	
    public void add(DATAVIEW_MathVector newv)
    {
       if(n != newv.length()) {
    	   throw new IllegalArgumentException("Dimensions are not equal for the plus operation.");
       }
       
       for(int i=0; i<=n; i++) {
    	   elements[i] += newv.get(i);
       } // end for       
    } // end 
	
    public void add(int i, double val) 
    {    	
    	elements[i] += val;
    }
    
    public void add(double val) 
    {    	
    	for(int i=0; i<n; i++)
    		elements[i] += val;
    }

    
    public void sutract(int i, double val)
    {
    	elements[i] -= val;
    }

    public void subtract(double val) 
    {    	
    	for(int i=0; i<n; i++)
    		elements[i] -= val;
    }
    
    public void multiply(int i, double val)
    {
    	elements[i] *= val;
    }

    public void multiply(double val) 
    {    	
    	for(int i=0; i<n; i++)
    		elements[i] *= val;
    }
    
    public void divide(int i, double val)
    {
    	elements[i] /= val;
    }

    public void divide(double val) 
    {    	
    	for(int i=0; i<n; i++)
    		elements[i] /= val;
    }   
    
    @Override
    public String toString() 
	{
		String str = this.elements[0]+"";
		
		for(int i = 1; i < n; i++) {
			str = str + ", " + this.elements[i];
		}
		
		return str;
	}

}
