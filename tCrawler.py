from bs4 import BeautifulSoup
import urllib2, json
import os, errno
import sys 

# Function to check whether the path exists or not
def make_sure_path_exists(path):
    try:
        os.makedirs(path)
    except OSError as exception:
        if exception.errno != errno.EEXIST:
            raise

# List of TechCrunch URLs for crawling 
URLs=[sys.argv[1]
#"http://techcrunch.com/2012/01/09/61-percent-disqus-comments-pseudonyms/"#,
#"http://techcrunch.com/2014/03/20/google-wants-everyone-to-stop-hating-on-glass/"
]

# List of TechCrunch URLs for getting the comments using Facebook Graph API
graphURLs=[
"https://graph.facebook.com/comments/?limit=500&ids="+sys.argv[1]#	http://techcrunch.com/2012/01/09/61-percent-disqus-comments-pseudonyms/"#,
#"https://graph.facebook.com/comments/?limit=500&ids=http://techcrunch.com/2014/03/20/google-wants-everyone-to-stop-hating-on-glass/"
]

i = len(URLs)
j = 0
datasetPath = "./dataset"

print "Let the crawling begin!\nTotal no. of blogs = " + str(i)

while j < i:
	netDirPath = os.path.join(datasetPath, "Blog " + str(j+1))
	make_sure_path_exists(netDirPath)

	# blogFile contains all the blogs with title, body etc.
	netBlogPath = os.path.join(netDirPath, "blogDataset.json")
	if os.path.exists(netBlogPath):
		os.remove(netBlogPath)
	blogFile = open(netBlogPath, 'w')

	# commentsFile contains all the comments
	netCommentsPath = os.path.join(netDirPath, "commentsDataset.json")
	if os.path.exists(netCommentsPath):
		os.remove(netCommentsPath)
	commentsFile = open(netCommentsPath, 'w')

	blogFile.write('{\n\t"blog" : {\n')
	
	responseComments = urllib2.urlopen(graphURLs[j])
	dataComments = json.loads(responseComments.read())
	commentsFile.write((json.dumps(dataComments, indent = 2)))
	
	responseBlog = urllib2.urlopen(URLs[j])
	html = responseBlog.read()
	soup = BeautifulSoup(html)

	blogFile.write('\n\t\t\t"url": "' + URLs[j] + '",\n' + '\t\t\t"title": "' + soup.h1.contents[0] + '",\n')

	for links in soup.find_all('div'):
		if links.get('class')!=None:
		  if links.get('class')[0]=="title-left":
		  	dateAuthor = links.get_text()
			dateAuthor = dateAuthor.strip().split(' ')			
			
			indexBy = dateAuthor.index("by")
			k = 1
			blogDateParts = ""
			while k < indexBy:
				blogDateParts += dateAuthor[k] + " "
				k = k + 1
			blogDate = blogDateParts

			blogAuthorList = dateAuthor[indexBy + 2].split('\n')
			blogAuthorLastName = ""
			nextStr = 0
			while nextStr < len(blogAuthorList):				
				if len(blogAuthorList[nextStr]) != 0 and "More" not in blogAuthorList[nextStr]:
					blogAuthorLastName = blogAuthorList[nextStr]
				nextStr = nextStr + 1
			blogAuthor = dateAuthor[indexBy + 1]+ ' ' + blogAuthorLastName

			blogFile.write('\t\t\t"date" : "' + blogDate + '",\n')
			blogFile.write('\t\t\t"author" : "' + blogAuthor + '",\n')

	
	blogBody = ""
	for links in soup.find_all('p'):
			blogBodyPart = links.get_text().strip()
			if len(blogBodyPart) > 0 and blogBodyPart != "More" and not "Latest headlines delivered to you daily" in blogBodyPart:
				blogBody += blogBodyPart.encode('utf-8' + '\n')
	blogFile.write('\t\t\t"body" : "' + blogBody + '"\n')			
	blogFile.write('\t}\n}')

	blogFile.close()
	commentsFile.close()

	j = j + 1
	print "Blog " + str(j) + " done"

print "Cool. Done with Blogs and Comments!\nblogDataset.json => blog title, date, author, body\ncommentsDataset.json => comments from graph API"

print "\nOnto the replies part now..."


i=len(URLs)
j=0
path = "replies"
userSubscribers = "userSubscribers"

