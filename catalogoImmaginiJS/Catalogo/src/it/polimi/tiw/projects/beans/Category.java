package it.polimi.tiw.projects.beans;

import java.util.ArrayList;

public class Category {
	private int ID;
	private String categoryName;
	private ArrayList <Category> subCategory = new ArrayList<>(); 
	private boolean primaryCategory = false;
	private boolean isSelected= false;
	private boolean isDestination = false;
	
	public int getID() {
		return this.ID;
	}
	
	public String getcategoryName() {
		return this.categoryName;
	}
	
	public boolean getprimaryCategory() {
		return this.primaryCategory;
	}
	
	public ArrayList<Category> getsubCategory(){
		return this.subCategory;
	}
	
	public void setID(int ID) {
		this.ID = ID;
	}
	
	public void setcategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	
	public void setsubCategory(ArrayList<Category> subCategory) {
		this.subCategory = subCategory;
	}
	
	public void setprimaryCategory(boolean primaryCategory) {
		this.primaryCategory = primaryCategory;
	}
	
	public void setIsSelected(int isSelected) {
		if(isSelected==0)
			this.isSelected=false;
		else if (isSelected==1)
			this.isSelected=true;
	}
	
	public boolean isSelected() {
		return isSelected;
	}
	public void setIsDestination(int isDestination) {
		if(isDestination==0)
			this.isDestination=false;
		else if (isDestination==1)
			this.isDestination=true;
	}
	
	public boolean isDestination() {
		return isDestination;
	}
	
}
