package it.polimi.tiw.projects.controllers;



public class CategoryChanged {
	private int[] categoryPick;
	private int[] categoryDestination;
	
	public CategoryChanged() {}
	
	
	public int[] getCategoryPick(){
		return this.categoryPick;
	}
	public int[] getCategoryDestination(){
		return this.categoryDestination;
	}
	
	public void setCategoryPick(int[] categoryPick){
		this.categoryPick = categoryPick;
	}
	public void setCategoryDestination(int[] categoryDestination){
		this.categoryDestination = categoryDestination;
	}
	

}