while j < i:
	print "Getting replies for all the comments in Blog " + str(j + 1) + " " + URLs[j]
	# Reading commentsDataset to get the replies for each comment
	netDirPath = os.path.join(datasetPath, "Blog " + str(j+1))
	netCommentsPath = os.path.join(netDirPath, "commentsDataset.json")
	commentsFile = open(netCommentsPath,'r')
	commentsJSON = json.load(commentsFile)

	# In "replies" folder, a separate folder for each blog will be created
	# which will contain replies of its respective comments with name as commentID
	commentsIDs = []
	userIDs = []
	numberOfComments = len(commentsJSON[URLs[j]]["comments"]["data"])
	for k in range(numberOfComments):
		commentsIDs.append(commentsJSON[URLs[j]]["comments"]["data"][k]["id"].encode('ascii'))
		if "from" in commentsJSON[URLs[j]]["comments"]["data"][k]:
			#print commentsJSON[URLs[j]]["comments"]["data"][k]["from"]["id"].encode('ascii')
			userIDs.append(commentsJSON[URLs[j]]["comments"]["data"][k]["from"]["id"].encode('ascii'))
		

	netReplyPath = os.path.join(netDirPath, path)
	if os.path.exists(netReplyPath):
		for the_file in os.listdir(netReplyPath):
		    file_path = os.path.join(netReplyPath, the_file)
		    try:
		        if os.path.isfile(file_path):
		            os.unlink(file_path)
		    except Exception, e:
		        print e
	make_sure_path_exists(netReplyPath)

	netUsersPath = os.path.join(netDirPath, userSubscribers)
	if os.path.exists(netUsersPath):
		for the_file in os.listdir(netUsersPath):
		    file_path = os.path.join(netUsersPath, the_file)
		    try:
		        if os.path.isfile(file_path):
		            os.unlink(file_path)
		    except Exception, e:
		        print e    
	make_sure_path_exists(netUsersPath)
	
	for commentsID in commentsIDs:
		# Creating a replies file for each comment with name as commentID		
		repliesFileName = 'replyFor-' + commentsID + '.json'
		netPathRepliesFile = os.path.join(netReplyPath, repliesFileName)

		if os.path.exists(netPathRepliesFile):
			os.remove(netPathRepliesFile)

		repliesFile = open((netPathRepliesFile), 'w')
		repliesURL = "https://graph.facebook.com/" + commentsID + "/comments/?limit=500"
		responseReplies = urllib2.urlopen(repliesURL)
		dataReplies = json.loads(responseReplies.read())
		repliesFile.write((json.dumps(dataReplies, indent = 2)))
		repliesFile.close()

		repliesFile = open(netPathRepliesFile,'r')
		repliesJSON = json.load(repliesFile)
		numberOfReplies = len(repliesJSON["data"])
		for k in range(numberOfReplies):
			if "from" in repliesJSON["data"][k]:
				#print repliesJSON["data"][k]["from"]["id"].encode('ascii')
				userIDs.append(repliesJSON["data"][k]["from"]["id"].encode('ascii'))
		repliesFile.close()

	print "Getting subscribers for all the users in Blog " + str(j + 1) + " " + URLs[j]
	for userID in userIDs:
		# Creating a replies file for each comment with name as commentID		
		userFileName = userID + '.json'
		netUserSubscribersFile = os.path.join(netUsersPath, userFileName)

		if os.path.exists(netUserSubscribersFile):
			os.remove(netUserSubscribersFile)

		userSubscribersFile = open((netUserSubscribersFile), 'w')
		#print "fetching subscribers for user " + userID
		userSubscribersURL = "https://graph.facebook.com/" + userID +  "/subscribers?access_token=CAAIq7L6i2AABALWkTAzc8FBdb99ZCf41airxD65xVTKts2wxpwYow81vYDMO3npWRbfo2fdJJisrWJ6NHLMc4eqhk8j2DUAmZAnJhClC7dwb5jwzWkvntZBonFsvTWc9YMmFY8zaNieZCrsAwlcX9DcNSjK5eeoXUZBr0OwTZBgMPFzQH2I6Hujxs8N2oKYw4ZD"

		requestSubscribers = urllib2.Request(userSubscribersURL)
		try:
		    responseSubscribers = urllib2.urlopen(requestSubscribers)
		    #print responseSubscribers
		    dataSubscribers = json.loads(responseSubscribers.read())
		    userSubscribersFile.write((json.dumps(dataSubscribers, indent = 2)))

		except urllib2.URLError, e:
		    if e:
		        #print "404"
		        userSubscribersFile.write('{\n\t"data": [],\n\t"summary": {\n\t\t "total_count": 0\n\t }\n}')
		userSubscribersFile.close()

	commentsFile.close()
	j = j + 1

print "Done! Check the 'replies' and 'userSubscribers' folder"