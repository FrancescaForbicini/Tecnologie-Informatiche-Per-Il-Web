{
	let categoryList=new Category(document.getElementById("categoryList"),
										document.getElementById("category"));
										
	let categoryPick = [];
	let categoryDestination = [];
    let categoryBody;
	let allCategory;
	let categoryToAssign;
	let categoryToMove;
	let categoryNoDroppable = [];
	let childrenToAssign = [];
	let startElement;
	let element;
	let firstdrop = 0;
	let primaryCategory;

	window.addEventListener("load",()=>{
		if (sessionStorage.getItem("username") == null)
			window.location.href = "index.html";
		else
			categoryList.show();
		
		},false);

	document.getElementById("Logout").addEventListener('click', (e) => logout(e));
	document.getElementById("submit").addEventListener('click', (e) => createCategory(e));
	document.getElementById("SaveCategory").hidden = true;

	/**
	 * Arrays to put into the json to save all changes
	 */
	function CategoryMoved(){
		this.categoryPick = categoryPick;
		this.categoryDestination = categoryDestination;
	}


	function logout(event) {
		let choice = true;
		if (firstdrop == 1)
			choice = window.confirm("Are you sure? All the changes will be lost");
		if (choice){
	        makeCall("GET", "Logout",null,
        		function(req) {
				let message = req.responseText;
				switch (req.status) {
              		case 200:
            			sessionStorage.setItem(null, message);
                		window.location.href = "index.html";
                		break;
              		case 400: // bad request
                		document.getElementById("errorMsg").textContent = message;
                		break;
				}
				}
     		);	
		}
	}

	function saveCategory(e){
		let categoryChanged = new CategoryMoved();
		let categoryJSON = JSON.stringify(categoryChanged);
		if (categoryJSON != null  && firstdrop === 1){		
			makeCallJSON("POST","SaveCategory",categoryJSON,
				function(req){
					if (req.readyState == XMLHttpRequest.DONE) {
						var message = req.responseText;
						if (req.status == 200) {
							alert("All changes are saved!");
						} 
						else{
							if (message == "")
								message = "Error";
							alert(message);
						} 	
					}  
				}
			)
			categoryPick = [];
			categoryDestination = [];
			firstdrop = 0;
			categoryList.show();
		}
	}
		
	function createCategory(e){
		let insertCategoryForm = e.target.closest("form");
		if (insertCategoryForm.checkValidity()){
			makeCall("POST", "InsertCategory", insertCategoryForm,
				function(req) {
					if (req.readyState == XMLHttpRequest.DONE) {
						var message = req.responseText;
						if (req.status == 200) {
							categoryList.show();
							} else{
							if (message == "")
								message = "Error";
							alert(message);
						}
					}
				}
			);
		}
		else
			alert("Something goes wrong");
	}

	function Category(category_list,category_body){
		this.categoryList = category_list;
		this.categoryBody = category_body;
		this.show = function() {
	     var self = this;
	      makeCall("GET", "Home", null,
	        function(req) {
	        //request is completed
	          if (req.readyState == 4) {
	            var message = req.responseText;
	            if (req.status == 200) {
	              var categoryToShow = JSON.parse(req.responseText);
				if (categoryToShow.length == 0) {
	                alert("There is not category to show");
					return;
	              }
	              self.update(categoryToShow);
	          } else{ 
						if (message == "")
							message = "Error";
						alert(message);
					}
              }
	        }
	      );
	    };
		
	    this.update = function(arrayCategory) {
			let categoryUpdated = [];
			setCategory(categoryUpdated,arrayCategory);
			var elem, i, row, IDcell, nameCell;
			// build updated list
			var self = this;
			this.categoryBody.innerHTML="";
			categoryUpdated.forEach(function(category) { // self visible here, not this
				row = document.createElement("tr");
				row.className  = "draggable";
	        	IDcell = document.createElement("td");
	        	IDcell.textContent = category.ID;
	        	IDcell.setAttribute('ID', category.ID); // set a custom HTML attribute
	        	IDcell.href = "#";
	        	row.appendChild(IDcell);
	        	nameCell = document.createElement("td");
	        	nameCell.textContent = category.categoryName;
	        	nameCell.setAttribute('name',category.categoryName);
	        	row.appendChild(nameCell);
	        	self.categoryBody.appendChild(row);
	    	});
			allCategory = categoryUpdated;
			this.categoryList.style.visibility = "visible";
			makeCategoryDraggable();
			categoryNoDroppable = [];
		};
		
	}
	
	
	function setCategory(categoryUpdated,arrayCategory) {
		arrayCategory.forEach(function(child){
			categoryUpdated.push(child);
			if (child.subCategory.length > 0){
				setCategory(categoryUpdated,child.subCategory);
			}
			else return;
		});
	}
	
	function makeCategoryDraggable(){
		let elements = document.getElementsByClassName("draggable");
		for (let i = elements.length - 1; i >= 0; i--) {
		  elements[i].draggable = true;
		  elements[i].addEventListener("dragstart", dragStart); //save dragged element reference
		  elements[i].addEventListener("dragover", dragover); // change color of reference element to red
		  elements[i].addEventListener("dragleave", dragLeave); // change color of reference element to black
		  elements[i].addEventListener("drop", drop); //change position of dragged element using the referenced element
		  elements[i].addEventListener("dragend", dragEnd);
	}

	function findCategory(listOfCategories,categoryToFind){
		listOfCategories.forEach(function(cat){
            	if(cat.ID === categoryToFind){
                	categoryToAssign = cat;
                	return ;
            	}
			})
	}

	function findPrimaryCategory (listOfCategories,child){
		listOfCategories.forEach(function(cat){
			if (cat.subCategory.length > 0){
					for (let i = 0; i < cat.subCategory.length; i++){
						if (cat.subCategory[i] === child){
							primaryCategory = cat;
							return;
						}
						if (cat.subCategory[i].subCategory.length > 0 )
							findPrimaryCategory(cat.subCategory[i].subCategory,child);
					}
			}
		})
	}

	function findSubCategory(father){
		father.subCategory.forEach(function(children){
			childrenToAssign.push(children);
			if (children.subCategory.length>0)
				findSubCategory(children);
		})

	}
	
	function dragEnd(event){
		elements=document.getElementsByClassName("nodraggable");
		let n= elements.length;
		for(let i=0; i<n; i++){
				elements[0].className="draggable";
		}
	}


	function dragStart(event) {
        startElement = event.target.closest("tr");
		let IDToMove = parseInt(startElement.firstElementChild.id);
		findCategory(allCategory,IDToMove);
        categoryToMove = categoryToAssign;
        let lengthSubCategory = categoryToMove.subCategory.length;
		if(lengthSubCategory > 0){
			childrenToAssign = [];
		    findSubCategory(categoryToMove);
			categoryNoDroppable = childrenToAssign;
		}
		categoryNoDroppable.push(categoryToMove);
		if (!categoryToMove.primaryCategory){
			findPrimaryCategory(allCategory,categoryToMove);
			categoryNoDroppable.push(primaryCategory);
		}
		elements=document.getElementsByClassName("draggable");
		for(let i=0; i<categoryNoDroppable.length; i++){
			for(let j=0; j<elements.length; j++){
				if(categoryNoDroppable[i].ID=== parseInt(elements[j].firstElementChild.id)){
					elements[j].className="nodraggable";
					j--;
				}
			}
		}
    }

	function dragover(event){
        event.preventDefault();
        element = event.target.closest("tr");
        if(element.className!= "nodraggable"){
        	element.className = "selected";
        }
    }

    function dragLeave(event){
        element = event.target.closest("tr");
        if(element.className!= "nodraggable"){
        	element.className = "notselected";
        }
    }


    function unselectRows(rowsArray) {
        for (var i = 0; i < rowsArray.length; i++) {
            rowsArray[i].className = "draggable";
        }
    }
    
    function drop(event) {
		let destinationElement = event.target.closest("tr");
        let table = destinationElement.closest('table'); 
        let rowsArray = Array.from(table.querySelectorAll('tbody > tr'));
		findCategory(allCategory, parseInt(destinationElement.firstElementChild.id));
		if (categoryToAssign.subCategory.length == 9){
			alert("You can't move this category into this category because it has alreadey 9 subcategories");
			unselectRows(rowsArray);
		}
		else{
			if (categoryNoDroppable.includes(categoryToAssign)){
				alert("You can't move this category into its subcategory");
				unselectRows(rowsArray);
			}
			else{
        		if  (window.confirm("Are you sure?")){
					firstdrop = 1;
        			let indexDest = rowsArray.indexOf(destinationElement);
        			let indexStart= rowsArray.indexOf(startElement);	
        			if (indexStart < indexDest)
						startElement.parentElement.insertBefore(startElement, rowsArray[indexDest + 1]);
        			else
						startElement.parentElement.insertBefore(startElement, rowsArray[indexDest + 1]);
        			unselectRows(rowsArray);
        			moveCategory(categoryToMove, destinationElement);        
				}		
			}
		if (firstdrop == 1 ){
			document.getElementById("SaveCategory").hidden = false;
			document.getElementById("SaveCategory").addEventListener('click', (e) => saveCategory(e));
		}
		categoryNoDroppable = [];
    }
}
    
	function moveCategory(categoryToMove,finalCategory){
		let lastID;
		this.categoryToMove = categoryToMove;
		categoryPick.push(categoryToMove.ID);
		this.categoryChildrenToMove= categoryToMove.subCategory;
		let IDDestination = parseInt(finalCategory.firstElementChild.id);
		findCategory(allCategory,IDDestination);
		finalCategory = categoryToAssign;
		categoryDestination.push(finalCategory.ID);
		let indexOfCategoryToMove;
		let indexOfDestination;
		if (!categoryToMove.primaryCategory){
			findPrimaryCategory(allCategory,categoryToMove);
			indexOfCategoryToMove = allCategory.indexOf(primaryCategory);
			allCategory.splice(indexOfCategoryToMove,1);
			indexOfCategoryToMove = primaryCategory.subCategory.indexOf(categoryToMove);
			primaryCategory.subCategory.splice(indexOfCategoryToMove,1);
			updateCategoryID(primaryCategory.subCategory,categoryToMove.ID);
			allCategory.push(primaryCategory);
		}
		else{
		    indexOfCategoryToMove= allCategory.indexOf(categoryToMove);
			allCategory.splice(indexOfCategoryToMove,1);
		}
		if (finalCategory.subCategory.length > 0 )
		    lastID = finalCategory.ID*10+finalCategory.subCategory.length+1;
		else
		    lastID = finalCategory.ID*10 + 1;
		categoryToMove.ID = lastID;
		if (categoryToMove.subCategory.length != 0){
			moveSubCategory(categoryToMove);
		}		
		categoryToMove.primaryCategory = false;
		indexOfDestination = allCategory.indexOf(finalCategory);
		finalCategory.subCategory.push(categoryToMove);
		allCategory[indexOfDestination] = finalCategory;
		cleanCategory(allCategory);
		categoryList.update(allCategory);
	}

	function moveSubCategory(father){
		let childCategory= father.subCategory;
		for (let i = 0; i < childCategory.length; i++){
			childCategory[i].ID = father.ID*10+i+1;
			if (childCategory[i].subCategory.length > 0 )
				moveSubCategory(childCategory[i]);
		}		
	}

	function updateCategoryID(arrayCategory,IDMoved){
		arrayCategory.forEach(function(category){
			if (category.ID> IDMoved){
				category.ID -= 1;
				if (category.subCategory.length > 0)
					updateSubCategoryID(category.subCategory,category.ID);
				else
					return;
			}
		});
	}

	function updateSubCategoryID(arrayCategory,IDfather){
		for (let i = 0; i < arrayCategory.length; i++){
			arrayCategory[i].ID = IDfather*10+i+1;
			if (arrayCategory[i].subCategory.length != 0)
				updateCategoryID(arrayCategory[i].subCategory,arrayCategory[i].ID);
		}
	}

	function cleanCategory(arrayCategory){
		let index;
		let categoryToRemove = [];
		arrayCategory.forEach(function(category){
			console.log(category);
			if (!category.primaryCategory){
				categoryToRemove.push(category);
			}
		});	
		categoryToRemove.forEach(function(category){
			index = allCategory.indexOf(category);
			allCategory.splice(index,1);
		});
	}
}
};