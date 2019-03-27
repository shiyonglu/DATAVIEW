package dataview.models;

/**
 * 
 * 
 *
 */
public class Stage {
	public Task t;
	public Task [] tlist;
	public boolean isSingle;
	
	
	public Stage(Task t)
	{
		isSingle = true;
		this.t = t;
	}
	
	public Stage(Task [] tlist)
	{
		isSingle = false;
		this.tlist = tlist;
	}
	
	public Object get()
	{
		if(isSingle) return t;
		else return tlist;	
	}
	
    @Override
    public String toString() 
	{
		String str = "";
		if(isSingle) str = str + t.toString();
		else {
			str = str+tlist[0].toString();
			for(int i=1; i<tlist.length; i++)
				str = str + ", " + tlist[i].toString(); 
		}	
		
		return str;
	}	
}
