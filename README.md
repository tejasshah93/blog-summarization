------
Comments Oriented Blog Summarization:
-------

A summarization technique for blogs like TechCrunch using its comments

Before runing make sure execute permission are given to run.sh `chmod +x run.sh`
To execute:

```
$ ./run.sh "URL-of-a-blog-here"
```

The argument passed as an URL can be from TechCrunch (current implementation)

e.g. `$ ./run.sh http://techcrunch.com/2014/03/20/google-wants-everyone-to-stop-hating-on-glass/`

Output would contain the summary of the blog in the 'Summary' file for the given URL input

Installation of the Chrome plugin : 

1. Open a new tab in the Google chrome.
2. Type -  `chrome://extensions/` in the address bar.
3. Drag and drop the extension CRX file to this window.

For development of Chrome Plugin:
1. Open a new tab in the Google chrome.
2. Type -  `chrome://extensions/` in the address bar.
3. Turn on developer mode
4. Click on `Load unpacked extension` and browse for the ChromeExtension folder as in the repository
