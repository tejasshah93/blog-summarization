// This callback function is called when the content script has been 
// injected and returned its results
function onPageInfo(o)  { 
    document.getElementById('title').innerText = o.title; 
    document.getElementById('url').value = o.url; 
    document.getElementById('summary').innerText = o.summary; 
} 

// Global reference to the status display SPAN
var statusDisplay = null;

// POST the data to the server using XMLHttpRequest
function getSummary() {
    // Cancel the form submit
    event.preventDefault();

    // The URL to POST our data to
    var postUrl = 'http://web.iiit.ac.in/~tejas.shah/scripting/chromeExtension/majorProject/summarize.php';

    // Set up an asynchronous AJAX POST request
    var xhr = new XMLHttpRequest();
    xhr.open('POST', postUrl, true);
    
    // Prepare the data to be POSTed
    var title = encodeURIComponent(document.getElementById('title').innerHTML);
    var url = encodeURIComponent(document.getElementById('url').value);
    var summary = encodeURIComponent(document.getElementById('summary').value);
    var tags = encodeURIComponent(document.getElementById('tags').value);

    var params = 'title=' + title + 
                 '&url=' + url + 
                 '&summary=' + summary +
                 '&tags=' + tags;
    
    // Replace any instances of the URLEncoded space char with +
    params = params.replace(/%20/g, '+');

    // Set correct header for form data 
    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');

    // Handle request state change events
    xhr.onreadystatechange = function() { 
        // If the request completed
        if (xhr.readyState == 4) {
            statusDisplay.innerHTML = '';
            if (xhr.status == 200) {
                // If it was a success, close the popup after a short delay
                
                summaryTextBox.innerHTML = xhr.responseText;                
                summaryTextBox.style.height = "";
                summaryTextBox.style.height = Math.min(summaryTextBox.scrollHeight, 300) + "px";
                getSummaryButton.value = 'Done';
                download.disabled = false;                

                //window.setTimeout(window.close, 1000);
            } else {// Show what went wrong
                statusDisplay.innerHTML = 'Error sending: ' + xhr.statusText;
            }
        }
    };

    // Send the request and set status
    xhr.send(params);
    // statusDisplay.innerHTML = 'Summarizing...';
    getSummaryButton.value = 'Summarizing..';
    downloadButton.value = 'Preparing for download';
}

// When the popup HTML has loaded
window.addEventListener('load', function(evt) {
    // Handle the summary form submit event with our getSummary function
    document.getElementById('getSummary').addEventListener('submit', getSummary);
    // Cache a reference to the status display SPAN
    getSummaryButton = document.getElementById("save");
    statusDisplay = document.getElementById('status-display');
    summaryTextBox = document.getElementById('summary');
    downloadButton = document.getElementById('download');
    // Call the getPageInfo function in the background page, injecting content_script.js 
    // into the current HTML page and passing in our onPageInfo function as the callback
    chrome.extension.getBackgroundPage().getPageInfo(onPageInfo);
});