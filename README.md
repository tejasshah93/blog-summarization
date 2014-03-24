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

Output would contain the status of crawler and word extracted from the comments and their corresponding weight.
