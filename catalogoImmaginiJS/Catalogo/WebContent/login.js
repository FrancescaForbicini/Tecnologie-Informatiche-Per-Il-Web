/**
 * 
 */
 
(function(){
	document.getElementById("submit").addEventListener('click', (e) => {
	var form = e.target.closest("form");
	if (form.checkValidity()){
		makeCall("POST","CheckLogin",e.target.closest("form"),
		  function(req){
		  	if (req.readyState == XMLHttpRequest.DONE) {
            var message = req.responseText;
            switch (req.status) {
              case 200:
            	sessionStorage.setItem('username', message);
                window.location.href = "Home.html";
                break;
              case 400: // bad request
                document.getElementById("errorMsg").textContent = message;
                break;
              case 401: // unauthorized
                  document.getElementById("errorMsg").textContent = message;
                  break;
              case 500: // server error
            	document.getElementById("errorMsg").textContent = message;
                break;
            }
          }
        }
      );
    } else {
    	 form.reportValidity();
    }
  });

})();